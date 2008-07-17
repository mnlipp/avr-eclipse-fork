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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;

/**
 * A container for byte values.
 * <p>
 * </p>
 * <p>
 * It manages an arbitrary array of byte values and knows for which MCU these byte values are valid.
 * It is up to the subclasses to assign a meaning to these byte value
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

	/** Map of the bitfielddescriptions */
	private Map<String, IBitFieldDescription>	fBitFieldNames	= null;

	/**
	 * Create a new byte values container for a given MCU.
	 * <p>
	 * The MCU parameter is stored but only used for reference. The actual number of bytes does not
	 * depend on the MCU but is taken from the subclass via the {@link #getByteCount()} hook method.
	 * </p>
	 * 
	 * @param mcuid
	 *            <code>String</code> with a MCU id value.
	 */
	public ByteValues(FuseType type, String mcuid) {
		Assert.isNotNull(mcuid);
		fType = type;
		fMCUId = mcuid;
		fByteCount = getByteCount();
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
	 */
	protected ByteValues(String mcuid, ByteValues source) {
		fType = source.fType;
		fMCUId = mcuid;
		fByteCount = getByteCount();
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
	 * Get the current MCU for which the byte values are valid.
	 * 
	 * @return <code>String</code> with a MCU id.
	 */
	public String getMCUId() {
		return fMCUId;
	}

	/**
	 * Sets the byte at the given index to a value.
	 * 
	 * @param index
	 *            The index of the byte to set. Must be between 0 and <code>getMaxBytes() - 1</code>.
	 * @param value
	 *            The new value. Must be a byte value (0-255) or -1 to unset the value.
	 * @throws IllegalArgumentException
	 *             if the index is out of bounds or the value is out of range.
	 */
	public void setValue(int index, int value) {

		checkIndex(index);

		if (value < -1 || value > 255) {
			throw new IllegalArgumentException("Value [" + value + "] out of range (-1...255)");
		}

		fValues[index] = value;
	}

	/**
	 * Get the value of the byte with the given index.
	 * 
	 * @param index
	 *            The index of the byte to read. Must be between 0 and
	 *            <code>getMaxBytes() - 1</code>.
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
	 * <code>int[]</code> has less entries than this class supports, then the remaining bytes
	 * remain untouched. Only <code>getMaxBytes()</code> bytes are copied. Any additional bytes in
	 * the source array are ignored.
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
	 * The returned array is a copy of the internal structure.
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
	 * Returns the value of the bitfield with the given name.
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
		IBitFieldDescription desc = fBitFieldNames.get(name);
		if (desc == null) {
			throw new IllegalArgumentException("Bitfield name [" + name + "] is not known.");
		}
		int index = desc.getIndex();
		int value = fValues[index];
		if (value == -1)
			return value;
		return desc.bitfieldToValue(value);
	}

	public void setNamedValue(String name, int value) {
		initBitFieldNames();
		IBitFieldDescription desc = fBitFieldNames.get(name);
		if (desc == null) {
			throw new IllegalArgumentException("Bitfield name [" + name + "] is not known.");
		}
		int index = desc.getIndex();

		// Test if the value is within bounds
		if (value > desc.getMaxValue() || value < 0) {
			throw new IllegalArgumentException("Value [" + value + "] out of range (0..."
					+ desc.getMaxValue() + ")");
		}

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
		IFusesDescription fusedescription = getDescription(fMCUId);
		return fusedescription.getByteCount(fType);
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

	private void initBitFieldNames() {

		if (fBitFieldNames != null) {
			return;
		}

		IFusesDescription fusedescription = getDescription(fMCUId);
		fBitFieldNames = new HashMap<String, IBitFieldDescription>();

		List<IByteDescription> bytedesclist = fusedescription.getByteDescriptions(fType);

		for (IByteDescription bytedesc : bytedesclist) {
			List<IBitFieldDescription> bitfieldlist = bytedesc.getBitFieldDescriptions();
			for (IBitFieldDescription desc : bitfieldlist) {
				fBitFieldNames.put(desc.getName(), desc);
			}
		}
	}

	private IFusesDescription getDescription(String mcuid) {

		if (fDescription == null) {
			try {
				fDescription = Fuses.getDefault().getDescription(mcuid);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return fDescription;
	}

}
