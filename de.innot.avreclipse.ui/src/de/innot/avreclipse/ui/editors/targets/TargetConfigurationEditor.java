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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.FormPage;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TargetConfigurationEditor extends FormEditor {

	private ITargetConfigurationWorkingCopy	fWorkingCopy;

	private FormPage						fMainPage;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#init(org.eclipse.ui.IEditorSite,
	 * org.eclipse.ui.IEditorInput)
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof TCEditorInput)) {
			throw new PartInitException("Invalid Input: Must be an AVR Target Configuration");
		}

		super.init(site, editorInput);

		// Use the name of the Configuration as a part name
		setPartName(editorInput.getName());

		// Description is not required as it should be obvious to the user what he is editing.
		// setContentDescription("Edit Target Configuration");

		// Get a working copy of the target configuration which will be the model for the actual
		// pages.
		fWorkingCopy = (ITargetConfigurationWorkingCopy) getEditorInput().getAdapter(
				ITargetConfigurationWorkingCopy.class);
		if (fWorkingCopy == null) {
			throw new PartInitException("Could not create a editable target configuration object");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		try {
			fMainPage = new MainPage(this, "id", "Main");
			addPage(fMainPage);
		} catch (PartInitException e) {
			//
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {

		try {
			fMainPage.doSave(monitor);

			fWorkingCopy.doSave();

			firePropertyChange(PROP_DIRTY);

			// Update the part title (in case the target configuration name has been changed)
			setPartName(fWorkingCopy.getName());

		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#isSaveAsAllowed()
	 */
	@Override
	public boolean isSaveAsAllowed() {
		// TargetConfigurations can not be saved under a different name.
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		// Save as not supported for target configurations
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormEditor#createPages()
	 */
	@Override
	protected void createPages() {
		// We only have a single page in the editor and don't want the little tab at the bottom.
		// So we just set the height of the tab to 0.
		super.createPages();
		if (getPageCount() == 1 && (getContainer() instanceof CTabFolder)) {
			((CTabFolder) getContainer()).setTabHeight(0);
		}
	}

}
