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
/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IPath;

/**
 * @author U043192
 * 
 */
public abstract class BaseToolInfo {

	private String	fCommandName	= null;

	protected BaseToolInfo(String toolid) {
		// First: Get the command name from the toolchain
		ITool tool = ManagedBuildManager.getExtensionTool(toolid);
		if (tool != null) {
			fCommandName = tool.getToolCommand();
			if (fCommandName.startsWith("-")) {
				// remove leading "-" in command name
				// (used to suppress "make" exit on errors)
				fCommandName = fCommandName.substring(1);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.IToolInfo#getToolPath()
	 */
	public IPath getToolPath() {
		// Base implementation. Override as necessary.
		return null;
	}

	public String getCommandName() {
		return fCommandName;
	}
}
