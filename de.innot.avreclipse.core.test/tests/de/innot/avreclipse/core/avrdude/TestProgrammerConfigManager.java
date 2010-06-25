/**
 * 
 */
package de.innot.avreclipse.core.avrdude;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

/**
 * @author Thomas Holland
 * 
 */
public class TestProgrammerConfigManager {

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.ProgrammerConfig#ProgrammerConfig(java.lang.String)}.
	 * 
	 * @throws BackingStoreException
	 */
	@Test
	public void testProgrammerConfig() throws BackingStoreException {
		ProgrammerConfigManager manager = ProgrammerConfigManager.getDefault();

		assertNotNull("Failed to init the Manager", manager);

		// Create an Config with the default values
		ProgrammerConfig config = manager.createNewConfig();

		assertNotNull(config);

		// Test default values
		assertTrue(config.getDescription().length() > 1);
		assertEquals("stk500v2", config.getProgrammer());
		assertEquals("", config.getPort());
		assertEquals("", config.getBaudrate());
		assertEquals("", config.getExitspecResetline());
		assertEquals("", config.getExitspecVCCline());

		// Check that the config is not yet in the list of configs
		Set<String> allids = manager.getAllConfigIDs();
		assertEquals(0, allids.size());

		// Fill the config with values
		config.setName("testname");
		config.setDescription("testdescription");
		config.setProgrammer("c2n232i"); // last entry
		config.setPort("/test/port");
		config.setBaudrate("123456");
		config.setExitspecResetline("reset");
		config.setExitspecVCCline("novcc");

		// And check that they are correct
		assertEquals("testname", config.getName());
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

		// Test Save
		manager.saveConfig(config);

		// The new config should now be in the list of all configs
		String configid = config.getId();

		allids = manager.getAllConfigIDs();
		assertEquals(1, allids.size());
		assertTrue(allids.contains(configid));

		// Test that the name is in the list of all names
		Map<String, String> allnames = manager.getAllConfigNames();
		assertNotNull(allnames);
		assertEquals(1, allnames.size());
		assertEquals("testname", allnames.get(configid));

		// Test getConfig()
		ProgrammerConfig config1 = manager.getConfig(configid);
		assertNotNull(config1);
		assertSame(config, config1);

		// Test getConfigEditable()
		ProgrammerConfig config2 = manager.getConfigEditable(config);
		assertNotSame(config, config2);
		assertEquals(configid, config2.getId());
		assertEquals("testname", config2.getName());
		assertEquals("testdescription", config2.getDescription());
		assertEquals("c2n232i", config2.getProgrammer());
		assertEquals("/test/port", config2.getPort());
		assertEquals("123456", config2.getBaudrate());
		assertEquals("reset", config2.getExitspecResetline());
		assertEquals("novcc", config2.getExitspecVCCline());

		// Change a few values, save them and test if they have propagated
		config2.setName("changedname");
		config2.setDescription("changeddesription");
		config2.setProgrammer("avrisp"); // first entry
		config2.setPort("/test/port/2");
		config2.setBaudrate("654321");
		config2.setExitspecResetline("noreset");
		config2.setExitspecVCCline("vcc");
		manager.saveConfig(config2);

		// Because config2 is a clone of config, its values should
		// have propagated to config when config2 was saved.
		assertEquals("changedname", config.getName());
		assertEquals("changeddesription", config.getDescription());
		assertEquals("avrisp", config.getProgrammer());
		assertEquals("/test/port/2", config.getPort());
		assertEquals("654321", config.getBaudrate());
		assertEquals("noreset", config.getExitspecResetline());
		assertEquals("vcc", config.getExitspecVCCline());

		// Test delete
		manager.deleteConfig(config2);

		// Once the clone config2 is deleted, reloading the id should return a
		// null value.
		config = manager.getConfig(configid);
		assertNull("getConfig(removedId) did not return null", config);

	}

}
