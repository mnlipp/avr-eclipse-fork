/*******************************************************************************
 * Copyright (c) 2012 Michael Lipp (mnl@mnl.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Lipp - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.debug.gdbservers.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import de.innot.avreclipse.debug.gdbserver.IGDBServerFactory;

public class RemoteGDBServerFactory implements IGDBServerFactory {

	@Override
	public void launchServer(ILaunchConfiguration config, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

}
