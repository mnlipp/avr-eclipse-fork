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
 * $Id: DeviceModelContentProvider.java 10 2007-11-25 22:42:29Z thomas $
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui.views.avrdevice;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.innot.avreclipse.devicedescription.IDeviceDescriptionProvider;

public class DeviceListContentProvider implements IStructuredContentProvider {

	private static IDeviceDescriptionProvider dmprovider = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		dmprovider = (IDeviceDescriptionProvider)newInput;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		List<String> devices = dmprovider.getDeviceList();
		if (devices == null) {
			// if the list is null, an internal Provider Error has occurred.
			String[] empty = {""};
			return empty;
		}
		return devices.toArray();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

}
