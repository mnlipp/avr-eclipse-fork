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
package de.innot.avreclipse.ui.editors;

import de.innot.avreclipse.core.toolinfo.fuses.FuseType;

/**
 * The Lockbits File Editor.
 * <p>
 * The only difference to the {@link FusesEditor} is the different memory type, so this class has
 * only one method to get the type.
 * </p>
 * 
 * @see FusesEditor
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class LockbitsEditor extends FusesEditor {

	@Override
	protected FuseType getType() {
		return FuseType.LOCKBITS;
	}
}
