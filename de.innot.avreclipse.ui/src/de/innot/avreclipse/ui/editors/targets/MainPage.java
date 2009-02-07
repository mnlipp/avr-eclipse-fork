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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.forms.HyperlinkSettings;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
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

		// Set up the header
		ScrolledForm form = managedForm.getForm();
		// form.setImage();
		form.setText("Target Configuration");
		FormToolkit toolkit = managedForm.getToolkit();
		toolkit.decorateFormHeading(form.getForm());
		toolkit.getHyperlinkGroup().setHyperlinkUnderlineMode(HyperlinkSettings.UNDERLINE_HOVER);

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

		FormToolkit toolkit = managedForm.getToolkit();
		Composite body = managedForm.getForm().getBody();
		body.setLayout(new TableWrapLayout());

		GeneralSection gensection = new GeneralSection(body, toolkit);
		gensection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		managedForm.addPart(gensection);

		MCUSection mcusection = new MCUSection(body, toolkit);
		mcusection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		managedForm.addPart(mcusection);

		ProgrammerSection programmersection = new ProgrammerSection(body, toolkit);
		programmersection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		managedForm.addPart(programmersection);

		ImageLoaderSection imageloadersection = new ImageLoaderSection(body, toolkit);
		imageloadersection.getSection().setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		managedForm.addPart(imageloadersection);

	}
}
