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
package de.innot.avreclipse.ui.editors;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;

/**
 * An <code>IEditorPart</code> to edit a <code>ByteValues</code> object.
 * <p>
 * The editor consists of three <code>IFormParts</code>:
 * <ul>
 * <li>The title part which will always display the current MCU in the form title.</li>
 * <li>The BitField sections part to edit the bitfields.</li>
 * <li>The comment sectionpart to edit the comment.</li>
 * </ul>
 * In addition to these three parts some actions are added to the form toolbar (shown to the right
 * of the title).
 * </p>
 * 
 * @see FusesEditor
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class FuseByteEditorPage extends FormPage {

	/** The current <code>ByteValues</code> that this editor works with. */
	private ByteValues	fByteValues;

	/**
	 * @param editor
	 *            The parent Editor. Must be a {@link FusesEditor}.
	 * @param id
	 *            The id of this page.
	 * @param title
	 *            The title of this editor, shown in the bottom tab of this editor.
	 */
	public FuseByteEditorPage(FusesEditor editor, String id, String title) {
		super(editor, id, title);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormPage#initialize(org.eclipse.ui.forms.editor.FormEditor)
	 */
	@Override
	public void initialize(FormEditor editor) {
		if (editor instanceof FusesEditor) {
			FusesEditor parent = (FusesEditor) editor;
			fByteValues = parent.getByteValuesFromInput();
		}
		super.initialize(editor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {

		// Add the toolbar actions
		managedForm.addPart(new MCUTypeActionPart());
		managedForm.addPart(new MCUReadActionPart());
		managedForm.addPart(new MCUDefaultsActionPart());

		fillBody(managedForm);

		managedForm.setInput(fByteValues);
	}

	/**
	 * Fill the managed Form.
	 * <p>
	 * This method will add three parts to the form:
	 * <ul>
	 * <li>The title part which will always display the current MCU in the form title.</li>
	 * <li>The BitField sections part to edit the bitfields.</li>
	 * <li>The comment sectionpart to edit the comment.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param managedForm
	 * @param toolkit
	 */
	private void fillBody(IManagedForm managedForm) {

		Composite body = managedForm.getForm().getBody();
		FormToolkit toolkit = managedForm.getToolkit();

		body.setLayout(new TableWrapLayout());

		// Add a part that will update the form title to the latest MCU.
		ByteValuesTitlePart titlepart = new ByteValuesTitlePart();
		managedForm.addPart(titlepart);

		// The main section has all BitField sections
		Composite main = toolkit.createComposite(body);
		main.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		managedForm.addPart(new ByteValuesMainPart(main, fByteValues));

		// The comments section is separate to cover all columns
		Composite comment = toolkit.createComposite(body);
		comment.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		comment.setLayout(new FillLayout());
		ByteValuesCommentPart commentpart = new ByteValuesCommentPart(comment, toolkit,
				Section.TITLE_BAR | Section.TWISTIE | Section.EXPANDED);
		managedForm.addPart(commentpart);

	}

}
