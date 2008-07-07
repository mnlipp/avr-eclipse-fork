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
package de.innot.avreclipse.core.toolinfo.fuses;

import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;

/**
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class LockbitsReader extends AbstractFusesReader {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#getTargetNodeName()
	 */
	@Override
	protected String getTargetMemspace() {
		return "LOCKBIT";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#getDescriptionHolder(java.lang.String,
	 *      int)
	 */
	@Override
	protected IDescriptionHolder getDescriptionHolder(String mcuid, int bytecount) {
		return new LockbitsDescription(mcuid, bytecount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#addBitFields(de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder,
	 *      int, de.innot.avreclipse.core.toolinfo.fuses.BitFieldDescription[])
	 */
	@Override
	protected void addBitFields(IDescriptionHolder desc, int index, String name,
			BitFieldDescription[] bitfields) {

		LockbitsDescription lockbitsdesc = (LockbitsDescription) desc;
		lockbitsdesc.setBitFieldDescriptions(index, name, bitfields);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#getStoragePath()
	 */
	@Override
	protected IPath getStoragePath() {
		// The default is to get the folder from the {@link Locks} class.
		return Locks.getDefault().getInstanceStorageLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#setDefaultValues(org.w3c.dom.Document,
	 *      de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder)
	 */
	@Override
	protected void setDefaultValues(Document document, IDescriptionHolder desc) {

		LockbitsDescription lockbitsdesc = (LockbitsDescription) desc;

		// We can safely set the default lockbits to 0xff (all bits set), because -as far as I can
		// see- this means "no locks" for all AVR MCUs.
		if (lockbitsdesc.getByteCount() > 0) {
			lockbitsdesc.setDefaultValue(0, 0xff);
		}
	}

}
