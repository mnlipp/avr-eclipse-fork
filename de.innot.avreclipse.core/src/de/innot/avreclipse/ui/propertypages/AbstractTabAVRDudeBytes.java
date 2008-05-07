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
import org.eclipse.swt.graphics.Image;
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
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.UIJob;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.AVRDudeSchedulingRule;
import de.innot.avreclipse.core.avrdude.AbstractBytes;
import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.ui.dialogs.AVRDudeErrorDialogJob;

/**
 * The base AVRDude Tab page for Fuses and Lockbits.
 * <p>
 * The GUI for Fuse bytes and for Lockbits is the same and is handled in this class. The subclasses
 * just supply a few basic informations, but do not need to do any user interface handling.
 * </p>
 * <p>
 * Subclasses of this tab have three radio buttons:
 * <ul>
 * <li>Do not upload anything</li>
 * <li>Upload the byte values defined in a file</li>
 * <li>Upload some immediate byte values</li>
 * </ul>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public abstract class AbstractTabAVRDudeBytes extends AbstractAVRDudePropertyTab {

	private final static int	LABEL_GROUPNAME			= 0;
	private final static int	LABEL_NAME				= 1;

	// The GUI texts
	private final static String	GROUP_NAME				= "Upload {0}";
	private final static String	TEXT_NOUPLOAD			= "do not set {0}";
	private final static String	TEXT_FROMFILE			= "from {0} file";
	private final static String	TEXT_IMMEDIATE			= "direct hex value{0}";

	private final static String	TEXT_READDEVICE			= "Load from MCU";
	private final static String	TEXT_READDEVICE_BUSY	= "Loading...";
	private final static String	TEXT_COPYFILE			= "Copy from file";

	private final static String	WARN_BYTESINCOMPATIBLE	= "These values are for an {0} MCU. This is not compatible with the {2} MCU setting [{1}].";

	private static final Image	IMG_WARN				= PlatformUI
																.getWorkbench()
																.getSharedImages()
																.getImage(
																		ISharedImages.IMG_OBJS_WARN_TSK);

	// The GUI widgets
	private Button				fNoUploadButton;

	private Button				fUploadFileButton;
	private Text				fFileText;
	private Button				fWorkplaceButton;
	private Button				fFilesystemButton;
	private Button				fVariableButton;
	private Composite			fFileWarning;

	private Button				fImmediateButton;
	private Composite			fBytesCompo;
	private Button				fReadButton;
	private Button				fCopyButton;
	private Composite			fBytesWarning;

	private Composite[]			fByteCompos;
	private Text[]				fValueTexts;

	/** Number of bytes for the current MCU handled on the page */
	private int					fCount					= 0;

	/** The Properties that this page works with */
	private AVRDudeProperties	fTargetProps;

	/** The AbstractBytes property object this page works with */
	private AbstractBytes		fBytes;

	// The abstract hook methods for the subclasses

	/**
	 * Get an array of label strings.
	 * <p>
	 * Currently the returned array must contain two Strings. The first entry is used for the group
	 * label ("Upload {0}") and the second entry is used in multiple places like ("from {0} file").
	 * </p>
	 * 
	 * @return Array of <code>String</code>s with label strings.
	 */
	protected abstract String[] getLabels();

	/**
	 * Get the maximum number of byte value editor fields to generate.
	 * 
	 * @return 3 for the fuses page and 1 for the lockbits page.
	 */
	protected abstract int getMaxBytes();

	/**
	 * Get the actual number of bytes for the given MCU.
	 * 
	 * @param mcuid
	 *            Target MCU id value.
	 * @return 0-3 for the fuse page and 1 for the lockbits page.
	 */
	protected abstract int getByteCount(String mcuid);

	/**
	 * Load the ByteValues from the target MCU.
	 * 
	 * @param avrdudeprops
	 *            The current properties, including the ProgrammerConfig needed by avrdude.
	 * @return A <code>ByteValues</code> object with the bytes read from the MCU.
	 * @throws AVRDudeException
	 *             for any Exception thrown by avrdude
	 */
	protected abstract ByteValues getByteValues(AVRDudeProperties avrdudeprops)
			throws AVRDudeException;

	/**
	 * Get the Label text for the n-th byte.
	 * 
	 * @param index
	 *            0-2 for fuses, 0 for lockbits
	 * @return <code>String</code> with the name of the byte at the index.
	 */
	protected abstract String getByteEditorLabel(int index);

	/**
	 * Get an array with file extensions.
	 * <p>
	 * This list is used by the "from FileSystem" file dialog to show only files with the
	 * appropriate extension.
	 * 
	 * @return Array of <code>String</code>s with file extensions like ".fuses".
	 */
	protected abstract String[] getFileExtensions();

	/**
	 * Get the actual Byte properties this tab works with.
	 * 
	 * @param avrdudeprops
	 *            Source properties
	 * @return <code>FuseBytes</code> or <code>LockbitBytes</code> object extracted from the
	 *         given <code>AVRDudeProperties</code>
	 */
	protected abstract AbstractBytes getByteProps(AVRDudeProperties avrdudeprops);

	// The GUI stuff

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {

		// init the arrays
		int maxbytes = getMaxBytes();
		fByteCompos = new Composite[maxbytes];
		fValueTexts = new Text[maxbytes];

		parent.setLayout(new GridLayout(1, false));

		addMainSection(parent);

	}

	/**
	 * Add the main selection group.
	 * <p>
	 * This group has three sections (with radio buttons):
	 * <ul>
	 * <li>Do not write bytes</li>
	 * <li>Write bytes from a user selectable file</li>
	 * <li>Write the bytes given</li>
	 * </ul>
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>
	 */
	private void addMainSection(Composite parent) {

		// Group Setup
		Group group = new Group(parent, SWT.NONE);
		group.setText(MessageFormat.format(GROUP_NAME, getLabels()[LABEL_GROUPNAME]));
		group.setLayout(new GridLayout(4, false));
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		addNoUploadSection(group);

		// addSeparator(group);

		addFromFileSection(group);

		// addSeparator(group);

		addImmediateSection(group);
	}

	/**
	 * The "No upload" Section.
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>
	 */
	private void addNoUploadSection(Composite parent) {

		fNoUploadButton = new Button(parent, SWT.RADIO);
		fNoUploadButton.setText(MessageFormat.format(TEXT_NOUPLOAD, getLabels()[LABEL_NAME]));
		fNoUploadButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.FILL, false, false, 1, 1));
		fNoUploadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Set the properties
				fBytes.setWrite(false);

				// and disable the other controls
				enableFileGroup(false);
				enableByteGroup(false);

				updatePreview(fTargetProps);
			}
		});

		// Dummy to fill up the next 3 columns of the gridlayout
		Label dummy = new Label(parent, SWT.NONE);
		dummy.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));

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
	private void addFromFileSection(Composite parent) {

		// TODO remove this once files are supported.
		if (true)
			return;

		fUploadFileButton = new Button(parent, SWT.RADIO);
		fUploadFileButton.setText(MessageFormat.format(TEXT_FROMFILE, getLabels()[LABEL_NAME]));
		fUploadFileButton.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1));
		fUploadFileButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fBytes.setWrite(true);
				fBytes.setUseFile(true);
				enableFileGroup(true);
				enableByteGroup(false);
				updatePreview(fTargetProps);
			}
		});

		fFileText = new Text(parent, SWT.BORDER);
		fFileText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 3, 1));
		fFileText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String newpath = fFileText.getText();
				fBytes.setFileName(newpath);
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

		fWorkplaceButton = setupWorkplaceButton(compo, fFileText);
		fFilesystemButton = setupFilesystemButton(compo, fFileText, getFileExtensions());
		fVariableButton = setupVariableButton(compo, fFileText);

	}

	/**
	 * The "Upload from direct values" Section.
	 * <p>
	 * Contains controls to edit all bytes directly and two buttons to read the byte values from the
	 * programmer and to copy the values from the file.
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>
	 */
	private void addImmediateSection(Composite parent) {

		// add the radio button
		fImmediateButton = new Button(parent, SWT.RADIO);
		fImmediateButton
				.setText(MessageFormat.format(TEXT_IMMEDIATE, getMaxBytes() > 1 ? "s" : ""));
		GridData buttonGD = new GridData(SWT.FILL, SWT.TOP, false, false, 1, 1);
		// This is somewhat arbitrarily and looks good on my setup.
		// Your milage may vary.
		buttonGD.verticalIndent = 4;
		fImmediateButton.setLayoutData(buttonGD);
		fImmediateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fBytes.setWrite(true);
				fBytes.setUseFile(false);
				enableFileGroup(false);
				enableByteGroup(true);
				updatePreview(fTargetProps);
			}
		});

		// add the byte editor composites (wrapped in a composite)
		fBytesCompo = new Composite(parent, SWT.NONE);
		GridData bytesGD = new GridData(SWT.BEGINNING, SWT.FILL, false, false, 1, 1);

		// Make the size of the byte edit fields somewhat dependent on the font
		// size. I use 6 chars instead of the actual required 2, because 2 was
		// just to small.
		// Also a little vertical indent to get it aligned with the Button
		// behind it (this again works for me and YMMV.
		FontMetrics fm = getFontMetrics(parent);
		bytesGD.widthHint = Dialog.convertWidthInCharsToPixels(fm, 6) * getMaxBytes();
		bytesGD.verticalIndent = 2;
		fBytesCompo.setLayoutData(bytesGD);
		fBytesCompo.setLayout(new FillLayout(SWT.HORIZONTAL));

		// Insert the byte editor compos
		for (int i = 0; i < getMaxBytes(); i++) {
			makeByteEditComposite(fBytesCompo, i);
		}

		// add the read button
		fReadButton = new Button(parent, SWT.PUSH);
		fReadButton.setText(TEXT_READDEVICE);
		fReadButton.setBackground(parent.getBackground());
		GridData editbuttonGD = new GridData(SWT.FILL, SWT.TOP, false, false);
		fReadButton.setLayoutData(editbuttonGD);
		fReadButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				readFuseBytesFromDevice();
			}
		});

		// add the copy button
		fCopyButton = new Button(parent, SWT.PUSH);
		fCopyButton.setText(TEXT_COPYFILE);
		fCopyButton.setBackground(parent.getBackground());
		GridData copybuttonGD = new GridData(SWT.BEGINNING, SWT.TOP, true, false);
		fCopyButton.setLayoutData(copybuttonGD);

		// TODO Remove this line once files are supported.
		fCopyButton.setVisible(false);

		// The Warning Composite
		fBytesWarning = new Composite(parent, SWT.NONE);
		fBytesWarning.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 4, 1));
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;
		fBytesWarning.setLayout(gl);

		Label warnicon = new Label(fBytesWarning, SWT.LEFT);
		warnicon.setLayoutData(new GridData(GridData.BEGINNING));
		warnicon.setImage(IMG_WARN);

		Label warnmessage = new Label(fBytesWarning, SWT.LEFT);
		warnmessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		warnmessage.setText(WARN_BYTESINCOMPATIBLE);

		fBytesWarning.setVisible(true);

	}

	/**
	 * Create an byte "Editor" composite.
	 * <p>
	 * The editor consists of a Text control to enter the byte value and a Label below it with the
	 * name of the byte.
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
	 *            The byte index for the editor.
	 * 
	 * @return <code>Composite</code> with the editor.
	 */
	private Composite makeByteEditComposite(Composite parent, int index) {

		FillLayout layout = new FillLayout(SWT.VERTICAL);

		Composite compo = new Composite(parent, SWT.NONE);
		compo.setLayout(layout);
		fByteCompos[index] = compo;

		// Add the Text control
		Text text = new Text(compo, SWT.BORDER | SWT.CENTER);
		text.setTextLimit(2);
		text.setSize(10, 20);
		text.setData(new Integer(index));

		// Add a modification listener to set the fuse byte
		text.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				// Get the byte number
				Text source = (Text) e.widget;
				int index = (Integer) source.getData();

				// Get the new value...
				int newvalue;
				if (source.getText().length() > 0) {
					newvalue = Integer.parseInt(source.getText(), 16);
				} else {
					// Text control is empty. Use the default
					newvalue = -1;
				}

				// ... and set the property
				fBytes.setValue(index, newvalue);
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
		fValueTexts[index] = text;

		// Add the label
		Label label = new Label(compo, SWT.CENTER);
		label.setText(getByteEditorLabel(index));
		label.setSize(10, 0);

		return compo;
	}

	/**
	 * Enable / Disable the file selector Controls
	 * 
	 * @param enabled
	 *            <code>true</code> to enable, <code>false</code> to disable.
	 */
	private void enableFileGroup(boolean enabled) {
		// TODO remove this once files are supported.
		if (true)
			return;

		fFileText.setEnabled(enabled);
		fWorkplaceButton.setEnabled(enabled);
		fFilesystemButton.setEnabled(enabled);
		fVariableButton.setEnabled(enabled);
	}

	/**
	 * Enable / Disable the Byte Editor Controls
	 * <p>
	 * When enabling, only those editors are enabled that are actually valid for the current MCU.
	 * </p>
	 * 
	 * @param enabled
	 *            <code>true</code> to enable, <code>false</code> to disable.
	 */
	private void enableByteGroup(boolean enabled) {
		for (int i = 0; i < fByteCompos.length; i++) {
			if (i >= fCount) {
				setEnabled(fByteCompos[i], false);
			} else {
				setEnabled(fByteCompos[i], enabled);
			}
		}
		fReadButton.setEnabled(enabled);
		// fCopyButton.setEnabled(enabled);
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

		// Copy the settings from the FuseBytes sub-properties
		AbstractBytes src = getByteProps(fTargetProps);
		AbstractBytes dst = getByteProps(dstprops);

		dst.setWrite(src.getWrite());
		dst.setUseFile(src.getUseFile());
		dst.setFileName(src.getFileName());
		dst.setValues(src.getValuesFromImmediate());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performCopy(AVRDudeProperties srcprops) {

		// Copy the settings from the AbstractBytes sub-properties
		AbstractBytes src = getByteProps(srcprops);
		AbstractBytes dst = getByteProps(fTargetProps);

		dst.setWrite(src.getWrite());
		dst.setUseFile(src.getUseFile());
		dst.setFileName(src.getFileName());
		dst.setValues(src.getValuesFromImmediate());

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
		fBytes = getByteProps(props);

		// Get the target MCU and its number of fusebytes
		String mcuid = props.getParent().getMCUId();
		fCount = getByteCount(mcuid);

		// TODO: Test if bytes are still valid and generate some warnings if
		// not.

		// Update the radio buttons

		// There are three possibilities:
		// a) No upload wanted: Write == false
		// b) Upload from file: Write == true && useFile == true
		// c) Upload from immediate: Write == true && useFile == false
		if (!fBytes.getWrite()) {
			// a) No upload wanted
			fNoUploadButton.setSelection(true);
			// fUploadFileButton.setSelection(false);
			fImmediateButton.setSelection(false);
			enableFileGroup(false);
			enableByteGroup(false);
		} else {
			// write bytes
			fNoUploadButton.setSelection(false);
			if (fBytes.getUseFile()) {
				// b) write bytes - use supplied file
				// fUploadFileButton.setSelection(true);
				fImmediateButton.setSelection(false);
				enableFileGroup(true);
				enableByteGroup(false);
			} else {
				// c) write bytes - use immediate bytes
				// fUploadFileButton.setSelection(false);
				fImmediateButton.setSelection(true);
				enableFileGroup(false);
				enableByteGroup(true);
			}
		}

		// Set the text for the filename
		// fFileText.setText(src.getFileName());

		// Set the immediate fuse values
		int[] values = fBytes.getValues();
		int count = Math.min(values.length, getMaxBytes());

		for (int i = 0; i < count; i++) {
			String newvalue = "";
			int currvalue = values[i];
			if (0 <= currvalue && currvalue <= 255) {
				newvalue = Integer.toHexString(currvalue).toUpperCase();
			}
			fValueTexts[i].setText(newvalue);
		}
	}

	/**
	 * Load the Bytes from the currently attached MCU.
	 * <p>
	 * This method will start a new Job to load the values and return immediately.
	 * </p>
	 */
	private void readFuseBytesFromDevice() {
		// Disable the Load Button. It is re-enabled by the load job when it finishes.
		fReadButton.setEnabled(false);
		fReadButton.setText(TEXT_READDEVICE_BUSY);

		// The Job that does the actual loading.
		Job readJob = new Job("Reading Fuse Bytes") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				try {
					monitor.beginTask("Starting AVRDude", 100);

					final ByteValues bytevalues = getByteValues(fTargetProps);

					monitor.worked(95);

					// and update the user interface
					if (!fReadButton.isDisposed()) {
						fReadButton.getDisplay().syncExec(new Runnable() {
							public void run() {
								// Check if the mcus match
								String projectmcu = fTargetProps.getParent().getMCUId();
								String newmcu = bytevalues.getMCUId();
								if (!projectmcu.equals(newmcu)) {
									// No, they don't match. Ask the user what to do
									// "Accept anyway" or "Cancel"
									Dialog dialog = new MCUMismatchDialog(fReadButton.getShell(),
											newmcu, projectmcu);
									int choice = dialog.open();
									if (choice == 1) {
										return;
									}
								}
								// Clear the current bytes and transfer the new values
								fBytes.clearValues();
								fBytes.setValues(bytevalues.getValues());

								updateData(fTargetProps);
							}
						});
					}
					monitor.worked(5);
				} catch (AVRDudeException ade) {
					// Show an Error message and exit
					if (!fReadButton.isDisposed()) {
						UIJob messagejob = new AVRDudeErrorDialogJob(fReadButton.getDisplay(), ade,
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
					// Enable the Load from MCU Button
					if (!fReadButton.isDisposed()) {
						fReadButton.getDisplay().syncExec(new Runnable() {
							public void run() {
								// Re-Enable the Button
								fReadButton.setEnabled(true);
								fReadButton.setText(TEXT_READDEVICE);
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
	 * An small Warning dialog that will be shown when the MCU for the bytes does not match the
	 * current Project / Configuration MCU.
	 * <p>
	 * In addition to a fixed warning message, this dialog sports two buttons to accept the byte
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

			super(shell, "AVRDude Warning", null, "", WARNING, new String[] { "Accept", "Cancel" },
					0);

			String proptype = isPerConfig() ? "build configuration" : "project";

			String source = "The {3} values are valid for an {0} MCU.\n"
					+ "This MCU is not compatible with the current {2} MCU [{1}].\n\n"
					+ "\"Accept\" to accept the new values anyway.\n"
					+ "\"Cancel\" to discard the new values.";

			this.message = MessageFormat.format(source, newmcu, projectmcu, proptype,
					getLabels()[LABEL_NAME]);
		}
	}

}