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
package de.innot.avreclipse.debug.gdbservers.avarice;

import de.innot.avreclipse.debug.core.AVRDebugPlugin;

/**
 * The <code>ILaunchConfiguration</code> attributes for AVaRICE, including their default values.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface IGDBServerAvariceConstants {

	// Get a reference to the core debug plugin id for quick reference
	public final static String		PLUGIN_ID								= AVRDebugPlugin.PLUGIN_ID;

	// ///////////////////////////////////////////////////////////////
	//
	// The constants for the avarice gdbserver
	//
	// ///////////////////////////////////////////////////////////////

	// ///////////////////////////////////////////////
	// Main attributes
	// ///////////////////////////////////////////////

	/** The name (optional incl. path) of the avarice executable. Default: "avarice" */
	public static final String		ATTR_GDBSERVER_AVARICE_COMMAND			= PLUGIN_ID
																					+ ".avarice.command";
	public static final String		DEFAULT_GDBSERVER_AVARICE_COMMAND		= "avarice";

	/** Flag for verbose (debugging) output of avarice. Default: <code>false</code> */
	public static final String		ATTR_GDBSERVER_AVARICE_VERBOSE			= PLUGIN_ID
																					+ ".avarice.verbose";
	// FIXME: Change this to false for the release
	public static final boolean		DEFAULT_GDBSERVER_AVARICE_VERBOSE		= true;

	/** The hostname for the avarice gdbserver. Default: "localhost". Should not be changed. */
	public static final String		ATTR_GDBSERVER_AVARICE_HOSTNAME			= PLUGIN_ID
																					+ ".avarice.hostname";
	public static final String		DEFAULT_GDBSERVER_AVARICE_HOSTNAME		= "localhost";

	/** The port for avarice. Default: 4242 */
	public static final String		ATTR_GDBSERVER_AVARICE_PORT				= PLUGIN_ID
																					+ ".avarice.port";
	public static final int			DEFAULT_GDBSERVER_AVARICE_PORT			= 4242;

	// ///////////////////////////////////////////////
	// JTAG Interface attributes
	// ///////////////////////////////////////////////

	/**
	 * The JTAG interface for avarice. Possible values:
	 * <ul>
	 * <li>"AVR Dragon" [Default]</li>
	 * <li>"AVRISP MkI or compatible"</li>
	 * <li>"AVRICE MkII or compatible"</li>
	 * </ul>
	 */
	public static final String		ATTR_GDBSERVER_AVARICE_INTERFACE		= PLUGIN_ID
																					+ ".avarice.interface";
	public static final String		DEFAULT_GDBSERVER_AVARICE_INTERFACE		= "AVR Dragon";

	/** The host system port for the JTAG interface. Default: "usb". */
	public static final String		ATTR_GDBSERVER_AVARICE_JTAGPORT			= PLUGIN_ID
																					+ ".avarice.jtagport";
	public static final String		DEFAULT_GDBSERVER_AVARICE_JTAGPORT		= "usb";

	/** The JTAG bitrate. Default: undefined => avarice default of 250kHz */
	public static final String		ATTR_GDBSERVER_AVARICE_JTAGBITRATE		= PLUGIN_ID
																					+ ".avarice.jtagbitrate";
	public static final String		DEFAULT_GDBSERVER_AVARICE_JTAGBITRATE	= "";

	/** Flag for avarice to use the debugWire interface. Default: <code>false</code> */
	public static final String		ATTR_GDBSERVER_AVARICE_DEBUGWIRE		= PLUGIN_ID
																					+ ".avarice.debugwire";
	public static final boolean		DEFAULT_GDBSERVER_AVARICE_DEBUGWIRE		= false;

	// ///////////////////////////////////////////////
	// Interrupt control attributes
	// ///////////////////////////////////////////////

	/** Flag for avarice to ignore interrupts. Default: <code>true</code> */
	public static final String		ATTR_GDBSERVER_AVARICE_IGNOREINTR		= PLUGIN_ID
																					+ ".avarice.ignoreintr";
	public static final boolean		DEFAULT_GDBSERVER_AVARICE_IGNOREINTR	= true;

	// ///////////////////////////////////////////////
	// The "Other options" attribute
	// ///////////////////////////////////////////////

	/** Other avarice options not covered by the plugin. Default: none */
	public static final String		ATTR_GDBSERVER_AVARICE_OTHEROPTIONS		= PLUGIN_ID
																					+ ".avarice.otheroptions";
	public static final String		DEFAULT_GDBSERVER_AVARICE_OTHEROPTIONS	= "";

}
