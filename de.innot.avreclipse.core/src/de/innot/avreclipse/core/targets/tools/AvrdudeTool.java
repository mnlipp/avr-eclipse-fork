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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.IProgrammerTool;
import de.innot.avreclipse.core.toolinfo.AVRDude;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class AvrdudeTool implements IProgrammerTool {

	private final static String		ID			= "avreclipse.avrdude";

	private final static String		NAME		= "AVRDude";

	private final static AVRDude	fAVRDude	= AVRDude.getDefault();

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
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getMCUs()
	 */
	public Set<String> getMCUs() {
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
	public Set<IProgrammer> getProgrammers() {
		try {
			List<IProgrammer> list = fAVRDude.getProgrammersList();
			return new HashSet<IProgrammer>(list);
		} catch (AVRDudeException e) {
			return Collections.emptySet();
		}
	}

}
