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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.ui.PlatformUI;

/**
 * @author Thomas Holland
 * 
 */
public class URLColumnLabelProvider extends ColumnLabelProvider {

	private MCUProviderEnum fProvider;

	/** Color for the URL Link. Use DARK_BLUE to make it somewhat resemble a Link */
	private static Color fForeColor = PlatformUI.getWorkbench().getDisplay().getSystemColor(SWT.COLOR_BLUE);

	public URLColumnLabelProvider(MCUProviderEnum provider) {
		fProvider = provider;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getForeground(java.lang.Object)
	 */
	@Override
	public Color getForeground(Object element) {
		return fForeColor;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ColumnLabelProvider#getText(java.lang.Object)
	 */
	@Override
	public String getText(Object element) {
		Assert.isTrue(element instanceof String);
		
		String mcuid = (String) element;
		String urlstring = fProvider.getMCUInfo(mcuid);
		if (urlstring != null) {
			String filename = getFilenameFromURL(urlstring);
			return filename;
		}
		return "";
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.Object)
	 */
	@Override
	public String getToolTipText(Object element) {
		String mcuid = (String) element;
		String url = fProvider.getMCUInfo(mcuid);
		return url;
	}
	
	

	/* (non-Javadoc)
     * @see org.eclipse.jface.viewers.CellLabelProvider#getToolTipShift(java.lang.Object)
     */
    @Override
    public Point getToolTipShift(Object object) {
    	Point tooltipoffset = new Point (5,5);
    	return tooltipoffset;
    }

	private String getFilenameFromURL(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

}
