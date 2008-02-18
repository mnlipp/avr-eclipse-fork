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
package de.innot.avreclipse.core.toolinfo;

import java.util.ArrayList;
import java.util.List;

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

	// Human readable Description for the Supported MCUs View
	private final static String DESCRIPTION = "Name";

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
	public Object getMCUInfo(String mcuid) {
		return getName(mcuid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUInfoDescription()
	 */
	public String getMCUInfoDescription() {
		return DESCRIPTION;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUList()
	 */
	public List<String> getMCUList() {
		return new ArrayList<String>(0);
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
