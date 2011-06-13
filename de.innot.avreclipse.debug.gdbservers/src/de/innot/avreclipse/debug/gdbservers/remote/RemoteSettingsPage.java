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
package de.innot.avreclipse.debug.gdbservers.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import de.innot.avreclipse.debug.core.IAVRGDBConstants;
import de.innot.avreclipse.debug.gdbservers.AbstractGDBServerSettingsPage;
import de.innot.avreclipse.debug.gdbservers.avarice.IGDBServerAvariceConstants;

/**
 * The settings page for the avarice gdbserver.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 * @see IGDBServerAvariceConstants
 * 
 */
public class RemoteSettingsPage extends AbstractGDBServerSettingsPage implements
		IGDBServerRemoteConstants {

	/** Name to be shown in the user interface. */
	private final static String	COMMANDNAME	= "Remote";

	// The GUI widgets
	private Text				fHostname;
	private Text				fPortNumber;

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.gdbservers.AbstractGDBServerSettingsPage#getGDBServerName()
	 */
	@Override
	protected String getGDBServerName() {
		return COMMANDNAME;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#setDefaults(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {

		// Set all configuration values to their default setting.

		// TODO: get the defaults from the preferences if the user has set them.

		// //////////////////////////////////////////
		// The main attributes
		// //////////////////////////////////////////

		// Note that HOSTNAME is set to the default, even if it is not user selectable.

		configuration.setAttribute(ATTR_GDBSERVER_REMOTE_HOSTNAME,
				DEFAULT_GDBSERVER_REMOTE_HOSTNAME);
		configuration.setAttribute(ATTR_GDBSERVER_REMOTE_PORT, DEFAULT_GDBSERVER_REMOTE_PORT);

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#createSettingsPage(org.eclipse.swt.widgets
	 * .Composite)
	 */
	@Override
	public void createSettingsPage(Composite parent) {

		Composite compo = new Composite(parent, SWT.NONE);
		compo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compo.setLayout(new GridLayout(2, false));

		createMainOptions(compo);

	}

	/**
	 * @param compo
	 */
	private void createMainOptions(Composite compo) {
		// //////////////////////////////////////////
		// The main options
		// //////////////////////////////////////////

		fHostname = createHostnameField(compo);
		fHostname
				.setToolTipText("The host address of the remote gdbserver. Either a host name or a ip address.");

		fPortNumber = createPortField(compo);
		fPortNumber
				.setToolTipText("The port number for communication betweem avr-gdb and the remote gdbserver");

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#initializeFrom(org.eclipse.debug.core
	 * .ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {

		// Update all user interface widgets with the attributes of the ILaunchConfiguration
		// If an attribute has not yet been set the default value is used instead.

		try {

			// //////////////////////////////////////////
			// The main options
			// //////////////////////////////////////////

			String hostname = configuration.getAttribute(ATTR_GDBSERVER_REMOTE_HOSTNAME,
					DEFAULT_GDBSERVER_REMOTE_HOSTNAME);
			fHostname.setText(hostname);

			int port = configuration.getAttribute(ATTR_GDBSERVER_REMOTE_PORT,
					DEFAULT_GDBSERVER_REMOTE_PORT);
			String porttext = port >= 0 ? Integer.toString(port) : "";
			fPortNumber.setText(porttext);

		} catch (CoreException ce) {
			// TODO: log exception

		}
	}

	/*
	 * (non-Javadoc)
	 * @seede.innot.avreclipse.debug.ui.IGDBServerSettingsPage#performApply(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		// Copy the current settings to the given ILaunchConfiguration working copy

		// //////////////////////////////////////////
		// The main options
		// //////////////////////////////////////////

		String hostname = fHostname.getText();
		configuration.setAttribute(ATTR_GDBSERVER_REMOTE_HOSTNAME, hostname);

		String port = fPortNumber.getText();
		int portnumber;
		if (port.length() > 0) {
			portnumber = Integer.parseInt(port);
		} else {
			portnumber = -1;
		}
		configuration.setAttribute(ATTR_GDBSERVER_REMOTE_PORT, portnumber);

	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#isValid(org.eclipse.debug.core.
	 * ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {

		// Check if the remote gdbserver is active
		try {
			String gdbserverid = configuration.getAttribute(IAVRGDBConstants.ATTR_GDBSERVER_ID, "");
			if (!getGDBServerID().equals(gdbserverid)) {
				// remote gdbserver is not active - no further testing needed
				return true;
			}

			// The hostname must not be empty
			String commandname = configuration.getAttribute(ATTR_GDBSERVER_REMOTE_HOSTNAME, "");
			if (commandname.length() == 0) {
				return false;
			}

			// The port must be within the range 0-65535
			int portnumber = configuration.getAttribute(ATTR_GDBSERVER_REMOTE_PORT, -1);
			if (portnumber < 0 || portnumber > 0xffff) {
				return false;
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return true;
	}

}
