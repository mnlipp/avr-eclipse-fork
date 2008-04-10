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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.core.avrdude.AVRDudeAction;
import de.innot.avreclipse.core.avrdude.AVRDudeActionFactory;
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
	private String fMCUid;

	private static final String KEY_FCPU = "ClockFrequency";
	private static final int DEFAULT_FCPU = 1000000;
	private int fFCPU;

	private static final String KEY_USE_EXT_RAM = "ExtendedRAM";
	private static final boolean DEFAULT_USE_EXT_RAM = false;
	private boolean fUseExtRAM;

	private static final String KEY_EXT_RAM_SIZE = "ExtRAMSize";
	private int fExtRAMSize;

	private static final String KEY_USE_EXT_RAM_HEAP = "UseExtendedRAMforHeap";
	private boolean fUseExtRAMforHeap;

	private static final String KEY_USE_EEPROM = "UseEEPROM";
	private static final boolean DEFAULT_USE_EEPROM = false;
	private boolean fUseEEPROM;

	private static final String KEY_AVRDUDE_PROGRAMMER = "avrdudeProgrammerID";
	private ProgrammerConfig fAVRDudeProgrammer;
	private String fAVRDudeProgrammerId;

	private static final String KEY_AVRDUDE_BITCLOCK = "avrdudeBitclock";
	private static final String DEFAULT_AVRDUDE_BITCLOCK = "";
	private String fAVRDudeBitclock;

	private static final String KEY_AVRDUDE_BITBANGDELAY = "avrdudeBitBangDelay";
	private static final String DEFAULT_AVRDUDE_BITBANGDELAY = "";
	private String fAVRDudeBitBangDelay;

	private static final String KEY_AVRDUDE_NOSIGCHECK = "avrdudeNoSigCheck";
	private static final boolean DEFAULT_AVRDUDE_NOSIGCHECK = false;
	private boolean fAVRDudeNoSigCheck;

	private static final String KEY_AVRDUDE_NOVERIFY = "avrdudeNoVerify";
	private static final boolean DEFAULT_AVRDUDE_NOVERIFY = false;
	private boolean fAVRDudeNoVerify;

	private static final String KEY_AVRDUDE_NOWRITE = "avrdudeNoWrite";
	private static final boolean DEFAULT_AVRDUDE_NOWRITE = false;
	private boolean fAVRDudeNoWrite;

	private static final String KEY_AVRDUDE_USECOUNTER = "avrdudeUseCounter";
	private static final boolean DEFAULT_AVRDUDE_USECOUNTER = false;
	private boolean fAVRDudeUseCounter;

	private static final String KEY_AVRDUDE_WRITEFLASH = "avrdudeWriteFlash";
	private static final boolean DEFAULT_AVRDUDE_WRITEFLASH = true;
	private boolean fAVRDudeWriteFlash;

	private static final String KEY_AVRDUDE_FLASHFILE = "avrdudeFlashFile";
	private static final String DEFAULT_AVRDUDE_FLASHFILE = "";
	private String fAVRDudeFlashFile;

	private static final String KEY_AVRDUDE_WRITEEEPROM = "avrdudeWriteEEPROM";
	private static final boolean DEFAULT_AVRDUDE_WRITEEEPROM = false;
	private boolean fAVRDudeWriteEEPROM;

	private static final String KEY_AVRDUDE_EEPROMFILE = "avrdudeEEPROMFile";
	private static final String DEFAULT_AVRDUDE_EEPROMFILE = "";
	private String fAVRDudeEEPROMFile;

	private static final String KEY_AVRDUDE_WRITEFUSES = "avrdudeWriteFusesBytes";
	private static final boolean DEFAULT_AVRDUDE_WRITEFUSES = false;
	private boolean fAVRDudeWriteFuses;

	// private static final String NODE_FUSES = "Fuses";
	// private FuseBytes fAVRDudeFuseBytes;

	private static final String KEY_AVRDUDE_WRITELOCKBITS = "avrdudeWriteLockbits";
	private static final boolean DEFAULT_AVRDUDE_WRITELOCKBITS = false;
	private boolean fAVRDudeWriteLockbits;

	// private static final String NODE_LOCKS = "Locks";
	// private LockBits fAVRDudeLockbits;

	private static final String KEY_AVRDUDE_WRITECALIBRATION = "avrdudeWriteCalibrartion";
	private static final boolean DEFAULT_AVRDUDE_WRITECALIBRATION = false;
	private boolean fAVRDudeWriteCalibration;

	// private static final String NODE_CALIBRATION = "CalibrartionBytes";
	// private CalibrationBytes fAVRDudeCalibration;

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
	public AVRProjectProperties(IEclipsePreferences prefs,
			AVRProjectProperties source) {
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
		fAVRDudeBitBangDelay = source.fAVRDudeBitBangDelay;
		fAVRDudeNoSigCheck = source.fAVRDudeNoSigCheck;
		fAVRDudeNoVerify = source.fAVRDudeNoVerify;
		fAVRDudeNoWrite = source.fAVRDudeNoWrite;
		fAVRDudeUseCounter = source.fAVRDudeUseCounter;

		fAVRDudeWriteFlash = source.fAVRDudeWriteFlash;
		fAVRDudeFlashFile = source.fAVRDudeFlashFile;

		fAVRDudeWriteEEPROM = source.fAVRDudeWriteEEPROM;
		fAVRDudeEEPROMFile = source.fAVRDudeEEPROMFile;

		fAVRDudeWriteFuses = source.fAVRDudeWriteFuses;
		// fAVRDudeFuseBytes = new FuseBytes(source.fAVRDudeFuseBytes);

		fAVRDudeWriteLockbits = source.fAVRDudeWriteLockbits;
		// fAVRDudeLockbits = new LockBits(source.fAVRDudeLockbits);

		fAVRDudeWriteCalibration = source.fAVRDudeWriteCalibration;
		// fAVRDudeCalibration = new
		// CalibrationBytes(source.fAVRDudeCalibration);

		fDirty = source.fDirty;
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
		}
		return fAVRDudeProgrammer;

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

	public String getAVRDudeBitBangDelay() {
		return fAVRDudeBitBangDelay;
	}

	public void setAVRDudeBitBangDelay(String bitbangdelay) {
		if (!fAVRDudeBitBangDelay.equals(bitbangdelay)) {
			fAVRDudeBitBangDelay = bitbangdelay;
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

	public boolean getAVRDudeNoVerify() {
		return fAVRDudeNoVerify;
	}

	public void setAVRDudeNoVerify(boolean noverify) {
		if (fAVRDudeNoVerify != noverify) {
			fAVRDudeNoVerify = noverify;
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

	public boolean getAVRDudeWriteFlash() {
		return fAVRDudeWriteFlash;
	}

	public void setAVRDudeWriteFlash(boolean enabled) {
		if (fAVRDudeWriteFlash != enabled) {
			fAVRDudeWriteFlash = enabled;
			fDirty = true;
		}
	}

	public String getAVRDudeFlashFile() {
		return fAVRDudeFlashFile;
	}

	public void setAVRDudeFlashFile(String filename) {
		if (!fAVRDudeFlashFile.equals(filename)) {
			fAVRDudeFlashFile = filename;
			fDirty = true;
		}
	}

	public boolean getAVRDudeWriteEEPROM() {
		return fAVRDudeWriteEEPROM;
	}

	public void setAVRDudeWriteEEPROM(boolean enabled) {
		if (fAVRDudeWriteEEPROM != enabled) {
			fAVRDudeWriteEEPROM = enabled;
			fDirty = true;
		}
	}

	public String getAVRDudeEEPROMFile() {
		return fAVRDudeEEPROMFile;
	}

	public void setAVRDudeEEPROMFile(String filename) {
		if (!fAVRDudeEEPROMFile.equals(filename)) {
			fAVRDudeEEPROMFile = filename;
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

		// add the bitclock value
		if (fAVRDudeBitclock.length() != 0) {
			arguments.add("-B" + fAVRDudeBitclock);
		}

		// add the BitBang delay value
		if (fAVRDudeBitBangDelay.length() != 0) {
			arguments.add("-i" + fAVRDudeBitBangDelay);
		}

		if (fAVRDudeNoSigCheck) {
			arguments.add("-F");
		}

		if (fAVRDudeNoVerify) {
			arguments.add("-V");
		}

		if (fAVRDudeNoWrite) {
			arguments.add("-n");
			// Add the "no Verify" flag to suppress nuisance error messages
			arguments.add("-V");
		}

		if (fAVRDudeUseCounter) {
			arguments.add("-y");
		}

		return arguments;
	}

	public List<String> getAVRDudeActionArguments(IConfiguration buildcfg) {
		List<String> arguments = new ArrayList<String>();

		AVRDudeAction action = null;

		if (fAVRDudeWriteFlash) {
			if (fAVRDudeFlashFile.length() == 0) {
				action = AVRDudeActionFactory.writeFlashAction(buildcfg);
			} else {
				action = AVRDudeActionFactory
						.writeFlashAction(fAVRDudeFlashFile);
			}
			if (action != null) {
				arguments.add(action.getArgument());
			}
		}

		if (fAVRDudeWriteEEPROM) {
			if (fAVRDudeEEPROMFile.length() == 0) {
				action = AVRDudeActionFactory.writeEEPROMAction(buildcfg);
			} else {
				action = AVRDudeActionFactory
						.writeEEPROMAction(fAVRDudeEEPROMFile);
			}
			if (action != null) {
				arguments.add(action.getArgument());
			}
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
		fAVRDudeBitclock = fPrefs.get(KEY_AVRDUDE_BITCLOCK,
				DEFAULT_AVRDUDE_BITCLOCK);
		fAVRDudeBitBangDelay = fPrefs.get(KEY_AVRDUDE_BITBANGDELAY,
				DEFAULT_AVRDUDE_BITBANGDELAY);
		fAVRDudeNoSigCheck = fPrefs.getBoolean(KEY_AVRDUDE_NOSIGCHECK,
				DEFAULT_AVRDUDE_NOSIGCHECK);
		fAVRDudeNoVerify = fPrefs.getBoolean(KEY_AVRDUDE_NOVERIFY,
				DEFAULT_AVRDUDE_NOVERIFY);
		fAVRDudeNoWrite = fPrefs.getBoolean(KEY_AVRDUDE_NOWRITE,
				DEFAULT_AVRDUDE_NOWRITE);
		fAVRDudeUseCounter = fPrefs.getBoolean(KEY_AVRDUDE_USECOUNTER,
				DEFAULT_AVRDUDE_USECOUNTER);

		fAVRDudeWriteFlash = fPrefs.getBoolean(KEY_AVRDUDE_WRITEFLASH,
				DEFAULT_AVRDUDE_WRITEFLASH);
		fAVRDudeFlashFile = fPrefs.get(KEY_AVRDUDE_FLASHFILE,
				DEFAULT_AVRDUDE_FLASHFILE);

		fAVRDudeWriteEEPROM = fPrefs.getBoolean(KEY_AVRDUDE_WRITEEEPROM,
				DEFAULT_AVRDUDE_WRITEEEPROM);
		fAVRDudeEEPROMFile = fPrefs.get(KEY_AVRDUDE_EEPROMFILE,
				DEFAULT_AVRDUDE_EEPROMFILE);

		fAVRDudeWriteFuses = fPrefs.getBoolean(KEY_AVRDUDE_WRITEFUSES,
				DEFAULT_AVRDUDE_WRITEFUSES);
		// fAVRDudeFuseBytes = new FuseBytes(fPrefs.node(NODE_FUSES));

		fAVRDudeWriteLockbits = fPrefs.getBoolean(KEY_AVRDUDE_WRITELOCKBITS,
				DEFAULT_AVRDUDE_WRITELOCKBITS);
		// fAVRDudeLockbits = new LockBits(fPrefs.node(NODE_LOCKS));

		fAVRDudeWriteCalibration = fPrefs.getBoolean(
				KEY_AVRDUDE_WRITECALIBRATION, DEFAULT_AVRDUDE_WRITECALIBRATION);
		// fAVRDudeCalibration = new
		// CalibrationBytes(fPrefs.node(NODE_CALIBRATION));
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
				fPrefs.put(KEY_AVRDUDE_BITBANGDELAY, fAVRDudeBitBangDelay);
				fPrefs.putBoolean(KEY_AVRDUDE_NOSIGCHECK, fAVRDudeNoSigCheck);
				fPrefs.putBoolean(KEY_AVRDUDE_NOVERIFY, fAVRDudeNoVerify);
				fPrefs.putBoolean(KEY_AVRDUDE_NOWRITE, fAVRDudeNoWrite);
				fPrefs.putBoolean(KEY_AVRDUDE_USECOUNTER, fAVRDudeUseCounter);

				fPrefs.putBoolean(KEY_AVRDUDE_WRITEFLASH, fAVRDudeWriteFlash);
				fPrefs.put(KEY_AVRDUDE_FLASHFILE, fAVRDudeFlashFile);

				fPrefs.putBoolean(KEY_AVRDUDE_WRITEEEPROM, fAVRDudeWriteEEPROM);
				fPrefs.put(KEY_AVRDUDE_EEPROMFILE, fAVRDudeEEPROMFile);

				fPrefs.putBoolean(KEY_AVRDUDE_WRITEFUSES, fAVRDudeWriteFuses);
				fPrefs.putBoolean(KEY_AVRDUDE_WRITELOCKBITS,
						fAVRDudeWriteLockbits);
				fPrefs.putBoolean(KEY_AVRDUDE_WRITECALIBRATION,
						fAVRDudeWriteCalibration);

				fPrefs.flush();

				if (fAVRDudeProgrammer != null) {
					ProgrammerConfigManager.getDefault().saveConfig(
							fAVRDudeProgrammer);
				}
			}
			// fAVRDudeFuseBytes.save();
			// fAVRDudeLockbits.save();
			// fAVRDudeCalibration.save();

		} catch (IllegalStateException ise) {
			// This should not happen, but just in case we ignore this unchecked
			// exception
			ise.printStackTrace();
		}
	}

	@Override
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
