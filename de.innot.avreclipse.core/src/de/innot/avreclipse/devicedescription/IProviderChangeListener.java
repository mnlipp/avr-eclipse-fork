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
 *     Manuel Stahl - original idea to parse the <avr/io.h> file and the patterns
 *     
 * $Id$
 *     
 *******************************************************************************/
package de.innot.avreclipse.devicedescription;


/**
 * @author Thomas Holland
 * 
 */
public interface IProviderChangeListener {

	/**
	 * Notification that the DeviceDescriptionProvider has changed.
	 * <p>
	 * This method gets called when the observed object fires a Provider change
	 * event.
	 * </p>
	 * 
	 */
	public void providerChange();

}
