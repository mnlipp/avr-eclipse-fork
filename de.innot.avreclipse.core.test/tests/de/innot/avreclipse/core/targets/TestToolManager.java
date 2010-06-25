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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.core.targets.tools.AvariceTool;
import de.innot.avreclipse.core.targets.tools.AvrdudeTool;
import de.innot.avreclipse.core.targets.tools.NoneToolFactory;

/**
 * @author Thomas Holland
 * @since 2.4
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
	 * Test method for {@link ToolManager#getExtensionPointIDs()}.
	 */
	@Test
	public void testGetExtensionPointIDs() {
		String[] ids = manager.getExtensionPointIDs();
		assertNotNull(ids);
		assertEquals(1, ids.length);
		assertEquals(ToolManager.EXTENSIONPOINT, ids[0]);
	}

	/**
	 * Test method for {@link ToolManager#getToolName(String)}.
	 */
	@Test
	public void testGetToolName() {

		assertEquals(AvariceTool.NAME, manager.getToolName(AvariceTool.ID));
		assertEquals(AvrdudeTool.NAME, manager.getToolName(AvrdudeTool.ID));
		assertEquals(NoneToolFactory.NAME, manager.getToolName(NoneToolFactory.ID));

		// Test invalid id
		assertEquals(null, manager.getToolName("foobar"));
		assertEquals(null, manager.getToolName(""));
		assertEquals(null, manager.getToolName(null));
	}

	/**
	 * Test method for {@link ToolManager#getTool(String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetTool() throws IOException {

		ITargetConfiguration hc = TargetConfigurationManager.getDefault().createNewConfig();

		ITargetConfigurationTool avarice = manager.getTool(hc, AvariceTool.ID);
		assertNotNull("Could not load avarice extension point", avarice);

		ITargetConfigurationTool avrdude = manager.getTool(hc, AvrdudeTool.ID);
		assertNotNull("Could not load avrdude extension point", avrdude);

		ITargetConfigurationTool none = manager.getTool(hc, NoneToolFactory.ID);
		assertNotNull("Could not load NoneTool extension point", none);
	}

	/**
	 * Test method for {@link ToolManager#getAllTools(String)}.
	 */
	@Test
	public void testGetAllTools() {
		// Test unfiltered
		List<String> alltools = manager.getAllTools(null);
		assertNotNull(alltools);

		// Currently there are three tools build into the plugin:
		// - avrdude,
		// - avarice, and
		// - none
		assertTrue(alltools.contains(AvrdudeTool.ID));
		assertTrue(alltools.contains(AvariceTool.ID));
		assertTrue(alltools.contains(NoneToolFactory.ID));

		// Test filtered
		alltools = manager.getAllTools(ToolManager.AVRPROGRAMMERTOOL);
		assertTrue(alltools.contains(AvrdudeTool.ID));
		assertTrue(alltools.contains(AvariceTool.ID));
		assertTrue(alltools.contains(NoneToolFactory.ID));

		alltools = manager.getAllTools(ToolManager.AVRGDBSERVER);
		assertFalse(alltools.contains(AvrdudeTool.ID));
		assertTrue(alltools.contains(AvariceTool.ID));
		assertTrue(alltools.contains(NoneToolFactory.ID));

		// only the 'none-tool' matches *all* tool types
		alltools = manager.getAllTools("foobar");
		assertNotNull(alltools);
		assertEquals(1, alltools.size());
		assertTrue(alltools.contains(NoneToolFactory.ID));
	}

	/**
	 * Test Method for {@link ToolManager#setLastAccess(String, long)} and
	 * {@link ToolManager#getLastAccess(String)}
	 */
	@Test
	public void testAccessTimes() {

		final String testport = "/foo/bar";
		final long testvalue = 123456L;

		assertEquals(0L, manager.getLastAccess(testport));

		manager.setLastAccess(testport, testvalue);

		assertEquals(testvalue, manager.getLastAccess(testport));

	}
}
