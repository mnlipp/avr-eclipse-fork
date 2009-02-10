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
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.HostInterface;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.ITargetConfigConstants;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class SectionProgrammer extends AbstractTargetConfigurationEditorPart implements
		ITargetConfigConstants {

	private Combo								fProgrammersCombo;

	private Combo								fHostPortCombo;

	/** Reverse mapping of programmer description to id. */
	final private Map<String, String>			fMapDescToId		= new HashMap<String, String>();

	/** Reverse mapping of host interface description to host interface. */
	final private Map<String, HostInterface>	fMapDescToHostPort	= new HashMap<String, HostInterface>();

	private final static String[]				PART_ATTRS			= new String[] {
			ATTR_PROGRAMMER_ID, ATTR_HOSTINTERFACE					};
	private final static String[]				PART_DEPENDS		= new String[] {
			ATTR_IMAGE_LOADER_ID, ATTR_GDBSERVER_ID				};

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getTitle()
	 */
	@Override
	protected String getTitle() {
		return "Programmer Hardware / Interface";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getDescription()
	 */
	@Override
	protected String getDescription() {
		return null; // TODO: add a description
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getPartAttributes
	 * ()
	 */
	@Override
	public String[] getPartAttributes() {
		return PART_ATTRS;
	}

	/*
	 * (non-Javadoc)
	 * @seede.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#
	 * getDependentAttributes()
	 */
	@Override
	String[] getDependentAttributes() {
		return PART_DEPENDS;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	public void createSectionContent(Composite parent, FormToolkit toolkit) {

		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		layout.horizontalSpacing = 12;
		parent.setLayout(layout);

		//
		// The Programmers Combo
		// 
		Label label = toolkit.createLabel(parent, "Programmer:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));

		fProgrammersCombo = new Combo(parent, SWT.READ_ONLY);
		toolkit.adapt(fProgrammersCombo, true, true);
		fProgrammersCombo.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
		fProgrammersCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ITargetConfigurationWorkingCopy tcwc = getTargetConfiguration();
				String description = fProgrammersCombo.getText();
				String id = fMapDescToId.get(description);
				IProgrammer programmer = tcwc.getProgrammer(id);

				tcwc.setAttribute(ATTR_PROGRAMMER_ID, id);
				fProgrammersCombo.setToolTipText(programmer.getAdditionalInfo());

				updateHostInterfaceCombo(programmer);

				getManagedForm().dirtyStateChanged();
			}
		});

		//
		// The host port selector combo
		//
		label = toolkit.createLabel(parent, "Host interface:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));

		fHostPortCombo = new Combo(parent, SWT.READ_ONLY);
		toolkit.adapt(fHostPortCombo, true, true);
		fHostPortCombo.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
		fHostPortCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String description = fHostPortCombo.getText();
				HostInterface hi = fMapDescToHostPort.get(description);
				getTargetConfiguration().setAttribute(ATTR_HOSTINTERFACE, hi.name());

				// Ensure that the HostPortCombo is still visible after the layout reflow caused
				// by the new layout
				IManagedForm form = getManagedForm();
				form.getForm().showControl(fProgrammersCombo);
				form.dirtyStateChanged();
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
	 */
	@Override
	public void refreshSectionContent() {

		// Get the list of valid Programmers and fill the description -> id map
		fMapDescToId.clear();
		List<IProgrammer> allprogrammers = getTargetConfiguration().getSupportedProgrammers(false);

		for (IProgrammer programmer : allprogrammers) {
			String description = programmer.getDescription();
			fMapDescToId.put(description, programmer.getId());
		}

		// Check if the currently selected programmer is still in the list
		String currentprogrammerid = getTargetConfiguration().getAttribute(ATTR_PROGRAMMER_ID);
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

		IProgrammer currentprogrammer = getTargetConfiguration().getProgrammer(currentprogrammerid);
		fProgrammersCombo.setText(currentprogrammer.getDescription());
		fProgrammersCombo.setToolTipText(currentprogrammer.getAdditionalInfo());

		// Now set the host interface
		updateHostInterfaceCombo(currentprogrammer);
	}

	/**
	 * @param visible
	 */
	private void showWarning(boolean visible) {
		if (visible) {
			String msg = "Selected Programmer is not supported by image loader and / or gdb server";
			getMessageManager().addMessage(fProgrammersCombo, msg, null, IMessageProvider.WARNING,
					fProgrammersCombo);
		} else {
			getMessageManager().removeMessage(fProgrammersCombo, fProgrammersCombo);
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
		String currentHI = getTargetConfiguration().getAttribute(ATTR_HOSTINTERFACE);
		for (HostInterface hi : availableHIs) {
			if (hi.name().equals(currentHI)) {
				// The set port is valid. Just set the name and be done
				fHostPortCombo.setText(hi.toString());
				return;
			}
		}

		// The selected programmer uses a different host interface.
		// Update the combo and the target configuration
		HostInterface newHI = availableHIs[0];
		fHostPortCombo.setText(newHI.toString());
		getTargetConfiguration().setAttribute(ATTR_HOSTINTERFACE, newHI.name());

	}

}
