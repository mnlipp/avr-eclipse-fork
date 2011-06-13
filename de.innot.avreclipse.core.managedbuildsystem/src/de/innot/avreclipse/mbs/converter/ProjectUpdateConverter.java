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
package de.innot.avreclipse.mbs.converter;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;

/**
 * @author Thomas
 * 
 */
public class ProjectUpdateConverter implements IConvertManagedBuildObject {

	/**
	 * Update a given Project to the latest AVR Eclipse Plugin settings
	 * 
	 * @author Thomas Holland
	 * 
	 */
	public ProjectUpdateConverter() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject#convert(org.eclipse.cdt.managedbuilder.core.IBuildObject,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	public IBuildObject convert(IBuildObject buildObj, String fromId,
			String toId, boolean isConfirmed) {

		// This is currently only called from the CDT ConvertTargetDialog and
		// only for an existing AVR Eclipse Plugin project.
		
		if (toId.endsWith("2.1.0")) {
			buildObj = Convert21.convert(buildObj, fromId);
		}
		return buildObj;
	}

}
