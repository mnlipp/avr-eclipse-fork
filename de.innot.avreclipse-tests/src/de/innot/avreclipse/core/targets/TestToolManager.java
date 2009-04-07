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

package de.innot.avreclipse.core.targets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.core.targets.tools.AvariceTool;
import de.innot.avreclipse.core.targets.tools.AvrdudeTool;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TestToolManager {

	private ToolManager	manager;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = ToolManager.getDefault();
		assertNotNull("No toolmanager", manager);
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.ToolManager#getExtensionPointIDs()}.
	 */
	@Test
	public void testGetExtensionPointIDs() {
		String[] ids = manager.getExtensionPointIDs();
		assertNotNull(ids);
		assertEquals(1, ids.length);
		assertEquals(ToolManager.EXTENSIONPOINT, ids[0]);
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.ToolManager#getProgrammerTools()}.
	 */
	@Test
	public void testGetProgrammerTools() {
		IProgrammerTool[] alltools = manager.getProgrammerTools();

		// Currently there are two programmer tools build into the plugin:
		// - avrdude and
		// - avarice
		assertTrue(alltools.length >= 2);
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.ToolManager#getGDBServerTools()}.
	 */
	@Test
	public void testGetGDBServerTools() {
		IGDBServerTool[] alltools = manager.getGDBServerTools();

		// Currently there is only one gdbserver tool build into the plugin:
		// - avarice
		assertTrue(alltools.length >= 1);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.ToolManager#getProgrammerTool(java.lang.String)}.
	 */
	@Test
	public void testGetProgrammerTool() {
		IProgrammerTool avarice = manager.getProgrammerTool(AvariceTool.ID);
		assertNotNull("Could not load avarice extension point", avarice);

		IProgrammerTool avrdude = manager.getProgrammerTool(AvrdudeTool.ID);
		assertNotNull("Could not load avrdude extension point", avrdude);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.ToolManager#getGDBServerTool(java.lang.String)}.
	 */
	@Test
	public void testGetGDBServerTool() {
		IGDBServerTool avarice = manager.getGDBServerTool(AvariceTool.ID);
		assertNotNull("Could not load avarice extension point", avarice);

	}

}
