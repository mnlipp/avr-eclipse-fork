package de.innot.avreclipse.core.toolinfo.fuses;

import java.io.Serializable;
import java.util.List;

public class BitFieldDescription implements Serializable {

    private static final long serialVersionUID = -2810533010056086834L;

	public String name;

	public String description;

	public int mask;

	public List<BitFieldValue> values;

	@Override
	public String toString() {
		return name + ": mask=0x" + Integer.toHexString(mask) + " desc=" + description;
	}

}
