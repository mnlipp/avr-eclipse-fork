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
package de.innot.avreclipse.debug.ui;

import java.io.File;

import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.cdt.debug.mi.core.MIPlugin;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryDescriptor;
import org.eclipse.cdt.debug.mi.core.command.factories.CommandFactoryManager;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.innot.avreclipse.debug.core.IAVRGDBConstants;

/**
 * The LaunchConfigurationTab for general settings of the debugger
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TabDebugger extends AbstractLaunchConfigurationTab implements IAVRGDBConstants,
		IMILaunchConfigurationConstants {

	private static final String	TAB_NAME	= "Debugger";

	// The GUI Elements
	private Text				fAVRGDBCommand;
	private Button				fVerboseMode;

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.AbstractLaunchConfigurationTab#activated(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		// TODO Auto-generated method stub
		super.activated(workingCopy);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return TAB_NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return AVRGDBImages.getImage(AVRGDBImages.TAB_DEBUGGER_IMG);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.
	 * ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String gdbCommandAttr = configuration.getAttribute(ATTR_DEBUG_NAME,
					DEFAULT_COMMAND_NAME);
			fAVRGDBCommand.setText(gdbCommandAttr);

			boolean verboseModeAttr = configuration.getAttribute(ATTR_DEBUGGER_VERBOSE_MODE,
					DEFAULT_VERBOSE_MODE);
			fVerboseMode.setSelection(verboseModeAttr);

		} catch (CoreException e) {
			AVRGDBUIPlugin.log(e.getStatus());
		}

	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_DEBUG_NAME, fAVRGDBCommand.getText().trim());
		configuration.setAttribute(ATTR_DEBUGGER_COMMAND_FACTORY, DEFAULT_COMMAND_FACTORY);
		configuration.setAttribute(ATTR_DEBUGGER_PROTOCOL, DEFAULT_DEBUGGER_PROTOCOL);
		configuration.setAttribute(ATTR_DEBUGGER_VERBOSE_MODE, fVerboseMode.getSelection());
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_DEBUG_NAME, DEFAULT_COMMAND_NAME);

		CommandFactoryManager cfManager = MIPlugin.getDefault().getCommandFactoryManager();
		CommandFactoryDescriptor defDesc = cfManager.getDefaultDescriptor(DEBUGGER_ID);
		configuration.setAttribute(ATTR_DEBUGGER_COMMAND_FACTORY, defDesc.getName());
		configuration.setAttribute(ATTR_DEBUGGER_PROTOCOL, defDesc.getMIVersions()[0]);

		configuration.setAttribute(ATTR_DEBUGGER_VERBOSE_MODE, DEFAULT_VERBOSE_MODE);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
		// IDebugHelpContextIds.SOURCELOOKUP_TAB);
		GridLayout topLayout = new GridLayout();
		// topLayout.marginWidth = 0;
		// topLayout.marginHeight = 0;
		// topLayout.numColumns = 1;
		comp.setLayout(topLayout);
		comp.setFont(parent.getFont());

		// Composite comp = new Composite(sc, SWT.NONE);
		// comp.setLayout(new GridLayout());
		// sc.setContent(comp);

		createCommandGroup(comp);

	}

	/**
	 * Add the "GDB Setup" group to the parent composite.
	 * 
	 * @param parent
	 */
	private void createCommandGroup(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout());
		group.setText("GDB Setup");

		// /////////////////////////////////////////////////////////
		//
		// The "AVR GDB command" option
		//
		// /////////////////////////////////////////////////////////

		Composite commandComp = new Composite(group, SWT.NONE);
		commandComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		commandComp.setLayout(new GridLayout(3, false));

		Label label = new Label(commandComp, SWT.NONE);
		label.setText("AVR GDB command:");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false, 3, 1));

		fAVRGDBCommand = new Text(commandComp, SWT.SINGLE | SWT.BORDER);
		fAVRGDBCommand.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fAVRGDBCommand.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		Button button = new Button(commandComp, SWT.NONE);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonAction("Select AVR GDB Binary", fAVRGDBCommand);
			}
		});

		button = new Button(commandComp, SWT.NONE);
		button.setText("Variables...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				variablesButtonAction(fAVRGDBCommand);
			}
		});

		// /////////////////////////////////////////////////////////
		//
		// The "Verbose console mode" option
		//
		// /////////////////////////////////////////////////////////

		fVerboseMode = new Button(commandComp, SWT.CHECK);
		fVerboseMode.setText("Verbose console mode");
		fVerboseMode.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

	}

	/**
	 * Open a file dialog with the given title and for the given <code>Text</code> Control.
	 * <p>
	 * If the user selects a new file then the full path of the file will written to the
	 * <code>Text</code> control value. If the user cancels the dialog, then the <code>Text</code>
	 * control is untouched.
	 * </p>
	 * 
	 * @param title
	 *            Dialog title string
	 * @param text
	 *            The <code>Text</code> control to be modified
	 */
	private void browseButtonAction(String title, Text text) {
		FileDialog dialog = new FileDialog(getShell(), SWT.NONE);
		dialog.setText(title);

		// Check if the previously entered commandname contains a path segment
		File commandfile = new File(text.getText().trim());
		String commandfilepath = commandfile.getParent();
		if (commandfilepath != null) {
			dialog.setFilterPath(commandfilepath);
		}
		String newcommand = dialog.open();
		if (newcommand != null)
			text.setText(newcommand);
	}

	/**
	 * Open a variables browser dialog and append the selected variable to the content of the given
	 * <code>Text</code> control.
	 * 
	 * @param text
	 *            The <code>Text</code> control to be modified
	 */
	private void variablesButtonAction(Text text) {
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
		dialog.open();
		text.append(dialog.getVariableExpression());
	}

	/**
	 * Enable / Disable the given Composite.
	 * 
	 * @param compo
	 *            A <code>Composite</code> with some controls.
	 * @param value
	 *            <code>true</code> to enable, <code>false</code> to disable the given group.
	 */
	protected void setEnabled(Composite compo, boolean value) {
		Control[] children = compo.getChildren();
		for (Control child : children) {
			child.setEnabled(value);
		}
	}

}
