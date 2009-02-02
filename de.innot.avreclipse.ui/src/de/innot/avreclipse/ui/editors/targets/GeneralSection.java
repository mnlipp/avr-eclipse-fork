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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class GeneralSection extends SectionPart {

	private ITargetConfigurationWorkingCopy	fTCWC;

	private Text							fNameText;
	private Text							fDescriptionText;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public GeneralSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.TITLE_BAR | Section.DESCRIPTION);

		getSection().setText("General");
		getSection().setDescription(
				"The name of this target configuration and an optional description.");
		getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
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

		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;

		content.setLayout(layout);
		content.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Label label = toolkit.createLabel(content, "Name:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));
		fNameText = toolkit.createText(content, "");
		fNameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		fNameText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getManagedForm().dirtyStateChanged();
			}
		});

		label = toolkit.createLabel(content, "Description:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));
		fDescriptionText = toolkit.createText(content, "");
		fDescriptionText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		fDescriptionText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				getManagedForm().dirtyStateChanged();
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

		fNameText.setText(fTCWC.getName());
		fDescriptionText.setText(fTCWC.getDescription());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave) {
		super.commit(onSave);
		fTCWC.setName(fNameText.getText());
		fTCWC.setDescription(fDescriptionText.getText());
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (!fTCWC.getName().equals(fNameText.getText())) {
			return true;
		}
		if (!fTCWC.getDescription().equals(fDescriptionText.getText())) {
			return true;
		}
		return super.isDirty();
	}
}
