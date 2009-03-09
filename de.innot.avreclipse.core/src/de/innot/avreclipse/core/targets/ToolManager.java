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

package de.innot.avreclipse.core.targets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.targets.tools.AvariceTool;
import de.innot.avreclipse.core.targets.tools.AvrdudeTool;

/**
 * Manages the tools for the target configuration
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class ToolManager implements IRegistryEventListener {

	private static ToolManager				fInstance;

	private final static String				NAMESPACE			= AVRPlugin.PLUGIN_ID;
	private final static String				EXTPOINT_PROGRAMMER	= "programmertool";
	private final static String				EXTPOINT_GDBSERVER	= "gdbserver";

	// The list of all ImageLoaderSettingPage extensions
	private Map<String, IProgrammerTool>	fProgrammerTools;

	// The list of all GDBServerSettingsPage extensions
	private Map<String, IGDBServerTool>		fGDBServerTools;

	public static ToolManager getDefault() {
		if (fInstance == null) {
			fInstance = new ToolManager();
		}
		return fInstance;
	}

	// prevent instantiation
	private ToolManager() {

	}

	public String[] getExptensionPointIDs() {
		String[] extpoints = new String[2];
		extpoints[0] = EXTPOINT_PROGRAMMER;
		extpoints[1] = EXTPOINT_GDBSERVER;
		return extpoints;
	}

	public IProgrammerTool[] getProgrammerTools() {

		// TODO: Dummy implementation
		IProgrammerTool[] tools = new IProgrammerTool[2];
		tools[0] = new AvrdudeTool();
		tools[1] = new AvariceTool();

		return tools;
	}

	public IGDBServerTool[] getGDBServerTools() {

		return null;
	}

	public IProgrammerTool getProgrammerTool(String id) {

		return null;
	}

	public IGDBServerTool getGDBServerTool(String id) {

		return null;
	}

	/**
	 * 
	 */
	private Map<String, ITargetConfigurationTool> loadSettingsExtensions(String requiredtype) {
		Map<String, ITargetConfigurationTool> results = new HashMap<String, ITargetConfigurationTool>();

		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTPOINT_PROGRAMMER);
		for (IConfigurationElement element : elements) {

			// Get the type and check if is of the requested type
			String type = element.getAttribute("type");
			if (type == null || !type.equalsIgnoreCase(requiredtype)) {
				continue;
			}
			// Get the id and the description of the extension.
			String gdbserverid = element.getAttribute("launcherId");
			if (gdbserverid == null) {
				// TODO log an error
				continue;
			}

			String description = element.getAttribute("description");
			if (description == null) {
				// TODO log an error
				continue;
			}

			// Get an instance of the implementing class
			Object obj;
			try {
				obj = element.createExecutableExtension("class");
			} catch (CoreException e) {
				// TODO log exception
				continue;
			}
			if (obj instanceof ITargetConfigurationTool) {
				ITargetConfigurationTool settingspage = (ITargetConfigurationTool) obj;
				results.put(gdbserverid, settingspage);
			}
		}

		return results;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtension[])
	 */
	public void added(IExtension[] extensions) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtensionPoint
	 * [])
	 */
	public void added(IExtensionPoint[] extensionPoints) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtension
	 * [])
	 */
	public void removed(IExtension[] extensions) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtensionPoint
	 * [])
	 */
	public void removed(IExtensionPoint[] extensionPoints) {
		// TODO Auto-generated method stub

	}

}
