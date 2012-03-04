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
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * A basic implementation of an PartDescriptionFile reader.
 * <p>
 * This class will fetch the MCU id from the given Document and pass the document to the
 * {@link #parse(Document)} method of the subclass.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public abstract class BaseReader implements IPDFreader {

	/** Element name for the MCU type */
	private final static String		ELEM_DEVICE	= "device";
	protected final static String	ATTR_NAME	= "name";

	/** The MCU id value as read from the part description file */
	protected String				fMCUid;

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader#read(org.w3c.dom.Document)
	 */
	public void read(File xmlfile) {

		Document document = getDocument(xmlfile);

		// Get the MCU name from the <device> element
		NodeList nodes = document.getElementsByTagName(ELEM_DEVICE);
		NamedNodeMap attributes = nodes.item(0).getAttributes();
		Node node = attributes.getNamedItem(ATTR_NAME);
		String partname = node.getNodeValue();
		fMCUid = AVRMCUidConverter.name2id(partname);

		if (partname.endsWith("comp")) {
			// ignore entries ending with "comp".
			// These are the descriptions of a different MCU running in a compatibility mode and
			// acting as this MCU. This might be useful for Debugging, but for now we ignore these
			// files.
			return;
		}

		parse(document, xmlfile);
	}

	/**
	 * Parse the given <code>Document</code>.
	 * <p>
	 * The MCU id has already been parsed and is stored in the field {@link #fMCUid}.
	 * </p>
	 * 
	 * @param document
	 *            XML DOM with the content of a single Atmel part description file.
	 * @param sourcefile
	 *            The file the DOM is derived from. May be used to gather meta-information about the
	 *            file.
	 */
	abstract protected void parse(Document document, File sourcefile);

	/**
	 * Read and parse the given XML file and return an DOM for it.
	 * 
	 * @param pdffile
	 *            <code>File</code> to an XML file.
	 * @return <code>Document</code> root node of the DOM or <code>null</code> if the file could not
	 *         be read or parsed.
	 */
	private Document getDocument(File pdffile) {

		Document root = null;
		try {
			// Read the xml file
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			root = builder.parse(pdffile);

		} catch (SAXParseException spe) {
			System.out.println("\n** Parsing error, line " + spe.getLineNumber() + ", uri "
					+ spe.getSystemId());
			System.out.println("   " + spe.getMessage());
			Exception e = (spe.getException() != null) ? spe.getException() : spe;
			e.printStackTrace();
		} catch (SAXException sxe) {
			Exception e = (sxe.getException() != null) ? sxe.getException() : sxe;
			e.printStackTrace();
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ioe) {
			ioe.printStackTrace();
		}

		return root;
	}

}
