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
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetConfigurationManager;
import de.innot.avreclipse.core.targets.ToolManager;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TestAVRDudeTool {

	private AvrdudeTool						avrdude;

	private ITargetConfigurationWorkingCopy	config;

	@Before
	public void setup() throws IOException {
		ITargetConfiguration hc = TargetConfigurationManager.getDefault().createNewConfig();
		config = TargetConfigurationManager.getDefault().getWorkingCopy(hc.getId());
		avrdude = (AvrdudeTool) ToolManager.getDefault().getTool(config, AvrdudeTool.ID);
	}

	@After
	public void tearDown() throws IOException {
		// Remove the generated Hardware Configuration
		TargetConfigurationManager.getDefault().deleteConfig(config.getId());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.tools.AvrdudeTool#getName()}.
	 */
	@Test
	public void testBasics() {
		assertEquals("AVRDude", avrdude.getName());
		assertEquals(AvrdudeTool.ID, avrdude.getId());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvrdudeTool#getCommand(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetCommand() throws IOException {
		// first check if default is returned for an unmodified hardware config
		String cmd = avrdude.getCommand();
		assertNotNull("Null command", cmd);
		assertTrue("Empty command", cmd.length() > 0);
		assertEquals("Wrong default command", "avrdude", cmd);

		// Change the command attribute and check that it is propagated
		config.setAttribute(AvrdudeTool.ATTR_CMD_NAME, "foobar");
		assertEquals("Wrong command", "foobar", avrdude.getCommand());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.tools.AvrdudeTool#getDefaults()}.
	 */
	@Test
	public void testGetAttributes() {
		// Get the list and check that it contains all known attributes
		String[] attrs = avrdude.getAttributes();
		assertNotNull("Null defaults", attrs);
		assertTrue("Empty defaults", attrs.length > 0);

		List<String> attributeList = Arrays.asList(attrs);
		assertTrue("Command attr missing", attributeList.contains(AvrdudeTool.ATTR_CMD_NAME));
		assertTrue("UseConsole attr missing", attributeList.contains(AvrdudeTool.ATTR_USE_CONSOLE));

		// Add other attributes once they have been implemented
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvrdudeTool#getVersion(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 * 
	 * @throws IOException
	 * @throws AVRDudeException
	 */
	@Test
	public void testGetVersion() throws IOException, AVRDudeException {
		// Use the default avrdude command (avrdude must be on the system path)
		String version = avrdude.getVersion();
		assertNotNull("Null version", version);
		assertTrue("Empty version", version.length() > 0);

		// Do this again to test the cache. The result of this can be seen in the EclEmma test
		// coverage output.
		version = avrdude.getVersion();
		assertNotNull("Null version", version);
		assertTrue("Empty version", version.length() > 0);

		// Test with invalid command. This should throw an AVRDudeException
		config.setAttribute(AvrdudeTool.ATTR_CMD_NAME, "invalidcommandname");

		try {
			version = avrdude.getVersion();
			fail("No Exception thrown for invalid command name");
		} catch (AVRDudeException ade) {
			// Maybe check the correct reason here
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvrdudeTool#getMCUs(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 * 
	 * @throws IOException
	 * @throws AVRDudeException
	 */
	@Test
	public void testGetMCUs() throws IOException, AVRDudeException {
		// Use the default avrdude command (avrdude must be on the system path)
		Set<String> allmcus = avrdude.getMCUs();
		assertNotNull("Null MCU list", allmcus);
		assertTrue("Empty MCU List", allmcus.size() > 0);

		// Check a few entries
		String[] testmcus = new String[] { "atxmega128a1", "atmega6450", "at90usb1287", "attiny84",
				"attiny11" };

		for (String mcu : testmcus) {
			assertTrue("MCU List missing " + mcu, allmcus.contains(mcu));
		}

		// Test with invalid command. This should throw an AVRDudeException
		config.setAttribute(AvrdudeTool.ATTR_CMD_NAME, "invalidcommandname");
		try {
			allmcus = avrdude.getMCUs();
			fail("No Exception thrown for invalid command name");
		} catch (AVRDudeException ade) {
			// Maybe check the correct reason here
		}

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvrdudeTool#getProgrammers(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 */
	@Test
	public void testGetProgrammers() {
		// Check some currently implemented entries
		String[] testproggers = new String[] { "c2n232i", "avrisp", "jtag1", "jtag2", "jtag2dw" };

		for (String programmerid : testproggers) {
			IProgrammer progger = config.getProgrammer(programmerid);
			assertNotNull("Null programmer", progger);
			assertEquals("Wrong progger id", programmerid, progger.getId());
			assertTrue("Empty programmer description", progger.getDescription().length() > 0);
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvrdudeTool#getProgrammer(de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String)}
	 * .
	 * 
	 * @throws AVRDudeException
	 */
	@Test
	public void testGetProgrammer() throws AVRDudeException {
		// Use the default avrdude command (avrdude must be on the system path)
		Set<String> allproggers = avrdude.getProgrammers();
		assertNotNull("Null Programmers list", allproggers);
		assertTrue("Empty Programmers List", allproggers.size() > 0);

		// Check some currently implemented programmers
		String[] testproggers = new String[] { "c2n232i", "avrisp", "jtag1", "jtag2", "jtag2dw" };

		for (String progger : testproggers) {
			assertTrue("Programmer missing " + progger, allproggers.contains(progger));
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvrdudeTool#validate(de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String)}
	 * .
	 */
	@Test
	public void testValidate() {
		fail("Not yet implemented");
	}

}
