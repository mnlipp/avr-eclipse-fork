/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.ui.views.supportedmcu;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;

import de.innot.avreclipse.core.IMCUProvider;

/**
 * Intermediate class between the ContentProvider and the URLCellEditor.
 * <p>
 * This class does little more than supply the TableViewer with an
 * URLCellEditor. As an URLCellEditor is not a real Editor, there is need to
 * save anything.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class URLEditingSupport extends EditingSupport {

	private URLCellEditor fCellEditor;
	private IMCUProvider fProvider;

	public URLEditingSupport(TableViewer viewer, IMCUProvider provider) {
		super(viewer);

		fProvider = provider;

		fCellEditor = new URLCellEditor(viewer.getTable());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
	 */
	@Override
	protected boolean canEdit(Object element) {
		// Test is there is actually an URL for the mcuid
		return fProvider.hasMCU((String) element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
	 */
	@Override
	protected CellEditor getCellEditor(Object element) {
		return fCellEditor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
	 */
	@Override
	protected Object getValue(Object element) {

		String mcuinfo = fProvider.getMCUInfo((String) element);
		return mcuinfo;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object,
	 *      java.lang.Object)
	 */
	@Override
	protected void setValue(Object element, Object value) {
		// not a real editor, so nothing is stored
	}

}
