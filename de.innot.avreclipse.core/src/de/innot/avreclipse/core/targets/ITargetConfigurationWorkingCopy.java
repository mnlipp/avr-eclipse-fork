/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.core.targets;

import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public interface ITargetConfigurationWorkingCopy extends ITargetConfiguration {

	/**
	 * Set a new name for this configuration.
	 * 
	 * @param name
	 *            the Name to set
	 */
	public void setName(String name);

	/**
	 * Set a new description for this configuration.
	 * 
	 * @param name
	 *            the Name to set
	 */
	public void setDescription(String description);

	/**
	 * @param mcuid
	 *            the MCU to set
	 */
	public void setMCU(String mcuid);

	/**
	 * Change the target MCU clock.
	 * 
	 * @param fcpu
	 *            the FCPU to set
	 */
	public void setFCPU(int fcpu);

	/**
	 * Persist this configuration to the preference storage.
	 * <p>
	 * This will not do anything if the configuration has not been modified.
	 * </p>
	 * 
	 * @throws BackingStoreException
	 *             If this configuration cannot be written to the preference storage area.
	 */
	public void doSave() throws BackingStoreException;

	public void setAttribute(String attributeName, String newValue);

	/**
	 * Reset this Configuration to the default values.
	 * <p>
	 * The ID and the Name of this Configuration are not changed.
	 * </p>
	 */
	public void setDefaults();

}