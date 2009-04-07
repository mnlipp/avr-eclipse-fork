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

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.IGDBServerTool;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.IProgrammerTool;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfiguration.ValidationResult;

/**
 * This is a special virtual tool that represents no selected tool.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class NoneTool implements IGDBServerTool, IProgrammerTool {

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getId()
	 */
	public String getId() {
		return "none";
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getName()
	 */
	public String getName() {
		return "None";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getVersion(de.innot.avreclipse.
	 * core.targets.ITargetConfiguration)
	 */
	public String getVersion(ITargetConfiguration tc) throws AVRDudeException {
		return getName();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getMCUs(de.innot.avreclipse.core
	 * .targets.ITargetConfiguration)
	 */
	public Set<String> getMCUs(ITargetConfiguration tc) throws AVRDudeException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammer(de.innot.avreclipse
	 * .core.targets.ITargetConfiguration, java.lang.String)
	 */
	public IProgrammer getProgrammer(ITargetConfiguration tc, String id) throws AVRDudeException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammers(de.innot.avreclipse
	 * .core.targets.ITargetConfiguration)
	 */
	public Set<String> getProgrammers(ITargetConfiguration tc) throws AVRDudeException {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getDefaults()
	 */
	public Map<String, String> getDefaults() {
		return Collections.emptyMap();
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
