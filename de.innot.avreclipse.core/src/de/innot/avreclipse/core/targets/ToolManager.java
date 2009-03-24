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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IRegistryEventListener;
import org.eclipse.core.runtime.Platform;

import de.innot.avreclipse.AVRPlugin;

/**
 * Manages the tools for the target configuration.
 * <p>
 * This class manages the
 * </p>
 * <p>
 * This class implements the singleton pattern. There is only one instance of this class, accessible
 * with {@link #getDefault()}.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class ToolManager implements IRegistryEventListener {

	private static ToolManager				fInstance;

	private final static String				NAMESPACE			= AVRPlugin.PLUGIN_ID;
	public final static String				EXTENSIONPOINT		= NAMESPACE + ".targetTool";
	private final static String				ELEMENT_PROGRAMMER	= "programmertool";
	private final static String				ELEMENT_GDBSERVER	= "gdbservertool";

	/** The list of all ProgrammerTool extensions, mapped to their ID for quick access. */
	private Map<String, IProgrammerTool>	fProgrammerTools;

	/** The list of all GDBServerTool extensions, mapped to their ID for quick access. */
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

	/**
	 * Get a list of all extension point ids used for the target tools.
	 * <p>
	 * The list is used by the AVRPlugin class to register the toolmanager as a listener for
	 * additions/removal of tool extension plugins.
	 * </p>
	 * 
	 * @return Array with the unique extension points for the toolmanager.
	 */
	public String[] getExtensionPointIDs() {
		String[] extpoints = new String[1];
		extpoints[0] = EXTENSIONPOINT;
		return extpoints;
	}

	public IProgrammerTool[] getProgrammerTools() {

		loadExtensions();
		Collection<IProgrammerTool> alltools = fProgrammerTools.values();
		return alltools.toArray(new IProgrammerTool[alltools.size()]);

	}

	public IGDBServerTool[] getGDBServerTools() {

		loadExtensions();
		Collection<IGDBServerTool> alltools = fGDBServerTools.values();
		return alltools.toArray(new IGDBServerTool[alltools.size()]);

	}

	public IProgrammerTool getProgrammerTool(String id) {

		loadExtensions();
		return fProgrammerTools.get(id);
	}

	public IGDBServerTool getGDBServerTool(String id) {
		loadExtensions();
		return fGDBServerTools.get(id);
	}

	/**
	 * Load all gdbserverTool extensions.
	 * <p>
	 * The list is cached until some extensions are either added or removed
	 * </p>
	 * 
	 * @see #added(IExtension[])
	 * 
	 */
	private void loadExtensions() {
		if (fProgrammerTools == null) {

			fProgrammerTools = new HashMap<String, IProgrammerTool>();
			fGDBServerTools = new HashMap<String, IGDBServerTool>();

			IConfigurationElement[] elements = Platform.getExtensionRegistry()
					.getConfigurationElementsFor(EXTENSIONPOINT);
			for (IConfigurationElement element : elements) {

				// Get an instance of the implementing class
				Object obj;
				try {
					obj = element.createExecutableExtension("class");
				} catch (CoreException e) {
					// TODO log exception
					continue;
				}
				String type = element.getName();

				if (ELEMENT_PROGRAMMER.equalsIgnoreCase(type)) {
					if (obj instanceof IProgrammerTool) {
						IProgrammerTool tool = (IProgrammerTool) obj;
						fProgrammerTools.put(tool.getId(), tool);
					}
				} else if (ELEMENT_GDBSERVER.equalsIgnoreCase(type)) {
					if (obj instanceof IGDBServerTool) {
						IGDBServerTool tool = (IGDBServerTool) obj;
						fGDBServerTools.put(tool.getId(), tool);
					}
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtension[])
	 */
	public void added(IExtension[] extensions) {
		// Check if the extensions match any of the two points used by this manager.
		// To keep things simple we just invalidate the current list of known extensions so that the
		// list will be regenerated the next time getXxxTool() is called.
		for (IExtension ext : extensions) {
			if (ext.getUniqueIdentifier().equals(EXTENSIONPOINT)) {
				fProgrammerTools = null;
				fGDBServerTools = null;
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.IRegistryEventListener#added(org.eclipse.core.runtime.IExtensionPoint
	 * [])
	 */
	public void added(IExtensionPoint[] extensionPoints) {
		// Don't care if any extension points have changed
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtension
	 * [])
	 */
	public void removed(IExtension[] extensions) {
		// remove or add doesn't matter for our simple implementation.
		added(extensions);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.runtime.IRegistryEventListener#removed(org.eclipse.core.runtime.IExtensionPoint
	 * [])
	 */
	public void removed(IExtensionPoint[] extensionPoints) {
		// Don't care if any extension points have changed
	}

}
