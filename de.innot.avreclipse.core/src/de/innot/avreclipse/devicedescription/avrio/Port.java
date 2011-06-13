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
package de.innot.avreclipse.devicedescription.avrio;

import de.innot.avreclipse.devicedescription.IEntry;

/**
 * A I/O Port description for the avr/io.h device model.
 * <p>
 * This extends {@link Register}. The only difference is, that port are always
 * in I/O address space.
 * </p>
 * 
 * @author Thomas Holland
 * 
 */
public class Port extends Register {

	public Port(IEntry parent) {
		super(parent);
	}

	@Override
	public String getAddrType() {
		// ports are always in io space
		return "IO";
	}

}
