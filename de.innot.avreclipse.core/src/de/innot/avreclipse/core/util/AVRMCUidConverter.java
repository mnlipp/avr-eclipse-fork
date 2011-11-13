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
package de.innot.avreclipse.core.util;

/**
 * @author Thomas Holland
 * 
 */
public class AVRMCUidConverter {

	/**
	 * Change the lower case mcuid into the official Name.
	 * 
	 * @param mcuid
	 * @return String with UI name of the MCU or <code>null</code> if the given mcuid does not match
	 *         any of the supported name families.
	 */
	public static String id2name(String mcuid) {

		// check invalid mcu names
		if (mcuid == null) {
			return null;
		}
		if ("".equals(mcuid.trim())) {
			return null;
		}

		// AVR Specific
		if (mcuid.startsWith("atxmega")) {
			return "ATXmega" + mcuid.substring(7).toUpperCase();
		}
		if (mcuid.startsWith("atmega")) {
			return "ATmega" + mcuid.substring(6).toUpperCase();
		}
		if (mcuid.startsWith("attiny")) {
			return "ATtiny" + mcuid.substring(6).toUpperCase();
		}

		// The new Atmel Touchscreen Controllers
		if (mcuid.startsWith("atmxt")) {
			return "mXT" + mcuid.substring(5).toUpperCase();
		}

		// All other Atmel MCUs
		if (mcuid.startsWith("at")) {
			return mcuid.toUpperCase();
		}

		// Special AVR compatible motion controller chips from Schneider Electrics
		if (mcuid.startsWith("m30")) {
			return mcuid.toUpperCase();
		}

		// AVRDude now supports some AVR32 processors
		// Even though the plugin does not we still accept the name
		if (mcuid.startsWith("32")) {
			return mcuid.toUpperCase();
		}

		if (mcuid.startsWith("avr")) {
			// don't include the generic family names
			return null;
		}

		return null;
	}

	public static String name2id(String mcuname) {
		// just convert to lowercase
		return mcuname.toLowerCase();

	}

}
