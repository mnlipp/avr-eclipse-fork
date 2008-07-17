package de.innot.avreclipse.core.toolinfo.fuses;

public interface IBitFieldValueDescription {

	/**
	 * @return the description of this bitfield value enumeration element
	 */
	public String getDescription();

	/**
	 * @return the value of this bitfield value enumeration element
	 */
	public int getValue();

}