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

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.mbs.BuildMacro;

/**
 * Storage independent container for the Fuse and Locks Byte values.
 * <p>
 * This class has two modes. Depending on the {@link #fUseFile} flag, it will either read the fuse
 * values from a supplied file or immediate values stored in a byte values object. The mode is
 * selected by the user in the Properties user interface.
 * </p>
 * <p>
 * This class can be used either standalone or as part of the AVRProjectProperties structure.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public abstract class AbstractBytes {

	/** The MCU id for which the current fuse byte values are valid */
	private String					fMCUid;
	private static final String		KEY_MCUID			= "MCUid";

	/**
	 * Write flag
	 * <p>
	 * If <code>true</code>, the byte values are written to the target device when avrdude is
	 * executed.
	 * </p>
	 */
	private boolean					fWriteFlag;
	private final static String		KEY_WRITEFLAG		= "Write";
	private final static boolean	DEFAULT_WRITEFLAG	= false;

	/**
	 * Use file flag.
	 * <p>
	 * If <code>true</code>, the byte values are read from a file.
	 * </p>
	 * <p>
	 * If <code>false</code> the values from {@link #fByteValues} are used.
	 * </p>
	 */
	private boolean					fUseFile;
	private final static String		KEY_USEFILE			= "UseFile";
	private final static boolean	DEFAULT_USEFILE		= false;

	/**
	 * The name of the file.
	 * <p>
	 * This is used when the {@link #fUseFile} flag is <code>true</code>.
	 * </p>
	 * <p>
	 * The name can contain macros. They can be resolved by the caller or with the
	 * {@link #getFileNameResolved(IConfiguration)} method.
	 * </p>
	 */
	private String					fFileName;
	private final static String		KEY_FILENAME		= "FileName";
	private final static String		DEFAULT_FILENAME	= "";

	/**
	 * The current byte values.
	 * <p>
	 * This is used when the {@link #fUseFile} flag is <code>false</code>.
	 * </p>
	 */
	private ByteValues				fByteValues;
	private final static String		KEY_BYTEVALUES		= "ByteValues";
	private final static String		SEPARATOR			= ":";

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
	private boolean					fDirty				= false;

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
	protected AbstractBytes(Preferences prefs, AVRDudeProperties parent) {
		fPrefs = prefs;
		fParent = parent;
		fByteValues = createByteValuesObject(parent.getParent().getMCUId());

		load();
	}

	/**
	 * Cloning constructor.
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
	public AbstractBytes(Preferences prefs, AVRDudeProperties parent, AbstractBytes source) {
		fPrefs = prefs;
		fParent = parent;

		fMCUid = source.fMCUid;

		fWriteFlag = source.fWriteFlag;
		fUseFile = source.fUseFile;
		fByteValues = createByteValuesObject(source.fByteValues);
	}

	/**
	 * Hook method for subclasses to supply a new {@link ByteValues} object.
	 * 
	 * @param mcuid
	 *            <code>String</code> with a MCU id value.
	 * @return New <code>ByteValue</code> object.
	 */
	protected abstract ByteValues createByteValuesObject(String mcuid);

	/**
	 * Hook method for subclasses to supply a new {@link ByteValues} object.
	 * 
	 * @param source
	 *            <code>ByteValue</code> source object to copy.
	 * @return New <code>ByteValue</code> object.
	 */
	protected abstract ByteValues createByteValuesObject(ByteValues source);

	/**
	 * Returns the maximum number of bytes the subclass supports.
	 * <p>
	 * Subclasses must override this to tell <code>AbstractBytes</code> how many bytes they
	 * support.
	 * </p>
	 * 
	 * @return <code>1</code> up to
	 *         <code>6<code> for fuse bytes and <code>1</code> for lockbit bytes.
	 */
	protected abstract int getByteCount();

	/**
	 * Get the MCU id value for which this object is valid.
	 * 
	 * @return <code>String</code> with an mcu id.
	 */
	public String getMCUId() {

		// TODO: if a file is used, return the mcuid from the file.

		return fMCUid;
	}

	/**
	 * Tells this class that the current byte values are valid for the given MCU.
	 * <p>
	 * Use this method with care, as there will be no checks if the current values actually make
	 * sense for the new MCU type.
	 * </p>
	 * <p>
	 * The new setting is only valid for the internally stored values. If a file is used it is not
	 * affected and a call to {@link #getMCUId()} will return the mcu from the file, not this one.
	 * </p>
	 * 
	 * @param mcuid
	 */
	public void setMCUId(String mcuid) {
		fMCUid = mcuid;

		// copy the old byte values to a new ByteValues Object for the given MCU
		ByteValues newByteValues = createByteValuesObject(mcuid);
		newByteValues.setValues(fByteValues.getValues());
		fByteValues = newByteValues;

	}

	/**
	 * Get the "write to target MCU" flag.
	 * 
	 * @return <code>true</code> if the byte values should be written to the target device when
	 *         avrdude is executed.
	 */
	public boolean getWrite() {
		return fWriteFlag;
	}

	/**
	 * Set the "write to target MCU" flag.
	 * 
	 * @param enable
	 *            <code>true</code> to enable writing the bytes managed by this class when avrdude
	 *            is executed.
	 */
	public void setWrite(boolean enable) {
		if (fWriteFlag != enable) {
			fWriteFlag = enable;
			fDirty = true;
		}
	}

	/**
	 * Get the current value of the "Use File" flag.
	 * 
	 * @see #setFileName(String)
	 * @see #getValue(int)
	 * @see #getValues()
	 * 
	 * @return <code>true</code> if the byte values are taken from a file, <code>false</code> if
	 *         the values stored in this object are used.
	 */
	public boolean getUseFile() {
		return fUseFile;
	}

	/**
	 * Set the value of the "Use File" flag.
	 * 
	 * @see #setFileName(String)
	 * @see #getValue(int)
	 * @see #getValues()
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
	 * Get the current name of the file with all macros resolved.
	 * <p>
	 * Note: The returned path may still be OS independent and needs to be converted to an OS
	 * specific path (e.g. with <code>new Path(resolvedname).toOSString()</code>
	 * </p>
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> with the macro context.
	 * @return <code>String</code> with the resolved filename. May be empty and may not point to
	 *         an actual or valid file.
	 */
	public String getFileNameResolved(IConfiguration buildcfg) {
		return BuildMacro.resolveMacros(buildcfg, fFileName);
	}

	/**
	 * Get the current name of the file.
	 * <p>
	 * The returned string may still contain macros.
	 * </p>
	 * 
	 * @return <code>String</code> with the name of the file. May be empty and may not point to an
	 *         actual or valid file.
	 */
	public String getFileName() {
		return fFileName;
	}

	/**
	 * Set the name of the file.
	 * <p>
	 * The given filename is stored as-is. There are no checks if the file is valid or even exists.
	 * </p>
	 * 
	 * @param fusesfile
	 *            <code>String</code> with the name of a file.
	 */
	public void setFileName(String filename) {
		if (!fFileName.equals(filename)) {
			fFileName = filename;
			fDirty = true;
		}
	}

	/**
	 * Get all current byte values.
	 * <p>
	 * Get all bytes according to the current setting either from a file or from the object storage.
	 * </p>
	 * <p>
	 * All values are either a valid bytes (0 - 255) or <code>-1</code> if no value was set.
	 * </p>
	 * 
	 * @return Array of <code>int</code> with all byte values.
	 */
	public int[] getValues() {
		if (fUseFile) {
			return getValuesFromFile();
		}
		return getValuesFromImmediate();
	}

	/**
	 * Get all current byte values from the file.
	 * <p>
	 * All values are either a valid bytes (0 - 255) or <code>-1</code> if no value was set.
	 * </p>
	 * 
	 * @return Array of <code>int</code> with all byte values.
	 */
	public int[] getValuesFromFile() {
		// TODO: read values from file
		return getValuesFromImmediate();
	}

	/**
	 * Get all current byte values stored in the object.
	 * <p>
	 * All values are either a valid bytes (0 - 255) or <code>-1</code> if no value was set.
	 * </p>
	 * 
	 * @return Array of <code>int</code> with all byte values.
	 */
	public int[] getValuesFromImmediate() {
		return fByteValues.getValues();
	}

	/**
	 * Sets the values of all bytes in the object.
	 * <p>
	 * Even with the "Use File" flag set, this method will set the values stored in the object.
	 * </p>
	 * 
	 * @see #setByteValue(int, int)
	 * 
	 * @param values
	 *            Array of <code>int</code> with the new values.
	 * @throws IllegalArgumentException
	 *             if any value in the array is not a byte value or not <code>-1</code>
	 */
	public void setValues(int[] values) {
		// While values[].length should be equal to the length of the internal
		// field (and equal to MAX_FUSEBYTES), we use this to avoid any
		// OutOfBoundExceptions
		int min = Math.min(values.length, getByteCount());

		// Set all individual values. setFuseValue() will take care of setting
		// the dirty flag as needed.
		for (int i = 0; i < min; i++) {
			setValue(i, values[i]);
		}
	}

	/**
	 * Get a single byte value.
	 * <p>
	 * Get the byte according to the current setting either from a file or from the object storage.
	 * </p>
	 * 
	 * @param index
	 *            The byte to read. Must be between 0 and <code>getMaxBytes() - 1</code>
	 * @return <code>int</code> with the byte value or <code>-1</code> if the value was not set
	 *         or the index is out of bounds.
	 */
	public int getValue(int index) {
		if (!(0 <= index && index < getByteCount())) {
			return -1;
		}

		// getByteValues() will take care of the "use file" flag and
		// return the relevant byte values.
		int[] values = getValues();
		return values[index];
	}

	/**
	 * Set a single byte value.
	 * <p>
	 * The value is always written to the object storage, regardless of the "Use File" flag.
	 * </p>
	 * 
	 * @param index
	 *            The byte to set. Must be between 0 and <code>getByteCount() - 1</code>,
	 *            otherwise the value is ignored.
	 * @param value
	 *            <code>int</code> with the byte value (0-255) or <code>-1</code> to unset the
	 *            value.
	 * @throws IllegalArgumentException
	 *             if the the value is out of range (-1 to 255)
	 */
	public void setValue(int index, int value) {
		if (!(0 <= index && index < getByteCount())) {
			return;
		}
		if (!(-1 <= value && value <= 255)) {
			throw new IllegalArgumentException("invalid value:" + index
					+ " (must be between 0 and 255)");
		}

		if (fByteValues.getValue(index) != value) {
			fByteValues.setValue(index, value);
			fDirty = true;
		}
	}

	/**
	 * Clears all values.
	 * <p>
	 * This method will set the value of all bytes to <code>-1</code>
	 * </p>
	 */
	public void clearValues() {
		fByteValues.clearValues();
	}

	/**
	 * Copies the byte values from the file to the object storage.
	 */
	public void syncFromFile() {
		// TODO Not implemented yet
	}

	/**
	 * Get the list of avrdude arguments required to write all bytes.
	 * <p>
	 * Note: This method does <strong>not</strong> set the "-u" flag to disable the safemode. It is
	 * up to the caller to add this flag. If the "disable safemode" flag is not set, avrdude will
	 * restore the previous fusebyte values after the new values have been written.
	 * </p>
	 * 
	 * @return <code>List&lt;String&gt;</code> with avrdude action options.
	 */
	public abstract List<String> getArguments(String mcuid);

	/**
	 * Load the properties from the Preferences.
	 * <p>
	 * The <code>Preferences</code> object used is set in the constructor of this class.
	 * </p>
	 * 
	 */
	private void load() {

		// Get the MCU id of the parent TargetProperties
		// This is used as the default mcuid for this bytes object.
		String parentmcuid = fParent.getParent().getMCUId();

		fMCUid = fPrefs.get(KEY_MCUID, parentmcuid);
		fWriteFlag = fPrefs.getBoolean(KEY_WRITEFLAG, DEFAULT_WRITEFLAG);
		fUseFile = fPrefs.getBoolean(KEY_USEFILE, DEFAULT_USEFILE);
		fFileName = fPrefs.get(KEY_FILENAME, DEFAULT_FILENAME);

		String fusevaluestring = fPrefs.get(KEY_BYTEVALUES, "");

		// Clear the old values
		fByteValues.clearValues();

		// split the values
		String[] values = fusevaluestring.split(SEPARATOR);
		int count = Math.min(values.length, getByteCount());
		for (int i = 0; i < count; i++) {
			String value = values[i];
			if (value.length() != 0) {
				fByteValues.setValue(i, Integer.parseInt(values[i]));
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
			fPrefs.put(KEY_MCUID, fMCUid);
			fPrefs.putBoolean(KEY_WRITEFLAG, fWriteFlag);
			fPrefs.putBoolean(KEY_USEFILE, fUseFile);
			fPrefs.put(KEY_FILENAME, fFileName);

			// convert the values to a single String
			StringBuilder sb = new StringBuilder(20);
			for (int i = 0; i < getByteCount(); i++) {
				if (i > 0)
					sb.append(SEPARATOR);
				sb.append(fByteValues.getValue(i));
			}
			fPrefs.put(KEY_BYTEVALUES, sb.toString());

			fPrefs.flush();
		}
	}

	/**
	 * Test if this Object is valid for the given MCU.
	 * 
	 * @return <code>true</code> if the current byte values (either immediate or from a file) are
	 *         valid for the given MCU id.
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
