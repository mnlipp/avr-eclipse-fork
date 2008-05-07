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
package de.innot.avreclipse.ui.propertypages;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.AVRDudeSchedulingRule;
import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.ui.dialogs.AVRDudeErrorDialogJob;

/**
 * The AVRDude Actions Tab page.
 * <p>
 * On this tab, the following properties are edited:
 * <ul>
 * <li>The automatic verify check</li>
 * <li>The Signature check</li>
 * <li>Enable the no-Write / Simulation mode</li>
 * <li>Enable Flash memory erase cycle counter, incl. the non-property related options to load /
 * write the current cycle counter value to / from a connected device</li>
 * </ul>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class TabAVRDudeAdvanced extends AbstractAVRDudePropertyTab {

	// The GUI texts

	// No verify group
	private final static String	GROUP_NOVERIFY			= "Verify Check";
	private final static String	LABEL_NOVERIFY			= "Disabling the automatic verify check will improve upload time at the risk of unnoticed upload errors.";
	private final static String	TEXT_NOVERIFY			= "Disable automatic verify check";

	// No Signature check group
	private final static String	GROUP_NOSIGCHECK		= "Device Signature Check";
	private final static String	LABEL_NOSIGCHECK		= "Enable this if the target MCU has a broken (erased or overwritten) device signature\n"
																+ "but is otherwise operating normally.";
	private final static String	TEXT_NOSIGCHECK			= "Disable device signature check";

	// No write / simulation group
	private final static String	GROUP_NOWRITE			= "Simulation Mode";
	private final static String	LABEL_NOWRITE			= "Note: Even with this option set, AVRDude might still perform a chip erase.";
	private final static String	TEXT_NOWRITE			= "Simulation mode (no data is actually written to the device)";

	// Flash erase cycle counter check
	private final static String	GROUP_COUNTER			= "Flash memory erase cycle counter";
	private final static String	LABEL_COUNTER			= "Enable this to have avrdude count the number of flash erase cycles.\n"
																+ "Note: the value is stored in the last four bytes of the EEPROM,\n"
																+ "so do not enable this if your application needs the last four bytes of the EEPROM.";
	private final static String	TEXT_COUNTER			= "Enable erase cycle counter";
	private final static String	TEXT_READBUTTON			= "Load from MCU";
	private final static String	TEXT_READBUTTON_BUSY	= "Loading...";
	private final static String	TEXT_WRITEBUTTON		= "Write to MCU";
	private final static String	TEXT_WRITEBUTTON_BUSY	= "Writing...";

	// The GUI widgets
	private Button				fNoVerifyButton;

	private Button				fNoSigCheckButton;

	private Button				fNoWriteCheck;

	private Button				fCounterCheck;
	private Composite			fCounterOptionsComposite;
	private Text				fCounterValue;
	private Button				fReadButton;
	private Button				fWriteButton;

	/** The Properties that this page works with */
	private AVRDudeProperties	fTargetProps;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		addNoVerifySection(parent);

		addNoSignatureSection(parent);

		addNoWriteSection(parent);

		addUseCycleCounterSection(parent);

	}

	/**
	 * Add the No Verify check button.
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	private void addNoVerifySection(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout(1, false));
		group.setText(GROUP_NOVERIFY);

		Label label = new Label(group, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		label.setText(LABEL_NOVERIFY);
		fNoVerifyButton = setupCheck(group, TEXT_NOVERIFY, 1, SWT.FILL);
	}

	/**
	 * Add the No Signature Check check button.
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	private void addNoSignatureSection(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout(1, false));
		group.setText(GROUP_NOSIGCHECK);

		Label label = new Label(group, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		label.setText(LABEL_NOSIGCHECK);
		fNoSigCheckButton = setupCheck(group, TEXT_NOSIGCHECK, 1, SWT.FILL);
	}

	/**
	 * Add the No Write / Simulate check button.
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	private void addNoWriteSection(Composite parent) {

		Group group = setupGroup(parent, GROUP_NOWRITE, 1, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		setupLabel(group, LABEL_NOWRITE, 1, SWT.NONE);
		fNoWriteCheck = setupCheck(group, TEXT_NOWRITE, 1, SWT.CHECK);
	}

	/**
	 * Add the GUI section for the Flash memory cycle counter settings.
	 * <p>
	 * This section contains not only the actual checkbox, but also some controls to read/write the
	 * current cycle counter. These controls are not directly related to the properties, but it
	 * seems like a good idea to put them here.
	 * </p>
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	private void addUseCycleCounterSection(Composite parent) {

		Group countergroup = setupGroup(parent, GROUP_COUNTER, 1, SWT.NONE);
		countergroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		setupLabel(countergroup, LABEL_COUNTER, 2, SWT.NONE);

		fCounterCheck = setupCheck(countergroup, TEXT_COUNTER, 2, SWT.NONE);

		// Cycle Counter load / write control composite

		fCounterOptionsComposite = new Composite(countergroup, SWT.NONE);
		fCounterOptionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		fCounterOptionsComposite.setLayout(new GridLayout(4, false));

		setupLabel(fCounterOptionsComposite, "Current Erase Cycle Counter", 1, SWT.NONE);

		fCounterValue = new Text(fCounterOptionsComposite, SWT.BORDER | SWT.RIGHT);
		fCounterValue.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		fCounterValue.setTextLimit(5); // Max. 65535
		fCounterValue.addVerifyListener(new VerifyListener() {
			// only allow digits as cycle counter values
			public void verifyText(VerifyEvent event) {
				String text = event.text;
				if (!text.matches("[0-9]*")) {
					event.doit = false;
				}
			}
		});

		// Read Button
		fReadButton = setupButton(fCounterOptionsComposite, TEXT_READBUTTON, 1, SWT.NONE);
		fReadButton.setBackground(parent.getBackground());
		fReadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				readCycleCounter();
			}
		});

		// Write Button
		fWriteButton = setupButton(fCounterOptionsComposite, TEXT_WRITEBUTTON, 1, SWT.NONE);
		fWriteButton.setBackground(parent.getBackground());
		fWriteButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				writeCycleCounter();
			}
		});

		// Disable by default. The updateData() method will enable/disable
		// according to the actual property settings.
		setEnabled(fCounterOptionsComposite, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#checkPressed(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	protected void checkPressed(SelectionEvent e) {
		// This is called for all checkbuttons / tributtons which have been set
		// up with the setupXXX() calls

		Control source = (Control) e.widget;

		if (source.equals(fNoVerifyButton)) {
			// No Verify checkbox selected
			boolean noverify = fNoVerifyButton.getSelection();
			fTargetProps.setNoVerify(noverify);

		} else if (source.equals(fNoSigCheckButton)) {
			// No Signature checkbox selected
			boolean nosigcheck = fNoSigCheckButton.getSelection();
			fTargetProps.setNoSigCheck(nosigcheck);

		} else if (source.equals(fNoWriteCheck)) {
			// No Write = Simulation Checkbox has been selected
			// Write the new value to the target properties
			boolean newvalue = fNoWriteCheck.getSelection();
			fTargetProps.setNoWrite(newvalue);

		} else if (source.equals(fCounterCheck)) {
			// Use Cycle Counter Checkbox has been selected
			// Write the new value to the target properties
			// and enable / disable the Counter Options composite accordingly
			boolean newvalue = fCounterCheck.getSelection();
			fTargetProps.setUseCounter(newvalue);
			setEnabled(fCounterOptionsComposite, newvalue);
		}

		updatePreview(fTargetProps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performApply(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performApply(AVRDudeProperties dstprops) {

		if (fTargetProps == null) {
			// updataData() has not been called and this tab has no (modified)
			// settings yet.
			return;
		}

		// Copy the currently selected values of this tab to the given, fresh
		// Properties.
		// The caller of this method will handle the actual saving
		dstprops.setNoVerify(fTargetProps.getNoVerify());
		dstprops.setNoSigCheck(fTargetProps.getNoSigCheck());
		dstprops.setNoWrite(fTargetProps.getNoWrite());
		dstprops.setUseCounter(fTargetProps.getUseCounter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performCopy(AVRDudeProperties srcprops) {

		// Reload the items on this page
		fTargetProps.setNoVerify(srcprops.getNoVerify());
		fTargetProps.setNoSigCheck(srcprops.getNoSigCheck());
		fTargetProps.setNoWrite(srcprops.getNoWrite());
		fTargetProps.setUseCounter(srcprops.getUseCounter());
		updateData(fTargetProps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#updateData(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void updateData(AVRDudeProperties props) {

		fTargetProps = props;

		// Update the GUI widgets on this Tab.
		fNoVerifyButton.setSelection(fTargetProps.getNoVerify());
		fNoSigCheckButton.setSelection(fTargetProps.getNoSigCheck());
		fNoWriteCheck.setSelection(fTargetProps.getNoWrite());

		boolean usecounter = fTargetProps.getUseCounter();
		fCounterCheck.setSelection(usecounter);
		setEnabled(fCounterOptionsComposite, usecounter);

	}

	/**
	 * Read the current cycles value from the currently attached device.
	 * <p>
	 * The actual read is done in a separate Thread to reduce the impact on the GUI.
	 * </p>
	 */
	private void readCycleCounter() {

		// Disable the Load Button. It is re-enabled by the load job when it finishes.
		fReadButton.setEnabled(false);
		fReadButton.setText(TEXT_READBUTTON_BUSY);

		// The Job that does the actual loading.
		Job readJob = new CycleJob(fReadButton, false, 0, TEXT_READBUTTON);

		// now set the Job properties and start it
		readJob.setRule(new AVRDudeSchedulingRule(fTargetProps.getProgrammer()));
		readJob.setPriority(Job.SHORT);
		readJob.setUser(true);
		readJob.schedule();
	}

	/**
	 * Write the selected cycles value to the currently attached device.
	 * <p>
	 * The actual load is done in a separate Thread to reduce the impact on the GUI.
	 * </p>
	 */
	private void writeCycleCounter() {

		// Disable the Load Button. It is re-enabled by the load job when it finishes.
		fWriteButton.setEnabled(false);
		fWriteButton.setText(TEXT_WRITEBUTTON_BUSY);

		int newvalue = Integer.parseInt(fCounterValue.getText());

		// 65534 is the largest value accepted (65535 (0xffff) is interpreted by avrdude as an unset
		// cycle counter).
		if (newvalue >= 0xfffe) {
			newvalue = 0xfffe;
		}

		// The Job that does the actual loading.
		Job readJob = new CycleJob(fWriteButton, true, newvalue, TEXT_WRITEBUTTON);

		// now set the Job properties and start it
		readJob.setRule(new AVRDudeSchedulingRule(fTargetProps.getProgrammer()));
		readJob.setPriority(Job.SHORT);
		readJob.setUser(true);
		readJob.schedule();
	}

	/**
	 * Private class to read or write the erase cycle counter.
	 */
	private class CycleJob extends Job {

		private final Button	fButton;
		private final String	fButtonText;

		private final boolean	fWrite;
		private final int		fNewCounterValue;

		/**
		 * @param button
		 *            The button that was selected. Required to re-enable the button once the job
		 *            has finished.
		 * @param write
		 *            <code>true</code> if the <code>newcounter</code> value will be written to
		 *            the device. if <code>false</code> only the current MCU counter is read.
		 * @param newcounter
		 *            New erase cycle counter value (ignored if <code>write</code> is
		 *            <code>false</code>.
		 * @param buttontext
		 *            The normal text for the source button. Required to restore the text once the
		 *            job has finished.
		 */
		public CycleJob(Button button, boolean write, int newcounter, String buttontext) {
			super("Accessing Erase Cycle Counter");
			fButton = button;
			fWrite = write;
			fNewCounterValue = newcounter;
			fButtonText = buttontext;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			try {
				monitor.beginTask("Starting AVRDude", 100);

				final int loadedvalue;

				if (fWrite) {
					loadedvalue = AVRDude.getDefault().setEraseCycleCounter(
							fTargetProps.getProgrammer(), fNewCounterValue);
				} else {
					loadedvalue = AVRDude.getDefault().getEraseCycleCounter(
							fTargetProps.getProgrammer());
				}

				monitor.worked(95);

				// and update the user interface
				if (!fButton.isDisposed()) {
					fButton.getDisplay().syncExec(new Runnable() {
						public void run() {
							if (loadedvalue >= 0) {
								fCounterValue.setText(Integer.toString(loadedvalue));
							} else
								fCounterValue.setText("0");
						}
					});
				}
				monitor.worked(5);
			} catch (AVRDudeException ade) {
				// Show an Error message and exit
				if (!fButton.isDisposed()) {
					UIJob messagejob = new AVRDudeErrorDialogJob(fButton.getDisplay(), ade,
							fTargetProps.getProgrammerId());
					messagejob.setPriority(Job.INTERACTIVE);
					messagejob.schedule();
					try {
						messagejob.join(); // block until the dialog is closed.
					} catch (InterruptedException e) {
						// Don't care if the dialog is interrupted from outside.
					}
				}
			} catch (SWTException swte) {
				// The display has been disposed, so the user is not
				// interested in the results from this job
				return Status.CANCEL_STATUS;
			} finally {
				monitor.done();
				// Re-Enable the Button
				if (!fButton.isDisposed()) {
					fButton.getDisplay().syncExec(new Runnable() {
						public void run() {
							// Re-Enable the Button
							fButton.setEnabled(true);
							fButton.setText(fButtonText);
						}
					});
				}
			}

			return Status.OK_STATUS;
		}
	};
}
