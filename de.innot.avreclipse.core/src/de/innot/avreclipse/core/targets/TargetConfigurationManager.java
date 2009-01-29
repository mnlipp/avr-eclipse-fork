/*******************************************************************************
 * 
 * Copyright (c) 2008 Thomas Holland (thomas@innot.de) and others
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.preferences.AVRDudePreferences;

/**
 * Manages the list of {@link TargetConfiguration} objects in the preferences.
 * <p>
 * This manager has methods to
 * <ul>
 * <li>get a configuration from the preferences: {@link #getConfig(String)},</li>
 * <li>get a working copy configuration: {@link #getWorkingCopy(String)},</li>
 * <li>create a new configuration: {@link #createNewConfig()},</li>
 * <li>save a configuration: {@link #saveConfig(ProgrammerConfig)} and</li>
 * <li>delete a configuration: {@link #deleteConfig(ProgrammerConfig)}</li>
 * </p>
 * <p>
 * The manager also has methods to get a list of all available programmers and their names:
 * {@link #getAllConfigIDs()} and {@link #getAllConfigNames()}.
 * </p>
 * <p>
 * To improve access times all retrieved configurations are stored in an internal cache.
 * </p>
 * <p>
 * This class implements the singleton pattern and can be accessed with the static
 * {@link #getDefault()} method.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TargetConfigurationManager {

	/** Static singleton instance */
	private static TargetConfigurationManager		fInstance		= null;

	private static final String						CLASSNAME		= "targets";
	private static final String						QUALIFIER		= AVRPlugin.PLUGIN_ID + "/"
																			+ CLASSNAME;
	private static final String						CONFIGQUALIFIER	= QUALIFIER + "/configs/";

	/**
	 * The prefix for programmer configuration id values. This is appended with a running number to
	 * get the real id.
	 */
	private final static String						CONFIG_PREFIX	= "targetconfig.";

	/** Cache of all Configs that have been used in this session */
	private final Map<String, TargetConfiguration>	fConfigsCache;

	/**
	 * The preferences this manager works on.
	 * 
	 * @see AVRDudePreferences#getConfigPreferences()
	 */
	private final IEclipsePreferences				fPreferences;

	private final static List<String>				EMPTYLIST		= new ArrayList<String>();

	/**
	 * Gets the session <code>TargetConfigurationManager</code>.
	 * 
	 * @return <code>TargetConfigurationManager</code> for the current Eclipse session.
	 */
	public static TargetConfigurationManager getDefault() {
		if (fInstance == null) {
			fInstance = new TargetConfigurationManager();
		}
		return fInstance;
	}

	// Private to prevent instantiation of this class.
	private TargetConfigurationManager() {
		// Set up Preferences and the internal lists
		IScopeContext scope = new InstanceScope();
		fPreferences = scope.getNode(CONFIGQUALIFIER);
		fConfigsCache = new HashMap<String, TargetConfiguration>();
	}

	/**
	 * Create a new TargetConfiguration.
	 * <p>
	 * The returned TargetConfiguration is filled with some default values. It is immediately
	 * created in the preference store.
	 * </p>
	 * 
	 * @return A new <code>ProgrammerConfig</code>
	 */
	public ITargetConfiguration createNewConfig() {

		// The id has the form "targetconfig.#" where # is a running
		// number.
		// Find the first free id.
		// Check the list of existing config nodes in the preferences.
		String newid = null;
		int i = 1;

		try {
			do {
				newid = CONFIG_PREFIX + i++;
			} while (fPreferences.nodeExists(newid));

			TargetConfiguration newconfig = new TargetConfiguration(newid);
			newconfig.doSave();
			fConfigsCache.put(newconfig.getId(), newconfig);

			return newconfig;

		} catch (BackingStoreException bse) {
			// TODO What shall we do if we can't access the Preferences?
			// For now log an error and return null.
			logException(bse);
			return null;
		}
	}

	/**
	 * Deletes the given configuration from the preference storage area.
	 * <p>
	 * Note: This Object is still valid and further calls to {@link #saveConfig(ProgrammerConfig)}
	 * will add this configuration back to the preference storage.
	 * </p>
	 * 
	 * @param id
	 *            The id of the target configuration to delete.
	 */
	public void deleteConfig(String id) {

		// If the config is in the cache, remove it from the cache
		if (fConfigsCache.containsKey(id)) {
			// First clear any listeners so that we don't have dangling references
			ITargetConfiguration tc = fConfigsCache.get(id);
			tc.dispose();
			fConfigsCache.remove(id);
		}

		// Remove the Preference node for the config and flush the preferences
		// If the node does not exist do nothing - no need to create the node
		// just to remove it again
		try {
			if (fPreferences.nodeExists(id)) {
				Preferences cfgnode = fPreferences.node(id);
				cfgnode.removeNode();
				fPreferences.flush();
			}
		} catch (BackingStoreException bse) {
			logException(bse);
		}
	}

	/**
	 * Get the {@link TargetConfiguration} with the given ID.
	 * <p>
	 * If the config has been requested before, a reference to the config in the internal cache is
	 * returned. All modifications to the returned config will affect the config in the cache.
	 * </p>
	 * <p>
	 * While these changes are only persisted when saveConfig() is called, it is usually better to
	 * use the {@link #getWorkingCopy(String)} call to get a safely modifiable config.
	 * </p>
	 * 
	 * @see #getWorkingCopy(String)
	 * 
	 * @param id
	 *            <code>String</code> with an ID value.
	 * @return The requested <code>TargetConfiguration</code> or <code>null</code> if no config with
	 *         the given ID exists.
	 */
	public ITargetConfiguration getConfig(String id) {

		return internalGetConfig(id);
	}

	private TargetConfiguration internalGetConfig(String id) {
		// Test for empty / null id
		if (id == null)
			return null;
		if (id.length() == 0)
			return null;

		// Test if the config is already in the cache
		if (fConfigsCache.containsKey(id)) {
			return fConfigsCache.get(id);
		}

		// The config was not in the cache

		// The node must exist, otherwise return null
		try {

			if (!fPreferences.nodeExists(id)) {
				return null;
			}
		} catch (BackingStoreException bse) {
			// TODO What shall we do if we can't access the Preferences?
			// For now log an error and return null.
			logException(bse);
			return null;
		}

		// Load the Config from the Preferences
		Preferences cfgprefs = fPreferences.node(id);
		TargetConfiguration config = new TargetConfiguration(id, cfgprefs);

		fConfigsCache.put(id, config);

		return config;
	}

/**
	 * Get a working copy of the {@link TargetConfiguration} with the given Id.
	 * <p>
	 * The returned config is not backed by the cache, so any modifications will not be visible
	 * until the {@link #saveConfig(ProgrammerConfig) method is called with the returned config.<p>
	 * </p>
	 * 
	 * @param sourceconfig
	 *            Source <code>TargetConfiguration</code> to clone.
	 * @return New working copy of an existing configuration, or <code>null</code> if no config with the given id exists.
	 */
	public ITargetConfigurationWorkingCopy getWorkingCopy(String id) {

		// Clone the source config
		TargetConfiguration sourceconfig = internalGetConfig(id);
		if (sourceconfig == null) {
			return null;
		}
		ITargetConfigurationWorkingCopy cloneconfig = new TargetConfiguration(sourceconfig);

		return cloneconfig;
	}

	/**
	 * Checks if a target configuration with the given id exists.
	 * 
	 * @param id
	 *            A target configuration id string
	 * @return <code>true</code> if the configuration exists.
	 */
	public boolean exists(String id) {
		return false;
	}

	/**
	 * Get a list of all available target configuration id's.
	 * 
	 * @return List of all id strings
	 */
	public List<String> getConfigurationIDs() {
		// All Programmer Configurations are children of the rootnode in the
		// preferences. So fetch all children and create a list from them.
		String[] confignames;

		try {
			confignames = fPreferences.childrenNames();
			return Arrays.asList(confignames);
		} catch (BackingStoreException bse) {
			// TODO What shall we do if we can't access the Preferences?
			// For now log an error and return an empty list
			logException(bse);
		}
		return EMPTYLIST;
	}

	public Preferences getPreferences(String id) {
		return fPreferences.node(id);
	}

	/**
	 * Log an BackingStoreException.
	 * 
	 * @param bse
	 *            <code>BackingStoreException</code> to log.
	 */
	private void logException(BackingStoreException bse) {
		// TODO Check if we really should do this here or if we just throw the
		// Exception all the way up to the GUI code to show an error dialog,
		// like in the saveConfig() and deleteConfig() methods (where this
		// Exception is more likely to happen as something is actually written
		// to the Preferences)
		// 
		Status status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
				"Can't access the list of avrdude configuration preferences", bse);
		AVRPlugin.getDefault().log(status);
	}
}
