package de.innot.avreclipse.mbs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AVROptionsManager {

	private static Map<String, String> options = new HashMap<String, String>();
	private static List<IOptionsChangeListener> changeListeners = new ArrayList<IOptionsChangeListener>();

	public static String getOption(String name) {

		synchronized (options) {
			return options.get(name);
		}
	}

	public static void setOption(String option, String value) {
		synchronized (options) {
			options.put(option, value);
		}
		for (IOptionsChangeListener listener : changeListeners) {
			listener.optionChanged(option, value);
		}
	}

	public static void addOptionChangeListener(IOptionsChangeListener listener) {
		changeListeners.add(listener);
	}

}
