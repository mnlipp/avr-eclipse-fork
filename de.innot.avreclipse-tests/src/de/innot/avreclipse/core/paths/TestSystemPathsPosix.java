package de.innot.avreclipse.core.paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;

import de.innot.avreclipse.core.paths.posix.SystemPathsPosix;

public class TestSystemPathsPosix {

	@Test
	public void testGetDefault() {

		if (isWindows()) return;
		
		IPath path = SystemPathsPosix.getDefault().getSystemPath(AVRPath.AVRGCC);
		assertFalse(path.isEmpty());
	}

	@Test
	public void testGetSystemPath() {
		
		if (isWindows()) return;
		
		SystemPathsPosix spp = SystemPathsPosix.getDefault();
		AVRPath[] allpaths = AVRPath.values();
		for(AVRPath avrpath : allpaths) {
			IPath path = spp.getSystemPath(avrpath);
			assertNotNull(avrpath.getName(), path);
			if (!avrpath.isOptional()) {
				assertFalse(avrpath.getName(), path.isEmpty());
			}
		}
	}


	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}


}
