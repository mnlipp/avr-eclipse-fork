/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/

package de.innot.avreclipse.core.targets.tools;

import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationTool;
import de.innot.avreclipse.core.targets.IToolFactory;
import de.innot.avreclipse.core.targets.ToolManager;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class AvariceToolFactory implements IToolFactory {

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.IToolFactory#getId()
	 */
	public String getId() {
		return AvariceTool.ID;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.IToolFactory#getName()
	 */
	public String getName() {
		return AvariceTool.NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.IToolFactory#isType(java.lang.String)
	 */
	public boolean isType(String tooltype) {

		// AvariceTool is both a programmer tool and a gdbserver

		if (ToolManager.AVRPROGRAMMERTOOL.equals(tooltype)) {
			return true;
		}

		if (ToolManager.AVRGDBSERVER.equals(tooltype)) {
			return true;
		}

		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.IToolFactory#createTool(de.innot.avreclipse.core.targets
	 * .ITargetConfiguration)
	 */
	public ITargetConfigurationTool createTool(ITargetConfiguration tc) {
		return new AvariceTool(tc);
	}

}
