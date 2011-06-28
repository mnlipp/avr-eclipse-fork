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
package de.innot.avreclipse.debug.gdbservers.avarice;

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

/**
 * The settings page for the avarice gdbserver.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 * @see IGDBServerAvariceConstants
 * 
 */
public class AvariceSettingsPage extends AbstractGDBServerSettingsPage implements
		IGDBServerAvariceConstants {

	/** Name to be shown in the user interface. */
	private final static String		COMMANDNAME				= "AVaRICE";

	private final static String[][]	AVARICE_INTERFACES		= new String[][] {
			{ "AVR Dragon", "--dragon" }, { "AVRISP MkI or compatible", "--mkI" },
			{ "AVRICE MkII or compatible", "--mkII" }		};

	/** The (fixed?) MkI bitrates. MkII bitrates can be between 22 and 6400 kHz */
	private final static String[]	AVARICE_JTAG_BITRATES	= new String[] { "125kHz", "250kHz",
			"500kHz", "1000kHz"							};

	// The GUI widgets
	private Text					fCommandName;
	private Button					fVerbose;
	private Text					fPortNumber;
	private Combo					fInterface;
	private Text					fJTAGPort;
	private Combo					fJTAGBitRate;
	private Button					fDebugWire;
	private Button					fIgnoreIntr;
	private Text					fOtherOptions;

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.gdbservers.AbstractGDBServerSettingsPage#getCommandName()
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

		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_COMMAND,
				DEFAULT_GDBSERVER_AVARICE_COMMAND);
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_HOSTNAME,
				DEFAULT_GDBSERVER_AVARICE_HOSTNAME);
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_VERBOSE,
				DEFAULT_GDBSERVER_AVARICE_VERBOSE);
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_PORT, DEFAULT_GDBSERVER_AVARICE_PORT);

		// //////////////////////////////////////////
		// The JTAG interface attributes
		// //////////////////////////////////////////

		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_INTERFACE,
				DEFAULT_GDBSERVER_AVARICE_INTERFACE);
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_JTAGPORT,
				DEFAULT_GDBSERVER_AVARICE_JTAGPORT);
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_JTAGBITRATE,
				DEFAULT_GDBSERVER_AVARICE_JTAGBITRATE);
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_DEBUGWIRE,
				DEFAULT_GDBSERVER_AVARICE_DEBUGWIRE);

		// //////////////////////////////////////////
		// The interrupt control attributes
		// //////////////////////////////////////////

		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_IGNOREINTR,
				DEFAULT_GDBSERVER_AVARICE_IGNOREINTR);
		// TODO: add the non-interrupting event list.

		// //////////////////////////////////////////
		// The obligatory "other options" attribute
		// //////////////////////////////////////////

		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_OTHEROPTIONS,
				DEFAULT_GDBSERVER_AVARICE_OTHEROPTIONS);
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

		createJTAGInterfaceOptions(compo);

		createInterruptControlOptions(compo);

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
				.setToolTipText("The port number for communication betweem avr-gdb and avarice.\n"
						+ "Leave this at 4242 unless this port is blocked by another application.");

		fVerbose = new Button(compo, SWT.CHECK);
		fVerbose.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false, 4, 1));
		fVerbose.setText("Verbose console mode");
		fVerbose.setToolTipText("Select this to receive avarice debugging output on the console.");
		fVerbose.addSelectionListener(new SelectionAdapter() {
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
	private void createJTAGInterfaceOptions(Composite compo) {
		// //////////////////////////////////////////
		// The JTAG interface options
		// //////////////////////////////////////////

		Group interfacegroup = new Group(compo, SWT.NONE);
		interfacegroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		interfacegroup.setLayout(new GridLayout(2, false));
		interfacegroup.setText("JTAG interface settings");

		Label label = new Label(interfacegroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("JTAG interface adapter:");

		fInterface = new Combo(interfacegroup, SWT.READ_ONLY);
		fInterface.setToolTipText("The JTAG adapter interface which is connected to this system");
		fInterface.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false));
		for (String[] interfaces : AVARICE_INTERFACES) {
			fInterface.add(interfaces[0]);
		}
		fInterface.addSelectionListener(new SelectionAdapter() {
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

		label = new Label(interfacegroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("JTAG interface port:");

		fJTAGPort = new Text(interfacegroup, SWT.BORDER | SWT.SINGLE);
		fJTAGPort.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fJTAGPort.setToolTipText("The port to which the JTAG interface adapter is connected to");
		fJTAGPort.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				updatePage();
			}
		});

		label = new Label(interfacegroup, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("JTAG interface bitrate:");
		fJTAGBitRate = new Combo(interfacegroup, SWT.NONE);
		GridData gd = new GridData(SWT.BEGINNING, SWT.FILL, false, false);
		gd.widthHint = 100;
		fJTAGBitRate.setLayoutData(gd);
		fJTAGBitRate.setItems(AVARICE_JTAG_BITRATES);
		fJTAGBitRate
				.setToolTipText("The bitrate for the communication between the JTAG interface adapter and the target system.\n"
						+ "Set this to no more than 1/4 of the target system clock frequency.\n"
						+ "The following modifieres are allowed:\n" //
						+ " - 'm' or 'mHz'\n" //
						+ " - 'k' or 'kHz'\n" //
						+ "If left empty the avarice default is used (250kHz)");
		fJTAGBitRate.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				// TODO: handle m/mHz and k/kHz modifiers

				updatePage();
			}
		});

		fDebugWire = new Button(interfacegroup, SWT.CHECK);
		fDebugWire.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 4, 1));
		fDebugWire.setText("Use debugWire mode");
		fDebugWire
				.setToolTipText("Select this if your target AVR system is connected via the debugWire interface");
		fDebugWire.addSelectionListener(new SelectionAdapter() {
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
	private void createInterruptControlOptions(Composite compo) {
		// //////////////////////////////////////////
		// The interrupt control options
		// //////////////////////////////////////////

		Group controlgroup = new Group(compo, SWT.NONE);
		controlgroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 4, 1));
		controlgroup.setLayout(new GridLayout(2, false));
		controlgroup.setText("Interrupt control settings");

		fIgnoreIntr = new Button(controlgroup, SWT.CHECK);
		fIgnoreIntr.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, true, false, 2, 1));
		fIgnoreIntr.setText("Do not stop at each interrupt");
		fIgnoreIntr
				.setToolTipText(" - If selected avarice will step over all interrupts (unless a breakpoint has been set)\n"
						+ " - If deselected avarice will stop on all interrupts.\n"
						+ "This will not work with target devices fused for compatibility.");
		fIgnoreIntr.addSelectionListener(new SelectionAdapter() {
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

			String commandname = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_COMMAND,
					DEFAULT_GDBSERVER_AVARICE_COMMAND);
			fCommandName.setText(commandname);

			// Port number: a -1 value is translated to an empty port => error shown in UI
			int port = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_PORT,
					DEFAULT_GDBSERVER_AVARICE_PORT);
			String porttext = port >= 0 ? Integer.toString(port) : "";
			fPortNumber.setText(porttext);

			boolean verbose = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_VERBOSE,
					DEFAULT_GDBSERVER_AVARICE_VERBOSE);
			fVerbose.setSelection(verbose);

			// //////////////////////////////////////////
			// The JTAG interface options
			// //////////////////////////////////////////

			String jtaginterface = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_INTERFACE,
					DEFAULT_GDBSERVER_AVARICE_INTERFACE);
			fInterface.select(fInterface.indexOf(jtaginterface));

			String jtagport = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_JTAGPORT,
					DEFAULT_GDBSERVER_AVARICE_JTAGPORT);
			fJTAGPort.setText(jtagport);

			String jtagbitrate = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_JTAGBITRATE,
					DEFAULT_GDBSERVER_AVARICE_JTAGBITRATE);
			fJTAGBitRate.setText(jtagbitrate);

			boolean debugwire = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_DEBUGWIRE,
					DEFAULT_GDBSERVER_AVARICE_DEBUGWIRE);
			fDebugWire.setSelection(debugwire);

			// //////////////////////////////////////////
			// The interrupt control options
			// //////////////////////////////////////////

			boolean ignoreintr = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_IGNOREINTR,
					DEFAULT_GDBSERVER_AVARICE_IGNOREINTR);
			fIgnoreIntr.setSelection(ignoreintr);

			// //////////////////////////////////////////
			// The "other options" option
			// //////////////////////////////////////////

			String otheroptions = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_OTHEROPTIONS,
					DEFAULT_GDBSERVER_AVARICE_OTHEROPTIONS);
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
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_COMMAND, commandname);

		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_HOSTNAME,
				DEFAULT_GDBSERVER_AVARICE_HOSTNAME);

		String port = fPortNumber.getText();
		int portnumber;
		if (port.length() > 0) {
			portnumber = Integer.parseInt(port);
		} else {
			portnumber = -1;
		}
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_PORT, portnumber);

		boolean verbose = fVerbose.getSelection();
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_VERBOSE, verbose);

		// //////////////////////////////////////////
		// The JTAG interface options
		// //////////////////////////////////////////

		String jtaginterface = fInterface.getText();
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_INTERFACE, jtaginterface);

		String jtagport = fJTAGPort.getText();
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_JTAGPORT, jtagport);

		String jtagbitrate = fJTAGBitRate.getText();
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_JTAGBITRATE, jtagbitrate);

		boolean debugwire = fDebugWire.getSelection();
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_DEBUGWIRE, debugwire);

		// //////////////////////////////////////////
		// The interrupt control options
		// //////////////////////////////////////////

		boolean ignoreintr = fIgnoreIntr.getSelection();
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_IGNOREINTR, ignoreintr);

		// //////////////////////////////////////////
		// The "other options" option
		// //////////////////////////////////////////

		String otheroptions = fOtherOptions.getText();
		configuration.setAttribute(ATTR_GDBSERVER_AVARICE_OTHEROPTIONS, otheroptions);

	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#isValid(org.eclipse.debug.core.
	 * ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {

		// Check if the avarice gdbserver is active
		try {
			String gdbserverid = configuration.getAttribute(IAVRGDBConstants.ATTR_GDBSERVER_ID, "");
			if (!getGDBServerID().equals(gdbserverid)) {
				// avarice is not active - no further testing needed
				return true;
			}

			// The commandname must not be empty
			String commandname = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_COMMAND, "");
			if (commandname.length() == 0) {
				return false;
			}

			// The port must be within the range 0-65535
			int portnumber = configuration.getAttribute(ATTR_GDBSERVER_AVARICE_PORT, -1);
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
