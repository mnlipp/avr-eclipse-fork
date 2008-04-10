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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.preferences.AVRProjectProperties;
import de.innot.avreclipse.ui.preferences.AVRDudeConfigEditor;

/**
 * The main / general AVRDude options tab.
 * <p>
 * On this tab, the following properties are edited:
 * <ul>
 * <li>Avrdude Programmer Configuration, incl. buttons to edit the current
 * config or add a new config</li>
 * <li>Enable Flash memory erase cycle counter, incl. the non-property related
 * options to load / write the current cycle counter value to / from a connected
 * device</li>
 * <li>Enable the no-Write / Simulation mode</li>
 * </ul>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class TabAVRDudeMain extends AbstractAVRDudePropertyTab {

	// The GUI texts
	private final static String GROUP_PROGCONFIG = "Programmer configuration";
	private final static String TEXT_EDITBUTTON = "Edit...";
	private final static String TEXT_NEWBUTTON = "New...";
	private final static String LABEL_CONFIG_WARNING = "The Programmer configuration previously associated with this project/configuration\n"
			+ "does not exist anymore. Please select a different one.";
	private final static String LABEL_NOCONFIG = "Please select a Programmer Configuration to enable avrdude functions";

	private final static String GROUP_COUNTER = "Flash memory erase cycle counter";
	private final static String LABEL_COUNTER = "Enable this to have avrdude count the number of flash erase cycles.\n"
			+ "Note: the value is stored in the last four bytes of the EEPROM,\n"
			+ "so do not enable this if your application needs the last four bytes of the EEPROM.";
	private final static String TEXT_COUNTER = "Enable erase cycle counter";
	private final static String TEXT_READBUTTON = "Read";
	private final static String TEXT_WRITEBUTTON = "Write";

	private final static String GROUP_NOWRITE = "Simulation Mode";
	private final static String LABEL_NOWRITE = "Note: Even with this option set, AVRDude might still perform a chip erase.";
	private final static String TEXT_NOWRITE = "Simulation mode (no data is actually written to the device)";

	// The GUI widgets
	private Combo fProgrammerCombo;
	private Label fConfigWarningIcon;
	private Label fConfigWarningMessage;
	private Button fNoWriteCheck;
	private Button fCounterCheck;
	private Composite fCounterOptionsComposite;
	private Text fCounterValue;

	/** The Properties that this page works with */
	private AVRProjectProperties fTargetProps;

	/** Warning image used for invalid Programmer Config values */
	private static final Image IMG_WARN = PlatformUI.getWorkbench()
			.getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {

		parent.setLayout(new GridLayout(1, false));

		addProgrammerConfigSection(parent);

		addUseCycleCounterSection(parent);

		addNoWriteSection(parent);

	}

	/**
	 * Add the Programmer Configuration selection <code>Combo</code> and the
	 * "Edit", "New" Buttons.
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	private void addProgrammerConfigSection(Composite parent) {

		Group configgroup = setupGroup(parent, GROUP_PROGCONFIG, 3, SWT.NONE);
		configgroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false,
				1, 1));

		fProgrammerCombo = new Combo(configgroup, SWT.READ_ONLY);
		fProgrammerCombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false));
		fProgrammerCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedname = fProgrammerCombo.getItem(fProgrammerCombo
						.getSelectionIndex());
				String selectedid = getProgrammerConfigId(selectedname);
				fTargetProps.setAVRDudeProgrammerId(selectedid);
				showProgrammerWarning("", false);
				updatePreview(fTargetProps);
			}
		});
		// Init the combo with the list of available programmer configurations
		loadProgrammerConfigs();

		// Edit... Button
		Button editButton = setupButton(configgroup, TEXT_EDITBUTTON, 1,
				SWT.NONE);
		editButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editButtonAction(false);
			}
		});

		// New... Button
		Button newButton = setupButton(configgroup, TEXT_NEWBUTTON, 1, SWT.NONE);
		newButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				editButtonAction(true);
			}
		});

		// The Warning icon / message composite
		Composite warningComposite = new Composite(configgroup, SWT.NONE);
		warningComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false, 3, 1));
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;
		warningComposite.setLayout(gl);

		fConfigWarningIcon = new Label(warningComposite, SWT.LEFT);
		fConfigWarningIcon.setLayoutData(new GridData(SWT.BEGINNING,
				SWT.BEGINNING, false, false));
		fConfigWarningIcon.setImage(IMG_WARN);

		fConfigWarningMessage = new Label(warningComposite, SWT.LEFT);
		fConfigWarningMessage.setLayoutData(new GridData(
				GridData.FILL_HORIZONTAL));
		fConfigWarningMessage.setText("two-line\ndummy");

		// By default make the warning invisible
		// updateData() will make it visible when required
		fConfigWarningIcon.setVisible(false);
		fConfigWarningMessage.setVisible(false);

	}

	/**
	 * Add the GUI section for the Flash memory cycle counter settings.
	 * <p>
	 * This section contains not only the actual checkbox, but also some
	 * controls to read/write the current cycle counter. These controls are not
	 * directly related to the properties, but it seems like a good idea to put
	 * them here.
	 * </p>
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	private void addUseCycleCounterSection(Composite parent) {

		Group countergroup = setupGroup(parent, GROUP_COUNTER, 1, SWT.NONE);
		countergroup.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false, 1, 1));

		setupLabel(countergroup, LABEL_COUNTER, 2, SWT.NONE);

		fCounterCheck = setupCheck(countergroup, TEXT_COUNTER, 2, SWT.NONE);

		// Cycle Counter load / write control composite

		fCounterOptionsComposite = new Composite(countergroup, SWT.NONE);
		fCounterOptionsComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				true, false, 2, 1));
		fCounterOptionsComposite.setLayout(new GridLayout(4, false));

		setupLabel(fCounterOptionsComposite, "Current Erase Cycle Counter", 1,
				SWT.NONE);

		fCounterValue = setupText(fCounterOptionsComposite, 1, SWT.BORDER);
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
		Button readButton = setupButton(fCounterOptionsComposite,
				TEXT_READBUTTON, 1, SWT.NONE);
		readButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				readCycleCounter();
			}
		});

		// Write Button
		Button writeButton = setupButton(fCounterOptionsComposite,
				TEXT_WRITEBUTTON, 1, SWT.NONE);
		writeButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				writeCycleCounter();
			}
		});

		// Disable by default. The updateData() method will enable/disable
		// according to the actual property settings.
		setEnabled(fCounterOptionsComposite, false);
	}

	/**
	 * Add the No Write / Simulate check button.
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	private void addNoWriteSection(Composite parent) {

		Group group = setupGroup(parent, GROUP_NOWRITE, 1, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true,
				false, 1, 1));

		setupLabel(group, LABEL_NOWRITE, 1, SWT.NONE);
		fNoWriteCheck = setupCheck(group, TEXT_NOWRITE, 1, SWT.CHECK);
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
		Control b = (Control) e.widget;

		if (b.equals(fNoWriteCheck)) {
			// No Write = Simulation Checkbox has been selected
			// Write the new value to the target properties
			boolean newvalue = fNoWriteCheck.getSelection();
			fTargetProps.setAVRDudeNoWrite(newvalue);

		} else if (b.equals(fCounterCheck)) {
			// Use Cycle Counter Checkbox has been selected
			// Write the new value to the target properties
			// and enable / disable the Counter Options composite accordingly
			boolean newvalue = fCounterCheck.getSelection();
			fTargetProps.setAVRDudeUseCounter(newvalue);
			setEnabled(fCounterOptionsComposite, newvalue);
		}

		// Update the avrdude command line preview
		updatePreview(fTargetProps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performApply(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performApply(AVRProjectProperties dst) {

		// Save all new / modified programmer configurations
		saveProgrammerConfigs();

		// Copy the currently selected values of this tab to the given, fresh
		// Properties.
		// The caller of this method will handle the actual saving
		dst.setAVRDudeProgrammerId(fTargetProps.getAVRDudeProgrammerId());
		dst.setAVRDudeNoWrite(fTargetProps.getAVRDudeNoWrite());
		dst.setAVRDudeUseCounter(fTargetProps.getAVRDudeUseCounter());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performCopy(AVRProjectProperties source) {

		// Let the superclass update the avrdude command preview
		super.performCopy(source);

		// Reset the list of Programmer Configurations
		loadProgrammerConfigs();

		// Reload the items on this page
		fTargetProps.setAVRDudeProgrammerId(source.getAVRDudeProgrammerId());
		fTargetProps.setAVRDudeNoWrite(source.getAVRDudeNoWrite());
		fTargetProps.setAVRDudeUseCounter(source.getAVRDudeUseCounter());
		updateData(fTargetProps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#updateData(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void updateData(AVRProjectProperties props) {

		// Let the superclass update the avrdude command preview
		super.updateData(props);

		fTargetProps = props;

		// Set the selection of the Programmercombo
		// If the programmerid of the target properties does not exist,
		// show a warning and select the first item (without copying it into the
		// Target Properties)
		String programmerid = props.getAVRDudeProgrammerId();
		if (programmerid.length() == 0) {
			// No Programmer has been set yet
			// Deselect the combo and show a Message
			fProgrammerCombo.deselect(fProgrammerCombo.getSelectionIndex());
			showProgrammerWarning(LABEL_NOCONFIG, false);
		} else {
			// Programmer id exists. Now test if it is still valid
			if (!isValidId(programmerid)) {
				// id is not valid. Deselect Combo and show a Warning
				fProgrammerCombo.deselect(fProgrammerCombo.getSelectionIndex());
				showProgrammerWarning(LABEL_CONFIG_WARNING, true);
			} else {
				// everything is good. Select the id in the combo
				String programmername = getProgrammerConfigName(programmerid);
				int index = fProgrammerCombo.indexOf(programmername);
				fProgrammerCombo.select(index);
				showProgrammerWarning("", false);
			}
		}

		fNoWriteCheck.setSelection(props.getAVRDudeNoWrite());

		boolean usecounter = props.getAVRDudeUseCounter();
		fCounterCheck.setSelection(usecounter);
		setEnabled(fCounterOptionsComposite, usecounter);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRDudePropertyTab#doProgConfigsChanged(java.lang.String[],
	 *      int)
	 */
	@Override
	protected void doProgConfigsChanged(String[] configs, int newindex) {

		fProgrammerCombo.setItems(configs);

		// make the combo show all available items (no scrollbar)
		fProgrammerCombo.setVisibleItemCount(configs.length);

		if (newindex != -1) {
			fProgrammerCombo.select(newindex);
		} else {
			fProgrammerCombo.deselect(fProgrammerCombo.getSelectionIndex());
		}
	};

	/**
	 * Adds a new configuration or edits the currently selected Programmer
	 * Configuration.
	 * <p>
	 * Called when either the new or the edit button has been clicked.
	 * </p>
	 * 
	 * @see AVRDudeConfigEditor
	 */
	private void editButtonAction(boolean createnew) {
		ProgrammerConfig oldconfig = null;

		// Create a list of all currently available configurations
		// This is used by the editor to avoid name clashes
		// (a configuration name needs to be unique)
		String[] allcfgs = fProgrammerCombo.getItems();
		Set<String> allconfignames = new HashSet<String>(allcfgs.length);
		for (String cfg : allcfgs) {
			allconfignames.add(cfg);
		}

		if (createnew) { // new config
			// Create a new configuration with a default name
			// (with a trailing running number if required),
			// a sample Description text and stk500v2 as programmer
			// (because I happen to have one)
			// All other options remain at the default (empty)
			String basename = "New Configuration";
			String defaultname = basename;
			int i = 1;
			while (allconfignames.contains(defaultname)) {
				defaultname = basename + " (" + i++ + ")";
			}
			oldconfig = fCfgManager.createNewConfig();
			oldconfig.setName(defaultname);
		} else { // edit existing config
			// Get the ProgrammerConfig from the Combo
			String configname = allcfgs[fProgrammerCombo.getSelectionIndex()];
			String configid = getProgrammerConfigId(configname);
			oldconfig = getProgrammerConfig(configid);
		}

		// Open the Config Editor.
		// If the OK Button was selected, the modified Config is fetched from
		// the Dialog and the the superclass is informed about the addition /
		// modification.
		AVRDudeConfigEditor dialog = new AVRDudeConfigEditor(fProgrammerCombo
				.getShell(), oldconfig, allconfignames);
		if (dialog.open() == Window.OK) {
			// OK Button selected:
			ProgrammerConfig newconfig = dialog.getResult();
			fTargetProps.setAVRDudeProgrammer(newconfig);

			addProgrammerConfig(newconfig);
			updateData(fTargetProps);
		}
	}

	/**
	 * Show the supplied Warning in the Programmer config group.
	 * 
	 * @param text
	 *            Message to display.
	 * @param warning
	 *            <code>true</code> to make the warning visible,
	 *            <code>false</code> to hide it.
	 */
	private void showProgrammerWarning(String text, boolean warning) {
		fConfigWarningIcon.setVisible(warning);
		fConfigWarningMessage.setText(text);
		fConfigWarningMessage.pack();
		fConfigWarningMessage.setVisible(true);
	}

	/**
	 * Enable / Disable the given composite.
	 * <p>
	 * This method will call setEnabled(value) for all children of the supplied
	 * composite.
	 * </p>
	 * 
	 * @param composite
	 *            A <code>Composite</code> with some controls.
	 * @param value
	 *            <code>true</code> to enable, <code>false</code> to disable
	 *            the given composite.
	 */
	private void setEnabled(Composite composite, boolean value) {
		Control[] children = composite.getChildren();
		for (Control child : children) {
			child.setEnabled(value);
		}
	}

	/**
	 * Read the current cycles value from the currently attached device.
	 * <p>
	 * The actual read is done in a separate Thread to reduce the impact on the
	 * GUI.
	 * </p>
	 */
	private void readCycleCounter() {

		// TODO implement this

		MessageDialog dlg = new MessageDialog(fCounterOptionsComposite
				.getShell(), "Information", null, "Read not implemented yet",
				MessageDialog.INFORMATION, new String[] { "OK" }, 0);
		dlg.setBlockOnOpen(true);
		dlg.open();
		fCounterValue.setText("1234");
	}

	/**
	 * Write the selected cycles value to the currently attached device.
	 * <p>
	 * The actual load is done in a separate Thread to reduce the impact on the
	 * GUI.
	 * </p>
	 */
	private void writeCycleCounter() {

		// TODO implement this

		MessageDialog dlg = new MessageDialog(fCounterOptionsComposite
				.getShell(), "Information", null, "Write not implemented yet",
				MessageDialog.INFORMATION, new String[] { "OK" }, 0);
		dlg.setBlockOnOpen(true);
		dlg.open();
	}

}
