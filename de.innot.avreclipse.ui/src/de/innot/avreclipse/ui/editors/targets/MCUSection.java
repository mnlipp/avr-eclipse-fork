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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;

import de.innot.avreclipse.core.targets.ITargetConfigConstants;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class MCUSection extends SectionPart implements ITargetConfigChangeListener,
		ITargetConfigConstants {

	private ITargetConfigurationWorkingCopy	fTCWC;

	private Combo							fMCUcombo;
	private Combo							fFCPUcombo;

	private Map<String, String>				fMCUList;
	private List<String>					fMCUNames;

	private String							fOldMCU;
	private int								fOldFCPU;

	/** List of common MCU frequencies (taken from mfile) */
	private static final String[]			FCPU_VALUES	= { "1000000", "1843200", "2000000",
			"3686400", "4000000", "7372800", "8000000", "11059200", "14745600", "16000000",
			"18432000", "20000000"						};

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public MCUSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.TITLE_BAR);

		getSection().setText("Target Processor");

		fMCUList = new HashMap<String, String>();
		fMCUNames = new ArrayList<String>();
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

		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 12;
		content.setLayout(layout);

		//
		// The MCU Combo
		// 
		toolkit.createLabel(content, "MCU type:");
		fMCUcombo = new Combo(content, SWT.READ_ONLY);
		toolkit.adapt(fMCUcombo, true, true);
		fMCUcombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false));
		fMCUcombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String mcuid = AVRMCUidConverter.name2id(fMCUcombo.getText());
				fTCWC.setMCU(mcuid);
				getManagedForm().dirtyStateChanged();
			}
		});

		//
		// The FCPU Combo
		//
		toolkit.createLabel(content, "MCU clock frequency:");
		fFCPUcombo = new Combo(content, SWT.NONE);
		toolkit.adapt(fFCPUcombo, true, true);
		fFCPUcombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false));
		fFCPUcombo.setTextLimit(9); // max. 999 MHz
		fFCPUcombo.setToolTipText("Target Hardware Clock Frequency in Hz");
		fFCPUcombo.setVisibleItemCount(FCPU_VALUES.length);
		fFCPUcombo.setItems(FCPU_VALUES);

		fFCPUcombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fTCWC.setFCPU(Integer.parseInt(fFCPUcombo.getText()));
				getManagedForm().dirtyStateChanged();
			}
		});

		// The verify listener to restrict the input to integers
		fFCPUcombo.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				String text = event.text;
				if (!text.matches("[0-9]*")) {
					event.doit = false;
				}
			}
		});

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

		// Add a listener for attribute changes.
		// If either the image loader or the gdbserver is changed then
		// mark the form as stale which will cause a call to refresh() which
		// in turn will check if the mcu is still valid
		fTCWC.addPropertyChangeListener(this);

		fOldMCU = fTCWC.getMCUId();
		fOldFCPU = fTCWC.getFCPU();

		refresh();

		return true;
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

		// Get the list of valid MCUs, sort them, convert to MCU name and fill the internal cache
		fMCUList.clear();
		fMCUNames.clear();
		List<String> allmcuids = fTCWC.getSupportedMCUs(false);
		Collections.sort(allmcuids);

		for (String mcuid : allmcuids) {
			String name = AVRMCUidConverter.id2name(mcuid);
			fMCUList.put(mcuid, name);
			fMCUNames.add(name);
		}

		// Check if the currently selected mcu is still in the list
		String currentmcu = fTCWC.getMCUId();
		if (fMCUList.containsKey(currentmcu)) {
			// Yes -- The selected MCU is still supported.
			// Clear any warnings
			showMCUWarning(false);
		} else {
			// No -- The MCU is not supported by the current config.
			// show a warning
			showMCUWarning(true);
		}

		// finally tell the fMCUCombo about the new list but keep the previously selected MCU
		fMCUcombo.setItems(fMCUNames.toArray(new String[fMCUNames.size()]));
		fMCUcombo.setVisibleItemCount(Math.min(fMCUNames.size(), 20));

		String currentMCUName = AVRMCUidConverter.id2name(fTCWC.getMCUId());
		fMCUcombo.setText(currentMCUName);

		// For the FCPU we can take the value directly from the target configuration.
		fFCPUcombo.setText(Integer.toString(fTCWC.getFCPU()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (!fTCWC.getMCUId().equals(fOldMCU)) {
			return true;
		}
		if (!(fTCWC.getFCPU() == fOldFCPU)) {
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

		// But remember the current MCU / FCPU for the
		// dirty state tracking
		fOldMCU = fTCWC.getMCUId();
		fOldFCPU = fTCWC.getFCPU();

		super.commit(onSave);
	}

	private void showMCUWarning(boolean visible) {
		if (visible) {
			String msg = MessageFormat.format("MCU {0} is not supported by the selected tools",
					fMCUcombo.getText());
			getManagedForm().getMessageManager().addMessage(fMCUcombo, msg, null,
					IMessageProvider.ERROR, fMCUcombo);
		} else {
			getManagedForm().getMessageManager().removeMessage(fMCUcombo, fMCUcombo);
		}

	}
}
