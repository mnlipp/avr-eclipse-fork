/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.utils.spawner.ProcessFactory;

import de.innot.avreclipse.PluginIDs;

/**
 * @author Thomas
 * 
 */
public class GCC implements IToolInfo {

	private static GCC instance = null;

	private String fCommandName = null;
	
	private String fCommandFolder = null;

	/**
	 * Get an instance of this Tool.
	 */
	public static GCC getDefault() {
		if (instance == null)
			instance = new GCC();
		return instance;
	}

	private GCC() {
		// First: Get the command name from the toolchain
		setCommandNameFromToolchain();
		
		// Find the tool and get the parent folder
		setCommandFolder();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.IToolInfo#getToolPath()
	 */
	public String getToolPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getToolInfo(String type) {
		return null;
	}

	public List<String> getToolInfoTypes() {
		List<String> types = new ArrayList<String>(1);
		types.add(TOOLINFOTYPE_MCUS);
		return types;
	}

	/**
	 * Set the compiler command from the extension toolchain.
	 * 
	 * User modified commands are ignored.
	 * 
	 */
	private void setCommandNameFromToolchain() {
		ITool compiler = ManagedBuildManager
				.getExtensionTool(PluginIDs.PLUGIN_TOOLCHAIN_TOOL_COMPILER);
		if (compiler != null) {
			fCommandName = compiler.getToolCommand();
		}
		// "sensible" default in case the toolchain did not yield a command name
		fCommandName = "avr-gcc";
	}
	
	private String setCommandFolder(String command) {
		
		ProcessFactory.getFactory().exec(cmd, envp, dir)
		return null;
	}
}
