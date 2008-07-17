package de.innot.avreclipse.core.toolinfo.fuses;

import java.util.List;

public interface IBitFieldDescription {

	/**
	 * @return the index
	 */
	public int getIndex();

	/**
	 * @return the name
	 */
	public String getName();

	/**
	 * @return the description
	 */
	public String getDescription();

	/**
	 * @return the mask
	 */
	public int getMask();

	/**
	 * Return the maximum value acceptable for this bitfield.
	 * <p>
	 * This is 2^^(number of 1-bits in the mask) minus 1.
	 * </p>
	 * 
	 * @return max value
	 */
	public int getMaxValue();

	/**
	 * Convert a normalize value to a bitfield value.
	 * <p>
	 * This method will left-shift the given value for the required number of places to match the
	 * mask.
	 * </p>
	 * 
	 * @param value
	 *            the normalized value (range 0 to getMaxValue())
	 * @return <code>int</code> with the shifted value.
	 */
	public int valueToBitfield(int value);

	/**
	 * Convert a bitfield value to a normalized value.
	 * <p>
	 * This method will mask off all bits outside this bitfield and then right-shift the result so
	 * that a normalized value (range 0 to {@link #getMaxValue()}) is returned.
	 * </p>
	 * 
	 * @param bitfieldvalue
	 *            a byte from which to extract and normalize the value of this bitfield.
	 * @return <code>int</code> with the normalized value.
	 */
	public int bitfieldToValue(int bitfieldvalue);

	/**
	 * @return a copy of the bitfield value enumeration, or <code>null</code> if the no values are
	 *         defined.
	 */
	public List<IBitFieldValueDescription> getValues();

}