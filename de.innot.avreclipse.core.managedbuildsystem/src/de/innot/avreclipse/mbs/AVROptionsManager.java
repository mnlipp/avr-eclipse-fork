/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
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
