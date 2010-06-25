package de.innot.avreclipse.core.paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.IPath;
import org.junit.Test;

public class TestSystemPathHelper {

	@Test
	public void testGetPath() {
		AVRPath[] allpaths = AVRPath.values();

		for (AVRPath avrpath : allpaths) {
			IPath path = SystemPathHelper.getPath(avrpath, false);
			assertNotNull(avrpath.getName() + "returned null path", path);
			if (!avrpath.isOptional()) {
				assertFalse(avrpath.getName() + " has empty path", path.isEmpty());
			}
		}
	}
}
