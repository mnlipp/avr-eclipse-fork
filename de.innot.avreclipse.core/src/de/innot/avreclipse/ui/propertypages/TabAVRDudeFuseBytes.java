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
package de.innot.avreclipse.ui.propertypages;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.BaseBytesProperties;
import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;

/**
 * The AVRDude Lockbits Tab page.
 * <p>
 * On this tab, the following properties are edited:
 * <ul>
 * <li>Upload of the Lockbits</li>
 * </ul>
 * The lockbit values can either be entered directly, or a lockbits file can be selected which
 * provides the lockbit values.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class TabAVRDudeFuseBytes extends AbstractTabAVRDudeBytes {

	/** Max number of Fuse bytes */
	private final static int		MAX_FUSES	= 6;

	/** The byte editor labels */
	private final static String[]	FUSENAMES	= { "low", "high", "ext." };

	private final static String[]	LABELS		= new String[] { "Fuse Bytes", "fuse bytes" };

	/** The file extensions for fuses files. Used by the file selector. */
	private final static String[]	FUSES_EXTS	= new String[] { "*.fuses" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getMaxBytes()
	 */
	@Override
	protected int getMaxBytes() {
		return MAX_FUSES;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getByteEditorLabel(int)
	 */
	@Override
	protected String getByteEditorLabel(int index) {
		int fusecount = fBytes.getValues().length;

		if (fusecount == 1) {
			// Single Fuse byte MCU: Name "fuse"
			return "fuse";
		}

		if (fusecount <= 3) {
			// pre-ATXmega format: up to three fusebytes with the name "low", "high" and "ext."
			if (0 <= index && index < FUSENAMES.length) {
				return FUSENAMES[index];
			}
			// Return an empty name for invalid index values.
			return "";
		}

		// new ATXmega format: more than three fusebytes, just numbered 1...n
		return Integer.toString(index);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getByteProps(de.innot.avreclipse.core.properties.AVRDudeProperties)
	 */
	@Override
	protected BaseBytesProperties getByteProps(AVRDudeProperties avrdudeprops) {
		return avrdudeprops.getFuseBytes();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getByteValues(de.innot.avreclipse.core.properties.AVRDudeProperties)
	 */
	@Override
	protected ByteValues getByteValues(AVRDudeProperties avrdudeprops) throws AVRDudeException {
		return AVRDude.getDefault().getFuseBytes(avrdudeprops.getProgrammer());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getFileExtensions()
	 */
	@Override
	protected String[] getFileExtensions() {
		return FUSES_EXTS;
	}

	@Override
	protected String[] getLabels() {
		return LABELS;
	}

}
