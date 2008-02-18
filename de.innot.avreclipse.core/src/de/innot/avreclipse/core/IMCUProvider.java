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
package de.innot.avreclipse.core;

import java.util.List;

/**
 * Methods for accessing MCU id values for a tool that has a list of supported MCUs.
 * <p>
 * This interface is used by the Supported MCU View to get a list of all MCU id values
 * the implementor supports.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public interface IMCUProvider {

	/**
	 * Returns a <code>List</code> of all MCU id values the implementor supports.
	 * 
	 * @return List of Strings with the supported MCU id values
	 */
	public List<String> getMCUList();

	/**
	 * Test if the implementor supports the given MCU id.
	 * 
	 * @param mcuid
	 *            String with a MCU id
	 * @return <code>true</code> if the implementor supports this id,
	 *         <code>false</code> otherwise.
	 */
	public boolean hasMCU(String mcuid);

	/**
	 * Returns any information the implementor associates with the given MCU id.
	 * <p>
	 * The type of the returned Object is implementation specific. Currently the following return Types are supported by the viewer:
	 * <ul>
	 * <li><code>Boolean</code>: An checkmark is shown if <code>true</code>
	 * <li><code>String</code>: The string value is shown.</li>
	 * <li><code>URL</code>: A link is shown and the content of the URL is shown when clicked.</li>
	 * </ul>
	 * An error "X" is shown when <code>null</code> is returned.
	 * </p>
	 * 
	 * @param mcuid
	 *            String with a MCU id
	 * @return <code>Object</code> with some information about the MCU or
	 *         <code>null</code> if no information is available or the MCU id
	 *         is unknown.
	 */
	public Object getMCUInfo(String mcuid);

	/**
	 * Get a descriptive String for the implementor.
	 * <p>
	 * This is used as the column header in the Supported MCU View.
	 * </p>
	 * 
	 * @return String with a human readable description.
	 */
	public String getMCUInfoDescription();

}
