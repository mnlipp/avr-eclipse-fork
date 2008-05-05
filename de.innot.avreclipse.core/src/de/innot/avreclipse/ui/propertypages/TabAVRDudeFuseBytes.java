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

import java.io.IOException;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.AbstractBytes;
import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.Fuses;

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
	private final static int		MAX_FUSES	= 3;

	/** The byte editor labels */
	private final static String[]	FUSENAMES	= { "low", "high", "ext." };

	private final static String[]	LABELS		= new String[] { "Fuse Bytes", "fuse bytes" };

	/** The file extensions for fuses files. Used by the file selector. */
	public final static String[]	FUSES_EXTS	= new String[] { "*.fuses" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getByteCount(java.lang.String)
	 */
	@Override
	protected int getByteCount(String mcuid) {
		try {
			return Fuses.getDefault().getByteCount(mcuid);
		} catch (IOException e) {
			// can't load the fuses description for the MCU.
			// return 0 = no fuses.
			return 0;
		}
	}

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
		if (0 <= index && index < FUSENAMES.length) {
			return FUSENAMES[index];
		}
		// Return an empty name for yet unknown index values.
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getByteProps(de.innot.avreclipse.core.properties.AVRDudeProperties)
	 */
	@Override
	protected AbstractBytes getByteProps(AVRDudeProperties avrdudeprops) {
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
