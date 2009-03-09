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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class BasePage extends FormPage {

	final List<ITCEditorPart>				fParts	= new ArrayList<ITCEditorPart>();

	final private SharedHeaderFormEditor	fEditor;

	/**
	 * @param editor
	 * @param id
	 * @param title
	 */
	public BasePage(SharedHeaderFormEditor editor, String id, String title) {
		super(editor, id, title);

		fEditor = editor;
	}

	protected void registerPart(ITCEditorPart part) {
		fParts.add(part);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#getEditor()
	 */
	@Override
	public SharedHeaderFormEditor getEditor() {
		return fEditor;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.editor.FormPage#selectReveal(java.lang.Object)
	 */
	@Override
	public boolean selectReveal(Object object) {
		if (object instanceof String) {
			String attribute = (String) object;
			for (ITCEditorPart part : fParts) {
				if (part.setFocus(attribute)) {
					// found a part that knows the attribute
					fEditor.setActivePage(getId());
					// do the set focus again because setActivePage() changes the focus
					part.setFocus(attribute);
					return true;
				}
			}
		}
		return false;
	}

	public IMessageManager getMessageManager() {
		return fEditor.getHeaderForm().getMessageManager();
	}

}