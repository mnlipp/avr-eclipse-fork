/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.core.targets;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface ITargetConfigConstants {

	public final static String	CONFIG_SAVED			= "save";

	public final static String	ATTR_NAME				= "name";
	public final static String	DEF_NAME				= "New target";

	public final static String	ATTR_DESCRIPTION		= "description";
	public final static String	DEF_DESCRIPTION			= "";

	public final static String	ATTR_MCU				= "mcu";
	public final static String	DEF_MCU					= "atmega16";

	public final static String	ATTR_FCPU				= "fcpu";
	public final static int		DEF_FCPU				= 1000000;

	public final static String	ATTR_PROGRAMMER_ID		= "programmer";
	public final static String	DEF_PROGRAMMER_ID		= "stk500v2";

	public final static String	ATTR_HOSTINTERFACE		= "hostinterface";
	public final static String	DEF_HOSTINTERFACE		= "SERIAL";

	public final static String	ATTR_PROGRAMMER_PORT	= "port";
	public final static String	DEF_PROGRAMMER_PORT		= "";

	public final static String	ATTR_PROGRAMMER_BAUD	= "baud";
	public final static String	DEF_PROGRAMMER_BAUD		= "";

	public final static String	ATTR_BITBANGDELAY		= "bitbangdelay";
	public final static String	DEF_BITBANGDELAY		= "";

	public final static String	ATTR_PAR_EXITSPEC		= "exitspec";
	public final static String	DEF_PAR_EXITSPEC		= "";

	public final static String	ATTR_USB_DELAY			= "usbdelay";
	public final static String	DEF_USB_DELAY			= "";

	public final static String	ATTR_JTAG_CLOCK			= "jtagclock";
	public final static String	DEF_JTAG_CLOCK			= "";

	public final static String	ATTR_JTAG_DAISYCHAIN	= "jtagdaisychain";
	public final static String	DEF_JTAG_DAISYCHAIN		= "";

	public final static String	ATTR_IMAGE_LOADER_ID	= "imageloader";

	public final static String	ATTR_GDBSERVER_ID		= "gdbserver";

}
