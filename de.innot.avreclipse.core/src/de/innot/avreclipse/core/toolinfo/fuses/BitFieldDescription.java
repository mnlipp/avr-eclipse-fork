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

public class BitFieldDescription implements IBitFieldDescription {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IBitFieldDescription#getIndex()
	 */
	public int getIndex() {
		return fIndex;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IBitFieldDescription#getName()
	 */
	public String getName() {
		return fName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IBitFieldDescription#getDescription()
	 */
	public String getDescription() {
		return fDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IBitFieldDescription#getMask()
	 */
	public int getMask() {
		return fMask;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IBitFieldDescription#getMaxValue()
	 */
	public int getMaxValue() {
		return (2 << (Integer.bitCount(fMask) - 1)) - 1;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IBitFieldDescription#valueToBitfield(int)
	 */
	public int valueToBitfield(int value) {
		// left-shift the value to the right place (as determined by the mask)
		return value << Integer.numberOfTrailingZeros(fMask);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IBitFieldDescription#bitfieldToValue(int)
	 */
	public int bitfieldToValue(int bitfieldvalue) {
		// Mask the appropriate bits and rightshift (normalize) the result
		int masked = bitfieldvalue & fMask;
		return masked >> Integer.numberOfTrailingZeros(fMask);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IBitFieldDescription#getValues()
	 */
	public List<IBitFieldValueDescription> getValues() {
		if (fValues != null) {
			return new ArrayList<IBitFieldValueDescription>(fValues);
		}
		return null;
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
