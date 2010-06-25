/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;

/**
 * @author U043192
 *
 */
public class TestSize {

	private Size tool = null;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tool = Size.getDefault();
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.GCC#getDefault()}.
	 */
	@Test
	public void testGetDefault() {
		assertNotNull(tool);
		// this next test will fail if other than avr-gcc toolchain is used
		assertEquals("avr-size", tool.getCommandName());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.GCC#getToolPath()}.
	 */
	@Test
	public void testGetToolPath() {
		IPath gccpath = tool.getToolPath();
		assertNotNull("No ToolPath returned", gccpath);
		File gccfile = gccpath.toFile();
		if (isWindows()) {
			// append .exe
			String windowsname = gccfile.getPath() +".exe";
			gccfile = new File(windowsname);
		}

		assertTrue("Toolpath does not point to an executable file", gccfile.canRead());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.GCC#getToolInfo(java.lang.String)}.
	 */
	@Test
	public void testGetToolInfo() {
		Map<String, String> options = tool.getSizeOptions();
		assertNotNull(options);
		assertTrue(options.size()>1); // at least two formats should be in the list
		assertTrue(options.containsValue("sysv"));
		assertTrue(options.containsKey("SysV Format"));
		if (options.size() == 3) {
			// test the avr option
			assertTrue(options.containsValue("avr"));
			assertTrue(options.containsKey("AVR Specific Format"));
		}
		assertFalse(options.containsValue(""));
		assertFalse(options.containsValue(null));
	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}


}
