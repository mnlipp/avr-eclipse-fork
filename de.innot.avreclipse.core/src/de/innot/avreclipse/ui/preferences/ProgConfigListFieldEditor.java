/**
 * 
 */
package de.innot.avreclipse.ui.preferences;

import java.util.Set;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Widget;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.preferences.AVRDudePreferences;

/**
 * @author Thomas
 * 
 */
public class ProgConfigListFieldEditor extends FieldEditor {

	/** The Table Control */
	private Table fTableControl;

	/** The button box Composite containing the Add, Remove and Edit buttons */
	private Composite fButtonComposite;

	private Button fAddButton;
	private Button fRemoveButton;
	private Button fEditButton;

	/**
	 * Creates a AVRDude Programmers Configuration List field editor.
	 * 
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 */
	public ProgConfigListFieldEditor(String label, Composite parent) {
		super();
		super.setLabelText(label);
		createControl(parent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#adjustForNumColumns(int)
	 */
	@Override
	protected void adjustForNumColumns(int numColumns) {
		Control control = getLabelControl();
		((GridData) control.getLayoutData()).horizontalSpan = numColumns;
		((GridData) fTableControl.getLayoutData()).horizontalSpan = numColumns - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doFillIntoGrid(org.eclipse.swt.widgets.Composite,
	 *      int)
	 */
	@Override
	protected void doFillIntoGrid(Composite parent, int numColumns) {
		Control control = getLabelControl(parent);
		GridData gd = new GridData();
		gd.horizontalSpan = numColumns;
		control.setLayoutData(gd);

		fTableControl = getTableControl(parent);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.verticalAlignment = GridData.FILL;
		gd.horizontalSpan = numColumns - 1;
		gd.grabExcessHorizontalSpace = true;
		gd.grabExcessVerticalSpace = true;
		fTableControl.setLayoutData(gd);

		fButtonComposite = getButtonBoxComposite(parent);
		gd = new GridData();
		gd.verticalAlignment = GridData.BEGINNING;
		fButtonComposite.setLayoutData(gd);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doLoad()
	 */
	@Override
	protected void doLoad() {
		if (fTableControl != null) {
			Set<String> allconfigs = AVRDudePreferences.getAllConfigs();
			for (String configname : allconfigs) {
				if (!configname.isEmpty()) {
					TableItem item = new TableItem(fTableControl, SWT.NONE);
					item.setText(configname);
					ProgrammerConfig config = new ProgrammerConfig(configname);
					item.setData(config);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doLoadDefault()
	 */
	@Override
	protected void doLoadDefault() {
		// No defaults supported for the List of Programmer Configurations

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#doStore()
	 */
	@Override
	protected void doStore() {
		TableItem[] allitems = fTableControl.getItems();
		
		for (TableItem item : allitems) {
			ProgrammerConfig config = (ProgrammerConfig)item.getData();
			try {
				config.save();
			} catch (BackingStoreException e) {
				// TODO Pop up message that some configs could not be saved
				e.printStackTrace();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#getNumberOfControls()
	 */
	@Override
	public int getNumberOfControls() {
		// Two: List and Buttons Composite
		return 2;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditor#setFocus()
	 */
	@Override
	public void setFocus() {
		if (fTableControl != null) {
			fTableControl.setFocus();
		}
	}

	/**
	 * Returns this field editor's list control.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the list control
	 */
	public Table getTableControl(Composite parent) {
		if (fTableControl == null) {
			fTableControl = new Table(parent, SWT.BORDER | SWT.SINGLE
					| SWT.V_SCROLL | SWT.H_SCROLL);
			fTableControl.setFont(parent.getFont());
			fTableControl.setLinesVisible(true);
			fTableControl.setHeaderVisible(false);
			fTableControl.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					Widget widget = e.widget;
					if (widget == fTableControl) {
						selectionChanged();
					}
				}
			});

			fTableControl.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					fTableControl = null;
				}
			});

			TableColumn column = new TableColumn(fTableControl, SWT.NONE);
			column.setText("Configuration");
			column.setWidth(100);
			column = new TableColumn(fTableControl, SWT.NONE);
			column.setText("Description");
			column.setWidth(200);

		} else {
			checkParent(fTableControl, parent);
		}
		return fTableControl;
	}

	/**
	 * Returns this field editor's button box containing the Add, Remove and
	 * Edit buttons.
	 * 
	 * @param parent
	 *            the parent control
	 * @return the button box
	 */
	public Composite getButtonBoxComposite(Composite parent) {
		if (fButtonComposite == null) {
			fButtonComposite = new Composite(parent, SWT.NULL);
			GridLayout layout = new GridLayout();
			layout.marginWidth = 0;
			fButtonComposite.setLayout(layout);
			createButtons(fButtonComposite);
			fButtonComposite.addDisposeListener(new DisposeListener() {
				public void widgetDisposed(DisposeEvent event) {
					fAddButton = null;
					fRemoveButton = null;
					fEditButton = null;
				}
			});

		} else {
			checkParent(fButtonComposite, parent);
		}

		selectionChanged();
		return fButtonComposite;
	}

	/**
	 * Creates the Add, Remove and Edit buttons in the given button box.
	 * 
	 * @param box
	 *            the box for the buttons
	 */
	private void createButtons(Composite box) {
		fAddButton = createPushButton(box, "Add...");
		fRemoveButton = createPushButton(box, "Remove");
		fEditButton = createPushButton(box, "Edit...");
	}

	/**
	 * Helper method to create a push button.
	 * 
	 * @param parent
	 *            the parent control
	 * @param key
	 *            the resource name used to supply the button's label text
	 * @return Button
	 */
	private Button createPushButton(Composite parent, String key) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(JFaceResources.getString(key));
		button.setFont(parent.getFont());
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		int widthHint = convertHorizontalDLUsToPixels(button,
				IDialogConstants.BUTTON_WIDTH);
		data.widthHint = Math.max(widthHint, button.computeSize(SWT.DEFAULT,
				SWT.DEFAULT, true).x);
		button.setLayoutData(data);
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				Widget widget = e.widget;
				if (widget == fAddButton) {
					addButtonAction();
				} else if (widget == fRemoveButton) {
					removeButtonAction();
				} else if (widget == fEditButton) {
					editButtonAction();
				}
			}
		});
		return button;
	}

	/**
	 * Notifies that the list selection has changed.
	 */
	private void selectionChanged() {

		int index = fTableControl.getSelectionIndex();

		fRemoveButton.setEnabled(index >= 0);
		fEditButton.setEnabled(index >= 0);

		fTableControl.redraw();
	}

	/**
	 * Notifies that the Add button has been pressed.
	 */
	private void addButtonAction() {
		setPresentsDefaultValue(false);

		ProgrammerConfig config = new ProgrammerConfig();

		AVRDudeConfigEditor dialog = new AVRDudeConfigEditor(fTableControl
				.getShell(), config);
		if (dialog.open() == Window.OK) {
			// OK Button selected:
			ProgrammerConfig newconfig = dialog.getResult();
			
			// Add the configuration to the table
			TableItem newitem = new TableItem(fTableControl, SWT.NONE);
			newitem.setText(new String[] { newconfig.getName(),
					newconfig.getDescription() });
			newitem.setData(newconfig);
			selectionChanged();
		}
	}

	/**
	 * Notifies that the Remove button has been pressed.
	 */
	private void removeButtonAction() {
		setPresentsDefaultValue(false);
		int index = fTableControl.getSelectionIndex();
		if (index >= 0) {
			fTableControl.remove(index);
			selectionChanged();
		}
	}

	/**
	 * Notifies that the Edit button has been pressed.
	 */
	private void editButtonAction() {
		setPresentsDefaultValue(false);
		TableItem ti = fTableControl.getItem(fTableControl.getSelectionIndex());
		ProgrammerConfig config = (ProgrammerConfig)ti.getData();

		AVRDudeConfigEditor dialog = new AVRDudeConfigEditor(fTableControl
				.getShell(), config);
		if (dialog.open() == Window.OK) {
			// OK Button selected:
			ProgrammerConfig newconfig = dialog.getResult();
			
			// Change the TableItem
			ti.setText(new String[] {newconfig.getName(), newconfig.getDescription()});
			ti.setData(newconfig);
			selectionChanged();
		}
	}

}
