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
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.ListenerList;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.toolinfo.AVRDude;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TargetConfiguration implements ITargetConfiguration, ITargetConfigurationWorkingCopy,
		ITargetConfigConstants {

	private String				fId;

	private boolean				fDirty;

	private Preferences			fPrefs;

	private Map<String, String>	fAttributes	= new HashMap<String, String>();
	private Map<String, String>	fDefaults	= new HashMap<String, String>();

	/**
	 * List of registered listeners (element type: <code>ITargetConfigChangeListener</code>). These
	 * listeners are to be informed when the current value of an attribute changes.
	 */
	protected ListenerList		fListeners	= new ListenerList();

	/** The source target configuration if this is a working copy */
	private TargetConfiguration	fOriginal;

	private TargetConfiguration() {
		initDefaults();
	}

	/**
	 * Instantiate a new target configuration with the given id.
	 * <p>
	 * The constructor will set a few default values.
	 * </p>
	 * 
	 * @param id
	 */
	protected TargetConfiguration(String id) {
		this();
		fId = id;
		setDefaults();
		fDirty = false;
	}

	/**
	 * Constructs a TargetConfiguration with the given id and load its values from the Preferences.
	 * 
	 * @param id
	 *            Unique id of the configuration.
	 * @param prefs
	 *            <code>Preferences</code> node from which to load.
	 */
	protected TargetConfiguration(String id, Preferences prefs) {
		this();
		fId = id;
		try {
			loadFromPrefs(prefs);
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Make a Working copy of the given <code>TargetConfiguration</code>.
	 * <p>
	 * The copy can be safely modified without affecting the source until the {@link #doSave()}
	 * method is called, which will then copy all changes to the source configuration.
	 * </p>
	 * 
	 * @param config
	 */
	protected TargetConfiguration(TargetConfiguration config) {
		this();
		fOriginal = config;
		fId = config.fId;
		loadFromConfig(config);
	}

	/**
	 * @return
	 */
	public String getId() {
		return fId;
	}

	/**
	 * Get the name of the target configuration.
	 * 
	 * @return the Name
	 */
	public String getName() {
		return getAttribute(ATTR_NAME);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy#setName(java.lang.String)
	 */
	public void setName(String name) {
		setAttribute(ATTR_NAME, name);
	}

	/**
	 * Get the user supplied description of the target configuration.
	 * 
	 * @return the Name
	 */
	public String getDescription() {
		return getAttribute(ATTR_DESCRIPTION);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy#setDescription(java.lang
	 * .String)
	 */
	public void setDescription(String description) {
		setAttribute(ATTR_DESCRIPTION, description);
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#getMCUId()
	 */
	public String getMCUId() {
		return getAttribute(ATTR_MCU);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy#setMCU(java.lang.String)
	 */
	public void setMCU(String mcuid) {
		setAttribute(ATTR_MCU, mcuid);
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#getFCPU()
	 */
	public int getFCPU() {
		String fcpu = getAttribute(ATTR_FCPU);
		return Integer.parseInt(fcpu);
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy#setFCPU(int)
	 */
	public void setFCPU(int fcpu) {
		String value = Integer.toString(fcpu);
		setAttribute(ATTR_FCPU, value);
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#getSupportedMCUs(boolean)
	 */
	public List<String> getSupportedMCUs(boolean filtered) {
		// TODO This is a dummy
		List<String> allmcus = new ArrayList<String>();
		allmcus.add("atmega16");
		allmcus.add("atmega32");
		allmcus.add("at90s2323");

		if (!filtered) {
			allmcus.add("attiny12");
			allmcus.add("at90s4433");
			allmcus.add("atxmega64a3");
		}

		return allmcus;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#getSupportedProgrammers(boolean)
	 */
	public List<IProgrammer> getSupportedProgrammers(boolean filtered) {
		// TODO This is a dummy
		List<IProgrammer> programmerlist = null;
		try {
			programmerlist = AVRDude.getDefault().getProgrammersList();
		} catch (AVRDudeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return programmerlist;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#getProgrammer(java.lang.String)
	 */
	public IProgrammer getProgrammer(String programmerid) {
		// TODO This is a dummy
		IProgrammer programmer = null;
		try {
			programmer = AVRDude.getDefault().getProgrammer(programmerid);
		} catch (AVRDudeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return programmer;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy#isDirty()
	 */
	public boolean isDirty() {
		return fDirty;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy#doSave()
	 */
	public synchronized void doSave() throws BackingStoreException {

		if (fPrefs == null) {
			fPrefs = TargetConfigurationManager.getDefault().getPreferences(fId);
		}

		if (fDirty) {
			// write all values to the preferences
			for (String key : fAttributes.keySet()) {
				String value = fAttributes.get(key);
				fPrefs.put(key, value);
			}

			// flush the Preferences to the persistent storage
			fPrefs.flush();

			fDirty = false;

			if (fOriginal != null) {
				// Copy the changes to the original
				fOriginal.loadFromConfig(this);
			}
		}
	}

	/**
	 * Load the values of this Configuration from the preference storage area.
	 * 
	 * @param prefs
	 *            <code>Preferences</code> node for this configuration
	 * @throws BackingStoreException
	 */
	private void loadFromPrefs(Preferences prefs) throws BackingStoreException {
		fPrefs = prefs;
		for (String key : prefs.keys()) {
			String value = fPrefs.get(key, "");
			fAttributes.put(key, value);
		}
		fDirty = false;
	}

	/**
	 * Load the values of this Configuration from the given <code>TargetConfiguration</code>.
	 * 
	 * @param prefs
	 *            Source <code>TargetConfiguration</code>.
	 */
	private void loadFromConfig(TargetConfiguration config) {
		fAttributes.clear();
		for (String attr : config.fAttributes.keySet()) {
			setAttribute(attr, config.getAttribute(attr));
		}
		fDirty = config.fDirty;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy#setDefaults()
	 */
	public void setDefaults() {
		// Set the defaults
		for (String key : fDefaults.keySet()) {
			String defvalue = fDefaults.get(key);
			setAttribute(key, defvalue);
		}
		fDirty = true;
	}

	private void initDefaults() {
		fDefaults.put(ATTR_NAME, DEF_NAME);
		fDefaults.put(ATTR_DESCRIPTION, DEF_DESCRIPTION);
		fDefaults.put(ATTR_MCU, DEF_MCU);
		fDefaults.put(ATTR_FCPU, Integer.toString(DEF_FCPU));
		fDefaults.put(ATTR_PROGRAMMER_ID, DEF_PROGRAMMER_ID);
		fDefaults.put(ATTR_HOSTINTERFACE, DEF_HOSTINTERFACE);
		fDefaults.put(ATTR_PROGRAMMER_PORT, DEF_PROGRAMMER_PORT);
		fDefaults.put(ATTR_PROGRAMMER_BAUD, DEF_PROGRAMMER_BAUD);
		fDefaults.put(ATTR_BITBANGDELAY, DEF_BITBANGDELAY);
		fDefaults.put(ATTR_PAR_EXITSPEC, DEF_PAR_EXITSPEC);
		fDefaults.put(ATTR_USB_DELAY, DEF_USB_DELAY);
		fDefaults.put(ATTR_JTAG_CLOCK, DEF_JTAG_CLOCK);
		fDefaults.put(ATTR_JTAG_DAISYCHAIN, DEF_JTAG_DAISYCHAIN);
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#getAttribute(java.lang.String,
	 * java.lang.String)
	 */
	public String getAttribute(String attributeName) {
		Assert.isNotNull(attributeName);
		String value = fAttributes.get(attributeName);
		if (value == null) {
			value = fDefaults.get(attributeName);
		}
		return value;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy#setAttribute(java.lang.String
	 * , java.lang.String)
	 */
	public void setAttribute(String attributeName, String newvalue) {
		Assert.isNotNull(newvalue);
		Assert.isNotNull(attributeName);
		String oldvalue = fAttributes.get(attributeName);
		if (oldvalue == null || !oldvalue.equals(newvalue)) {
			// only change attribute & fire event if the value is actually changed
			fAttributes.put(attributeName, newvalue);
			fireAttributeChangeEvent(attributeName, oldvalue, newvalue);
			fDirty = true;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#getAttributes()
	 */
	public Map<String, String> getAttributes() {
		return new HashMap<String, String>(fAttributes);
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#isDebugCapable()
	 */
	public boolean isDebugCapable() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#isImageLoaderCapable()
	 */
	public boolean isImageLoaderCapable() {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.ITargetConfiguration#dispose()
	 */
	public void dispose() {
		fListeners.clear();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfiguration#addPropertyChangeListener(de.innot.
	 * avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener)
	 */
	public void addPropertyChangeListener(ITargetConfigChangeListener listener) {
		fListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.ITargetConfiguration#removePropertyChangeListener(de.innot
	 * .avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener)
	 */
	public void removePropertyChangeListener(ITargetConfigChangeListener listener) {
		fListeners.remove(listener);
	}

	/**
	 * Informs all registered listeners that an attribute has changed.
	 * 
	 * @param name
	 *            the name of the changed attribute
	 * @param oldValue
	 *            the old value, or <code>null</code> if not known or not relevant
	 * @param newValue
	 *            the new value, or <code>null</code> if not known or not relevant
	 */
	protected void fireAttributeChangeEvent(String name, String oldValue, String newValue) {
		if (name == null)
			throw new IllegalArgumentException();

		Object[] allListeners = fListeners.getListeners();

		// Don't fire anything if there are no listeners
		if (allListeners.length == 0) {
			return;
		}

		for (Object changeListener : allListeners) {
			ITargetConfigChangeListener listener = (ITargetConfigChangeListener) changeListener;
			listener.attributeChange(TargetConfiguration.this, name, oldValue, newValue);
		}
	}

	/**
	 * Listener for Target Configuration changes.
	 */
	public interface ITargetConfigChangeListener extends EventListener {

		/**
		 * Notification that a Target Configuration property has changed.
		 * <p>
		 * This method gets called when any attribute of the observered target configuration is
		 * changed.
		 * </p>
		 * 
		 * @param config
		 *            The <code>TargetConfiguration</code> which has changed
		 * @param name
		 *            the name of the changed attribute
		 * @param oldValue
		 *            the old value, or <code>null</code> if not known or not relevant
		 * @param newValue
		 *            the new value, or <code>null</code> if not known or not relevant
		 */
		public void attributeChange(ITargetConfiguration config, String attribute, String oldvalue,
				String newvalue);
	}

}
