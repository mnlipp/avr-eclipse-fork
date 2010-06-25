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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
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
	 * @throws IOException
	 */
	public static URL getPluginFileURL(String filename) throws IOException {
		AVRPlugin plugin = AVRPlugin.getDefault();
		Bundle bundle = plugin.getBundle();
		URL url = FileLocator.find(bundle, new Path(filename), null);
		URL fileurl = FileLocator.toFileURL(url);
		return fileurl;
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
	 * @throws IOException
	 */
	public static File getPluginFile(String filename) throws IOException {
		URL url = getPluginFileURL(filename);
		try {
			return new File(url.toURI());
		} catch (URISyntaxException e) {
			// Unlikely to happen
			e.printStackTrace();
		}

		return null;
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
	 * @throws IOException
	 */
	public static IPath getPluginFilePath(String filename) throws IOException {
		File file = getPluginFile(filename);
		return new Path(file.getAbsolutePath());
	}

	/**
	 * Get an <code>IFile</code> resource for a file in the test plugin.
	 * <p>
	 * Because <code>IFiles</code> can only exist within a Workspace this method
	 * will create a new Project in the current Workspace and link the requested
	 * file to the project.<br>
	 * The name of the project will be equivalent to the path of the given file.
	 * </p>
	 * 
	 * @param filepath
	 *            Path of a file in the test plugin.
	 * @return <code>IFile</code> to a link to the given file.
	 * @throws IOException
	 * @throws CoreException
	 */
	public static IFile getPluginIFile(String filepath) throws IOException,
			CoreException {

		IPath fullpath = new Path(filepath);
		String filename = fullpath.lastSegment();
		String projectname = fullpath.removeLastSegments(1).toString();
		projectname = projectname.replace('/', '_'); // remove path separators

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IProject project = root.getProject(projectname);
		IFile file = project.getFile(filename);

		if (!project.exists())
			project.create(null);
		if (!project.isOpen())
			project.open(null);

		if (!file.exists()) {
			try {
				URI origuri = getPluginFileURL(filepath).toURI();
				file.createLink(origuri, IResource.NONE, null);
			} catch (URISyntaxException e1) {
				// unlikely to happen. If it does happen wrap in a IOException
				e1.printStackTrace();
				throw new IOException(e1);
			}
		}

		return file;

	}
}
