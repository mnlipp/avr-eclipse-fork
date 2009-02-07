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
public class ImageLoaderSection extends SectionPart {

	private ITargetConfigurationWorkingCopy	fTCWC;

	private Text							fNameText;
	private Text							fDescriptionText;

	// Remember the last saved name / description to determine if this
	// part is actually dirty.
	private String							fOldName;
	private String							fOldDescription;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public ImageLoaderSection(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.TITLE_BAR | Section.DESCRIPTION);

		getSection().setText("Upload tool");
		getSection()
				.setDescription(
						"The upload tool is used to program the flash / eeprom / fuses / lockbits of the target MCU.");
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
				fTCWC.setName(fNameText.getText());
				getManagedForm().dirtyStateChanged();
			}
		});

		label = toolkit.createLabel(content, "Description:");
		label.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.MIDDLE));
		fDescriptionText = toolkit.createText(content, "");
		fDescriptionText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		fDescriptionText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fTCWC.setDescription(fDescriptionText.getText());
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

		fOldName = fTCWC.getName();
		fOldDescription = fTCWC.getDescription();

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
	 * @see org.eclipse.ui.forms.AbstractFormPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (!fTCWC.getName().equals(fOldName)) {
			return true;
		}
		if (!fTCWC.getDescription().equals(fOldDescription)) {
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

		// But remember the current name / description for the
		// dirty state tracking
		fOldName = fTCWC.getName();
		fOldDescription = fTCWC.getDescription();

		super.commit(onSave);
	}

}
