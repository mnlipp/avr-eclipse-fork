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
package de.innot.avreclipse.mbs;

/**
 * Default names and values for the AVR Eclipse Plugin.
 * 
 * <p>Currently two names with their corresponding toolchain option id's are 
 * defined. One for the Target MCU type and one for the Target MCU
 * Clock Frequency.</p>
 * 
 * <p>They are used as
 * <ul>
 * 	<li>name for the <code>valueHandlerExtraArgument</code> attribute of the corresponding
 * 		option in the plugin.xml </li>
 *  <li>name of the generated <code>BuildMacro</code></li>
 *  <li>name of the generated <code>Configuration</code> environment variable</li>
 * </ul>
 * 
 * @author Thomas Holland
 * @version 1.0
 */
public interface BuildConstants {

	/** Name of the extraArgument / buildMacro / environment variable. Set to {@value} */
	public static String TARGET_MCU_NAME = "AVRTARGETMCU";

	/** Name of the extraArgument / buildMacro / Environment Variable. Set to {@value} */
	public static String TARGET_FCPU_NAME = "AVRTARGETFCPU";
}
