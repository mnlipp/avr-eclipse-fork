/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.core.toolinfo.partdescriptionfiles;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.toolinfo.fuses.BitFieldDescription;
import de.innot.avreclipse.core.toolinfo.fuses.BitFieldValueDescription;
import de.innot.avreclipse.core.toolinfo.fuses.ByteDescription;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;
import de.innot.avreclipse.core.toolinfo.fuses.Fuses;
import de.innot.avreclipse.core.toolinfo.fuses.IMCUDescription;
import de.innot.avreclipse.core.toolinfo.fuses.MCUDescription;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * Fuses info reader.
 * <p>
 * This Class will take a Atmel Device XML Document and read either the Fuse Byte or the Lockbit
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
public class FusesReader extends BaseReader {

	private final static String			FILE_POSTFIX		= ".desc";

	private final static String			ELEM_REGISTER_GROUP	= "register-group";
	private final static String			ELEM_REGISTER		= "register";
	private final static String			ELEM_BITFIELD		= "bitfield";
	private final static String			ELEM_VALUE_GROUP	= "value-group";
	private final static String			ELEM_VALUE			= "value";

	private final static String			ATTR_CAPTION		= "caption";
	private final static String			ATTR_TEXT			= "text";
	private final static String			ATTR_OFFSET			= "offset";
	private final static String			ATTR_SIZE			= "size";
	private final static String			ATTR_MASK			= "mask";
	private final static String			ATTR_VALUES			= "values";
	private final static String			ATTR_VALUE			= "value";

	/** List of all Fuses Descriptions */
	private Map<String, MCUDescription>	fFuseDescriptions;

	// FIXME: debugging only
	File								fFile;

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#start()
	 */
	public void start() {
		fFuseDescriptions = new HashMap<String, MCUDescription>();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#parse(org.w3c.dom.Document)
	 */
	@Override
	public void parse(Document document, File sourcefile) {

		fFile = sourcefile;

		// initialize the description object we will fill with data
		MCUDescription desc = new MCUDescription(fMCUid);

		// Read all <value-group> nodes and store them in a hashmap indexed by their name for easy
		// access later on.
		Map<String, Node> bitfieldValueNodes = new HashMap<String, Node>();
		NodeList valueGroupList = document.getElementsByTagName(ELEM_VALUE_GROUP);
		for (int i = 0; i < valueGroupList.getLength(); i++) {
			Node node = valueGroupList.item(i);
			Node nameAttr = node.getAttributes().getNamedItem(ATTR_NAME);
			String name = nameAttr.getNodeValue();
			name = valueGroupNameFixer(desc, name);
			bitfieldValueNodes.put(name, node);
		}

		// Get all <register-group> nodes and filter those with an attribute of either
		// name="*FUSE*", i.e. "FUSE", "FUSES" or "NVM_FUSES"
		NodeList registerGroupList = document.getElementsByTagName(ELEM_REGISTER_GROUP);
		for (int i = 0; i < registerGroupList.getLength(); i++) {
			Node registerGroupNode = registerGroupList.item(i);
			NamedNodeMap attributesMap = registerGroupNode.getAttributes();
			Node nameAttributeNode = attributesMap.getNamedItem(ATTR_NAME);
			String nameValue = nameAttributeNode.getNodeValue();
			if (nameValue.contains("FUSE")) {
				readRegisters(registerGroupNode, desc, FuseType.FUSE, bitfieldValueNodes);
			}
			if (nameValue.contains("LOCKBIT")) {
				readRegisters(registerGroupNode, desc, FuseType.LOCKBITS, bitfieldValueNodes);
			}
		}

		// and the build version and the status
		setVersion(sourcefile, desc);

		// Add the description object to the internal list of all
		// descriptions.
		fFuseDescriptions.put(fMCUid, desc);

	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader#finish()
	 */
	public void finish() {

		// The FuseDescription Objects are written as xml files to the plugin storage area.

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
			FileOutputStream fos = null;

			MCUDescription fusesdesc = fFuseDescriptions.get(mcuid);

			// Create a blank XML DOM document...
			try {

				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				DocumentBuilder parser = factory.newDocumentBuilder();
				Document document = parser.newDocument();

				// Add a few comments to the document
				Comment comment = document
						.createComment("Fuse/Lockbit description file for the AVR Eclipse plugin");
				document.appendChild(comment);

				comment = document
						.createComment("Author: automatically created by AVR Eclipse plugin");
				document.appendChild(comment);

				comment = document.createComment("Date: "
						+ new SimpleDateFormat().format(new Date()));
				document.appendChild(comment);

				comment = document.createComment("Based on: Atmel Device File \""
						+ AVRMCUidConverter.id2name(mcuid) + ".xml\"");
				document.appendChild(comment);

				// And now add the Actual Description to the document
				fusesdesc.toXML(document);

				// Use a Transformer for output
				TransformerFactory tFactory = TransformerFactory.newInstance();
				Transformer transformer = tFactory.newTransformer();
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");

				DOMSource source = new DOMSource(document);

				fos = new FileOutputStream(file);

				StreamResult result = new StreamResult(fos);
				transformer.transform(source, result);

				fos.close();

				// if (false) {
				// // Short fragment I used for debugging.
				// // This dumps all BitFieldDescriptions into a large CSV file,
				// // which can then be imported and analyzed with a Database.
				// FileWriter fw = new FileWriter(new File("allfuses.csv"), true);
				// for (FuseType type : FuseType.values()) {
				// List<IByteDescription> list = fusesdesc.getByteDescriptions(type);
				// for (IByteDescription desc : list) {
				// List<BitFieldDescription> bfdlist = desc.getBitFieldDescriptions();
				// for (BitFieldDescription bfd : bfdlist) {
				// fw.write(mcuid + "; " + type.name() + "; " + bfd.getName() + "; \""
				// + bfd.getDescription() + "\"; " + bfd.getMaxValue() + "\n");
				// }
				// }
				// }
				// fw.close();
				// }

			} catch (ParserConfigurationException pce) {
				IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
						"Could not create a XML object for " + mcuid, pce);
				AVRPlugin.getDefault().log(status);
				// TODO throw an Exception to notify the caller
				// For now we just continue and try the next object.
			} catch (TransformerException te) {
				IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
						"Could not transform the XML content for " + mcuid, te);
				AVRPlugin.getDefault().log(status);
				// TODO throw an Exception to notify the caller
				// For now we just continue and try the next object.
			} catch (FileNotFoundException fnfe) {
				IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
						"Could not create the MCUDescription file for " + mcuid, fnfe);
				AVRPlugin.getDefault().log(status);
				// TODO throw an Exception to notify the caller
				// For now we just continue and try the next object.
			} catch (IOException ioe) {
				IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
						"Could not write the MCUDescription file for " + mcuid, ioe);
				AVRPlugin.getDefault().log(status);
				// TODO throw an Exception to notify the caller
				// For now we just continue and try the next object.
			}
		} // for loop

	}

	/**
	 * Get the storage destination folder for the fuse description files.
	 * <p>
	 * Override this method to supply a different location.
	 * </p>
	 * 
	 * @see Fuses#getInstanceStorageLocation()
	 * 
	 * @return <code>IPath</code> to the instance storage area.
	 */
	protected IPath getStoragePath() {
		// The default is to get the folder from the {@link Fuses} class.
		return Fuses.getDefault().getInstanceStorageLocation();
	}

	/**
	 * Read all fuse or lockbits bytes from the given &lt;registry-group&gt; node.
	 * 
	 * @param registerGroupNode
	 *            A &lt;register-group&gt; node for fuses
	 * @param desc
	 *            A <code>MCUDescription</code> container which will be filled with the
	 *            <code>ByteDescription</code> objects for each byte.
	 */
	private void readRegisters(Node registerGroupNode, MCUDescription desc, FuseType type,
			Map<String, Node> valueGroupList) {
		// Now get and parse all <register> nodes. Each <register> node stands for one fuses /
		// lockbits object (either 8 bit or 32 bit).
		Node registerNode = registerGroupNode.getFirstChild();

		while (registerNode != null) {

			if (ELEM_REGISTER.equalsIgnoreCase(registerNode.getNodeName())) {

				// Get the caption, name, offset and size of this fuse register.
				NamedNodeMap attributesMap = registerNode.getAttributes();
				Node captionNode = attributesMap.getNamedItem(ATTR_CAPTION);
				Node nameNode = attributesMap.getNamedItem(ATTR_NAME);
				Node sizeNode = attributesMap.getNamedItem(ATTR_SIZE);
				Node offsetNode = attributesMap.getNamedItem(ATTR_OFFSET);

				// Sanity check
				if (captionNode == null || nameNode == null || sizeNode == null
						|| offsetNode == null) {
					System.err.println("Missing attribute in the XML for MCU " + desc.getMCUId());
				}

				String caption = captionNode.getNodeValue();
				String name = nameNode.getNodeValue();
				int offset = hex2int(offsetNode.getNodeValue());
				int size = Integer.parseInt(sizeNode.getNodeValue());

				ByteDescription bytedesc = new ByteDescription(type, caption, name, offset, size,
						-1);

				// Now we can read the bitfields for each node
				Node bitfieldnode = registerNode.getFirstChild();
				while (bitfieldnode != null) {
					if (ELEM_BITFIELD.equals(bitfieldnode.getNodeName())) {
						readBitfieldNode(bytedesc, bitfieldnode, valueGroupList, offset);
					}
					bitfieldnode = bitfieldnode.getNextSibling();
				}

				desc.addByteDescription(type, bytedesc);
			}

			registerNode = registerNode.getNextSibling();
		}
	}

	/**
	 * @param desc
	 *            The fuses / lockbits object onto which to add this bitfield description.
	 * @param node
	 *            A &lt;bitfield&gt; node
	 * @param valuesGroupNodeMap
	 *            The map of all &lt;value-group&gt; element nodes.
	 * @param index
	 *            the fuse byte index this bitfield is for.
	 * @return A <code>BitFieldDescription</code> object representing the given &lt;bitfield&gt;
	 *         node.
	 */
	private void readBitfieldNode(ByteDescription desc, Node node,
			Map<String, Node> valuesGroupNodeMap, int index) {

		List<BitFieldValueDescription> valuesList = null;

		// Get the Attributes of the <bitfield> node
		NamedNodeMap attrs = node.getAttributes();
		String name = attrs.getNamedItem(ATTR_NAME).getTextContent();
		Long mask = Long.decode(attrs.getNamedItem(ATTR_MASK).getTextContent());

		// The caption attribute is optional. If it is not set then take the name of the value as
		// its description.
		Node descriptionNode = attrs.getNamedItem(ATTR_CAPTION);
		String description = descriptionNode != null ? descriptionNode.getNodeValue() : name;

		// The values attribute is optional as well. It contains the name of a <values-group>
		// element.
		Node valuesNode = attrs.getNamedItem(ATTR_VALUES);

		if (valuesNode != null) {
			String valuesRefName = valuesNode.getNodeValue();
			Node valueGroupNode = valuesGroupNodeMap.get(valuesRefName);
			if (valueGroupNode != null) {
				valuesList = readValueGroupNode(valueGroupNode);
			} else {
				System.out.println(fFile.getName()
						+ " Found non-existing <value-group> reference: " + valuesRefName);
			}
		}

		// Check if this bitfield is one that contains errors in the part description files and fix
		// the errors.
		name = bitfieldNameFixer(name, mask, description);

		BitFieldDescription bitfield = new BitFieldDescription(index, name, description,
				mask.intValue(), -1, valuesList);

		desc.addBitFieldDescription(bitfield);
	}

	/**
	 * Read the given &lt;value-group&gt; node and add its content to the given global enumerators
	 * list.
	 * <p>
	 * All &lt;value&gt; children of the &lt;value-group&gt; node are collected in a list of
	 * BitFieldValueDescription objects and mapped to the name of the enumerator node, which is then
	 * added to the global list.
	 * </p>
	 * 
	 * @param node
	 *            A &lt;enumerator&gt; node
	 * @param enums
	 *            The global map of all enumerator names and their
	 *            <code>BitFieldValueDescription</code> lists
	 */
	private List<BitFieldValueDescription> readValueGroupNode(Node valueGroupNode) {

		List<BitFieldValueDescription> valuesList = new ArrayList<BitFieldValueDescription>();

		// Get all <value> children
		Node valueNode = valueGroupNode.getFirstChild();
		while (valueNode != null) {
			if (ELEM_VALUE.equals(valueNode.getNodeName())) {
				BitFieldValueDescription bfvd = readValueNode(valueNode);
				valuesList.add(bfvd);
			}
			valueNode = valueNode.getNextSibling();
		}

		return valuesList;
	}

	/**
	 * Read the given &lt;value&gt; node and return a new BitFieldValueDescription with the
	 * descriptive text and the value of the value node.
	 * 
	 * @param node
	 *            A &lt;value&gt; node
	 * @return New <code>BitFieldValueDescription</code> object with the values from the "caption",
	 *         "name" and "value" attributes.
	 */
	private BitFieldValueDescription readValueNode(Node node) {

		NamedNodeMap attrs = node.getAttributes();

		String name = attrs.getNamedItem(ATTR_NAME).getNodeValue();
		int value = Integer.decode(attrs.getNamedItem(ATTR_VALUE).getNodeValue());

		// The caption attribute is optional. If it is not set then take the name of the value as
		// its description.
		Node descriptionNode = attrs.getNamedItem(ATTR_CAPTION);
		String description = descriptionNode != null ? descriptionNode.getNodeValue() : name;

		// The same goes for the text attribute, which is used by the AVR32 files instead of the
		// caption attribute.
		Node textNode = attrs.getNamedItem(ATTR_TEXT);
		description = textNode != null ? textNode.getNodeValue() : description;

		return new BitFieldValueDescription(value, description, name);
	}

	/**
	 * Set the version of the {@link IMCUDescription} to the last modification date of the source
	 * file.
	 * 
	 * @param sourcefile
	 *            Device description file
	 * @param desc
	 *            The MCUDescription
	 */
	private void setVersion(File sourcefile, MCUDescription desc) {

		// get the file date and store it as the version number
		long lastmodified = sourcefile.lastModified();
		Date date = new Date(lastmodified);
		SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
		String versionString = df.format(date);
		int version = Integer.parseInt(versionString);

		desc.setVersion(version);
	}

	/**
	 * With the given parameters check if they match a few obvious errors in the part description
	 * files and fix them.
	 * <p>
	 * As long as the description files generated by the plugin are only used by the plugin this
	 * should be save. If we ever need to interoperate with some external tools this modification of
	 * the fuses information needs to be checked to avoid compatibility issues.
	 * </p>
	 * 
	 * @param name
	 * @param mask
	 * @param description
	 * @return The fixed name, or the original name if no fix was required.
	 */
	private String bitfieldNameFixer(String name, long mask, String description) {
		if (name.equals("WTDON") && description.startsWith("Watch-Dog Timer")) {
			// This is a typo in the atmega8.xml file
			// Should be "WDTON", like in all other MCUs.
			return "WDTON";
		}

		// The atmega103.xml file is really fucked up. All Fuses names are wrong in the part
		// description file! We can fix two of them, but because at this point we do not know the
		// MCU Id, we cannot fix the last bug: The "CKSEL" field is in reality a "SUT" as
		// documented in the datasheet.

		// AVR Studio 5.0 does not have a file for the ATmega103 anymore. But we leave this in
		// anyway

		if (name.equals("CKSEL3") && description.startsWith("Preserve EEPROM")) {
			// According to the description this bitfield should be "EESAVE"
			return "EESAVE";
		}
		if (name.equals("SUT1") && description.startsWith("Serial program")) {
			// According to the description this bitfield should be "SPIEN"
			return "SPIEN";
		}

		return name;
	}

	/**
	 * Try to fix some obvious errors in the Atmel device files.
	 * <p>
	 * This method is called with the name of the &lt;value-group&gt; element and returns the name
	 * that matches the values attribute of the &lt;bitfield&gt; element.
	 * </p>
	 * 
	 * @param desc
	 *            IMCUDescrtiption with the current MCU.
	 * @param name
	 *            of the &lt;value-group&gt; element.
	 * @return name used by the value attribute.
	 */
	private String valueGroupNameFixer(MCUDescription desc, String name) {

		String mcuid = desc.getMCUId();

		// Fix for the AT32UC3C1512C.xml & AT32UC3C2512C.xml files
		// The BODxx references all end with "_VALUES", while the actual <value-group> names do not
		if (mcuid.equals("at32uc3c1512c") || mcuid.equals("at32uc3c2512c")) {

			if (name.equals("BOD33EN")) {
				return "BODEN33_VALUES";
			}
			if (name.equals("BODEN") || name.equals("BODHYST") || name.equals("BODLEVEL")) {
				return name + "_VALUES";
			}
		}

		// Fix for the AT32UC3C0512C.xml file.
		// The reference is "BODEN33", while the actual <value-group> name is "BOD33EN".
		if (mcuid.equals("at32uc3c0512c") && name.equals("BOD33EN")) {
			return "BODEN33";
		}

		return name;
	}

	/**
	 * Convert a hex String to an integer.
	 * <p>
	 * The hex string may or may not start with '0x'.
	 * </p>
	 * 
	 * @param hex
	 * @return
	 */
	private int hex2int(String hex) {
		String value = hex.toLowerCase();
		if (value.startsWith("0x")) {
			value = value.substring(2);
		}

		return Integer.parseInt(value, 16);
	}
}
