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
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.innot.avreclipse.core.targets.ITargetConfigConstants;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class SectionMCU extends AbstractTargetConfigurationEditorPart implements
		ITargetConfigConstants {

	private Combo						fMCUcombo;
	private Combo						fFCPUcombo;

	final private Map<String, String>	fMCUList		= new HashMap<String, String>();
	final private List<String>			fMCUNames		= new ArrayList<String>();

	private final static String[]		PART_ATTRS		= new String[] { ATTR_MCU, ATTR_FCPU };
	private final static String[]		PART_DEPENDS	= new String[] { ATTR_LOADER_TOOL_ID,
			ATTR_GDBSERVER_ID							};

	/** List of common MCU frequencies (taken from mfile) */
	private static final String[]		FCPU_VALUES		= { "1000000", "1843200", "2000000",
			"3686400", "4000000", "7372800", "8000000", "11059200", "14745600", "16000000",
			"18432000", "20000000"						};

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getTitle()
	 */
	@Override
	protected String getTitle() {
		return "Target Processor";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getDescription()
	 */
	@Override
	protected String getDescription() {
		return "The name of this target configuration and an optional description.";
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
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#createSectionContent
	 * (org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createSectionContent(Composite parent, FormToolkit toolkit) {
		GridLayout layout = new GridLayout(2, false);
		layout.horizontalSpacing = 12;
		parent.setLayout(layout);

		//
		// The MCU Combo
		// 
		toolkit.createLabel(parent, "MCU type:");
		fMCUcombo = new Combo(parent, SWT.READ_ONLY);
		toolkit.adapt(fMCUcombo, true, true);
		fMCUcombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		fMCUcombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String mcuid = AVRMCUidConverter.name2id(fMCUcombo.getText());
				getTargetConfiguration().setMCU(mcuid);
				getManagedForm().dirtyStateChanged();
			}
		});

		//
		// The FCPU Combo
		//
		toolkit.createLabel(parent, "MCU clock frequency:");
		fFCPUcombo = new Combo(parent, SWT.NONE);
		toolkit.adapt(fFCPUcombo, true, true);
		fFCPUcombo.setLayoutData(new GridData(SWT.FILL, SWT.NONE, false, false));
		fFCPUcombo.setTextLimit(9); // max. 999 MHz
		fFCPUcombo.setToolTipText("Target Hardware Clock Frequency in Hz");
		fFCPUcombo.setVisibleItemCount(FCPU_VALUES.length);
		fFCPUcombo.setItems(FCPU_VALUES);

		fFCPUcombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetConfiguration().setFCPU(Integer.parseInt(fFCPUcombo.getText()));
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
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#updateSectionContent
	 * ()
	 */
	@Override
	protected void refreshSectionContent() {
		// Get the list of valid MCUs, sort them, convert to MCU name and fill the internal cache
		fMCUList.clear();
		fMCUNames.clear();
		List<String> allmcuids = getTargetConfiguration().getSupportedMCUs(false);
		Collections.sort(allmcuids);

		for (String mcuid : allmcuids) {
			String name = AVRMCUidConverter.id2name(mcuid);
			fMCUList.put(mcuid, name);
			fMCUNames.add(name);
		}

		// Check if the currently selected mcu is still in the list
		String currentmcu = getTargetConfiguration().getMCU();
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

		String currentMCUName = AVRMCUidConverter.id2name(getTargetConfiguration().getMCU());
		fMCUcombo.setText(currentMCUName);

		// For the FCPU we can take the value directly from the target configuration.
		fFCPUcombo.setText(Integer.toString(getTargetConfiguration().getFCPU()));
	}

	private void showMCUWarning(boolean visible) {
		if (visible) {
			String msg = MessageFormat.format("MCU {0} is not supported by the selected tools",
					fMCUcombo.getText());
			getMessageManager().addMessage(fMCUcombo, msg, ATTR_MCU, IMessageProvider.ERROR,
					fMCUcombo);
		} else {
			getMessageManager().removeMessage(fMCUcombo, fMCUcombo);
		}

	}
}
