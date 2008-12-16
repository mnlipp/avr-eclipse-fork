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

	// The common attributes for the gdbserver selection.
	// Other attributes for the gdbserver are handled by the specific extensions
	public static final String	DEFAULT_GDBSERVER_ID			= "de.innot.avreclipse.debug.gdbservers.avarice";
	public static final String	DEFAULT_GDBSERVER_IP_ADDRESS	= "localhost";
	public static final int		DEFAULT_GDBSERVER_PORT_NUMBER	= 4242;

	public static final String	ATTR_GDBSERVER_ID				= AVRDebugPlugin.PLUGIN_ID
																		+ ".gdbserverID";

	public static final String	ATTR_GDBSERVER_IP_ADDRESS		= AVRDebugPlugin.PLUGIN_ID
																		+ ".ipAddress";

	public static final String	ATTR_GDBSERVER_PORT_NUMBER		= AVRDebugPlugin.PLUGIN_ID
																		+ ".portNumber";

}
