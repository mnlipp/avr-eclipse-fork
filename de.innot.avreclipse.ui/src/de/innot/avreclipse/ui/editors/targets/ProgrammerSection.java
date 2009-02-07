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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.HostInterface;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.ITargetConfigConstants;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class ProgrammerSection extends SectionPart implements ITargetConfigChangeListener,
		ITargetConfigConstants {

	private ITargetConfigurationWorkingCopy	fTCWC;

	private Composite						fSectionClient;
	private Combo							fProgrammersCombo;

	private Combo							fHostPortCombo;

	private SectionPart						fHostInterfaceSection;
	private SectionPart						fTargetInterfaceSection;

	/** Reverse mapping of programmer description to id. */
	private Map<String, String>				fMapDescToId;

	/** Reverse mapping of host interface description to host interface. */
	private Map<String, HostInterface>		fMapDescToHostPort;

	// Remember the last saved name / description to determine if this
	// part is actually dirty.
	private String							fOldProgrammerId;
	private String							fOldHostInterface;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public ProgrammerSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.TITLE_BAR);

		fMapDescToId = new HashMap<String, String>();
		fMapDescToHostPort = new HashMap<String, HostInterface>();

		getSection().setText("Programmer Hardware / Interface");

		fSectionClient = toolkit.createComposite(getSection());
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 12;
		fSectionClient.setLayout(layout);

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);

		FormToolkit toolkit = form.getToolkit();
		//
		// The Programmers Combo
		// 
		Label label = toolkit.createLabel(fSectionClient, "Programmer:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));

		fProgrammersCombo = new Combo(fSectionClient, SWT.READ_ONLY);
		toolkit.adapt(fProgrammersCombo, true, true);
		fProgrammersCombo.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
		fProgrammersCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String description = fProgrammersCombo.getText();
				String id = fMapDescToId.get(description);
				IProgrammer programmer = fTCWC.getProgrammer(id);

				fTCWC.setAttribute(ATTR_PROGRAMMER_ID, id);
				fProgrammersCombo.setToolTipText(programmer.getAdditionalInfo());

				IManagedForm form = getManagedForm();
				ScrollBar sb = getManagedForm().getForm().getVerticalBar();
				int lastScrollbarPosition = sb != null ? sb.getSelection() : 0;
				updateHostInterfaceCombo(programmer);

				// Ensure that the ProgrammerCombo is still visible after the layout reflow caused
				// by the new layout

				sb = form.getForm().getVerticalBar();
				if (sb != null) {
					sb.setSelection(lastScrollbarPosition);
				}
				// form.getForm().showControl(getSection());
				// form.getForm().showControl(fProgrammersCombo);
				form.dirtyStateChanged();
			}
		});

		//
		// The host port selector combo
		//
		label = toolkit.createLabel(fSectionClient, "Host interface:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));

		fHostPortCombo = new Combo(fSectionClient, SWT.READ_ONLY);
		toolkit.adapt(fHostPortCombo, true, true);
		fHostPortCombo.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
		fHostPortCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String description = fHostPortCombo.getText();
				HostInterface hi = fMapDescToHostPort.get(description);
				fTCWC.setAttribute(ATTR_HOSTINTERFACE, hi.name());

				// Ensure that the HostPortCombo is still visible after the layout reflow caused
				// by the new layout
				IManagedForm form = getManagedForm();
				form.getForm().showControl(fProgrammersCombo);
				form.dirtyStateChanged();
			}
		});

		toolkit.createLabel(fSectionClient, null); // Dummy to fill the first column
		fHostInterfaceSection = new HostInterfaceSettingsPart(fSectionClient, toolkit);
		fHostInterfaceSection.getSection().setLayoutData(
				new TableWrapData(TableWrapData.FILL, TableWrapData.TOP, 1, 1));
		getManagedForm().addPart(fHostInterfaceSection);

		toolkit.createLabel(fSectionClient, null); // Dummy to fill the first column
		fTargetInterfaceSection = new TargetInterfaceSettingsPart(fSectionClient, toolkit);
		fTargetInterfaceSection.getSection().setLayoutData(
				new TableWrapData(TableWrapData.FILL, TableWrapData.TOP, 1, 1));
		getManagedForm().addPart(fTargetInterfaceSection);

		getSection().setClient(fSectionClient);
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

		// Add a listener for attribute changes.
		// If either the image loader or the gdbserver is changed then
		// mark the form as stale which will cause a call to refresh() which
		// in turn will check if the programmer is still valid
		fTCWC.addPropertyChangeListener(this);

		fOldProgrammerId = fTCWC.getAttribute(ATTR_PROGRAMMER_ID);
		fOldHostInterface = fTCWC.getAttribute(ATTR_HOSTINTERFACE);

		refresh();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (!fTCWC.getAttribute(ATTR_PROGRAMMER_ID).equals(fOldProgrammerId)) {
			return true;
		}

		if (!fTCWC.getAttribute(ATTR_HOSTINTERFACE).equals(fOldHostInterface)) {
			return true;
		}

		return super.isDirty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave) {
		// The actual saving is done somewhere upstream.

		// But remember the current settings for the
		// dirty state tracking
		fOldProgrammerId = fTCWC.getAttribute(ATTR_PROGRAMMER_ID);
		fOldHostInterface = fTCWC.getAttribute(ATTR_HOSTINTERFACE);

		super.commit(onSave);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
	 */
	@Override
	public void dispose() {
		// remove the listener
		fTCWC.removePropertyChangeListener(this);
		super.dispose();
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
		String currentprogrammerid = fTCWC.getAttribute(ATTR_PROGRAMMER_ID);
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
		updateHostInterfaceCombo(currentprogrammer);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener#attributeChange
	 * (de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	public void attributeChange(ITargetConfiguration config, String attribute, String oldvalue,
			String newvalue) {
		// Check if the image loader or the gdbserver have changed
		if (ATTR_IMAGE_LOADER_ID.equals(attribute) || ATTR_GDBSERVER_ID.equals(attribute)) {
			markStale();
		}
	}

	/**
	 * @param visible
	 */
	private void showWarning(boolean visible) {
		if (visible) {
			String msg = "Selected Programmer is not supported by image loader and / or gdb server";
			getManagedForm().getMessageManager().addMessage(fProgrammersCombo, msg, null,
					IMessageProvider.WARNING, fProgrammersCombo);
		} else {
			getManagedForm().getMessageManager()
					.removeMessage(fProgrammersCombo, fProgrammersCombo);
		}
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
		String actualHI = fTCWC.getAttribute(ATTR_HOSTINTERFACE);
		for (HostInterface hi : availableHIs) {
			if (hi.name().equals(actualHI)) {
				// The set port is valid. Just set the name and be done
				fHostPortCombo.setText(hi.toString());
				return;
			}
		}

		// The selected programmer uses a different host interface.
		// Update the combo and the target configuration
		HostInterface newHI = availableHIs[0];
		fHostPortCombo.setText(newHI.toString());
		fTCWC.setAttribute(ATTR_HOSTINTERFACE, newHI.name());

		// Change the host port settings section
		changeHostPortType(newHI);
	}

	/**
	 * @param hostinterface
	 */
	private void changeHostPortType(HostInterface hostinterface) {

		getManagedForm().getForm().reflow(true);
	}

}
