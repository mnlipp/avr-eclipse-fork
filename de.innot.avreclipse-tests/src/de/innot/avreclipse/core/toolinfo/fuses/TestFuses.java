/**
 * 
 */
package de.innot.avreclipse.core.toolinfo.fuses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.util.Set;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author U043192
 * 
 */
public class TestFuses {

	private static Fuses	fFuses;

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fFuses = Fuses.getDefault();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.fuses.Fuses#getMCUList()}.
	 */
	@Test
	public void testGetMCUList() {
		Set<String> allmcus = fFuses.getMCUList();
		assertNotNull("getMCUList() return null", allmcus);
		assertFalse("getMCUList() returned empty list", allmcus.size() == 0);

		assertFalse("MCU list has null entries", allmcus.contains(null));
		assertFalse("MCU List has empty entries", allmcus.contains(""));
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.Fuses#hasMCU(java.lang.String)}.
	 */
	@Test
	public void testHasMCU() {

		// our main test MCU
		assertEquals("hasMCU(\"atmega16\")", true, fFuses.hasMCU("atmega16"));

		// Test the first and last MCU (alphabetically)
		assertEquals("hasMCU(\"at90can128\")", true, fFuses.hasMCU("at90can128"));
		assertEquals("hasMCU(\"attiny861\")", true, fFuses.hasMCU("attiny861"));

		// Test MCUs without Fusebytes, but with Lockbits
		assertEquals("hasMCU(\"at89s52\")", true, fFuses.hasMCU("at89s52"));

		// Test a few failures
		assertEquals("hasMCU(\"foobar\")", false, fFuses.hasMCU("foobar"));
		assertEquals("hasMCU(\"\")", false, fFuses.hasMCU(""));
		assertEquals("hasMCU(null)", false, fFuses.hasMCU(null));
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.Fuses#getFusesDescription(java.lang.String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetFusesDescription() throws IOException {
		// get a few Descriptions and test that they returned good values
		IMCUDescription test = fFuses.getDescription("atmega16");
		assertNotNull("getFusesDescription(\"atmega16\") returned null", test);

		test = fFuses.getDescription("at90s1200");
		assertNotNull("getFusesDescription(\"at90s1200\") returned null", test);

		test = fFuses.getDescription("atxmega64a1");
		assertNotNull("getFusesDescription(\"atxmega64a1\") returned null", test);

		// Test a no fuse MCU.
		test = fFuses.getDescription("at89s51");
		assertNotNull("getFusesDescription(\"at89s51\") returned null", test);

		// Test a non-Existing MCU. This should return null
		test = fFuses.getDescription("foobar");
		assertNull("getFusesDescription(\"foobar\") did not return null", test);

		test = fFuses.getDescription("");
		assertNull("getFusesDescription(\"\") did not return null", test);

		test = fFuses.getDescription(null);
		assertNull("getFusesDescription(null) did not return null", test);

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.Fuses#getFuseByteCount(java.lang.String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetFuseByteCount() throws IOException {

		// Test a 6 fuse bytes MCU
		int count = fFuses.getFuseByteCount("atxmega128a1");
		assertEquals("getFuseByteCount(\"atxmega128a1\")", 6, count);

		// Test a 3 fuse bytes MCU
		count = fFuses.getFuseByteCount("at90pwm2");
		assertEquals("getFuseByteCount(\"at90pwm2\")", 3, count);

		// Test a 2 fuse bytes MCU
		count = fFuses.getFuseByteCount("atmega323");
		assertEquals("getFuseByteCount(\"atmega323\")", 2, count);

		// Test a 1 fuse bytes MCU
		count = fFuses.getFuseByteCount("attiny12");
		assertEquals("getFuseByteCount(\"attiny12\")", 1, count);

		// Test a 0 fuse bytes MCU
		count = fFuses.getFuseByteCount("at86rf401");
		assertEquals("getFuseByteCount(\"at86rf401\")", 0, count);

		// Test invalid MCU names
		count = fFuses.getFuseByteCount("foobar");
		assertEquals("getFuseByteCount(\"foobar\")", 0, count);

		count = fFuses.getFuseByteCount("");
		assertEquals("getFuseByteCount(\"\")", 0, count);

		count = fFuses.getFuseByteCount(null);
		assertEquals("getFuseByteCount(null)", 0, count);

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.Fuses#getMCUInfo(java.lang.String)}.
	 */
	@Test
	public void testGetMCUInfo() {
		// This is the same as getFuseByteCount(), just wrapped in a String.
		// So not tested here
	}

}
