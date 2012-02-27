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

import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.service.GdbDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.service.command.CommandFactory_6_8;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @since 8.0
 */
public class AVRDebugServicesFactory extends GdbDebugServicesFactory {


	public AVRDebugServicesFactory(String version) {
		super(version);
	}

	protected ICommandControl createCommandControl(DsfSession session, ILaunchConfiguration config) {
		if (GDB_7_2_VERSION.compareTo(getVersion()) <= 0) {
			return new AVRGDBControl_7_2(session, config, new CommandFactory_6_8());
		}
		if (GDB_7_0_VERSION.compareTo(getVersion()) <= 0) {
			return new AVRGDBControl_7_0(session, config, new CommandFactory_6_8());
		}
		if (GDB_6_8_VERSION.compareTo(getVersion()) <= 0) {
			return new AVRGDBControl(session, config, new CommandFactory_6_8());
		}
		return new AVRGDBControl(session, config, new CommandFactory());
	}
}
