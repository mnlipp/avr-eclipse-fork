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
/**
 * 
 */
package de.innot.avreclipse.debug.core;

import org.eclipse.cdt.core.settings.model.CConfigurationStatus;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.gdbjtag.core.GDBJtagDSFLaunchConfigurationDelegate;
import org.eclipse.cdt.dsf.debug.service.IDsfDebugServicesFactory;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;

import de.innot.avreclipse.debug.core.service.AVRDebugServicesFactory;
import de.innot.avreclipse.debug.gdbserver.IGDBServerFactory;

/**
 * @author Michael
 * 
 */
public class AVRGDBLaunchDelegate extends GDBJtagDSFLaunchConfigurationDelegate {

	@Override
	public void launch( ILaunchConfiguration config, String mode, ILaunch launch, IProgressMonitor monitor ) throws CoreException {
		// First launch GDBServer
		org.eclipse.cdt.launch.LaunchUtils.enableActivity("org.eclipse.cdt.debug.dsfgdbActivity", true); //$NON-NLS-1$
		if ( monitor == null ) {
			monitor = new NullProgressMonitor();
		}
		if ( mode.equals( ILaunchManager.DEBUG_MODE ) ) {
			launchGDBServer( config, launch, monitor );
		}
		
		// Now launch GDB
		super.launch(config, mode, launch, monitor);
	}
	
	private void launchGDBServer(ILaunchConfiguration config, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {
		monitor.beginTask(LaunchMessages.getString("GdbLaunchDelegate.0"), 10);  //$NON-NLS-1$
		if ( monitor.isCanceled() ) {
			return;
		}

		try {
			String serverId = config.getAttribute
					(IAVRGDBConstants.ATTR_GDBSERVER_ID, "");
			IGDBServerFactory factory = AVRDebugPlugin.getDefault()
				.getGDBServerFactories().get(serverId);
			if (factory == null) {
				abort("Selected GDB Server not available", null, 
						ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);
			}
    		factory.launchServer( config, launch, monitor );
		}
		finally {
			monitor.done();
		}		
	}

	protected IDsfDebugServicesFactory newServiceFactory
				(ILaunchConfiguration config, String version) {
		return new AVRDebugServicesFactory(version);
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.launch.AbstractCLaunchDelegate#getPluginID()
	 */
	@Override
	protected String getPluginID() {
		return AVRDebugPlugin.PLUGIN_ID;
	}

}
