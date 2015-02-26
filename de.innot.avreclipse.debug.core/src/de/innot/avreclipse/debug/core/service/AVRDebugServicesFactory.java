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

import org.eclipse.cdt.debug.gdbjtag.core.dsf.gdb.service.GdbJtagDebugServicesFactory;
import org.eclipse.cdt.dsf.debug.service.IDisassembly;
import org.eclipse.cdt.dsf.debug.service.command.ICommandControl;
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
		if (major >= 7) {
			return new AVRGDBControl(session, config, new PatchedCommandFactory_6_8());
		}
		int minor = Integer.parseInt(versionParts.nextToken());
		if (major == 6 && minor >= 8) {
			return new AVRGDBControl(session, config, new PatchedCommandFactory_6_8());
		}
		return new AVRGDBControl(session, config, new PatchedCommandFactory());
	}

}
