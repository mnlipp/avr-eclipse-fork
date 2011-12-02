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
package de.innot.avreclipse.core.toolinfo.partdescriptionfiles;

import java.io.File;

import org.w3c.dom.Document;

import de.innot.avreclipse.core.toolinfo.PartDescriptionFilesReader;

/**
 * Part Description File reader interface.
 * <p>
 * A PDFreader is used to parse Atmel part description files. The files are handed to the
 * implementation as a XML DOM Document. It is used by the {@link PartDescriptionFilesReader} Class
 * to delegate the parsing of the files to specific modules.
 * </p>
 * <p>
 * The reader has a simple life cycle model with three states:
 * <ul>
 * <li>{@link #start()}: This is called before the reading of files begin. Implementations can set
 * up their property stores in this method.</li>
 * <li>{@link #read(Document)}: This is called for every part description file.</li>
 * <li>{@link #finish()}: This is called after all files have been read. Implementations can persist
 * the read properties here.</li>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public interface IPDFreader {

	/**
	 * Initialize the reader.
	 * <p>
	 * This is called before any files are read.
	 * </p>
	 */
	public void start();

	/**
	 * Read the given file and extract any relevant information.
	 * <p>
	 * This is called for every Atmel part description file found.
	 * </p>
	 * 
	 * @param xmlfile
	 *            The xml file with the description of the MCU.
	 */
	public void read(File xmlfile);

	/**
	 * Close the reader.
	 * <p>
	 * This is called after all files have been read. Here all new / changed properties can be
	 * written to the persistent storage.
	 * </p>
	 */
	public void finish();

}
