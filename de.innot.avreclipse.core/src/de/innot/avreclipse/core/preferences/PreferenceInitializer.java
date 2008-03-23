/*******************************************************************************
 * 
 * Copyright (c) 2007,2008 Thomas Holland (thomas@innot.de) and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 * $Id$
 *     
 *******************************************************************************/
package de.innot.avreclipse.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;


/**
 * Class used to initialize default preference values.
 * 
 * <p>
 * This class is called directly from the plugin.xml (in the
 * <code>org.eclipse.core.runtime.preferences</code
 * extension point. It sets default values for the Plugin preferences.
 * </p> 
 * @author Thomas Holland
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// Store default values to default preferences
	 	AVRPathsPreferences.initializeDefaultPreferences();
	 	DatasheetPreferences.initializeDefaultPreferences();
	 	AVRDudePreferences.initializeDefaultPreferences();
	 	
	 	// TODO: get the 2.0.x preferences and update to 2.1
	 	
	}

}
