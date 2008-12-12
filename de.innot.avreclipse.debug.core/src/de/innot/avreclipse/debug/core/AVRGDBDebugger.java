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
 * $Id$
 *     
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
