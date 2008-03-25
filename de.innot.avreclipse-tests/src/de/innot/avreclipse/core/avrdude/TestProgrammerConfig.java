/**
 * 
 */
package de.innot.avreclipse.core.avrdude;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Thomas Holland
 * 
 */
public class TestProgrammerConfig {

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.ProgrammerConfig#ProgrammerConfig(java.lang.String)}.
	 * 
	 * @throws BackingStoreException
	 */
	@Test
	public void testProgrammerConfig() throws BackingStoreException {
		// create a new ProgrammerConfig
		ProgrammerConfig config = new ProgrammerConfig("test1");
		assertNotNull(config);
		assertEquals("test1", config.getName());

		// test all options
		config.setDescription("testdescription");
		config.setProgrammer("c2n232i"); // last entry
		config.setPort("/test/port");
		config.setBaudrate("123456");
		config.setExitspecResetline("reset");
		config.setExitspecVCCline("novcc");

		assertEquals("testdescription", config.getDescription());
		assertEquals("c2n232i", config.getProgrammer());
		assertEquals("/test/port", config.getPort());
		assertEquals("123456", config.getBaudrate());
		assertEquals("reset", config.getExitspecResetline());
		assertEquals("novcc", config.getExitspecVCCline());

		// Test Commandline arguments
		List<String> expected = new ArrayList<String>();
		expected.add("-cc2n232i");
		expected.add("-P/test/port");
		expected.add("-b123456");
		expected.add("-Ereset,novcc");
		List<String> actual = config.getArguments();
		assertEquals(expected, actual);

		// Test cloning
		ProgrammerConfig config2 = new ProgrammerConfig(config);
		assertEquals("testdescription", config2.getDescription());
		assertEquals("c2n232i", config2.getProgrammer());
		assertEquals("/test/port", config2.getPort());
		assertEquals("123456", config2.getBaudrate());
		assertEquals("reset", config2.getExitspecResetline());
		assertEquals("novcc", config2.getExitspecVCCline());

		// Test save
		config.setDescription("changeddesription");
		config.setProgrammer("avrisp"); // first entry
		config.setPort("/test/port/2");
		config.setBaudrate("654321");
		config.setExitspecResetline("noreset");
		config.setExitspecVCCline("vcc");
		config.save();

		ProgrammerConfig config3 = new ProgrammerConfig("test1");
		assertEquals("changeddesription", config3.getDescription());
		assertEquals("avrisp", config3.getProgrammer());
		assertEquals("/test/port/2", config3.getPort());
		assertEquals("654321", config3.getBaudrate());
		assertEquals("noreset", config3.getExitspecResetline());
		assertEquals("vcc", config3.getExitspecVCCline());

		// Test a name change
		config.setName("newname");
		config.save();

		// Settings off previous name should be gone
		ProgrammerConfig config5 = new ProgrammerConfig("test1");
		assertEquals("", config5.getDescription());

		// Test if the last changes have been persisted
		ProgrammerConfig config4 = new ProgrammerConfig("newname");
		assertEquals("changeddesription", config4.getDescription());
		assertEquals("avrisp", config4.getProgrammer());
		assertEquals("/test/port/2", config4.getPort());
		assertEquals("654321", config4.getBaudrate());
		assertEquals("noreset", config4.getExitspecResetline());
		assertEquals("vcc", config4.getExitspecVCCline());

		// Test delete
		config.delete();

		// saving the clone of deleted config should not throw an exception
		config2.save();

	}

}
