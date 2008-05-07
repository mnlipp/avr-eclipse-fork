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
package de.innot.avreclipse.core.toolinfo.partdescriptionfiles;

import java.io.IOException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.toolinfo.Signatures;

/**
 * Signature reader.
 * <p>
 * This Class will take a PartDescriptionFile Document and read the MCU signature from it.
 * </p>
 * <p>
 * The Signature is taken from the three Elements "ADDR000", "ADDR001" and "ADDR002"
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class SignatureReader extends BaseReader {

	/** ADDR00x Element prefix */
	private final static String		ADDR00x		= "ADDR00";

	/** Signatures manager instance. */
	private static final Signatures	fSignatures	= Signatures.getDefault();

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#start()
	 */
	public void start() {
		// Nothing to init.
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader#parse(org.w3c.dom.Document)
	 */
	@Override
	public void parse(Document document) {

		// Get the signature from the three <ADDR000> to <ADDR002> elements and
		// concatenate them to a single String with a "0x" prefix.
		StringBuffer id = new StringBuffer("0x");
		for (int i = 0; i < 3; i++) {
			NodeList nodes = document.getElementsByTagName(ADDR00x + i);
			if (nodes.getLength() == 0) {
				return;
			}
			String basevalue = nodes.item(0).getFirstChild().getNodeValue();
			// skip the leading "$" of the element data
			id.append(basevalue.substring(1));
		}
		String signature = id.toString();

		storeSignature(fMCUid, signature);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader#finish()
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
	 * This method can be overridden to store the signature to somewhere else. The default is to
	 * call {@link Signatures#addSignature(String, String)} to add it to the plugin instance
	 * properties.
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
