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

/**
 * An <code>IDeviceDescriptionProvider</code>
 * 
 * @author Thomas Holland
 * 
 */
public interface IDeviceDescriptionProvider {

	/**
	 * Returns the name of the DeviceDescriptionProvider.
	 * 
	 * This name is used by the viewer to store the current user-modifiable
	 * column layout. Each DeviceDescriptionProvider must have a unique name.
	 * 
	 * For the time being this is also the name shown in the Viewers Menu. But
	 * this will be replaced as soon as this Class is externalized into a
	 * separate Fragment.
	 * 
	 * @return <code>String</code> with the name of this Provider
	 * @deprecated
	 */
	@Deprecated
	public String getName();

	/**
	 * Returns a <code>List</code> with the names of all Devices known to this
	 * provider.
	 * 
	 * @return <code>List</code> of Strings
	 */
	public List<String> getDeviceList();

	/**
	 * Returns a {@link IDeviceDescription} Object for the Device with the given
	 * name.
	 * 
	 * The name must be one of the names in the list returned by
	 * {@link #getDeviceList()}.
	 * 
	 * @param name
	 *            The name of the requested device.
	 * @return {@link IDeviceDescription} Object of <code>null</code> if the
	 *         name was not known or any error occurred reading / parsing the
	 *         underlying source file(s)
	 */
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

	/**
	 * Returns any stored error messages.
	 * 
	 * If either {@link #getDeviceList()} {@link #getDevice(String)} returns
	 * null, this method can be called to retrieve a message describing the
	 * error.
	 * 
	 * @return String The stored error message or null if no error stored.
	 */
	public String getErrorMessage();

	/**
	 * Adds a Provider change listener to this Provider.
	 * 
	 * The listener is called whenever the internal data for the provider has
	 * changed, e.g. when the user changes some preference settings.
	 * 
	 * @param pcl
	 *            The IProviderChangeListener to add
	 */
	public void addProviderChangeListener(IProviderChangeListener pcl);

	/**
	 * Removes a Provider change listener from this provider.
	 * 
	 * @param pcl
	 *            The IProviderChangeListener to remove
	 */
	public void removeProviderChangeListener(IProviderChangeListener pcl);
}
