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
 * $Id: PathSettingDialog.java 338 2008-03-01 10:53:16Z innot $
 *     
 *******************************************************************************/

package de.innot.avreclipse.ui.preferences;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.AVRDude.ConfigEntry;

/**
 * Dialog to edit a AVRDude Programmer Configuration.
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class AVRDudeConfigEditor extends StatusDialog {

	// The GUI Widgets
	private Text fNameControl;
	private Text fDescriptionControl;
	private List fProgrammerListControl;
	private Composite fProgrammerDetails;
	private Text fPortControl;
	private Combo fBaudrateControl;
	private Group fExitspecResetGroup;

	// The working copy of the given source Configuration
	private ProgrammerConfig fConfig = null;

	private Map<String, ConfigEntry> fConfigIDMap;
	private Map<String, ConfigEntry> fConfigNameMap;

	/**
	 * Constructor for a new Configuration Editor.
	 * 
	 * The passed IPathManager is copied and any changes to the path are only
	 * written back to it when the {@link #getResult()} method is called.
	 * 
	 * @param parent
	 *            Parent <code>Shell</code>
	 * @param pathmanager
	 *            IPathManager with the path to edit
	 */
	public AVRDudeConfigEditor(Shell parent, ProgrammerConfig config) {
		super(parent);

		fConfig = new ProgrammerConfig(config);

		setTitle("Edit AVRDude Programmer Configuration " + config.getName());

		// Allow this dialog to be resizeable
		setShellStyle(getShellStyle() | SWT.RESIZE);

		// Get the List of AVRDude Programmer ConfigEntries
		Set<String> programmers = AVRDude.getDefault().getProgrammersList();
		fConfigIDMap = new HashMap<String, ConfigEntry>(programmers.size());
		fConfigNameMap = new HashMap<String, ConfigEntry>(programmers.size());
		for (String progid : programmers) {
			ConfigEntry entry = AVRDude.getDefault().getProgrammerInfo(progid);
			fConfigIDMap.put(progid, entry);
			fConfigNameMap.put(entry.description, entry);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(3, false));

		addNameControl(composite);

		addDescriptionControl(composite);

		addProgrammersComposite(composite);

		addPortControl(composite);

		addBaudrateControl(composite);

		addExitspecComposite(composite);

		return composite;
	}

	private void addNameControl(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Configuration name");
		fNameControl = new Text(parent, SWT.BORDER);
		fNameControl.setText(fConfig.getName());
		fNameControl.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false, 2, 1));
		fNameControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String newname = fNameControl.getText();
				fConfig.setName(newname);
			}
		});

	}

	private void addDescriptionControl(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Description");
		fDescriptionControl = new Text(parent, SWT.BORDER);
		fDescriptionControl.setText(fConfig.getDescription());
		fDescriptionControl.setLayoutData(new GridData(SWT.FILL, SWT.NONE,
				true, false, 2, 1));
		fDescriptionControl.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String newdescription = fDescriptionControl.getText();
				fConfig.setDescription(newdescription);
			}
		});
	}

	private void addProgrammersComposite(Composite parent) {
		Group listgroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		listgroup.setText("Programmer Hardware (-c)");
		listgroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 3,
				1));
		FillLayout fl = new FillLayout();
		fl.marginHeight = 5;
		fl.marginWidth = 5;
		listgroup.setLayout(fl);

		SashForm sashform = new SashForm(listgroup, SWT.HORIZONTAL);
		fProgrammerListControl = new List(sashform, SWT.BORDER | SWT.V_SCROLL
				| SWT.H_SCROLL);
		String[] allprogrammers = getProgrammesList();
		fProgrammerListControl.setItems(allprogrammers);
		fProgrammerListControl
				.select(fProgrammerListControl.indexOf(fConfigIDMap.get(fConfig
						.getProgrammer()).description));
		fProgrammerDetails = new Composite(sashform, SWT.BORDER);
	}

	private void addPortControl(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Port (-P)");
		fPortControl = new Text(parent, SWT.BORDER);
		fPortControl.setText(fConfig.getPort());
		fPortControl.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true,
				false, 2, 1));
		fPortControl.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				String newport = (String)e.data;
				fConfig.setPort(newport);
			}
		});
	}

	private void addBaudrateControl(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Override Baudrate (-b)");
		fBaudrateControl = new Combo(parent, SWT.BORDER);
		fBaudrateControl.setText(fConfig.getDescription());
		fBaudrateControl.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, true,
				false, 2, 1));
		fBaudrateControl.add("default");
		fBaudrateControl
				.setItems(new String[] { "", "1200", "2400", "4800", "9600",
						"19200", "38400", "57600", "115200", "230400", "460800" });
		fBaudrateControl
				.select(fBaudrateControl.indexOf(fConfig.getBaudrate()));

		fBaudrateControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String newbaudrte = fBaudrateControl.getText();
				fConfig.setBaudrate(newbaudrte);
			}
		});
	}

	private void addExitspecComposite(Composite parent) {
		Label label = new Label(parent, SWT.NONE);
		label.setText("State of Parallel Port lines after AVRDude exit");
		label
				.setLayoutData(new GridData(SWT.LEFT, SWT.NONE, false, false,
						3, 1));
		Composite groupcontainer = new Composite(parent, SWT.NONE);
		groupcontainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false,
				false, 3, 1));
		FillLayout containerlayout = new FillLayout(SWT.HORIZONTAL);
		containerlayout.spacing = 10;
		groupcontainer.setLayout(containerlayout);

		FillLayout grouplayout = new FillLayout(SWT.VERTICAL);
		grouplayout.marginHeight = 5;
		grouplayout.marginWidth = 5;
		grouplayout.spacing = 5;

		Group resetgroup = new Group(groupcontainer, SWT.NONE);
		resetgroup.setText("/Reset Line");
		resetgroup.setLayout(grouplayout);
		Button resetDefault = new Button(resetgroup, SWT.RADIO);
		resetDefault.setText("default");
		Button resetReset = new Button(resetgroup, SWT.RADIO);
		resetReset.setText("activated (-E reset)");
		Button resetNoReset = new Button(resetgroup, SWT.RADIO);
		resetNoReset.setText("deactivated (-E noreset)");

		Group vccgroup = new Group(groupcontainer, SWT.NONE);
		vccgroup.setText("Vcc Lines");
		vccgroup.setLayout(grouplayout);
		Button vccDefault = new Button(vccgroup, SWT.RADIO);
		vccDefault.setText("default");
		Button vccVCC = new Button(vccgroup, SWT.RADIO);
		vccVCC.setText("activated (-E vcc)");
		Button vccNoVcc = new Button(vccgroup, SWT.RADIO);
		vccNoVcc.setText("deactivated (-E novcc)");
	}

	/**
	 * Get the results from this dialog.
	 * <p>
	 * It will return a new ProgrammerConfig with the updated items.
	 * </p>
	 * <p>
	 * This should only be called when <code>open()</code> returned
	 * <code>OK</code> (OK Button clicked). Otherwise canceled changes will be
	 * returned.
	 * </p>
	 * 
	 * @return The ProgrammerConfig with the modified values.
	 */
	public ProgrammerConfig getResult() {
		return fConfig;
	}

	private String[] getProgrammesList() {

		Set<String> nameset = fConfigNameMap.keySet();
		String[] allnames = nameset.toArray(new String[nameset.size()]);
		Arrays.sort(allnames, String.CASE_INSENSITIVE_ORDER);
		return allnames;
	}

}
