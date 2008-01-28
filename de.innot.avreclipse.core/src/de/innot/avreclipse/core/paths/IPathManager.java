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
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * This Interface is used to modify plugin paths.
 * 
 * @author Thomas Holland
 * @since 2.1
 * 
 */
public interface IPathManager {

	public enum SourceType {
		Bundled, System, Custom
	}

	/**
	 * Gets the current path.
	 * 
	 * This is different from IPathProvider.getPath() because the returned path
	 * is cached internally and can be modified with the setPath() method.
	 * 
	 * 
	 * @return <code>IPath</code>
	 */
	public IPath getPath();

	/**
	 * Gets the UI name of the underlying AVRPath.
	 * 
	 * @return String with the name
	 */
	public String getName();

	/**
	 * Gets a description from the underlying AVRPath.
	 * 
	 * @return String with the description of the path
	 */
	public String getDescription();

	/**
	 * Gets the source of this path.
	 * 
	 * This can be one of the {@link SourceType} values
	 * <ul>
	 * <li><code>Bundled</code> if the path points to a bundled avr-gcc
	 * toolchain.</li>
	 * <li><code>System</code> if the system default path is used.</li>
	 * <li><code>Custom</code> if the path is selected by the user.</li>
	 * </ul>
	 * 
	 * @return
	 */
	public SourceType getSourceType();

	/**
	 * Sets the PreferenceStore the PathManager should work on.
	 * 
	 * By default the PathManager will work on the Instance Preference store.
	 * 
	 * @param store
	 */
	public void setPreferenceStore(IPreferenceStore store);

	/**
	 * Gets the default path.
	 * 
	 * @return <code>IPath</code> to the default source directory
	 */
	public IPath getDefaultPath();

	/**
	 * Gets the system path.
	 * 
	 * This is the path as determined by system path / windows registry.
	 * 
	 * @return <code>IPath</code> to the system dependent source directory
	 */
	public IPath getSystemPath();

	/**
	 * Gets the path from the Eclipse bundle with the given id.
	 * 
	 * @param bundleid
	 *            ID of the source bundle
	 * @return <code>IPath</code> to the source directory within the bundle.
	 */
	public IPath getBundlePath(String bundleid);

	/**
	 * Sets the path in the preference store.
	 * 
	 * @param newpath
	 * @param context
	 */
	public void setPath(String newpath, SourceType source);

	/**
	 * Sets the path back to the default value.
	 */
	public void setToDefault();

	/**
	 * Stores the path in the PreferenceStore.
	 * 
	 * Until <code>store()</code> is called, all modifications to the path are
	 * only internal to this IPathManager and not visible outside.
	 */
	public void store();

	/**
	 * Checks if the current path is valid.
	 * <p>
	 * Some paths are required, some are optional.
	 * </p>
	 * <p>
	 * For required paths this method returns <code>true</code> if a
	 * internally defined testfile exists in the given path.
	 * </p>
	 * <p>
	 * For optional paths this method also returns true if - and only if - the
	 * path is empty ("").
	 * </p>
	 * 
	 * @return <code>true</code> if the path points to a valid source folder.
	 */
	public boolean isValid();

	/**
	 * Returns a clone of this object
	 * 
	 * @return New IPathManager object with the same internal state as this
	 *         object.
	 */
	public IPathManager clone();

}
