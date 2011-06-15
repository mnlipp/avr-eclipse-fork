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
import de.innot.avreclipse.core.targets.ITargetConfiguration.Result;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetConfigurationManager;
import de.innot.avreclipse.core.targets.ToolManager;
import de.innot.avreclipse.core.targets.ITargetConfiguration.ValidationResult;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class AvariceToolTest {

	private AvariceTool						avarice;

	private ITargetConfigurationWorkingCopy	config;

	@Before
	public void setup() throws IOException {
		ITargetConfiguration hc = TargetConfigurationManager.getDefault().createNewConfig();
		config = TargetConfigurationManager.getDefault().getWorkingCopy(hc.getId());
		avarice = (AvariceTool) ToolManager.getDefault().getTool(config, AvariceTool.ID);
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
		String cmd = avarice.getCommand();
		assertNotNull("Null command", cmd);
		assertTrue("Empty command", cmd.length() > 0);
		assertEquals("Wrong default command", "avarice", cmd);

		// Change the command attribute and check that it is propagated
		config.setAttribute(AvariceTool.ATTR_CMD_NAME, "foobar");
		assertEquals("Wrong command", "foobar", avarice.getCommand());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getAttributes()}.
	 */
	@Test
	public void testGetAttributes() {
		// Get the list and check that it contains all known attributes
		String[] attrs = avarice.getAttributes();
		assertNotNull("Null defaults", attrs);
		assertTrue("Empty defaults", attrs.length > 0);

		List<String> attributeList = Arrays.asList(attrs);
		assertTrue("Command attr missing", attributeList.contains(AvariceTool.ATTR_CMD_NAME));
		assertTrue("UseConsole attr missing", attributeList.contains(AvariceTool.ATTR_USE_CONSOLE));

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
		String version = avarice.getVersion();
		assertNotNull("Null version", version);
		assertTrue("Empty version", version.length() > 0);

		// Do this again to test the cache. The result of this can be seen in the EclEmma test
		// coverage output.
		version = avarice.getVersion();
		assertNotNull("Null version", version);
		assertTrue("Empty version", version.length() > 0);

		// Test with invalid command. This should throw an AVRDudeException
		config.setAttribute(AvariceTool.ATTR_CMD_NAME, "invalidcommandname");

		try {
			version = avarice.getVersion();
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
		// Use the default avarice command (avarice must be on the system path)
		Set<String> allmcus = avarice.getMCUs();
		assertNotNull("Null MCU list", allmcus);
		assertTrue("Empty MCU List", allmcus.size() > 0);

		// Check a few more or less entries
		String[] testmcus = new String[] { "at90can128", "atmega128", "attiny13", "atxmega128a1" };

		for (String mcu : testmcus) {
			assertTrue("MCU List missing " + mcu, allmcus.contains(mcu));
		}

		// Test with invalid command. This should throw an AVRDudeException
		config.setAttribute(AvariceTool.ATTR_CMD_NAME, "invalidcommandname");

		try {
			allmcus = avarice.getMCUs();
			fail("No Exception thrown for invalid command name");
		} catch (AVRDudeException ade) {
			// Maybe check the correct reason here
		}

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getProgrammers(de.innot.avreclipse.core.targets.ITargetConfiguration)}
	 * .
	 * 
	 * @throws IOException
	 * @throws AVRDudeException
	 */
	@Test
	public void testGetProgrammers() throws IOException, AVRDudeException {
		// Use the default avarice command (avarice must be on the system path)
		Set<String> allproggers = avarice.getProgrammers();
		assertNotNull("Null Programmers list", allproggers);
		assertTrue("Empty Programmers List", allproggers.size() > 0);

		// Check all currently implemented entries
		String[] testproggers = new String[] { "dragon_jtag", "dragon_dw", "jtag1", "jtag2",
				"jtag2dw" };

		for (String progger : testproggers) {
			assertTrue("Programmer missing " + progger, allproggers.contains(progger));
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#getProgrammer(de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetProgrammer() {
		// Check all currently implemented entries
		String[] testproggers = new String[] { "dragon_jtag", "dragon_dw", "jtag1", "jtag2",
				"jtag2dw" };

		for (String programmerid : testproggers) {
			IProgrammer progger = config.getProgrammer(programmerid);
			assertNotNull("Null programmer", progger);
			assertEquals("Wrong progger id", programmerid, progger.getId());
			assertTrue("Empty programmer description", progger.getDescription().length() > 0);
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.tools.AvariceTool#validate(de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String)}
	 * .
	 */
	@Test
	public void testValidate() {

		String[] attributes = avarice.getAttributes();
		
		// Generic test for all supported attributes
		for (String attr : attributes) {
			ValidationResult result = avarice.validate(attr);
			assertNotNull("Null ValidationResult for "+ attr, result);
		}

		// Test invalid attribute, should return Null
		ValidationResult result = avarice.validate("foobar");
		assertEquals("Unexpected ValidationResult", Result.UNKNOWN_ATTRIBUTE, result.result);

		// Specific tests
		
		// As avarice may or may not be installed for the test we can only check if validate()
		// returns a valid ValidationResult
		result = avarice.validate(AvariceTool.ATTR_CMD_NAME);
		assertNotNull("Null ValidationResult for ATTR_CMD_NAME", result);
		
		// Test the other supported attributes
		result = avarice.validate(AvariceTool.ATTR_USE_CONSOLE);
		assertNotNull("Null ValidationResult for ATTR_USE_CONSOLE", result);
		assertEquals("ATTR_USE_CONSOLE should be supported", Result.OK, result.result);

		
		

	}

	@Test
	public void testInvocationDelay() {
		// Test that a defined invocation delay is honored
		// for this we start two
	}
}
