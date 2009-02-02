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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TestTargetConfigurationManager {

	private TargetConfigurationManager	manager;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = TargetConfigurationManager.getDefault();
		assertNotNull("No target configuration manager", manager);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfigurationManager#createNewConfig()}.
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testCreateNewConfig() throws CoreException {
		ITargetConfiguration tc = manager.createNewConfig();
		assertNotNull("CreateNewConfig returned null", tc);
		assertNotNull("New config has null id", tc.getId());
		assertTrue("New config has empty id", tc.getId().length() > 0);

		// Check some defaults
		assertNotNull("New config has null name", tc.getName());
		assertTrue("New config has empty name", tc.getName().length() > 0);

		assertNotNull("New config has null mcuid", tc.getMCUId());
		assertTrue("New config has empty mcuid", tc.getMCUId().length() > 0);

		assertNotNull(tc.getAttribute(ITargetConfiguration.ATTR_NAME));
		assertNotNull(tc.getAttribute(ITargetConfiguration.ATTR_MCU));
		assertNotNull(tc.getAttribute(ITargetConfiguration.ATTR_FCPU));

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfigurationManager#exists(java.lang.String)}.
	 */
	@Test
	public void testExists() {
		ITargetConfiguration tc = manager.createNewConfig();

		assertTrue("Config does not exist", manager.exists(tc.getId()));

		// Check some non existing configs
		assertFalse(manager.exists("foobar"));
		assertFalse(manager.exists(""));
		assertFalse(manager.exists(null));

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfigurationManager#deleteConfig(java.lang.String)}
	 * .
	 */
	@Test
	public void testDeleteConfig() {
		// Create a new config and then delete it again
		ITargetConfiguration tc = manager.createNewConfig();

		manager.deleteConfig(tc.getId());
		assertFalse("Config was not deleted", manager.exists(tc.getId()));
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfigurationManager#getConfig(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetConfig() {
		// Create two new configs and then get them
		ITargetConfiguration tc1 = manager.createNewConfig();
		ITargetConfiguration tc2 = manager.createNewConfig();

		assertSame(tc1, manager.getConfig(tc1.getId()));
		assertSame(tc2, manager.getConfig(tc2.getId()));

		// Check some failure modes
		assertNull(manager.getConfig("foobar"));
		assertNull(manager.getConfig(""));
		assertNull(manager.getConfig(null));
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfigurationManager#getWorkingCopy(java.lang.String)}
	 * .
	 * 
	 * @throws CoreException
	 */
	@Test
	public void testGetWorkingCopy() throws CoreException {
		ITargetConfiguration tc = manager.createNewConfig();

		ITargetConfigurationWorkingCopy tcwc = manager.getWorkingCopy(tc.getId());

		assertNotNull("Null Working Copy", tcwc);
		assertEquals("Working copy has different id", tc.getId(), tcwc.getId());

		// Check that all attributes are the same
		Map<String, String> tcattrs = tc.getAttributes();
		Map<String, String> wcattrs = tcwc.getAttributes();
		String[] tcattrsarray = tcattrs.keySet().toArray(new String[tcattrs.size()]);
		String[] wcattrsarray = wcattrs.keySet().toArray(new String[tcattrs.size()]);

		// Sort the arrays to have both in the same order for comparing
		Arrays.sort(tcattrsarray);
		Arrays.sort(wcattrsarray);

		assertArrayEquals("Working copy has different attributes", tcattrsarray, wcattrsarray);
		for (String attr : tcattrs.keySet()) {
			String tcvalue = tcattrs.get(attr);
			String wcvalue = wcattrs.get(attr);
			assertEquals("Attribute " + attr + " of working copy differs from original", tcvalue,
					wcvalue);
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfigurationManager#getConfigurationIDs()}.
	 */
	@Test
	public void testGetConfigurationIDs() {
		// The list may or may not be empty at this point depending on the other tests that have
		// already run.

		// Add two more configs and check their ids in the list
		ITargetConfiguration tc1 = manager.createNewConfig();
		ITargetConfiguration tc2 = manager.createNewConfig();

		List<String> allids = manager.getConfigurationIDs();
		assertNotNull("ID list is null", allids);
		assertTrue("ID list must have 2 or more entries", allids.size() >= 2);

		assertTrue("First config id missing", allids.contains(tc1.getId()));
		assertTrue("Second config id missing", allids.contains(tc2.getId()));

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfigurationManager#getPreferences(java.lang.String)}
	 * .
	 * 
	 * @throws BackingStoreException
	 * @throws CoreException
	 */
	@Test
	public void testGetPreferences() throws BackingStoreException, CoreException {
		ITargetConfiguration tc = manager.createNewConfig();
		Preferences prefs = manager.getPreferences(tc.getId());
		assertNotNull(prefs);

		assertEquals("Prefs name should be same as TC id", tc.getId(), prefs.name());

		Map<String, String> tcattrs = tc.getAttributes();
		String[] attrs = tcattrs.keySet().toArray(new String[tcattrs.size()]);
		String[] prefkeys = prefs.keys();
		assertTrue("Prefs must have at least 3 attributes", prefkeys.length >= 3);
		for (String attr : attrs) {
			assertNotNull("Prefs is missing attribute" + attr, prefs.get(attr, null));
		}
	}

}
