package de.innot.avreclipse.core.toolinfo.fuses;

import java.io.Serializable;
import java.util.List;

public class BitFieldValue implements Serializable {

	private static final long serialVersionUID = -1548655346036395776L;

	public String text;

	public int value;

	public List<BitFieldValue> subvalues;

	@Override
	public String toString() {
		return "0x" + Integer.toHexString(value) + ": " + text;
	}
}
