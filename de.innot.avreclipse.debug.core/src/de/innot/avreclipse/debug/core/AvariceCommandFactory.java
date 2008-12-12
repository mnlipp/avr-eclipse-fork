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

package de.innot.avreclipse.debug.core;

import org.eclipse.cdt.debug.mi.core.command.factories.StandardCommandFactory;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AvariceCommandFactory extends StandardCommandFactory {

	/**
	 * 
	 */
	public AvariceCommandFactory() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param miVersion
	 */
	public AvariceCommandFactory(String miVersion) {
		super(miVersion);
		// TODO Auto-generated constructor stub
	}

}
