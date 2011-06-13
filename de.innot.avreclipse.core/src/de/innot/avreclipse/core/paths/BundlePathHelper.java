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
import org.eclipse.core.runtime.Path;

/**
 * Convenience class to get the path for a given resource from a Eclipse bundle.
 * 
 * @author Thomas Holland
 * @since 2.1
 */
final class BundlePathHelper {

	/**
	 * @param path
	 *            AVRPath for the path
	 * @param bundeid
	 *            Id of the Bundle from which to get the path
	 * @return IPath with the path
	 */
	public static IPath getPath(AVRPath path, String bundeid) {

		// TODO: not implemented yet
		return new Path("");
	}

}
