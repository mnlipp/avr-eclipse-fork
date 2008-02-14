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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;

import de.innot.avreclipse.PluginIDs;
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
public class GCC extends BaseToolInfo {

	private static final String TOOL_ID = PluginIDs.PLUGIN_TOOLCHAIN_TOOL_COMPILER;

	protected String[] toolinfotypes = { TOOLINFOTYPE_MCUS };

	private static GCC instance = null;

	private Map<String, String> fMCUmap = null;

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
		super.toolinfotypes = this.toolinfotypes;
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

	@Override
	public Map<String, String> getToolInfo(String type) {

		if (TOOLINFOTYPE_MCUS.equals(type)) {
			return getMCUList();
		}

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.BaseToolInfo#getToolInfoTypes()
	 */
	@Override
	public List<String> getToolInfoTypes() {
		List<String> types = new ArrayList<String>(1);
		types.add(TOOLINFOTYPE_MCUS);
		return types;
	}

	/**
	 * @return Map &lt;internal name, UI name&gt; of all supported MCUs
	 */
	private Map<String, String> getMCUList() {

		if (fMCUmap != null) {
			return fMCUmap;
		}

		fMCUmap = new HashMap<String, String>();

		Process cmdproc = null;
		InputStream is = null;

		try {
			// Execute avr-gcc with the "--target-help" option and parse the
			// output
			cmdproc = ProcessFactory.getFactory().exec(
			        getToolPath().toOSString() + " --target-help");
			is = cmdproc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			boolean start = false;
			String line;

			while ((line = br.readLine()) != null) {
				if ("Known MCU names:".equals(line)) {
					start = true;
					continue;
				}
				if (start && !line.startsWith(" ")) {
					// finished
					break;
				}
				if (start) {
					String[] names = line.split(" ");
					for (int i = 0; i < names.length; i++) {
						String mcuid = names[i];
						String mcuname = AVRMCUidConverter.id2name(mcuid);
						if (mcuname == null) {
							// some mcuid are generic and should not be included
							continue;
						}
						fMCUmap.put(mcuid, mcuname);
					}
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
			try {
				if (cmdproc != null) {
					cmdproc.waitFor();
				}
			} catch (InterruptedException e) {
			}
		}

		return fMCUmap;
	}

}
