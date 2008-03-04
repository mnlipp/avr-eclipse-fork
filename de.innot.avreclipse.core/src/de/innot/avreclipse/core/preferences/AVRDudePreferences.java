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
 * $Id: DatasheetPreferences.java 338 2008-03-01 10:53:16Z innot $
 *     
 *******************************************************************************/
package de.innot.avreclipse.core.preferences;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.AVRPlugin;

/**
 * This class handles access to the AVRDude preferences.
 * 
 * These preferences are stored per instance.
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class AVRDudePreferences {

	private static final String CLASSNAME = "avrdude";
	private static final String QUALIFIER = AVRPlugin.PLUGIN_ID + "/"
			+ CLASSNAME;
	private static final String CONFIGQUALIFIER = QUALIFIER + "/configs/";
	
	private static IPreferenceStore fInstanceStore = null;

	/** Set <code>true</code> to use a custom configuration file */
	public static final String KEY_USECUSTOMCONFIG = "customconfigfile";

	/** Path to a custom avrdude.conf configuration file */
	public static final String KEY_CONFIGFILE = "avrdudeconf";

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

		IScopeContext scope = new InstanceScope();
		IPreferenceStore store = new ScopedPreferenceStore(scope, QUALIFIER);

		fInstanceStore = store;
		return store;
	}

	/**
	 * Gets a list of all Programmer configuration names available.
	 * 
	 * @return <code>Set&lt;String&gt;</code> with all configuration names
	 */
	public static Set<String> getAllConfigs() {
		IScopeContext scope = new InstanceScope();
		IEclipsePreferences root = scope.getNode(CONFIGQUALIFIER);
		String[] confignames = new String[0];
		try {
			confignames = root.childrenNames();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Set<String> allconfigs = new HashSet<String>();
		for (String conf : confignames) {
			allconfigs.add(conf);
		}
		
		return allconfigs;
	}

	/**
	 * Gets the instance PreferenceStore for the Programmer Configuration with the given name.
	 * @param name
	 * @return
	 */
	public static Preferences getConfigPreferences(String name) {
		IScopeContext scope = new InstanceScope();
		IEclipsePreferences root = scope.getNode(CONFIGQUALIFIER);
		Preferences confignode = root.node(name);
		return confignode;
	}
	
	/**
	 * Saves the changed preferences.
	 * 
	 * This has to be called to make any changes to the PreferenceStore
	 * persistent.
	 * 
	 * @param store
	 * @throws IOException
	 */
	public static void savePreferences(IPreferenceStore store)
			throws IOException {
		Assert.isTrue(store instanceof ScopedPreferenceStore);
		((ScopedPreferenceStore) store).save();
	}

	/**
	 * Gets the default AVRDude preferences
	 * 
	 * @return
	 */
	public static IEclipsePreferences getDefaultPreferences() {
		IScopeContext scope = new DefaultScope();
		return scope.getNode(QUALIFIER);
	}

	/**
	 * Initialize the default AVRDude preference values.
	 * 
	 * This is called from
	 * {@link de.innot.avreclipse.core.preferences.PreferenceInitializer}.
	 * Clients are not supposed to call this method.
	 */
	public static void initializeDefaultPreferences() {
		IEclipsePreferences prefs = getDefaultPreferences();

		prefs.putBoolean(KEY_USECUSTOMCONFIG, Boolean.valueOf(false));
		prefs.put(KEY_CONFIGFILE, "");

		// TODO: make a Default configuration and add it to the Configurations
		// List

	}

}
