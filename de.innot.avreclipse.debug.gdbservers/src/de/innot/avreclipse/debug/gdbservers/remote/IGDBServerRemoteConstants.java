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
package de.innot.avreclipse.debug.gdbservers.remote;

import de.innot.avreclipse.debug.core.AVRDebugPlugin;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface IGDBServerRemoteConstants {

	// Get a reference to the core debug plugin id for quick reference
	public final static String	PLUGIN_ID							= AVRDebugPlugin.PLUGIN_ID;

	// ///////////////////////////////////////////////////////////////
	//
	// The constants for a remote gdbserver
	//
	// ///////////////////////////////////////////////////////////////

	/** The hostname for the remote gdbserver. Default: "localhost". */
	public static final String	ATTR_GDBSERVER_REMOTE_HOSTNAME		= PLUGIN_ID
																			+ ".remote.hostname";
	public static final String	DEFAULT_GDBSERVER_REMOTE_HOSTNAME	= "localhost";

	/** The port for the remote gdbserver. Default: 4242 */
	public static final String	ATTR_GDBSERVER_REMOTE_PORT			= PLUGIN_ID + ".remote.port";
	public static final int		DEFAULT_GDBSERVER_REMOTE_PORT		= 4242;

}
