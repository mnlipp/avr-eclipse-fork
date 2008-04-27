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
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.IMCUProvider;

/**
 * This class handles the list of Fuse descriptions.
 * <p>
 * Most AVR MCUs have between one and three fusebytes. The description of these fuses for each MCU
 * type is stored in a {@link IDescriptionHolder} object. This class manages the list of all
 * available fuse byte descriptions.
 * </p>
 * <p>
 * To get the <code>IDescriptionHolder</code for a MCU id use
 * {@link #getDescription(String)}.
 * </p>
 * <p>
 * This class manages two lists of fuse description files. 
 * The default list is included with the plugin and can be 
 * found in the <code>properties/fusedesc/</code> folder 
 * of this Plugin.
 * </p>
 * <p>
 * The second list is the for the current Eclipse instance 
 * and is located in the instance state area 
 * (<code>.metadata/.plugins/de.innot.avreclipse.core/fusesdesc/</code>)
 * </p>
 * <p>
 * Each folder contains serialized <code>FusesDescription</code> 
 * objects. This class also has a cache of all descriptions already 
 * requested to reduce disk access.
 * </p>
 * @author Thomas Holland
 * @since 2.2
 *
 */
public class Fuses implements IMCUProvider {

	// paths to the default and instance properties files
	private final static String						DEFAULTFOLDER			= "/properties/fusedesc";
	private final static String						INSTANCEFOLDER			= "fusedesc";

	/** File name extension for <code>IDescriptionHolder</code> objects. */
	private final static String						DESCRIPTION_EXTENSION	= ".desc";

	/** Cache of accessed <code>IDescriptionHolder</code> objects */
	private final Map<String, IDescriptionHolder>	fCache;

	/** List of all MCU id values for which Descriptions exist */
	private Set<String>								fMCUList				= null;

	private static Fuses							fInstance				= null;

	/**
	 * Get the default instance of the Fuses class
	 */
	public static Fuses getDefault() {
		if (fInstance == null)
			fInstance = new Fuses();
		return fInstance;
	}

	// protected constructor to prevent outside instantiation.
	protected Fuses() {
		// Init the cache
		fCache = new HashMap<String, IDescriptionHolder>();
	}

	/**
	 * Get the {@link IDescriptionHolder} with the fuse byte bitfield descriptions for the given MCU
	 * id.
	 * 
	 * @param mcuid
	 *            <code>String</code> with a valid MCU id
	 * @return <code>IDescriptionHolder</code> for the MCU or <code>null</code> if the given MCU
	 *         id is unknown.
	 * @throws IOException
	 *             if either the storage locations can't be accessed or a file exists, but can't be
	 *             accessed.
	 */
	public IDescriptionHolder getDescription(String mcuid) throws IOException {

		if (mcuid == null || (mcuid.length() == 0)) {
			return null;
		}

		// Check the cache first
		if (fCache.containsKey(mcuid)) {
			return fCache.get(mcuid);
		}

		// Look in the instance location first,
		// then in the defaults location
		IPath instancelocation = getInstanceStorageLocation();
		IPath defaultlocation = null;
		try {
			defaultlocation = getDefaultStorageLocation();
		} catch (IOException e1) {
			// This is rather unlikely and will only occur, if I forgot to copy
			// the build-in settings to the plugin. But just in case we continue
			// with something reasonable: The Instance location
			defaultlocation = getInstanceStorageLocation();
		}
		IPath[] allpaths = new IPath[] { instancelocation, defaultlocation };

		IDescriptionHolder desc = null;

		for (IPath path : allpaths) {
			desc = getDescriptionFromLocation(mcuid, path);
			if (desc != null) {
				break;
			}
		}

		// If a description was found enter it to the cache
		if (desc != null) {
			fCache.put(mcuid, desc);
		}

		return desc; // will still be null if nothing was found
	}

	/**
	 * Return the number of Fuse Bytes for the given MCU.
	 * <p>
	 * This is a convenience method (almost) equivalent to
	 * 
	 * <pre>
	 * getFusesDescription(mcuid).getFuseByteCount();
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param mcuid
	 *            A valid MCU id value.
	 * @return <code>int</code> with the number of fuse bytes for the mcu or -1 if the MCU id is
	 *         unknown.
	 * @throws IOException
	 */
	public int getByteCount(String mcuid) throws IOException {
		// Get the Description
		IDescriptionHolder desc;
		desc = getDescription(mcuid);
		if (desc != null) {
			return desc.getByteCount();
		}
		return -1;
	}

	//
	// Methods of the IMCUProvider Interface
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUInfo(java.lang.String)
	 */
	public String getMCUInfo(String mcuid) {
		int count = 0;
		try {
			count = getByteCount(mcuid);
		} catch (IOException e) {
			return null;
		}

		return Integer.toString(count);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUList()
	 */
	public Set<String> getMCUList() {

		if (fMCUList != null) {
			return fMCUList;
		}

		fMCUList = new HashSet<String>();

		// Look in the defaults location first,
		// then add the descriptions from the instance location
		IPath defaultlocation = null;
		try {
			defaultlocation = getDefaultStorageLocation();
		} catch (IOException e1) {
			// This is rather unlikely and will only occur, if I forgot to copy
			// the build-in settings to the plugin. But just in case we continue
			// with something reasonable: The Instance location
			defaultlocation = getInstanceStorageLocation();
		}
		IPath instancelocation = getInstanceStorageLocation();
		IPath[] allpaths = new IPath[] { defaultlocation, instancelocation };

		for (IPath path : allpaths) {
			File currfolder = path.toFile();
			String[] allfiles = currfolder.list(new FilenameFilter() {
				// Little filter to accept only files ending with ".desc"
				public boolean accept(File dir, String name) {
					if (name.endsWith(DESCRIPTION_EXTENSION)) {
						return true;
					}
					return false;
				}
			});

			// Iterate over all returned filenames, strip the extension and add
			// it to the set.
			if (allfiles != null) {
				for (String filename : allfiles) {
					String mcuid = filename.substring(0, filename.lastIndexOf('.'));
					fMCUList.add(mcuid);
				}
			}
		}

		// Return a copy of the List
		return new HashSet<String>(fMCUList);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#hasMCU(java.lang.String)
	 */
	public boolean hasMCU(String mcuid) {
		if (fMCUList == null) {
			getMCUList();
		}
		return fMCUList.contains(mcuid);
	}

	/**
	 * De-Serialize the DescriptionHolder object for a MCU from the given location.
	 * 
	 * @param mcuid
	 *            <code>String</code> with the MCU id value.
	 * @param location
	 *            <code>IPath</code> to a folder containing the serialized description holder
	 *            objects.
	 * @return
	 */
	private IDescriptionHolder getDescriptionFromLocation(String mcuid, IPath location) {

		String filename = mcuid + DESCRIPTION_EXTENSION;

		// Test if a file with the right name is in the location
		File file = location.append(filename).toFile();
		if (!file.canRead()) {
			// File does not exist or can't be accessed.
			return null;
		}

		// OK, file exist. De-serialize it
		FusesDescription newdesc = null;
		FileInputStream fis = null;
		ObjectInputStream in = null;
		try {
			fis = new FileInputStream(file);
			in = new ObjectInputStream(fis);
			newdesc = (FusesDescription) in.readObject();
			in.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException ex) {
			ex.printStackTrace();
		}

		return newdesc;
	}

	/**
	 * Get the folder for the instance fuse description files.
	 * <p>
	 * The default location is the <code>fusedesc</code> folder in the core plugin storage area (<code>Worspace_loc/.metadata/plugins/de.innot.avreclipse.core/fusedesc</code>).
	 * </p>
	 * 
	 * @return <code>IPath</code> to the instance storage area.
	 */
	public IPath getInstanceStorageLocation() {

		IPath statelocation = AVRPlugin.getDefault().getStateLocation();
		IPath location = statelocation.append(INSTANCEFOLDER);

		return location;
	}

	/**
	 * Get the folder for the build-in fuse description files.
	 * <p>
	 * The default location is the <code>properties/fusedesc</code> folder in the core plugin.
	 * </p>
	 * 
	 * @return <code>IPath</code> to the default location.
	 * @throws IOException
	 */
	public IPath getDefaultStorageLocation() throws IOException {

		Bundle avrplugin = AVRPlugin.getDefault().getBundle();
		URL locationurl = avrplugin.getEntry(DEFAULTFOLDER);
		IPath location = new Path(FileLocator.toFileURL(locationurl).getPath());

		return location;
	}
}
