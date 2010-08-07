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
package de.innot.avreclipse.core.paths.win32;

import static org.junit.Assert.*;

import java.util.List;

import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Thomas Holland
 * @since 2.3.2
 * 
 */
public class TestMyWindowsRegistry {

	private MyWindowsRegistry	registry;

	@Before
	public void setup() {
		if (!isWindows()) {
			// Test is only valid on Windows systems
			return;
		}

		registry = MyWindowsRegistry.getRegistry();
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.paths.win32.MyWindowsRegistry#getRegistry()}.
	 */
	@Test
	public void testGetRegistry() {
		if (!isWindows()) {
			// Test is only valid on Windows systems
			return;
		}

		assertNotNull(registry);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.paths.win32.MyWindowsRegistry#getKeyValue(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetLocalMachineKeyValue() {
		if (!isWindows()) {
			// Test is only valid on Windows systems
			return;
		}

		// Test the fallback
		registry.setInhibitOriginal(true);

		String value = registry.getKeyValue(
				"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", "www");
		assertEquals("http://", value);

		// Compare with original
		registry.setInhibitOriginal(false);
		value = registry.getKeyValue(
				"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", "www");
		assertEquals("http://", value);

		// Invalid key returns null
		registry.setInhibitOriginal(true);
		value = registry.getKeyValue("HKLM\\Software\\foo", "bar");
		assertNull(value);

		// invalid name return null
		value = registry.getKeyValue(
				"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", "foobar");
		assertNull(value);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.paths.win32.MyWindowsRegistry#getKeyName(java.lang.String, int)}
	 * .
	 */
	@Test
	public void testGetLocalMachineKeyName() {
		if (!isWindows()) {
			// Test is only valid on Windows systems
			return;
		}

		// Test the fallback
		registry.setInhibitOriginal(true);

		// The Prefixes registry entry has at least 5 entries
		for (int i = 0; i < 5; i++) {

			// Test the fallback
			registry.setInhibitOriginal(true);

			String name1 = registry.getKeyName(
					"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", i);
			assertNotNull(name1);

			// Compare with original
			registry.setInhibitOriginal(false);
			String name2 = registry.getKeyName(
					"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", i);
			assertNotNull(name2);
			assertEquals(name1, name2);
		}

		// Invalid key returns null
		registry.setInhibitOriginal(true);
		String name = registry.getKeyName("HKLM\\Software\\foo", 0);
		assertNull(name);

		// invalid index returns null
		name = registry.getKeyName(
				"HKLM\\SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", 99);
		assertNull(name);

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.paths.win32.MyWindowsRegistry#getLocalMachineSubeys(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetLocalMachineSubkeys() {
		if (!isWindows()) {
			// Test is only valid on Windows systems
			return;
		}

		// Test the fallback
		registry.setInhibitOriginal(true);
		
		// The test requires that at least one WinAVR is installed
		String key = "HKLM\\SOFTWARE\\Free Software Foundation\\";
		List<String> winavr1 = registry.getSubkeys(key);
		assertNotNull("getLocalMachineSubKeys returned null", winavr1);
		
		// Test that all returned keys with WinAVR exist
		boolean noavr = true;
		for (String subkey : winavr1) {
			if(subkey.contains("WinAVR")) {
				String gcc = registry.getKeyValue(subkey, "GCC");
				assertTrue("Empty GCC key returned", gcc.length() > 0);
				noavr = false;
			}
		}
		assertFalse("No WinAVR subkey found", noavr);
		
		
		// Check the original CDT Winregistry returns the same result
		registry.setInhibitOriginal(false);
		
		// The test requires that at least one WinAVR is installed
		List<String> winavr_orig = registry.getSubkeys(key);
		assertNotNull("getLocalMachineSubKeys returned null", winavr_orig);

		// Test that all returned keys with WinAVR exist
		noavr = true;
		for (String subkey : winavr_orig) {
			if(subkey.contains("WinAVR")) {
				String gcc = registry.getKeyValue(subkey, "GCC");
				assertTrue("Empty GCC key returned", gcc.length() > 0);
				noavr = false;
			}
		}
		assertFalse("No WinAVR subkey found", noavr);

		assertEquals("Difference between original and replacement",winavr1.size(), winavr_orig.size());
		
		
	}
	

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}

}
