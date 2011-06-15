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
import org.junit.Test;

public class SystemPathHelperTest {

	@Test
	public void testGetPath() {
		AVRPath[] allpaths = AVRPath.values();

		for (AVRPath avrpath : allpaths) {
			IPath path = SystemPathHelper.getPath(avrpath, false);
			assertNotNull(avrpath.getName() + "returned null path", path);
			if (!avrpath.isOptional()) {
				assertFalse(avrpath.getName() + " has empty path", path.isEmpty());
			}
		}
	}
}
