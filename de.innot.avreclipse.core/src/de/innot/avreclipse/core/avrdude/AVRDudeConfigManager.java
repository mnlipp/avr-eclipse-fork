/**
 * 
 */
package de.innot.avreclipse.core.avrdude;

import java.util.Map;

import de.innot.avreclipse.core.preferences.AVRDudeManagerPreferences;

/**
 * @author Thomas Holland
 * @since 2.2
 *
 */
public class AVRDudeConfigManager {

	private static Map<String, ProgrammerConfig> fProgrammerConfigs = null;
	
	public static ProgrammerConfig getProgrammerConfig(String name) {
		Map<String, ProgrammerConfig> configs = internalReloadConfigs();
		return configs.get(name);
	}
	
	
	private static Map<String, ProgrammerConfig> internalReloadConfigs() {
	
		if (fProgrammerConfigs != null) {
			return fProgrammerConfigs;
		}
		
		AVRDudeManagerPreferences.
	}
}
