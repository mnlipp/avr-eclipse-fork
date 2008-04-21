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

import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.progress.UIJob;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.AVRDudeSchedulingRule;
import de.innot.avreclipse.core.avrdude.FuseBytes;
import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.fuses.FuseByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.Fuses;
import de.innot.avreclipse.ui.dialogs.AVRDudeErrorDialogJob;

/**
 * The AVRDude Fuses Tab page.
 * <p>
 * On this tab, the following properties are edited:
 * <ul>
 * <li>Upload of the Fuses</li>
 * </ul>
 * The Fuse byte values can either be entered directly, or a fuses file can be selected which
 * provides the fuse bytes.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class TabAVRDudeFuses extends AbstractAVRDudePropertyTab {

	/** Max number of Fuse bytes */
	private final static int		MAX_FUSES				= 3;

	/** The name of the fuses for 1, 2 or 3 fusebytes */
	private final static String[]	FUSENAMES				= { "Low", "High", "Ext." };

	// The GUI texts
	private final static String		GROUP_FUSES				= "Upload Fuse Bytes";
	private final static String		TEXT_FUSES_NOUPLOAD		= "do not set fuses";
	private final static String		TEXT_FUSES_FROMFILE		= "from fuses file";
	private final static String		TEXT_FUSES_IMMEDIATE	= "use hex values";

	private final static String		TEXT_READDEVICE			= "Load from MCU";
	private final static String		TEXT_READDEVICE_BUSY	= "Loading...";
	private final static String		TEXT_COPYFILE			= "Copy from file";

	// The GUI widgets
	private Button					fFusesNoUploadButton;

	private Button					fFusesUploadFileButton;
	private Text					fFusesFileText;
	private Button					fFusesWorkplaceButton;
	private Button					fFusesFilesystemButton;
	private Button					fFusesVariableButton;

	private Button					fFusesImmediateButton;
	private Composite				fFusesBytesCompo;
	private Button					fFusesReadButton;
	private Button					fFusesCopyButton;

	private final Composite[]		fFuseByteCompos			= new Composite[MAX_FUSES];
	private final Text[]			fFuseValueTexts			= new Text[MAX_FUSES];

	/** Number of fuse bytes for the current MCU */
	private int						fFuseCount				= 0;

	/** The Properties that this page works with */
	private AVRDudeProperties		fTargetProps;

	/** The file extensions for image files. Used by the file selector. */
	public final static String[]	IMAGE_EXTS				= new String[] { "*.fuses" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		addFusesSection(parent);

	}

	/**
	 * Add the fuses selection group.
	 * <p>
	 * This group has three sections (with radio buttons):
	 * <ul>
	 * <li>Do not write fusebytes</li>
	 * <li>Write fusebytes from a user selectable file</li>
	 * <li>Write the fusebytes given</li>
	 * </ul>
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>
	 */
	private void addFusesSection(Composite parent) {

		// Group Setup
		Group group = new Group(parent, SWT.NONE);
		group.setText(GROUP_FUSES);
		group.setLayout(new GridLayout(4, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		addFusesNoUploadSection(group);

		addSeparator(group);

		addFusesFromFileSection(group);

		addSeparator(group);

		addFusesImmediateSection(group);
	}

	/**
	 * The "No upload" Section.
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>
	 */
	private void addFusesNoUploadSection(Composite parent) {

		fFusesNoUploadButton = new Button(parent, SWT.RADIO);
		fFusesNoUploadButton.setText(TEXT_FUSES_NOUPLOAD);
		fFusesNoUploadButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 4, 1));
		fFusesNoUploadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Set the properties
				fTargetProps.setWriteFuses(false);

				// and disable the other contols
				enableFusesFileGroup(false);
				enableFusesByteGroup(false);

				updatePreview(fTargetProps);
			}
		});

	}

	/**
	 * The "Upload from file" Section.
	 * <p>
	 * Contains a Text control to enter a filename and three buttons to select the filename from the
	 * workplace, the filesystem or from a build variable.
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>
	 */
	private void addFusesFromFileSection(Composite parent) {

		fFusesUploadFileButton = new Button(parent, SWT.RADIO);
		fFusesUploadFileButton.setText(TEXT_FUSES_FROMFILE);
		fFusesUploadFileButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		fFusesUploadFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fTargetProps.setWriteFuses(true);
				fTargetProps.getFuseBytes().setUseFile(true);
				enableFusesFileGroup(true);
				enableFusesByteGroup(false);
				updatePreview(fTargetProps);
			}
		});

		fFusesFileText = new Text(parent, SWT.BORDER);
		fFusesFileText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));
		fFusesFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String newpath = fFusesFileText.getText();
				fTargetProps.getFuseBytes().setFusesFile(newpath);
				updatePreview(fTargetProps);
			}
		});

		// The three File Dialog Buttons (and a alignment/filler Label),
		// all wrapped in a composite.
		Composite compo = new Composite(parent, SWT.NONE);
		compo.setBackgroundMode(SWT.INHERIT_FORCE);
		compo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 4, 1));
		compo.setLayout(new GridLayout(4, false));

		Label dummy = new Label(compo, SWT.NONE); // Filler
		dummy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		fFusesWorkplaceButton = setupWorkplaceButton(compo, fFusesFileText);
		fFusesFilesystemButton = setupFilesystemButton(compo, fFusesFileText, IMAGE_EXTS);
		fFusesVariableButton = setupVariableButton(compo, fFusesFileText);

	}

	/**
	 * The "Upload from direct values" Section.
	 * <p>
	 * Contains controls to edit all fuse bytes directly and two buttons to read the fuse byte
	 * values from the programmer and to copy the values from the fuses file.
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>
	 */
	private void addFusesImmediateSection(Composite parent) {

		// add the radio button
		fFusesImmediateButton = new Button(parent, SWT.RADIO);
		fFusesImmediateButton.setText(TEXT_FUSES_IMMEDIATE);
		GridData buttonGD = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		// This is somewhat arbitrarily and looks good on my setup.
		// Your milage may vary.
		buttonGD.verticalIndent = 4;
		fFusesImmediateButton.setLayoutData(buttonGD);
		fFusesImmediateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fTargetProps.setWriteFuses(true);
				fTargetProps.getFuseBytes().setUseFile(false);
				enableFusesFileGroup(false);
				enableFusesByteGroup(true);
				updatePreview(fTargetProps);
			}
		});

		// add the byte editor composites (wrapped in a composite)
		fFusesBytesCompo = new Composite(parent, SWT.NONE);
		GridData bytesGD = new GridData(SWT.BEGINNING, SWT.FILL, false, false, 1, 1);

		// Make the size of the byte edit fields somewhat dependent on the font
		// size. I use 6 chars instead of the actual required 4, because 4 was
		// just to small.
		// Also a little vertical indent to get it aligned with the Button
		// behind it (this again works for me and YMMV.
		FontMetrics fm = getFontMetrics(parent);
		bytesGD.widthHint = Dialog.convertWidthInCharsToPixels(fm, 6) * 3;
		bytesGD.verticalIndent = 2;
		fFusesBytesCompo.setLayoutData(bytesGD);
		fFusesBytesCompo.setLayout(new FillLayout(SWT.HORIZONTAL));

		// Insert the byte editor compos
		for (int i = 0; i < MAX_FUSES; i++) {
			makeFuseByteEditComposite(fFusesBytesCompo, i);
		}

		// add the read button
		fFusesReadButton = new Button(parent, SWT.PUSH);
		fFusesReadButton.setText(TEXT_READDEVICE);
		fFusesReadButton.setBackground(parent.getBackground());
		GridData editbuttonGD = new GridData(SWT.FILL, SWT.TOP, false, false);
		fFusesReadButton.setLayoutData(editbuttonGD);
		fFusesReadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				readFuseBytesFromDevice();
			}
		});

		// add the copy button
		fFusesCopyButton = new Button(parent, SWT.PUSH);
		fFusesCopyButton.setText(TEXT_COPYFILE);
		fFusesCopyButton.setBackground(parent.getBackground());
		GridData copybuttonGD = new GridData(SWT.BEGINNING, SWT.TOP, true, false);
		fFusesCopyButton.setLayoutData(copybuttonGD);

	}

	/**
	 * Create an fuse byte "Editor" composite.
	 * <p>
	 * The editor consists of a Text control to enter the byte value and a Label below it with the
	 * name of the fusebyte.
	 * </p>
	 * <p>
	 * The Text control will only accept (up to) 2 hex digits (converted to uppercase). The value is
	 * stored as an <code>int</code> in the target properties, with <code>-1</code> representing
	 * an empty value.
	 * </p>
	 * <p>
	 * The Editor uses a <code>FillLayout</code> to pack both elements tightly. It is up to the
	 * caller to set the LayoutData for this composite
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>
	 * @param index
	 *            The fuse byte index for the editor.
	 * 
	 * @return <code>Composite</code> with the editor.
	 */
	private Composite makeFuseByteEditComposite(Composite parent, int index) {

		FillLayout layout = new FillLayout(SWT.VERTICAL);

		Composite compo = new Composite(parent, SWT.NONE);
		compo.setLayout(layout);
		fFuseByteCompos[index] = compo;

		// Add the Text control
		Text text = new Text(compo, SWT.BORDER | SWT.CENTER);
		text.setTextLimit(2);
		text.setSize(10, 20);
		text.setData(new Integer(index));

		// Add a modification listener to set the fuse byte
		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				// Get the Fuse byte number
				Text source = (Text) e.widget;
				int fusenumber = (Integer) source.getData();

				// Get the new value...
				int newvalue;
				if (source.getText().length() > 0) {
					newvalue = Integer.parseInt(source.getText(), 16);
				} else {
					// Text control is empty. Use the default
					newvalue = -1;
				}

				// ... and set the property
				fTargetProps.getFuseBytes().setFuseValue(fusenumber, newvalue);
				updatePreview(fTargetProps);
			}

		});

		// Add a verify listener to only accept hex digits and convert them to
		// upper case
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				String text = event.text.toUpperCase();
				if (!text.matches("[0-9A-F]*")) {
					event.doit = false;
				}
				event.text = text;
			}
		});

		// Add a focus listener to select the complete text when the control gets the focus
		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusGained(FocusEvent e) {
				Text source = (Text) e.widget;
				source.selectAll();
			}

		});

		// Store a reference to the Text control, so that the updateData()
		// method can update the content when required.
		fFuseValueTexts[index] = text;

		// Add the label
		Label label = new Label(compo, SWT.CENTER);
		label.setText(FUSENAMES[index]);
		label.setSize(10, 0);

		return compo;
	}

	/**
	 * Enable / Disable the Fuses file selector Controls
	 * 
	 * @param enabled
	 *            <code>true</code> to enable, <code>false</code> to disable.
	 */
	private void enableFusesFileGroup(boolean enabled) {
		fFusesFileText.setEnabled(enabled);
		fFusesWorkplaceButton.setEnabled(enabled);
		fFusesFilesystemButton.setEnabled(enabled);
		fFusesVariableButton.setEnabled(enabled);
	}

	/**
	 * Enable / Disable the Fuses Byte Editor Controls
	 * <p>
	 * When enabling, only those editors are enabled that are actually valid for the current MCU.
	 * </p>
	 * 
	 * @param enabled
	 *            <code>true</code> to enable, <code>false</code> to disable.
	 */
	private void enableFusesByteGroup(boolean enabled) {
		for (int i = 0; i < fFuseByteCompos.length; i++) {
			if (i >= fFuseCount) {
				setEnabled(fFuseByteCompos[i], false);
			} else {
				setEnabled(fFuseByteCompos[i], enabled);
			}
		}
		fFusesReadButton.setEnabled(enabled);
		fFusesCopyButton.setEnabled(enabled);
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
		dstprops.setWriteFuses(fTargetProps.getWriteFuses());

		// Copy the settings from the FuseBytes sub-properties
		FuseBytes src = fTargetProps.getFuseBytes();
		FuseBytes dst = dstprops.getFuseBytes();

		dst.setUseFile(src.getUseFile());
		dst.setFusesFile(src.getFusesFile());
		dst.setFuseValues(src.getFuseValuesFromObject());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performCopy(AVRDudeProperties srcprops) {

		// Copy the settings from the AVRDudeProperties
		fTargetProps.setWriteFuses(srcprops.getWriteFuses());

		// Copy the settings from the FuseBytes sub-properties
		FuseBytes src = srcprops.getFuseBytes();
		FuseBytes dst = fTargetProps.getFuseBytes();

		dst.setUseFile(src.getUseFile());
		dst.setFusesFile(src.getFusesFile());
		dst.setFuseValues(src.getFuseValuesFromObject());

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

		// Get the target MCU and its number of fusebytes
		String mcuid = props.getParent().getMCUId();
		fFuseCount = 0;
		try {
			fFuseCount = Fuses.getDefault().getFuseByteCount(mcuid);
		} catch (IOException e) {
			// Failed to get the fusebytecount, leave it a zero
		}

		// TODO: Test if Fusebytes are still valid and generate some warnings if
		// not.

		// Update the fuses group
		FuseBytes src = fTargetProps.getFuseBytes();

		// There are three possibilities:
		// a) No upload wanted: WriteFuses == false
		// b) Upload from file: WriteFuses == true && useFile == true
		// c) Upload from immediate: WriteFuses == true && useFile == false
		if (!fTargetProps.getWriteFuses()) {
			// a) No upload wanted
			fFusesNoUploadButton.setSelection(true);
			fFusesUploadFileButton.setSelection(false);
			fFusesImmediateButton.setSelection(false);
			enableFusesFileGroup(false);
			enableFusesByteGroup(false);
		} else {
			// write fuses
			fFusesNoUploadButton.setSelection(false);
			if (src.getUseFile()) {
				// b) write fuses - use supplied file
				fFusesUploadFileButton.setSelection(true);
				fFusesImmediateButton.setSelection(false);
				enableFusesFileGroup(true);
				enableFusesByteGroup(false);
			} else {
				// c) write flash - use immediate bytes
				fFusesUploadFileButton.setSelection(false);
				fFusesImmediateButton.setSelection(true);
				enableFusesFileGroup(false);
				enableFusesByteGroup(true);
			}
		}

		// Set the text for the filename
		fFusesFileText.setText(src.getFusesFile());

		// Set the immediate fuse values
		int[] values = src.getFuseValues();
		int count = Math.min(values.length, MAX_FUSES);

		for (int i = 0; i < count; i++) {
			String newvalue = "";
			int currvalue = values[i];
			if (0 <= currvalue && currvalue <= 255) {
				newvalue = Integer.toHexString(currvalue).toUpperCase();
			}
			fFuseValueTexts[i].setText(newvalue);
		}
	}

	/**
	 * Load the Fuse Bytes from the currently attached MCU.
	 * <p>
	 * This method will start a new Job to load the values and return immediately.
	 * </p>
	 */
	private void readFuseBytesFromDevice() {
		// Disable the Load Button. It is re-enabled by the load job when it finishes.
		fFusesReadButton.setEnabled(false);
		fFusesReadButton.setText(TEXT_READDEVICE_BUSY);

		// The Job that does the actual loading.
		Job readJob = new Job("Reading Fuse Bytes") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				try {
					monitor.beginTask("Starting AVRDude", 100);

					final FuseByteValues fusebytevalues = AVRDude.getDefault().getFuseBytes(
							fTargetProps.getProgrammer());

					monitor.worked(95);

					// and update the user interface
					if (!fFusesReadButton.isDisposed()) {
						fFusesReadButton.getDisplay().syncExec(new Runnable() {
							public void run() {
								// Check if the mcus match
								String projectmcu = fTargetProps.getParent().getMCUId();
								String newmcu = fusebytevalues.getMCUId();
								if (!projectmcu.equals(newmcu)) {
									// No, they don't match. Ask the user what to do
									// "Accept anyway" or "Cancel"
									Dialog dialog = new MCUMismatchDialog(fFusesReadButton
											.getShell(), newmcu, projectmcu);
									int choice = dialog.open();
									if (choice == 1) {
										return;
									}
								}
								// Clear the current fusebytes and transfer the new values
								fTargetProps.getFuseBytes().setFuseValues(FuseBytes.EMPTY_FUSES);
								fTargetProps.getFuseBytes().setFuseValues(
										fusebytevalues.getValues());

								updateData(fTargetProps);
							}
						});
					}
					monitor.worked(5);
				} catch (AVRDudeException ade) {
					// Show an Error message and exit
					if (!fFusesReadButton.isDisposed()) {
						UIJob messagejob = new AVRDudeErrorDialogJob(fFusesReadButton.getDisplay(),
								ade, fTargetProps.getProgrammerId());
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
					// Enable the Load from MCU Button
					if (!fFusesReadButton.isDisposed()) {
						fFusesReadButton.getDisplay().syncExec(new Runnable() {
							public void run() {
								// Re-Enable the Button
								fFusesReadButton.setEnabled(true);
								fFusesReadButton.setText(TEXT_READDEVICE);
							}
						});
					}
				}

				return Status.OK_STATUS;
			}
		};

		// now set the Job properties and start it
		readJob.setRule(new AVRDudeSchedulingRule(fTargetProps.getProgrammer()));
		readJob.setPriority(Job.SHORT);
		readJob.setUser(true);
		readJob.schedule();
	}

	/**
	 * Enable / Disable a Composite and all of its children.
	 * 
	 * @param compo
	 *            A <code>Composite</code> with some controls.
	 * @param enabled
	 *            <code>true</code> to enable, <code>false</code> to disable the given group.
	 */
	private void setEnabled(Composite compo, boolean enabled) {
		compo.setEnabled(enabled);
		Control[] children = compo.getChildren();
		for (Control child : children) {
			child.setEnabled(enabled);
		}
	}

	/**
	 * An small Warning dialog that will be shown when the MCU for the fusebytes does not match the
	 * current Project / Configuration MCU.
	 * <p>
	 * In addition to a fixed warning message, this dialog sports two buttons to accept the fuse
	 * values, even if they don't match, or to cancel the changes.
	 * </p>
	 * <p>
	 * The open method of this dialog will returns two values
	 * <ul>
	 * <li><code>0</code> Accept button pressed.</li>
	 * <li><code>1</code> Cancel button pressed.</li>
	 * </ul>
	 * </p>
	 */
	private class MCUMismatchDialog extends MessageDialog {

		/**
		 * Create a new Dialog.
		 * <p>
		 * The dialog will not be shown until the <code>open()</code> method has been called.
		 * </p>
		 * 
		 * @param shell
		 *            Parent <code>Shell</code>
		 * @param newmcu
		 *            The MCU id for the fuse bytes.
		 * @param projectmcu
		 *            The MCU id for the project or build configuration.
		 */
		public MCUMismatchDialog(Shell shell, String newmcu, String projectmcu) {

			super(shell, "AVRDude Fuses Warning", null, "", WARNING, new String[] { "Accept",
					"Cancel" }, 0);

			String proptype = isPerConfig() ? "build configuration" : "project";

			String source = "The fuse bytes values are valid for an {0} MCU.\n"
					+ "This MCU is not compatible with the current {2} MCU [{1}].\n\n"
					+ "\"Accept\" to accept the new values anyway.\n"
					+ "\"Cancel\" to discard the new values.";

			this.message = MessageFormat.format(source, newmcu, projectmcu, proptype);
		}
	}

}
