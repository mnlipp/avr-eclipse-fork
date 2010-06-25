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
	 * {@link de.innot.avreclipse.core.paths.win32.MyWindowsRegistry#getLocalMachineValue(java.lang.String, java.lang.String)}
	 * .
	 */
	@Test
	public void testGetLocalMachineValue() {
		if (!isWindows()) {
			// Test is only valid on Windows systems
			return;
		}

		// Test the fallback
		registry.setInhibitOriginal(true);

		String value = registry.getLocalMachineValue(
				"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", "www");
		assertEquals("http://", value);

		// Compare with original
		registry.setInhibitOriginal(false);
		value = registry.getLocalMachineValue(
				"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", "www");
		assertEquals("http://", value);

		// Invalid key returns null
		registry.setInhibitOriginal(true);
		value = registry.getLocalMachineValue("Software\\foo", "bar");
		assertNull(value);

		// invalid name return null
		value = registry.getLocalMachineValue(
				"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", "foobar");
		assertNull(value);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.paths.win32.MyWindowsRegistry#getLocalMachineValueName(java.lang.String, int)}
	 * .
	 */
	@Test
	public void testGetLocalMachineValueName() {
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

			String name1 = registry.getLocalMachineValueName(
					"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", i);
			assertNotNull(name1);

			// Compare with original
			registry.setInhibitOriginal(false);
			String name2 = registry.getLocalMachineValueName(
					"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", i);
			assertNotNull(name2);
			assertEquals(name1, name2);
		}

		// Invalid key returns null
		registry.setInhibitOriginal(true);
		String name = registry.getLocalMachineValueName("Software\\foo", 0);
		assertNull(name);

		// invalid index returns null
		name = registry.getLocalMachineValueName(
				"SOFTWARE\\Microsoft\\Windows\\CurrentVersion\\URL\\Prefixes", 99);
		assertNull(name);

	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}

}
