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
 * $Id$
 *     
 *******************************************************************************/

package de.innot.avreclipse.ui.editors.targets;

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class MainPage extends FormPage {

	final private ITargetConfigurationWorkingCopy	fTCWC;

	/**
	 * @param editor
	 * @param id
	 * @param title
	 */
	public MainPage(FormEditor editor, String id, String title) {
		super(editor, id, title);
		// TODO Auto-generated constructor stub
		IEditorInput ei = editor.getEditorInput();

		fTCWC = (ITargetConfigurationWorkingCopy) ei
				.getAdapter(ITargetConfigurationWorkingCopy.class);

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {

		// fill the form
		fillBody(managedForm);

		managedForm.setInput(fTCWC);

	}

	/**
	 * Fill the managed Form.
	 * <p>
	 * This method will the following parts to the form:
	 * <ul>
	 * <li>The general section with name and description.</li>
	 * <li>The target MCU and its clock freq.</li>
	 * <li>...</li>
	 * </ul>
	 * </p>
	 * 
	 * @param managedForm
	 */
	private void fillBody(IManagedForm managedForm) {

		Composite body = managedForm.getForm().getBody();
		FormToolkit toolkit = managedForm.getToolkit();

		body.setLayout(new TableWrapLayout());

		// Add a part that will update the form title to the current MCU.
		NamePart namepart = new NamePart();
		managedForm.addPart(namepart);

		// // The main section has all BitField sections
		// Composite main = toolkit.createComposite(body);
		// main.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// managedForm.addPart(new ByteValuesMainPart(main, fByteValues));
		//
		// // The comments section is separate to cover all columns
		// Composite comment = toolkit.createComposite(body);
		// comment.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		// comment.setLayout(new FillLayout());
		// ByteValuesCommentPart commentpart = new ByteValuesCommentPart(comment, toolkit,
		// Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		// managedForm.addPart(commentpart);

	}

	private class NamePart extends AbstractFormPart {

		private ITargetConfigurationWorkingCopy	fTCWC;

		private Text							fNameText;
		private Text							fDescriptionText;

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.ui.forms.AbstractFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
		 */
		@Override
		public void initialize(IManagedForm form) {
			super.initialize(form);

			Composite body = form.getForm().getBody();
			FormToolkit toolkit = form.getToolkit();

			TableWrapLayout layout = new TableWrapLayout();
			layout.numColumns = 2;

			body.setLayout(layout);
			body.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			toolkit.createLabel(body, "Name:");
			fNameText = toolkit.createText(body, "");
			fNameText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			fNameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getManagedForm().dirtyStateChanged();
				}
			});

			toolkit.createLabel(body, "Description:");
			fDescriptionText = toolkit.createText(body, "");
			fDescriptionText.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			fDescriptionText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getManagedForm().dirtyStateChanged();
				}
			});

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
}
