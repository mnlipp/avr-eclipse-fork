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
 * $Id: AVRTools.java 9 2007-11-25 21:51:59Z thomas $
 *     
 *******************************************************************************/
/**
 * 
 */
package de.innot.avreclipse.util.win32;

import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

/**
 * @author Thomas Holland
 * 
 */
public class AVRTools {

	private static IPath avrtoolspath = null;
	private static IPath partdescriptionfilespath = null;

	public static IPath getPartDescriptionFilesPath() {

		if (partdescriptionfilespath == null) {
			IPath avrtools = getAVRToolsPath();
			if (avrtools != null) {
				partdescriptionfilespath = avrtools.append("Partdescriptionfiles");
			}
		}
		return partdescriptionfilespath;
	}

	public static IPath getAVRToolsPath() {

		if (avrtoolspath == null) {
			String avrtools = WindowsRegistry.getRegistry().getLocalMachineValue(
			        "SOFTWARE\\Atmel\\AVRTools", "AVRToolsPath");
			if (avrtools != null) {
				avrtoolspath = new Path(avrtools);
			}
		}
		return avrtoolspath;
	}
}
