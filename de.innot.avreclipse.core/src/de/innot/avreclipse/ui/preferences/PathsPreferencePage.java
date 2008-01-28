/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.innot.avreclipse.core.preferences.AVRPathsPreferences;

/**
 * Paths Preference page of the AVR Eclipse plugin.
 * 
 * <p>
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * </p>
 */

public class PathsPreferencePage extends FieldEditorPreferencePage implements
        IWorkbenchPreferencePage {

	private IPreferenceStore fPreferenceStore = null;

	public PathsPreferencePage() {
		super(GRID);

		// Get the instance scope path preference store
		fPreferenceStore = AVRPathsPreferences.getPreferenceStore();
		setPreferenceStore(fPreferenceStore);
		setDescription("Path Settings for the AVR Eclipse Plugin");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.FieldEditorPreferencePage#createFieldEditors()
	 */
	@Override
	public void createFieldEditors() {
		// This is very simple, as the preference page has only one control, a
		// custom field editor for the paths. All work is handled there.
		Composite parent = getFieldEditorParent();

		AVRPathsFieldEditor fPaths = new AVRPathsFieldEditor(parent);
		addField(fPaths);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	public void init(IWorkbench workbench) {
		// nothing to init
	}

}
