/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de)
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 *******************************************************************************/
package de.innot.avreclipse;

/**
 * Definitions of id values used in the plugin.xml
 * 
 * Some id's from the plugin.xml are used quite frequently to programmatically access some parts of the toolchain.
 * They are defined here in one central place to aid refactoring of the plugin.xml
 * 
 * @author Thomas Holland
 * @since 1.0
 *
 */
public interface PluginIDs {

	/** 
	 * ID of the base toolchain, all other toolchains are derived from this.
	 * Value: {@value}
	 */
	public static String PLUGIN_BASE_TOOLCHAIN = "de.innot.avreclipse.toolchain.winavr.base";
	
	/** ID of the mcu type option of the base toolchain. Value: {@value} */
	public static String PLUGIN_TOOLCHAIN_OPTION_MCU = "de.innot.avreclipse.toolchain.options.target.mcutype";
	
	/** ID of the cpu frequency option of the base toolchain. Value: {@value} */
	public static String PLUGIN_TOOLCHAIN_OPTION_FCPU = "de.innot.avreclipse.toolchain.options.target.fcpu";
}
