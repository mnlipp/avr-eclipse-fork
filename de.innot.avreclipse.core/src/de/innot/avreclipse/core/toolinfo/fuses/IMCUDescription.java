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

/**
 * Describes all fuse and lockbits bytes of a single MCU type. An Object that holds
 * {@link BitFieldDescription} objects for a number of bytes.
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public interface IMCUDescription {

	/** <code>int</code> value that represents a non-existing or illegal value. */
	public final static int	NO_VALUE	= -1;

	/**
	 * Get the MCU id value for which the bitfield descriptions are valid.
	 * 
	 * @return <code>String</code> with the MCU id.
	 */
	public String getMCUId();

	/**
	 * Get the build number of the description.
	 * <p>
	 * As this is not provided anymore by the latest Atmel Device files, the file date of the source
	 * file is used instead.
	 * </p>
	 * <p>
	 * It is used to by the plugin to determine if a fuse/lockbit description file is newer than the
	 * one supplied by the plugin.
	 * </p>
	 * 
	 * @return The value of the build element.
	 */
	public int getVersion();

	/**
	 * Get the number of fuse or lockbits bytes that are defined in this description.
	 * 
	 * @param type
	 *            the type of of fuse memory for which the count is wanted. Either
	 *            {@link FuseType#FUSE} or {@link FuseType#LOCKBITS}.
	 * 
	 * @return <code>int</code> with the number of bytes.
	 */
	public int getByteCount(FuseType type);

	/**
	 * Get the {@link IFuseObjectDescription} for a single byte with the given name.
	 * 
	 * @param name
	 *            The name of the byte as used in the part description file.
	 * @return The description for the selected byte, or <code>null</code> if no byte with the name
	 *         exists.
	 */
	public IFuseObjectDescription getByteDescription(String name);

	/**
	 * Get the {@link IFuseObjectDescription} for a single fuse or lockbits byte.
	 * 
	 * @param type
	 *            the type of bytedescription required. Either {@link FuseType#FUSE} or
	 *            {@link FuseType#LOCKBITS}.
	 * @param index
	 *            The fuse byte for which to get the descriptions. Between 0 and 5, depending on the
	 *            MCU.
	 * @return The description for the selected byte
	 * @throws ArrayIndexOutOfBoundsException
	 *             when the index is invalid.
	 */
	public IFuseObjectDescription getByteDescription(FuseType type, int index);

	/**
	 * Get the list of {@link IFuseObjectDescription}s for all fuse or lockbits bytes.
	 * <p>
	 * The returned list is a copy of the internal list and can be modified without affecting the
	 * internal list.
	 * </p>
	 * 
	 * @param type
	 *            the type of bytedescriptions required. Either {@link FuseType#FUSE} or
	 *            {@link FuseType#LOCKBITS}.
	 * @return
	 */
	public List<IFuseObjectDescription> getByteDescriptions(FuseType type);

	/**
	 * Checks if the target IMCUDescription is compatible with this IMCUDescription.
	 * <p>
	 * They are compatible iff they have the same number of bytes and all BitFields have the same
	 * name and the same mask. The meaning of the BitFields are not checked since we assume that
	 * they are reasonably close or identical (this assumption has not yet been verified).
	 * </p>
	 * <p>
	 * Both the Fuses and the LockBits are compared with this call. As the LockBits seem to be
	 * identical for all AVR processors this should not be a problem.
	 * </p>
	 * 
	 * @param target
	 *            The <code>IMCUDescription</code> to check against.
	 * @param type
	 *            The type of descriptions to compare. Either {@link FuseType#FUSE} or
	 *            {@link FuseType#LOCKBITS}.
	 * @return <code>true</code> if the given description is (reasonable) compatible.
	 */
	public boolean isCompatibleWith(IMCUDescription target, FuseType type);
}