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
package de.innot.avreclipse.core.preferences;

import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import de.innot.avreclipse.AVRPlugin;

/**
 * This class handles access to the target hardware properties.
 * 
 * These properties are stored per project.
 * 
 * 
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public class AVRTargetProperties {

	public static final String KEY_AVR_ID = "AVRsettings";
	public static final String KEY_PER_CONFIG = "perConfig";
	public static final String KEY_MCUTYPE = "MCUType";
	public static final String KEY_FCPU = "ClockFrequency";
	public static final String KEY_USE_EXT_RAM = "ExtendedRAM";
	public static final String KEY_USE_EXT_RAM_HEAP = "UseExtendedRAMforHeap";
	public static final String KEY_USE_EXT_RAM_DATA = "UseExtendedRAMforData";
	public static final String KEY_USE_EEPROM = "UseEEPROM";

	private static final boolean DEFAULT_PER_CONFIG = false;
	private static final String DEFAULT_MCUTYPE = "atmega16";
	private static final int DEFAULT_FCPU = 1000000;
	private static final boolean DEFAULT_USE_EEPROM = false;
	private static final boolean DEFAULT_USE_EXT_RAM = false;

	private static final String CLASSNAME = "avrtarget";
	private static final String QUALIFIER = AVRPlugin.PLUGIN_ID + "/" + CLASSNAME;

	/**
	 * Gets the project AVR Target Hardware properties.
	 * 
	 * @param project
	 *            The project for which to get the properties
	 * @return IPreferenceStore with the properties
	 */
	public static IPreferenceStore getPropertyStore(IProject project) {

		Assert.isNotNull(project);
		IScopeContext scope = new ProjectScope(project);
		IPreferenceStore store = new ScopedPreferenceStore(scope, QUALIFIER);
		return store;
	}

	/**
	 * Gets the project build configuration AVR Target Hardware properties.
	 * 
	 * The returned store is backed by the project properties, so properties
	 * not set for the build configuration will fall back to the project 
	 * settings.
	 * 
	 * @param configuration
	 *            The build configuration for which to get the properties
	 * @return IPreferenceStore with the properties
	 */
	public static IPreferenceStore getPropertyStore(IConfiguration configuration) {
		
		Assert.isNotNull(configuration);
		IScopeContext configscope = new BuildConfigurationScope(configuration);
		ScopedPreferenceStore store = new ScopedPreferenceStore(configscope, QUALIFIER);
		
		IProject project = (IProject) configuration.getManagedProject()
				.getOwner();
		IScopeContext projectscope = new ProjectScope(project);
		
		store.setSearchContexts(new IScopeContext[] {configscope, projectscope});
		
		return store;
	}

	/**
	 * Saves the changed properties.
	 * 
	 * This has to be called to make any changes to the PreferenceStore persistent.
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
	 * Gets the default Target Hardware properties
	 * 
	 * @return
	 */
	public static IEclipsePreferences getDefaultPreferences() {
		IScopeContext scope = new DefaultScope();
		return scope.getNode(QUALIFIER);
	}
	
	/**
	 * Initialize the default property values.
	 * 
	 * This is called from {@link de.innot.avreclipse.core.preferences.PreferenceInitializer}
	 */
	public static void initializeDefaultPreferences() {
		IEclipsePreferences prefs = getDefaultPreferences();
		prefs.putBoolean(KEY_PER_CONFIG, DEFAULT_PER_CONFIG);
		prefs.put(KEY_MCUTYPE, DEFAULT_MCUTYPE);
		prefs.putInt(KEY_FCPU, DEFAULT_FCPU);
		prefs.putBoolean(KEY_USE_EEPROM, DEFAULT_USE_EEPROM);
		prefs.putBoolean(KEY_USE_EXT_RAM, DEFAULT_USE_EXT_RAM);

	}
}
