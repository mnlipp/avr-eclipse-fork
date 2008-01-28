package de.innot.avreclipse.core.paths;

import static org.junit.Assert.*;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.junit.Test;

import de.innot.avreclipse.core.paths.posix.FindCommandRunner;
import de.innot.avreclipse.core.paths.posix.SystemPathsPosix;
import de.innot.avreclipse.core.paths.win32.SystemPathsWin32;

public class TestSystemPathsPosix {

	@Test
	public void testGetDefault() {
		SystemPathsPosix spp = SystemPathsPosix.getDefault();
		assertNotNull(spp);
	}

	@Test
	public void testGetSystemPath() {
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

	@Test
	public void testExecuteCommand() {
		// This test will only work on Windows
		if (isWindows()) {
			IPath winavrutilsbin = SystemPathsWin32.MAKE.getPath();
			IPath winavr = winavrutilsbin.removeLastSegments(2);
			String command = winavrutilsbin.append("find").toOSString();
			
			// Successful search
			IPath test = SystemPathsPosix.executeCommand(command
			        + " " + winavr.toOSString() + " -path \"*/make.exe\"");
			assertFalse(test.isEmpty());
			assertTrue(test.toFile().getName().equals("make.exe"));
			
			// Failed search
			test = FindCommandRunner.executeCommand(command
			        + " " + winavr.toOSString() + " -path \"*/foobar\"");
			assertTrue(test.isEmpty());
		}
	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}


}
