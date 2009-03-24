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
import java.util.Map;
import java.util.Set;

import de.innot.avreclipse.core.targets.IGDBServerTool;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.IProgrammerTool;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.toolinfo.ICommandOutputListener;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AvariceTool extends AbstractTool implements IProgrammerTool, IGDBServerTool {

	public final static String		ID					= "avreclipse.avarice";

	private final static String		NAME				= "AVaRICE";

	public final static String		ATTR_CMD_NAME		= ID + ".command";
	private final static String		DEF_CMD_NAME		= "avarice";

	public final static String		ATTR_USE_CONSOLE	= ID + ".useconsole";
	public final static boolean		DEF_USE_CONSOLE		= true;						// TODO:
	// Change to
	// false
	// for release

	private Map<String, String>		fDefaults;

	private ICommandOutputListener	fOutputListener		= new AvariceOutputListener();

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
	 * @see de.innot.avreclipse.core.targets.tools.AbstractTool#getOutputListener()
	 */
	@Override
	protected ICommandOutputListener getOutputListener() {
		return fOutputListener;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getMCUs()
	 */
	public Set<String> getMCUs(ITargetConfiguration tc) {
		// TODO: Dummy implementation
		Set<String> allmcus = new HashSet<String>();
		allmcus.add("atmega8");
		allmcus.add("atmega16");
		allmcus.add("atmega32");
		allmcus.add("attiny12");
		return allmcus;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammers()
	 */
	public Set<IProgrammer> getProgrammers(ITargetConfiguration tc) {
		// TODO: Dummy implementation
		Set<IProgrammer> allprogrammers = new HashSet<IProgrammer>();
		allprogrammers.add(tc.getProgrammer("dragon_jtag"));
		allprogrammers.add(tc.getProgrammer("dragon_dw"));
		allprogrammers.add(tc.getProgrammer("jtag1"));
		allprogrammers.add(tc.getProgrammer("jtag2"));
		allprogrammers.add(tc.getProgrammer("jtag2dw"));
		return allprogrammers;
	}

}
