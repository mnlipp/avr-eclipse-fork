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
package de.innot.avreclipse.core.preferences.Attic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.commands.common.EventManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.jface.util.SafeRunnable;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.AVRPluginActivator;

/**
 * This class handles the target hardware properties.
 * 
 * These properties are stored per project.
 * 
 * 
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public class AVRTargetPreferenceStore extends EventManager implements
		IPreferenceStore, IPersistentPreferenceStore {

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

	private static final String QUALIFIER = AVRPluginActivator.PLUGIN_ID;
	private static final String CLASS_NODE = "avrtarget";
	private static final String PROJECTSCOPE = "all";

	private IScopeContext fDefaultContext = new DefaultScope();
	private IScopeContext fProjectContext = null;
	private boolean fIsPerConfig = false;
	private String fConfigName = PROJECTSCOPE;

	private IPreferenceChangeListener fChangeListener = null;
	private boolean fSilent = false;
	private boolean fDirty = false;

	public AVRTargetPreferenceStore(IProject project) {
		fProjectContext = new ProjectScope(project);
	}

	public AVRTargetPreferenceStore(IConfiguration configuration) {
		IProject project = (IProject) configuration.getManagedProject()
				.getOwner();

		fProjectContext = new ProjectScope(project);
		fConfigName = configuration.getName();
		fIsPerConfig = true;
	}

	public void setConfiguration(IConfiguration config) {
		if (config == null) {
			fConfigName = null;
			fIsPerConfig = false;
		}
		
		fConfigName = config.getName();
		fIsPerConfig = true;
	}

	private String internalGet(String key) {
		return Platform.getPreferencesService().get(key, null,
				getPreferenceNodes(true));
	}

	private Preferences[] getPreferenceNodes(boolean includeDefault) {

		List<Preferences> prefslist = new ArrayList<Preferences>(1);
		if ((!PROJECTSCOPE.equals(fConfigName)) && isPerConfig()) {
			prefslist.add(getConfigPreferences());
		}
		prefslist.add(getProjectPreferences());
		if (includeDefault) {
			prefslist.add(getDefaultPreferences());
		}
		return prefslist.toArray(new Preferences[prefslist.size()]);
	}

	/**
	 * Gets the active preferences. Either the Configuration preferences, if
	 * they have been set or the Project preferences otherwise.
	 * 
	 * @return Preferences for the current scope of this store
	 */
	private Preferences getActivePreferences() {
		if (isPerConfig()) {
			return getConfigPreferences();
		}
		return getProjectPreferences();
	}

	private Preferences getConfigPreferences() {
		return fProjectContext.getNode(QUALIFIER).node(CLASS_NODE).node(
				fConfigName);
	}

	private Preferences getProjectPreferences() {
		return fProjectContext.getNode(QUALIFIER).node(CLASS_NODE).node(
				PROJECTSCOPE);
	}

	private Preferences getDefaultPreferences() {
		return fDefaultContext.getNode(QUALIFIER).node(CLASS_NODE);
	}

	public static void initializeDefaultPreferences(
			IEclipsePreferences defaultPreferences) {
		Preferences prefs = defaultPreferences.node(CLASS_NODE);
		prefs.putBoolean(KEY_PER_CONFIG, DEFAULT_PER_CONFIG);
		prefs.put(KEY_MCUTYPE, DEFAULT_MCUTYPE);
		prefs.putInt(KEY_FCPU, DEFAULT_FCPU);
		prefs.putBoolean(KEY_USE_EEPROM, DEFAULT_USE_EEPROM);
		prefs.putBoolean(KEY_USE_EXT_RAM, DEFAULT_USE_EXT_RAM);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#addPropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		// generate a PreferenceChangeListener if it does not exist
		// This listener will listen to all change events of the underlying
		// Project Preferences and propagate them to all listeners
		if (fChangeListener == null) {
			fChangeListener = new PreferenceChangeListener();
			fProjectContext.getNode(QUALIFIER).addPreferenceChangeListener(
					fChangeListener);
		}
		addListenerObject(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#removePropertyChangeListener(org.eclipse.jface.util.IPropertyChangeListener)
	 */
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		removeListenerObject(listener);
		if (!isListenerAttached()) {
			fProjectContext.getNode(QUALIFIER).removePreferenceChangeListener(
					fChangeListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#firePropertyChangeEvent(java.lang.String,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void firePropertyChangeEvent(String name, Object oldValue,
			Object newValue) {

		if (fSilent) {
			return;
		}

		final Object[] listenerlist = getListeners();
		if (listenerlist.length == 0) {
			return;
		}
		final PropertyChangeEvent event = new PropertyChangeEvent(this, name,
				oldValue, newValue);
		for (Object listener : listenerlist) {
			((IPropertyChangeListener) listener).propertyChange(event);
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#contains(java.lang.String)
	 */
	public boolean contains(String name) {
		if (name == null) {
			return false;
		}
		return (Platform.getPreferencesService().get(name, null,
				getPreferenceNodes(true))) != null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultBoolean(java.lang.String)
	 */
	public boolean getDefaultBoolean(String name) {
		return getDefaultPreferences()
				.getBoolean(name, BOOLEAN_DEFAULT_DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultDouble(java.lang.String)
	 */
	public double getDefaultDouble(String name) {
		return getDefaultPreferences().getDouble(name, DOUBLE_DEFAULT_DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultFloat(java.lang.String)
	 */
	public float getDefaultFloat(String name) {
		return getDefaultPreferences().getFloat(name, FLOAT_DEFAULT_DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultInt(java.lang.String)
	 */
	public int getDefaultInt(String name) {
		return getDefaultPreferences().getInt(name, INT_DEFAULT_DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultLong(java.lang.String)
	 */
	public long getDefaultLong(String name) {
		return getDefaultPreferences().getLong(name, LONG_DEFAULT_DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDefaultString(java.lang.String)
	 */
	public String getDefaultString(String name) {
		return getDefaultPreferences().get(name, STRING_DEFAULT_DEFAULT);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String name) {
		String value = internalGet(name);
		return value == null ? BOOLEAN_DEFAULT_DEFAULT : Boolean.valueOf(value)
				.booleanValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getDouble(java.lang.String)
	 */
	public double getDouble(String name) {
		String value = internalGet(name);
		if (value == null) {
			return DOUBLE_DEFAULT_DEFAULT;
		}
		try {
			return Double.parseDouble(value);
		} catch (NumberFormatException e) {
			return DOUBLE_DEFAULT_DEFAULT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getFloat(java.lang.String)
	 */
	public float getFloat(String name) {
		String value = internalGet(name);
		if (value == null) {
			return FLOAT_DEFAULT_DEFAULT;
		}
		try {
			return Float.parseFloat(value);
		} catch (NumberFormatException e) {
			return FLOAT_DEFAULT_DEFAULT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getInt(java.lang.String)
	 */
	public int getInt(String name) {
		String value = internalGet(name);
		if (value == null) {
			return INT_DEFAULT_DEFAULT;
		}
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return INT_DEFAULT_DEFAULT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getLong(java.lang.String)
	 */
	public long getLong(String name) {
		String value = internalGet(name);
		if (value == null) {
			return LONG_DEFAULT_DEFAULT;
		}
		try {
			return Long.parseLong(value);
		} catch (NumberFormatException e) {
			return LONG_DEFAULT_DEFAULT;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#getString(java.lang.String)
	 */
	public String getString(String name) {
		String value = internalGet(name);
		return value == null ? STRING_DEFAULT_DEFAULT : value;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#isDefault(java.lang.String)
	 */
	public boolean isDefault(String name) {
		if (name == null) {
			return false;
		}
		return (Platform.getPreferencesService().get(name, null,
				getPreferenceNodes(false))) == null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#needsSaving()
	 */
	public boolean needsSaving() {
		return fDirty;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#putValue(java.lang.String,
	 *      java.lang.String)
	 */
	public void putValue(String name, String value) {
		try {
			// Do not notify listeners
			fSilent = true;
			getActivePreferences().put(name, value);
		} finally {
			fSilent = false;
			fDirty = true;
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
	 *      boolean)
	 */
	public void setDefault(String key, boolean value) {
		getDefaultPreferences().putBoolean(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
	 *      double)
	 */
	public void setDefault(String key, double value) {
		getDefaultPreferences().putDouble(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
	 *      float)
	 */
	public void setDefault(String key, float value) {
		getDefaultPreferences().putFloat(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
	 *      int)
	 */
	public void setDefault(String key, int value) {
		getDefaultPreferences().putInt(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
	 *      long)
	 */
	public void setDefault(String key, long value) {
		getDefaultPreferences().putLong(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setDefault(java.lang.String,
	 *      java.lang.String)
	 */
	public void setDefault(String key, String value) {
		getDefaultPreferences().put(key, value);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.IPreferenceStore#setToDefault(java.lang.String)
	 */
	public void setToDefault(String key) {

		String oldValue = getString(key);
		String defaultValue = null;
		Preferences prefs = null;

		if (isPerConfig()) {
			defaultValue = getProjectPreferences().get(key,
					getDefaultString(key));
			prefs = getConfigPreferences();
		} else {
			defaultValue = getDefaultString(key);
			prefs = getProjectPreferences();
		}
		try {
			fSilent = true;
			prefs.remove(key);
			fDirty = true;
			firePropertyChangeEvent(key, oldValue, defaultValue);
		} finally {
			fSilent = false;
		}

	}

	public void setValue(String name, double value) {

	}

	public void setValue(String name, float value) {

	}

	public void setValue(String name, int value) {

	}

	public void setValue(String name, long value) {

	}

	public void setValue(String name, String value) {

	}

	public void setValue(String name, boolean value) {
		boolean oldValue = getBoolean(name);
		if (oldValue == value) {
			return;
		}
		try {
			silentRunning = true;// Turn off updates from the store
			if (getDefaultBoolean(name) == value) {
				getStorePreferences().remove(name);
			} else {
				getStorePreferences().putBoolean(name, value);
			}
			dirty = true;
			firePropertyChangeEvent(name, oldValue ? Boolean.TRUE
					: Boolean.FALSE, value ? Boolean.TRUE : Boolean.FALSE);
		} finally {
			silentRunning = false;// Restart listening to preferences
		}
	}

	public void save() throws IOException {
		try {
			getProjectPreferences().flush();
			fDirty = false;
		} catch (BackingStoreException e) {
			throw new IOException(e.getMessage());
		}
	}

	private boolean isPerConfig() {
		return getProjectPreferences().getBoolean(KEY_PER_CONFIG, false);
	}

	private class PreferenceChangeListener implements IPreferenceChangeListener {

		public void preferenceChange(PreferenceChangeEvent event) {
			Object oldValue = event.getOldValue();
			Object newValue = event.getNewValue();
			String key = event.getKey();
			if (newValue == null) {
				newValue = getDefault(key, oldValue);
			} else if (oldValue == null) {
				oldValue = getDefault(key, newValue);
			}
			firePropertyChangeEvent(event.getKey(), oldValue, newValue);

		}

	}

	Object getDefault(String key, Object obj) {
		Preferences defaults = getDefaultPreferences();
		if (obj instanceof String) {
			return defaults.get(key, STRING_DEFAULT_DEFAULT);
		} else if (obj instanceof Integer) {
			return new Integer(defaults.getInt(key, INT_DEFAULT_DEFAULT));
		} else if (obj instanceof Double) {
			return new Double(defaults.getDouble(key, DOUBLE_DEFAULT_DEFAULT));
		} else if (obj instanceof Float) {
			return new Float(defaults.getFloat(key, FLOAT_DEFAULT_DEFAULT));
		} else if (obj instanceof Long) {
			return new Long(defaults.getLong(key, LONG_DEFAULT_DEFAULT));
		} else if (obj instanceof Boolean) {
			return defaults.getBoolean(key, BOOLEAN_DEFAULT_DEFAULT) ? Boolean.TRUE : Boolean.FALSE;
		} else {
			return null;
		}
	}
}
