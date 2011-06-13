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
package de.innot.avreclipse.debug.gdbservers;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

import de.innot.avreclipse.debug.ui.IGDBServerSettingsContext;
import de.innot.avreclipse.debug.ui.IGDBServerSettingsPage;

/**
 * Base implementation of the {@link IGDBServerSettingsPage} interface.
 * <p>
 * GDBServer providers can extends this class, which contains default implementations for some
 * methods as well as helper methods common to most subclasses.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public abstract class AbstractGDBServerSettingsPage implements IGDBServerSettingsPage {

	/** The ID of the gdbserver for which this page manages the settings. */
	private String						fGDBServerID;

	/** The parent context. */
	private IGDBServerSettingsContext	fParent;

	/** The descriptive name of the launch configuration. */
	private String						fDescription;

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#getName()
	 */
	public final String getDescription() {
		return fDescription;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#setDescription(java.lang.String)
	 */
	public final void setDescription(String description) {
		fDescription = description;
	}

	/**
	 * Gets the name of the gdbserver command for this page for the user interface.
	 * 
	 * @return
	 */
	protected abstract String getGDBServerName();

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#getGDBServerID()
	 */
	public final String getGDBServerID() {
		return fGDBServerID;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#setGDBServerID(java.lang.String)
	 */
	public final void setGDBServerID(String gdbserverid) {
		fGDBServerID = gdbserverid;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#setDefaults(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#setContext(de.innot.avreclipse.debug.
	 * ui.IGDBServerSettingsContext)
	 */
	public void setContext(IGDBServerSettingsContext parent) {
		fParent = parent;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#initializeFrom(org.eclipse.debug.core
	 * .ILaunchConfiguration)
	 */
	public abstract void initializeFrom(ILaunchConfiguration configuration);

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#createSettingsPage(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public abstract void createSettingsPage(Composite parent);

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#performApply(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public abstract void performApply(ILaunchConfigurationWorkingCopy configuration);

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#isValid(org.eclipse.debug.core.
	 * ILaunchConfiguration)
	 */
	abstract public boolean isValid(ILaunchConfiguration configuration);

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsPage#dispose()
	 */
	public void dispose() {
		// Nothing to dispose.
	}

	/**
	 * 
	 */
	protected void updatePage() {
		fParent.updateDialog();
	}

	/**
	 * Add a standard command name selection group to the parent composite.
	 * <p>
	 * The created <code>Text</code> control has a default ModifyListener that will call
	 * {@link #updatePage()} for each modification.
	 * </p>
	 * <p>
	 * A warning is shown in the user interface if the field is empty. However it is up to the
	 * subclass to set the valid flag in the {@link #isValid(ILaunchConfiguration)} method.
	 * </p>
	 * 
	 * @param parent
	 *            The parent composite to insert into. Must have an GridLayout with at least four
	 *            columns
	 * 
	 * @return the <code>Text</code> control.
	 */
	protected Text createCommandField(Composite parent) {

		int columns = getColumns(parent, 4);

		Label label = new Label(parent, SWT.NONE);
		label.setText(getGDBServerName() + " command: ");
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false, 1, 1));

		final Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, columns - 3, 1));
		text.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				// the text field may not be empty.
				if (e.widget instanceof Text) {
					String command = ((Text) e.widget).getText();
					if (command.length() == 0) {
						setErrorMessage(getGDBServerName() + " command may not be empty.");
					} else {
						setErrorMessage(null);
					}
					// TODO: could add a check if the selected command name is executable

					updatePage();
				}
			}
		});

		Button button = new Button(parent, SWT.NONE);
		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browseButtonAction("Select " + getGDBServerName() + " executable", text);
			}
		});

		button = new Button(parent, SWT.NONE);
		button.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		button.setText("Variables...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				variablesButtonAction(text);
			}
		});

		return text;
	}

	/**
	 * Create an standard "Hostname or IP address" text control with an appropriate label.
	 * <p>
	 * The created <code>Text</code> control has a default ModifyListener that will call
	 * {@link #updatePage()} for each modification.
	 * </p>
	 * <p>
	 * A warning is shown in the user interface if the field is empty. However it is up to the
	 * subclass to set the valid flag in the {@link #isValid(ILaunchConfiguration)} method.
	 * </p>
	 * 
	 * @param parent
	 *            The parent composite to insert into. Must have an GridLayout with at least two
	 *            columns
	 * 
	 * @return the <code>Text</code> control.
	 */
	protected Text createHostnameField(Composite parent) {

		int columns = getColumns(parent, 2);

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Remote hostname or ip address:");

		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, columns - 1, 1));
		text.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				// the text field may not be empty.
				if (e.widget instanceof Text) {
					String command = ((Text) e.widget).getText();
					if (command.length() == 0) {
						setErrorMessage("Hostname may not be empty.");
					} else {
						setErrorMessage(null);
					}
					// TODO: could add a check if the selected hostname is reachable

					updatePage();
				}
			}
		});

		return text;
	}

	/**
	 * Create an standard "Port number" text control with an appropriate label.
	 * <p>
	 * The created <code>Text</code> control has a default ModifyListener that will call
	 * {@link #updatePage()} for each modification. Also a VerifyListener is added that will accept
	 * only digits.
	 * </p>
	 * <p>
	 * A warning is shown in the user interface if the field is empty or its value out of range (0
	 * to 65535). However it is up to the subclass to set the valid flag in the
	 * {@link #isValid(ILaunchConfiguration)} method.
	 * </p>
	 * 
	 * @param parent
	 *            The parent composite to insert into. Must have an GridLayout with at least two
	 *            columns
	 * 
	 * @return the <code>Text</code> control.
	 */
	protected Text createPortField(Composite parent) {

		int columns = getColumns(parent, 2);

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Port number:");

		Text text = new Text(parent, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, true, false);
		gd.widthHint = 80;
		text.setLayoutData(gd);
		text.setTextLimit(5);

		text.addVerifyListener(new VerifyListener() {
			// allow only digits
			public void verifyText(VerifyEvent ve) {
				String text = ve.text;
				if (!text.matches("[0-9]*")) {
					ve.doit = false;
				}
			}
		});

		text.addModifyListener(new ModifyListener() {
			// Check that the port number is between 0 and 65.535
			public void modifyText(ModifyEvent e) {
				String porttext = ((Text) e.widget).getText();
				if (porttext.length() == 0) {
					setErrorMessage("Port number must not be empty");
				} else {
					int newport = Integer.decode(porttext.trim());
					if (newport < 0 || newport > 0xffff) {
						setErrorMessage("Invalid port number. Must be between 0 and 65535");
					} else {
						setErrorMessage(null);
					}
				}
				updatePage();

			}
		});

		if (columns > 2) {
			// fill remaining columns with a dummy label
			label = new Label(parent, SWT.NONE);
			label.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, columns - 2, 1));
		}

		return text;
	}

	/**
	 * Create an standard "Other options" text control with an appropriate label.
	 * <p>
	 * The created <code>Text</code> control has a default ModifyListener that will call
	 * {@link #updatePage()} for each modification.
	 * </p>
	 * 
	 * @param parent
	 *            The parent composite to insert into. Must have an GridLayout with at least two
	 *            columns
	 * 
	 * @return the <code>Text</code> control.
	 */
	protected Text createOtherOptionsField(Composite parent) {

		int columns = getColumns(parent, 2);

		Label label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.BEGINNING, SWT.CENTER, false, false));
		label.setText("Other " + getGDBServerName() + " options:");

		Text text = new Text(parent, SWT.BORDER | SWT.SINGLE);
		text.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, columns - 1, 1));
		text.setToolTipText("Add other " + getGDBServerName() + " command line options");
		text.addModifyListener(new ModifyListener() {
			/*
			 * (non-Javadoc)
			 * @see
			 * org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
			 */
			public void modifyText(ModifyEvent e) {
				updatePage();
			}
		});

		return text;
	}

	/**
	 * Set the error message of the parent dialog.
	 * <p>
	 * The error message is not shown immediately. The {@link #updatePage()} method needs to be
	 * called afterward to show the error message to the user.
	 * </p>
	 * 
	 * @param message
	 *            error message or <code>null</code> to clear the message.
	 */
	protected void setErrorMessage(String message) {
		fParent.setErrorMessage(getGDBServerID(), message);
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
		FileDialog dialog = new FileDialog(getParentControl().getShell(), SWT.NONE);
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
		StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getParentControl()
				.getShell());
		dialog.open();
		text.append(dialog.getVariableExpression());
	}

	/**
	 * Convenience method to get the control of the parent dialog.
	 * <p>
	 * The returned control should only be used to get the parent <code>Shell</code> or
	 * <code>Display</code>.
	 * </p>
	 * 
	 * @return <code>Control</code> of the parent dialog
	 */
	private Control getParentControl() {
		return fParent.getControl();
	}

	/**
	 * Checks that the given <code>Composite</code> has a <code>GridLayout</code> with at least
	 * <code>minColumns</code> columns.
	 * <p>
	 * If the parent layout is not an <code>GridLayout</code> or has less than the required number
	 * of columns, then an unchecked Exception is thrown to indicate a bug in the plugin.
	 * </p>
	 * 
	 * @param composite
	 *            Composite to check
	 * @param minColumns
	 *            minimum required number of columns.
	 * @return the actual number of columns
	 */
	private int getColumns(Composite composite, int minColumns) {

		Layout parentlayout = composite.getLayout();

		Assert.isTrue(parentlayout instanceof GridLayout);

		int columns = ((GridLayout) parentlayout).numColumns;

		Assert.isTrue(columns >= minColumns);

		return columns;
	}

}
