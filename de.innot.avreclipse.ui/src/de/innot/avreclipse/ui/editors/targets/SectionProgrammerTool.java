/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
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
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.IProgrammerTool;
import de.innot.avreclipse.core.targets.ITargetConfigConstants;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.ToolManager;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class SectionProgrammerTool extends AbstractTCSectionPart implements ITargetConfigConstants {

	private Combo						fProgrammerToolCombo;

	/** Reverse mapping of programmer tool name to id. */
	final private Map<String, String>	fMapNameToId	= new HashMap<String, String>();

	private final static String[]		PART_ATTRS		= new String[] { ATTR_PROGRAMMER_TOOL_ID };
	private final static String[]		PART_DEPENDS	= new String[] { ATTR_MCU };

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getTitle()
	 */
	@Override
	protected String getTitle() {
		return "Programmer Tool";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getDescription()
	 */
	@Override
	protected String getDescription() {
		return "The tool used to program the flash / eeprom / fuses / lockbits of the target MCU.";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getPartAttributes
	 * ()
	 */
	@Override
	public String[] getPartAttributes() {
		return Arrays.copyOf(PART_ATTRS, PART_ATTRS.length);
	}

	/*
	 * (non-Javadoc)
	 * @seede.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#
	 * getDependentAttributes()
	 */
	@Override
	protected String[] getDependentAttributes() {
		return Arrays.copyOf(PART_DEPENDS, PART_DEPENDS.length);
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTCSectionPart#setFocus(java.lang.String)
	 */
	@Override
	public boolean setFocus(String attribute) {
		if (ATTR_PROGRAMMER_TOOL_ID.equals(attribute)) {
			fProgrammerToolCombo.setFocus();
			return true;
		}

		return false;
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
		// The Programmer Tool Combo
		// 
		Label label = toolkit.createLabel(parent, "Programmer Tool:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));

		fProgrammerToolCombo = new Combo(parent, SWT.READ_ONLY);
		toolkit.adapt(fProgrammerToolCombo, true, true);
		fProgrammerToolCombo
				.setLayoutData(new TableWrapData(TableWrapData.LEFT, TableWrapData.TOP));
		fProgrammerToolCombo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				ITargetConfigurationWorkingCopy tcwc = getTargetConfiguration();
				String name = fProgrammerToolCombo.getText();
				String id = fMapNameToId.get(name);

				tcwc.setProgrammerTool(id);

				getManagedForm().dirtyStateChanged();
			}
		});

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
	 */
	@Override
	public void refreshSectionContent() {

		ToolManager manager = ToolManager.getDefault();
		ITargetConfigurationWorkingCopy wc = getTargetConfiguration();

		// Get the list of Programmer Tools and fill the name -> id map
		fMapNameToId.clear();
		List<String> alltoolids = ToolManager.getDefault().getAllTools(
				ToolManager.AVRPROGRAMMERTOOL);

		for (String id : alltoolids) {
			String name = manager.getToolName(id);
			fMapNameToId.put(name, id);
		}

		// Sort the tools alphabetically
		Set<String> nameset = fMapNameToId.keySet();
		String[] allnames = nameset.toArray(new String[nameset.size()]);
		Arrays.sort(allnames, new Comparator<String>() {
			// Custom Comparator to ignore upper/lower case
			public int compare(String o1, String o2) {
				return o1.compareToIgnoreCase(o2);
			}
		});

		// Get the id of the currently selected tool (or the default tool if no tool
		// has been set in the hardware configuration.
		IProgrammerTool currenttool = wc.getProgrammerTool();
		String currentid = currenttool != null ? currenttool.getId() : DEF_GDBSERVER_ID;

		// finally tell the fProgrammerToolsCombo about the new list but keep the previously
		// selected Programmer Tool
		fProgrammerToolCombo.setItems(allnames);
		fProgrammerToolCombo.setVisibleItemCount(Math.min(allnames.length, 25));

		fProgrammerToolCombo.setText(manager.getToolName(currentid));
	}

}
