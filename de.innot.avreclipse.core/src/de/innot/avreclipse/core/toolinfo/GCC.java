/*******************************************************************************
 * 
 * Copyright (c) 2008 Thomas Holland (thomas@innot.de) and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 * $Id$
 *     
 *******************************************************************************/
package de.innot.avreclipse.core.toolinfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.PluginIDs;
import de.innot.avreclipse.core.IMCUProvider;
import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathProvider;
import de.innot.avreclipse.core.paths.IPathProvider;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * This class provides some information about the used gcc compiler in the
 * toolchain.
 * 
 * It can return a list of all supported target mcus.
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public class GCC extends BaseToolInfo implements IMCUProvider {

	private static final String TOOL_ID = PluginIDs.PLUGIN_TOOLCHAIN_TOOL_COMPILER;

	private static GCC instance = null;

	private Map<String, String> fMCUmap = null;

	private IPath fCurrentPath = null;

	private IPathProvider fPathProvider = new AVRPathProvider(AVRPath.AVRGCC);

	/**
	 * Get an instance of this Tool.
	 */
	public static GCC getDefault() {
		if (instance == null)
			instance = new GCC();
		return instance;
	}

	private GCC() {
		// Let the superclass get the command name
		super(TOOL_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.IToolInfo#getToolPath()
	 */
	@Override
	public IPath getToolPath() {
		IPath path = fPathProvider.getPath();
		return path.append(getCommandName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUInfo(java.lang.String)
	 */
	public String getMCUInfo(String mcuid) {
		Map<String, String> internalmap = loadMCUList();
		return internalmap.get(mcuid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUList()
	 */
	public Set<String> getMCUList() {
		Map<String, String> internalmap = loadMCUList();
		Set<String> idlist = internalmap.keySet();
		return new HashSet<String>(idlist);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#hasMCU(java.lang.String)
	 */
	public boolean hasMCU(String mcuid) {
		Map<String, String> internalmap = loadMCUList();
		return internalmap.containsKey(mcuid);
	}

	/**
	 * @return Map &lt;mcu id, UI name&gt; of all supported MCUs
	 */
	private Map<String, String> loadMCUList() {

		if (!getToolPath().equals(fCurrentPath)) {
			// toolpath has changed, reload the list
			fMCUmap = null;
			fCurrentPath = getToolPath();
		}

		if (fMCUmap != null) {
			// return stored map
			return fMCUmap;
		}

		fMCUmap = new HashMap<String, String>();

		// Execute avr-gcc with the "--target-help" option and parse the
		// output
		String command = getToolPath().toOSString();
		List<String> argument = new ArrayList<String>(1);
		argument.add("--target-help");
		
		ExternalCommandLauncher gcc = new ExternalCommandLauncher(command, argument);
		try {
	        gcc.launch();
        } catch (IOException e) {
        	// Something didn't work while running the external command
        	IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "Could not start "+command, e);
        	AVRPlugin.getDefault().log(status);
        	return fMCUmap;
        }

        List<String> stdout = gcc.getStdOut();
		
		boolean start = false;
		
		for (String line : stdout) {
			if ("Known MCU names:".equals(line)) {
				start = true;
			} else if (start && !line.startsWith(" ")) {
				// finished
				start = false;
			} else if (start) {
				String[] names = line.split(" ");
				for (int i = 0; i < names.length; i++) {
					String mcuid = names[i];
					String mcuname = AVRMCUidConverter.id2name(mcuid);
					if (mcuname == null) {
						// some mcuid are generic and should not be
						// included
						continue;
					}
					fMCUmap.put(mcuid, mcuname);
				}
			} else {
				// a line outside of the "Known MCU names:" section
			}			
		}

		return fMCUmap;
	}
}
