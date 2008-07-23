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

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.core.toolinfo.partdescriptionfiles.FusesReader;

/**
 * Describes a BitField which is part of a Fuse byte or a LockBits byte.
 * <p>
 * This class represents all properties of a single BitField. They are:
 * <ul>
 * <li><code>Name</code>: the name of the BitField, e.g. <code>SPIEN</code>.</li>
 * <li><code>Description</code>: a more user friendly long description of the bitfield.</li>
 * <li><code>Mask</code>: an integer value that determines which bits are covered bitfield.</li>
 * <li><code>Index</code>: the index of the parent byte within its {@link ByteValues} structure.
 * This is not strictly a BitField property, but it is useful to work with the BitFieldDescription
 * object</li>
 * </ul>
 * Each of these properties has an associated getter method.
 * </p>
 * <p>
 * The interface has also some convenient methods to work with the BitFields. It also knows how to
 * serialize itself to an XML DOM and can be instantiated from the DOM.
 * </p>
 * <p>
 * This class has two constructors. One to pass the value and description directly and another one
 * to read the value and description from an XML document node. The first one is used by the
 * {@link FusesReader} while parsing the <em>part description file</em> while the second one is
 * used when a {@link ByteDescription} is constructed from an XML file.<br>
 * </p>
 * 
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class BitFieldDescription {

	/** XML element tag name name for a <code>BitFieldValueDescription</code> object. */
	public final static String						TAG_BITFIELD	= "bitfield";

	/** XML attribute name for the bitfield name */
	public final static String						ATTR_NAME		= "name";

	/** XML attribute name for the bitfield description */
	public final static String						ATTR_DESC		= "desc";

	/** XML attribute name for the bitfield mask */
	public final static String						ATTR_MASK		= "mask";

	/** The byte index of the fuse/lock byte this description belongs to */
	private int										fIndex			= -1;

	/** The name of this bitfield */
	private String									fName			= "unknown";

	/** Description of this bitfield */
	private String									fDescription	= "???";

	/** The mask for this bitfield */
	private int										fMask			= -1;

	/**
	 * A enumeration of all possible values. This is optional and may be empty if no values are
	 * defined in the part description files.
	 */
	private final List<BitFieldValueDescription>	fValues;

	/**
	 * Construct a new <code>BitFieldDescription</code> object from the given parameters.
	 * <p>
	 * This constructor is called from {@link FusesReader}.
	 * </p>
	 * 
	 * @param index
	 *            the fuse byte / locks byte offset
	 * @param name
	 *            the name of the bitfield
	 * @param description
	 *            the description of the bitfield
	 * @param mask
	 *            the mask defining which bits of the byte are represented by this bitfield.
	 * @param values
	 *            enumeration of all possible values (can be <code>null</code> if the bitfield has
	 *            no predefined values.
	 */
	public BitFieldDescription(int index, String name, String description, int mask,
			List<BitFieldValueDescription> values) {
		fIndex = index;
		fName = name;
		fDescription = description;
		fMask = mask;

		fValues = values;
	}

	/**
	 * Construct a new BitFieldValueDescription from a XML &lt;bitfield&gt; node.
	 * <p>
	 * This constructor will take the node and parse the values from the "name", "desc" and "mask"
	 * attributes. The index is taken from the "index" attribute of the parent node. If any
	 * attribute is missing a default value is used.
	 * </p>
	 * 
	 * @param bitfieldnode
	 *            A &lt;bitfield&gt; document node.
	 */
	protected BitFieldDescription(Node bitfieldnode) {

		// First get our own attributes
		NamedNodeMap attrs = bitfieldnode.getAttributes();
		for (int i = 0; i < attrs.getLength(); i++) {
			Node attr = attrs.item(i);
			if (ATTR_NAME.equalsIgnoreCase(attr.getNodeName())) {
				fName = attr.getTextContent();
			}
			if (ATTR_DESC.equalsIgnoreCase(attr.getNodeName())) {
				fDescription = attr.getTextContent();
			}
			if (ATTR_MASK.equalsIgnoreCase(attr.getNodeName())) {
				fMask = Integer.decode(attr.getTextContent());
			}
		}

		// Then collect the BitFieldValueDescription child nodes
		fValues = new ArrayList<BitFieldValueDescription>();

		NodeList bfvnodes = bitfieldnode.getChildNodes();

		for (int i = 0; i < bfvnodes.getLength(); i++) {
			Node child = bfvnodes.item(i);
			if (BitFieldValueDescription.TAG_VALUE.equalsIgnoreCase(child.getNodeName())) {
				BitFieldValueDescription value = new BitFieldValueDescription(child);
				fValues.add(value);
			}
		}

		// and finally get the index from the parent
		Node parent = bitfieldnode.getParentNode();
		NamedNodeMap parentattrs = parent.getAttributes();
		Node indexnode = parentattrs.getNamedItem("index");
		if (indexnode != null) {
			fIndex = Integer.decode(indexnode.getTextContent());
		}
	}

	/**
	 * Get the byte index of the parent fuse or lockbits byte.
	 * 
	 * @return <code>int</code> from <code>0</code> up to <code>5</code> for fuse bytes or
	 *         <code>0</code> for a lockbits.
	 */
	public int getIndex() {
		return fIndex;
	}

	/**
	 * Get the name of the BitField.
	 * <p>
	 * This is the name from the part description file, e.g. <em>"SPIEN"</em> or
	 * <em>"SUT_CKSEL"</em>".
	 * </p>
	 * 
	 * @return
	 */
	public String getName() {
		return fName;
	}

	/**
	 * Get the long, human readable, description for the BitField.
	 * <p>
	 * This is the description from the part description file.
	 * </p>
	 * 
	 * @return
	 */
	public String getDescription() {
		return fDescription;
	}

	/**
	 * Get the bitmask for the BitField.
	 * <p>
	 * The value is taken from the part description file. It has ones for all bits that make up the
	 * BitField, and zeros for all bits outside of the BitField.
	 * </p>
	 * 
	 * @return byte value between <code>0x00</code> and <code>0xFF</code>.
	 */
	public int getMask() {
		return fMask;
	}

	/**
	 * Return the maximum value acceptable for this BitField.
	 * <p>
	 * This is 2^^(number of 1-bits in the mask) minus 1.
	 * </p>
	 * 
	 * @return max value
	 */
	public int getMaxValue() {
		return (2 << (Integer.bitCount(fMask) - 1)) - 1;
	}

	/**
	 * Convert a normalize value to a bitfield value.
	 * <p>
	 * This method will left-shift the given value for the required number of places to match the
	 * mask.
	 * </p>
	 * 
	 * @param value
	 *            the normalized value (range 0 to {@link #getMaxValue()})
	 * @return <code>int</code> with the shifted value.
	 */
	public int valueToBitfield(int value) {
		// left-shift the value to the right place (as determined by the mask)
		return value << Integer.numberOfTrailingZeros(fMask);
	}

	/**
	 * Convert a BitField value to a normalized value.
	 * <p>
	 * This method will mask off all bits outside this BitField and then right-shift the result so
	 * that a normalized value (range 0 to {@link #getMaxValue()}) is returned.
	 * </p>
	 * 
	 * @param bitfieldvalue
	 *            a byte from which to extract and normalize the value of this bitfield.
	 * @return <code>int</code> with the normalized value.
	 */
	public int bitfieldToValue(int bitfieldvalue) {
		// Mask the appropriate bits and rightshift (normalize) the result
		int masked = bitfieldvalue & fMask;
		return masked >> Integer.numberOfTrailingZeros(fMask);
	}

	/**
	 * Get a list of all values for this BitField as defined in the part description file.
	 * <p>
	 * The returned list is a copy and any changes to the list will not affect this BitField object.
	 * </p>
	 * 
	 * @return List of <code>IBitFieldValueDescription</code> objects, or <code>null</code> if
	 *         no values are defined.
	 */
	public List<BitFieldValueDescription> getValuesEnumeration() {
		if (fValues != null) {
			return new ArrayList<BitFieldValueDescription>(fValues);
		}
		return null;
	}

	/**
	 * Get the descriptive text for the given BitField value.
	 * <p>
	 * Depending on the type of the BitField different return values are possible:
	 * <ul>
	 * <li>
	 * <p>
	 * The BitField has a some BitFieldValue description objects
	 * <ul>
	 * <li>The value matches one of them: return the value description.</li>
	 * <li>The value matches none of them: return the string "undefined"</li>
	 * </ul>
	 * </p>
	 * </li>
	 * <li>
	 * <p>
	 * The BitField has no BitFieldValue description objects
	 * <ul>
	 * <li>The BitField is a single bit: Return "Yes" for <code>0</code> and <code>"No"</code>
	 * for <code>1</code>. This matches the fuse byte logic used by ATMEL.</li>
	 * <li>The BitField covers multiple bits: return the value as a hex string</li>
	 * </ul>
	 * </p>
	 * </li>
	 * </p>
	 * 
	 * @param value
	 *            Normalized BitField value (range 0 up to {@link #getMaxValue()};
	 * @return
	 */
	public String getValueText(int value) {
		for (BitFieldValueDescription desc : fValues) {
			if (desc.getValue() == value) {
				return desc.getDescription();
			}
		}
		if (fValues.size() > 0) {
			// The value was not in the list. Return an error string
			return "undefined (" + ByteDescription.toHex(value) + ")";
		}

		// There are no BitFieldValueDescriptions available for this BitField
		// If if is a single-bit field return "yes"/"no" (fuse-byte logic where 0 means yes),
		// If it is a multi-bit field just return the value as hex string.
		if (Integer.bitCount(fMask) == 1) {
			return value == 0 ? "Yes" : "No";
		}
		return ByteDescription.toHex(value);
	}

	/**
	 * Convert to XML.
	 * <p>
	 * Create a new child &lt;bitfield&gt; node in the given parent node with the attributes "name",
	 * "desc" and "mask". All available {@link BitFieldValueDescription} objects are appended as
	 * childs of the the &lt;bitfield&gt; node.<br>
	 * The index is not stored as it is defined by the parent node.
	 * </p>
	 * 
	 * @param parentnode
	 *            &lt;bitfield&gt; document node.
	 */
	protected void toXML(Node parentnode) {
		Document document = parentnode.getOwnerDocument();
		Element element = document.createElement(TAG_BITFIELD);
		element.setAttribute(ATTR_NAME, fName);
		element.setAttribute(ATTR_DESC, fDescription);
		element.setAttribute(ATTR_MASK, ByteDescription.toHex(fMask));

		if (fValues != null && fValues.size() > 0) {
			for (BitFieldValueDescription bfv : fValues) {
				bfv.toXML(element);
			}
		}
		parentnode.appendChild(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return fName + ": mask=0x" + Integer.toHexString(fMask) + " desc=" + fDescription;
	}

}
