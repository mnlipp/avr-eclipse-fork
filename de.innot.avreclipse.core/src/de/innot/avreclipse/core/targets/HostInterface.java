/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.core.targets;

/**
 * Enumeration of all host interfaces.
 * <p>
 * The host interface is part of the {@link IProgrammer} interface and is used to filter the
 * programmers in the user interface. Also the user interface uses this to show only applicable
 * options.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public enum HostInterface {

	SERIAL_BB("Serial Port / BitBanger type"), SERIAL("Serial Port"), PARALLEL("Parallel Port"), USB(
			"USB Port");

	private final String	fDesc;

	private HostInterface(String desc) {
		fDesc = desc;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Enum#toString()
	 */
	public String toString() {
		return fDesc;
	}

}
