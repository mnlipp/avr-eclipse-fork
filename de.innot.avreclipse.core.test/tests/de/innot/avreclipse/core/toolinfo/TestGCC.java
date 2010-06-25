/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;

/**
 * @author U043192
 * 
 */
public class TestGCC {

	private GCC	tool	= null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tool = GCC.getDefault();
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.GCC#getDefault()}.
	 */
	@Test
	public void testGetDefault() {
		assertNotNull(tool);
		// this next test will fail if other than avr-tool toolchain is used
		assertEquals("avr-gcc", tool.getCommandName());
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
			String windowsname = gccfile.getPath() + ".exe";
			gccfile = new File(windowsname);
		}
		assertTrue("Toolpath does not point to an executable file", gccfile.canRead());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.GCC#getToolInfo(java.lang.String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetMCUList() throws IOException {
		Set<String> mcus = tool.getMCUList();
		assertNotNull(mcus);
		assertTrue(mcus.size() > 5); // at least a few micros should be in the list
		assertTrue(mcus.contains("atmega16"));
		assertFalse(mcus.contains("avr1"));
		assertFalse(mcus.contains(""));
		assertFalse(mcus.contains(null));
	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}

}
