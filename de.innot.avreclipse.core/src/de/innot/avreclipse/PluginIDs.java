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
package de.innot.avreclipse;

/**
 * Definitions of id values used in the plugin.xml
 * 
 * Some id's from the plugin.xml are used quite frequently to programmatically access some parts of the toolchain.
 * They are defined here in one central place to aid refactoring of the plugin.xml
 * 
 * @author Thomas Holland
 * @since 2.0
 *
 */
public interface PluginIDs {

	/** 
	 * ID of the base toolchain, all other toolchains are derived from this.
	 * Value: {@value}
	 */
	public final static String PLUGIN_BASE_TOOLCHAIN = "de.innot.avreclipse.toolchain.winavr.base";
	
	/** ID of the mcu type option of the base toolchain. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_OPTION_MCU = "de.innot.avreclipse.toolchain.options.target.mcutype";
	
	/** ID of the cpu frequency option of the base toolchain. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_OPTION_FCPU = "de.innot.avreclipse.toolchain.options.target.fcpu";
	
	/** ID of the "generate Flash" toolchain option. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_OPTION_GENERATEFLASH = "de.innot.avreclipse.toolchain.options.toolchain.objcopy.flash";
	
	/** ID of the "generate EEPROM" toolchain option. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_OPTION_GENERATEEEPROM = "de.innot.avreclipse.toolchain.options.toolchain.objcopy.eeprom";
	
	/** ID of the "avrdude" toolchain option. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_OPTION_AVRDUDE = "de.innot.avreclipse.toolchain.options.toolchain.objcopy.eeprom";

	/** ID of the compiler tool. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_TOOL_COMPILER = "de.innot.avreclipse.tool.compiler.winavr";

	/** ID of the linker tool. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_TOOL_LINKER = "de.innot.avreclipse.tool.linker.winavr";

	/** ID of the flash objcopy tools. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_TOOL_FLASH = "de.innot.avreclipse.tool.objcopy.flash.winavr";

	/** ID of the eeprom objcopy tools. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_TOOL_EEPROM = "de.innot.avreclipse.tool.objcopy.eeprom.winavr";

	/** ID of the size tool. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_TOOL_SIZE = "de.innot.avreclipse.tool.size.winavr";

	/** ID of the size tool format option with avr. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_TOOL_SIZE_FORMATWITHAVR = "de.innot.avreclipse.size.option.formatwithavr";
	
	/** ID of the size tool format option without avr. Value: {@value} */
	public final static String PLUGIN_TOOLCHAIN_TOOL_SIZE_FORMAT = "de.innot.avreclipse.size.option.format";
	
	/** ID of the AVR Nature. Value: {@value} */
	public final static String NATURE_ID = "de.innot.avreclipse.core.avrnature";
}
