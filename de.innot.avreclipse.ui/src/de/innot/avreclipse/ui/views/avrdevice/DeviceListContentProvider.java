/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.ui.views.avrdevice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;

import de.innot.avreclipse.core.util.AVRMCUidConverter;
import de.innot.avreclipse.devicedescription.IDeviceDescriptionProvider;

public class DeviceListContentProvider implements IStructuredContentProvider {

	private IDeviceDescriptionProvider	fDMprovider	= null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
	 *      java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fDMprovider = (IDeviceDescriptionProvider) newInput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		Set<String> devicesset = null;
		try {
			devicesset = fDMprovider.getMCUList();
		} catch (IOException e) {
			// do nothing. deviceset remains at null which causes the method to fail gracefully.
		}
		if (devicesset == null) {
			// if the list is null, an internal Provider Error has occurred.
			String[] empty = { "" };
			return empty;
		}

		// Convert to an List so that it can be sorted
		List<String> devices = new ArrayList<String>(devicesset);
		Collections.sort(devices);
		// Convert the IDs to names
		List<String> nameslist = new ArrayList<String>();
		for (String deviceid : devices) {
			String devicename = AVRMCUidConverter.id2name(deviceid);
			if (devicename != null) {
				nameslist.add(devicename);
			}
		}
		return nameslist.toArray(new String[nameslist.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		// Nothing to dispose
	}

}
