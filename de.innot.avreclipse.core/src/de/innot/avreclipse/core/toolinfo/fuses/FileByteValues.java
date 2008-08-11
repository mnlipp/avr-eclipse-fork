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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;

/**
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class FileByteValues implements IResourceChangeListener {

	private IFile							fFile;

	private ByteValues						fByteValues;

	private final String					fComment	= "";

	private List<IByteValuesChangeListener>	fListeners	= null;

	private boolean							hasSaved	= false;

	public FileByteValues(IPath path) throws CoreException {

		fFile = getFileFromLocation(path);

		load(fFile);
	}

	public FileByteValues(IFile file) throws CoreException {
		fFile = file;
		load(fFile);
	}

	private FileByteValues(IPath path, FuseType type, String mcuid) throws CoreException {

		fFile = getFileFromLocation(path);
		fByteValues = new ByteValues(type, mcuid);
	}

	public static FileByteValues createNewFile(IPath location, String mcuid, FuseType type)
			throws CoreException {

		FileByteValues newfile = new FileByteValues(location, type, mcuid);
		newfile.fByteValues.setDefaultValues();

		return newfile;
	}

	public void addChangeListener(IByteValuesChangeListener listener) {
		if (fListeners == null) {
			fListeners = new ArrayList<IByteValuesChangeListener>(1);
			ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		}

		if (!fListeners.contains(listener)) {
			fListeners.add(listener);
		}
	}

	public void removeChangeListener(IByteValuesChangeListener listener) {
		if (fListeners == null) {
			return;
		}

		if (fListeners.contains(listener)) {
			fListeners.remove(listener);
			if (fListeners.size() == 0) {
				fListeners = null;
				ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
			}
		}
	}

	public IFile getSourceFile() {
		return fFile;
	}

	public void save(IProgressMonitor monitor) throws CoreException {

		internalSave(monitor, fFile);
		hasSaved = true;
	}

	public void saveAs(IProgressMonitor monitor, IFile file) throws CoreException {

		internalSave(monitor, file);
		fFile = file;
		hasSaved = true;
	}

	private void internalSave(IProgressMonitor monitor, IFile file) throws CoreException {
		if (file.exists()) {
			file.setContents(createContentStream(), true, true, monitor);
		} else {
			file.create(createContentStream(), true, monitor);
		}
	}

	public ByteValues getByteValues() {
		return fByteValues;
	}

	public void setByteValue(ByteValues newvalues) {
		fByteValues = newvalues;
	}

	private IFile getFileFromLocation(IPath location) throws CoreException {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();

		IFile[] files = root.findFilesForLocation(location);
		if (files.length == 0) {
			// throw a FileNotFoundException
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "File ["
					+ location.toOSString() + "] not found.", new FileNotFoundException(location
					.toOSString()));
			throw new CoreException(status);
		}

		return files[0];

	}

	private void load(IFile file) throws CoreException {

		// First get the type of file from the extension
		FuseType type = null;
		String extension = file.getFileExtension();
		if (FuseType.FUSE.getExtension().equalsIgnoreCase(extension)) {
			type = FuseType.FUSE;
		} else if (FuseType.LOCKBITS.getExtension().equalsIgnoreCase(extension)) {
			type = FuseType.LOCKBITS;
		} else {
			// TODO: set a problem marker
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "File ["
					+ file.getFullPath().toOSString() + "] has an unrecognized extension.", null);
			throw new CoreException(status);
		}

		// Now read the content of the file
		InputStream is = file.getContents(true);

		BufferedReader br = new BufferedReader(new InputStreamReader(is));

		List<String> content = null;
		try {
			content = readFile(br);
		} catch (IOException e) {
			// TODO: set a problem marker
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "Can't read the file ["
					+ file.getFullPath().toOSString() + "]", null);
			throw new CoreException(status);
		}

		// Next we find the MCU id,
		// test that it is valid
		// and create a new ByteValues object for it
		String mcuid = null;
		int linenumber = -1;

		// Find the mcu
		for (int i = 0; i < content.size(); i++) {
			String line = content.get(i);
			if (line.startsWith("MCU")) {
				mcuid = getPropertyValue(line);
				linenumber = i;
				break;
			}
		}

		if (mcuid == null) {
			// TODO: set a problem marker
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "file ["
					+ file.getFullPath().toOSString() + "] does not have required MCU property",
					null);
			throw new CoreException(status);
		}

		// test that the mcu is actually supported for fuses
		if (!Fuses.getDefault().hasMCU(mcuid)) {
			// TODO: set a problem marker
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, file.getFullPath()
					.toOSString()
					+ ":" + linenumber + " MCU [" + mcuid + "]is not supported.", null);
			throw new CoreException(status);
		}

		// if this is the first call to load we need to initialize
		// our ByteValues.
		if (fByteValues == null) {
			fByteValues = new ByteValues(type, mcuid);
		}

		// This is required if the load() was called due to an external update of the file. In this
		// case the mcu might have changed.
		if (!fByteValues.getMCUId().equals(mcuid)) {
			fByteValues.setMCUId(mcuid, false);
		}

		List<String> allnames = fByteValues.getBitfieldNames();

		// and finally read the rest of the content
		for (int i = 0; i < content.size(); i++) {
			String line = content.get(i);
			if (line.startsWith("#")) {
				// ignore comments
				continue;
			}
			if (line.startsWith("MCU")) {
				// already handled this one
				continue;
			}
			if (line.startsWith("summary")) {
				fByteValues.setComment(getPropertyValue(line));
				continue;
			}
			String bitfieldname = getPropertyTag(line);
			String bitfieldvalue = getPropertyValue(line);

			if (!allnames.contains(bitfieldname)) {
				// Invalid bitfield name - ignore
				// TODO: Set a problem marker
				continue;
			}

			int value;
			try {
				value = Integer.decode(bitfieldvalue);
				fByteValues.setNamedValue(bitfieldname, value);
			} catch (NumberFormatException nfe) {
				// Invalid bitfield value - ignore
				// TODO: Set a problem marker
				continue;
			} catch (IllegalArgumentException iae) {
				// Invalid bitfield value - ignore
				// TODO: Set a problem marker
				continue;
			}
		}

		fireByteValuesChangedEvent();
	}

	public void fireByteValuesChangedEvent() {
		if (fListeners != null) {
			for (IByteValuesChangeListener listener : fListeners) {
				listener.ByteValuesChanged(fByteValues);
			}
		}
	}

	private List<String> readFile(BufferedReader reader) throws IOException {
		String line;
		List<String> list = new ArrayList<String>();

		try {
			while (null != (line = reader.readLine())) {
				list.add(line);
			}
		} finally {
			try {
				if (reader != null)
					reader.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		return list;

	}

	private String getPropertyTag(String line) {
		int splitat = line.indexOf('=');
		if (splitat == -1) {
			return null;
		}
		String tagpart = line.substring(0, splitat);
		return tagpart.trim();
	}

	private String getPropertyValue(String line) {
		int splitat = line.indexOf('=');
		if (splitat == -1) {
			return null;
		}
		String valuepart = line.substring(splitat + 1);
		return valuepart.trim();
	}

	private InputStream createContentStream() {

		StringBuilder sb = new StringBuilder();

		sb.append("MCU=" + fByteValues.getMCUId() + "\n");

		List<BitFieldDescription> allbfds = fByteValues.getBitfieldDescriptions();

		for (BitFieldDescription bfd : allbfds) {
			String name = bfd.getName();
			int value = fByteValues.getNamedValue(name);
			if (value == -1) {
				value = bfd.getMaxValue();
			}
			String valuetext = "0x" + Integer.toHexString(value);
			sb.append(name + "=" + valuetext + "\n");
		}

		sb.append("summary=" + fByteValues.getComment() + "\n");

		return new ByteArrayInputStream(sb.toString().getBytes());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(IResourceChangeEvent event) {

		if (hasSaved) {
			hasSaved = false;
			return;
		}

		// we are only interested in POST_CHANGE events
		if (event.getType() != IResourceChangeEvent.POST_CHANGE)
			return;
		IResourceDelta rootDelta = event.getDelta();

		// get the delta, if any, for the source file
		IResourceDelta fileDelta = rootDelta.findMember(fFile.getFullPath());
		if (fileDelta == null) {
			// The Resource change did not effect us.
			return;
		}

		// Now check what has happened to the file
		if (fileDelta.getKind() != IResourceDelta.CHANGED)
			return;

		// only interested in content changes
		if ((fileDelta.getFlags() & IResourceDelta.CONTENT) == 0)
			return;

		try {
			load(fFile);
		} catch (CoreException e) {
			// TODO: log exception
		}

	}
}
