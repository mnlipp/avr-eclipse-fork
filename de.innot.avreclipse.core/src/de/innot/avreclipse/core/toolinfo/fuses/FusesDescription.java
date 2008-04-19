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

import java.io.Serializable;

/**
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class FusesDescription implements Serializable {

	private static final long serialVersionUID = 1297397961257990498L;

	private String fMCUid;

	private int fFuseByteCount;

	private BitFieldDescription[][] fBitfields;

	public FusesDescription(String mcuid) {
		fMCUid = mcuid;
		fFuseByteCount = 0;
	}
	
	public int getFuseByteCount() {
		return fFuseByteCount;
	}

	public void setFuseByteCount(int count) {
		fFuseByteCount = count;
		fBitfields = new BitFieldDescription[count][];
	}

	public void setBitFields(int fusebyte, BitFieldDescription[] bitfields) {
		if (fusebyte >= fFuseByteCount) {
			throw new IllegalArgumentException("Parameter fusebyte value " + fusebyte + " > "
			        + (fFuseByteCount - 1));
		}
		fBitfields[fusebyte] = bitfields;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Fuses for " + fMCUid);
		sb.append(" (" + fFuseByteCount + ") [");
		for (int i = 0; i < fFuseByteCount; i++) {
			sb.append("[ Byte " + i + " ");
			BitFieldDescription[] fields = fBitfields[i];
			for (int j = 0; j < fields.length; j++) {
				sb.append("[");
				sb.append(fields[j].toString());
				sb.append("] ");
			}
			sb.append("] ");
		}
		sb.append("]");

		return sb.toString();
	}

}
