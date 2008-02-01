package de.innot.avreclipse.core.paths;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.IPath;
import org.junit.Test;

public class TestSystemPathHelper {

	@Test
	public void testInitSystemPaths() {
		try {
			SystemPathHelper.initSystemPaths();
		} catch (Exception e) {
			fail("initSystemPaths() throws Exception" + e.toString());
		}
	}

	@Test
	public void testGetPath() {
		AVRPath[] allpaths = AVRPath.values();
		
		for (AVRPath avrpath : allpaths) {
			IPath path = SystemPathHelper.getPath(avrpath);
			assertNotNull(avrpath.getName() + "returned null path", path);
			if (!avrpath.isOptional()) {
				assertFalse(avrpath.getName() + " has empty path", path.isEmpty());
			}
		}
	}
}
