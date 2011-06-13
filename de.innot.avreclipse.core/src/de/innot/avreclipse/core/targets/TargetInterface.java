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

package de.innot.avreclipse.core.targets;

/**
 * Enumeration of all known interfaces between a programmer and the target hardware.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public enum TargetInterface {

	/** Standard In System Programming interface */
	ISP("In-System Programming"),

	/** JTAG interface */
	JTAG("JTAG") {
		@Override
		public boolean isOCDCapable() {
			return true;
		}
	},

	/*** Special Atmel DebugWire interface */
	DW("DebugWire") {
		@Override
		public boolean isOCDCapable() {
			return true;
		}
	},

	/** Special Atmel High Voltage Serial Programming interface */
	HVSP("High Voltage Serial Programming"),

	/** Special Atmel Parallel Programming interface */
	PP("Parallel Programming"),

	/** Serial interface for a Bootloader */
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

	private TargetInterface(String description) {
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

}
