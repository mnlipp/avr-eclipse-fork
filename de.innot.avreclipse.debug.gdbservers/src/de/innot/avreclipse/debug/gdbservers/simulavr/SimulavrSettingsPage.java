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
package de.innot.avreclipse.debug.gdbservers.simulavr;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
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
public class SimulavrSettingsPage extends AbstractGDBServerSettingsPage implements
		IGDBServerSimulavrConstants {

	/** Name to be shown in the user interface. */
	private final static String	COMMANDNAME	= "SimulAVR";

	// The GUI widgets
	private Text				fCommandName;
	private Text				fPortNumber;
	private Button				fVerboseDebug;
	private Button				fVerboseGDBServer;
	private Combo				fMCUType;
	private Text				fFCPU;
	private Text				fOtherOptions;

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

		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_COMMAND,
				DEFAULT_GDBSERVER_SIMULAVR_COMMAND);
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_HOSTNAME,
				DEFAULT_GDBSERVER_SIMULAVR_HOSTNAME);
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_PORT, DEFAULT_GDBSERVER_SIMULAVR_PORT);

		// //////////////////////////////////////////
		// The verbose attributes
		// //////////////////////////////////////////

		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_VERBOSE_DEBUG,
				DEFAULT_GDBSERVER_SIMULAVR_VERBOSE_DEBUG);

		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_VERBOSE_GDBSERVER,
				DEFAULT_GDBSERVER_SIMULAVR_VERBOSE_GDBSERVER);

		// //////////////////////////////////////////
		// The MCU attributes
		// //////////////////////////////////////////

		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_MCUTYPE,
				DEFAULT_GDBSERVER_SIMULAVR_MCUTYPE);
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_FCPU, DEFAULT_GDBSERVER_SIMULAVR_FCPU);

		// //////////////////////////////////////////
		// The obligatory "other options" attribute
		// //////////////////////////////////////////

		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_OTHEROPTIONS,
				DEFAULT_GDBSERVER_SIMULAVR_OTHEROPTIONS);
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
		compo.setLayout(new GridLayout(4, false));

		createMainOptions(compo);

		createVerboseOptions(compo);

		createMCUOptions(compo);

		fOtherOptions = createOtherOptionsField(compo);
	}

	/**
	 * @param compo
	 */
	private void createMainOptions(Composite compo) {
		// //////////////////////////////////////////
		// The main options
		// //////////////////////////////////////////

		fCommandName = createCommandField(compo);

		fPortNumber = createPortField(compo);
		fPortNumber
				.setToolTipText("The port number for communication betweem avr-gdb and simulavr.\n"
						+ "Leave this at 1212 unless this port is blocked by another application.");
	}

	/**
	 * @param compo
	 */
	private void createVerboseOptions(Composite compo) {
		// //////////////////////////////////////////
		// The verbosity options
		// //////////////////////////////////////////

		Group verbositygroup = new Group(compo, SWT.NONE);
		verbositygroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		verbositygroup.setLayout(new GridLayout(2, false));
		verbositygroup.setText("Verbosity settings");

		fVerboseDebug = new Button(verbositygroup, SWT.CHECK);
		fVerboseDebug.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false, 4, 1));
		fVerboseDebug.setText("Debug instruction output");
		fVerboseDebug
				.setToolTipText("Select this to receive simulavr instruction debug output on the console.");
		fVerboseDebug.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * @seeorg.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.
			 * SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePage();
			}
		});

		fVerboseGDBServer = new Button(verbositygroup, SWT.CHECK);
		fVerboseGDBServer.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false, 4, 1));
		fVerboseGDBServer.setText("Print out gdbserver debug messages");
		fVerboseGDBServer
				.setToolTipText("Select this to receive simulavr gdbserver debug output on the console.");
		fVerboseGDBServer.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * @seeorg.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.
			 * SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePage();
			}
		});

	}

	/**
	 * @param compo
	 */
	private void createMCUOptions(Composite compo) {
		// //////////////////////////////////////////
		// The MCU options
		// //////////////////////////////////////////

		Group mcugroup = new Group(compo, SWT.NONE);
		mcugroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		mcugroup.setLayout(new GridLayout(2, false));
		mcugroup.setText("Simulated MCU settings");

		Label label = new Label(mcugroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Simulated MCU type:");

		fMCUType = new Combo(mcugroup, SWT.READ_ONLY);
		fMCUType.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fMCUType.setToolTipText("Select the MCU type to simulate.");
		fMCUType.addSelectionListener(new SelectionAdapter() {
			/*
			 * (non-Javadoc)
			 * @seeorg.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.
			 * SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				updatePage();
			}
		});
		// TODO: replace mock data with real values
		fMCUType.setItems(new String[] { "atmega8", "atmega16", "atmega32" });

		label = new Label(mcugroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Simulated clock speed:");

		fFCPU = new Text(mcugroup, SWT.BORDER | SWT.SINGLE);
		fFCPU.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fFCPU.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				updatePage();
			}
		});

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

			String commandname = configuration.getAttribute(ATTR_GDBSERVER_SIMULAVR_COMMAND,
					DEFAULT_GDBSERVER_SIMULAVR_COMMAND);
			fCommandName.setText(commandname);

			int port = configuration.getAttribute(ATTR_GDBSERVER_SIMULAVR_PORT,
					DEFAULT_GDBSERVER_SIMULAVR_PORT);
			String porttext = port >= 0 ? Integer.toString(port) : "";
			fPortNumber.setText(porttext);

			// //////////////////////////////////////////
			// The verbosity options
			// //////////////////////////////////////////

			boolean verboseDebug = configuration
					.getAttribute(ATTR_GDBSERVER_SIMULAVR_VERBOSE_DEBUG,
							DEFAULT_GDBSERVER_SIMULAVR_VERBOSE_DEBUG);
			fVerboseDebug.setSelection(verboseDebug);

			boolean verboseGDBServer = configuration.getAttribute(
					ATTR_GDBSERVER_SIMULAVR_VERBOSE_GDBSERVER,
					DEFAULT_GDBSERVER_SIMULAVR_VERBOSE_GDBSERVER);
			fVerboseGDBServer.setSelection(verboseGDBServer);

			// //////////////////////////////////////////
			// The MCU options
			// //////////////////////////////////////////

			String mcuid = configuration.getAttribute(ATTR_GDBSERVER_SIMULAVR_MCUTYPE,
					DEFAULT_GDBSERVER_SIMULAVR_MCUTYPE);
			fMCUType.select(fMCUType.indexOf(mcuid));

			int fcpu = configuration.getAttribute(ATTR_GDBSERVER_SIMULAVR_FCPU,
					DEFAULT_GDBSERVER_SIMULAVR_FCPU);
			fFCPU.setText(Integer.toString(fcpu));

			// //////////////////////////////////////////
			// The "other options" option
			// //////////////////////////////////////////

			String otheroptions = configuration.getAttribute(ATTR_GDBSERVER_SIMULAVR_OTHEROPTIONS,
					DEFAULT_GDBSERVER_SIMULAVR_OTHEROPTIONS);
			fOtherOptions.setText(otheroptions);

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
		// The main options, incl. the non user-selectable hostname.
		// //////////////////////////////////////////

		String commandname = fCommandName.getText();
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_COMMAND, commandname);

		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_HOSTNAME,
				DEFAULT_GDBSERVER_SIMULAVR_HOSTNAME);

		String port = fPortNumber.getText();
		int portnumber;
		if (port.length() > 0) {
			portnumber = Integer.parseInt(port);
		} else {
			portnumber = -1;
		}
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_PORT, portnumber);

		// //////////////////////////////////////////
		// The verbosity options
		// //////////////////////////////////////////

		boolean verboseDebug = fVerboseDebug.getSelection();
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_VERBOSE_DEBUG, verboseDebug);

		boolean verboseGDBServer = fVerboseDebug.getSelection();
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_VERBOSE_GDBSERVER, verboseGDBServer);

		// //////////////////////////////////////////
		// The MCU options
		// //////////////////////////////////////////

		String mcutype = fMCUType.getText();
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_MCUTYPE, mcutype);

		String fcpu = fFCPU.getText();
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_FCPU, fcpu);

		// //////////////////////////////////////////
		// The "other options" option
		// //////////////////////////////////////////

		String otheroptions = fOtherOptions.getText();
		configuration.setAttribute(ATTR_GDBSERVER_SIMULAVR_OTHEROPTIONS, otheroptions);

	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#isValid(org.eclipse.debug.core.
	 * ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {

		// Check if the simulavr gdbserver is active
		try {
			String gdbserverid = configuration.getAttribute(IAVRGDBConstants.ATTR_GDBSERVER_ID, "");
			if (!getGDBServerID().equals(gdbserverid)) {
				// simulavr is not active - no further testing needed
				return true;
			}

			// The commandname must not be empty
			String commandname = configuration.getAttribute(ATTR_GDBSERVER_SIMULAVR_COMMAND, "");
			if (commandname.length() == 0) {
				return false;
			}

			// The port must be within the range 0-65535
			int portnumber = configuration.getAttribute(ATTR_GDBSERVER_SIMULAVR_PORT, -1);
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
