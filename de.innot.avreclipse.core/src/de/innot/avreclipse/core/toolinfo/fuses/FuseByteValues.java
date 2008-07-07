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

/**
 * Container for the fuse bytes.
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class FuseByteValues extends ByteValues {

	/**
	 * Create a new fuse byte values container for a given MCU.
	 * <p>
	 * The MCU parameter is stored but only used for reference. The actual number of bytes does not
	 * depend on the MCU but is taken from the subclass via the {@link #getByteCount()} hook method.
	 * </p>
	 * 
	 * @param mcuid
	 *            <code>String</code> with a MCU id value.
	 */
	public FuseByteValues(String mcuid) {
		super(mcuid);
	}

	/**
	 * Clone constructor.
	 * <p>
	 * Creates a new fuse byte values container and copies all values (and the MCU id) from the
	 * source.
	 * </p>
	 * 
	 * @param source
	 *            <code>ByteValues</code> object to clone.
	 */
	public FuseByteValues(ByteValues source) {
		super(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.ByteValues#getByteCount()
	 */
	@Override
	public int getByteCount() {
		try {
			return Fuses.getDefault().getByteCount(getMCUId());
		} catch (IOException e) {
			// If you want to see the Exception use the Fuses class directly
			return 0;
		}
	}
}
