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

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class ByteValuesFactory {

	public static ByteValues createByteValues(IFile file) throws CoreException {

		ByteValues values = null;

		String fileextension = file.getFileExtension();
		FuseType fusetype = FuseType.getTypeFromExtension(fileextension);
		if (fusetype == null) {
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
					"File does not have a valid extension");
			throw new CoreException(status);
		}

		InputStream inStream = file.getContents();
		values = createByteValuesFromStream(inStream, fusetype);

		return values;
	}

	public static ByteValues createByteValuesFromStream(InputStream inStream, FuseType type)
			throws CoreException {

		Properties props = new Properties();
		try {
			props.load(inStream);
		} catch (IOException ioe) {
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "Can't read the file",
					ioe);
			throw new CoreException(status);
		}

		// Get the MCU and create the ByteValues object
		String mcuid = props.getProperty("MCU");

		if (mcuid == null) {
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
					"file has no [MCU] property");
			throw new CoreException(status);
		}

		ByteValues values = new ByteValues(type, mcuid);
		
		// The file summary property is used as a bytevalues comment
		String comment = props.getProperty("summary", "");
		values.setComment(comment);

		// Now read all Bitfieldvalues
		for (Object keyobj : props.keySet()) {
			String key = (String) keyobj;
			if ("MCU".equalsIgnoreCase(key) || "summary".equalsIgnoreCase(key)) {
				// Ignore MCU and summary properties
				continue;
			}

			// Test if the key is a valid BitField name.
			// If no skip it, if yes read the key value and apply it
			if (values.getBitFieldDescription(key) == null) {
				continue;
			}

			try {
				int intvalue = Integer.decode((String) props.get(key));
				values.setNamedValue(key, intvalue);
			} catch (NumberFormatException nfe) {
				// ignore - the value is not applied
			} catch (IllegalArgumentException iae) {
				// ignore - the value is not applied
			}

		}

		return values;

	}
}
