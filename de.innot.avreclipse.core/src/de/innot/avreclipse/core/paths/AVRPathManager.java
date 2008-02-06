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
package de.innot.avreclipse.core.paths;

import java.io.File;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;

import de.innot.avreclipse.core.preferences.AVRPathsPreferences;

public class AVRPathManager implements IPathProvider, IPathManager {

	private IPreferenceStore fPrefs;
	private AVRPath fAvrPath;

	private String fPrefsValue = null;

	private static IPath fEmptyPath = new Path("");
	
	/**
	 * Creates a PathProvider for the instance Preference Store and AVRPath.
	 * 
	 */
	public AVRPathManager(AVRPath avrpath) {
		this(AVRPathsPreferences.getPreferenceStore(), avrpath);
	}

	/**
	 * Creates a PathProvider for the given Preference Store and AVRPath.
	 * 
	 */
	public AVRPathManager(IPreferenceStore store, AVRPath avrpath) {
		fPrefs = store;
		fAvrPath = avrpath;

	}

	public String getName() {
		return fAvrPath.toString();
	}
	
	public String getDescription() {
		return fAvrPath.getDescription();
	}
	
	/* (non-Javadoc)
	 * @see de.innot.avreclipse.core.paths.IPathProvider#getPath()
	 */
	public IPath getPath() {
		// get the path from the preferences store and returns its value,
		// depending on the selected path source

		if (fPrefsValue == null) {
			fPrefsValue = fPrefs.getString(fAvrPath.name());
		}

		if (fPrefsValue.equals(IPathManager.SourceType.System.name())) {
			// System path
			return getSystemPath();
		}

		if (fPrefsValue.startsWith(IPathManager.SourceType.Bundled.name())) {
			// Bundle path
			String bundleid = fPrefsValue.substring(fPrefsValue.indexOf(':') + 1);
			return getBundlePath(bundleid);
		}
		// else: a custom path
		IPath path = new Path(fPrefsValue);
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.paths.IPathManager#getPath(de.innot.avreclipse.core.paths.IPathManager.SourceType)
	 */
	public IPath getDefaultPath() {
		// Don't want to duplicate the parsing done in getPath() so
		// just set the current value to the default, call getPath and
		// restore the current value afterward.
		String defaultvalue = fPrefs.getDefaultString(fAvrPath.name());
		String oldPrefsValue = fPrefsValue;
		fPrefsValue = defaultvalue;
		IPath defaultpath = getPath();
		fPrefsValue = oldPrefsValue;
		return defaultpath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.paths.IPathManager#getSystemPath()
	 */
	public IPath getSystemPath() {
		return SystemPathHelper.getPath(fAvrPath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.paths.IPathManager#getBundlePath(java.lang.String)
	 */
	public IPath getBundlePath(String bundleid) {
		return BundlePathHelper.getPath(fAvrPath, bundleid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.paths.IPathManager#setPath(org.eclipse.core.runtime.IPath)
	 */
	public void setPath(String newpath, SourceType source) {
		String newvalue = null;
		switch (source) {
		case System:
			newvalue = source.name();
			break;
		case Bundled:
			newvalue = source.name() + ":" + newpath;
			break;
		case Custom:
			newvalue = newpath;
		}
		fPrefsValue = newvalue;
	}

	public void setToDefault() {
		fPrefsValue = fPrefs.getDefaultString(fAvrPath.name());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.paths.IPathManager#getSourceType()
	 */
	public IPathManager.SourceType getSourceType() {
		if (fPrefsValue == null) {
			// get the path source from the preferences store
			fPrefsValue = fPrefs.getString(fAvrPath.name());
		}
		if (fPrefsValue.equals(IPathManager.SourceType.System.name())) {
			return IPathManager.SourceType.System;
		}
		if (fPrefsValue.startsWith(IPathManager.SourceType.Bundled.name())) {
			return IPathManager.SourceType.Bundled;
		}
		// else: a custom path
		return IPathManager.SourceType.Custom;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.paths.IPathManager#isValid()
	 */
	public boolean isValid() {
		IPath path = getPath();
		// Test if the file is optional. If optional,
		// then an empty Path is also valid
		if (fAvrPath.isOptional()) {
			if (fEmptyPath.equals(path)) {
				return true;
			}
		}

		// Test if the testfile exists in the given folder
		IPath testpath = path.append(fAvrPath.getTest());
		File file = testpath.toFile();
		if (file.canRead()) {
			return true;
		}
		
		// try with ".exe" appended, as otherwise on Windows
		// file.canRead() will fail
		testpath = path.append(fAvrPath.getTest()+".exe");
		file = testpath.toFile();
		if (file.canRead()) {
			return true;
		}
		
		return false;
	}

	/* (non-Javadoc)
	 * @see de.innot.avreclipse.core.paths.IPathManager#setPreferenceStore(org.eclipse.jface.preference.IPreferenceStore)
	 */
	public void setPreferenceStore(IPreferenceStore store) {
		fPrefs = store;
	}

	/* (non-Javadoc)
	 * @see de.innot.avreclipse.core.paths.IPathManager#store()
	 */
	public void store() {
		if (fPrefsValue != null) {
			fPrefs.setValue(fAvrPath.name(), fPrefsValue);
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#clone()
	 */
	@Override
	public IPathManager clone() {
		AVRPathManager clone = new AVRPathManager(fAvrPath);
		clone.fPrefs = fPrefs;
		clone.fPrefsValue = fPrefsValue;
		
		return clone;
	}
}
