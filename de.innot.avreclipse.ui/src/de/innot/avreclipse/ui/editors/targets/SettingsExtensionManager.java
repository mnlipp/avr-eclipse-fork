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

package de.innot.avreclipse.ui.editors.targets;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.Platform;

import de.innot.avreclipse.ui.AVRUIPlugin;

/**
 * Manages the extension setting parts for the target configuration editor.
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
public class SettingsExtensionManager {

	private static SettingsExtensionManager	fInstance;

	private final static String				NAMESPACE		= AVRUIPlugin.PLUGIN_ID;
	public final static String				EXTENSIONPOINT	= NAMESPACE + ".targetToolSettings";

	public static SettingsExtensionManager getDefault() {
		if (fInstance == null) {
			fInstance = new SettingsExtensionManager();
		}

		return fInstance;
	}

	// prevent instantiation
	private SettingsExtensionManager() {
		// empty constructor
	}

	public ITCEditorPart getSettingsPartForTool(String id) {

		return loadExtension(id);
	}

	/**
	 * Load all extensions.
	 * 
	 * @see #added(IExtension[])
	 * 
	 */
	private ITCEditorPart loadExtension(String toolid) {
		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(EXTENSIONPOINT);
		for (IConfigurationElement element : elements) {

			// Get the id of the tool this settings part is applicable for.
			String id = element.getAttribute("toolId");
			if (!id.equals(toolid)) {
				// Not the required id -- continue searching
				continue;
			}

			// Get an instance of the implementing class
			Object obj;
			try {
				obj = element.createExecutableExtension("class");
			} catch (CoreException e) {
				// TODO log an error
				continue;
			}

			if (obj instanceof ITCEditorPart) {
				ITCEditorPart part = (ITCEditorPart) obj;
				return part;
			}
		}

		// No part found for the given tool id
		// TODO: return a default part which contains a meaningful error message
		return null;

	}

}
