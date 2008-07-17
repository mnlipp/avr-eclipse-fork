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

/**
 * Description for a single Fuse or Lockbits byte.
 * <p>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class ByteDescription implements Comparable<ByteDescription>, IByteDescription {

	private final static String				ATTR_BYTE_INDEX		= "index";
	private final static String				ATTR_BYTE_NAME		= "name";

	private final static String				ELEMENT_DEFAULT		= "default";
	private final static String				ATTR_DEFAULT_VALUE	= "value";

	/** The type of this byte. Either FUSE or LOCKBITS */
	private FuseType						fType;
	// TODO: Do we need this?

	/** Fuse byte name (from the part description file). */
	private String							fName;

	/** The index of the Byte. (0 up to 5 for fuse bytes, 0 for the lockbits byte) */
	private int								fIndex;

	/** The default values. <code>-1</code> if no default value is defined. */
	private int								fDefaultValue;

	/** List with all BitFieldDescriptions for this byte. */
	private final List<BitFieldDescription>	fBitFieldList;

	/**
	 * Create a new ByteDescription for the byte with the given number parameters.
	 * 
	 * @param type
	 *            Either {@link FuseType#FUSE} or {@link FuseType#LOCKBITS}
	 * @param name
	 *            The name of this byte as defined in the part description file, e.g "LOW",
	 *            "FUSEBYTE3" or "LOCKBITS".
	 * @param index
	 *            The index of this byte within its memory. <code>0</code> up to <code>5</code>
	 *            for fuse bytes (depending on MCU) or <code>0</code> for the lockbits byte.
	 */
	public ByteDescription(FuseType type, String name, int index) {
		fType = type;
		fName = name;
		fIndex = index;
		fDefaultValue = -1;
		fBitFieldList = new ArrayList<BitFieldDescription>();
	}

	public ByteDescription(Node byteelement) throws IllegalArgumentException {

		// Default / Error check values
		fName = null;
		fIndex = -1;
		fDefaultValue = -1;
		fBitFieldList = new ArrayList<BitFieldDescription>();

		// Get the type of this byte from the given node.
		if (byteelement.getNodeName().equalsIgnoreCase(FuseType.FUSE.getElementName())) {
			fType = FuseType.FUSE;
		} else {
			fType = FuseType.LOCKBITS;
		}

		// The byte element has two attributes: "index" and "name"
		// Both must be present and valid, otherwise an Exception is thrown.

		NamedNodeMap attrs = byteelement.getAttributes();
		Node indexnode = attrs.getNamedItem(ATTR_BYTE_INDEX);
		if (indexnode == null) {
			throw new IllegalArgumentException("Required attribute \"" + ATTR_BYTE_INDEX
					+ "\" for element <" + fType.getElementName() + "> missing.");
		}
		fIndex = Integer.decode(indexnode.getTextContent());

		Node namenode = attrs.getNamedItem(ATTR_BYTE_NAME);
		if (namenode == null) {
			throw new IllegalArgumentException("Required attribute \"" + ATTR_BYTE_NAME
					+ "\" for element <" + fType.getElementName() + "> missing.");
		}
		fName = namenode.getNodeValue();

		// Now read the children of the byte element:
		// one default element (optional) and at least one <bitfield>
		NodeList children = byteelement.getChildNodes();
		if (children.getLength() == 0) {
			throw new IllegalArgumentException("Element <\"" + fType.getElementName()
					+ "\"> must have at least one <" + BitFieldDescription.TAG_BITFIELD
					+ "> child element");
		}

		for (int n = 0; n < children.getLength(); n++) {
			Node child = children.item(n);
			if (ELEMENT_DEFAULT.equalsIgnoreCase(child.getNodeName())) {
				// The <default> element has one attribute: "value"
				attrs = child.getAttributes();
				for (int k = 0; k < attrs.getLength(); k++) {
					Node attr = attrs.item(k);
					if (ATTR_DEFAULT_VALUE.equalsIgnoreCase(attr.getNodeName())) {
						fDefaultValue = Integer.decode(attr.getNodeValue());
					}
				}
			} else if (BitFieldDescription.TAG_BITFIELD.equalsIgnoreCase(child.getNodeName())) {
				// get the BitfieldDescription Object and add it to the list
				BitFieldDescription bfd = new BitFieldDescription(child);
				fBitFieldList.add(bfd);
			}
		}

		if (fBitFieldList.size() == 0) {
			throw new IllegalArgumentException("Element <\"" + fType.getElementName()
					+ "\"> must have at least one <" + BitFieldDescription.TAG_BITFIELD
					+ "> child element");
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#getBitFieldDescriptions()
	 */
	public List<IBitFieldDescription> getBitFieldDescriptions() {
		return new ArrayList<IBitFieldDescription>(fBitFieldList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteDescription#getDefaultValue()
	 */
	public int getDefaultValue() {
		if (fType == FuseType.LOCKBITS) {
			return 0xff;
		}
		return fDefaultValue;
	}

	/**
	 * Sets the default value for this byte.
	 * <p>
	 * The default value can be retrieved with {@link #getDefaultValue()}
	 * </p>
	 * 
	 * @param value
	 *            The new default value. Can be <code>-1</code> to indicate that no default is
	 *            available.
	 */
	public void setDefaultValue(int value) {
		fDefaultValue = value;
	}

	/**
	 * Add a <code>BitFieldDescription</code> object to this ByteDescription.
	 * <p>
	 * The list of all BitFieldDescriptions can be retrieved with {@link #getBitFieldDescriptions()}.
	 * </p>
	 * 
	 * @param bitfielddescription
	 *            A single <code>BitFieldDescription</code>
	 */
	public void addBitFieldDescription(BitFieldDescription bitfielddescription) {
		fBitFieldList.add(bitfielddescription);
	}

	/**
	 * Convert this ByteDescription Object to XML.
	 * 
	 * @see ByteDescriptions#toXML(Document) for the DTD of the generated xml
	 * 
	 * @param parentelement
	 *            A &lt;fusedescription&gt; element node to which this description is added
	 */
	public void toXML(Node parentnode) {
		Document document = parentnode.getOwnerDocument();

		// Create the byte node
		Element bytenode = document.createElement(fType.getElementName());
		bytenode.setAttribute(ATTR_BYTE_INDEX, Integer.toString(fIndex));
		bytenode.setAttribute(ATTR_BYTE_NAME, fName);

		if (fDefaultValue != -1) {
			Element defaultnode = document.createElement(ELEMENT_DEFAULT);
			defaultnode.setAttribute(ATTR_DEFAULT_VALUE, toHex(fDefaultValue));
			bytenode.appendChild(defaultnode);
		}

		// add the bitfield description elements
		for (BitFieldDescription bfd : fBitFieldList) {
			bfd.toXML(bytenode);
		}

		parentnode.appendChild(bytenode);
	}

	/**
	 * Format the given integer to a String with the format "0xXX".
	 * <p>
	 * Unlike the normal <code>Integer.toHexString(i)</code> method, this method will always
	 * produce two digits, even with the high nibble at zero, and will output the hex value in
	 * uppercase. This should make the value more readable than the standard
	 * <code>Integer.toHexString</code> output.
	 * </p>
	 * 
	 * @param value
	 *            Single byte value
	 * @return String with the byte value as "0xXX"
	 */
	public static String toHex(int value) {
		String hex = "00" + Integer.toHexString(value);
		return "0x" + hex.substring(hex.length() - 2).toUpperCase();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(fType.getElementName());
		sb.append(" [");
		sb.append(" default=" + fDefaultValue);
		for (IBitFieldDescription bfd : fBitFieldList) {
			sb.append(", " + bfd.toString());
		}
		sb.append("]");

		return sb.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	public int compareTo(ByteDescription o) {

		if (fIndex == o.fIndex) {
			return 0;
		}
		if (fIndex > o.fIndex) {
			return 1;
		}
		return -1;
	}

}
