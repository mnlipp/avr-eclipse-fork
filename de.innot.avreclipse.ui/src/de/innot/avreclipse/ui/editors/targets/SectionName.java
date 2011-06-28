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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;

import de.innot.avreclipse.core.targets.ITargetConfigConstants;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class SectionName extends AbstractTCSectionPart implements
		ITargetConfigConstants {

	private Text					fNameText;
	private Text					fDescriptionText;

	private final static String[]	PART_ATTRS	= new String[] { ATTR_NAME, ATTR_DESCRIPTION };

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getTitle()
	 */
	@Override
	protected String getTitle() {
		return "General";
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
		return Arrays.copyOf(PART_ATTRS, PART_ATTRS.length);
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

		toolkit.createLabel(parent, "Name:");
		fNameText = toolkit.createText(parent, "");
		fNameText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetConfiguration().setName(fNameText.getText());
				getManagedForm().dirtyStateChanged();
			}
		});

		toolkit.createLabel(parent, "Description:");
		fDescriptionText = toolkit.createText(parent, "");
		fDescriptionText.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fDescriptionText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getTargetConfiguration().setDescription(fDescriptionText.getText());
				getManagedForm().dirtyStateChanged();
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
		fNameText.setText(getTargetConfiguration().getName());
		fDescriptionText.setText(getTargetConfiguration().getDescription());
	}
}
