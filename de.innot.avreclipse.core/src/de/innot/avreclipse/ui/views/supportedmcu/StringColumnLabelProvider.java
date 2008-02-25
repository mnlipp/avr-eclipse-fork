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
package de.innot.avreclipse.ui.views.supportedmcu;

import org.eclipse.jface.viewers.ColumnLabelProvider;

/**
 * @author U043192
 * 
 */
public class StringColumnLabelProvider extends ColumnLabelProvider {

	private MCUProviderEnum fProvider = null;

	public StringColumnLabelProvider(MCUProviderEnum provider) {
		fProvider = provider;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {

		String mcuid = (String) element;
		String info = fProvider.getMCUInfo(mcuid);

		return info != null ? info : "n/a";
	}
}
