/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

/**
 * @author Thomas Holland
 * @since 2.3.2
 *
 */
public class TestCorePlugin {

	/**
	 * Test method for {@link de.innot.avreclipse.AVRPlugin#getDefault()}.
	 */
	@Test
	public void testGetDefault() {
		AVRPlugin plugin = AVRPlugin.getDefault();
		assertNotNull(plugin);
	}
	
	

}
