/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.core.toolinfo.fuses;

import java.util.List;

public interface IFuseObjectDescription {

	/**
	 * Get the list of <code>BitFieldDescription</code> objects for this byte.
	 * <p>
	 * The returned list is a copy of the actual list. Any modifications to the returned list do not
	 * apply to the original list of this byte description object.
	 * </p>
	 * 
	 * @return <code>List&lt;BitFieldDescription&gt;</code>
	 */
	public List<BitFieldDescription> getBitFieldDescriptions();

	/**
	 * Get the name of this fuse byte object.
	 * <p>
	 * This is the name as defined in the Atmel device XML file. Currently the name may be one of
	 * the following:
	 * <ul>
	 * <li><code>LOW</code>, <code>HIGH</code> or <code>EXTENDED</code> for the fuse bytes of
	 * pre-ATXmega MCUs.</li>
	 * <li><code>FUSEBYTE0</code>, <code>FUSEBYTE1</code>, ..., <code>FUSEBYTE5</code> for the fuse
	 * bytes of ATXmega MCUs.</li>
	 * <li><code>LOCKBITS</code> for the lockbits byte.</li>
	 * </ul>
	 * </p>
	 * <p>
	 * <strong>Note:</strong> AVRDude uses different names to access the fuse bytes. It is up to the
	 * caller to map the names as required.
	 * </p>
	 * 
	 * @return The name of the byte.
	 */
	public String getName();

	/**
	 * Get a description of this byte object.
	 * <p>
	 * A (more or less) meaningful description of this object as provided by the Atmel device XML
	 * file.
	 * </p>
	 * 
	 * @return A descriptive string. May be <code>null</code>
	 */
	public String getDescription();

	/**
	 * The number of bytes in this fuses object.
	 * <p>
	 * This will be <code>1</code> for the 8 bit AVR processors and <code>4</code> for the 32 bit
	 * AVR processors.
	 * </p>
	 * 
	 * @return
	 */
	public int getSize();

	/**
	 * Get the index of this byte.
	 * <p>
	 * The index is the address of this byte within the Fuses memory block. It is between
	 * <code>0</code> for the first byte (usually called "low") up to the maximum number of btes
	 * supported by the MCU.
	 * </p>
	 * 
	 * @return The byte index.
	 */
	public int getIndex();

	/**
	 * Get the default value of this byte.
	 * <p>
	 * The part description files have only default settings for some MCUs. In these MCUs the return
	 * value will by a byte value (0-255).<br>
	 * For fuse bytes without default value <code>-1</code> is returned.<br>
	 * For lockbit bytes the default value of <code>0xFF</code> is returned (= no locks).
	 * </p>
	 * <p>
	 * As of AVR Studio 5.0 and the new device XML files Atmel does not give the default values
	 * anymore. Therefore this method has been deprecated as of 2.4.
	 * </p>
	 * 
	 * @return The default value or <code>-1</code> if no default available.
	 */
	@Deprecated
	public int getDefaultValue();

	/**
	 * Checks if the target IByteDescription is compatible with this IByteDescription.
	 * <p>
	 * They are compatible if all BitFields have the same name and the same mask. The meaning of the
	 * BitFields are not checked since we assume that they are reasonably close or identical (this
	 * assumption has not yet been verified).
	 * </p>
	 * 
	 * @param target
	 *            The <code>IByteDescription</code> to check against.
	 * @return <code>true</code> if the given description is (reasonable) compatible.
	 */
	public boolean isCompatibleWith(IFuseObjectDescription target);
}