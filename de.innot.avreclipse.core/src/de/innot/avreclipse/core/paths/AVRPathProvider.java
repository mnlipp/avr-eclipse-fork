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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.preference.IPreferenceStore;

import de.innot.avreclipse.core.preferences.AVRPathsPreferences;

public class AVRPathProvider implements IPathProvider {

	private IPreferenceStore fPrefs;
	private AVRPath fAvrPath;

	/**
	 * Creates a PathProvider for the instance Preference Store and AVRPath.
	 * 
	 */
	public AVRPathProvider(AVRPath avrpath) {
		this(AVRPathsPreferences.getPreferenceStore(), avrpath);
	}

	/**
	 * Creates a PathProvider for the given Preference Store and AVRPath.
	 * 
	 */
	public AVRPathProvider(IPreferenceStore store, AVRPath avrpath) {
		fPrefs = store;
		fAvrPath = avrpath;
	}

	public String getName() {
		return fAvrPath.toString();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.paths.IPathProvider#getPath()
	 */
	public IPath getPath() {
		// get the path from the preferences store and returns its value,
		// depending on the selected path source

		String pathvalue = fPrefs.getString(fAvrPath.name());

		if (pathvalue.equals(IPathManager.SourceType.System.name())) {
			// System path
			return SystemPathHelper.getPath(fAvrPath);
		}

		if (pathvalue.startsWith(IPathManager.SourceType.Bundled.name())) {
			// Bundle path
			String bundleid = pathvalue.substring(pathvalue.indexOf(':') + 1);
			return BundlePathHelper.getPath(fAvrPath, bundleid);
		}
		// else: a custom path
		IPath path = new Path(pathvalue);
		return path;
	}

}
