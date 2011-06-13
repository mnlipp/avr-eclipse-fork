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
package de.innot.avreclipse.debug.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AVRGDBUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String					PLUGIN_ID					= "de.innot.avreclipse.debug.ui";

	// The shared instance
	private static AVRGDBUIPlugin				plugin;

	private final static String					GDBSERVERSETTINGS_EXTENSION	= "de.innot.avreclipse.debug.ui.gdbserverSettingsPage";

	// The list of all GDBServerSettingsPage extensions
	private Map<String, IGDBServerSettingsPage>	fGDBServerSettingsPages;

	/**
	 * The constructor
	 */
	public AVRGDBUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext )
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext )
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AVRGDBUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the thread calling this
	 * method has an associated display. If so, this display is returned. Otherwise the method
	 * returns the default display.
	 * 
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/**
	 * @return The currently active Shell
	 */
	public static Shell getActiveShell() {
		return getStandardDisplay().getActiveShell();
	}

	/**
	 * Log the given status and print it to the err Stream if debugging is enabled.
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		ILog log = getDefault().getLog();
		if (status.getSeverity() >= Status.WARNING) {
			log.log(status);
		}
		if (getDefault().isDebugging()) {
			System.err.print(PLUGIN_ID + ": " + status.getMessage());
			if (status.getCode() != 0) {
				System.err.print("(" + status.getCode() + ")");
			}
			System.out.println("");
			if (status.getException() != null) {
				status.getException().printStackTrace();
			}
		}
	}

	/**
	 * Get a list of all {@link IGDBServerSettingsPage}'s defined by extensions.
	 * <p>
	 * As a convenience the list is mapped to the gdbserver id values.
	 * </p>
	 * 
	 * @return Map with gdbserver id's as keys and the settingspages as values.
	 */
	public Map<String, IGDBServerSettingsPage> getGDBServerSettingsPages() {

		if (fGDBServerSettingsPages == null) {
			loadSettingsExtensions();
		}

		// Return a copy of the internal map
		return new HashMap<String, IGDBServerSettingsPage>(fGDBServerSettingsPages);

	}

	/**
	 * 
	 */
	private void loadSettingsExtensions() {
		fGDBServerSettingsPages = new HashMap<String, IGDBServerSettingsPage>();

		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(GDBSERVERSETTINGS_EXTENSION);
		for (IConfigurationElement element : elements) {

			// Get the id and the description of the extension.
			String gdbserverid = element.getAttribute("gdbserverid");
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
			if (obj instanceof IGDBServerSettingsPage) {
				IGDBServerSettingsPage settingspage = (IGDBServerSettingsPage) obj;
				settingspage.setGDBServerID(gdbserverid);
				settingspage.setDescription(description);
				fGDBServerSettingsPages.put(gdbserverid, settingspage);
			}
		}
	}
}
