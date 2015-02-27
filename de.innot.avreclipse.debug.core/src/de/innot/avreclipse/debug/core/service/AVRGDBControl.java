/*******************************************************************************
 * Copyright (c) 2011 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Ericsson - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.debug.core.service;

import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GDBJtagControl;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Jtag control which selects the Jtag CompleteInitializationSequence.
 */
public class AVRGDBControl extends GDBJtagControl {

	public AVRGDBControl(DsfSession session, ILaunchConfiguration config, CommandFactory factory) {
		super(session, config, factory);
	}
}