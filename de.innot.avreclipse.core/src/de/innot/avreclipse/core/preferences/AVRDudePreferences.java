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
package de.innot.avreclipse.core.preferences;

import java.io.IOException;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import de.innot.avreclipse.AVRPlugin;

/**
 * This class handles access to the AVRDude preferences.
 * <p>
 * These preferences are stored per instance.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class AVRDudePreferences {

	private static final String		CLASSNAME				= "avrdude";
	private static final String		QUALIFIER				= AVRPlugin.PLUGIN_ID + "/" + CLASSNAME;
	private static final String		CONFIGQUALIFIER			= QUALIFIER + "/configs/";

	private static IPreferenceStore	fInstanceStore			= null;

	/** Set <code>true</code> to use a custom configuration file */
	public static final String		KEY_USECUSTOMCONFIG		= "customconfigfile";
	public static final boolean		DEFAULT_USECUSTOMCONFIG	= false;

	/** Path to a custom avrdude.conf configuration file */
	public static final String		KEY_CONFIGFILE			= "avrdudeconf";

	public static final String		KEY_USECONSOLE			= "avrdudeUseConsole";
	public static final boolean		DEFAULT_USECONSOLE		= false;

	/**
	 * Gets the instance AVRDude preferences.
	 * 
	 * @return IPreferenceStore with the properties
	 */
	public static IPreferenceStore getPreferenceStore() {
		// The instance Path PreferenceStore is cached
		if (fInstanceStore != null) {
			return fInstanceStore;
		}

		IPreferenceStore store = new ScopedPreferenceStore(new InstanceScope(), QUALIFIER);

		fInstanceStore = store;
		return store;
	}

	/**
	 * Gets the instance root node Preferences for all Programmer Configurations.
	 * 
	 * @return
	 */
	public static IEclipsePreferences getConfigPreferences() {
		IEclipsePreferences root = new InstanceScope().getNode(CONFIGQUALIFIER);
		return root;
	}

	/**
	 * Saves a changed PreferencesStore.
	 * <p>
	 * This has to be called to make any changes to the given PreferenceStore persistent.
	 * </p>
	 * 
	 * @param store
	 * @throws IOException
	 *             if the PreferenceStore could not be written to the storage.
	 */
	public static void savePreferences(IPreferenceStore store) throws IOException {
		Assert.isTrue(store instanceof IPersistentPreferenceStore);
		((IPersistentPreferenceStore) store).save();
	}

	/**
	 * Gets the default AVRDude preferences
	 * 
	 * @return
	 */
	public static IEclipsePreferences getDefaultPreferences() {
		return new DefaultScope().getNode(QUALIFIER);
	}

	/**
	 * Initialize the default AVRDude preference values.
	 * 
	 * This is called from {@link de.innot.avreclipse.core.preferences.PreferenceInitializer}.
	 * Clients are not supposed to call this method.
	 */
	public static void initializeDefaultPreferences() {
		IEclipsePreferences prefs = getDefaultPreferences();

		// currently only one global property: The name of a avrdude config file
		// other than the build-in.
		// And the default is to not use it.

		prefs.putBoolean(KEY_USECUSTOMCONFIG, DEFAULT_USECUSTOMCONFIG);
		prefs.put(KEY_CONFIGFILE, "");
		prefs.putBoolean(KEY_USECONSOLE, DEFAULT_USECONSOLE);

	}

}
