/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.ui.editors.targets;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.HostInterface;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class ProgrammerSection extends SectionPart {

	private ITargetConfigurationWorkingCopy	fTCWC;

	private Combo							fProgrammersCombo;
	private Label							fProgrammersWarningImageLabel;

	private Combo							fHostPortCombo;

	private Section							fHostInterfaceSection;
	private Section							fTargetInterfaceSection;

	/** Reverse mapping of programmer description to id. */
	private Map<String, String>				fMapDescToId;

	/** Reverse mapping of host interface description to host interface. */
	private Map<String, HostInterface>		fMapDescToHostPort;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public ProgrammerSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.TITLE_BAR | Section.DESCRIPTION);

		getSection().setText("Programmer Hardware / Interface");
		getSection().setDescription("TODO Description");
		getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		fMapDescToId = new HashMap<String, String>();
		fMapDescToHostPort = new HashMap<String, HostInterface>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);

		FormToolkit toolkit = form.getToolkit();
		Composite content = toolkit.createComposite(getSection());
		content.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 3;
		content.setLayout(layout);

		//
		// The Programmers Combo
		// 
		Label label = toolkit.createLabel(content, "Programmer:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));

		fProgrammersCombo = new Combo(content, SWT.READ_ONLY);
		toolkit.adapt(fProgrammersCombo, true, true);
		fProgrammersCombo.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.TOP));
		fProgrammersCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				getManagedForm().dirtyStateChanged();

				// Set the tooltip to the additional info.
				String programmerid = fMapDescToId.get(fProgrammersCombo.getText());
				IProgrammer programmer = fTCWC.getProgrammer(programmerid);
				fProgrammersCombo.setToolTipText(programmer.getAdditionalInfo());
				updateHostInterfaceCombo(programmer);
			}
		});

		fProgrammersWarningImageLabel = toolkit.createLabel(content, null);
		fProgrammersWarningImageLabel.setImage(PlatformUI.getWorkbench().getSharedImages()
				.getImage(ISharedImages.IMG_OBJS_ERROR_TSK));
		fProgrammersWarningImageLabel.setLayoutData(new TableWrapData(TableWrapData.LEFT,
				TableWrapData.MIDDLE));
		fProgrammersWarningImageLabel
				.setToolTipText("The selected Programmer is not supported by the image loader and/or the gdbserver");
		fProgrammersWarningImageLabel.setVisible(false);

		//
		// The host port selector combo
		//
		label = toolkit.createLabel(content, "Host interface:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));

		fHostPortCombo = new Combo(content, SWT.READ_ONLY);
		toolkit.adapt(fHostPortCombo, true, true);
		fHostPortCombo
				.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP, 1, 2));
		fHostPortCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				getManagedForm().dirtyStateChanged();
				// updateHostPort();
				// TODO update form for the new host port
			}
		});

		// Fill the combo with all values so that the layout is correct.
		// The actual entries are filled later when the setFormInput() is called.
		// update the combo to only show available interfaces
		HostInterface[] allhis = HostInterface.values();
		String[] allnames = new String[allhis.length];
		for (int i = 0; i < allhis.length; i++) {
			allnames[i] = allhis[i].toString();
		}
		fHostPortCombo.setItems(allnames);

		getSection().setClient(content);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#setFormInput(java.lang.Object)
	 */
	@Override
	public boolean setFormInput(Object input) {
		if (!(input instanceof ITargetConfigurationWorkingCopy)) {
			return false;
		}

		fTCWC = (ITargetConfigurationWorkingCopy) input;
		// fTCWC.addChangeListener(this);

		refresh();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		if (fTCWC == null) {
			return; // not initialized yet
		}

		// Get the list of valid Programmers and fill the description -> id map
		fMapDescToId.clear();
		List<IProgrammer> allprogrammers = fTCWC.getSupportedProgrammers(false);

		for (IProgrammer programmer : allprogrammers) {
			String description = programmer.getDescription();
			fMapDescToId.put(description, programmer.getId());
		}

		// Check if the currently selected programmer is still in the list
		String currentprogrammerid = fTCWC.getAttribute(ITargetConfiguration.ATTR_PROGRAMMER_ID);
		if (fMapDescToId.containsValue(currentprogrammerid)) {
			// Yes -- The selected Programmer is still supported.
			// Clear any warnings
			showWarning(false);
		} else {
			// No -- The Programmer is not supported by the current config.
			// show a warning
			showWarning(true);
		}

		// Get all descriptions and sort them alphabetically
		Set<String> descset = fMapDescToId.keySet();
		String[] alldescs = descset.toArray(new String[descset.size()]);
		Arrays.sort(alldescs, new Comparator<String>() {
			// Custom Comparator to ignore upper/lower case
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});

		// finally tell the fProgrammersCombo about the new list but keep the previously selected
		// Programmer
		fProgrammersCombo.setItems(alldescs);
		fProgrammersCombo.setVisibleItemCount(Math.min(alldescs.length, 25));

		IProgrammer currentprogrammer = fTCWC.getProgrammer(currentprogrammerid);
		fProgrammersCombo.setText(currentprogrammer.getDescription());
		fProgrammersCombo.setToolTipText(currentprogrammer.getAdditionalInfo());

		// Now set the host interface
		String hostinterface = fTCWC.getAttribute(ITargetConfiguration.ATTR_HOSTINTERFACE);
		HostInterface currHI;
		try {
			currHI = HostInterface.valueOf(HostInterface.class, hostinterface);
		} catch (IllegalArgumentException iae) {
			// This should not happen unless the preferences have been garbled.
			// Just in case we select something reasonable
			currHI = currentprogrammer.getHostInterfaces()[0];
		}
		fHostPortCombo.setText(currHI.toString());
		updateHostInterfaceCombo(currentprogrammer);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave) {
		String description = fProgrammersCombo.getText();

		String id = fMapDescToId.get(description);
		fTCWC.setAttribute(ITargetConfiguration.ATTR_PROGRAMMER_ID, id);

		description = fHostPortCombo.getText();
		HostInterface hi = fMapDescToHostPort.get(description);
		fTCWC.setAttribute(ITargetConfiguration.ATTR_HOSTINTERFACE, hi.name());

		super.commit(onSave);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		String description = fProgrammersCombo.getText();
		String id = fMapDescToId.get(description);
		if (!fTCWC.getAttribute(ITargetConfiguration.ATTR_PROGRAMMER_ID).equals(id)) {
			return true;
		}

		HostInterface currHI = fMapDescToHostPort.get(fHostPortCombo.getText());
		if (!fTCWC.getAttribute(ITargetConfiguration.ATTR_HOSTINTERFACE).equals(currHI.name())) {
			return true;
		}

		return super.isDirty();
	}

	/**
	 * @param visible
	 */
	private void showWarning(boolean visible) {
		fProgrammersWarningImageLabel.setVisible(visible);
	}

	/**
	 * @param programmer
	 */
	private void updateHostInterfaceCombo(IProgrammer programmer) {
		HostInterface[] availableHIs = programmer.getHostInterfaces();

		// update the combo to only show available interfaces
		fMapDescToHostPort.clear();
		for (HostInterface hi : availableHIs) {
			fMapDescToHostPort.put(hi.toString(), hi);
		}
		String[] allhostinterfaces = fMapDescToHostPort.keySet().toArray(
				new String[fMapDescToHostPort.size()]);
		fHostPortCombo.setItems(allhostinterfaces);

		// Check if the currently selected port is still valid
		HostInterface actualHI = fMapDescToHostPort.get(fHostPortCombo.getText());
		for (HostInterface hi : availableHIs) {
			if (hi.equals(actualHI)) {
				// The set port is valid. Nothing to do.
				return;
			}
		}

		// The selected programmer uses a different host interface.
		// Update the combo
		HostInterface newHI = availableHIs[0];
		String desc = newHI.toString();
		fHostPortCombo.setText(desc);

		// Change the host port settings section
		changeHostPortType(newHI);
	}

	/**
	 * @param hostinterface
	 */
	private void changeHostPortType(HostInterface hostinterface) {

	}

	private interface IHostPortSection {

		public HostInterface getInterface();
	}

}
