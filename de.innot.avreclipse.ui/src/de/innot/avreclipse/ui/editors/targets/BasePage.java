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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.editor.SharedHeaderFormEditor;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class BasePage extends FormPage {

	final Map<String, ITCEditorPart>		fManagedAttributes	= new HashMap<String, ITCEditorPart>();

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
		// Get the attributes of the part and add them to our internal list
		String[] attributes = part.getPartAttributes();

		for (String attr : attributes) {
			fManagedAttributes.put(attr, part);
		}
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
			ITCEditorPart part = fManagedAttributes.get(attribute);
			if (part != null) {
				part.setFocus(attribute);
				return true;
			}
		}
		return false;
	}

	public IMessageManager getMessageManager() {
		return fEditor.getHeaderForm().getMessageManager();
	}

}