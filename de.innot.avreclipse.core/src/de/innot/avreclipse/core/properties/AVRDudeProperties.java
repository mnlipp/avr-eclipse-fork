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
package de.innot.avreclipse.core.properties;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.core.avrdude.AVRDudeAction;
import de.innot.avreclipse.core.avrdude.AVRDudeActionFactory;
import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.avrdude.ProgrammerConfigManager;
import de.innot.avreclipse.core.toolinfo.AVRDude;

/**
 * Container for all AVRDude specific properties of a project.
 * <p>
 * Upon instantiation, the properties are loaded from the given preference
 * store. All changes are local to the object until the {@link #save()} method
 * is called.
 * </p>
 * <p>
 * <code>AVRDudeProperties</code> objects do not reflect changes made to other
 * <code>AVRDudeProperties</code> for the same Project/Configuration, so they
 * should not be held on to and be reloaded every time the current values are
 * required.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class AVRDudeProperties {

	private AVRProjectProperties fParent;

	private static final String KEY_PROGRAMMER = "ProgrammerID";
	private ProgrammerConfig fProgrammer;
	private String fProgrammerId;

	private static final String KEY_BITCLOCK = "Bitclock";
	private static final String DEFAULT_BITCLOCK = "";
	private String fBitclock;

	private static final String KEY_BITBANGDELAY = "BitBangDelay";
	private static final String DEFAULT_BITBANGDELAY = "";
	private String fBitBangDelay;

	private static final String KEY_NOSIGCHECK = "NoSigCheck";
	private static final boolean DEFAULT_NOSIGCHECK = false;
	private boolean fNoSigCheck;

	private static final String KEY_NOVERIFY = "NoVerify";
	private static final boolean DEFAULT_NOVERIFY = false;
	private boolean fNoVerify;

	private static final String KEY_NOWRITE = "NoWrite";
	private static final boolean DEFAULT_NOWRITE = false;
	private boolean fNoWrite;

	private static final String KEY_USECOUNTER = "UseCounter";
	private static final boolean DEFAULT_USECOUNTER = false;
	private boolean fUseCounter;

	private static final String KEY_WRITEFLASH = "WriteFlash";
	private static final boolean DEFAULT_WRITEFLASH = true;
	private boolean fWriteFlash;

	private static final String KEY_FLASHFILE = "FlashFile";
	private static final String DEFAULT_FLASHFILE = "";
	private String fFlashFile;

	private static final String KEY_WRITEEEPROM = "WriteEEPROM";
	private static final boolean DEFAULT_WRITEEEPROM = false;
	private boolean fWriteEEPROM;

	private static final String KEY_EEPROMFILE = "EEPROMFile";
	private static final String DEFAULT_EEPROMFILE = "";
	private String fEEPROMFile;

	private static final String KEY_WRITEFUSES = "WriteFusesBytes";
	private static final boolean DEFAULT_WRITEFUSES = false;
	private boolean fWriteFuses;

//	private static final String NODE_FUSES = "Fuses";
//	private FuseBytes fFuseBytes;

	private static final String KEY_WRITELOCKBITS = "WriteLockbits";
	private static final boolean DEFAULT_WRITELOCKBITS = false;
	private boolean fWriteLockbits;

	// private static final String NODE_LOCKS = "Locks";
	// private LockBits fLockbits;

	private static final String KEY_WRITECALIBRATION = "WriteCalibrartion";
	private static final boolean DEFAULT_WRITECALIBRATION = false;
	private boolean fWriteCalibration;

	// private static final String NODE_CALIBRATION = "CalibrationBytes";
	// private CalibrationBytes fAVRDudeCalibration;

	/**
	 * The source/target Preferences for the properties or <code>null</code>
	 * if default properties are represented.
	 */
	private Preferences fPrefs;

	/** Flag if any properties have been changed */
	private boolean fDirty;

	/**
	 * Load the AVR project properties from the given Preferences.
	 * 
	 * @param prefs
	 *            <code>IEclipsePreferences</code>
	 */
	public AVRDudeProperties(Preferences prefs, AVRProjectProperties parent) {
		fPrefs = prefs;
		fParent = parent;
		loadData();
	}

	/**
	 * Load the AVR Project properties from the given
	 * <code>AVRConfigurationProperties</code> object.
	 * 
	 * @param source
	 */
	public AVRDudeProperties(Preferences prefs, AVRProjectProperties parent, AVRDudeProperties source) {
		fParent = source.fParent;
		fPrefs = prefs;

		fProgrammer = source.fProgrammer;
		fProgrammerId = source.fProgrammerId;
		fBitclock = source.fBitclock;
		fBitBangDelay = source.fBitBangDelay;
		fNoSigCheck = source.fNoSigCheck;
		fNoVerify = source.fNoVerify;
		fNoWrite = source.fNoWrite;
		fUseCounter = source.fUseCounter;

		fWriteFlash = source.fWriteFlash;
		fFlashFile = source.fFlashFile;

		fWriteEEPROM = source.fWriteEEPROM;
		fEEPROMFile = source.fEEPROMFile;

		fWriteFuses = source.fWriteFuses;
		// fFuseBytes = new FuseBytes(source.fAVRDudeFuseBytes);

		fWriteLockbits = source.fWriteLockbits;
		// fAVRDudeLockbits = new LockBits(source.fAVRDudeLockbits);

		fWriteCalibration = source.fWriteCalibration;
		// fAVRDudeCalibration = new
		// CalibrationBytes(source.fAVRDudeCalibration);

		fDirty = source.fDirty;
	}

	public AVRProjectProperties getParent() {
		return fParent;
	}

	public ProgrammerConfig getProgrammer() {
		if (fProgrammer == null) {
			return ProgrammerConfigManager.getDefault().getConfig(fProgrammerId);
		}
		return fProgrammer;

	}

	public void setProgrammer(ProgrammerConfig progcfg) {
		if (!progcfg.equals(fProgrammer)) {
			fProgrammer = progcfg;
			fProgrammerId = progcfg.getId();
			fDirty = false;
		}
	}

	public String getProgrammerId() {
		return fProgrammerId;
	}

	public void setProgrammerId(String programmerid) {
		if (!fProgrammerId.equals(programmerid)) {
			fProgrammerId = programmerid;
			fProgrammer = null;
			fDirty = true;
		}
	}

	public String getBitclock() {
		return fBitclock;
	}

	public void setBitclock(String bitclock) {
		if (!fBitclock.equals(bitclock)) {
			fBitclock = bitclock;
			fDirty = true;
		}
	}

	public String getBitBangDelay() {
		return fBitBangDelay;
	}

	public void setBitBangDelay(String bitbangdelay) {
		if (!fBitBangDelay.equals(bitbangdelay)) {
			fBitBangDelay = bitbangdelay;
			fDirty = true;
		}
	}

	public boolean getNoSigCheck() {
		return fNoSigCheck;
	}

	public void setNoSigCheck(boolean nosigcheck) {
		if (fNoSigCheck != nosigcheck) {
			fNoSigCheck = nosigcheck;
			fDirty = true;
		}
	}

	public boolean getNoVerify() {
		return fNoVerify;
	}

	public void setNoVerify(boolean noverify) {
		if (fNoVerify != noverify) {
			fNoVerify = noverify;
			fDirty = true;
		}
	}

	public boolean getNoWrite() {
		return fNoWrite;
	}

	public void setNoWrite(boolean nowrite) {
		if (fNoWrite != nowrite) {
			fNoWrite = nowrite;
			fDirty = true;
		}
	}

	public boolean getUseCounter() {
		return fUseCounter;
	}

	public void setUseCounter(boolean usecounter) {
		if (fUseCounter != usecounter) {
			fUseCounter = usecounter;
			fDirty = true;
		}
	}

	public boolean getWriteFlash() {
		return fWriteFlash;
	}

	public void setWriteFlash(boolean enabled) {
		if (fWriteFlash != enabled) {
			fWriteFlash = enabled;
			fDirty = true;
		}
	}

	public String getFlashFile() {
		return fFlashFile;
	}

	public void setFlashFile(String filename) {
		if (!fFlashFile.equals(filename)) {
			fFlashFile = filename;
			fDirty = true;
		}
	}

	public boolean getWriteEEPROM() {
		return fWriteEEPROM;
	}

	public void setWriteEEPROM(boolean enabled) {
		if (fWriteEEPROM != enabled) {
			fWriteEEPROM = enabled;
			fDirty = true;
		}
	}

	public String getEEPROMFile() {
		return fEEPROMFile;
	}

	public void setEEPROMFile(String filename) {
		if (!fEEPROMFile.equals(filename)) {
			fEEPROMFile = filename;
			fDirty = true;
		}
	}

	public boolean getWriteFuses() {
		return fWriteFuses;
	}

	public void setWriteFuses(boolean enabled) {
		if (!fWriteFuses != enabled) {
			fWriteFuses = enabled;
			fDirty = true;
		}
	}

	/**
	 * Gets the avrdude command arguments as defined by the properties.
	 * 
	 * @return <code>List&lt;String&gt;</code> with the avrdude options, one
	 *         per list entry.
	 */
	public List<String> getArguments() {
		List<String> arguments = new ArrayList<String>();

		// Convert the mcu id to the avrdude format and add it
		String mcuid = fParent.getMCUId();
		String avrdudemcuid = AVRDude.getDefault().getMCUInfo(mcuid);
		arguments.add("-p" + avrdudemcuid);

		// Add the options from the programmer configuration
		ProgrammerConfig progcfg = getProgrammer();

		if (progcfg != null) {
			arguments.addAll(progcfg.getArguments());
		}

		// add the bitclock value
		if (fBitclock.length() != 0) {
			arguments.add("-B" + fBitclock);
		}

		// add the BitBang delay value
		if (fBitBangDelay.length() != 0) {
			arguments.add("-i" + fBitBangDelay);
		}

		if (fNoSigCheck) {
			arguments.add("-F");
		}

		if (fNoVerify) {
			arguments.add("-V");
		}

		if (fNoWrite) {
			arguments.add("-n");
			// Add the "no Verify" flag to suppress nuisance error messages
			arguments.add("-V");
		}

		if (fUseCounter) {
			arguments.add("-y");
		}

		return arguments;
	}

	public List<String> getActionArguments(IConfiguration buildcfg) {
		List<String> arguments = new ArrayList<String>();

		AVRDudeAction action = null;

		if (fWriteFlash) {
			if (fFlashFile.length() == 0) {
				action = AVRDudeActionFactory.writeFlashAction(buildcfg);
			} else {
				action = AVRDudeActionFactory.writeFlashAction(fFlashFile);
			}
			if (action != null) {
				arguments.add(action.getArgument());
			}
		}

		if (fWriteEEPROM) {
			if (fEEPROMFile.length() == 0) {
				action = AVRDudeActionFactory.writeEEPROMAction(buildcfg);
			} else {
				action = AVRDudeActionFactory.writeEEPROMAction(fEEPROMFile);
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
		fProgrammerId = fPrefs.get(KEY_PROGRAMMER, "");
		fBitclock = fPrefs.get(KEY_BITCLOCK, DEFAULT_BITCLOCK);
		fBitBangDelay = fPrefs.get(KEY_BITBANGDELAY, DEFAULT_BITBANGDELAY);
		fNoSigCheck = fPrefs.getBoolean(KEY_NOSIGCHECK, DEFAULT_NOSIGCHECK);
		fNoVerify = fPrefs.getBoolean(KEY_NOVERIFY, DEFAULT_NOVERIFY);
		fNoWrite = fPrefs.getBoolean(KEY_NOWRITE, DEFAULT_NOWRITE);
		fUseCounter = fPrefs.getBoolean(KEY_USECOUNTER, DEFAULT_USECOUNTER);

		fWriteFlash = fPrefs.getBoolean(KEY_WRITEFLASH, DEFAULT_WRITEFLASH);
		fFlashFile = fPrefs.get(KEY_FLASHFILE, DEFAULT_FLASHFILE);

		fWriteEEPROM = fPrefs.getBoolean(KEY_WRITEEEPROM,
		        DEFAULT_WRITEEEPROM);
		fEEPROMFile = fPrefs.get(KEY_EEPROMFILE, DEFAULT_EEPROMFILE);

		fWriteFuses = fPrefs.getBoolean(KEY_WRITEFUSES, DEFAULT_WRITEFUSES);
		// fFuseBytes = new FuseBytes(fPrefs.node(NODE_FUSES));

		fWriteLockbits = fPrefs.getBoolean(KEY_WRITELOCKBITS,
		        DEFAULT_WRITELOCKBITS);
		// fAVRDudeLockbits = new LockBits(fPrefs.node(NODE_LOCKS));

		fWriteCalibration = fPrefs.getBoolean(KEY_WRITECALIBRATION,
		        DEFAULT_WRITECALIBRATION);
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

				fPrefs.put(KEY_PROGRAMMER, fProgrammerId);
				fPrefs.put(KEY_BITCLOCK, fBitclock);
				fPrefs.put(KEY_BITBANGDELAY, fBitBangDelay);
				fPrefs.putBoolean(KEY_NOSIGCHECK, fNoSigCheck);
				fPrefs.putBoolean(KEY_NOVERIFY, fNoVerify);
				fPrefs.putBoolean(KEY_NOWRITE, fNoWrite);
				fPrefs.putBoolean(KEY_USECOUNTER, fUseCounter);

				fPrefs.putBoolean(KEY_WRITEFLASH, fWriteFlash);
				fPrefs.put(KEY_FLASHFILE, fFlashFile);

				fPrefs.putBoolean(KEY_WRITEEEPROM, fWriteEEPROM);
				fPrefs.put(KEY_EEPROMFILE, fEEPROMFile);

				fPrefs.putBoolean(KEY_WRITEFUSES, fWriteFuses);
				fPrefs.putBoolean(KEY_WRITELOCKBITS, fWriteLockbits);
				fPrefs.putBoolean(KEY_WRITECALIBRATION, fWriteCalibration);

				fPrefs.flush();

				if (fProgrammer != null) {
					ProgrammerConfigManager.getDefault().saveConfig(fProgrammer);
				}
			}
			// fFuseBytes.save();
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
		sb.append("ProgrammerID=" + fProgrammerId);
		sb.append("]");
		return sb.toString();
	}

}
