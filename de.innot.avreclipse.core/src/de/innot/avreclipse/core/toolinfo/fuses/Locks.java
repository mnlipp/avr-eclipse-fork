/**
 * 
 */
package de.innot.avreclipse.core.toolinfo.fuses;

import java.io.IOException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

import de.innot.avreclipse.AVRPlugin;

/**
 * This class handles the list of Lockbit descriptions.
 * <p>
 * All AVR MCUs have one byte with lockbits. The description of these bits for each MCU type is
 * stored in a {@link IDescriptionHolder} object. This class manages the list of all available
 * lockbit descriptions.
 * </p>
 * <p>
 * To get the <code>IDescriptionHolder</code for a MCU id use
 * {@link #getDescription(String)}.
 * </p>
 * <p>
 * This class manages two lists of lockbit description files. 
 * The default list is included with the plugin and can be 
 * found in the <code>properties/lockbitdesc/</code> folder 
 * of this Plugin.
 * </p>
 * <p>
 * The second list is the for the current Eclipse instance 
 * and is located in the instance state area 
 * (<code>.metadata/.plugins/de.innot.avreclipse.core/lockbitdesc/</code>)
 * </p>
 * <p>
 * Each folder contains serialized <code>LockbitDescription</code> 
 * objects. This class also has a cache of all descriptions already 
 * requested to reduce disk access.
 * </p>
 * @author Thomas Holland
 * @since 2.2
 *
 */
public class Locks extends Fuses {

	// paths to the default and instance properties files
	private final static String	DEFAULTFOLDER	= "/properties/lockbitdesc";
	private final static String	INSTANCEFOLDER	= "lockbitdesc";

	private static Locks		fInstance		= null;

	/**
	 * Get the default instance of the Fuses class
	 */
	public static Locks getDefault() {
		if (fInstance == null)
			fInstance = new Locks();
		return fInstance;
	}

	// protected constructor to prevent outside instantiation.
	protected Locks() {
		super();
	}

	/**
	 * Get the folder for the instance fuse description files.
	 * <p>
	 * The default location is the <code>fusedesc</code> folder in the core plugin storage area (<code>Worspace_loc/.metadata/plugins/de.innot.avreclipse.core/fusedesc</code>).
	 * </p>
	 * 
	 * @return <code>IPath</code> to the instance storage area.
	 */
	@Override
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
	@Override
	public IPath getDefaultStorageLocation() throws IOException {

		Bundle avrplugin = AVRPlugin.getDefault().getBundle();
		URL locationurl = avrplugin.getEntry(DEFAULTFOLDER);
		IPath location = new Path(FileLocator.toFileURL(locationurl).getPath());

		return location;
	}

}
