/**
 * 
 */
package de.innot.avreclipse.core.avrdude;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class ProgrammerConfig {

	private IPreferenceStore fStore;

	private String fID;
	public final static String KEY_ID = "id";

	private String fName;
	public final static String KEY_NAME = "name";

	private String fProgrammer;
	public final static String KEY_PROGRAMMER = "programmer";

	private String fPort;
	public final static String KEY_PORT = "port";

	private String fBaudrate;
	public final static String KEY_BAUDRATE = "baudrate";

	private Boolean fExitSpec;
	public final static String KEY_EXITSPEC = "setParallelPortOnExit";

	private String fExitReset;
	public final static String KEY_EXITSPEC_RESET = "reset";

	private String fExitVcc;
	public final static String KEY_EXITSPEC_VCC = "vcc";

	
	
	public List<String> getArguments() {

		List<String> args = new ArrayList<String>();

		args.add("-c"+fStore.getString(KEY_PROGRAMMER));

		String port = fStore.getString(KEY_PORT);
		if (!port.isEmpty()) {
			args.add("-P"+port);
		}
		
		String baudrate = fStore.getString(KEY_BAUDRATE);
		if (!baudrate.isEmpty()) {
			args.add("-b"+baudrate);
		}

		Boolean exitspec = fStore.getBoolean(KEY_EXITSPEC);
		if (exitspec) {
			args.add("-E"+fStore.getString(KEY_EXITSPEC_RESET)+","+fStore.getString(KEY_EXITSPEC_VCC));
		}
		
		return args;
	}

}
