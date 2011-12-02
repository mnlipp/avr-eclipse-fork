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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.toolinfo.Signatures;

/**
 * Signature reader.
 * <p>
 * This Class will take a PartDescriptionFile Document and read the MCU
 * signature from it.
 * </p>
 * <p>
 * The Signature is taken from the three <PROPERTY> Elements "SIGNATURE0",
 * "SIGNATURE1" and "SIGNATURE2"
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class SignatureReader extends BaseReader {

	private final static String ELEM_PROPERTY = "property";
	private final static String ATTR_NAME = "name";
	private final static String ATTR_VALUE = "value";

	/** SIGNATUREx Attribute name prefix */
	private final static String NAME_SIGNATURE = "signature";

	/** Signatures manager instance. */
	private static final Signatures fSignatures = Signatures.getDefault();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#start()
	 */
	public void start() {
		// Nothing to init.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#parse
	 * (org.w3c.dom.Document)
	 */
	@Override
	public void parse(Document document, File sourcefile) {

		String[] signatureBytes = new String[3];
		int counter = 0;

		// Get all property elements and then find the three elements with the
		// "SIGNATUREx" names.
		// The Document may not have any signature properties (e.g for AVR32
		// MCUs)
		NodeList nodes = document.getElementsByTagName(ELEM_PROPERTY);
		for (int i = 0; i < nodes.getLength(); i++) {
			Node n = nodes.item(i);
			NamedNodeMap attributes = n.getAttributes();
			Node nameattr = attributes.getNamedItem(ATTR_NAME);
			if (nameattr != null) {
				String name = nameattr.getNodeValue().toLowerCase();
				if (name.startsWith(NAME_SIGNATURE)) {
					// The last char is the index
					int idx = name.charAt(name.length() - 1) - '0';
					signatureBytes[idx] = attributes.getNamedItem(ATTR_VALUE)
							.getNodeValue().toUpperCase();
					counter++;
				}
			}
		}

		if (counter < 3) {
			// No valid signature found
			// TODO: Read the JTAG signature once we can do something with it
			return;
		}

		// Assemble the signature and store it in the internal list.

		StringBuilder sig = new StringBuilder("0x");
		for (int i = 0; i < signatureBytes.length; i++) {
			// Each signature byte appears to begin with "0x" in the Atmel
			// Device xml files.
			// So we just take the last the two characters which should
			// protect against changes of the current format.
			sig.append(signatureBytes[i].substring(signatureBytes[i].length() - 2));
		}

		String signature = sig.toString();

		storeSignature(fMCUid, signature);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader#finish
	 * ()
	 */
	public void finish() {
		try {
			fSignatures.storeSignatures();
		} catch (IOException e) {
			// Can't write to the instance property storage.
			// Log an error message.
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
					"Can't write signatures.properties file", e);
			AVRPlugin.getDefault().log(status);
		}
	}

	/**
	 * Add the signature to the signature properties.
	 * <p>
	 * This method can be overridden to store the signature to somewhere else.
	 * The default is to call {@link Signatures#addSignature(String, String)} to
	 * add it to the plugin instance properties.
	 * </p>
	 * 
	 * @param mcuid
	 *            The MCU id value
	 * @param signature
	 *            The Signature value for the MCU
	 */
	protected void storeSignature(String mcuid, String signature) {
		fSignatures.addSignature(mcuid, signature);
	}
}
