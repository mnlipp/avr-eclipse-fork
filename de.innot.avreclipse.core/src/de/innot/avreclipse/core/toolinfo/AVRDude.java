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
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.IMCUProvider;
import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathProvider;
import de.innot.avreclipse.core.paths.IPathProvider;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * This class handles all interactions with the avrdude program.
 * 
 * It can return a list of all supported target mcus.
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class AVRDude implements IMCUProvider {

	private static AVRDude instance = null;

	private Map<String, ConfigEntry> fMCUList;
	private Map<String, ConfigEntry> fProgrammerList;

	private Map<String, String> fMCUIdMap = null;

	private IPath fCurrentPath = null;

	private String fCommandName = "avrdude";

	private IPathProvider fPathProvider = new AVRPathProvider(AVRPath.AVRDUDE);

	/**
	 * Get an instance of this Tool.
	 */
	public static AVRDude getDefault() {
		if (instance == null)
			instance = new AVRDude();
		return instance;
	}

	private AVRDude() {
	}

	/**
	 * @return
	 */
	public String getCommandName() {
		return fCommandName;
	}

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
		Set<String> idset = internalmap.keySet();
		return new HashSet<String>(idset);
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
	 * @return
	 */
	public Set<String> getProgrammersList() {
		Map<String, ConfigEntry> internalmap = loadProgrammersList();
		Set<String> idset = internalmap.keySet();
		return new HashSet<String>(idset);
	}

	/**
	 * @return Map&lt;mcu id, avrdude id&gt; of all supported MCUs
	 */
	private Map<String, String> loadMCUList() {

		if (!getToolPath().equals(fCurrentPath)) {
			// toolpath has changed, reload the list
			fMCUList = null;
			fMCUIdMap = null;
			fCurrentPath = getToolPath();
		}

		if (fMCUIdMap != null) {
			// return stored map
			return fMCUIdMap;
		}

		fMCUList = new HashMap<String, ConfigEntry>();
		// Execute avrdude with the "-p?" to get a list of all supported mcus.
		readAVRDudeConfigOutput(fMCUList, "-p?");

		// The returned list has avrdude mcu id values, which are not the same
		// as the ones used in this Plugin. Instead the returned name is
		// converted into an Pluin mcu id value.
		fMCUIdMap = new HashMap<String, String>(fMCUList.size());
		Collection<ConfigEntry> allentries = fMCUList.values();
		for (ConfigEntry entry : allentries) {
			String mcuid = AVRMCUidConverter.name2id(entry.description);
			fMCUIdMap.put(mcuid, entry.avrdudeid);
		}

		return fMCUIdMap;
	}

	/**
	 * @return Map&lt;mcu id, avrdude id&gt; of all supported MCUs
	 */
	private Map<String, ConfigEntry> loadProgrammersList() {

		if (!getToolPath().equals(fCurrentPath)) {
			// toolpath has changed, reload the list
			fProgrammerList = null;
			fCurrentPath = getToolPath();
		}

		if (fProgrammerList != null) {
			// return stored list
			return fProgrammerList;
		}
		fProgrammerList = new HashMap<String, ConfigEntry>();
		// Execute avrdude with the "-p?" to get a list of all supported mcus.
		readAVRDudeConfigOutput(fProgrammerList, "-pm16", "-c?");
		return fProgrammerList;
	}

	private void readAVRDudeConfigOutput(Map<String, ConfigEntry> resultmap, String... arguments) {

		List<String> stdout = runCommand(arguments);
		if (stdout == null) {
			return;
		}

		// Avrdude output for configuration items looks like:
		// " id = description [pathtoavrdude.conf:line]"
		// The following pattern splits this into the four groups:
		// id / description / path / line
		Pattern mcuPat = Pattern.compile("\\s*(\\w+)\\s*=\\s*(.+?)\\s*\\[(.+):(\\d+)\\]\\.*");
		Matcher m;

		for (String line : stdout) {
			m = mcuPat.matcher(line);
			if (!m.matches()) {
				continue;
			}
			ConfigEntry entry = new ConfigEntry();
			entry.avrdudeid = m.group(1);
			entry.description = m.group(2);
			entry.configfile = new Path(m.group(3));
			entry.linenumber = Integer.valueOf(m.group(4));

			resultmap.put(entry.avrdudeid, entry);
		}
	}

	/**
	 * Get the command name and the current version of avrdude.
	 * <p>
	 * The name is defined in {@link #fCommandName}. The version is gathered by
	 * executing with the "-v" option and parsing the output.
	 * </p>
	 * 
	 * @return <code>String</code> with the command name and version
	 */
	public String getNameAndVersion() {

		// Execute avrdude with the "-v" option and parse the
		// output
		List<String> stdout = runCommand("-v");
		if (stdout == null) {
			// Return default name on failures
			return getCommandName() + " n/a";
		}

		// look for a line matching "*Version TheVersionNumber *"
		Pattern mcuPat = Pattern.compile(".*Version\\s+([\\d\\.]+).*");
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
		return getCommandName() + " ?.?";
	}

	/**
	 * Runs avrdude with the given arguments.
	 * <p>
	 * The Output of stdout and stderr are merged and returned in a
	 * <code>List&lt;String&gt;</code>.
	 * </p>
	 * <p>
	 * If the command fails to execute an entry is written to the log and
	 * <code>null</code> is returned
	 * 
	 * @param arguments
	 *            Zero or more arguments for avrdude
	 * @return A list of all output lines, or <code>null</code> if the command
	 *         could not be launched.
	 */
	private List<String> runCommand(String... arguments) {

		String command = getToolPath().toOSString();
		List<String> arglist = new ArrayList<String>(1);
		for (String arg : arguments) {
			arglist.add(arg);
		}

		ExternalCommandLauncher avrdude = new ExternalCommandLauncher(command, arglist);
		avrdude.redirectErrorStream(true);
		try {
			avrdude.launch();
		} catch (IOException e) {
			// Something didn't work while running the external command
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "Could not start "
			        + command, e);
			AVRPlugin.getDefault().log(status);
			return null;
		}

		List<String> stdout = avrdude.getStdOut();

		return stdout;
	}

	private static class ConfigEntry {
		public String avrdudeid;
		public String description;
		public IPath configfile;
		public int linenumber;
	}
}
