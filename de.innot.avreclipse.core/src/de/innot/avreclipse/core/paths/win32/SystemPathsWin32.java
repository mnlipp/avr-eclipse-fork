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

import org.eclipse.cdt.utils.WindowsRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

import de.innot.avreclipse.core.paths.IPathProvider;

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
public enum SystemPathsWin32 implements IPathProvider {

	AVRGCC {
		public IPath getPath() {
			if (fAVRGCCPath == null) {
				IPath basepath = getWinAVRBasePath();
				if (isEmptyPath(basepath)) {
					return basepath;
				}
				fAVRGCCPath = basepath.append("bin");
			}
			return fAVRGCCPath;
		}
	},

	MAKE {
		public IPath getPath() {
			if (fMakePath == null) {
				IPath basepath = getWinAVRBasePath();
				if (isEmptyPath(basepath)) {
					return basepath;
				}
				fMakePath = basepath.append("utils").append("bin");
			}
			return fMakePath;
		}
	},

	AVRINCLUDE {
		public IPath getPath() {
			if (fAVRIncludePath == null) {
				IPath basepath = getWinAVRBasePath();
				if (isEmptyPath(basepath)) {
					return basepath;
				}
				fAVRIncludePath = basepath.append("avr").append("include");
			}
			return fAVRIncludePath;

		}
	},

	AVRDUDE {
		public IPath getPath() {
			if (fAVRDUDEPath == null) {
				IPath basepath = getWinAVRBasePath();
				if (isEmptyPath(basepath)) {
					return basepath;
				}
				fAVRDUDEPath = basepath.append("bin");
			}
			return fAVRDUDEPath;
		}
	},

	AVRDUDECONFIG {
		public IPath getPath() {
			if (fAVRDUDEConfigPath == null) {
				IPath basepath = getWinAVRBasePath();
				if (isEmptyPath(basepath)) {
					return basepath;
				}
				fAVRDUDEConfigPath = basepath.append("bin");
			}
			return fAVRDUDEConfigPath;
		}
	},

	PDFPATH {
		public IPath getPath() {
			if (fPDFPath == null) {
				IPath basepath = getAVRToolsPath();
				if (isEmptyPath(basepath)) {
					return basepath;
				}
				fPDFPath = basepath.append("Partdescriptionfiles");
			}
			return fPDFPath;
		}
	};

	/* (non-Javadoc)
	 * @see de.innot.avreclipse.core.paths.IPathProvider#getPath()
	 */
	public abstract IPath getPath();

	// cached paths
	private static IPath fAVRGCCPath = null;
	private static IPath fAVRIncludePath = null;
	private static IPath fMakePath = null;
	private static IPath fAVRDUDEPath = null;
	private static IPath fAVRDUDEConfigPath = null;
	private static IPath fPDFPath = null;

	private static IPath fWinAVRPath = null;
	private static IPath fAVRToolsPath = null;

	private final static IPath fEmptyPath = new Path("");

	/**
	 * Clears the cached values.
	 */
	public void clear() {
		fAVRGCCPath = null;
		fAVRIncludePath = null;
		fMakePath = null;
		fAVRDUDEPath = null;
		fAVRDUDEConfigPath = null;
		fPDFPath = null;

		fWinAVRPath = null;
		fAVRToolsPath = null;
		
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

	private static boolean isEmptyPath(IPath path) {
		return fEmptyPath.equals(path);
	}

}
