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
package de.innot.avreclipse.debug.gdbservers.simulavr;

import de.innot.avreclipse.debug.core.AVRDebugPlugin;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface IGDBServerSimulavrConstants {

	// Get a reference to the core debug plugin id for quick reference
	public final static String	PLUGIN_ID										= AVRDebugPlugin.PLUGIN_ID;

	// ///////////////////////////////////////////////////////////////
	//
	// The constants for the simulavr gdbserver
	//
	// ///////////////////////////////////////////////////////////////

	/** The name (optional incl. path) of the simulavr executable. Default: "simulavr" */
	public static final String	ATTR_GDBSERVER_SIMULAVR_COMMAND					= PLUGIN_ID
																						+ ".simulavr.command";
	public static final String	DEFAULT_GDBSERVER_SIMULAVR_COMMAND				= "simulavr";

	/** The hostname for the simulavr gdbserver. Default: "localhost". Should not be changed. */
	public static final String	ATTR_GDBSERVER_SIMULAVR_HOSTNAME				= PLUGIN_ID
																						+ ".simulavr.hostname";
	public static final String	DEFAULT_GDBSERVER_SIMULAVR_HOSTNAME				= "localhost";

	/** The port for avarice. Default: 4242 */
	public static final String	ATTR_GDBSERVER_SIMULAVR_PORT					= PLUGIN_ID
																						+ ".simulavr.port";
	public static final int		DEFAULT_GDBSERVER_SIMULAVR_PORT					= 1212;

	/** Flag for verbose (gdbserver) output of simulavr. Default: <code>false</code> */
	public static final String	ATTR_GDBSERVER_SIMULAVR_VERBOSE_GDBSERVER		= PLUGIN_ID
																						+ ".simulavr.verbose.gdbserver";

	// FIXME: Change this to false for the release
	public static final boolean	DEFAULT_GDBSERVER_SIMULAVR_VERBOSE_GDBSERVER	= true;

	/** Flag for verbose (simulator) output of simulavr. Default: <code>false</code> */
	public static final String	ATTR_GDBSERVER_SIMULAVR_VERBOSE_DEBUG			= PLUGIN_ID
																						+ ".simulavr.verbose.debug";
	public static final boolean	DEFAULT_GDBSERVER_SIMULAVR_VERBOSE_DEBUG		= false;

	/** The AVR processor to simulate by simulavr. Default: "atmega16" */
	public static final String	ATTR_GDBSERVER_SIMULAVR_MCUTYPE					= PLUGIN_ID
																						+ ".simulavr.mcutype";
	public static final String	DEFAULT_GDBSERVER_SIMULAVR_MCUTYPE				= "atmega16";

	/** The AVR processor CPU clock to simulate by simulavr. Default: 1.000.000Hz */
	public static final String	ATTR_GDBSERVER_SIMULAVR_FCPU					= PLUGIN_ID
																						+ ".simulavr.fcpu";
	public static final int		DEFAULT_GDBSERVER_SIMULAVR_FCPU					= 1000000;

	// ///////////////////////////////////////////////
	// The "Other options" attribute
	// ///////////////////////////////////////////////

	/** Other avarice options not covered by the plugin. Default: none */
	public static final String	ATTR_GDBSERVER_SIMULAVR_OTHEROPTIONS			= PLUGIN_ID
																						+ ".avarice.otheroptions";
	public static final String	DEFAULT_GDBSERVER_SIMULAVR_OTHEROPTIONS			= "";

}
