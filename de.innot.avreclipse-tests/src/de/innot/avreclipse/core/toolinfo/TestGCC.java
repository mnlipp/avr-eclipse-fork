/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import static org.junit.Assert.*;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
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
		assertTrue("Toolpath does not point to an executable file", gccfile.canExecute());
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
		assertTrue(mcus.containsValue("atmega16"));
		assertTrue(mcus.containsKey("ATmega16"));
		assertFalse(mcus.containsValue("avr1"));
		assertFalse(mcus.containsValue(""));
		assertFalse(mcus.containsValue(null));
		for(String mcuid : mcus.keySet()) {
			System.out.println(mcuid + " = " + mcus.get(mcuid));
		}
	}


}
