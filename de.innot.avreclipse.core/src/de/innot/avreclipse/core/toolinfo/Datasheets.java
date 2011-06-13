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
package de.innot.avreclipse.core.toolinfo;

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;

import de.innot.avreclipse.core.IMCUProvider;
import de.innot.avreclipse.core.preferences.DatasheetPreferences;

/**
 * This class handles the Datasheets.
 * <p>
 * This class has two main functions:
 * <ol>
 * <li>It maps the {@link DatasheetPreferences} to the {@link IMCUProvider} Interface.</li>
 * <li>It manages the access to the actual Datasheet files.</li>
 * </ol>
 * Datasheets can be accessed with the {@link #getFile(String, IProgressMonitor)} method. This
 * method will download the file from the URL stored in the preferences, and store it in a cache for
 * later access.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class Datasheets implements IMCUProvider {

	private static Datasheets	fInstance			= null;

	private IPreferenceStore	fPreferenceStore	= null;

	/**
	 * Get the default instance of the Datasheets class
	 */
	public static Datasheets getDefault() {
		if (fInstance == null)
			fInstance = new Datasheets();
		return fInstance;
	}

	// private constructor to prevent instantiation
	private Datasheets() {

		// Get the preference store for the datasheets
		fPreferenceStore = DatasheetPreferences.getPreferenceStore();
	}

	//
	// Methods of the IMCUProvider Interface
	//

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUInfo(java.lang.String)
	 */
	public String getMCUInfo(String mcuid) {
		return fPreferenceStore.getString(mcuid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUList()
	 */
	public Set<String> getMCUList() {
		Set<String> allmcus = DatasheetPreferences.getAllMCUs();
		return allmcus;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#hasMCU(java.lang.String)
	 */
	public boolean hasMCU(String mcuid) {
		String value = fPreferenceStore.getString(mcuid);
		return "".equals(value) ? false : true;
	}

}
