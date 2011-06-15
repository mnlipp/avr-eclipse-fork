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
package de.innot.avreclipse.core.paths.win32;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.IPath;
import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.core.paths.AVRPath;

/**
 * @author Thomas Holland
 * @since 
 *
 */
public class SystemPathsWin32Test {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.paths.win32.SystemPathsWin32#getSystemPath(de.innot.avreclipse.core.paths.AVRPath)}.
	 */
	@Test
	public void testGetSystemPath() {
		
		// This test assumes that all paths exist (WinAVR and Atmel Tools installed)
		for (AVRPath path : AVRPath.values()) {
			IPath pathvalue = SystemPathsWin32.getSystemPath(path);
			assertNotNull(pathvalue);
		}
	}


}
