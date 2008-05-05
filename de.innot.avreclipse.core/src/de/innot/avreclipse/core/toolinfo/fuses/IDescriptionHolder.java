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

/**
 * An Object that holds {@link BitFieldDescription} objects for a number of bytes.
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public interface IDescriptionHolder {

	/** <code>int</code> value that represents a non-existing or illegal value. */
	public final static int	NO_VALUE	= -1;

	/**
	 * Get the MCU id value for which the bitfield descriptions are valid.
	 * 
	 * @return <code>String</code> with the MCU id.
	 */
	public String getMCUId();

	/**
	 * Get the number of bytes that the DescriptionHolder has descriptions for.
	 * <p>
	 * Between 0 and 3 for FuseDescriptions, and always 1 for LockbitsDescriptions.
	 * </p>
	 * 
	 * @return <code>int</code> with the number of bytes.
	 */
	public int getByteCount();

	/**
	 * Get the {@link BitFieldDescription}s for a single byte.
	 * 
	 * 
	 * @param index
	 *            The byte for which to get the bitfield descriptions. Between 0 and 2, depending on
	 *            the MCU.
	 * @return Array of <code>BitFieldDescription</code> objects for the byte at the index.
	 * @throws IllegalArgumentException
	 *             when the index is invalid.
	 */
	public BitFieldDescription[] getBitFieldDescriptions(int index);

	/**
	 * Get the default value for a single byte.
	 * <p>
	 * The default value is derived from the part description files. Might be <code>NO_VALUE</code>
	 * if no default value has been defined in the part description files.
	 * </p>
	 * <p>
	 * The default value for LockbitBytes is always <code>0xff</code> (all bits set), which means no
	 * locks.
	 * </p>
	 * 
	 * @param index
	 *            The byte for which to get the default value. Between 0 and 2, depending on the
	 *            MCU.
	 * @return <code>int</code> with a byte value (0-255), or <code>NO_VALUE</code> if no
	 *         default value is available.
	 */
	public int getDefaultValue(int index);

}