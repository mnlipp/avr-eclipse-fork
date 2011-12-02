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
package de.innot.avreclipse.devutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.osgi.framework.Bundle;
import org.w3c.dom.Document;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.toolinfo.PartDescriptionFilesReader;
import de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader;
import de.innot.avreclipse.core.toolinfo.partdescriptionfiles.FusesReader;
import de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader;
import de.innot.avreclipse.core.toolinfo.partdescriptionfiles.SignatureReader;

/**
 * This is a simple Eclipse Application to read all Atmel part description
 * files, parse them and store the extracted properties.
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class GenerateMCULists implements IApplication {

	private final IPath DATASHEETPROPSPATH = new Path(
			"properties/datasheet.properties");

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#start(org.eclipse.equinox.app.
	 * IApplicationContext)
	 */
	public Object start(IApplicationContext context) throws Exception {

		// Query the path of the Description Files
		Display display = new Display();
		Shell shell = new Shell(display);

		DirectoryDialog directoryDialog = new DirectoryDialog(shell);

		directoryDialog.setMessage("Please select the PartDescriptionFiles directory  and click OK");

		String dir = directoryDialog.open();
		if (dir == null) {
			return null;
		}

		IPath folder = new Path(dir);
		
		// Build a list of partdescriptionfiles readers
		List<IPDFreader> readers = new ArrayList<IPDFreader>();
		readers.add(new MyFusesReader());
		readers.add(new MySignatureReader());
		readers.add(new MyDatasheetReader());

		PartDescriptionFilesReader pdreader = new PartDescriptionFilesReader(
				readers);

		pdreader.setDevicesFolder(folder);
		pdreader.parseAllFiles(new NullProgressMonitor());

		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.equinox.app.IApplication#stop()
	 */
	public void stop() {
		// TODO Auto-generated method stub

	}

	/**
	 * Modified {@link FusesReader}.
	 * <p>
	 * The only modifications is a different storage location (The storage area
	 * of this plugin as opposed to the storage area of the core plugin.
	 * </p>
	 * The generated files can be copied to the <code>properties/fusedesc</code>
	 * Folder of the core plugin. </p>
	 */
	private static class MyFusesReader extends FusesReader {

		private final static String DESCRIPTION_FOLDER = "fusedesc";

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.FusesReader
		 * #getStoragePath()
		 */
		@Override
		protected IPath getStoragePath() {
			IPath statelocation = Activator.getDefault().getStateLocation();
			IPath descriptionlocation = statelocation
					.append(DESCRIPTION_FOLDER);

			return descriptionlocation;
		}

	}

	/**
	 * Modified {@link SignatureReader}.
	 * <p>
	 * Instead of interacting with the <code>Signatures</code> class, this
	 * modified reader will store all signatures in a new properties file
	 * "signature.properties", located in the state storage area of this
	 * application plugin.
	 * </p>
	 * <p>
	 * The generated file can be copied to the <code>properties</code> folder of
	 * the core plugin.
	 * </p>
	 * 
	 */
	private static class MySignatureReader extends SignatureReader {

		private final Properties fSignatureProperties;

		public MySignatureReader() {
			fSignatureProperties = new Properties();

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.SignatureReader
		 * #storeSignature(java.lang.String, java.lang.String)
		 */
		@Override
		protected void storeSignature(String mcuid, String signature) {
			fSignatureProperties.setProperty(mcuid, signature);
		}

		@Override
		public void finish() {
			// Write the signature properties to the file "signature.properties"
			// in the de.innot.avreclipse.utils state storage area
			IPath statelocation = Activator.getDefault().getStateLocation();

			File signaturefile = statelocation.append("signature.properties")
					.toFile();

			try {
				FileOutputStream ostream = new FileOutputStream(signaturefile);
				fSignatureProperties.store(ostream,
						"# Created by GenerateMCULists - do not edit");
				ostream.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}

	/**
	 * Internal class to handle the Datasheet properties.
	 * <p>
	 * The previous list of datasheet URLs is loaded, any new MCU id values in
	 * the part description files are added (with an empty datasheet URL) and
	 * then the whole list is written back.
	 * </p>
	 */
	private class MyDatasheetReader extends BaseReader {

		private Properties fDatasheetProps;

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader
		 * #start()
		 */
		public void start() {
			// Load the previous datasheet props from the core plugin
			Properties oldDatasheetProps = new Properties();
			Bundle avrplugin = AVRPlugin.getDefault().getBundle();
			InputStream is = null;
			try {
				is = FileLocator.openStream(avrplugin, DATASHEETPROPSPATH,
						false);
				oldDatasheetProps.load(is);
				is.close();
			} catch (IOException e) {
				// this should not happen because the signatures.properties is
				// part of the plugin and always there.
				AVRPlugin.getDefault().log(
						new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
								"Can't find signatures.properties", e));
			}

			fDatasheetProps = oldDatasheetProps;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.BaseReader
		 * #parse(org.w3c.dom.Document)
		 */
		@Override
		protected void parse(Document document, File sourcefile) {
			// Just take the MCU, which has already been extracted by the
			// superclass, and add it to the datasheet list properties if it did
			// not exist before.
			if (!fDatasheetProps.containsKey(fMCUid)) {
				fDatasheetProps.put(fMCUid, "");
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * de.innot.avreclipse.core.toolinfo.partdescriptionfiles.IPDFreader
		 * #finish()
		 */
		public void finish() {
			// Write the signature properties to the file "datasheet.properties"
			// in the de.innot.avreclipse.utils state storage area

			IPath statelocation = Activator.getDefault().getStateLocation();
			File datasheetfile = statelocation.append("datasheet.properties")
					.toFile();

			try {
				FileOutputStream ostream = new FileOutputStream(datasheetfile);
				fDatasheetProps.store(ostream,
						"# Add datasheet URLs as available");
				ostream.close();
			} catch (IOException ioe) {
				ioe.printStackTrace();
			}
		}
	}
}
