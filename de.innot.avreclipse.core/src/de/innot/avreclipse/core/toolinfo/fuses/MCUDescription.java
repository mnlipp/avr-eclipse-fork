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
package de.innot.avreclipse.core.toolinfo.fuses;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import de.innot.avreclipse.AVRPlugin;

/**
 * {@link IMCUDescription} implementation for FuseBytes.
 * <p>
 * Objects of this class hold the {@link BitFieldDescription} objects for all fuse bytes of a single
 * MCU.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class MCUDescription implements IMCUDescription {

	private final static String			ELEMENT_ROOT		= "description";
	private final static String			ATTR_ROOT_MCUTYPE	= "mcutype";

	private final static String			ELEMENT_VERSION		= "version";
	private final static String			ATTR_VERSION_BUILD	= "build";

	/** The MCU for this description. */
	private String						fMCUid;

	/**
	 * The build number from the Part Description File. As of AVR Studio 5.0 this is the date of the
	 * last modification of the source file.
	 */
	private int							fBuildVersion;

	/** The list of Fuse byte descriptions. */
	private final List<ByteDescription>	fFuseByteDescList;

	/** The list of Lockbits byte descriptions. */
	private final List<ByteDescription>	fLockbitsByteDescList;

	/**
	 * Create a new MCUDescription for a MCU with the given number of fuse bytes.
	 * 
	 * @param mcuid
	 *            <code>String</code> with a MCU id value.
	 * @param bytecount
	 *            <code>int</code> with the number of fuse bytes this MCU has.
	 */
	public MCUDescription(String mcuid) {
		fMCUid = mcuid;
		fFuseByteDescList = new ArrayList<ByteDescription>();
		fLockbitsByteDescList = new ArrayList<ByteDescription>();
	}

	public MCUDescription(Document document) throws IllegalArgumentException {

		fMCUid = null;
		fFuseByteDescList = new ArrayList<ByteDescription>();
		fLockbitsByteDescList = new ArrayList<ByteDescription>();

		// Default attribute values for the <version> element.
		// Used when these attributes are missing from the xml file.
		fBuildVersion = 0;

		// Get the root node and read the attributes
		NodeList nodes = document.getElementsByTagName(ELEMENT_ROOT);
		if (nodes.getLength() != 1) {
			throw new IllegalArgumentException("No root node <" + ELEMENT_ROOT + "> found.");
		}

		Node rootnode = nodes.item(0);
		NamedNodeMap attrs = rootnode.getAttributes();

		// The root node has one attributes: "mcutype". Must be present and valid.
		Node mcutypenode = attrs.getNamedItem(ATTR_ROOT_MCUTYPE);
		if (mcutypenode != null) {
			fMCUid = mcutypenode.getTextContent();
		} else {
			throw new IllegalArgumentException("required attribute \"" + ATTR_ROOT_MCUTYPE
					+ "\" is missing from the <" + ELEMENT_ROOT + "> element.");
		}

		// read the <version> element (only first one if multiple elements are present).
		NodeList versionnodes = document.getElementsByTagName(ELEMENT_VERSION);
		if (versionnodes.getLength() > 0) {
			// The <version> element has one attribute: "build".
			// The "status" attribute is deprecated and ignored
			attrs = versionnodes.item(0).getAttributes();
			for (int i = 0; i < attrs.getLength(); i++) {
				Node attr = attrs.item(i);
				if (ATTR_VERSION_BUILD.equalsIgnoreCase(attr.getNodeName())) {
					fBuildVersion = Integer.decode(attr.getNodeValue());
				}
			}
		}

		// read the <fusebyte> elements
		NodeList bytenodes = document.getElementsByTagName(FuseType.FUSE.getElementName());
		for (int i = 0; i < bytenodes.getLength(); i++) {
			ByteDescription bd = new ByteDescription(bytenodes.item(i));
			addByteDescription(FuseType.FUSE, bd);
		}

		// and the <lockbitsbyte> elements.
		bytenodes = document.getElementsByTagName(FuseType.LOCKBITS.getElementName());
		for (int i = 0; i < bytenodes.getLength(); i++) {
			ByteDescription bd = new ByteDescription(bytenodes.item(i));
			addByteDescription(FuseType.LOCKBITS, bd);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IMCUDescription#getMCUId()
	 */
	public String getMCUId() {
		return fMCUid;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IMCUDescription#getVersion()
	 */
	public int getVersion() {
		return fBuildVersion;
	}

	/**
	 * Set the build version and the status as defined by the part description file.
	 * 
	 * @param buildversion
	 */
	public void setVersion(int buildversion) {
		fBuildVersion = buildversion;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.toolinfo.fuses.IMCUDescription#getByteCount(de.innot.avreclipse.
	 * core.toolinfo.fuses.FuseType)
	 */
	public int getByteCount(FuseType type) {
		switch (type) {
			case FUSE:
				return fFuseByteDescList.size();
			case LOCKBITS:
				return fLockbitsByteDescList.size();
			default:
				IStatus status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
						"Internal error: undefined fuse memory type " + type.toString(), null);
				AVRPlugin.getDefault().log(status);
				return 0;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.toolinfo.fuses.IMCUDescription#getByteDescription(java.lang.String)
	 */
	public IFuseObjectDescription getByteDescription(String name) {
		// go thru both fusebytes and lockbitsbyte list and see if we can find a match.
		for (ByteDescription bd : fFuseByteDescList) {
			if (bd.getName().equalsIgnoreCase(name)) {
				return bd;
			}
		}
		for (ByteDescription bd : fLockbitsByteDescList) {
			if (bd.getName().equalsIgnoreCase(name)) {
				return bd;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.toolinfo.fuses.IMCUDescription#getByteDescription(de.innot.avreclipse
	 * .core.toolinfo.fuses.FuseType, int)
	 */
	public IFuseObjectDescription getByteDescription(FuseType type, int index) {
		switch (type) {
			case FUSE:
				return fFuseByteDescList.get(index);
			case LOCKBITS:
				return fLockbitsByteDescList.get(index);
			default:
				return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.toolinfo.fuses.IMCUDescription#getByteDescriptions(de.innot.avreclipse
	 * .core.toolinfo.fuses.FuseType)
	 */
	public List<IFuseObjectDescription> getByteDescriptions(FuseType type) {
		switch (type) {
			case FUSE:
				return new ArrayList<IFuseObjectDescription>(fFuseByteDescList);
			case LOCKBITS:
				return new ArrayList<IFuseObjectDescription>(fLockbitsByteDescList);
			default:
				IStatus status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
						"Internal error: undefined fuse memory type " + type.toString(), null);
				AVRPlugin.getDefault().log(status);

				return null;
		}
	}

	public void addByteDescription(FuseType type, ByteDescription desc) {

		int index = desc.getIndex();
		List<ByteDescription> list = null;
		switch (type) {
			case FUSE:
				list = fFuseByteDescList;
				break;
			case LOCKBITS:
				list = fLockbitsByteDescList;
				break;
			default:
				IStatus status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
						"Internal error: undefined fuse memory type " + type.toString(), null);
				AVRPlugin.getDefault().log(status);
				return;
				// do nothing
		}

		while (index >= list.size()) {
			list.add(null);
		}
		list.set(index, desc);
	}

	/**
	 * Convert this MCUDescription Object to XML.
	 * <p>
	 * This method is used to serialize the descriptions to a XML DOM tree. Saving the DOM tree to a
	 * file is left to the caller.
	 * </p>
	 * <p>
	 * The DTD format of the created content:
	 * 
	 * <pre>
	 * &lt;!ELEMENT fuse ( version, byte+ ) &gt;
	 * &lt;!ATTLIST fuse bytecount NMTOKEN #REQUIRED &gt;
	 * &lt;!ATTLIST fuse mcutype NMTOKEN #REQUIRED &gt;
	 * 
	 * &lt;!ELEMENT version EMPTY &gt;
	 * &lt;!ATTLIST version build NMTOKEN #REQUIRED &gt;
	 * &lt;!ATTLIST version status NMTOKEN #REQUIRED &gt;
	 * 
	 * &lt;!ELEMENT byte ( default, bitfield+ ) &gt;
	 * &lt;!ATTLIST byte index NMTOKEN #REQUIRED &gt;
	 * &lt;!ATTLIST byte name NMTOKEN #REQUIRED &gt;
	 * 
	 * &lt;!ELEMENT default EMPTY &gt;
	 * &lt;!ATTLIST default value NMTOKEN #REQUIRED &gt;
	 * 
	 * &lt;!ELEMENT bitfield ( value* ) &gt;
	 * &lt;!ATTLIST bitfield desc CDATA #REQUIRED &gt;
	 * &lt;!ATTLIST bitfield mask NMTOKEN #REQUIRED &gt;
	 * &lt;!ATTLIST bitfield name ID #REQUIRED &gt;
	 * 
	 * &lt;!ELEMENT value EMPTY &gt;
	 * &lt;!ATTLIST value desc CDATA #REQUIRED &gt;
	 * &lt;!ATTLIST value val NMTOKEN #REQUIRED &gt;
	 * 
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param document
	 *            a blank DOM document to write the content to
	 */
	public void toXML(Document document) {

		Element rootnode = document.createElement(ELEMENT_ROOT);
		rootnode.setAttribute(ATTR_ROOT_MCUTYPE, fMCUid);
		document.appendChild(rootnode);

		// Set the version and release status
		Element versionnode = document.createElement(ELEMENT_VERSION);
		versionnode.setAttribute(ATTR_VERSION_BUILD, Integer.toString(fBuildVersion));
		rootnode.appendChild(versionnode);

		// Now a <fusebyte> element for each fusebytes
		for (ByteDescription bd : fFuseByteDescList) {
			if (bd != null) {
				bd.toXML(rootnode);
			}
		}

		// and the same for the lockbit bytes
		for (ByteDescription bd : fLockbitsByteDescList) {
			if (bd != null) {
				bd.toXML(rootnode);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.toolinfo.fuses.IMCUDescription#isCompatibleWith(de.innot.avreclipse
	 * .core.toolinfo.fuses.IMCUDescription, de.innot.avreclipse.core.toolinfo.fuses.FuseType)
	 */
	public boolean isCompatibleWith(IMCUDescription target, FuseType type) {

		// First check the byte count. If they differ, then it can not be compatible
		if (getByteCount(type) != target.getByteCount(type)) {
			return false;
		}

		// Next get a list of all ByteDescription Objects and compare them each
		List<IFuseObjectDescription> ourlist = getByteDescriptions(type);
		List<IFuseObjectDescription> targetlist = target.getByteDescriptions(type);

		for (int i = 0; i < ourlist.size(); i++) {
			IFuseObjectDescription ourbyte = ourlist.get(i);
			IFuseObjectDescription targetbyte = targetlist.get(i);
			if (ourbyte == null) {
				return targetbyte == null;
			}
			if (!ourbyte.isCompatibleWith(targetbyte)) {
				return false;
			}
		}

		// all bytes matched -> success
		return true;

	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Fuses Description for " + getMCUId());
		sb.append(" [");
		sb.append(" build version " + fBuildVersion);
		sb.append(" Fuses: ");
		for (IFuseObjectDescription bd : fFuseByteDescList) {
			sb.append("[" + bd.toString() + "]");
		}
		sb.append(" Fuses: ");
		for (IFuseObjectDescription bd : fFuseByteDescList) {
			sb.append("[" + bd.toString() + "]");
		}
		sb.append(" Lockbits: ");
		for (IFuseObjectDescription bd : fLockbitsByteDescList) {
			sb.append("[" + bd.toString() + "]");
		}
		sb.append(" ]");

		return sb.toString();
	}
}
