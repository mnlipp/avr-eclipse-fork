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

import de.innot.avreclipse.core.avrdude.LockbitBytes;

/**
 * Container for the Lockbit byte(s).
 * <p>
 * While all current AVR MCUs only have one lockbit byte, this class could support multiple bytes in
 * the future because it uses the same API as {@link FuseByteValues}.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class LockbitsByteValues extends ByteValues {

	private final static String	LOCK	= "lock";

	/**
	 * Create a new lockbits byte value container for a given MCU.
	 * <p>
	 * The MCU parameter is stored but only used for reference. The actual number of bytes does not
	 * depend on the MCU but is taken from the subclass via the {@link #getMaxBytes()} hook method.
	 * </p>
	 * 
	 * @param mcuid
	 *            <code>String</code> with a MCU id value.
	 */
	public LockbitsByteValues(String mcuid) {
		super(mcuid);
	}

	/**
	 * Clone constructor.
	 * <p>
	 * Creates a new lockbits byte value container and copies all values (and the MCU id) from the
	 * source.
	 * </p>
	 * 
	 * @param source
	 *            <code>ByteValues</code> object to clone.
	 */
	public LockbitsByteValues(ByteValues source) {
		super(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.ByteValues#getMaxBytes()
	 */
	@Override
	public int getMaxBytes() {
		return LockbitBytes.MAX_LOCKBITBYTES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.ByteValues#getByteCount()
	 */
	@Override
	public int getByteCount() {
		try {
			return Locks.getDefault().getByteCount(getMCUId());
		} catch (IOException e) {
			// If you want to see the Exception use the Fuses class directly
			return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.ByteValues#nameToIndex(java.lang.String)
	 */
	@Override
	public int nameToIndex(String name) {
		int index = -1;
		if (LOCK.equals(name)) {
			index = 0;
		}

		return index;
	}

}
