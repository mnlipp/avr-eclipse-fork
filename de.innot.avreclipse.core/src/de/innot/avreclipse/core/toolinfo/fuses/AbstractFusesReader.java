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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader;

/**
 * Fuses info reader.
 * <p>
 * This Class will take a PartDescriptionFile Document and read either the Fuse Byte or the Lockbit
 * settings from it. What to read is determined by the subclass.
 * </p>
 * <p>
 * The definitions of Fuses and Lockbits are very similar, therefore this class does most of the
 * parsing and the subclasses just need to supply a few informations.
 * </p>
 * <p>
 * 
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public abstract class AbstractFusesReader extends BaseReader {

	private final static String				FILE_POSTFIX	= ".desc";

	/** List of all Fuses Descriptions */
	private Map<String, IDescriptionHolder>	fFuseDescriptions;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#start()
	 */
	public void start() {
		fFuseDescriptions = new HashMap<String, IDescriptionHolder>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#parse(org.w3c.dom.Document)
	 */
	@Override
	public void parse(Document document) {

		// Find the <registers memspace="FUSE|LOCKBIT"> node.
		// Get all <registers> nodes and look for the one which has the
		// right memspace attribute. The memspace attribute name is supplied
		// by the subclass with the #getTargetNodeName() method.

		NodeList allregistersnodes = document.getElementsByTagName("registers");
		String targetmemspace = getTargetMemspace();
		Node registersnode = null;

		for (int i = 0; i < allregistersnodes.getLength(); i++) {
			Node node = allregistersnodes.item(i);
			if (node.hasAttributes()) {
				NamedNodeMap attributes = allregistersnodes.item(i).getAttributes();
				Node memspaceattr = attributes.getNamedItem("memspace");
				if (memspaceattr != null) {
					if (targetmemspace.equals(memspaceattr.getTextContent())) {
						registersnode = node;
						break;
					}
				}
			}
		}

		if (registersnode == null) {
			// No target node found (some MCUs don't have fuses / lockbits)
			return;
		}

		// OK, we have right node.

		// Read all enumerators. The Enumerators are siblings of our target node.
		Map<String, List<BitFieldValue>> enumerators = new HashMap<String, List<BitFieldValue>>();
		Node child = registersnode.getParentNode().getFirstChild();
		while (child != null) {
			if ("enumerator".equals(child.getNodeName())) {
				readEnumeratorNode(child, enumerators);
			}
			child = child.getNextSibling();
		}

		// Analyze the Registers node
		IDescriptionHolder desc = null;

		desc = readRegistersNode(registersnode, enumerators);

		if (desc == null) {
			// no <registers> element found
			return;
		}

		// Now get the default values (if present)
		setDefaultValues(document, desc);

		// Add the description object to the internal list of all
		// descriptions.
		fFuseDescriptions.put(fMCUid, desc);

	}

	/**
	 * Return a new {@link IDescriptionHolder} for the given MCU and with the given number of bytes.
	 * <p>
	 * The number of bytes is currently between 0 and 3, but may be greater in future AVR MCUs.
	 * </p>
	 * 
	 * @param mcuid
	 * @param bytecount
	 * @return
	 */
	protected abstract IDescriptionHolder getDescriptionHolder(String mcuid, int bytecount);

	/**
	 * @param document
	 * @param fusedesc
	 */
	protected abstract void setDefaultValues(Document document, IDescriptionHolder fusedesc);

	/**
	 * Add the array of <code>BitFieldDescription</code> objects to the description holder.
	 * 
	 * @param desc
	 *            The description holder created by the {@link #getDescriptionHolder(String, int)}
	 *            call.
	 * @param index
	 *            The byte index for which to set the bitfield descriptions.
	 * @param name
	 *            The name for the byte as used in the part escription file, e.g. "LOW" or
	 *            "FUSEBYTE0"
	 * @param bitfields
	 *            The array of bitfields.
	 */
	protected abstract void addBitFields(IDescriptionHolder desc, int index, String name,
			BitFieldDescription[] bitfields);

	/**
	 * Get the name of the target Element.
	 * <p>
	 * The two subclasses will return either <code>FUSE</code> or <code>LOCKBIT</code>
	 * </p>
	 * 
	 * @return String with a valid module class name.
	 */
	protected abstract String getTargetMemspace();

	/**
	 * Get the storage destination folder for the fuse description files.
	 * <p>
	 * Override this method to supply a location.
	 * </p>
	 * 
	 * @see Fuses#getInstanceStorageLocation()
	 * 
	 * @return <code>IPath</code> to the instance storage area.
	 */
	protected abstract IPath getStoragePath();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader#finish()
	 */
	public void finish() {

		// The FuseDescription Objects are serialized to the plugin storage
		// area.

		// Serialization was chosen instead of text properties, because the
		// FuseDescription is a more complex object which would have required
		// some code to write to and read from a properties file. Object
		// serialization is much easier to code and, because the files are
		// generated and should not require manual modifications, this binary
		// storage format is suitable.
		// However, changes to the FuseDescription Class or its subclasses
		// should be made carefully not to break compatibility.

		// get the location where the descriptions will be written to.
		IPath location = getStoragePath();

		// Create the fusedesc folder if necessary
		File folder = location.toFile();
		if (!folder.isDirectory()) {
			// Create the folder
			if (!folder.mkdirs()) {
				IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
						"Can't create fusedesc folder [" + folder.toString() + "]", null);
				AVRPlugin.getDefault().log(status);
				// TODO Throw an Exception
				return;
			}
		}

		// Now serialize all FuseDescription Objects to the storage area
		Set<String> allmcus = fFuseDescriptions.keySet();

		for (String mcuid : allmcus) {
			// Generate a filename: "mcuid.desc"
			File file = location.append(mcuid + FILE_POSTFIX).toFile();

			IDescriptionHolder fusesdesc = fFuseDescriptions.get(mcuid);

			FileOutputStream fos = null;
			ObjectOutputStream out = null;
			try {
				// Write the Object
				fos = new FileOutputStream(file);
				out = new ObjectOutputStream(fos);
				out.writeObject(fusesdesc);
				out.close();
			} catch (IOException ioe) {
				IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
						"Can't serialize the FuseDescription Object for " + mcuid, ioe);
				AVRPlugin.getDefault().log(status);
				// TODO throw an Exception to notify the caller
				// For now we just continue and try the next object.
			}
		}
	}

	/**
	 * Read the given &lt;enumerator&gt; node and add its content to the given global enumerators
	 * list.
	 * <p>
	 * All &lt;enum&gt; children of the &lt;enumerator&gt; node are collected in a list of
	 * BitFieldValue objects and mapped to the name of the enumerator node, which is then added to
	 * the global list.
	 * </p>
	 * 
	 * @param node
	 *            A &lt;enumerator&gt; node
	 * @param enums
	 *            The global map of all enumerator names and their <code>BitFieldValue</code>
	 *            lists
	 */
	private void readEnumeratorNode(Node node, Map<String, List<BitFieldValue>> enums) {

		List<BitFieldValue> values = new ArrayList<BitFieldValue>();

		// Get the name of the <enumerator>
		NamedNodeMap attrs = node.getAttributes();
		Node nameattr = attrs.getNamedItem("name");
		String enumname = nameattr.getTextContent();

		// Get all <enum> children
		NodeList children = node.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			if ("enum".equalsIgnoreCase(child.getNodeName())) {
				BitFieldValue value = readEnumNode(child);
				values.add(value);
			}
		}

		enums.put(enumname, values);
	}

	/**
	 * Read the given &lt;enum&gt; node and return a new BitFieldValue with the descriptive text and
	 * the value of the enum node.
	 * 
	 * @param node
	 *            A &lt;enum&gt; node
	 * @return New <code>BitFieldValue</code> object with the values from the "text" and "val"
	 *         attributes.
	 */
	private BitFieldValue readEnumNode(Node node) {
		BitFieldValue value = new BitFieldValue();

		NamedNodeMap attrs = node.getAttributes();

		value.text = attrs.getNamedItem("text").getNodeValue();
		value.value = Integer.decode(attrs.getNamedItem("val").getNodeValue());

		return value;
	}

	/**
	 * @param node
	 *            A &lt;registers memspace="FUSE|LOCKBIT"&gt; node.
	 * @param enumerators
	 *            The global list of enumerators for this node.
	 * @return
	 */
	private IDescriptionHolder readRegistersNode(Node node,
			Map<String, List<BitFieldValue>> enumerators) {

		List<Node> regnodes = new ArrayList<Node>();
		List<Integer> offsetlist = new ArrayList<Integer>();
		List<String> namelist = new ArrayList<String>();

		int bytecount = 0;

		// The reading is done in two steps.
		// 1. Read all <reg> nodes to get the total count of bytes.
		// 2. Read the <bitfield> nodes of each <reg> node to add the actual
		// information.

		// Get the <reg> nodes
		Node regnode = node.getFirstChild();

		while (regnode != null) {
			if ("reg".equals(regnode.getNodeName())) {
				bytecount++;
				regnodes.add(regnode);

				// read the offset attribute and store it
				// This is used to get the correct order of the bytes (they
				// are in reverse order within the document).
				// We also read the name of the byte. This is later used to
				// map to the correct name for avrdude.
				Node offsetattr = regnode.getAttributes().getNamedItem("offset");
				Integer offsetvalue = Integer.decode(offsetattr.getTextContent());
				offsetlist.add(offsetvalue);

				Node nameattr = regnode.getAttributes().getNamedItem("name");
				if (nameattr != null) {
					String namevalue = nameattr.getTextContent();
					namelist.add(namevalue);
				} else {
					namelist.add("");
				}
			}
			regnode = regnode.getNextSibling();
		}

		// Get a description holder object for the accumulated number of fuse
		// bytes from the subclass
		IDescriptionHolder desc = getDescriptionHolder(fMCUid, bytecount);

		// Now we can read the bitfields for each node
		for (int i = 0; i < regnodes.size(); i++) {
			regnode = regnodes.get(i);

			// Get the child <bitfield> nodes and create a new bitfield list
			Node bitfieldnode = regnode.getFirstChild();
			List<BitFieldDescription> bitfields = new ArrayList<BitFieldDescription>();

			while (bitfieldnode != null) {
				if ("bitfield".equals(bitfieldnode.getNodeName())) {
					BitFieldDescription bitfield = readBitfieldNode(bitfieldnode, enumerators);
					bitfields.add(bitfield);
				}
				bitfieldnode = bitfieldnode.getNextSibling();
			}

			// all bitfields have been read
			// Add them to the description
			addBitFields(desc, offsetlist.get(i), namelist.get(i), bitfields
					.toArray(new BitFieldDescription[bitfields.size()]));
		}

		return desc;
	}

	/**
	 * @param node
	 *            A &lt;bitfield&gt; node
	 * @param enumerators
	 *            The global list of enumerators for this node.
	 * @return A <code>BitFieldDescription</code> object representing the given &lt;bitfield&gt;
	 *         node.
	 */
	private BitFieldDescription readBitfieldNode(Node node,
			Map<String, List<BitFieldValue>> enumerators) {

		BitFieldDescription bitfield = new BitFieldDescription();

		// Get the Attributes of the <bitfield> node
		NamedNodeMap attrs = node.getAttributes();
		bitfield.name = attrs.getNamedItem("name").getTextContent();
		bitfield.description = attrs.getNamedItem("text").getTextContent();
		bitfield.mask = Integer.decode(attrs.getNamedItem("mask").getTextContent());

		Node enumattrnode = attrs.getNamedItem("enum");
		if (enumattrnode != null) {
			String enumname = enumattrnode.getTextContent();
			List<BitFieldValue> values = enumerators.get(enumname);
			if (values == null) {
				System.out.println("Found non-existing enum value: " + enumname);
			}
			bitfield.values = values;
		}

		return bitfield;
	}

}
