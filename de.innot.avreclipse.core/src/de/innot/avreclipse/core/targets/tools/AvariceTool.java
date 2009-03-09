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

import java.util.Set;

import de.innot.avreclipse.core.targets.IGDBServerTool;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.IProgrammerTool;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class AvariceTool implements IProgrammerTool, IGDBServerTool {

	private final static String	ID		= "avreclipse.avarice";

	private final static String	NAME	= "AVaRICE";

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
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammers()
	 */
	public Set<IProgrammer> getProgrammers() {
		// TODO Auto-generated method stub
		return null;
	}

}
