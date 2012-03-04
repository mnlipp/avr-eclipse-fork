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
package de.innot.avreclipse.debug.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;

import de.innot.avreclipse.debug.gdbserver.IGDBServerFactory;

/**
 * The activator class controls the plug-in life cycle
 */
public class AVRDebugPlugin extends Plugin {

	// The plug-in ID
	public static final String	PLUGIN_ID	= "de.innot.avreclipse.debug.core";

	private final static String	GDBSERVER_EXTENSION	
		= "de.innot.avreclipse.debug.core.gdbServer";
	
	// The shared instance
	private static AVRDebugPlugin	plugin;

	private HashMap<String, IGDBServerFactory> fGDBServerFactories = null;
	
	/**
	 * The constructor
	 */
	public AVRDebugPlugin() {
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.Plugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static BundleContext getBundleContext() {
		return getDefault().getBundle().getBundleContext();
	}
    
	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AVRDebugPlugin getDefault() {
		return plugin;
	}

	/**
	 * Log the given status and print it to the err Stream if debugging is enabled.
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		if (status.getSeverity() > Status.OK) {
			ILog log = getDefault().getLog();
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
	public Map<String, IGDBServerFactory> getGDBServerFactories() {

		if (fGDBServerFactories == null) {
			loadServerExtensions();
		}

		// Return a copy of the internal map
		return new HashMap<String, IGDBServerFactory>(fGDBServerFactories);
	}
	
	/**
	 * 
	 */
	private void loadServerExtensions() {
		fGDBServerFactories = new HashMap<String, IGDBServerFactory>();

		IConfigurationElement[] elements = Platform.getExtensionRegistry()
				.getConfigurationElementsFor(GDBSERVER_EXTENSION);
		for (IConfigurationElement element : elements) {

			// Get the id and the description of the extension.
			String gdbserverid = element.getAttribute("gdbserverid");
			if (gdbserverid == null) {
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
			if (obj instanceof IGDBServerFactory) {
				IGDBServerFactory factory = (IGDBServerFactory) obj;
				// settingspage.setGDBServerID(gdbserverid);
				// settingspage.setDescription(description);
				fGDBServerFactories.put(gdbserverid, factory);
			}
		}
	}
	
}
