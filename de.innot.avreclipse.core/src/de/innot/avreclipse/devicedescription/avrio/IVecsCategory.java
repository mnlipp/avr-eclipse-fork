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
 *     
 * $Id$
 *     
 *******************************************************************************/
package de.innot.avreclipse.devicedescription.avrio;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import de.innot.avreclipse.devicedescription.ICategory;
import de.innot.avreclipse.devicedescription.IEntry;

/**
 * Implements a ICategory for Interrupt Vector Elements.
 * 
 * @author Thomas Holland
 * 
 * @see PortCategory
 * @see RegisterCategory
 */
public class IVecsCategory extends BaseEntry implements ICategory {

	// The indices for Register Entry column fields
	final static int IDX_NAME = 0;
	final static int IDX_SIGNAME = 1;
	final static int IDX_DESCRIPTION = 2;
	final static int IDX_VECTOR = 3;

	// The labels for Register Entry column data fields
	final static String STR_NAME = "Name";
	final static String STR_SIGNAME = "Old Name";
	final static String STR_DESCRIPTION = "Description";
	final static String STR_VECTOR = "Vector";

	final static String[] fLabels = { STR_NAME, STR_SIGNAME, STR_DESCRIPTION,
			STR_VECTOR };
	final static int[] fDefaultWidths = { 20, 20, 35, 7 };

	public final static String CATEGORY_NAME = "Interrupts";

	/**
	 * Instantiate a new IVecsCategory. The name is fixed to
	 * {@value #CATEGORY_NAME}
	 */
	public IVecsCategory() {
		super.setName(CATEGORY_NAME);
	}

	/**
	 * @return the name of this Category (fixed to {@value #CATEGORY_NAME})
	 */
	@Override
	public String getName() {
		return CATEGORY_NAME;
	}

	public int getColumnCount() {
		return fLabels.length;
	}

	public String[] getColumnLabels() {
		return Arrays.copyOf(fLabels, fLabels.length);
	}

	public int[] getColumnDefaultWidths() {
		return Arrays.copyOf(fDefaultWidths, fDefaultWidths.length);
	}

	@Override
	public List<IEntry> getChildren() {
		// The list of IVecs looks better when sorted
		// TODO: remove as soon as the view knows how to sort according to user
		// input
		List<IEntry> tmplist = super.getChildren();
		Collections.sort(tmplist, new IEntry.EntryColumnComperator(0));
		return tmplist;
	}
}
