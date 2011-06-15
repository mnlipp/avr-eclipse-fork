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
package de.innot.avreclipse;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author Thomas Holland
 * @since 2.3.2
 *
 */
public class CorePluginTest {

	/**
	 * Test method for {@link de.innot.avreclipse.AVRPlugin#getDefault()}.
	 */
	@Test
	public void testGetDefault() {
		AVRPlugin plugin = AVRPlugin.getDefault();
		assertNotNull(plugin);
	}
	
	

}
