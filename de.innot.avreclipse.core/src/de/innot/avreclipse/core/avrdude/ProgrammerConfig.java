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
package de.innot.avreclipse.core.avrdude;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.core.preferences.AVRDudePreferences;

/**
 * Container class for all Programmer specific options of AVRDude.
 * <p>
 * This class also acts as an Interface to the preference store. It knows how to
 * save and delete configurations.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class ProgrammerConfig {

	/**
	 * The root preferences for all configurations. Needed to add / remove
	 * configurations.
	 */
	private IEclipsePreferences fConfigPrefs;

	/** The preference node for this configuration */
	private Preferences fCurrentPrefs;

	/** The unique name of this configuration */
	private String fName;
	public final static String KEY_NAME = "name";

	/** A custom description of this configuration */
	private String fDescription;
	public final static String KEY_DESCRIPTION = "description";

	/** The avrdude id of the programmer for this configuration */
	private String fProgrammer;
	public final static String KEY_PROGRAMMER = "programmer";

	/**
	 * The port for this configuration. If empty it will not be included in the
	 * command line arguments.
	 */
	private String fPort;
	public final static String KEY_PORT = "port";

	/**
	 * The baudrate for this configuration. If empty it will not be included in
	 * the command line arguments.
	 */
	private String fBaudrate;
	public final static String KEY_BAUDRATE = "baudrate";

	/**
	 * The Exitspec for the resetline. If empty it will not be included in the
	 * command line arguments.
	 * <p>
	 * Valid values are "reset", "noreset" and ""
	 * </p>
	 */
	private String fExitReset;
	public final static String KEY_EXITSPEC_RESET = "ppresetline";

	/**
	 * The Exitspec for the Vcc lines. If empty or <code>null</code> it will
	 * not be included in the command line arguments.
	 * <p>
	 * Valid values are "vcc", "novcc" and ""
	 * </p>
	 */
	private String fExitVcc;
	public final static String KEY_EXITSPEC_VCC = "ppvccline";

	/** Flag to mark modifications of this config */
	private boolean fDirty;

	/**
	 * Constructs a ProgrammerConfig with the given name.
	 * <p>
	 * If the configuration already existed, its stored values are loaded from
	 * the persistent preference storage.
	 * </p>
	 * 
	 * @param name
	 *            Unique name of the configuration.
	 */
	public ProgrammerConfig(String name) {
		fName = name;
		fConfigPrefs = AVRDudePreferences.getConfigPreferences();
		fCurrentPrefs = fConfigPrefs.node(name);
		readConfig();
		fDirty = false;
	}

	/**
	 * Make a copy of the given <code>ProgrammerConfig</code>.
	 * <p>
	 * The copy does not reflect any changes of the original or vv.
	 * </p>
	 * Note: This copy can be saved, even when the given original has been
	 * deleted.
	 * </p>
	 * 
	 * @param config
	 */
	public ProgrammerConfig(ProgrammerConfig config) {
		fName = config.fName;
		fConfigPrefs = config.fConfigPrefs;
		fCurrentPrefs = config.fCurrentPrefs;
		fDescription = config.fDescription;
		fProgrammer = config.fProgrammer;
		fPort = config.fPort;
		fBaudrate = config.fBaudrate;
		fExitReset = config.fExitReset;
		fExitVcc = config.fExitVcc;
		fDirty = config.fDirty;
	}

	/**
	 * Persist this configuration to the preference storage.
	 * <p>
	 * This will not do anything if the configuration has not been modified.
	 * </p>
	 * 
	 * @throws BackingStoreException
	 *             If this configuration cannot be written to the preference
	 *             storage area.
	 */
	public synchronized void save() throws BackingStoreException {

		if (fDirty) {
			// In case the name has changed we remove the old preference node of
			// this configuration and create a new node with the current name
			try {
				fCurrentPrefs.removeNode();
			} catch (IllegalStateException ise) {
				// The node has already been removed (by another Instance of
				// this config)
				// For now we ignore this and happily write the configuration
				// back to the storage.
				// TODO Should removed configuration not be re-saveable?
			}
			fCurrentPrefs = fConfigPrefs.node(fName);

			// write all values to the preferences
			fCurrentPrefs.put(KEY_DESCRIPTION, fDescription);
			fCurrentPrefs.put(KEY_PROGRAMMER, fProgrammer);
			fCurrentPrefs.put(KEY_PORT, fPort);
			fCurrentPrefs.put(KEY_BAUDRATE, fBaudrate);
			fCurrentPrefs.put(KEY_EXITSPEC_RESET, fExitReset);
			fCurrentPrefs.put(KEY_EXITSPEC_VCC, fExitVcc);

			// flush the Preferences to the persistent storage
			// we call this on the "root" node to flush the removed node
			fConfigPrefs.flush();
		}
	}

	/**
	 * Deletes this configuration from the preference storage area.
	 * <p>
	 * Note: This Object is still valid and further calls to {@link #save()}
	 * will add this configuration back to the preference storage.
	 * </p>
	 * 
	 * @throws BackingStoreException
	 */
	public synchronized void delete() throws BackingStoreException {
		fCurrentPrefs.removeNode();
		fConfigPrefs.flush();
	}

	/**
	 * @return A <code>List&lt;Strings&gt;</code> with all avrdude options as
	 *         defined by this configuration
	 */
	public List<String> getArguments() {

		List<String> args = new ArrayList<String>();

		args.add("-c" + fProgrammer);

		if (fPort.length() > 0) {
			args.add("-P" + fPort);
		}

		if (fBaudrate.length() > 0) {
			args.add("-b" + fBaudrate);
		}

		StringBuffer exitspec = new StringBuffer();
		if (fExitReset.length() > 0) {
			exitspec.append(fExitReset);
		}
		if (fExitVcc.length() > 0) {
			if (fExitReset.length() > 0) {
				exitspec.append(",");
			}
			exitspec.append(fExitVcc);
		}
		if (exitspec.length() > 0) {
			args.add("-E" + exitspec.toString());
		}
		return args;
	}

	/**
	 * Sets the name of this configuration.
	 * <p>
	 * The name must not contain any slashes ('/'), as this would cause problems
	 * with the preference store.
	 * </p>
	 * 
	 * @param name
	 *            <code>String</code> with the new name.
	 */
	public void setName(String name) {
		Assert.isTrue(!name.contains("/"));
		fName = name;
		fDirty = true;
	}

	/**
	 * @return The current name of this configuration.
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Sets the description of this configuration.
	 * 
	 * @param name
	 *            <code>String</code> with the new description.
	 */
	public void setDescription(String description) {
		fDescription = description;
		fDirty = true;
	}

	/**
	 * @return The current description of this configuration.
	 */
	public String getDescription() {
		return fDescription;
	}

	/**
	 * Sets the avrdude programmer id of this configuration.
	 * <p>
	 * The programmer id is not checked for validity. It is up to the caller to
	 * ensure that the given id is valid.
	 * </p>
	 * 
	 * @param name
	 *            <code>String</code> with the new programmer id.
	 */
	public void setProgrammer(String programmer) {
		fProgrammer = programmer;
		fDirty = true;
	}

	/**
	 * @return The current avrdude programmer id of this configuration.
	 */
	public String getProgrammer() {
		return fProgrammer;
	}

	/**
	 * Sets the port of this configuration.
	 * <p>
	 * The port name is not checked for validity. It is up to the caller to
	 * ensure that the port name is valid.
	 * </p>
	 * 
	 * @param name
	 *            <code>String</code> with the new port, may be an empty
	 *            String to use the avrdude default port.
	 */
	public void setPort(String port) {
		fPort = port;
		fDirty = true;
	}

	/**
	 * @return The current port of this configuration, empty if default is to be
	 *         used.
	 */
	public String getPort() {
		return fPort;
	}

	/**
	 * Sets the baudrate of this configuration.
	 * <p>
	 * The baudrate is not checked for validity. It is up to the caller to
	 * ensure that the baudrate is a valid integer (or empty).
	 * </p>
	 * 
	 * @param name
	 *            <code>String</code> with the new baudrate, may be an empty
	 *            String to use the avrdude default baudrate.
	 */
	public void setBaudrate(String baudrate) {
		fBaudrate = baudrate;
		fDirty = true;
	}

	/**
	 * @return The current baudrate of this configuration, empty if default is
	 *         to be used.
	 */
	public String getBaudrate() {
		return fBaudrate;
	}

	/**
	 * Sets the reset line ExitSpec of this configuration.
	 * <p>
	 * Only the values "reset", "noreset" and "" (empty String) are valid. It is
	 * up to the caller to ensure that the given value is valid.
	 * </p>
	 * 
	 * @param name
	 *            <code>String</code> with the resetline ExitSpec, may be an
	 *            empty String to use the avrdude default.
	 */
	public void setExitspecResetline(String resetline) {
		fExitReset = resetline;
		fDirty = true;
	}

	/**
	 * @return The current reset line ExitSpec of this configuration, empty if
	 *         default is to be used.
	 */
	public String getExitspecResetline() {
		return fExitReset;
	}

	/**
	 * Sets the Vcc lines ExitSpec of this configuration.
	 * <p>
	 * Only the values "vcc", "novcc" and "" (empty String) are valid.It is up
	 * to the caller to ensure that the given value is valid.
	 * </p>
	 * 
	 * @param name
	 *            <code>String</code> with the resetline ExitSpec, may be an
	 *            empty String to use the avrdude default.
	 */
	public void setExitspecVCCline(String vccline) {
		fExitVcc = vccline;
		fDirty = true;
	}

	/**
	 * @return The current Vcc lines ExitSpec of this configuration, empty if
	 *         default is to be used.
	 */
	public String getExitspecVCCline() {
		return fExitVcc;
	}

	/**
	 * Fill the values of this Configuration from the preference storage area.
	 */
	private void readConfig() {
		fDescription = fCurrentPrefs.get(KEY_DESCRIPTION, "");
		fProgrammer = fCurrentPrefs.get(KEY_PROGRAMMER, "");
		fPort = fCurrentPrefs.get(KEY_PORT, "");
		fBaudrate = fCurrentPrefs.get(KEY_BAUDRATE, "");
		fExitReset = fCurrentPrefs.get(KEY_EXITSPEC_RESET, "");
		fExitVcc = fCurrentPrefs.get(KEY_EXITSPEC_VCC, "");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		// for the debugger
		return fName + " (" + fDescription + "): " + getArguments();
	}

}
