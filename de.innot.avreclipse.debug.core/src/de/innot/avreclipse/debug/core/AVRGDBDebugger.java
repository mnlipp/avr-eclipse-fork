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

package de.innot.avreclipse.debug.core;

import org.eclipse.cdt.debug.mi.core.AbstractGDBCDIDebugger;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.CommandFactory;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AVRGDBDebugger extends AbstractGDBCDIDebugger {

	@Override
	protected CommandFactory getCommandFactory(ILaunchConfiguration config) throws CoreException {
		String miVersion = MIPlugin.getMIVersion(config);
		return new AvariceCommandFactory(miVersion);
	}

}
