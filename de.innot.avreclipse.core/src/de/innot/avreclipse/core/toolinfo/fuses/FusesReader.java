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

import org.eclipse.core.runtime.IPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class FusesReader extends AbstractFusesReader {

	/** Names of the three fuse bytes as used in the part description files */
	private final static String[]	FUSENAMES	= new String[] { "LOW", "HIGH", "EXTENDED" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#getDescriptionHolder(java.lang.String,
	 *      int)
	 */
	@Override
	protected IDescriptionHolder getDescriptionHolder(String mcuid, int bytecount) {
		return new FusesDescription(mcuid, bytecount);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#addBitFields(de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder,
	 *      int, de.innot.avreclipse.core.toolinfo.fuses.BitFieldDescription[])
	 */
	@Override
	protected void addBitFields(IDescriptionHolder desc, int index, BitFieldDescription[] bitfields) {
		FusesDescription fusesdesc = (FusesDescription) desc;
		fusesdesc.setBitFieldDescriptions(index, bitfields);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#getStoragePath()
	 */
	@Override
	protected IPath getStoragePath() {
		// The default is to get the folder from the {@link Fuses} class.
		return Fuses.getDefault().getInstanceStorageLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#getNode(org.w3c.dom.Document)
	 */
	@Override
	protected String getTargetNodeName() {
		return "FUSE";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.AbstractFusesReader#setDefaultValues(org.w3c.dom.Document,
	 *      de.innot.avreclipse.core.toolinfo.fuses.IDescriptionHolder)
	 */
	@Override
	protected void setDefaultValues(Document document, IDescriptionHolder desc) {
		FusesDescription fusesdesc = (FusesDescription) desc;

		// Get the <FUSE> nodes (from the old, pre-V2, part of the PDF)
		NodeList allfusenodes = document.getElementsByTagName("FUSE");
		for (int i = 0; i < allfusenodes.getLength(); i++) {
			Node fusenode = allfusenodes.item(i);

			String[] fusenames = new String[] {};

			// The <LIST> child node has the name of all Fuse Bytes defined
			Node childnode = fusenode.getFirstChild();
			while (childnode != null) {
				if ("LIST".equalsIgnoreCase(childnode.getNodeName())) {
					// Get the value of the <LIST> node, remove the Brackets and
					// split it into the separate fusenames.
					Node valuenode = childnode.getFirstChild();
					String allnames = valuenode.getNodeValue();
					allnames = allnames.substring(1, allnames.length() - 1);
					fusenames = allnames.split(":");
					break;
				}
				childnode = childnode.getNextSibling();
			}

			// Now we have the names of the fuse bytes
			// Iterate once again through all child nodes, this time look for a node
			// which matches one of the fusenames. Once found pass this node to
			// getDefaultValue() to read all fuse bits and their default value.

			childnode = fusenode.getFirstChild();
			while (childnode != null) {
				for (String fuse : fusenames) {
					if (fuse.equalsIgnoreCase(childnode.getNodeName())) {
						// Found the fuse node in the childnodes
						int defaultvalue = readFuseByteNode(childnode);
						int fuseindex = nameToIndex(childnode.getNodeName());
						fusesdesc.setDefaultValue(fuseindex, defaultvalue);
					}
				}
				childnode = childnode.getNextSibling();
			}
		}

	}

	/**
	 * With the given fuse byte element (&lt;LOW&gt;, &lt;HIGH&gt; or &lt;EXTENDED&gt;) extract the
	 * default values.
	 * <p>
	 * For all bits of the byte which do not have a &lt;DEFAULT&gt; element, <code>1</code> is
	 * used as a default value (= unset fusebit)
	 * </p>
	 * 
	 * @param fusenode
	 *            The fuse byte element node.
	 * @return An <code>int</code> with a byte value (0x00-0xff).
	 */
	private int readFuseByteNode(Node fusenode) {

		int value = 0xFF;

		// We could read the <NMB_FUSE_BITS> node to get the number of fuse
		// bits. However we don't bother and read just interpret all <FUSEx>
		// child nodes that are there.
		Node bitnode = fusenode.getFirstChild();
		while (bitnode != null) {
			String nodename = bitnode.getNodeName();
			if (nodename.startsWith("FUSE")) {
				String bitnumberstring = nodename.substring(4);
				int bitnumber = Integer.parseInt(bitnumberstring);
				int bitvalue = readFuseBitNode(bitnode);
				// Clear the bit if <DEFAULT>0</DEFAULT>
				if (bitvalue == 0)
					value &= ~(1 << bitnumber);
				// we don't need to set bits because they are set by default.
			}

			bitnode = bitnode.getNextSibling();
		}
		return value;
	}

	/**
	 * With the given &lt;FUSEx&gt; element node, return the value of the &lt;DEFAULT&gt; child
	 * element.
	 * 
	 * @param bitnode
	 *            A &lt;FUSEx&gt; element node.
	 * @return The value of the &lt;DEFAULT&gt; element, or <code>1</code> if no &lt;DEFAULT&gt;
	 *         element exists.
	 */
	private int readFuseBitNode(Node bitnode) {

		Node childnode = bitnode.getFirstChild();
		while (childnode != null) {

			String nodename = childnode.getNodeName();
			if ("DEFAULT".equalsIgnoreCase(nodename)) {
				// This is the right node. Get its value
				// The value is in the first child (the TEXT node)
				String value = childnode.getFirstChild().getNodeValue();
				return Integer.parseInt(value, 2);
			}
			childnode = childnode.getNextSibling();
		}

		// Did not find any <DEFAULT> Node.
		// Return 1, which means "not set"
		return 1;
	}

	/**
	 * Convert a fuse byte name to an index.
	 * <p>
	 * The names (and their order) are defined in the {@link AbstractFusesReader#FUSENAMES} array.
	 * If the given name matches an entry in this array, the ordinal number of the entry is
	 * returned.
	 * </p>
	 * <p>
	 * Currently there are three names supported: <code>LOW</code>, <code>HIGH</code> and
	 * <code>EXTENDED</code>. More fusebytes names can be supported by adding them to
	 * <code>FUSENAMES</code>
	 * </p>
	 * 
	 * @param name
	 *            Name of the fuse byte (LOW, HIGH or EXTENDED)
	 * @return 1, 2 or 3 respectively. Or -1 if the name is not recognized.
	 */
	private int nameToIndex(String name) {

		for (int i = 0; i < FUSENAMES.length; i++) {
			if (FUSENAMES[i].equalsIgnoreCase(name)) {
				return i;
			}
		}

		// Name not found;
		return -1;
	}

}
