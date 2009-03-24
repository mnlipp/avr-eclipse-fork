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

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.IProgrammerTool;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.ICommandOutputListener;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class AvrdudeTool extends AbstractTool implements IProgrammerTool {

	public final static String		ID					= "avreclipse.avrdude";

	private final static String		NAME				= "AVRDude";

	private final static AVRDude	fAVRDude			= AVRDude.getDefault();

	private final static String		ATTR_CMD_NAME		= ID + ".command";
	private final static String		DEF_CMD_NAME		= "avrdude";

	public final static String		ATTR_USE_CONSOLE	= ID + ".useconsole";
	public final static boolean		DEF_USE_CONSOLE		= true;

	private Map<String, String>		fDefaults;

	private ICommandOutputListener	fOutputListener		= new AvrdudeOutputListener();

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
		try {
			return fAVRDude.getMCUList();
		} catch (IOException e) {
			return Collections.emptySet();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammers()
	 */
	public Set<IProgrammer> getProgrammers(ITargetConfiguration tc) {
		try {
			List<IProgrammer> list = fAVRDude.getProgrammersList();
			return new HashSet<IProgrammer>(list);
		} catch (AVRDudeException e) {
			return Collections.emptySet();
		}
	}

}
