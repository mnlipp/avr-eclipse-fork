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
package de.innot.avreclipse.core.toolinfo;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathProvider;
import de.innot.avreclipse.core.paths.IPathProvider;
import de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader;
import de.innot.avreclipse.core.toolinfo.partdescriptionfiles.SignatureReader;

/**
 * This is an utility class to read and parse the Atmel Part Description files.
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class PartDescriptionFilesReader {

	private static PartDescriptionFilesReader fInstance = null;

	private List<IPDFreader> fReaders;

	private IPath fDevicesFolder;

	public static PartDescriptionFilesReader getDefault() {
		if (fInstance == null) {
			fInstance = new PartDescriptionFilesReader();
		}
		return fInstance;
	}

	private PartDescriptionFilesReader() {
		fReaders = new ArrayList<IPDFreader>();
		fReaders.add(new SignatureReader());
	}

	public PartDescriptionFilesReader(List<IPDFreader> readers) {
		fReaders = readers;
	}

	/**
	 * Set the folder containing all PartDescriptionFiles.
	 * 
	 * @param folder
	 */
	public void setDevicesFolder(IPath folder) {
		fDevicesFolder = folder;
	}

	/**
	 * Read all PartDescriptionFiles, parse them and pass each document to all
	 * registered readers for further analysis.
	 * 
	 * @param monitor
	 */
	public void parseAllFiles(IProgressMonitor monitor) {

		// Get the path to the PartDescriptionFiles
		if (fDevicesFolder == null) {
			IPathProvider provider = new AVRPathProvider(AVRPath.PDFPATH);
			fDevicesFolder = provider.getPath();
		}

		File pdfdirectory = fDevicesFolder.toFile();
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
			monitor.beginTask("Parsing Atmel Part Description Files",
					allfiles.length);

			// Tell all registered readers that we are about to start reading
			// files.
			for (IPDFreader reader : fReaders) {
				reader.start();
			}

			// Go through all files and call the read() method for each file and
			// every registered reader
			for (File pdffile : allfiles) {
				monitor.subTask("Reading [" + pdffile.getName() + "]");

				// Pass the file to all readers
				for (IPDFreader reader : fReaders) {
					reader.read(pdffile);
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}

		// Tell all registered readers that all files have been processed and
		// that they can close up shop.
		for (IPDFreader reader : fReaders) {
			reader.finish();
		}

	}


}
