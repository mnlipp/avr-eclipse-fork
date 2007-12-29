/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 * $Id: PreferenceConstants.java 9 2007-11-25 21:51:59Z thomas $
 *     
 *******************************************************************************/

package de.innot.avreclipse.ui.preferences;

/**
 * Constant definitions for plug-in preferences.
 * 
 * <p>
 * Here the IDs of all preference items are defined.
 * </p>
 */
public interface PreferenceConstants {

	/** id for the path to the gcc executable */
	public static final String PREF_AVRGCCPATH = "gccpath";
	
	/** id for the path to the make executable */
	public static final String PREF_AVRMAKEPATH = "makepath";
	
	/** id for the path to the avrdude executable */
	public static final String PREF_AVRDUDEPATH = "avrdudepath";
	
	/** id for the path to the avrdude config file */
	public static final String PREF_AVRDUDECONFIGPATH = "avrdudeconfigpath";
	
	
	/** Stores the current source for the DeviceView */
	public static final String PREF_DEVICEVIEW_CONTENTSOURCE = "deviceviewcontentsource";
	// Value for source <avr/io.h>
	public static final String PREFVALUE_DEVICEVIEW_SOURCE_AVRIO = "avrio";
	// Value for source partdescriptionfiles
	public static final String PREFVALUE_DEVICEVIEW_SOURCE_PDF = "pdf";

	/** Stores the path of the <code><avr/io.h></code> file */
	public static final String PREF_DEVICEVIEW_AVR_IO_H = "avriohfile";

	/** Stores the path of the <code>partdefinitionfiles</code> folder */
	public static final String PREF_DEVICEVIEW_AVRPDF_PATH = "avrpdfpath";

}
