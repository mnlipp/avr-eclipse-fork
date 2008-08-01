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
 * Enumeration of fuse type memories.
 * <p>
 * Currently fuse bytes {@link #FUSE} and lockbits bytes {@link #LOCKBITS} are supported, however
 * more enumeration values could be added to support e.g. calibration bytes.
 * </p>
 * <p>
 * This enum is used throughout the <code>de.innot.avreclipse.toolinfo.fuses</code> package to
 * differentiate between fuses and lockbits which are almost the same from the plugin perspective.
 * </p>
 * 
 * @see ByteValues
 * @see ByteDescription
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public enum FuseType {

	/** Fuse byte type */
	FUSE("fusebyte", "FUSE", 6),

	/** Lockbits byte type */
	LOCKBITS("lockbitsbyte", "LOCKBIT", 1);

	private final String	fElementName;
	private final String	fMemspaceName;
	private final int		fMaxBytes;

	private FuseType(String elementname, String memspacename, int maxbytes) {
		fElementName = elementname;
		fMemspaceName = memspacename;
		fMaxBytes = maxbytes;
	}

	/**
	 * Get the xml element tag name for the fuse memory type.
	 * 
	 * @return XML tag name used in the *.desc files.
	 */
	public String getElementName() {
		return fElementName;
	}

	/**
	 * Get the name used in the "memspace" attribute in the part description file.
	 * <p>
	 * Currently this method will return either "FUSE" or "LOCKBIT".
	 * </p>
	 * 
	 * @return
	 */
	public String getMemspaceName() {
		return fMemspaceName;
	}

	/**
	 * Convert a memspace attribute from the part description file to a FuseType enum value.
	 * 
	 * @param memspace
	 *            A <code>String</code> with the text content of the memspace attribute.
	 * @return
	 */
	public static FuseType getTypeFromPDFmemspace(String memspace) {
		if ("FUSE".equalsIgnoreCase(memspace)) {
			return FuseType.FUSE;
		} else if ("LOCKBIT".equalsIgnoreCase(memspace)) {
			return FuseType.LOCKBITS;
		} else {
			// add other types as when they are become supported
		}

		return null;
	}

	/**
	 * Gets the maximum number of bytes this type of fuse memory can support.
	 * <p>
	 * This method is deprecated because all code should dynamically determine the number of fuse
	 * bytes for the current MCU instead of using this static reference which might not be enough
	 * for future MCUs.
	 * </p>
	 * 
	 * @return <code>6</code> for Fuse memory and <code>1</code> for Lockbit memory.
	 */
	@Deprecated
	public int getMaxBytes() {
		return fMaxBytes;
	}

}
