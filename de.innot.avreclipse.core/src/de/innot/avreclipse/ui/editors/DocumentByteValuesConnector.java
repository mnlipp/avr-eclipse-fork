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
package de.innot.avreclipse.ui.editors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Position;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.IElementStateListener;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.toolinfo.fuses.BitFieldDescription;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValueChangeEvent;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;
import de.innot.avreclipse.core.toolinfo.fuses.IByteValuesChangeListener;

/**
 * Connect a Fuses file document to a ByteValues object.
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class DocumentByteValuesConnector {

	private final IDocumentProvider			fProvider;

	/** Source of the Document. Used to set / clear problem markers. */
	private IFile							fSource;

	/** The source document */
	private final IDocument					fDocument;

	/** The ByteValues created from and synchronized with the source IDocument. */
	private ByteValues						fByteValues			= null;

	/**
	 * <code>true</code> while the source IDocument is modified from this class, so the document
	 * change listener can ignore the resulting change events.
	 */
	private boolean							fInDocumentChange	= false;

	/**
	 * <code>true</code> while the ByteValues are modified from this class, so the ByteValues
	 * change listener can ignore the resulting change events.
	 */
	private boolean							fInByteValuesChange	= false;

	private final IDocumentListener			fDocumentListener;
	private final IByteValuesChangeListener	fByteValuesListener;
	private final IElementStateListener		fElementStateListener;

	private final Map<String, String>		fKeyValueMap		= new HashMap<String, String>();
	private final Map<String, Integer>		fKeyLineMap			= new HashMap<String, Integer>();
	private final Map<String, Position>		fKeyRegionMap		= new HashMap<String, Position>();
	private final Map<String, Position>		fKeyValueRegionMap	= new HashMap<String, Position>();

	private final static String				KEY_MCU				= "MCU";
	private final static String				KEY_COMMENT			= "summary";

	private final static Pattern			fCommentPattern		= Pattern.compile("\\s*#.*");
	private final static Pattern			fPropertyPattern	= Pattern
																		.compile("\\s*(\\w*)\\s*=(.*)");

	private class MyDocumentListener implements IDocumentListener {
		// ------------ IDocumentListener -------------------

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.text.IDocumentListener#documentAboutToBeChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentAboutToBeChanged(DocumentEvent event) {
			// ignore event
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.text.IDocumentListener#documentChanged(org.eclipse.jface.text.DocumentEvent)
		 */
		public void documentChanged(DocumentEvent event) {
			if (fInDocumentChange) {
				// don't listen to events we generated ourself
				return;
			}

			// This is sub-optimal (but easy):
			// For each modification of the document we parse the complete document again.
			// Not very efficient, but the fuses files are small and even on my
			// old and slow Laptop this was easily finished between two keystrokes.
			readFromDocument();
		}
	}

	private class MyByteValuesChangedListener implements IByteValuesChangeListener {
		// ------------ IByteValuesChangedListener -------------------

		/*
		 * (non-Javadoc)
		 * 
		 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteValuesChangeListener#byteValuesChanged(de.innot.avreclipse.core.toolinfo.fuses.ByteValueChangeEvent[])
		 */
		public void byteValuesChanged(ByteValueChangeEvent[] events) {

			if (fInByteValuesChange) {
				// don't listen to events we generated ourself
				return;
			}

			for (ByteValueChangeEvent event : events) {
				String key = event.name;
				if (key.equals(ByteValues.MCU_CHANGE_EVENT)) {
					clearDocument();
					writeToDocument(fByteValues);
				} else if (key.equals(ByteValues.COMMENT_CHANGE_EVENT)) {
					String comment = fByteValues.getComment();
					if (comment == null) {
						comment = "";
					}
					comment = comment.replace("\r\n", "\\n");
					comment = comment.replace("\n", "\\n");
					setDocumentValue(KEY_COMMENT, comment);
				} else {
					if (event.bytevalue != -1) {
						setDocumentValue(key, event.bitfieldvalue);
					} else {
						removeDocumentValue(key);
					}
				}
			}
		}

	}

	private class MyElementStateListener implements IElementStateListener {
		// ---- DocumentProvider Element Change Listener Methods ------

		public void elementContentAboutToBeReplaced(Object element) {
			// Nothing to do
		}

		public void elementContentReplaced(Object element) {
			// Nothing to do
		}

		public void elementDeleted(Object element) {
			// Nothing to do
		}

		public void elementDirtyStateChanged(Object element, boolean isDirty) {
			// Nothing to do
		}

		public void elementMoved(Object originalElement, Object movedElement) {

			if (originalElement == null) {
				return;
			}
			// If our source file has moved, we need to store the new file, so that we can create
			// new problem markers for it. Existing problem markers are automatically moved by the
			// Workbench.
			IFile originalfile = getFileFromAdaptable(originalElement);
			IFile movedfile = getFileFromAdaptable(movedElement);
			if (fSource.equals(originalfile)) {
				fSource = movedfile;
			}
		}
	}

	public void writeByteValues(ByteValues sourcevalues) {
		writeToDocument(sourcevalues);
	}

	/**
	 * @param provider
	 * @param element
	 * @param document
	 * @throws CoreException
	 */
	public DocumentByteValuesConnector(IDocumentProvider provider, Object element, IDocument document)
			throws CoreException {

		fSource = getFileFromAdaptable(element);
		if (fSource == null) {
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
					"Object must be an IFile", null);
			throw new CoreException(status);
		}

		fDocumentListener = new MyDocumentListener();
		fByteValuesListener = new MyByteValuesChangedListener();
		fElementStateListener = new MyElementStateListener();

		fProvider = provider;
		fProvider.addElementStateListener(fElementStateListener);
		fDocument = document;
		fDocument.addDocumentListener(fDocumentListener);

		// The ByteValues object will be created lazily, i.e. when actually required.
		fByteValues = null;
	}

	public void dispose() {
		fDocument.removeDocumentListener(fDocumentListener);
		fProvider.removeElementStateListener(fElementStateListener);

		if (fByteValues != null) {
			fByteValues.removeChangeListener(fByteValuesListener);
		}
	}

	// ------- Public Methods -------

	public ByteValues getByteValues() {
		if (fByteValues == null) {
			fByteValues = createByteValues();
			readFromDocument();
		}
		return fByteValues;
	}

	private ByteValues createByteValues() {

		FuseType type = null;
		try {
			type = getTypeFromFileExtension(fSource);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}

		parseDocument();

		String mcuid = fKeyValueMap.get(KEY_MCU);
		if (mcuid == null) {
			setMissingMCUMarker("MCU");
			return null;
		}

		ByteValues newvalues = new ByteValues(type, mcuid);
		newvalues.addChangeListener(fByteValuesListener);
		if (newvalues.getByteCount() == 0) {
			// unknown MCU
			setIllegalValue("MCU", mcuid);
		} else {
			clearMarker("MCU");
		}
		return newvalues;

	}

	private void readFromDocument() {

		if (fByteValues == null) {
			fByteValues = createByteValues();
			if (fByteValues == null) {
				// The MCU tag is still missing. No need to continue until the user has added a
				// MCU=... line to the document.
				return;
			}
		}

		parseDocument();

		fInByteValuesChange = true;
		for (String key : fKeyValueMap.keySet()) {
			String value = fKeyValueMap.get(key);
			if ("MCU".equalsIgnoreCase(key)) {
				String oldmcuid = fByteValues.getMCUId();
				if (!oldmcuid.equals(value)) {
					fByteValues.setMCUId(value, false);
					// All previously set BitFields might have changed
					// so just restart from the beginning.
					// On the second iteration the MCUs will match, so no
					// danger of recursion.
					readFromDocument();
					return;
				}
				if (fByteValues.getByteCount() == 0) {
					// unknown MCU
					setIllegalValue("MCU", value);
				} else {
					clearMarker("MCU");
				}
				continue;
			}

			if ("summary".equalsIgnoreCase(key)) {
				String comment = value.replace("\\n", "\n");
				fByteValues.setComment(comment);
				continue;
			}

			if (fByteValues.getBitFieldDescription(key) == null) {
				setInvalidKey(key);
				continue;
			}
			try {
				int intvalue = Integer.decode(fKeyValueMap.get(key));
				fByteValues.setNamedValue(key, intvalue);
				clearMarker(key);
			} catch (NumberFormatException nfe) {
				setIllegalValue(key, value);
			} catch (IllegalArgumentException iae) {
				setIllegalValue(key, value);
			}
		}
		fInByteValuesChange = false;

	}

	public void writeToDocument(ByteValues newvalues) {

		clearDocument();

		setDocumentValue(KEY_MCU, newvalues.getMCUId());

		List<BitFieldDescription> bfdlist = newvalues.getBitfieldDescriptions();
		for (BitFieldDescription bfd : bfdlist) {
			String key = bfd.getName();
			int value = newvalues.getNamedValue(key);
			if (value == -1) {
				continue;
			}
			setDocumentValue(key, value);
		}

		String comment = newvalues.getComment();
		if (comment == null) {
			comment = "";
		}
		comment = comment.replace("\r\n", "\\n");
		comment = comment.replace("\n", "\\n");
		setDocumentValue(KEY_COMMENT, comment);

	}

	private void setDocumentValue(String key, int value) {
		String textvalue = "0x" + Integer.toHexString(value);
		setDocumentValue(key, textvalue);
	}

	private void setDocumentValue(String key, String value) {
		try {

			int offset;
			int length;
			String text;
			if (value == null) {
				value = "";
			}
			Integer linenumber = fKeyLineMap.get(key);
			if (linenumber == null) {
				// Key does not exist yet - add it.
				offset = fDocument.getLength();
				length = 0;
				text = key + "=" + value + "\n";
				fInDocumentChange = true;
				fDocument.replace(offset, length, text);
				linenumber = fDocument.getLineOfOffset(offset + 4);

			} else {
				// Key exists - replace value;
				Position region = fKeyValueRegionMap.get(key);
				offset = region.getOffset();
				length = region.getLength();
				text = value;
				fInDocumentChange = true;
				fDocument.replace(offset, length, text);

				// remove the key from all maps so that it can be re-read
				fKeyValueMap.remove(key);
				fKeyLineMap.remove(key);
				fDocument.removePosition(fKeyRegionMap.get(key));
				fDocument.removePosition(fKeyValueRegionMap.get(key));
				fKeyRegionMap.remove(key);
				fKeyValueRegionMap.remove(key);
			}
			// (re)read the line to update the internal maps
			parseLine(linenumber);
			clearMarker(key);

		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			fInDocumentChange = false;
		}
	}

	private void removeDocumentValue(String key) {
		try {
			Integer linenumber = fKeyLineMap.get(key);
			if (linenumber != null) {
				// first remove the key from all lists
				fKeyValueMap.remove(key);
				fKeyLineMap.remove(key);
				fDocument.removePosition(fKeyRegionMap.get(key));
				fDocument.removePosition(fKeyValueRegionMap.get(key));
				fKeyRegionMap.remove(key);
				fKeyValueRegionMap.remove(key);
				clearMarker(key);

				// then update the list of lines
				for (String otherkey : fKeyLineMap.keySet()) {
					int otherline = fKeyLineMap.get(otherkey);
					if (otherline > linenumber) {
						fKeyLineMap.put(otherkey, otherline - 1);
					}
				}

				// then remove the line from the document
				IRegion lineregion = fDocument.getLineInformation(linenumber);
				String delimiter = fDocument.getLineDelimiter(linenumber);
				int offset = lineregion.getOffset();
				int length = lineregion.getLength() + delimiter.length();
				fInDocumentChange = true;
				fDocument.replace(offset, length, null);
			}
		} catch (BadLocationException ble) {
			// ignore
		} finally {
			fInDocumentChange = false;
		}

	}

	private void clearDocument() {

		clearAllMarkers();

		// remove all lines with keys in them
		for (String key : fKeyRegionMap.keySet()) {
			Position position = fKeyRegionMap.get(key);
			if (!position.isDeleted) {
				try {
					int line = fDocument.getLineOfOffset(position.offset);
					int offset = fDocument.getLineOffset(line);
					int length = fDocument.getLineLength(line);
					fInDocumentChange = true;
					fDocument.replace(offset, length, null);
				} catch (BadLocationException e) {
					// TODO log exception
					e.printStackTrace();
				} finally {
					fInDocumentChange = false;
				}
			}
		}
		fKeyLineMap.clear();
		fKeyRegionMap.clear();
		fKeyValueMap.clear();
		fKeyValueRegionMap.clear();

	}

	private void setMissingMCUMarker(String key) {
		String message = "Required Property 'MCU' missing";
		createMarker(key, IMarker.SEVERITY_ERROR, -1, 0, 0, message);
	}

	private void setInvalidKey(String key) {
		Position keyRegion = fKeyRegionMap.get(key);
		int start = keyRegion.getOffset();
		int end = start + keyRegion.getLength();
		int linenumber = fKeyLineMap.get(key);
		String message = "Invalid BitField name '" + key + "'";
		createMarker(key, IMarker.SEVERITY_WARNING, linenumber, start, end, message);
	}

	private void setIllegalValue(String key, String value) {
		Position valueRegion = fKeyValueRegionMap.get(key);
		int start = valueRegion.getOffset();
		int end = start + valueRegion.getLength();
		int linenumber = fKeyLineMap.get(key);
		String message = key + ": Invalid value '" + value + "'";
		createMarker(key, IMarker.SEVERITY_WARNING, linenumber, start, end, message);
	}

	private void setDuplicateKey(String key, int linenumber, int start, int end) {
		String message = "Duplicate BitField name " + key;
		createMarker(null, IMarker.SEVERITY_WARNING, linenumber, start, end, message);
	}

	private void createMarker(String key, int severity, int linenumber, int start, int end,
			String message) {

		if (fSource.exists()) {
			try {
				IMarker marker = fSource.createMarker(IMarker.PROBLEM);
				marker.setAttribute(IMarker.SEVERITY, severity);
				marker.setAttribute(IMarker.LINE_NUMBER, linenumber + 1);
				marker.setAttribute(IMarker.CHAR_START, start);
				marker.setAttribute(IMarker.CHAR_END, end);
				marker.setAttribute(IMarker.MESSAGE, message);
				marker.setAttribute(IMarker.SOURCE_ID, key);
				return;
			} catch (CoreException ce) {
				// ignore the exception -> no marker created
			}
		}
	}

	private void clearMarker(String key) {
		if (fSource.exists()) {
			// find all markers with the SOURCE_ID with the given key
			try {
				IMarker[] allmarkers = fSource.findMarkers(IMarker.PROBLEM, true,
						IResource.DEPTH_INFINITE);
				for (IMarker marker : allmarkers) {
					String markerkey = marker.getAttribute(IMarker.SOURCE_ID, "");
					if (markerkey.equals(key)) {
						marker.delete();
					}
				}

			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void clearAllMarkers() {
		if (fSource.exists()) {
			try {
				fSource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void parseDocument() {

		int lines = fDocument.getNumberOfLines();

		// Clear all maps
		fKeyLineMap.clear();
		fKeyRegionMap.clear();
		fKeyValueRegionMap.clear();
		fKeyValueMap.clear();
		clearAllMarkers();

		try {
			for (int linenumber = 0; linenumber < lines; linenumber++) {
				parseLine(linenumber);
			}
		} catch (BadLocationException ble) {
			// Should not happen
			// TODO: log an error
			ble.printStackTrace();
		} catch (CoreException ce) {
			// Could not set marker
			// TODO: log a error
			ce.printStackTrace();
		}
	}

	private void parseLine(int linenumber) throws BadLocationException, CoreException {

		IRegion lineregion = fDocument.getLineInformation(linenumber);

		int offset = lineregion.getOffset();
		int length = lineregion.getLength();
		String linecontent = fDocument.get(offset, length);

		Matcher matcher;

		// Test if valid property line
		matcher = fPropertyPattern.matcher(linecontent);
		if (matcher.matches()) {
			String key = matcher.group(1);
			String value = matcher.group(2).trim();
			if (fKeyValueMap.containsKey(key)) {
				// duplicate key -> marks as error
				setDuplicateKey(key, linenumber, offset + matcher.start(1), offset + matcher.end(1));
			}
			fKeyValueMap.put(key, value);
			fKeyLineMap.put(key, linenumber);

			int keyoffset = offset + matcher.start(1);
			int keylength = offset + matcher.end(1) - keyoffset;
			fKeyRegionMap.put(key, addPosition(keyoffset, keylength));

			int valueoffset = offset + matcher.start(2);
			int valuelength = offset + matcher.end(2) - valueoffset;
			fKeyValueRegionMap.put(key, addPosition(valueoffset, valuelength));
			return;

		}

		// Test if Comment
		matcher = fCommentPattern.matcher(linecontent);
		if (matcher.matches()) {
			return;
		}

		// Test if empty line
		if (linecontent.trim().length() == 0) {
			return;
		}

		createMarker(null, IMarker.SEVERITY_WARNING, linenumber, offset, offset + length,
				"Undefined line");
		return;
	}

	private Position addPosition(int offset, int length) {
		Position position = new Position(offset, length);
		try {
			fDocument.addPosition(position);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return position;
	}

	private FuseType getTypeFromFileExtension(IFile file) throws CoreException {

		// First get the type of file from the extension
		String extension = file.getFileExtension();
		if (FuseType.FUSE.getExtension().equalsIgnoreCase(extension)) {
			return FuseType.FUSE;
		} else if (FuseType.LOCKBITS.getExtension().equalsIgnoreCase(extension)) {
			return FuseType.LOCKBITS;
		} else {
			// TODO: set a problem marker
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "File ["
					+ file.getFullPath().toOSString() + "] has an unrecognized extension.", null);
			throw new CoreException(status);
		}
	}

	private IFile getFileFromAdaptable(Object element) {
		if (element instanceof IAdaptable) {
			IAdaptable adaptable = (IAdaptable) element;
			IFile file = (IFile) adaptable.getAdapter(IFile.class);
			return file;
		}
		return null;
	}

}
