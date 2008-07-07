/**
 * 
 */
package de.innot.avreclipse.core.toolinfo.fuses;

/**
 * {@link IDescriptionHolder} implementation for Locks.
 * <p>
 * Objects of this class hold the {@link BitFieldDescription} objects for the Locks byte of a single
 * MCU.
 * </p>
 * <p>
 * This Class is a simple extension of {@link FusesDescription}, as from an implementation point of
 * view fusebytes and lockbits are very similar.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class LockbitsDescription extends FusesDescription {

	/* Change this whenever the fields of this class have changed */
	private static final long	serialVersionUID	= 201286106985049090L;

	/**
	 * Create a new LockbitsDescription for a MCU with the given number of lock bytes.
	 * <p>
	 * All current AVR MCUs have only a single byte with lockbits. This class does support multiple
	 * lockbit bytes if required by future AVR MCUs.
	 * </p>
	 * 
	 * @param mcuid
	 *            <code>String</code> with a MCU id value.
	 * @param bytecount
	 *            <code>int</code> with the number of lockbits bytes this MCU has.
	 */
	public LockbitsDescription(String mcuid, int bytecount) {
		super(mcuid, bytecount);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.FusesDescription#toString()
	 */
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer("Locks for " + getMCUId());
		sb.append(" (" + getByteCount() + ") [");
		for (int i = 0; i < getByteCount(); i++) {
			sb.append("[ Byte " + i + " ");
			BitFieldDescription[] fields = getBitFieldDescriptions(i);
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
