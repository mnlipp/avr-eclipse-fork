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

package de.innot.avreclipse.core.paths.win32;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.SystemPathHelper;

/**
 * Gets the actual system paths to the winAVR and AVR Tools applications.
 * <p>
 * Unlike the Posix variant of this class, which actually looks through the (almost) complete
 * filesystem, this class will retrieve the paths from the Windows registry. But even this has a bit
 * of overhead, so the {@link SystemPathHelper}, which uses this class, should cache the results.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public class SystemPathsWin32 {
	private static IPath		fWinAVRPath		= null;
	private static IPath		fAVRToolsPath	= null;

	private final static IPath	fEmptyPath		= new Path("");

	private SystemPathsWin32() {
		// prevent instantiation
	}

	/**
	 * Find the system path for the given {@link AVRPath} enum value.
	 * 
	 * @param avrpath
	 * @return a valid path or <code>null</code> if no path could be found.
	 */
	public static IPath getSystemPath(AVRPath avrpath) {

		switch (avrpath) {
			case AVRGCC:
				return getWinAVRPath("bin");
			case AVRINCLUDE:
				return getWinAVRPath("avr/include");
			case AVRDUDE:
				return getWinAVRPath("bin");
			case MAKE:
				return getWinAVRPath("utils/bin");
			case PDFPATH:
				IPath basepath = getAVRToolsPath();
				if (basepath.isEmpty()) {
					return basepath;
				}
				return basepath.append("Partdescriptionfiles");
			default:
				// If we end up here the AVRPath Enum has new entries not yet covered.
				// Log this as an internal error and ignore otherwise
				IStatus status = new Status(
						IStatus.WARNING,
						AVRPlugin.PLUGIN_ID,
						"Internal problem! AVRPath with value ["
								+ avrpath.toString()
								+ "] is not covered. Please report to the AVR Eclipse plugin maintainer.",
						null);
				AVRPlugin.getDefault().log(status);
				return null;
		}
	}

	private static IPath getWinAVRPath(String append) {
		IPath basepath = getWinAVRBasePath();
		if (basepath.isEmpty()) {
			return basepath;
		}
		return basepath.append(append);
	}

	/**
	 * Get the path to the winAVR base directory from the Windows registry.
	 * 
	 * @return IPath with the current path to the winAVR base directory
	 */
	private static IPath getWinAVRBasePath() {
		if (fWinAVRPath != null) {
			return fWinAVRPath;
		}
		
		MyWindowsRegistry registry = MyWindowsRegistry.getRegistry();
		if (registry == null) {
			// Fix for Bug 2872447
			// can't access the registry. Fail gracefully by returning an empty String.
			// This will cause errors pointing the user to set the paths manually.
			fWinAVRPath = fEmptyPath;
			return fEmptyPath;
		}

		// get the newest installed version of winAVR.
		// There may be multiple versions of winAVR installed.
		// Grab all versions and sort them alphabetically go find the
		// most recent version
		int i = 0;
		List<String> winavrkeys = new ArrayList<String>();
		String nextkey = null;
		do {
			nextkey = WindowsRegistry.getRegistry().getLocalMachineValueName("SOFTWARE\\WinAVR", i);
			if (nextkey != null) {
				winavrkeys.add(nextkey);
				i++;
			}
		} while (nextkey != null);

		if (winavrkeys.size() > 0) {
			Collections.sort(winavrkeys);
			String winavrkey = winavrkeys.get(winavrkeys.size() - 1);

			String winavr = WindowsRegistry.getRegistry().getLocalMachineValue("SOFTWARE\\WinAVR",
					winavrkey);
			if (winavr != null) {
				fWinAVRPath = new Path(winavr);
				return fWinAVRPath;
			}
		}

		// No "HKML\Software\WinAVR" key in the registry
		// Lets try another location: "HKLM\Software\Free Software Foundation\WinAVR-xxxxx"
		// 

		i = 0;
		do {
			nextkey = WindowsRegistry.getRegistry().getLocalMachineKeyName(
					"SOFTWARE\\Free Software Foundation", i);
			if (nextkey != null && nextkey.startsWith("WinAVR-")) {
				winavrkeys.add(nextkey);
				i++;
			}
		} while (nextkey != null);

		if (winavrkeys.size() > 0 ) {
			Collections.sort(winavrkeys);
			String winavrkey = winavrkeys.get(winavrkeys.size() - 1);
			
			String winavr = WindowsRegistry.getRegistry().getLocalMachineValue(
					"SOFTWARE\\Free Software Foundation\\" + winavrkey, "GCC");
			if (winavr != null) {
				fWinAVRPath = new Path(winavr);
				return fWinAVRPath;
			}
		}

		// Couldn't find anything, so just return an empty path.
		// This will cause errors pointing the user to set the paths manually.
		fWinAVRPath = fEmptyPath;
		return fWinAVRPath;
	}

	/**
	 * Get the path to the Atmel AVR Tools base directory from the Windows registry.
	 * 
	 * @return IPath with the current path to the AVR Tools base directory
	 */
	private static IPath getAVRToolsPath() {

		if (fAVRToolsPath != null) {
			return fAVRToolsPath;
		}

		fAVRToolsPath = fEmptyPath;
		String avrtools = WindowsRegistry.getRegistry().getLocalMachineValue(
				"SOFTWARE\\Atmel\\AVRTools", "AVRToolsPath");
		if (avrtools != null) {
			fAVRToolsPath = new Path(avrtools);
		}
		return fAVRToolsPath;
	}

}
