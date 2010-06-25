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
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.toolinfo.AVRDude;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TestTargetConfiguration implements ITargetConfigConstants {

	/**
	 * Extension of {@link TargetConfiguration} to get access to the protected constructors.
	 * 
	 */
	private class MyTargetConfiguration extends TargetConfiguration {
		protected MyTargetConfiguration(IPath file) throws IOException {
			super(file);
		}

		protected MyTargetConfiguration(TargetConfiguration config) {
			super(config);
		}

	}

	private TargetConfiguration	tc;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		IPath testfile = getTestFile();
		tc = new MyTargetConfiguration(testfile);
		assertNotNull("Config is null", tc);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfiguration#TargetConfiguration(de.innot.avreclipse.core.targets.TargetConfiguration)}
	 * .
	 * 
	 * @throws IOException
	 */
	@Test
	public void testTargetConfigurationTargetConfiguration() throws IOException {

		ITargetConfigChangeListener faillistener = new ITargetConfigChangeListener() {
			public void attributeChange(ITargetConfiguration config, String attribute,
					String oldvalue, String newvalue) {
				fail("Changing an attribute of the copy caused a attributeChange event of the original");
			}
		};

		// Change a few settings in the standard target configuration
		tc.setName("testName");
		tc.setDescription("testDescription");
		tc.setMCU("testMCU");
		tc.setFCPU(12345678);

		tc.setAttribute("testattr1", "foo");
		tc.setAttribute("testattr2", "bar");

		tc.addPropertyChangeListener(faillistener);

		// Now clone this target config
		TargetConfiguration wc = new MyTargetConfiguration(tc);
		assertNotNull("Copy is null", wc);

		// Check that all changes were propagated
		assertEquals(tc.getId(), wc.getId());
		assertEquals(tc.getName(), wc.getName());
		assertEquals(tc.getDescription(), wc.getDescription());
		assertEquals(tc.getMCU(), wc.getMCU());
		assertEquals(tc.getFCPU(), wc.getFCPU());
		assertEquals("foo", wc.getAttribute("testattr1"));
		assertEquals("bar", wc.getAttribute("testattr2"));

		// Check that the listener was not copied
		wc.setAttribute("testattr1", "baz");

		// and that the copy is separate
		assertEquals("foo", tc.getAttribute("testattr1"));

		// but on safe the change should propagate to the original config
		tc.removePropertyChangeListener(faillistener);
		wc.doSave();

		assertEquals("baz", tc.getAttribute("testattr1"));

	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#getId()}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetId() throws Exception {
		IPath testfile = getTestFile();
		TargetConfiguration test = new MyTargetConfiguration(testfile);
		assertNotNull(test);
		assertEquals(testfile.lastSegment(), test.getId());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#getName()}.
	 */
	@Test
	public void testName() {
		String[] testnames = new String[] { "foo", "bar", "" };

		for (String name : testnames) {
			tc.setName(name);

			assertEquals(name, tc.getName());
			assertEquals(name, tc.getAttribute(ATTR_NAME));
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#getDescription()}
	 * .
	 */
	@Test
	public void testDescription() {
		String[] testdescriptions = new String[] { "foo", "bar", "" };

		for (String desc : testdescriptions) {
			tc.setDescription(desc);

			assertEquals(desc, tc.getDescription());
			assertEquals(desc, tc.getAttribute(ATTR_DESCRIPTION));
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#getMCU()}.
	 */
	@Test
	public void testMCUId() {
		String[] testmcus = new String[] { "foo", "bar", "" };

		for (String mcu : testmcus) {
			tc.setMCU(mcu);

			assertEquals(mcu, tc.getMCU());
			assertEquals(mcu, tc.getAttribute(ATTR_MCU));
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#getFCPU()}.
	 */
	@Test
	public void testFCPU() {
		int[] testvalues = new int[] { 1234, 123, 0 };

		for (int fcpu : testvalues) {
			tc.setFCPU(fcpu);

			assertEquals(fcpu, tc.getFCPU());
			assertEquals(fcpu, Integer.parseInt(tc.getAttribute(ATTR_FCPU)));
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#doSave()}.
	 * 
	 * @throws BackingStoreException
	 * @throws IOException
	 */
	@Test
	public void testDoSave() throws BackingStoreException, IOException {

		// Make a "working copy"
		TargetConfiguration wc = new MyTargetConfiguration(tc);

		// Change a few settings in the standard target configuration
		wc.setName("testNameCopy");
		wc.setDescription("testDescriptionCopy");
		wc.setMCU("testMCUCopy");
		wc.setFCPU(12345678);

		// pre-check the dirty flags
		assertTrue("Workingcopy should be dirty", wc.isDirty());
		assertFalse("Original should be clean", tc.isDirty());

		// save the config.
		wc.doSave();

		// check the dirty flags
		assertFalse("Workingcopy should be clean", wc.isDirty());
		assertFalse("Original should be clean", tc.isDirty());

		// check that the changes were propagated to the original
		assertEquals("testNameCopy", tc.getName());
		assertEquals("testDescriptionCopy", tc.getDescription());
		assertEquals("testMCUCopy", tc.getMCU());
		assertEquals(12345678, tc.getFCPU());

		// Now reload the save config from the preference storage area with the help of the target
		// configuration manager
		ITargetConfiguration tc2 = TargetConfigurationManager.getDefault().getConfig(tc.getId());

		// and do the same checks
		assertEquals("testNameCopy", tc2.getName());
		assertEquals("testDescriptionCopy", tc2.getDescription());
		assertEquals("testMCUCopy", tc2.getMCU());
		assertEquals(12345678, tc2.getFCPU());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#restoreDefaults()}.
	 */
	@Test
	public void testSetDefaults() {
		String id = tc.getId();
		// Change a few settings in the standard target configuration
		tc.setName("testName");
		tc.setDescription("testDescription");
		tc.setMCU("testMCU");
		tc.setFCPU(12345678);

		// Now set the defaults and check that they have been applied
		tc.restoreDefaults();

		// Check that all changes were propagated
		assertEquals(id, tc.getId());
		assertEquals(DEF_NAME, tc.getName());
		assertEquals(DEF_DESCRIPTION, tc.getDescription());
		assertEquals(DEF_MCU, tc.getMCU());
		assertEquals(DEF_FCPU, tc.getFCPU());

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfiguration#getAttribute(java.lang.String)}.
	 * 
	 * @throws BackingStoreException
	 */
	@Test
	public void testAttribute() throws BackingStoreException {

		assertFalse(tc.isDirty());

		tc.setAttribute("foo", "bar");

		assertTrue(tc.isDirty());
		assertEquals("bar", tc.getAttribute("foo"));

		// Test failures
		try {
			tc.setAttribute(null, "bar");
			fail("setAttribute(null, xxx) did not throw assertion error");
		} catch (Exception e) {
			// Exception expected
		}
		try {
			tc.setAttribute("foo", null);
			fail("setAttribute(xxx, null) did not throw assertion error");
		} catch (Exception e) {
			// Exception expected
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#getAttributes()}.
	 */
	@Test
	public void testGetAttributes() {
		// Add one more custom attribute
		tc.setAttribute("foo", "bar");

		// now get the list of all attributes and check that the standard attributes + the custom
		// attr are in the list
		Map<String, String> attrmap = tc.getAttributes();

		assertNotNull(attrmap);
		assertTrue(attrmap.size() > 0);

		assertTrue(attrmap.containsKey(ATTR_NAME));
		assertTrue(attrmap.containsKey(ATTR_DESCRIPTION));
		assertTrue(attrmap.containsKey(ATTR_MCU));
		assertTrue(attrmap.containsKey(ATTR_FCPU));
		assertTrue(attrmap.containsKey("foo"));

		assertFalse(attrmap.containsKey(null));

		// Test failures
		try {
			tc.getAttribute(null);
			fail("getAttribute(null) did not throw assertion error");
		} catch (Exception e) {
			// Exception expected
		}

	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.targets.TargetConfiguration#dispose()}.
	 */
	@Test
	public void testDispose() {
		ITargetConfigChangeListener faillistener = new ITargetConfigChangeListener() {
			public void attributeChange(ITargetConfiguration config, String attribute,
					String oldvalue, String newvalue) {
				fail("Change event generated after dispose()");
			}
		};

		tc.addPropertyChangeListener(faillistener);

		tc.dispose();
		// the next line will cause a change event, but the listener should be disposed
		tc.setName("foobar");
	}

	private String	fAttribute;
	private String	fOldValue;
	private String	fNewValue;

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfiguration#addPropertyChangeListener(de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener)}
	 * .
	 */
	@Test
	public void testAddPropertyChangeListener() {
		ITargetConfigChangeListener listener = new ITargetConfigChangeListener() {
			public void attributeChange(ITargetConfiguration config, String attribute,
					String oldvalue, String newvalue) {
				assertSame(tc, config);
				fAttribute = attribute;
				fOldValue = oldvalue;
				fNewValue = newvalue;
			}
		};

		tc.addPropertyChangeListener(listener);

		tc.setName("testname");
		assertEquals(ATTR_NAME, fAttribute);
		assertEquals("testname", fNewValue);

		tc.setName("testname2");
		assertEquals(ATTR_NAME, fAttribute);
		assertEquals("testname", fOldValue);
		assertEquals("testname2", fNewValue);

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.TargetConfiguration#removePropertyChangeListener(de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener)}
	 * .
	 */
	@Test
	public void testRemovePropertyChangeListener() {
		ITargetConfigChangeListener faillistener = new ITargetConfigChangeListener() {
			public void attributeChange(ITargetConfiguration config, String attribute,
					String oldvalue, String newvalue) {
				fail("Change event generated after listener has been removed");
			}
		};

		tc.addPropertyChangeListener(faillistener);
		tc.removePropertyChangeListener(faillistener);

		// the next line will cause a change event, but the listener should be removed
		tc.setName("foobar");
	}

	/**
	 * This is not a test but just a small utility to dump all information about all programmers to
	 * the console.
	 * 
	 * @throws AVRDudeException
	 */
	// @Test
	public void dumpProgrammers() throws AVRDudeException {
		List<IProgrammer> allprogrammers = AVRDude.getDefault().getProgrammersList();

		Collections.sort(allprogrammers, new Comparator<IProgrammer>() {

			public int compare(IProgrammer o1, IProgrammer o2) {
				return o1.getDescription().compareToIgnoreCase(o2.getDescription());
			}
		});

		for (IProgrammer programmer : allprogrammers) {
			StringBuilder sb = new StringBuilder();
			sb.append(programmer.getDescription());
			sb.append("\t");
			sb.append(programmer.getId());
			sb.append("\t");

			// find the type by looking for "type = xxx" in the info text
			Pattern typePat = Pattern.compile(".*type\\s*=\\s*(\\w*);.*", Pattern.DOTALL);
			Matcher m = typePat.matcher(programmer.getAdditionalInfo());
			if (m.matches()) {
				sb.append(m.group(1));
				sb.append("\t");
			} else {
				sb.append("type not found;\t");
			}

			HostInterface[] allhis = programmer.getHostInterfaces();
			for (HostInterface hi : allhis) {
				sb.append(hi.name());
				sb.append(" ");
			}
			sb.append("\t");
			sb.append(programmer.getTargetInterface().name());

			System.out.println(sb.toString());
		}
	}

	private IPath getTestFile() throws Exception {
		IPath folder = getConfigFolder();
		return folder.append("targetconfig.test");
	}

	private IPath getConfigFolder() throws IOException {
		IPath location = AVRPlugin.getDefault().getStateLocation().append("hardwareconfigs");
		File folder = location.toFile();
		if (!folder.exists()) {
			if (!folder.mkdirs()) {
				throw new IOException("Could not create hardware config storage folder '"
						+ folder.toString() + "'");
			}
		}
		return location;
	}

}
