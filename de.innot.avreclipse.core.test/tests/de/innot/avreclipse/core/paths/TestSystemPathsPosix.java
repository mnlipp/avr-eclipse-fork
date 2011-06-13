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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;

import de.innot.avreclipse.core.paths.posix.SystemPathsPosix;

public class TestSystemPathsPosix {

	@Test
	public void testGetDefault() {

		if (isWindows())
			return;

		IPath path = SystemPathsPosix.getSystemPath(AVRPath.AVRGCC);
		assertFalse(path.isEmpty());
	}

	@Test
	public void testGetSystemPath() {

		if (isWindows())
			return;

		AVRPath[] allpaths = AVRPath.values();
		for (AVRPath avrpath : allpaths) {
			IPath path = SystemPathsPosix.getSystemPath(avrpath);
			assertNotNull(avrpath.getName(), path);
			if (!avrpath.isOptional()) {
				assertFalse(avrpath.getName(), path.isEmpty());
			}
		}
	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}

}
