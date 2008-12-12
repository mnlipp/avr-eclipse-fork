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

package de.innot.avreclipse.debug.core;

/**
 * The interface contains the attributes for the AVR specific LaunchConfiguration settings.
 * <p>
 * Where applicable each Attribute has a default value.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface IAVRGDBConstants {

	public static final String	LAUNCH_TYPE_ID					= "de.innot.avreclipse.debug.lcTypeAVRGDBHardware";
	public static final String	DEBUGGER_ID						= "de.innot.avreclipse.debug.core.AVRGDBDebugger";

	// Debugger
	public final static String	DEFAULT_COMMAND_NAME			= "avr-gdb";
	public final static String	DEFAULT_DEBUGGER_PROTOCOL		= "mi";
	public final static String	DEFAULT_COMMAND_FACTORY			= "Standard";

	// TODO: change this to false for a non-beta release
	public final static boolean	DEFAULT_VERBOSE_MODE			= true;

	public static final boolean	DEFAULT_USE_REMOTE_GDBSERVER	= false;
	public static final String	DEFAULT_IP_ADDRESS				= "localhost";
	public static final int		DEFAULT_PORT_NUMBER				= 4242;

	public static final String	ATTR_USE_REMOTE_GDBSERVER		= Activator.PLUGIN_ID
																		+ ".useRemoteGDBSerer";

	public static final String	ATTR_IP_ADDRESS					= Activator.PLUGIN_ID
																		+ ".ipAddress";

	public static final String	ATTR_PORT_NUMBER				= Activator.PLUGIN_ID
																		+ ".portNumber";

}
