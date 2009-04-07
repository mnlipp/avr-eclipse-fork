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

package de.innot.avreclipse.core.targets.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetConfigurationManager;
import de.innot.avreclipse.core.targets.ToolManager;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TestAvariceTool {

	private AvariceTool						avarice;

	private ITargetConfigurationWorkingCopy	config;

	@Before
	public void setup() throws IOException {
		avarice = (AvariceTool) ToolManager.getDefault().getProgrammerTool(AvariceTool.ID);
		ITargetConfiguration tc = TargetConfigurationManager.getDefault().createNewConfig();
		config = TargetConfigurationManager.getDefault().getWorkingCopy(tc.getId());
	}

	@After
	public void tearDown() throws IOException {
		// Remove the generated Hardware Configuration
		TargetConfigurationManager.getDefault().deleteConfig(config.getId());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getName()}.
	 */
	@Test
	public void testBasics() {
		assertEquals("AVaRICE", avarice.getName());
		assertEquals(AvariceTool.ID, avarice.getId());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getCommand(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetCommand() throws IOException {
		// first check if default is returned for an unmodified hardware config
		String cmd = avarice.getCommand(config);
		assertNotNull("Null command", cmd);
		assertTrue("Empty command", cmd.length() > 0);
		assertEquals("Wrong default command", "avarice", cmd);

		// Change the command attribute and check that it is propagated
		config.setAttribute(AvariceTool.ATTR_CMD_NAME, "foobar");
		assertEquals("Wrong command", "foobar", avarice.getCommand(config));
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getDefaults()}.
	 */
	@Test
	public void testGetDefaults() {
		// Get the list and check that it contains all known attributes
		Map<String, String> defaults = avarice.getDefaults();
		assertNotNull("Null defaults", defaults);
		assertTrue("Empty defaults", defaults.size() > 0);

		assertTrue("Command attr missing", defaults.containsKey(AvariceTool.ATTR_CMD_NAME));
		assertTrue("UseConsole attr missing", defaults.containsKey(AvariceTool.ATTR_USE_CONSOLE));

		// Add other attributes once they have been implemented
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getVersion(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 * 
	 * @throws IOException
	 * @throws AVRDudeException
	 */
	@Test
	public void testGetVersion() throws IOException, AVRDudeException {
		// Use the default avarice command (avarice must be on the system path)
		String version = avarice.getVersion(config);
		assertNotNull("Null version", version);
		assertTrue("Empty version", version.length() > 0);

		// Do this again to test the cache. The result of this can be seen in the EclEmma test
		// coverage output.
		version = avarice.getVersion(config);
		assertNotNull("Null version", version);
		assertTrue("Empty version", version.length() > 0);

		// Test with invalid command. This should throw an AVRDudeException
		config.setAttribute(AvariceTool.ATTR_CMD_NAME, "invalidcommandname");

		try {
			version = avarice.getVersion(config);
			fail("No Exception thrown for invalid command name");
		} catch (AVRDudeException ade) {
			// Maybe check the correct reason here
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getMCUs(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 * 
	 * @throws IOException
	 * @throws AVRDudeException
	 */
	@Test
	public void testGetMCUs() throws IOException, AVRDudeException {
		ITargetConfiguration tc = TargetConfigurationManager.getDefault().createNewConfig();

		// Use the default avarice command (avarice must be on the system path)
		Set<String> allmcus = avarice.getMCUs(tc);
		assertNotNull("Null MCU list", allmcus);
		assertTrue("Empty MCU List", allmcus.size() > 0);

		// Check a few more or less entries
		String[] testmcus = new String[] { "at90can128", "atmega128", "attiny13", "atxmega128a1" };

		for (String mcu : testmcus) {
			assertTrue("MCU List missing " + mcu, allmcus.contains(mcu));
		}

		// Test with invalid command. This should throw an AVRDudeException
		ITargetConfigurationWorkingCopy wc = TargetConfigurationManager.getDefault()
				.getWorkingCopy(tc.getId());
		wc.setAttribute(AvariceTool.ATTR_CMD_NAME, "invalidcommandname");

		try {
			allmcus = avarice.getMCUs(wc);
			fail("No Exception thrown for invalid command name");
		} catch (AVRDudeException ade) {
			// Maybe check the correct reason here
		}

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getProgrammers(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 */
	@Test
	public void testGetProgrammers() {

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getProgrammer(de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetProgrammer() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#validate(de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String)}
	 * .
	 */
	@Test
	public void testValidate() {
		fail("Not yet implemented");
	}

}
