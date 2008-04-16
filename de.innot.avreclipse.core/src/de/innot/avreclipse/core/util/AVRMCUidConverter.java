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
 * $Id: GCC.java 163 2008-01-28 18:54:15Z innot $
 *     
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
	 * @return String with UI name of the MCU or null if it should not be
	 *         included (e.g. generic family names like 'avr2')
	 */
	public static String id2name(String mcuid) {
		// remove invalid entries
		if ("".equals(mcuid.trim())) {
			return null;
		}
		// AVR Specific
		if (mcuid.startsWith("atxmega")) {
			return "ATXmega"+mcuid.substring(7).toUpperCase();
		}
		if (mcuid.startsWith("atmega")) {
			return "ATmega" + mcuid.substring(6).toUpperCase();
		}
		if (mcuid.startsWith("attiny")) {
			return "ATtiny" + mcuid.substring(6).toUpperCase();
		}
		if (mcuid.startsWith("at")) {
			return mcuid.toUpperCase();
		}
		if (mcuid.startsWith("avr")) {
			// don't include the generic family names
			return null;
		}

		return mcuid;
	}
	
	public static String name2id(String mcuname) {
		// just convert to lowercase
		return mcuname.toLowerCase();

	}

}
