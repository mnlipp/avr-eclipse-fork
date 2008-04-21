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

import java.io.IOException;

import de.innot.avreclipse.core.avrdude.FuseBytes;

/**
 * @author Thomas
 * 
 */
public class FuseByteValues {

	private final static String	FUSE	= "fuse";
	private final static String	LFUSE	= "lfuse";
	private final static String	HFUSE	= "hfuse";
	private final static String	EFUSE	= "efuse";

	private final String		fMCUId;

	private final int[]			fValues	= new int[FuseBytes.MAX_FUSEBYTES];

	public FuseByteValues(String mcuid) {
		fMCUId = mcuid;
		for (int i = 0; i < fValues.length; i++) {
			fValues[i] = -1;
		}
	}

	public FuseByteValues(FuseByteValues source) {
		fMCUId = source.fMCUId;
		System.arraycopy(source.fValues, 0, fValues, 0, FuseBytes.MAX_FUSEBYTES);
	}

	public String getMCUId() {
		return fMCUId;
	}

	public void setValue(String name, int value) {

		int index = fusenameToIndex(name);

		if (index == -1) {
			throw new IllegalArgumentException("Fusename [" + name + "] is undefined.");
		}

		setValue(index, value);
	}

	public void setValue(int index, int value) {

		checkIndex(index);

		fValues[index] = value;
	}

	public int getValue(int index) {
		checkIndex(index);

		return fValues[index];
	}

	public void setValues(int[] newvalues) {
		int count = Math.min(newvalues.length, fValues.length);
		System.arraycopy(newvalues, 0, fValues, 0, count);
	}

	public int[] getValues() {
		// make a copy and return it
		int[] copy = new int[fValues.length];
		System.arraycopy(fValues, 0, copy, 0, fValues.length);
		return copy;
	}

	public int getFuseByteCount() {
		try {
			return Fuses.getDefault().getFuseByteCount(fMCUId);
		} catch (IOException e) {
			// If you want to see the Exception use the Fuses class directly
			return 0;
		}
	}

	public int fusenameToIndex(String name) {
		int index = -1;
		if (FUSE.equals(name) || LFUSE.equals(name)) {
			index = 0;
		} else if (HFUSE.equals(name)) {
			index = 1;
		} else if (EFUSE.equals(name)) {
			index = 2;
		}

		return index;
	}

	private void checkIndex(int index) {
		if (!(0 <= index && index < FuseBytes.MAX_FUSEBYTES)) {
			throw new IllegalArgumentException("[" + index + "] is not a valid fusebyte index.");
		}
	}
}
