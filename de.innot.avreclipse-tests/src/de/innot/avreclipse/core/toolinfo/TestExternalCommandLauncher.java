package de.innot.avreclipse.core.toolinfo;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestExternalCommandLauncher {

	@Test
	public void testExternalCommandLauncher() throws IOException, InterruptedException {
		String command = "echo";
		List<String> arguments;
		ExternalCommandLauncher testlauncher;

		// Test 1: just a simple launch
		arguments = new ArrayList<String>(1);
		arguments.add("test1");
		testlauncher = new ExternalCommandLauncher(command, arguments);
		int result = testlauncher.launch();
		assertEquals("Launcher return code", 0, result);
		List<String> stdout = testlauncher.getStdOut();
		assertEquals("Wrong stdout value", "test1", stdout.get(0));

		// Test 2: the LogEventListener
		arguments.add("test2");
		testlauncher = new ExternalCommandLauncher(command, arguments);
		testlauncher.setCommandOutputListener(new ICommandOutputListener() {
			public synchronized void handleLine(String line, StreamSource source) {
				assertEquals("Wrong Stream", StreamSource.STDOUT, source);
				assertEquals("Wrong line", "test2", line);
			}
		});
		result = testlauncher.launch();
		assertEquals("Launcher return code", 0, result);

		// Test 3: a canceled launch

	}

}
