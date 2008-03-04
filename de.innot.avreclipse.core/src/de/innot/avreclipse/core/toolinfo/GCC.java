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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 * <p>
 * It can return a list of all supported target mcus.
 * </p>
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
		List<String> stdout = runCommand("--target-help");
		if (stdout == null) {
			// Return empty map on failures
			return fMCUmap;
		}

		boolean start = false;

		// The parsing is done by reading the output line by line until a line
		// with "Known MCU names:" is found. Then the parsing starts and all
		// following lines are split into the mcu ids until a line is reached
		// that does not start with a space (the mcu id lines start always with
		// a space).
		//
		// Maybe this could be done with a Pattern matcher, but I don't know how
		// to do multiline pattern matching and this is probably faster anyway
		for (String line : stdout) {
			if ("Known MCU names:".equals(line)) {
				start = true;
			} else if (start && !line.startsWith(" ")) {
				// finished
				start = false;
			} else if (start) {
				String[] names = line.split(" ");
				for (String mcuid : names) {
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

	/**
	 * Get the command name and the current version of GCC.
	 * <p>
	 * The name comes from the buildDefinition. The version is gathered by
	 * executing with the "-v" option and parsing the output.
	 * </p>
	 * 
	 * @return <code>String</code> with the command name and version
	 */
	public String getNameAndVersion() {

		// Execute avr-gcc with the "-v" option and parse the
		// output
		List<String> stdout = runCommand("-v");
		if (stdout == null) {
			// Return default name on failures
			return getCommandName() + " n/a";
		}

		// look for a line matching "gcc version TheVersionNumber"
		Pattern mcuPat = Pattern.compile("gcc version\\s*(.*)");
		Matcher m;
		for (String line : stdout) {
			m = mcuPat.matcher(line);
			if (!m.matches()) {
				continue;
			}
			return getCommandName() + " " + m.group(1);
		}

		// could not read the version from the output, probably the regex has a
		// mistake. Return a reasonable default.
		return  getCommandName()+ "?.?";
	}

	/**
	 * Runs the GCC with the given arguments.
	 * <p>
	 * The Output of stdout and stderr are merged and returned in a
	 * <code>List&lt;String&gt;</code>.
	 * </p>
	 * <p>
	 * If the command fails to execute an entry is written to the log and
	 * <code>null</code> is returned
	 * 
	 * @param arguments
	 *            Zero or more arguments for gcc
	 * @return A list of all output lines, or <code>null</code> if the command
	 *         could not be launched.
	 */
	private List<String> runCommand(String... arguments) {

		String command = getToolPath().toOSString();
		List<String> arglist = new ArrayList<String>(1);
		for (String arg : arguments) {
			arglist.add(arg);
		}

		ExternalCommandLauncher gcc = new ExternalCommandLauncher(command, arglist);
		gcc.redirectErrorStream(true);
		try {
			gcc.launch();
		} catch (IOException e) {
			// Something didn't work while running the external command
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "Could not start "
			        + command, e);
			AVRPlugin.getDefault().log(status);
			return null;
		}

		List<String> stdout = gcc.getStdOut();

		return stdout;
	}
}
