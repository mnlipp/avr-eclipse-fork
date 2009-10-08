/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.osgi.framework.Bundle;

/**
 * Helper methods for unit tests.
 * 
 * @author Thomas Holland
 * @since 2.3.2
 * 
 */
public class TestHelper {

	/**
	 * Get the URL for a file within the plugin.
	 * <p>
	 * Used to access data files for the tests.
	 * </p>
	 * 
	 * @param filename
	 *            Path and name of the file relative to the plugin root.
	 * @return URL of the file.
	 */
	public static URL getPluginFileURL(String filename) {
		AVRPlugin plugin = AVRPlugin.getDefault();
		Bundle bundle = plugin.getBundle();
		URL url = FileLocator.find(bundle, new Path(filename), null);
		return url;
	}

	/**
	 * Get a <code>File</code> object for a file within the plugin.
	 * <p>
	 * Used to access data files for the tests.
	 * </p>
	 * 
	 * @param filename
	 *            Path and name of the file relative to the plugin root.
	 * @return <code>File</code>
	 */
	public static File getPluginFile(String filename) {
		URL url = getPluginFileURL(filename);
		return new File(url.getFile());
	}

	/**
	 * Get a <code>IPath</code> object for a file within the plugin.
	 * <p>
	 * Used to access data files for the tests.
	 * </p>
	 * 
	 * @param filename
	 *            Path and name of the file relative to the plugin root.
	 * @return <code>IPath</code>
	 */
	public static IPath getPluginFilePath(String filename) {
		URL url = getPluginFileURL(filename);
		return new Path(url.getFile());
	}
}
