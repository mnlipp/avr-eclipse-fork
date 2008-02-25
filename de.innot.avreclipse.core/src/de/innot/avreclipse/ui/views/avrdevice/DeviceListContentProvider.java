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
package de.innot.avreclipse.ui.views.avrdevice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.innot.avreclipse.core.util.AVRMCUidConverter;
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
		Set<String> devicesset = dmprovider.getMCUList();
		if (devicesset == null) {
			// if the list is null, an internal Provider Error has occurred.
			String[] empty = {""};
			return empty;
		}
		
		// Convert to an List so that it can be sorted
		List<String>devices = new ArrayList<String>(devicesset);
		Collections.sort(devices);
		// Convert the IDs to names
		String[] nameslist = new String[devices.size()];
		int i= 0;
		for (String deviceid : devices) {
			nameslist[i] = AVRMCUidConverter.id2name(deviceid);
			i++;
		}
		return nameslist;
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
