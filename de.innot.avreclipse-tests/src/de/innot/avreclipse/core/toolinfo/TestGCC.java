/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;

/**
 * @author U043192
 *
 */
public class TestGCC {

	private GCC tool = null;
	
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
			String windowsname = gccfile.getPath() +".exe";
			gccfile = new File(windowsname);
		}
		assertTrue("Toolpath does not point to an executable file", gccfile.canRead());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.GCC#getToolInfoTypes()}.
	 */
	@Test
	public void testGetToolInfoTypes() {
		List<String> types = tool.getToolInfoTypes();
		assertNotNull(types);
		assertTrue(types.size()>0);
		assertTrue(types.contains(IToolInfo.TOOLINFOTYPE_MCUS));
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.GCC#getToolInfo(java.lang.String)}.
	 */
	@Test
	public void testGetToolInfo() {
		Map<String, String> mcus = tool.getToolInfo(IToolInfo.TOOLINFOTYPE_MCUS);
		assertNotNull(mcus);
		assertTrue(mcus.size()>5); // at least a few micros should be in the list
		assertTrue(mcus.containsKey("atmega16"));
		assertTrue(mcus.containsValue("ATmega16"));
		assertFalse(mcus.containsKey("avr1"));
		assertFalse(mcus.containsKey(""));
		assertFalse(mcus.containsKey(null));
	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}



}
