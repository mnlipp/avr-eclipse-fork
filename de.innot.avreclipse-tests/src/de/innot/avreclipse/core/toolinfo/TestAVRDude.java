/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

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

import de.innot.avreclipse.core.avrdude.AVRDudeException;

/**
 * @author Thomas Holland
 * 
 */
public class TestAVRDude {

	private AVRDude	tool	= null;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		tool = AVRDude.getDefault();
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.AVRDude#getToolPath()}.
	 */
	@Test
	public void testGetToolPath() {
		IPath avrpath = tool.getToolPath();
		assertNotNull("No ToolPath returned", avrpath);
		File avrfile = avrpath.toFile();
		if (isWindows()) {
			// append .exe
			String windowsname = avrfile.getPath() + ".exe";
			avrfile = new File(windowsname);
		}
		assertTrue("Toolpath does not point to an executable file", avrfile.canRead());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.AVRDude#getToolInfo(java.lang.String)}.
	 * 
	 * @throws IOException
	 */
	@Test
	public void testGetMCUList() throws IOException {
		Set<String> mcus = tool.getMCUList();
		assertNotNull(mcus);
		assertTrue(mcus.size() > 5); // at least a few micros should be in
		// the list
		assertTrue(mcus.contains("atmega16"));
		assertFalse(mcus.contains("m16"));
		assertFalse(mcus.contains(""));
		assertFalse(mcus.contains(null));
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.AVRDude#getProgrammersList()}.
	 * 
	 * @throws AVRDudeException
	 */
	@Test
	public void testGetProgrammersList() throws AVRDudeException {
		Set<String> programmers = tool.getProgrammersList();
		assertNotNull(programmers);
		assertTrue(programmers.size() > 5); // at least a few programmers should
		// be in the list
		assertTrue(programmers.contains("stk500")); // default entry for the plugin
		assertTrue(programmers.contains("avrisp")); // last entry
		assertTrue(programmers.contains("c2n232i")); // first entry
		assertTrue(programmers.contains("pony-stk200")); // Bug 1984307
		assertTrue(programmers.contains("dragon_isp")); // Bug 1984307
		assertFalse(programmers.contains(null));
	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}

}
