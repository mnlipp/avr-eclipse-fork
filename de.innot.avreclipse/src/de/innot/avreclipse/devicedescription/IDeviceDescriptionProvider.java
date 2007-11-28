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
 * $Id: IDeviceDescriptionProvider.java 14 2007-11-27 12:02:05Z thomas $
 *     
 *******************************************************************************/
package de.innot.avreclipse.devicedescription;

import java.util.List;

import org.eclipse.core.runtime.IPath;

public interface IDeviceDescriptionProvider {

	public List<String> getDeviceList();

	public IDeviceDescription getDevice(String name);

	/**
	 * Get the Path of the directory where the device description comes from.
	 * 
	 * This method should be used together with
	 * {@link IDeviceDescription#getSourcesList()} to get a fully qualified path
	 * to the original source. This then can be used by the viewer to open the
	 * corresponding file upon user request.
	 * 
	 * @return IPath to the base directory
	 */
	public IPath getBasePath();
}
