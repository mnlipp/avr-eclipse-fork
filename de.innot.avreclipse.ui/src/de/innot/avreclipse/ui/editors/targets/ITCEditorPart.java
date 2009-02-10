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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IMessageManager;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public interface ITCEditorPart extends IFormPart {

	/**
	 * Returns a list of attributes that are managed by this form part.
	 * <p>
	 * This list is used to associate attributes with the form part that edits them. Internally the
	 * list is also used to manage the dirty state of the form part.
	 * </p>
	 * 
	 * @return Array with attributes. May be empty but never <code>null</code>
	 */
	public abstract String[] getPartAttributes();

	public void setParent(Composite parent);

	public void setMessageManager(IMessageManager manager);

	public Control getControl();

	public boolean setFocus(String attribute);

}