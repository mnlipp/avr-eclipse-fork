/**
 * 
 */
package de.innot.avreclipse.core.avrdude;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.innot.avreclipse.core.avrdude.AVRDudeAction.Action;
import de.innot.avreclipse.core.avrdude.AVRDudeAction.FileType;
import de.innot.avreclipse.core.avrdude.AVRDudeAction.MemType;

/**
 * @author Thomas Holland
 * 
 */
public class TestAVRDudeAction {

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.AVRDudeAction#getArgument()}.
	 */
	@Test
	public void testGetArgument() {
		AVRDudeAction action = new AVRDudeAction(MemType.flash, Action.read, "filename",
		        FileType.iHex);
		assertEquals("-Uflash:r:filename:i", action.getArgument());

		action = new AVRDudeAction(MemType.efuse, Action.write, 255);
		assertEquals("-Uefuse:w:0xff:m", action.getArgument());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.AVRDudeAction#getActionForArgument(java.lang.String)}.
	 */
	@Test
	public void testGetActionForArgument() {

		// Good arguments
		String[] tests = { "-Ufuse:w:\"filename\":i", "-Uflash:r:0xff:m", "-Ueeprom:v::a" };

		String argument = null;
		AVRDudeAction action;

		try {
			for (String testarg : tests) {
				argument = testarg; // Store arg in case an Exception is thrown
				action = AVRDudeAction.getActionForArgument(argument);
				assertEquals(argument, action.getArgument());
			}
		} catch (IllegalArgumentException iae) {
			fail("Test failed for " + argument);
		}

		// More good arguments (which will not match 1:1)
		argument = " -U  signature:verify:D:\\dir\\path:auto";
		action = AVRDudeAction.getActionForArgument(argument);
		assertEquals("-Usignature:v:D:\\dir\\path:a", action.getArgument());

		argument = "\t-U\teeprom:v:/dir/path";
		action = AVRDudeAction.getActionForArgument(argument);
		assertEquals("-Ueeprom:v:/dir/path:a", action.getArgument());

		argument = "-U filename";
		action = AVRDudeAction.getActionForArgument(argument);
		assertEquals("-Uflash:w:filename:a", action.getArgument());

		// Some failures
		argument = "-U foo:bar:filename:baz";
		try {
			action = AVRDudeAction.getActionForArgument(argument);
			fail("getActionForArgument() did not throw expected Exception");
		} catch (IllegalArgumentException iae) {
		}

		argument = "-Uflash:bar:filename:baz";
		try {
			action = AVRDudeAction.getActionForArgument(argument);
			fail("getActionForArgument() did not throw expected Exception");
		} catch (IllegalArgumentException iae) {
		}

		argument = "-Uflash:w:filename:q";
		try {
			action = AVRDudeAction.getActionForArgument(argument);
			fail("getActionForArgument() did not throw expected Exception");
		} catch (IllegalArgumentException iae) {
		}

		argument = "-Lflash:w:filename:a";
		try {
			action = AVRDudeAction.getActionForArgument(argument);
			fail("getActionForArgument() did not throw expected Exception");
		} catch (IllegalArgumentException iae) {
		}
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

}
