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
 * This Class will take a PartDescriptionFile Document and read the Fuse Byte settings from it.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public abstract class FusesReader extends BaseReader {

	private final static String				FILE_POSTFIX	= ".desc";

	/** List of all Fuses Descriptions */
	private Map<String, FusesDescription>	fFuseDescriptions;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#start()
	 */
	public void start() {
		fFuseDescriptions = new HashMap<String, FusesDescription>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#parse(org.w3c.dom.Document)
	 */
	@Override
	public void parse(Document document) {

		// Set up the Fuse info object
		FusesDescription fusedesc = new FusesDescription(fMCUid);

		Map<String, List<BitFieldValue>> enumerators = new HashMap<String, List<BitFieldValue>>();

		// Find the <module class="FUSE"> node.
		// Get all <module> nodes and look for the one with the attribute
		// "class"
		NodeList modulenodes = document.getElementsByTagName("module");
		Node targetmodule = null;

		for (int i = 0; i < modulenodes.getLength(); i++) {
			Node node = modulenodes.item(i);
			if (node.hasAttributes()) {
				NamedNodeMap attributes = modulenodes.item(i).getAttributes();
				Node classattr = attributes.getNamedItem("class");
				if (classattr != null) {
					String targetnode = getTargetNodeName();
					if (targetnode.equals(classattr.getTextContent())) {
						targetmodule = node;
						break;
					}
				}
			}
		}

		if (targetmodule == null) {
			// No target node found (some MCUs don't have fuses)
			return;
		}

		// OK, we have Fuses. The Fuse module has one "registers" node with the
		// actual definitions and zero or more "enumerator" nodes
		// which contain human readable presets.

		// Read all enumerators
		Node child = targetmodule.getFirstChild();
		while (child != null) {
			if ("enumerator".equals(child.getNodeName())) {
				readEnumeratorNode(child, enumerators);
			}
			child = child.getNextSibling();
		}

		// Analyze the Registers node
		child = targetmodule.getFirstChild();
		while (child != null) {
			if ("registers".equals(child.getNodeName())) {
				readRegisterNode(child, fusedesc, enumerators);
			}
			child = child.getNextSibling();
		}

		// Now get the default values (if present)
		setDefaultValues(document, fusedesc);

		// Add the description object to the internal list of all
		// descriptions.
		fFuseDescriptions.put(fMCUid, fusedesc);

	}

	/**
	 * Get the name of the target Element.
	 * <p>
	 * The two subclasses will return either <code>FUSE</code> or <code>LOCKBIT</code>
	 * </p>
	 * 
	 * @return String with a valid module class name.
	 */
	protected abstract String getTargetNodeName();

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

			FusesDescription fusesdesc = fFuseDescriptions.get(mcuid);

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

	private BitFieldValue readEnumNode(Node node) {
		BitFieldValue value = new BitFieldValue();

		NamedNodeMap attrs = node.getAttributes();

		value.text = attrs.getNamedItem("text").getNodeValue();
		value.value = Integer.decode(attrs.getNamedItem("val").getNodeValue());

		return value;
	}

	private void readRegisterNode(Node node, FusesDescription fusedesc,
			Map<String, List<BitFieldValue>> enumerators) {

		List<Node> regnodes = new ArrayList<Node>();
		List<Integer> offsetlist = new ArrayList<Integer>();

		int fusebytecount = 0;

		// The reading is done in two steps.
		// 1. Read all <reg> nodes to get the total count of bytes.
		// 2. Read the <bitfield> nodes of each <reg> node to add the actual
		// information.

		// Get the <reg> nodes
		Node regnode = node.getFirstChild();

		while (regnode != null) {
			if ("reg".equals(regnode.getNodeName())) {
				fusebytecount++;
				regnodes.add(regnode);

				// read the offset attribute and store it
				// This is used to get the order of the fusebytes correct (they
				// are in reverse order within the document).
				// Note: We do not use the name attribute, because we handle the
				// assignment of names ourself to be compatible with avrdude.
				Node offsetattr = regnode.getAttributes().getNamedItem("offset");
				Integer offsetvalue = Integer.decode(offsetattr.getTextContent());
				offsetlist.add(offsetvalue);
			}
			regnode = regnode.getNextSibling();
		}

		// Initialize the FusesDescription for the accumulated number of fuse
		// bytes
		fusedesc.setFuseByteCount(fusebytecount);

		// Now we can read the bitfields for each node
		for (int i = 0; i < regnodes.size(); i++) {
			regnode = regnodes.get(i);

			// Get the child <bitfield> nodes and prepare a new bitfield list
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
			fusedesc.setFuseBitFields(offsetlist.get(i), bitfields
					.toArray(new BitFieldDescription[bitfields.size()]));
		}

	}

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

	protected abstract void setDefaultValues(Document document, FusesDescription fusedesc);

}
