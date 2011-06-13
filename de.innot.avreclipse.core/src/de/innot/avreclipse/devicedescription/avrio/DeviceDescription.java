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
package de.innot.avreclipse.devicedescription.avrio;

import java.util.ArrayList;
import java.util.List;

import de.innot.avreclipse.devicedescription.ICategory;
import de.innot.avreclipse.devicedescription.IDeviceDescription;

/**
 * A DeviceDescription Class for avr/io.h based device descriptions.
 * 
 * @author Thomas Holland
 * 
 */
public class DeviceDescription implements IDeviceDescription {

	private String fName = null;
	
	private List<ICategory> fCategories = null;
	private List<String> fSources = null;

	/**
	 * Creates a DeviceDescription with the given name.
	 * 
	 * @param name String with name of the DeviceDescription 
	 */
	public DeviceDescription(String name) {
		fName = name;
		fCategories = new ArrayList<ICategory>(3);
		fCategories.add(0, new RegisterCategory());
		fCategories.add(1, new PortCategory());
		fCategories.add(2, new IVecsCategory());
		fSources = new ArrayList<String>(1);
	}

	public String getName() {
		return fName;
	}

	public void setName(String name) {
		fName = name;
	}

	/**
	 * Returns a List of all categories this DeviceDescription has.
	 * <p>
	 * The list is a copy of the internal list and can be modified (e.g. sorted)
	 * as required
	 * 
	 * @return List of ICategory elements
	 */
	public List<ICategory> getCategories() {
		List<ICategory> tmplist = new ArrayList<ICategory>(fCategories);
		return tmplist;
	}


	/**
	 * @param headerFile
	 *            Name of the header file that defines the properties of this
	 *            device
	 */
	protected void addHeaderFile(String headerFile) {
		fSources.add(headerFile);
	}

	public List<String> getSourcesList() {
		return new ArrayList<String>(fSources);
	}


}
