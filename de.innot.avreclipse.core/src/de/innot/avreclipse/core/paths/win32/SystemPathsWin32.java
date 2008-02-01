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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;

import de.innot.avreclipse.core.paths.AVRPath;

/**
 * Gets the actual system paths to the winAVR and AVR Tools applications.
 * 
 * The paths are taken from the Windows registry. As the values are fairly
 * static they are cached to avoid expensive registry lookups.
 * 
 * The cache can be cleared with the {@link #clear()} method
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public class SystemPathsWin32 {

	private static SystemPathsWin32 fInstance = null;

	private static ILock lock = Job.getJobManager().newLock();

	private Map<AVRPath, IPath> fPathCache = null;
	private IPath fWinAVRPath = null;
	private IPath fAVRToolsPath = null;

	private final static IPath fEmptyPath = new Path("");

	public static SystemPathsWin32 getDefault() {
		if (fInstance == null) {
			fInstance = new SystemPathsWin32();
		}
		return fInstance;
	}

	private SystemPathsWin32() {
		// prevent instantiation
	}

	public void clearCache() {

		try {
			lock.acquire();
			if (fPathCache != null) {
				fPathCache.clear();
			}
			fWinAVRPath = null;
			fAVRToolsPath = null;
		} finally {
			lock.release();
		}
	}

	public IPath getSystemPath(AVRPath avrpath) {
		IPath path = null;

		// This method may be called from different threads. To prevent
		// an undefined cache this method locks itself while it
		// is looking for the path

		try {
			lock.acquire();
			if (fPathCache == null) {
				fPathCache = new HashMap<AVRPath, IPath>(AVRPath.values().length);
			}

			// Test if it is already in the cache
			path = fPathCache.get(avrpath);
			if (path != null) {
				return path;
			}

			// not in cache, then try to find the path
			path = internalGetPath(avrpath);
			fPathCache.put(avrpath, path);
		} finally {
			lock.release();
		}
		return path;
	}

	private IPath internalGetPath(AVRPath avrpath) {

		switch (avrpath) {
		case AVRGCC:
			return getWinAVRPath("bin");
		case AVRINCLUDE:
			return getWinAVRPath("avr/include");
		case AVRDUDE:
			return getWinAVRPath("bin");
		case AVRDUDECONFIG:
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
			// TODO: log something
			return null;
		}
	}

	private IPath getWinAVRPath(String append) {
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
	private IPath getWinAVRBasePath() {
		if (fWinAVRPath != null) {
			return fWinAVRPath;
		}
		fWinAVRPath = fEmptyPath;
		String winavrkeyname = WindowsRegistry.getRegistry().getLocalMachineValueName(
		        "SOFTWARE\\WinAVR", 0);
		if (winavrkeyname != null) {
			String winavr = WindowsRegistry.getRegistry().getLocalMachineValue("SOFTWARE\\WinAVR",
			        winavrkeyname);
			if (winavr != null) {
				fWinAVRPath = new Path(winavr);
			}
		}

		return fWinAVRPath;
	}

	/**
	 * Get the path to the Atmel AVR Tools base directory from the Windows
	 * registry.
	 * 
	 * @return IPath with the current path to the AVR Tools base directory
	 */
	private IPath getAVRToolsPath() {

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
