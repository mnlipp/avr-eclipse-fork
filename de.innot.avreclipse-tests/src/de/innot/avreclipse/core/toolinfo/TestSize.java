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
		assertTrue("Size of wrong type", tool instanceof IToolInfo);
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
		assertTrue(types.contains(IToolInfo.TOOLINFOTYPE_OPTIONS));
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.GCC#getToolInfo(java.lang.String)}.
	 */
	@Test
	public void testGetToolInfo() {
		Map<String, String> options = tool.getToolInfo(IToolInfo.TOOLINFOTYPE_OPTIONS);
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
		for(String option : options.keySet()) {
			System.out.println(option + " = " + options.get(option));
		}
	}


}
