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

package de.innot.avreclipse.core.avrdude;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public enum ProgrammerInterface {

	// Standard In System Programming interface
	ISP("In-System Programming"),

	// JTAG interface
	JTAG("JTAG") {
		@Override
		public boolean isOCDCapable() {
			return true;
		}
	},

	// Special Atmel DebugWire interface
	DW("DebugWire") {
		@Override
		public boolean isOCDCapable() {
			return true;
		}
	},

	// Special Atmel High Voltage Serial Programming interface
	HVSP("High Voltage Serial Programming"),

	// Special Atmel Parallel Programming interface
	PP("Parallel Programming"),

	// Serial interface for a Bootloader
	BOOTLOADER("Bootloader over serial line");

	/**
	 * Checks if the interface supports On Chip Debugging
	 * 
	 * @return <code>true</code> if capable of OCD.
	 */
	public boolean isOCDCapable() {
		// All interfaces except JTAG and DEBUGWIRE can not
		// be used for On Chip Debugging
		return false;
	}

	private final String	fDescription;

	private ProgrammerInterface(String description) {
		fDescription = description;
	}

	/**
	 * Get a human readable description of the interface type
	 * 
	 * @return
	 */
	public String getDescription() {
		return fDescription;
	}

	/**
	 * Tries to guess the interface from the AVRDude config id.
	 * <p>
	 * This has been tested with avrdude 5.6. Future versions of avrdude might return wrong results.
	 * </p>
	 * 
	 * @param avrdudeid
	 *            The id of the programmer as used by avrdude.
	 * @return The interface used by the programmer.
	 */
	public static ProgrammerInterface getInterface(String avrdudeid) {

		// Check if this is one of the HVSP supporting devices
		if (avrdudeid.endsWith("hvsp")) {
			return HVSP;
		}

		// Check if this is one of the PP supporting devices
		if (avrdudeid.endsWith("pp")) {
			return PP;
		}

		// Check if this is one of the DebugWire supporting devices
		if (avrdudeid.endsWith("dw")) {
			return DW;
		}

		// First check for ISP devices (to filter JTAG devices with ISP mode)
		if (avrdudeid.endsWith("isp")) {
			return ISP;
		}

		// Check if this is JTAG device
		if (avrdudeid.contains("jtag") || avrdudeid.contains("xil")) {
			return JTAG;
		}

		// Check the serial bootloader types
		if (avrdudeid.equals("avr109") || avrdudeid.equals("butterfly")
				|| avrdudeid.equals("avr910")) {
			return BOOTLOADER;
		}

		// I think we filtered everything out, so what is left is a normal ISP programmer.
		return ISP;

	}
}
