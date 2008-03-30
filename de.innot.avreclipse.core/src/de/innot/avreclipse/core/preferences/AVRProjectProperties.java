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
package de.innot.avreclipse.core.preferences;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.avrdude.ProgrammerConfigManager;
import de.innot.avreclipse.core.toolinfo.AVRDude;

/**
 * Container for all AVR Plugin specific properties of a project.
 * <p>
 * Upon instantiation, the properties are loaded from the given preference
 * store. All changes are local to the object until the {@link #save()} method
 * is called.
 * </p>
 * <p>
 * AVRConfigurationProperties objects do not reflect changes made to other
 * AVRConfigurationProperties for the same Project/Configuration, so they should
 * not be held on to and be reloaded every time the current values are required.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class AVRProjectProperties {

	private static final String KEY_MCUTYPE = "MCUType";
	private static final String DEFAULT_MCUTYPE = "atmega16";

	private static final String KEY_FCPU = "ClockFrequency";
	private static final int DEFAULT_FCPU = 1000000;

	private static final String KEY_USE_EXT_RAM = "ExtendedRAM";
	private static final boolean DEFAULT_USE_EXT_RAM = false;

	private static final String KEY_EXT_RAM_SIZE = "ExtRAMSize";

	private static final String KEY_USE_EXT_RAM_HEAP = "UseExtendedRAMforHeap";

	private static final String KEY_USE_EEPROM = "UseEEPROM";
	private static final boolean DEFAULT_USE_EEPROM = false;

	private static final String KEY_AVRDUDE_PROGRAMMER = "avrdudeProgrammerID";

	private static final String KEY_AVRDUDE_BITCLOCK = "avrdudeBitclock";
	private static final String DEFAULT_AVRDUDE_BITCLOCK = "";

	private static final String KEY_AVRDUDE_NOSIGCHECK = "avrdudeNoSigCheck";
	private static final boolean DEFAULT_AVRDUDE_NOSIGCHECK = false;

	private static final String KEY_AVRDUDE_NOWRITE = "avrdudeNoWrite";
	private static final boolean DEFAULT_AVRDUDE_NOWRITE = false;

	private static final String KEY_AVRDUDE_USECOUNTER = "avrdudeUseCounter";
	private static final boolean DEFAULT_AVRDUDE_USECOUNTER = false;

	private String fMCUid;
	private int fFCPU;

	private boolean fUseExtRAM;
	private int fExtRAMSize;
	private boolean fUseExtRAMforHeap;
	private boolean fUseEEPROM;

	private ProgrammerConfig fAVRDudeProgrammer;
	private String fAVRDudeProgrammerId;
	private String fAVRDudeBitclock;
	private boolean fAVRDudeNoSigCheck;
	private boolean fAVRDudeNoWrite;
	private boolean fAVRDudeUseCounter;

	/**
	 * The source/target Preferences for the properties or <code>null</code>
	 * if default properties are represented.
	 */
	private IEclipsePreferences fPrefs;

	/** Flag if any properties have been changed */
	private boolean fDirty;

	/**
	 * Load the AVR project properties from the given Preferences.
	 * 
	 * @param prefs
	 *            <code>IEclipsePreferences</code>
	 */
	public AVRProjectProperties(IEclipsePreferences prefs) {
		fPrefs = prefs;
		loadData();
	}

	/**
	 * Load the AVR Project properties from the given
	 * <code>AVRConfigurationProperties</code> object.
	 * 
	 * @param source
	 */
	public AVRProjectProperties(IEclipsePreferences prefs, AVRProjectProperties source) {
		fPrefs = prefs;
		fMCUid = source.fMCUid;
		fFCPU = source.fFCPU;

		fUseExtRAM = source.fUseExtRAM;
		fExtRAMSize = source.fExtRAMSize;
		fUseExtRAMforHeap = source.fUseExtRAMforHeap;
		fUseEEPROM = source.fUseEEPROM;

		fAVRDudeProgrammer = source.fAVRDudeProgrammer;
		fAVRDudeProgrammerId = source.fAVRDudeProgrammerId;
		fAVRDudeBitclock = source.fAVRDudeBitclock;
		fAVRDudeNoSigCheck = source.fAVRDudeNoSigCheck;
		fAVRDudeNoWrite = source.fAVRDudeNoWrite;
		fAVRDudeUseCounter = source.fAVRDudeUseCounter;

		fDirty = true;
	}

	public String getMCUId() {
		return fMCUid;
	}

	public void setMCUId(String mcuid) {
		if (!fMCUid.equals(mcuid)) {
			fMCUid = mcuid;
			fDirty = false;
		}
	}

	public String getFCPU() {
		return Integer.toString(fFCPU);
	}

	public void setFCPU(String fcpu) {
		int newvalue = Integer.parseInt(fcpu);
		if (fFCPU != newvalue) {
			fFCPU = newvalue;
			fDirty = true;
		}
	}

	
	public ProgrammerConfig getAVRDudeProgrammer() {
		if (fAVRDudeProgrammer == null) {
			return ProgrammerConfigManager.getDefault().getConfig(
			        fAVRDudeProgrammerId);
		} else {
			return fAVRDudeProgrammer;
		}
	}
	
	public void setAVRDudeProgrammer(ProgrammerConfig progcfg) {
		if (!progcfg.equals(fAVRDudeProgrammer)) {
			fAVRDudeProgrammer = progcfg;
			fAVRDudeProgrammerId = progcfg.getId();
			fDirty = false;
		}
	}
	
	public String getAVRDudeProgrammerId() {
		return fAVRDudeProgrammerId;
	}

	public void setAVRDudeProgrammerId(String programmerid) {
		if (!fAVRDudeProgrammerId.equals(programmerid)) {
			fAVRDudeProgrammerId = programmerid;
			fAVRDudeProgrammer = null;
			fDirty = true;
		}
	}

	public String getAVRDudeBitclock() {
		return fAVRDudeBitclock;
	}

	public void setAVRDudeBitclock(String bitclock) {
		if (!fAVRDudeBitclock.equals(bitclock)) {
			fAVRDudeBitclock = bitclock;
			fDirty = true;
		}
	}

	public boolean getAVRDudeNoSigCheck() {
		return fAVRDudeNoSigCheck;
	}

	public void setAVRDudeNoSigCheck(boolean nosigcheck) {
		if (fAVRDudeNoSigCheck != nosigcheck) {
			fAVRDudeNoSigCheck = nosigcheck;
			fDirty = true;
		}
	}

	public boolean getAVRDudeNoWrite() {
		return fAVRDudeNoWrite;
	}

	public void setAVRDudeNoWrite(boolean nowrite) {
		if (fAVRDudeNoWrite != nowrite) {
			fAVRDudeNoWrite = nowrite;
			fDirty = true;
		}
	}

	public boolean getAVRDudeUseCounter() {
		return fAVRDudeUseCounter;
	}

	public void setAVRDudeUseCounter(boolean usecounter) {
		if (fAVRDudeUseCounter != usecounter) {
			fAVRDudeUseCounter = usecounter;
			fDirty = true;
		}
	}

	/**
	 * Gets the avrdude command arguments as defined by the properties.
	 * 
	 * @return <code>List&lt;String&gt;</code> with the avrdude options, one
	 *         per list entry.
	 */
	public List<String> getAVRDudeArguments() {
		List<String> arguments = new ArrayList<String>();

		// Convert the mcu id to the avrdude format and add it
		String avrdudemcuid = AVRDude.getDefault().getMCUInfo(fMCUid);
		arguments.add("-p" + avrdudemcuid);

		// Add the options from the programmer configuration
		ProgrammerConfig progcfg = getAVRDudeProgrammer();

		if (progcfg != null) {
			arguments.addAll(progcfg.getArguments());
		}
		
		// add the bitclock and the flags
		if (fAVRDudeBitclock.length() != 0) {
			arguments.add("-B" + fAVRDudeBitclock);
		}

		if (fAVRDudeNoSigCheck) {
			arguments.add("-F");
		}

		if (fAVRDudeNoWrite) {
			arguments.add("-n");
		}

		if (fAVRDudeUseCounter) {
			arguments.add("-y");
		}

		return arguments;
	}

	/**
	 * Load all options from the preferences.
	 */
	protected void loadData() {
		fMCUid = fPrefs.get(KEY_MCUTYPE, DEFAULT_MCUTYPE);
		fFCPU = fPrefs.getInt(KEY_FCPU, DEFAULT_FCPU);

		fUseExtRAM = fPrefs.getBoolean(KEY_USE_EXT_RAM, DEFAULT_USE_EXT_RAM);
		fExtRAMSize = fPrefs.getInt(KEY_EXT_RAM_SIZE, 0);
		fUseExtRAMforHeap = fPrefs.getBoolean(KEY_USE_EXT_RAM_HEAP, true);
		fUseEEPROM = fPrefs.getBoolean(KEY_USE_EEPROM, DEFAULT_USE_EEPROM);

		fAVRDudeProgrammerId = fPrefs.get(KEY_AVRDUDE_PROGRAMMER, "");
		fAVRDudeBitclock = fPrefs.get(KEY_AVRDUDE_BITCLOCK, DEFAULT_AVRDUDE_BITCLOCK);
		fAVRDudeNoSigCheck = fPrefs.getBoolean(KEY_AVRDUDE_NOSIGCHECK, DEFAULT_AVRDUDE_NOSIGCHECK);
		fAVRDudeNoWrite = fPrefs.getBoolean(KEY_AVRDUDE_NOWRITE, DEFAULT_AVRDUDE_NOWRITE);
		fAVRDudeUseCounter = fPrefs.getBoolean(KEY_AVRDUDE_USECOUNTER, DEFAULT_AVRDUDE_USECOUNTER);

		fDirty = false;
	}

	/**
	 * Save the modified properties to the persistent storage.
	 * 
	 * @throws BackingStoreException
	 */
	public void save() throws BackingStoreException {

		try {
			if (fDirty) {
				fDirty = false;
				fPrefs.put(KEY_MCUTYPE, fMCUid);
				fPrefs.putInt(KEY_FCPU, fFCPU);

				fPrefs.putBoolean(KEY_USE_EXT_RAM, fUseExtRAM);
				fPrefs.putInt(KEY_EXT_RAM_SIZE, fExtRAMSize);
				fPrefs.putBoolean(KEY_USE_EXT_RAM_HEAP, fUseExtRAMforHeap);
				fPrefs.putBoolean(KEY_USE_EEPROM, fUseEEPROM);

				fPrefs.put(KEY_AVRDUDE_PROGRAMMER, fAVRDudeProgrammerId);
				fPrefs.put(KEY_AVRDUDE_BITCLOCK, fAVRDudeBitclock);
				fPrefs.putBoolean(KEY_AVRDUDE_NOSIGCHECK, fAVRDudeNoSigCheck);
				fPrefs.putBoolean(KEY_AVRDUDE_NOWRITE, fAVRDudeNoWrite);
				fPrefs.putBoolean(KEY_AVRDUDE_USECOUNTER, fAVRDudeUseCounter);

				fPrefs.flush();
				
				if (fAVRDudeProgrammer != null) {
					ProgrammerConfigManager.getDefault().saveConfig(fAVRDudeProgrammer);
				}
			}
		} catch (IllegalStateException ise) {
			// This should not happen, but just in case we ignore this unchecked
			// exception
			ise.printStackTrace();
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append(fDirty ? "*" : " ");
		sb.append("[");
		sb.append("mcuid=" + fMCUid);
		sb.append(", fcpu=" + fFCPU);
		sb.append("]");
		return sb.toString();
	}

}
