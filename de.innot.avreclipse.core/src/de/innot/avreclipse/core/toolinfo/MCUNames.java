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
package de.innot.avreclipse.core.toolinfo;

import java.util.HashSet;
import java.util.Set;

import de.innot.avreclipse.core.IMCUProvider;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * This class handles the conversion of known MCU ids to MCU Names.
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class MCUNames implements IMCUProvider {

	private static MCUNames fInstance = null;

	/**
	 * Get the default instance of the Signatures class
	 */
	public static MCUNames getDefault() {
		if (fInstance == null)
			fInstance = new MCUNames();
		return fInstance;
	}

	// private constructor to prevent instantiation
	private MCUNames() {
	}

	/**
	 * Get the Name for the given MCU id.
	 * 
	 * @param mcuid
	 *            String with a MCU id
	 * @return String with the MCU Name.
	 */
	public String getName(String mcuid) {
		return AVRMCUidConverter.id2name(mcuid);
	}

	/**
	 * Get the MCU id for the given Name.
	 * 
	 * @param mcuname
	 *            String with an MCU name
	 * @return String with the corresponding MCU id or <code>null</code> if
	 *         the given id is invalid.
	 */
	public String getID(String mcuname) {
		return AVRMCUidConverter.name2id(mcuname);
	}

	//
	// Methods of the IMCUProvider Interface
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUInfo(java.lang.String)
	 */
	public String getMCUInfo(String mcuid) {
		return getName(mcuid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUList()
	 */
	public Set<String> getMCUList() {
		return new HashSet<String>(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#hasMCU(java.lang.String)
	 */
	public boolean hasMCU(String mcuid) {
		if (getName(mcuid)!= null) {
			return true;
		}
		return false;
	}

}
