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
 * $Id: FuseByteValues.java 427 2008-04-21 13:42:45Z innot $
 *     
 *******************************************************************************/
package de.innot.avreclipse.core.toolinfo.fuses;


/**
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class LockbitsValue {

	private final String	fMCUId;

	private int				fValue	= 0xff;

	public LockbitsValue(String mcuid) {
		fMCUId = mcuid;
	}

	public LockbitsValue(LockbitsValue source) {
		fMCUId = source.fMCUId;
		fValue = source.fValue;
	}

	public String getMCUId() {
		return fMCUId;
	}

	public void setValue(int value) {

		fValue = value;
	}

	public int getValue() {

		return fValue;
	}

}
