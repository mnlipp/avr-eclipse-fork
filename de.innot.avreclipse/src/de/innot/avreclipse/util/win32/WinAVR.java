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
 * $Id: WinAVR.java 9 2007-11-25 21:51:59Z thomas $
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
public class WinAVR {

	private static IPath winavrpath = null;
	private static IPath winavrincludepath = null;
	private static IPath winavr_io_h = null;
	
	public static IPath getAVR_io_h() {
		if (winavr_io_h == null) {
			IPath avrinclude = getAVRIncludePath();
			if (avrinclude != null) {
				winavr_io_h = avrinclude.append("avr").append("io.h");
			}
		}
		return winavr_io_h;
	}
	
	public static IPath getAVRIncludePath() {

		if (winavrincludepath == null) {
			IPath winavr = getWinAVRPath();
			if (winavr != null) {
				winavrincludepath = winavr.append("avr").append("include");
			}
		}
		return winavrincludepath;
	}

	public static IPath getWinAVRPath() {

		if (winavrpath == null) {
			String winavrkeyname = WindowsRegistry.getRegistry().getLocalMachineValueName(
			        "SOFTWARE\\WinAVR", 0);
			if (winavrkeyname != null) {
				String winavr = WindowsRegistry.getRegistry().getLocalMachineValue(
				        "SOFTWARE\\WinAVR", winavrkeyname);
				if (winavr != null) {
					winavrpath = new Path(winavr);

				}
			}
		}
		return winavrpath;
	}
}
