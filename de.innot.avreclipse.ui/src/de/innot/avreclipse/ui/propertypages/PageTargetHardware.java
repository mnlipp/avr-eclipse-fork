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
 * $Id: PageTargetHardware.java 354 2008-03-23 11:20:55Z innot $
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui.propertypages;

/**
 * The Target Hardware property page.
 * <p>
 * This page uses only one single tab
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class PageTargetHardware extends AbstractAVRPage {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#isSingle()
	 */
	@Override
	protected boolean isSingle() {
		return true;
	}

}
