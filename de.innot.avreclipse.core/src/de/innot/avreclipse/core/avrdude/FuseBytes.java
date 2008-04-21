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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.fuses.FuseByteValues;

/**
 * Container for the Fuse Byte values.
 * <p>
 * This class has two modes. Depending on the {@link #fUseFile} flag, it will either read the fuse
 * values from a supplied file or stores the actual fusebytes. The mode is selected by the user in
 * the Properties user interface.
 * </p>
 * <p>
 * This class can be used either standalone or as part of the AVRProjectProperties structure.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class FuseBytes {

	/** Maximum number of Fuse Bytes supported. */
	public final static int			MAX_FUSEBYTES			= 3;

	/** Empty fusebytes */
	public final static int[]		EMPTY_FUSES				= new int[] { -1, -1, -1 };

	/** The MCU id for which the current fuse byte values are valid */
	private String					fMCUid;
	private static final String		KEY_FUSEMCUID			= "FuseMCUid";

	/**
	 * Use fuses file flag.
	 * <p>
	 * If <code>true</code>, the fuse values are read from a file.
	 * </p>
	 * <p>
	 * If <code>false</code> the values from {@link #fFuseValues} are used.
	 * </p>
	 */
	private boolean					fUseFile;
	private final static String		KEY_USEFUSEFILE			= "UseFuseFile";
	private final static boolean	DEFAULT_USEFUSESFILE	= false;

	/**
	 * The name of the fuses file.
	 * <p>
	 * This is used when the {@link #fUseFile} flag is <code>true</code>.
	 * </p>
	 * <p>
	 * The name can contain macros. They can be resolved by the caller or with the
	 * {@link #getFusesFileResolved(IConfiguration)} method.
	 * </p>
	 */
	private String					fFusesFile;
	private final static String		KEY_FUSESFILE			= "FusesFile";
	private final static String		DEFAULT_FUSESFILE		= "";

	/**
	 * The current fuse byte values.
	 * <p>
	 * This is used when the {@link #fUseFile} flag is <code>false</code>.
	 * </p>
	 */
	private final FuseByteValues	fFuseValues;
	private final static String		KEY_FUSEVALUES			= "FuseValues";
	private final static String		SEPARATOR				= ":";

	/**
	 * The <code>Preferences</code> used to read / save the current properties.
	 * 
	 */
	private final Preferences		fPrefs;

	/**
	 * The Parent <code>AVRDudeProperties</code>. Can be <code>null</code> if this class is
	 * used in stand alone mode.
	 * 
	 */
	private final AVRDudeProperties	fParent;

	/** <code>true</code> if the properties have been modified and need saving. */
	private boolean					fDirty					= false;

	/**
	 * Create a new FuseBytes object and load the properties from the Preferences.
	 * <p>
	 * If the given Preferences has no saved properties yet, the default values are used.
	 * </p>
	 * 
	 * @param prefs
	 *            <code>Preferences</code> to read the properties from.
	 * @param parent
	 *            Reference to the <code>AVRDudeProperties</code> parent object.
	 */
	public FuseBytes(Preferences prefs, AVRDudeProperties parent) {
		fPrefs = prefs;
		fParent = parent;
		fFuseValues = new FuseByteValues(parent.getParent().getMCUId());

		loadFuseBytes();
	}

	/**
	 * Create a new FuseBytes object and copy from the given FuseByte object.
	 * <p>
	 * All values from the source are copied, except for the source Preferences and the Parent.
	 * </p>
	 * 
	 * @param prefs
	 *            <code>Preferences</code> to read the properties from.
	 * @param parent
	 *            Reference to the <code>AVRDudeProperties</code> parent object.
	 * @param source
	 *            <code>FuseBytes</code> object to copy.
	 */
	public FuseBytes(Preferences prefs, AVRDudeProperties parent, FuseBytes source) {
		fPrefs = prefs;
		fParent = parent;

		fMCUid = source.fMCUid;

		fUseFile = source.fUseFile;
		fFuseValues = new FuseByteValues(source.fFuseValues);
	}

	/**
	 * Get the MCU id value for which the the fuse bytes are valid.
	 * 
	 * @return <code>String</code> with an mcu id.
	 */
	public String getMCUId() {

		// TODO: if a fuses file is used, return the mcuid from the file.

		return fMCUid;
	}

	/**
	 * Tells this class that the current fuse byte values are valid for the given MCU.
	 * <p>
	 * Use this method with care, as there will be no checks if the current values actually make
	 * sense for the new MCU type.
	 * </p>
	 * <p>
	 * The new setting is only valid for the internally stored values. If a fuses file is used it is
	 * not affected and a call to {@link #getMCUId()} will return the mcu from the fuses file, not
	 * this one.
	 * </p>
	 * 
	 * @param mcuid
	 */
	public void setMCUId(String mcuid) {
		fMCUid = mcuid;
	}

	/**
	 * Get the current value of the "Use Fuses File" flag.
	 * 
	 * @see #setFusesFile(String)
	 * @see #getFuseValue(int)
	 * @see #getFuseValues()
	 * 
	 * @return <code>true</code> if the fuse values are taken from a file, <code>false</code> if
	 *         the values stored in this object are used.
	 */
	public boolean getUseFile() {
		return fUseFile;
	}

	/**
	 * Set the value of the "Use Fuses File" flag.
	 * 
	 * @see #setFusesFile(String)
	 * @see #getFuseValue(int)
	 * @see #getFuseValues()
	 * 
	 * 
	 * @param usefile
	 *            <code>true</code> if the fuse values should be read from the file,
	 *            <code>false</code> if the values stored in this object are used.
	 */
	public void setUseFile(boolean usefile) {
		if (fUseFile != usefile) {
			fUseFile = usefile;
			fDirty = true;
		}
	}

	/**
	 * Get the current name of the fuses file with all macros resolved.
	 * <p>
	 * Note: The returned path may still be OS independent and needs to be converted to an OS
	 * specific path (e.g. with <code>new Path(resolvedname).toOSString()</code>
	 * </p>
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> with the macro context.
	 * @return <code>String</code> with the resolved fuses filename. May be empty and may not
	 *         point to an actual or valid fuses file.
	 */
	public String getFusesFileResolved(IConfiguration buildcfg) {
		return resolveMacros(buildcfg, fFusesFile);
	}

	/**
	 * Get the current name of the fuses file.
	 * <p>
	 * The returned string may still contain macros.
	 * </p>
	 * 
	 * @return <code>String</code> with the name of the fuses file. May be empty and may not point
	 *         to an actual or valid fuses file.
	 */
	public String getFusesFile() {
		return fFusesFile;
	}

	/**
	 * Set the name of the fuses file.
	 * <p>
	 * The given filename is stored as-is. There are no checks if the file is valid or even exists.
	 * </p>
	 * 
	 * @param fusesfile
	 *            <code>String</code> with the name of a fuses file.
	 */
	public void setFusesFile(String fusesfile) {
		if (!fFusesFile.equals(fusesfile)) {
			fFusesFile = fusesfile;
			fDirty = true;
		}
	}

	/**
	 * Get all current fuse byte values.
	 * <p>
	 * Get all fuse bytes according to the current setting either from a file or from the object
	 * storage.
	 * </p>
	 * <p>
	 * All values are either a valid bytes (0 - 255) or <code>-1</code> if no value was set.
	 * </p>
	 * <p>
	 * This always returns the maximum number of values, regardless of the number of fuse bytes of
	 * the current MCU. It is up to the caller to use only those values actually supported.
	 * </p>
	 * 
	 * 
	 * @return Array of <code>int</code> with all fuse byte values.
	 */
	public int[] getFuseValues() {
		if (fUseFile) { return getFuseValuesFromFile(); }
		return getFuseValuesFromObject();
	}

	/**
	 * Get all current fuse byte values from the fuses file.
	 * <p>
	 * All values are either a valid bytes (0 - 255) or <code>-1</code> if no value was set.
	 * </p>
	 * <p>
	 * This always returns the maximum number of values, regardless of the number of fuse bytes of
	 * the current MCU. It is up to the caller to use only those values actually supported.
	 * </p>
	 * 
	 * 
	 * @return Array of <code>int</code> with all fuse byte values.
	 */
	public int[] getFuseValuesFromFile() {
		// TODO: read values from file
		return getFuseValuesFromObject();
	}

	/**
	 * Get all current fuse byte values stored in the object.
	 * <p>
	 * All values are either a valid bytes (0 - 255) or <code>-1</code> if no value was set.
	 * </p>
	 * <p>
	 * This always returns the maximum number of values, regardless of the number of fuse bytes of
	 * the current MCU. It is up to the caller to use only those values actually supported.
	 * </p>
	 * 
	 * @return Array of <code>int</code> with all fuse byte values.
	 */
	public int[] getFuseValuesFromObject() {
		return fFuseValues.getValues();
	}

	/**
	 * Sets the values of all fuse bytes in the object.
	 * <p>
	 * Even with the "Use Fuses File" flag set, this method will set the values stored in the
	 * object.
	 * </p>
	 * 
	 * @see #setFuseValue(int, int)
	 * 
	 * @param values
	 *            Array of <code>int</code> with the new values.
	 * @throws IllegalArgumentException
	 *             if any value in the array is not a byte value or not <code>-1</code>
	 */
	public void setFuseValues(int[] values) {
		// While values[].length should be equal to the length of the internal
		// field (and equal to MAX_FUSEBYTES), we use this to avoid any
		// OutOfBoundExceptions
		int min = Math.min(values.length, MAX_FUSEBYTES);

		// Set all individual values. setFuseValue() will take care of setting
		// the dirty flag as needed.
		for (int i = 0; i < min; i++) {
			setFuseValue(i, values[i]);
		}
	}

	/**
	 * Get a single Fuse byte value.
	 * <p>
	 * Get the fuse byte according to the current setting either from a file or from the object
	 * storage.
	 * </p>
	 * 
	 * @param index
	 *            The fuse byte to read. Must be between 0 and 2
	 * @return <code>int</code> with the byte value or <code>-1</code> if the value was not set
	 *         or the index is out of bounds.
	 */
	public int getFuseValue(int index) {
		if (!(0 <= index && index < MAX_FUSEBYTES)) { return -1; }

		// getFuseValues() will take care of the "use fuses file" flag and
		// return the relevant fusebyte values.
		int[] values = getFuseValues();
		return values[index];
	}

	/**
	 * Set a single Fuse byte value.
	 * <p>
	 * The value is always written to the object storage, regardless of the "Use Fuses File" flag.
	 * </p>
	 * 
	 * @param index
	 *            The fuse byte to set. Must be between 0 and 2
	 * @param value
	 *            <code>int</code> with the byte value (0-255) or <code>-1</code> to unset the
	 *            value.
	 * @throws IllegalArgumentException
	 *             if the index is out of bounds (0-2) or the value is out of range (-1 to 255)
	 */
	public void setFuseValue(int index, int value) {
		if (!(0 <= index && index < MAX_FUSEBYTES)) { throw new IllegalArgumentException(
				"invalid fusebyte index:" + index + " (must be between 0 and " + MAX_FUSEBYTES
						+ ")"); }
		if (!(-1 <= value && value <= 255)) { throw new IllegalArgumentException("invalid value:"
				+ index + " (must be between 0 and 255)"); }

		if (fFuseValues.getValue(index) != value) {
			fFuseValues.setValue(index, value);
			fDirty = true;
		}
	}

	/**
	 * Copies the fuse byte values from the fuses file to the object storage.
	 */
	public void syncFromFile() {
		// TODO Not implemented yet
	}

	/**
	 * Get the list of avrdude arguments required to write all fuse bytes.
	 * <p>
	 * Note: This method does <strong>not</strong> set the "-u" flag to disable the safemode. It is
	 * up to the caller to add this flag. If the "disable safemode" flag is not set, avrdude will
	 * restore the previous fusebyte values after the new values have been written.
	 * </p>
	 * 
	 * @return <code>List&lt;String&gt;</code> with avrdude action options.
	 */
	public List<String> getArguments(String mcuid) {
		List<String> args = new ArrayList<String>();

		if (!isCompatibleWith(mcuid)) {
			// If the fuse bytes are not valid (mismatch between read and
			// assigned mcu id) return an empty list,
			return args;
		}

		int[] fusevalues = getFuseValues();

		if (fUseFile) {
			// Use a fuses file
			// Read the fusevalues from the file.

			// TODO Not implemented yet
		}

		// The action factory will take of generating just the right number of
		// actions for the MCU.
		List<AVRDudeAction> allactions = AVRDudeActionFactory.writeFuseBytes(fMCUid, fusevalues);

		for (AVRDudeAction action : allactions) {
			args.add(action.getArgument());
		}

		return args;

	}

	/**
	 * Load the properties from the Preferences.
	 * <p>
	 * The <code>Preferences</code> object used is set in the constructor of this class.
	 * </p>
	 * 
	 */
	private void loadFuseBytes() {

		// Get the MCU id of the parent TargetProperties
		// This is used as the default mcuid for this fusebytes object.
		String parentmcuid = fParent.getParent().getMCUId();

		fMCUid = fPrefs.get(KEY_FUSEMCUID, parentmcuid);
		fUseFile = fPrefs.getBoolean(KEY_USEFUSEFILE, DEFAULT_USEFUSESFILE);
		fFusesFile = fPrefs.get(KEY_FUSESFILE, DEFAULT_FUSESFILE);

		String fusevaluestring = fPrefs.get(KEY_FUSEVALUES, "");

		// Clear the old values
		fFuseValues.setValues(EMPTY_FUSES);

		// split the values
		String[] values = fusevaluestring.split(SEPARATOR);
		int count = Math.min(values.length, MAX_FUSEBYTES);
		for (int i = 0; i < count; i++) {
			String value = values[i];
			if (value.length() != 0) {
				fFuseValues.setValue(i, Integer.parseInt(values[i]));
			}
		}

	}

	/**
	 * Save the current property values to the Preferences.
	 * <p>
	 * The <code>Preferences</code> object used is set in the constructor of this class.
	 * </p>
	 * 
	 * @throws BackingStoreException
	 */
	public void save() throws BackingStoreException {
		if (fDirty) {
			fPrefs.put(KEY_FUSEMCUID, fMCUid);
			fPrefs.putBoolean(KEY_USEFUSEFILE, fUseFile);
			fPrefs.put(KEY_FUSESFILE, fFusesFile);

			// convert the values to a single String
			StringBuilder sb = new StringBuilder(20);
			for (int i = 0; i < MAX_FUSEBYTES; i++) {
				if (i > 0) sb.append(SEPARATOR);
				sb.append(fFuseValues.getValue(i));
			}
			fPrefs.put(KEY_FUSEVALUES, sb.toString());

			fPrefs.flush();
		}
	}

	/**
	 * Resolve all CDT macros in the given string.
	 * <p>
	 * If the string did not contain macros or the macros could not be resolved, the original string
	 * is returned.
	 * </p>
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> for the macro context.
	 * @param value
	 *            The source <code>String</code> with macros
	 * @return The new <code>String</code> with all macros resolved.
	 */
	private String resolveMacros(IConfiguration buildcfg, String string) {

		String resolvedstring = string;

		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();

		try {
			resolvedstring = provider.resolveValue(string,
					"", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, buildcfg); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (BuildMacroException e) {
			// Do nothing = return the original string
		}

		return resolvedstring;
	}

	/**
	 * Test if this Object is valid for the given MCU.
	 * 
	 * @return <code>true</code> if the current fuse byte values (either immediate or from a file)
	 *         are valid for the given MCU id.
	 * 
	 */
	public boolean isCompatibleWith(String mcuid) {
		if (fUseFile) {
			// TODO: Check against the file
			return false;
		}
		return fMCUid.equals(mcuid);

		// TODO: check the FuseDescriptions if the fuse settings are compatible.
	}

}
