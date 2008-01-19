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
 * $Id: PreferenceInitializer.java 9 2007-11-25 21:51:59Z thomas $
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui.preferences;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

import de.innot.avreclipse.AVRPluginActivator;
import de.innot.avreclipse.core.preferences.AVRTargetProperties;
import de.innot.avreclipse.util.win32.AVRTools;
import de.innot.avreclipse.util.win32.WinAVR;

/**
 * Class used to initialize default preference values.
 * 
 * <p>
 * This class is called directly from the plugin.xml (in the
 * <code>org.eclipse.core.runtime.preferences</code
 * extension point. It sets default values for the Plugin preferences.
 * </p> 
 * @author Thomas Holland
 * @version 1.1
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer implements
		PreferenceConstants {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = AVRPluginActivator.getDefault().getPreferenceStore();

		// Default source for the AVR Device Viewer is <avr/io.h>, as it is
		// available
		// on all supported platforms
		store.setDefault(PREF_DEVICEVIEW_CONTENTSOURCE, PREFVALUE_DEVICEVIEW_SOURCE_AVRIO);

		// Path to PartDefinitionFiles folder
		store.setDefault(PREF_DEVICEVIEW_AVRPDF_PATH, getAVRStudioPath());

		// Path to <avr/io.h> file
		store.setDefault(PREF_DEVICEVIEW_AVR_IO_H, getAVR_io_h());
		
		// Path to the directory containing the avr-gcc executable
		store.setDefault(PREF_AVRGCCPATH, getAVRgccPath());
		
		// Path to the directory containing the make executable
		store.setDefault(PREF_AVRMAKEPATH, getAVRmakePath());
		
		// Store default values to default preferences
	 	AVRTargetProperties.initializeDefaultPreferences();
	}

	/**
	 * Get the path to the PartDefinitionFiles.
	 * 
	 * <p>
	 * The PartDefinitionFiles are part of the AVR Tools (which comes with the
	 * AVR Studio distribution). On Windows it will get the path from the
	 * Windows registry. On non-windows systems this folder has to be copied
	 * from a windows machine, so it can be anywhere. Therefore we default to
	 * root and let the user set the location.
	 * </p>
	 * 
	 * @return String containing the path in OS form
	 * 
	 * @see de.innot.avreclipse.util.win32.AVRTools
	 */
	public static String getAVRStudioPath() {
		if (isWindows()) {
			IPath pdfpath = AVRTools.getPartDescriptionFilesPath();
			if (pdfpath != null && pdfpath.toFile().isDirectory()) {
				return pdfpath.toOSString();
			}
		}
		return new Path("/").toOSString();
	}

	/**
	 * Get the path to <avr/io.h>.
	 * 
	 * <p>
	 * On windows this file is located thru the windows registry (via the winAVR
	 * settings). On non-Windows systems currently
	 * <code>/usr/avr/include/avr/io.h</code> is used as a default (works on
	 * ubuntu), but some more elaborate code checking multiple locations should
	 * be implemented.
	 * </p>
	 * 
	 * @return String containing the path in OS form
	 * 
	 * @see de.innot.avreclipse.util.win32.WinAVR
	 */
	public static String getAVR_io_h() {
		if (isWindows()) {
			IPath io_h = WinAVR.getAVR_io_h();
			if (io_h != null && io_h.toFile().canRead()) {
				return io_h.toOSString();
			}
			// file did not exist: return root
			return new Path("/").toOSString();
		}
		// Non-windows: test a few common locations
		File test = new File("/usr/avr/include/avr/io.h");
		if (test.isFile()) {
			return test.getPath();
		}
		test = new File("/usr/local/avr/include/avr/io.h");
		if (test.isFile()) {
			return test.getPath();
		}
		
		// Default: root - let the user search for it
		return new Path("/").toOSString();
	}

	/**
	 * Get the path to the directory containing avr-gcc.
	 * 
	 * <p>
	 * On windows this file is located thru the windows registry (via the winAVR
	 * settings). On non-Windows systems currently
	 * <code>/usr/bin</code> is used as a default (works on
	 * ubuntu), but some more elaborate code checking multiple locations should
	 * be implemented.
	 * </p>
	 * 
	 * @return String containing the path in OS form
	 * 
	 * @see de.innot.avreclipse.util.win32.WinAVR
	 */
	public static String getAVRgccPath() {
		if (isWindows()) {
			IPath avrgcc = WinAVR.getWinAVRPath().append("bin");
			if (avrgcc != null && avrgcc.toFile().isDirectory()) {
				return avrgcc.toOSString();
			}
			// file did not exist: return root
			return new Path("/").toOSString();
		}
		
		// Non-windows: test a few common locations
		File test = new File("/usr/local/bin/avr-gcc");
		if (test.isFile()) {	// .canExecute() is nicer, but only available on JRE 6.0
			return "/usr/local/bin/";
		}
		test = new File("/usr/bin/avr-gcc");
		if (test.isFile()) {
			return "/usr/bin/";
		}
		// Default: root - let the user search for it
		return new Path("/").toOSString();

	}

	/**
	 * Get the path to the directory containing the make executable.
	 * 
	 * <p>
	 * On windows this file is located thru the windows registry (via the winAVR
	 * settings). On non-Windows systems currently
	 * <code>/usr/bin</code> is used as a default (works on
	 * ubuntu), but some more elaborate code checking multiple locations should
	 * be implemented.
	 * </p>
	 * 
	 * @return String containing the path in OS form
	 * 
	 * @see de.innot.avreclipse.util.win32.WinAVR
	 */
	public static String getAVRmakePath() {
		if (isWindows()) {
			IPath avrgcc = WinAVR.getWinAVRPath().append("utils/bin");
			if (avrgcc != null && avrgcc.toFile().isDirectory()) {
				return avrgcc.toOSString();
			}
			// file did not exist: return root
			return new Path("/").toOSString();
		}
		// Non-windows: test a few common locations
		File test = new File("/usr/local/bin/make");
		if (test.isFile()) {
			return "/usr/local/bin/";
		}
		test = new File("/usr/bin/make");
		if (test.isFile()) {
			return "/usr/bin/";
		}
		
		// Default: root - let the user search for it
		return new Path("/").toOSString();

	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}

}
