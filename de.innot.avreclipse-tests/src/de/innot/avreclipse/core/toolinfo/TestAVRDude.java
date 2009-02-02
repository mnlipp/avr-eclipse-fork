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
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.HostInterface;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.TargetInterface;

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
		List<String> allids = new ArrayList<String>();

		List<IProgrammer> programmers = tool.getProgrammersList();
		assertNotNull(programmers);
		assertTrue(programmers.size() > 5); // at least a few programmers should
		// be in the list

		// Check the resulting list
		for (IProgrammer type : programmers) {
			assertNotNull("Null entry in the list of programmers", type);
			String id = type.getId();
			// Check all fields -- may not be empty
			assertNotNull("Programmer without id", id);
			assertTrue("Programmer with empty id", id.length() > 0);

			assertTrue("Programmer " + id + " without description",
					type.getDescription().length() > 0);
			assertNotNull("Programmer " + id + " has null Host interface", type.getHostInterfaces());
			assertNotNull("Programmer " + id + " has null Target interface", type
					.getTargetInterfaces());

			allids.add(type.getId());
		}

		// Check that the list contains a few selected entries
		assertTrue(allids.contains("stk500")); // default entry for the plugin
		assertTrue(allids.contains("avrisp")); // last entry
		assertTrue(allids.contains("c2n232i")); // first entry
		assertTrue(allids.contains("pony-stk200")); // Bug 1984307
		assertTrue(allids.contains("dragon_isp")); // Bug 1984307
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.AVRDude#getProgrammersList(TargetInterface)} .
	 * 
	 * @throws AVRDudeException
	 */
	@Test
	public void testGetHostTargetInterface() throws AVRDudeException {
		final Object[][] testvalues = {//
		{ "butterfly", 1, HostInterface.SERIAL, TargetInterface.BOOTLOADER },
				{ "avr109", 1, HostInterface.SERIAL, TargetInterface.BOOTLOADER },
				{ "avr910", 1, HostInterface.SERIAL, TargetInterface.BOOTLOADER },
				{ "dragon_dw", 1, HostInterface.USB, TargetInterface.DW },
				{ "jtag2dw", 2, HostInterface.SERIAL, TargetInterface.DW },
				{ "dragon_pp", 1, HostInterface.USB, TargetInterface.PP },
				{ "stk500pp", 1, HostInterface.SERIAL, TargetInterface.PP },
				{ "stk600pp", 1, HostInterface.USB, TargetInterface.PP },
				{ "dragon_hvsp", 1, HostInterface.USB, TargetInterface.HVSP },
				{ "stk500hvsp", 1, HostInterface.SERIAL, TargetInterface.HVSP },
				{ "stk600hvsp", 1, HostInterface.USB, TargetInterface.HVSP },
				{ "dragon_jtag", 1, HostInterface.USB, TargetInterface.JTAG },
				{ "jtag1", 2, HostInterface.SERIAL, TargetInterface.JTAG },
				{ "jtag2", 2, HostInterface.SERIAL, TargetInterface.JTAG },
				{ "xil", 1, HostInterface.PARALLEL, TargetInterface.JTAG },
				{ "dragon_isp", 1, HostInterface.USB, TargetInterface.ISP },
				{ "jtag2isp", 2, HostInterface.SERIAL, TargetInterface.ISP },
				{ "bsd", 1, HostInterface.PARALLEL, TargetInterface.ISP },
				{ "frank-stk200", 1, HostInterface.PARALLEL, TargetInterface.ISP },
				{ "ponyser", 1, HostInterface.SERIAL_BB, TargetInterface.ISP },
				{ "c2n232i", 1, HostInterface.SERIAL_BB, TargetInterface.ISP },
				{ "usbasp", 1, HostInterface.USB, TargetInterface.ISP },
				{ "usbtiny", 1, HostInterface.USB, TargetInterface.ISP }

		};

		for (Object[] testdata : testvalues) {
			String avrdudeid = (String) testdata[0];
			int eNumHostInterfaces = (Integer) testdata[1];
			HostInterface eHostInterface = (HostInterface) testdata[2];
			TargetInterface eTargetInterface = (TargetInterface) testdata[3];

			IProgrammer type = AVRDude.getDefault().getProgrammer(avrdudeid);

			assertEquals(avrdudeid + ": # host interfaces", eNumHostInterfaces, type
					.getHostInterfaces().length);
			assertEquals(avrdudeid + ": host interface", eHostInterface,
					type.getHostInterfaces()[0]);
			assertEquals(avrdudeid + ": target interfaces", eTargetInterface, type
					.getTargetInterfaces());
		}
	}

	/**
	 * @return true if running on windows
	 */
	private static boolean isWindows() {
		return (Platform.getOS().equals(Platform.OS_WIN32));
	}

}
