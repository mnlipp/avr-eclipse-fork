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

	SERIAL_BB("Serial Port / BitBanger"), SERIAL("Serial Port"), PARALLEL("Parallel Port"), USB(
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
