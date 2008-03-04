/**
 * 
 */
package de.innot.avreclipse.core.avrdude;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.core.preferences.AVRDudePreferences;

/**
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class ProgrammerConfig implements IPropertyChangeListener {

	private Preferences fConfigPrefs;

	private String fName;
	public final static String KEY_NAME = "name";

	private String fDescription;
	public final static String KEY_DESCRIPTION = "description";

	private String fProgrammer;
	public final static String KEY_PROGRAMMER = "programmer";

	private String fPort;
	public final static String KEY_PORT = "port";

	private String fBaudrate;
	public final static String KEY_BAUDRATE = "baudrate";

	private String fExitReset;
	public final static String KEY_EXITSPEC_RESET = "ppresetline";

	private String fExitVcc;
	public final static String KEY_EXITSPEC_VCC = "ppvccline";

	public ProgrammerConfig() {
		this("Default:stk500v2");
		fDescription = "Default AVRDude Programmer Configuration. Change for your setup";

		// The default is stk500v2 (because I happen to have one) and all other
		// options at the avrdude default.
		fProgrammer = "stk500v2";
		fPort = fBaudrate = fExitReset = fExitVcc = "";

	}

	public ProgrammerConfig(String name) {
		fName = name;
		fConfigPrefs = AVRDudePreferences.getConfigPreferences(name);
		readConfig();
	}

	public ProgrammerConfig(ProgrammerConfig config) {
		fName = config.fName;
		fConfigPrefs = config.fConfigPrefs;
		fDescription = config.fDescription;
		fProgrammer = config.fProgrammer;
		fPort = config.fPort;
		fBaudrate = config.fBaudrate;
		fExitReset = config.fExitReset;
		fExitVcc = config.fExitVcc;
	}

	public void save() throws BackingStoreException {
		// write all values to the preferences
		fConfigPrefs.put(KEY_DESCRIPTION, fDescription);
		fConfigPrefs.put(KEY_PROGRAMMER, fProgrammer);
		fConfigPrefs.put(KEY_PORT, fPort);
		fConfigPrefs.put(KEY_BAUDRATE, fBaudrate);
		fConfigPrefs.put(KEY_EXITSPEC_RESET, fExitReset);
		fConfigPrefs.put(KEY_EXITSPEC_VCC, fExitVcc);

		// flush the Preferences to the persistent storage
		fConfigPrefs.flush();
	}

	public List<String> getArguments() {

		List<String> args = new ArrayList<String>();

		args.add("-c" + fProgrammer);

		if (fPort.length() > 0) {
			args.add("-P" + fPort);
		}

		if (fBaudrate.length() > 0) {
			args.add("-b" + fBaudrate);
		}

		StringBuffer exitspec = new StringBuffer();
		if (fExitReset.length() > 0) {
			exitspec.append(fExitReset);
		}
		if (fExitVcc.length() > 0) {
			if (fExitReset.length() > 0) {
				exitspec.append(",");
			}
			exitspec.append(fExitVcc);
		}
		if (exitspec.length() > 0) {
			args.add("-E" + exitspec.toString());
		}
		return args;
	}

	public void setName(String name) {
		fName = name;
		// TODO: change the preference node
	}
	public String getName() {
		return fName;
	}

	public void setDescription(String description) {
		fDescription = description;
	}

	public String getDescription() {
		return fDescription;
	}

	public void setProgrammer(String programmer) {
		fProgrammer = programmer;
	}

	public String getProgrammer() {
		return fProgrammer;
	}

	public void setPort(String port) {
		fPort = port;
	}

	public String getPort() {
		return fPort;
	}

	public void setBaudrate(String baudrate) {
		fBaudrate = baudrate;
	}

	public String getBaudrate() {
		return fBaudrate;
	}

	public void setExitspecResetline(String resetline) {
		fExitReset = resetline;
	}

	public String getExitspecResetline() {
		return fExitReset;
	}

	public void setExitspecVCCline(String vccline) {
		fExitVcc = vccline;
	}

	public String getExitspecVCCline() {
		return fExitVcc;
	}

	private void readConfig() {
		fDescription = fConfigPrefs.get(KEY_DESCRIPTION, "");
		fProgrammer = fConfigPrefs.get(KEY_PROGRAMMER, "");
		fPort = fConfigPrefs.get(KEY_PORT, "");
		fBaudrate = fConfigPrefs.get(KEY_BAUDRATE, "");
		fExitReset = fConfigPrefs.get(KEY_EXITSPEC_RESET, "");
		fExitVcc = fConfigPrefs.get(KEY_EXITSPEC_VCC, "");

	}

	public void propertyChange(PropertyChangeEvent event) {
		String property = event.getProperty();

		if (KEY_NAME.equals(property)) {
			fName = (String) event.getNewValue();
		} else if (KEY_DESCRIPTION.equals(property)) {
			fDescription = (String) event.getNewValue();
		} else if (KEY_PROGRAMMER.equals(property)) {
			fProgrammer = (String) event.getNewValue();
		} else if (KEY_PORT.equals(property)) {
			fPort = (String) event.getNewValue();
		} else if (KEY_BAUDRATE.equals(property)) {
			fBaudrate = (String) event.getNewValue();
		} else if (KEY_EXITSPEC_RESET.equals(property)) {
			fExitReset = (String) event.getNewValue();
		} else if (KEY_EXITSPEC_VCC.equals(property)) {
			fExitVcc = (String) event.getNewValue();
		}
	}
}
