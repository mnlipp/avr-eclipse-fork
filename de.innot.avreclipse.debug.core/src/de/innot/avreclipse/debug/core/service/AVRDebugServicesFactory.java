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

import java.util.StringTokenizer;

import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GDBJtagControl;
import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GDBJtagControl_7_0;
import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GDBJtagControl_7_2;
import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GDBJtagControl_7_4;
import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GDBJtagControl_7_7;
import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GdbJtagDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
import org.eclipse.cdt.dsf.gdb.service.command.CommandFactory_6_8;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @since 8.0
 */
public class AVRDebugServicesFactory extends GdbJtagDebugServicesFactory {


	public AVRDebugServicesFactory(String version) {
		super(version);
	}

	protected ICommandControl createCommandControl(DsfSession session, ILaunchConfiguration config) {
		StringTokenizer versionParts = new StringTokenizer(getVersion(), ".");
		int major = Integer.parseInt(versionParts.nextToken());
		int minor = Integer.parseInt(versionParts.nextToken());
		if (major == 7 && minor >= 7) {
			return new GDBJtagControl_7_7(session, config, new PatchedCommandFactory_6_8());
		}
		if (major == 7 && minor >= 4) {
			return new GDBJtagControl_7_4(session, config, new PatchedCommandFactory_6_8());
		}
		if (major == 7 && minor >= 2) {
			return new GDBJtagControl_7_2(session, config, new PatchedCommandFactory_6_8());
		}
		if (major == 7) {
			return new GDBJtagControl_7_0(session, config, new PatchedCommandFactory_6_8());
		}
		return new GDBJtagControl(session, config, new PatchedCommandFactory());
	}

}
