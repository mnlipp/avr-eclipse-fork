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
package de.innot.avreclipse.core.toolinfo.fuses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;

/**
 * A container for byte values.
 * <p>
 * This class holds the actual byte values for either the Fuse bytes or a Lockbit byte. These byte
 * values are only valid for the MCU type set at construction time. To change the MCU a new
 * ByteValue Object for the new MCU has to be created, for example with the special copy constructor ({@link #ByteValues(String, ByteValues)}).
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class ByteValues {

	/** The type of Bytes, FUSE oder LOCKBITS */
	private final FuseType						fType;

	/** The MCU for which the byte values are valid. Set during instantiation. */
	private final String						fMCUId;

	/** The number of bytes in this object */
	private final int							fByteCount;

	/** The actual byte values. The array is initialized during instantiation. */
	private final int[]							fValues;

	/** The description of the bitfields */
	private IFusesDescription					fDescription	= null;

	/** Map of all bitfield descriptions mapped to their name for easy access */
	private Map<String, BitFieldDescription>	fBitFieldNames	= null;

	/**
	 * Create a new byte values container for a given MCU.
	 * <p>
	 * The new ByteValues object is
	 * 
	 * @param mcuid
	 *            <code>String</code> with a MCU id value.
	 */
	public ByteValues(FuseType type, String mcuid) {
		Assert.isNotNull(mcuid);
		fType = type;
		fMCUId = mcuid;
		fByteCount = loadByteCount();
		fValues = new int[fByteCount];
		clearValues();
	}

	/**
	 * Clone constructor.
	 * <p>
	 * Creates a new byte values container and copies all values (and the MCU id) from the source.
	 * </p>
	 * 
	 * @param source
	 *            <code>ByteValues</code> object to clone.
	 */
	public ByteValues(ByteValues source) {
		Assert.isNotNull(source);
		fType = source.fType;
		fMCUId = source.fMCUId;
		fByteCount = source.fByteCount;
		fValues = new int[fByteCount];
		System.arraycopy(source.fValues, 0, fValues, 0, fByteCount);
	}

	/**
	 * Create a new byte values container for the given MCU id and copies the values of the given
	 * source ByteValues object to the new ByteValues object.
	 * <p>
	 * This constructor is used to change the MCU for an existing <code>ByteValues</code> object.
	 * Because the same bit values have different meanings for different MCU and might even cause an
	 * MCU to lock up, this constructor should be used carefully and only after review by the user.
	 * </p>
	 * <p>
	 * If the source has more values, they are truncated. If it has less then the values array is
	 * filled up with <code>-1</code> values.
	 * </p>
	 * 
	 * @param mcuid
	 *            A valid MCU id value.
	 * @param source
	 *            <code>ByteValues</code> object to clone.
	 */
	public ByteValues(String mcuid, ByteValues source) {
		fType = source.fType;
		fMCUId = mcuid;
		fByteCount = loadByteCount();
		fValues = new int[fByteCount];
		for (int i = 0; i < fValues.length; i++) {
			fValues[i] = -1;
		}
		int copycount = Math.min(fByteCount, source.fByteCount);
		System.arraycopy(source.fValues, 0, fValues, 0, copycount);
	}

	/**
	 * Get the Fuse type for these byte values.
	 * <p>
	 * Currently {@link FuseType#FUSE} and {@link FuseType#LOCKBITS} are the only supported types.
	 * </p>
	 * 
	 * @return
	 */
	public FuseType getType() {
		return fType;
	}

	/**
	 * Get the MCU associated with this ByteValues object and for which the byte values are valid.
	 * 
	 * @return <code>String</code> with a MCU id.
	 */
	public String getMCUId() {
		return fMCUId;
	}

	/**
	 * Get the actual number of bytes supported by the MCU.
	 * <p>
	 * Depending on the type of this object either the number of fuse bytes (0 up to 6) or the
	 * number of lockbits bytes (currently always 1) is returned.
	 * </p>
	 * <p>
	 * If the MCU is not supported <code>0</code> is returned.
	 * </p>
	 * 
	 * @return Number of bytes supported by the MCU. Between <code>0</code> and <code>6</code>.
	 */
	public int getByteCount() {
		return fByteCount;
	}

	/**
	 * Sets the byte at the given index to a value.
	 * 
	 * @param index
	 *            The index of the byte to set. Must be between 0 and {@link #getByteCount()}-1.
	 * @param value
	 *            The new value. Must be a byte value (0-255) or -1 to unset the value.
	 * @throws IllegalArgumentException
	 *             if the index is out of bounds or the value is out of range.
	 */
	public void setValue(int index, int value) {

		checkIndex(index);

		if (value < -1 || 255 < value) {
			throw new IllegalArgumentException("Value [" + value + "] out of range (-1...255)");
		}

		fValues[index] = value;
	}

	/**
	 * Get the value of the byte with the given index.
	 * 
	 * @param index
	 *            The index of the byte to read. Must be between 0 and {@link #getByteCount()}-1.
	 * @return <code>int</code> with a byte value (0-255) or <code>-1</code> if the value is not
	 *         set.
	 * @throws IllegalArgumentException
	 *             if the index is out of bounds.
	 */
	public int getValue(int index) {
		checkIndex(index);

		return fValues[index];
	}

	/**
	 * Set all byte values.
	 * <p>
	 * Copies all values from the given array to the internal storage. If the given
	 * <code>int[]</code> has less entries than this class supports, then the remaining bytes are
	 * untouched. Only {@link #getByteCount()} bytes are copied. Any additional bytes in the source
	 * array are ignored.
	 * </p>
	 * <p>
	 * The values of the source are not checked.
	 * </p>
	 * 
	 * @param newvalues
	 *            Array of <code>int</code> with the new byte values (0 to 255 or -1).
	 */
	public void setValues(int[] newvalues) {
		int count = Math.min(newvalues.length, fValues.length);
		System.arraycopy(newvalues, 0, fValues, 0, count);
	}

	/**
	 * Returns an array with all byte values.
	 * <p>
	 * The returned array is a copy of the internal structure and any changes to it will not be
	 * reflected.
	 * </p>
	 * 
	 * @return Array of <code>int</code> with the current byte values (0 to 255 or -1).
	 */
	public int[] getValues() {
		// make a copy and return it
		int[] copy = new int[fValues.length];
		System.arraycopy(fValues, 0, copy, 0, fValues.length);
		return copy;
	}

	/**
	 * Gets the value of the bitfield with the given name.
	 * <p>
	 * The result is the current value of the bitfield, already normalized (range 0 to maxValue).
	 * </p>
	 * 
	 * @param name
	 *            The name of the bitfield.
	 * @return The current value of the bitfield, or <code>-1</code> if the bitfield value is not
	 *         yet set.
	 * @throws IllegalArgumentException
	 *             if the name of the bitfield is not valid.
	 */
	public int getNamedValue(String name) {
		initBitFieldNames();
		BitFieldDescription desc = fBitFieldNames.get(name);
		if (desc == null) {
			throw new IllegalArgumentException("Bitfield name [" + name + "] is not known.");
		}
		int index = desc.getIndex();
		int value = fValues[index];
		if (value == -1)
			return value;
		return desc.bitfieldToValue(value);
	}

	/**
	 * Sets the value of the bitfield with the given name.
	 * 
	 * @param name
	 *            The name of the bitfield.
	 * @param value
	 *            The normalized new value for the bitfield (between 0 to maxValue)
	 * @throws IllegalArgumentException
	 *             if the name of the bitfield is not valid or the value is out of range (0 to
	 *             maxValue).
	 */
	public void setNamedValue(String name, int value) {
		initBitFieldNames();

		BitFieldDescription desc = fBitFieldNames.get(name);
		if (desc == null) {
			throw new IllegalArgumentException("Bitfield name [" + name + "] is not known.");
		}

		// Test if the value is within bounds
		if (value < 0 || desc.getMaxValue() < value) {
			throw new IllegalArgumentException("Value [" + value + "] out of range (0..."
					+ desc.getMaxValue() + ")");
		}

		int index = desc.getIndex();

		// Now left-shift the value to the right place and insert it
		// into the current value.
		int bitfieldvalue = desc.valueToBitfield(value);
		int oldvalue = fValues[index];
		if (oldvalue == -1)
			oldvalue = 0xff;
		int newvalue = oldvalue & ~desc.getMask();
		newvalue |= bitfieldvalue;
		fValues[index] = newvalue;
	}

	/**
	 * Get the descriptive text for the value of the named bitfield.
	 * <p>
	 * This method returns a human readable text for the value of the bitfield. This may be one of
	 * the enumerations from the part description file or some other meaningful text if no
	 * enumerations have been defined.
	 * </p>
	 * 
	 * @see IBitFieldDescription#getValueText(int)
	 * @param name
	 *            The name of the bitfield.
	 * @return Human readable string
	 * 
	 */
	public String getNamedValueText(String name) {
		initBitFieldNames();
		BitFieldDescription desc = fBitFieldNames.get(name);
		int value = getNamedValue(name);
		if (value == -1) {
			return "undefined";
		}
		String valuetext = desc.getValueText(value);

		return valuetext;
	}

	/**
	 * Clears all values.
	 * <p>
	 * This method will set the value of all bytes to <code>-1</code>
	 * </p>
	 */
	public void clearValues() {
		for (int i = 0; i < fValues.length; i++) {
			fValues[i] = -1;
		}
	}

	/**
	 * Get a list of all BitField names.
	 * <p>
	 * The returned list is a copy of the internal list.
	 * </p>
	 * 
	 * @return <code>List&lt;String&gt;</code> with the names.
	 */
	public List<String> getBitfieldNames() {
		initBitFieldNames();
		return new ArrayList<String>(fBitFieldNames.keySet());
	}

	/**
	 * Get a list of all {@link BitFieldDescription} objects.
	 * <p>
	 * The returned list is a copy of the internal list.
	 * </p>
	 * 
	 * @return <code>List&lt;IBitFieldDescription&gt;</code>.
	 */
	public List<BitFieldDescription> getBitfieldDescriptions() {
		initBitFieldNames();
		return new ArrayList<BitFieldDescription>(fBitFieldNames.values());
	}

	/**
	 * Get a single named {@link BitFieldDescription}.
	 * 
	 * @param name
	 *            of the BitField
	 * @return The <code>BitFieldDescription</code> or <code>null</code> if no BitField with the
	 *         given name exists.
	 */
	public BitFieldDescription getBitFieldDescription(String name) {
		initBitFieldNames();
		return fBitFieldNames.get(name);
	}

	/**
	 * Get the name of the byte at the given index.
	 * 
	 * @param index
	 *            Between 0 and {@link #getByteCount()} - 1.
	 * @return Name of the byte from the part description file.
	 */
	public String getByteName(int index) {
		IFusesDescription fusesdesc = getDescription(fMCUId);
		IByteDescription bytedesc = fusesdesc.getByteDescription(fType, index);
		return bytedesc.getName();
	}

	/**
	 * Checks if the index is valid for the subclass.
	 * 
	 * @param index
	 *            Index value to test.
	 * @throws IllegalArgumentException
	 *             if the index is not valid.
	 */
	private void checkIndex(int index) {
		if (!(0 <= index && index < getByteCount())) {
			throw new IllegalArgumentException("[" + index + "] is not a valid byte index.");
		}
	}

	/**
	 * Initialize the Map of Bitfield names to their corresponding description objects.
	 * 
	 */
	private void initBitFieldNames() {

		if (fBitFieldNames != null) {
			// return if the map has already been initialized
			return;
		}

		fBitFieldNames = new HashMap<String, BitFieldDescription>();
		IFusesDescription fusedescription = getDescription(fMCUId);
		if (fusedescription == null) {
			// If the fusedescription could not be read we leave the map empty.
			return;
		}

		// Get all byte descriptions, get the bitfield descriptions from them
		// and fill the map.
		List<IByteDescription> bytedesclist = fusedescription.getByteDescriptions(fType);

		for (IByteDescription bytedesc : bytedesclist) {
			List<BitFieldDescription> bitfieldlist = bytedesc.getBitFieldDescriptions();
			for (BitFieldDescription desc : bitfieldlist) {
				fBitFieldNames.put(desc.getName(), desc);
			}
		}
	}

	/**
	 * Determine the bytecount for the mcu from the description object.
	 * <p>
	 * In case of errors determining the actual bytecount <code>0</code> is returned.
	 * </p>
	 * 
	 * @return Number of bytes supported by the MCU. Between <code>0</code> and <code>6</code>.
	 */
	private int loadByteCount() {
		IFusesDescription fusedescription = getDescription(fMCUId);
		if (fusedescription == null) {
			return 0;
		}
		return fusedescription.getByteCount(fType);
	}

	/**
	 * Get the description object for the given mcu id.
	 * 
	 * @param mcuid
	 * @return <code>IFusesdescription</code> Object or <code>null</code> if the description
	 *         could not be loaded.
	 */
	private IFusesDescription getDescription(String mcuid) {

		if (fDescription == null) {
			try {
				fDescription = Fuses.getDefault().getDescription(mcuid);
			} catch (IOException e) {
				// Could not read the Description from the plugin
				// Log the error and return null (indicates no fuse bytes)
				IStatus status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
						"Could not read the description file from the filesystem", e);
				AVRPlugin.getDefault().log(status);
				return null;
			}
		}
		return fDescription;
	}

	/**
	 * Test if this Object is compatible with the given MCU.
	 * <p>
	 * The test will be successfull iff all bitfields for this and the given MCU have the same name
	 * and the same mask. In this case we assume that the meaning of the bitfields is also the same
	 * or at least very close.
	 * </p>
	 * <p>
	 * If the two MCUs are compatible it is (hopefully) save to use the
	 * {@link #ByteValues(String, ByteValues)} constructor to make a copy of this ByteValues object
	 * with the new MCU.
	 * </p>
	 * 
	 * @return <code>true</code> if the current byte values are also valid for the given MCU id.
	 * 
	 */
	public boolean isCompatibleWith(String mcuid) {
		IFusesDescription ourdesc = getDescription(fMCUId);
		IFusesDescription targetdesc = getDescription(mcuid);
		return ourdesc.isCompatibleWith(targetdesc, fType);
	}

	/**
	 * Convert this ByteValue object to a different MCU.
	 * <p>
	 * This method works by getting a list of all BitField names for the new target MCU and compares
	 * them one by one with the BitField names of this object. If the names match and also the
	 * length of the mask match, then the single BitField value is copied from this object to a new
	 * ByteValues object.
	 * </p>
	 * <p>
	 * Because sometimes different names are used for the same BitField there is a list of
	 * exceptions ({@link #CONVERTIBLES}) which will also be used to find matching names.
	 * </p>
	 * <p>
	 * For the user interface this method will fill three Lists with all fields converted, source
	 * fields not copied and target fields not set.
	 * </p>
	 * 
	 * @param mcuid
	 *            The MCU id for the new <code>ByteValues</code> object.
	 * @param successlist
	 *            A List with the BitFields that have been copied (names as used by the target).
	 * @param notcopiedlist
	 *            A List with the BitFields in this (source) object, that were not copied to the
	 *            target.
	 * @param unsetlist
	 *            A List with the BitFields in the target object that have no matching BitField in
	 *            the this (source) object.
	 * @return A new <code>ByteValues</code> object valid for the given MCU and with those
	 *         BitFields filled that match this object. All other bits are set to <code>1</code>.
	 */
	public ByteValues convertTo(String mcuid, List<BitFieldDescription> successlist,
			List<BitFieldDescription> notcopiedlist, List<BitFieldDescription> unsetlist) {

		initBitFieldNames();

		// Add all our bitfields to the notcopied list and remove those entries that have been
		// matched.
		notcopiedlist.addAll(fBitFieldNames.values());

		// Create a new ByteValues Object for the target mcu
		ByteValues target = new ByteValues(fType, mcuid);

		List<String> targetbfdnames = target.getBitfieldNames();

		for (String name : targetbfdnames) {

			// Check if the name matches.
			if (fBitFieldNames.containsKey(name)) {
				// OK, we have a matching name. Now check if the mask matches
				BitFieldDescription targetbfd = target.getBitFieldDescription(name);
				BitFieldDescription ourbfd = fBitFieldNames.get(name);
				if (targetbfd.getMaxValue() == ourbfd.getMaxValue()) {
					// similar masks: now copy the value
					int ourvalue = getNamedValue(name);
					if (ourvalue == -1) {
						// If the value is undefined we do not copy
						unsetlist.add(ourbfd);
						continue;
					}
					target.setNamedValue(name, ourvalue);
					// mark the BitField as success and skip over the no-match parts
					successlist.add(targetbfd);
					notcopiedlist.remove(ourbfd);
					continue;
				}
			}
			// no match found
			unsetlist.add(target.getBitFieldDescription(name));
		}

		return target;
	}
}
