/**
 * 
 */
package de.innot.avreclipse.debug.core;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.AbstractCLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author Thomas
 * 
 */
public class AVRGDBLaunchConfigurationDelegate extends AbstractCLaunchDelegate {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.launch.AbstractCLaunchDelegate#getPluginID()
	 */
	@Override
	protected String getPluginID() {
		return AVRDebugPlugin.PLUGIN_ID;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.cdt.launch.AbstractCLaunchDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration
	 * , java.lang.String, org.eclipse.debug.core.ILaunch,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch,
			IProgressMonitor monitor) throws CoreException {

		cancel("TargetConfiguration not supported",
				ICDTLaunchConfigurationConstants.ERR_INTERNAL_ERROR);

	}

}
