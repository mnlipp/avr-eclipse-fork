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

package de.innot.avreclipse.debug;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.zip.ZipFile;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.dialogs.IOverwriteQuery;
import org.eclipse.ui.wizards.datatransfer.ImportOperation;
import org.eclipse.ui.wizards.datatransfer.ZipFileStructureProvider;
import org.osgi.framework.Bundle;

/**
 * Utility class to create AVR projects.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TestProjectCreator {

	public static ICProject getAVRProject(String projectname, IPath zipFile) throws CoreException {

		// check if Project already exists
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(projectname);
		if (!project.exists()) {
			project.create(null);
		} else {
			project.refreshLocal(IResource.DEPTH_INFINITE, null);
		}

		if (!project.isOpen()) {
			project.open(null);
		}

		try {
			importFilesFromZip(zipFile, project.getFullPath(), null);
		} catch (InvocationTargetException e) {
			IStatus status = new Status(IStatus.ERROR, DebugTestPlugin.PLUGIN_ID,
					"Could not import zipped project '" + zipFile + "'", e);
			throw new CoreException(status);
		} catch (IOException e) {
			IStatus status = new Status(IStatus.ERROR, DebugTestPlugin.PLUGIN_ID,
					"Could not unzip zipped project '" + zipFile + "'", e);
			throw new CoreException(status);
		}

		ICProject cproject = CCorePlugin.getDefault().getCoreModel().create(project);

		return cproject;
	}

	private static void importFilesFromZip(IPath zipPath, IPath destPath, IProgressMonitor monitor)
			throws InvocationTargetException, IOException {

		Bundle testplugin = DebugTestPlugin.getDefault().getBundle();
		URL zipurl = FileLocator.find(testplugin, zipPath, null);
		zipurl = FileLocator.toFileURL(zipurl);
		String zipfilename = zipurl.getFile();
		ZipFile zipfile = new ZipFile(zipfilename);

		ZipFileStructureProvider structureProvider = new ZipFileStructureProvider(zipfile);
		try {
			ImportOperation op = new ImportOperation(destPath, structureProvider.getRoot(),
					structureProvider, new ImportOverwriteQuery());
			op.run(monitor);
		} catch (InterruptedException e) {
			// should not happen

		}
	}

	private static class ImportOverwriteQuery implements IOverwriteQuery {
		public String queryOverwrite(String file) {
			return ALL;
		}
	}

}
