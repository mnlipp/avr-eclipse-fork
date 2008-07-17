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
package de.innot.avreclipse.core.avrdude;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;
import de.innot.avreclipse.core.toolinfo.fuses.Fuses;

/**
 * Storage independent container for the Fuse Byte values.
 * <p>
 * This class has two modes. Depending on the {@link #fUseFile} flag, it will either read the fuse
 * values from a supplied file or immediate values stored in a {@link FuseByteValues} object. The
 * mode is selected by the user in the Properties user interface.
 * </p>
 * <p>
 * This class can be used either standalone or as part of the AVRProjectProperties structure.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class FuseBytes extends AbstractBytes {

	/**
	 * Create a new FuseBytes object and load the properties from the Preferences.
	 * <p>
	 * If the given Preferences has no saved properties yet, the default values are used.
	 * </p>
	 * 
	 * @param prefs
	 *            <code>Preferences</code> to read the properties from.
	 * @param parent
	 *            Reference to the <code>AVRDudeProperties</code> parent object.
	 */
	public FuseBytes(Preferences prefs, AVRDudeProperties parent) {
		super(prefs, parent);
	}

	/**
	 * Create a new FuseBytes object and copy from the given FuseByte object.
	 * <p>
	 * All values from the source are copied, except for the source Preferences and the Parent.
	 * </p>
	 * 
	 * @param prefs
	 *            <code>Preferences</code> to read the properties from.
	 * @param parent
	 *            Reference to the <code>AVRDudeProperties</code> parent object.
	 * @param source
	 *            <code>FuseBytes</code> object to copy.
	 */
	public FuseBytes(Preferences prefs, AVRDudeProperties parent, FuseBytes source) {
		super(prefs, parent, source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.avrdude.AbstractBytes#createByteValuesObject(java.lang.String)
	 */
	@Override
	protected ByteValues createByteValuesObject(String mcuid) {
		return new ByteValues(FuseType.FUSE, mcuid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.avrdude.AbstractBytes#createByteValuesObject(de.innot.avreclipse.core.toolinfo.fuses.ByteValues)
	 */
	@Override
	protected ByteValues createByteValuesObject(ByteValues source) {
		return new ByteValues(source);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.avrdude.AbstractBytes#getMaxBytes()
	 */
	@Override
	protected int getByteCount() {
		try {
			return Fuses.getDefault().getFuseByteCount(getMCUId());
		} catch (IOException e) {
			// If you want to see the Exception use the Fuses class directly
			return 0;
		}
	}

	/**
	 * Get the list of avrdude arguments required to write all fuse bytes.
	 * <p>
	 * Note: This method does <strong>not</strong> set the "-u" flag to disable the safemode. It is
	 * up to the caller to add this flag. If the "disable safemode" flag is not set, avrdude will
	 * restore the previous fusebyte values after the new values have been written.
	 * </p>
	 * 
	 * @return <code>List&lt;String&gt;</code> with avrdude action options.
	 */
	@Override
	public List<String> getArguments(String mcuid) {
		List<String> args = new ArrayList<String>();

		if (!isCompatibleWith(mcuid)) {
			// If the fuse bytes are not valid (mismatch between read and
			// assigned mcu id) return an empty list,
			return args;
		}

		int[] fusevalues = getValues();

		if (getUseFile()) {
			// Use a fuses file
			// Read the fusevalues from the file.

			// TODO Not implemented yet
		}

		// The action factory will take of generating just the right number of
		// actions for the MCU.
		List<AVRDudeAction> allactions = AVRDudeActionFactory
				.writeFuseBytes(getMCUId(), fusevalues);

		for (AVRDudeAction action : allactions) {
			args.add(action.getArgument());
		}

		return args;

	}

}
