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

import java.io.Serializable;

/**
 * {@link IDescriptionHolder} implementation for FuseBytes.
 * <p>
 * Objects of this class hold the {@link BitFieldDescription} objects for all fuse bytes of a single
 * MCU.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class FusesDescription implements Serializable, IDescriptionHolder {

	/* Change this whenever the fields of this class have changed */
	private static final long				serialVersionUID	= 1210974654053970636L;

	/** The MCU for this description. */
	private final String					fMCUid;

	/** The number of bytes in this object holds */
	private final int						fByteCount;

	/** Array of arrays with BitFieldDescriptions. One entry for each fuse byte. */
	private final BitFieldDescription[][]	fBitfields;

	/** Array with default values, one for each byte */
	private final int[]						fDefaultValues;

	/** Array with the Fuse byte names (from the part description file) */
	private final String[]					fByteName;

	/**
	 * Create a new FusesDescription for a MCU with the given number of fuse bytes.
	 * 
	 * @param mcuid
	 *            <code>String</code> with a MCU id value.
	 * @param bytecount
	 *            <code>int</code> with the number of fuse bytes this MCU has.
	 */
	public FusesDescription(String mcuid, int bytecount) {
		fMCUid = mcuid;
		fByteCount = bytecount;
		fBitfields = new BitFieldDescription[bytecount][];
		fByteName = new String[bytecount];
		fDefaultValues = new int[bytecount];

		// set the defaults to -1 = no default available
		for (int i = 0; i < bytecount; i++) {
			fByteName[i] = "";
			fDefaultValues[i] = -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder#getMCUId()
	 */
	public String getMCUId() {
		return fMCUid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder#getByteCount()
	 */
	public int getByteCount() {
		return fByteCount;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder#getBitFields(int)
	 */
	public BitFieldDescription[] getBitFieldDescriptions(int index) {
		checkIndex(index);
		return fBitfields[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder#getByteName(int)
	 */
	public String getByteName(int index) {
		checkIndex(index);
		return fByteName[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder#getDefaultValue(int)
	 */
	public int getDefaultValue(int index) {
		checkIndex(index);
		return fDefaultValues[index];
	}

	/**
	 * Sets the <code>BitFieldDescriptions</code> for the fuse byte at the given index.
	 * <p>
	 * The new array replaces any previous array. Only a reference to the array is stored; the array
	 * itself is not copied and will reflect any subsequent changes made by the caller.
	 * </p>
	 * <p>
	 * The array can be retrieved with {@link #getBitFieldDescriptions(int)}.
	 * </p>
	 * 
	 * @param index
	 *            <code>int</code> with the fuse byte index.
	 * @param name
	 *            <code>String</code> with the part description file name of the fuse byte.
	 * @param bitfields
	 *            Array of <code>BitFieldDescription</code> objects for the byte at the given
	 *            index.
	 * @throws IllegalArgumentException
	 *             if the index is not valid for this MCU.
	 */
	public void setBitFieldDescriptions(int index, String name, BitFieldDescription[] bitfields) {
		checkIndex(index);
		fBitfields[index] = bitfields;
		fByteName[index] = name;
	}

	/**
	 * Sets the default value for the fuse byte at the given index.
	 * <p>
	 * The default value can be retrieved with {@link #getDefaultValue(int)}
	 * </p>
	 * 
	 * @param index
	 *            <code>int</code> with the fuse byte index.
	 * @param value
	 *            The new default value.
	 * @throws IllegalArgumentException
	 *             if the index is not valid for this MCU.
	 */
	public void setDefaultValue(int index, int value) {
		checkIndex(index);
		fDefaultValues[index] = value;
	}

	/**
	 * Checks if the index is valid.
	 * <p>
	 * If the index is not valid for this DescriptionHolder an <code>IllegalArgumentException</code>
	 * is thrown.
	 * </p>
	 * 
	 * @param index
	 *            Byte index.
	 */
	private void checkIndex(int index) {
		if (index >= fByteCount) {
			throw new IllegalArgumentException("index value " + index + " > " + (fByteCount - 1));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Fuses for " + getMCUId());
		sb.append(" (" + getByteCount() + ") [");
		for (int i = 0; i < getByteCount(); i++) {
			sb.append("[ (" + i + ") " + fByteName[i] + " ");
			BitFieldDescription[] fields = getBitFieldDescriptions(i);
			for (int j = 0; j < fields.length; j++) {
				sb.append("[");
				sb.append(fields[j].toString());
				sb.append("] ");
			}
			sb.append("] ");
		}
		sb.append("]");

		return sb.toString();
	}
}
