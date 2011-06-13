/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
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
