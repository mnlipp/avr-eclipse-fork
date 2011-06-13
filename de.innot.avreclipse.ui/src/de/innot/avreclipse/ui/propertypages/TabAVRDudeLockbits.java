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
package de.innot.avreclipse.ui.propertypages;

import org.eclipse.core.runtime.IProgressMonitor;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.BaseBytesProperties;
import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;

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
public class TabAVRDudeLockbits extends AbstractTabAVRDudeBytes {

	private final static String[]	LABELS			= new String[] { "Lockbits", "lockbits" };

	/** The file extensions for lockbits files. Used by the file selector. */
	private final static String[]	LOCKBITS_EXTS	= new String[] { "*.locks" };

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getType()
	 */
	@Override
	protected FuseType getType() {
		return FuseType.LOCKBITS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getByteEditorLabel(int)
	 */
	@Override
	protected String getByteEditorLabel(int index) {
		// don't use a label for the lockbits byte value editor
		return "";
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getByteProps(de.innot.avreclipse.core.properties.AVRDudeProperties)
	 */
	@Override
	protected BaseBytesProperties getByteProps(AVRDudeProperties avrdudeprops) {
		return avrdudeprops.getLockbitBytes(getCfg());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getByteValues(de.innot.avreclipse.core.properties.AVRDudeProperties)
	 */
	@Override
	protected ByteValues getByteValues(AVRDudeProperties avrdudeprops, IProgressMonitor monitor)
			throws AVRDudeException {
		return AVRDude.getDefault().getLockbits(avrdudeprops.getProgrammer(), monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getFileExtensions()
	 */
	@Override
	protected String[] getFileExtensions() {
		return LOCKBITS_EXTS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractTabAVRDudeBytes#getLabelString(int)
	 */
	@Override
	protected String[] getLabels() {
		return LABELS;
	}

}
