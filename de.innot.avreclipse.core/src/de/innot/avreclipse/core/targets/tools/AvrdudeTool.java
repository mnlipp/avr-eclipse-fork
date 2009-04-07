/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.core.targets.tools;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.IProgrammerTool;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfiguration.ValidationResult;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.ICommandOutputListener;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class AvrdudeTool extends AbstractTool implements IProgrammerTool {

	public final static String			ID					= "avreclipse.avrdude";

	private final static String			NAME				= "AVRDude";

	private final static AVRDude		fAVRDude			= AVRDude.getDefault();

	public final static String			ATTR_CMD_NAME		= ID + ".command";
	public final static String			DEF_CMD_NAME		= "avrdude";

	public final static String			ATTR_USE_CONSOLE	= ID + ".useconsole";
	public final static boolean			DEF_USE_CONSOLE		= true;

	private Map<String, String>			fDefaults;

	/** Cache of all Name/Version strings, mapped to their respective command name. */
	private Map<String, String>			fNameVersionMap		= new HashMap<String, String>();

	/** Cache of all MCU Sets, mapped to their respective command name */
	private Map<String, Set<String>>	fMCUMap				= new HashMap<String, Set<String>>();

	/** Mapping of mcu id values to their AVRDude format counterparts. */
	private Map<String, String>			fMCUAVRudeFormatMap	= new HashMap<String, String>();

	private ICommandOutputListener		fOutputListener		= new AvrdudeOutputListener();

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getDefaults()
	 */
	public Map<String, String> getDefaults() {
		if (fDefaults == null) {
			fDefaults = new HashMap<String, String>();

			fDefaults.put(ATTR_CMD_NAME, DEF_CMD_NAME);
			fDefaults.put(ATTR_USE_CONSOLE, Boolean.toString(DEF_USE_CONSOLE));
		}

		return fDefaults;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getId()
	 */
	public String getId() {
		return ID;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getName()
	 */
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.tools.AbstractTool#getCommand(de.innot.avreclipse.core.targets
	 * .ITargetConfiguration)
	 */
	public String getCommand(ITargetConfiguration tc) {
		String command = tc.getAttribute(ATTR_CMD_NAME);
		if (command == null) {
			command = DEF_CMD_NAME;
		}
		return command;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.tools.AbstractTool#getOutputListener()
	 */
	@Override
	protected ICommandOutputListener getOutputListener() {
		return fOutputListener;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getVersion(de.innot.avreclipse.
	 * core.targets.ITargetConfiguration)
	 */
	public String getVersion(ITargetConfiguration tc) throws AVRDudeException {

		String cmd = getCommand(tc);

		// Check if we already have the version in the cache
		if (fNameVersionMap.containsKey(cmd)) {
			return fNameVersionMap.get(cmd);
		}

		// Execute avrdude in verbose mode with a dummy programmer (to silence the warning that
		// would cause an AVRDudeException by the output listener)
		// The name / version are in the first full line of the output in the format
		// "avrdude: Version 5.6cvs, compiled on Nov 10 2008 at 17:15:38"
		String name = null;
		List<String> stdout = runCommand(tc, "-cstk500v2", "-v");

		if (stdout != null) {
			// look for a line matching "*Version TheVersionNumber *"
			Pattern mcuPat = Pattern.compile(".*Version\\s+([\\w\\.]+).*");
			Matcher m;
			for (String line : stdout) {
				m = mcuPat.matcher(line);
				if (!m.matches()) {
					continue;
				}
				name = getName() + " " + m.group(1);
				break;
			}
		}
		if (name == null) {
			// could not read the version from the output, probably the regex has a
			// mistake. Return a reasonable default.
			return getName() + " ?.?";
		}

		fNameVersionMap.put(cmd, name);
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getMCUs()
	 */
	public Set<String> getMCUs(ITargetConfiguration tc) throws AVRDudeException {

		// Check if we already have the list in the cache
		String cmd = getCommand(tc);

		if (fMCUMap.containsKey(cmd)) {
			return fMCUMap.get(cmd);
		}
		// Execute avrdude with the "-p?" to get a list of all supported mcus.
		// The parse the all output for lines matching
		// avrdudeid = mcuid [otherstuff]
		Set<String> allmcus = new HashSet<String>();
		List<String> stdout;

		stdout = runCommand(tc, "-p?");

		if (stdout != null) {
			Pattern mcuPat = Pattern.compile("\\s*(\\w+)\\s*=\\s*(\\w+).*");
			Matcher m;
			for (String line : stdout) {
				m = mcuPat.matcher(line);
				if (!m.matches()) {
					continue;
				}
				String avrdudeid = m.group(1);
				String mcuid = m.group(2).toLowerCase();
				fMCUAVRudeFormatMap.put(mcuid, avrdudeid);
				allmcus.add(mcuid);
			}
		}

		// Save the set in the cache
		fMCUMap.put(cmd, allmcus);
		return allmcus;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammers()
	 */
	public Set<String> getProgrammers(ITargetConfiguration tc) throws AVRDudeException {
		// FIXME: Change the AVRDude.class API to take the Target configuration into account.
		return fAVRDude.getProgrammerIDs();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammer(de.innot.avreclipse
	 * .core.targets.ITargetConfiguration, java.lang.String)
	 */
	public IProgrammer getProgrammer(ITargetConfiguration tc, String id) throws AVRDudeException {
		// FIXME: Change the AVRDude.class API to take the Target configuration into account.
		return fAVRDude.getProgrammer(id);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#validate(de.innot.avreclipse.core
	 * .targets.ITargetConfiguration, java.lang.String)
	 */
	public ValidationResult validate(ITargetConfiguration tc, String attr) {
		// TODO Auto-generated method stub
		return null;
	}

}
