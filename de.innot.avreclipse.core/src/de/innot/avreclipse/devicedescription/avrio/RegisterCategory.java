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
package de.innot.avreclipse.devicedescription.avrio;

import java.util.Collections;
import java.util.List;

import de.innot.avreclipse.devicedescription.ICategory;
import de.innot.avreclipse.devicedescription.IEntry;

/**
 * Implements a ICategory for Register Elements.
 * 
 * This is extended from {@link PortCategory}. The only change currently is,
 * that the registers are sorted.
 * 
 * @author Thomas Holland
 * 
 * @see PortCategory
 * @see IVecsCategory
 */
public class RegisterCategory extends PortCategory implements ICategory {

	public final static String CATEGORY_NAME = "Registers";

	/**
	 * Instantiate a new RegistersCategory. The name is fixed to
	 * {@value #CATEGORY_NAME}
	 */
	public RegisterCategory() {
		super.setName(CATEGORY_NAME);
	}

	/**
	 * @return the name of this Category (fixed to {@value #CATEGORY_NAME})
	 */
	@Override
	public String getName() {
		return CATEGORY_NAME;
	}

	@Override
	public List<IEntry> getChildren() {
		// The list of Registers looks better when sorted
		// TODO: remove as soon as the view knows how to sort according to user
		// input
		List<IEntry> tmplist = super.getChildren();
		Collections.sort(tmplist, new IEntry.EntryColumnComperator(0));
		return tmplist;
	}

}
