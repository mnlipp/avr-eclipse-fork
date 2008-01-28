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

/**
 * Interface to get the current path from the preference store.
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public interface IPathProvider {

	/**
	 * Gets the currently active path.
	 * 
	 * @return <code>IPath</code> to the active source directory
	 */
	public IPath getPath();

	
}
