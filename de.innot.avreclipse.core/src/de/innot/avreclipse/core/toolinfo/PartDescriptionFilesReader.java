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
package de.innot.avreclipse.core.toolinfo;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathProvider;
import de.innot.avreclipse.core.paths.IPathProvider;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * This is an utility class to read and parse the Atmel Part Description files.
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class PartDescriptionFilesReader {

	private final static String PARTNAME = "PART_NAME";
	private final static String ADDR00x = "ADDR00";

	private static PartDescriptionFilesReader fInstance = null;

	public static PartDescriptionFilesReader getDefault() {
		if (fInstance == null) {
			fInstance = new PartDescriptionFilesReader();
		}
		return fInstance;
	}

	public void parseAllFiles(IProgressMonitor monitor) {

		// Get the path to the PartDescriptionFiles
		IPathProvider provider = new AVRPathProvider(AVRPath.PDFPATH);
		IPath pdfpath = provider.getPath();
		File pdfdirectory = pdfpath.toFile();
		if (!pdfdirectory.isDirectory()) {
			return;
		}

		// get and parse all XML files in the PartDescriptionFiles directory
		File[] allfiles = pdfdirectory.listFiles(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				if (name.endsWith(".xml")) {
					return true;
				}
				return false;
			}
		});

		try {
			monitor.beginTask("Parsing Atmel Part Description Files", allfiles.length);
			for (File pdffile : allfiles) {
				monitor.subTask("Reading [" + pdffile.getName() + "]");
				Document root = getDocument(pdffile);

				// retrieve the MCU signature from the document
				Entry sig = readSignature(root);
				if (sig != null) {
					addSignature(sig);
				}

				// TODO: retrieve the fuse bits from the document
				// TODO: retrieve the lock bits from the document

				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}

		// store the changed properties
		try {
			Signatures.getDefault().storeSignatures();
		} catch (IOException e) {
			AVRPlugin.getDefault().log(
			        new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
			                "Can't write signatures.properties file", e));
		}
		
		// TODO: save new fusebits
		// TODO: save new lockbits

	}

	/**
	 * This is a hook method extending classes can override to do something else
	 * with a signature entry. The default is to add the signature to the
	 * instance list of signatures, overriding an existing signature value if it
	 * is different.
	 * 
	 * @param entry
	 */
	protected void addSignature(Entry entry) {
		Signatures.getDefault().addSignature(entry.getKey(), entry.getValue());
	}

	/**
	 * Read and parse the given XML file and return an DOM for it.
	 * 
	 * @param pdffile
	 *            <code>File</code> to an XML file.
	 * @return <code>Document</code> root node of the DOM or <code>null</code>
	 *         if the file could not be read or parsed.
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

	/**
	 * Fetch the MCU type and its signature bytes from an AVR Part Description
	 * DOM model.
	 * <p>
	 * The MCU id is generated from the &lt;PART_NAME&gt; element, the signature
	 * is a concatenation of the three elements &lt;ADDR000&gt; to
	 * &lt;ADDR002&gt;.
	 * </p>
	 * 
	 * @param document
	 *            <code>Document</code> root node of a Part Description File
	 * @return <code>Entry</code> with the mcuid as the key and the
	 *         concatenated signature bytes as the value ("0x123456"), or
	 *         <code>null</code> if the given DOM did not have the required
	 *         signature bytes.
	 * 
	 */
	private Entry readSignature(Document document) {
		String mcuid = null;
		String signature = null;

		// Get the MCU name from the <PART_NAME> element
		NodeList nodes = document.getElementsByTagName(PARTNAME);
		String partname = nodes.item(0).getFirstChild().getNodeValue();
		mcuid = AVRMCUidConverter.name2id(partname);

		if (partname.endsWith("comp")) {
			// ignore entries ending with "comp", as they only seem to be an
			// "complete" version of base files with the same signature
			return null;
		}

		// Get the signature from the three <ADDR000> to <ADDR002> elements.
		StringBuffer id = new StringBuffer("0x");
		for (int i = 0; i < 3; i++) {
			nodes = document.getElementsByTagName(ADDR00x + i);
			if (nodes.getLength() == 0) {
				return null;
			}
			String basevalue = nodes.item(0).getFirstChild().getNodeValue();
			// skip the leading "$" of the element data
			id.append(basevalue.substring(1));
		}
		signature = id.toString();

		return new Entry(mcuid, signature);

	}

	/**
	 * Internal storage for a key/value pair.
	 * 
	 * This is somewhat reminiscent of the Map.Entry class.
	 */
	protected static class Entry {
		private String fKey;
		private String fValue;

		public Entry(String key, String value) {
			fKey = key;
			fValue = value;
		}

		public String getKey() {
			return fKey;
		}

		public String getValue() {
			return fValue;
		}
	}

}
