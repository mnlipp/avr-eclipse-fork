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

		ProgrammerConfigManager manager = ProgrammerConfigManager.getDefault();
		// create a new ProgrammerConfig
		ProgrammerConfig config = manager.createNewConfig();
		assertNotNull(config);

		// test all options
		config.setName("test1");
		config.setDescription("testdescription");
		config.setProgrammer("c2n232i"); // last entry
		config.setPort("/test/port");
		config.setBaudrate("123456");
		config.setExitspecResetline("reset");
		config.setExitspecVCCline("novcc");

		assertEquals("test1", config.getName());
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
		ProgrammerConfig config2 = manager.getConfigEditable(config);
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
		manager.saveConfig(config);

		// Test if the last changes have been persisted
		ProgrammerConfig config3 = manager.getConfig(config.getId());
		assertEquals("changeddesription", config3.getDescription());
		assertEquals("avrisp", config3.getProgrammer());
		assertEquals("/test/port/2", config3.getPort());
		assertEquals("654321", config3.getBaudrate());
		assertEquals("noreset", config3.getExitspecResetline());
		assertEquals("vcc", config3.getExitspecVCCline());

		// Test delete
		manager.deleteConfig(config);
		assertTrue(manager.getConfig(config.getId()) == null);

	}
}
