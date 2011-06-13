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
package de.innot.avreclipse.ui.editors;


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

	// @Override
	// protected FuseType getType() {
	// return FuseType.LOCKBITS;
	// }
}
