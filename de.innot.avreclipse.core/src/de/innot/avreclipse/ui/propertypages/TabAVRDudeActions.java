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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.innot.avreclipse.core.preferences.AVRProjectProperties;

/**
 * The AVRDude Actions Tab page.
 * <p>
 * On this tab, the following properties are edited:
 * <ul>
 * <li></li>
 * </ul>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class TabAVRDudeActions extends AbstractAVRDudePropertyTab {

	// The GUI texts

	// The GUI widgets
	private static final String LABEL_BACKUPGROUP = "Backup";
	private static final String TEXT_BACKUPENABLE = "Enable device backup";
	private static final String LABEL_BACKUPFOLDER = "Backup folder";
	private Button fBackupEnableCheck;
	private Text fBackupFolderText;
	private Label fBackupWarnImage;
	private Label fBackupWarnLabel;
	private Button fBackupWorkplaceButton;
	private Button fBackupFilesystemButton;
	private Button fBackupVariableButton;

	private static final String TEXT_FLASHENABLE = "Write flash memory";
	private Button fFlashEnableCheck;
	
	private static final String TEXT_EEPROMENABLE = "Write EEPROM";
	private Button fEEPROMEnableCheck;

	/** The Properties that this page works with */
	private AVRProjectProperties fTargetProps;

	/** Warning image used for invalid Programmer Config values */
	private static final Image IMG_WARN = PlatformUI.getWorkbench().getSharedImages().getImage(
	        ISharedImages.IMG_OBJS_WARN_TSK);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));

		addBackupSection(usercomp);

		fFlashEnableCheck = setupCheck(usercomp, TEXT_FLASHENABLE, 2, SWT.NONE);

		fEEPROMEnableCheck = setupCheck(usercomp, TEXT_EEPROMENABLE,2, SWT.NONE);

		// addFusesSection(usercomp);
		//
		// addLockbitsSection(usercomp);

	}

	/**
	 * Add the Backup Group.
	 * <p>
	 * This consists of a checkbox to enable/disable backups and a Text Control
	 * for the destination folder.<br>
	 * There are three buttons to select Folders: From Workplace, from
	 * FileSystem, and insert Variable. This is the same Layout used at other
	 * places in the CDT GUI.
	 * </p>
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	private void addBackupSection(Composite parent) {

		Group backupgroup = setupGroup(parent, LABEL_BACKUPGROUP, 2, SWT.NONE);
		backupgroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));

		fBackupEnableCheck = setupCheck(backupgroup, TEXT_BACKUPENABLE, 2, SWT.NONE);

		Label label = new Label(backupgroup, SWT.NONE);
		label.setText(LABEL_BACKUPFOLDER);
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		fBackupFolderText = new Text(backupgroup, SWT.BORDER);
		fBackupFolderText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fBackupFolderText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String newpath = fBackupFolderText.getText();
				fTargetProps.setAVRDudeBackupFolder(newpath);
			}
		});

		Composite compo = new Composite(backupgroup, SWT.NONE);
		compo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		compo.setLayout(new GridLayout(5, false));

		fBackupWarnImage = new Label(compo, SWT.NONE);
		fBackupWarnImage.setImage(IMG_WARN);
		fBackupWarnImage.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false));
		fBackupWarnImage.setVisible(false);

		fBackupWarnLabel = new Label(compo, SWT.NONE);
		fBackupWarnLabel.setText("Not a valid directory");
		fBackupWarnLabel.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fBackupWarnLabel.setVisible(false);

		fBackupWorkplaceButton = setupWorkplaceButton(compo, fBackupFolderText);
		fBackupFilesystemButton = setupFilesystemButton(compo, fBackupFolderText);
		fBackupVariableButton = setupVariableButton(compo, fBackupFolderText);

	}

	private void enableBackup(boolean enabled) {
		fBackupFolderText.setEnabled(enabled);
		fBackupWarnImage.setEnabled(enabled);
		fBackupWarnLabel.setEnabled(enabled);
		fBackupWorkplaceButton.setEnabled(enabled);
		fBackupFilesystemButton.setEnabled(enabled);
		fBackupVariableButton.setEnabled(enabled);
	}


	private Button setupWorkplaceButton(Composite compo, final Text text) {
		Button button = new Button(compo, SWT.PUSH);
		button.setText(WORKSPACEBUTTON_NAME);
		GridData gd = new GridData(SWT.CENTER, SWT.NONE, false, false);
		gd.minimumWidth = BUTTON_WIDTH;
		button.setLayoutData(gd);
		button.setData(text);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String location = getWorkspaceDirDialog(usercomp.getShell(), EMPTY_STR);
				if (location != null) {
					text.setText(location);
				}
			}
		});
		return button;
	}

	private Button setupFilesystemButton(Composite compo, final Text text) {
		Button button = new Button(compo, SWT.PUSH);
		button.setText(FILESYSTEMBUTTON_NAME);
		GridData gd = new GridData(SWT.CENTER, SWT.NONE, false, false);
		gd.minimumWidth = BUTTON_WIDTH;
		button.setLayoutData(gd);
		button.setData(text);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String location = getFileSystemDirDialog(usercomp.getShell(), EMPTY_STR);
				if (location != null) {
					text.setText(location);
				}
			}
		});
		return button;
	}

	private Button setupVariableButton(Composite compo, final Text text) {
		Button button = new Button(compo, SWT.PUSH);
		button.setText(VARIABLESBUTTON_NAME);
		GridData gd = new GridData(SWT.CENTER, SWT.NONE, false, false);
		gd.minimumWidth = BUTTON_WIDTH;
		button.setLayoutData(gd);
		button.setData(text);
		button.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				String var = getVariableDialog(usercomp.getShell(), getResDesc().getConfiguration());
				if (var != null) {
					text.insert(var);
				}
			}
		});
		return button;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#checkPressed(org.eclipse.swt.events.SelectionEvent)
	 */
	protected void checkPressed(SelectionEvent e) {
		// This is called for all checkbuttons / tributtons which have been set
		// up with the setupXXX() calls
		Control source = (Control) e.widget;
		if (source.equals(fBackupEnableCheck)) {
			// Enable Backup checkbox selected
			boolean enabled = fBackupEnableCheck.getSelection();
			fTargetProps.setAVRDudeBackupEnabled(enabled);
			enableBackup(enabled);
		} else if (source.equals(fFlashEnableCheck)) {
			boolean enabled = fFlashEnableCheck.getSelection();
			fTargetProps.setAVRDudeWriteFlash(enabled);
		} else if (source.equals(fEEPROMEnableCheck))  {
			boolean enabled = fEEPROMEnableCheck.getSelection();
			fTargetProps.setAVRDudeWriteEEPROM(enabled);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performApply(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performApply(AVRProjectProperties dst) {

		if (fTargetProps == null) {
			// updataData() has not been called and this tab has no (modified) settings yet.
			return;
		}
		// Copy the currently selected values of this tab to the given, fresh
		// Properties.
		// The caller of this method will handle the actual saving
		dst.setAVRDudeBackupEnabled(fTargetProps.getAVRDudeBackupEnabled());
		dst.setAVRDudeBackupFolder(fTargetProps.getAVRDudeBackupFolder());
		dst.setAVRDudeWriteFlash(fTargetProps.getAVRDudeWriteFlash());
		dst.setAVRDudeWriteEEPROM(fTargetProps.getAVRDudeWriteEEPROM());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performCopy(AVRProjectProperties source) {

		// Reload the items on this page
		fTargetProps.setAVRDudeBackupEnabled(source.getAVRDudeBackupEnabled());
		fTargetProps.setAVRDudeBackupFolder(source.getAVRDudeBackupFolder());
		fTargetProps.setAVRDudeWriteFlash(source.getAVRDudeWriteFlash());
		fTargetProps.setAVRDudeWriteEEPROM(source.getAVRDudeWriteEEPROM());
		updateData(fTargetProps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#updateData(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void updateData(AVRProjectProperties props) {

		fTargetProps = props;

		boolean backupEnabled = props.getAVRDudeBackupEnabled();
		fBackupEnableCheck.setSelection(props.getAVRDudeBackupEnabled());
		enableBackup(backupEnabled);

		fBackupFolderText.setText(props.getAVRDudeBackupFolder());
		
		fFlashEnableCheck.setSelection(props.getAVRDudeWriteFlash());
		
		fEEPROMEnableCheck.setSelection(props.getAVRDudeWriteEEPROM());

	}

}
