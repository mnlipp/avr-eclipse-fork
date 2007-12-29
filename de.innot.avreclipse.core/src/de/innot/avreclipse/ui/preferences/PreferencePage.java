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
 * $Id: PreferencePage.java 21 2007-11-28 00:52:07Z thomas $
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui.preferences;

import java.io.File;

import org.eclipse.cdt.utils.ui.controls.ControlFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import de.innot.avreclipse.AVRPluginActivator;

/**
 * Main (and only) Preference page of the AVR Eclipse plugin.
 * 
 * <p>
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>,
 * we can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * </p>
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 * </p>
 */

public class PreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage,
		PreferenceConstants {

	// The GUI FieldEditor Widgets
	private BooleanFieldEditor fUseDefaultPaths = null;
	private Group fPathsGroup = null;
	private AVRiohFileFieldEditor fAVR_io_h_file = null;
//	private AVRpdfDirectoryFieldEditor fAVRpdfFolder = null;

	/**
	 * Extends the FileFieldEditor Class to do some custom validation of the
	 * value.
	 */
	private class AVRiohFileFieldEditor extends FileFieldEditor {

		public AVRiohFileFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		/**
		 * Test if the new text value points to the avr/io.h file
		 */
		@Override
		protected boolean checkState() {
			// the superclass checks if the file actually exists
			if (!super.checkState())
				return false;
			String filename = getStringValue();
			File file = new File(filename);

			// Test if the file is readable, has the name "io.h" and the parent
			// directory of "avr"
			if (file.canRead()) {
				if ("io.h".equals(file.getName())) {
					String parent = file.getParent();
					if ((parent != null) && (parent.endsWith("avr"))) {
						clearErrorMessage();
						return true;
					}
				}
			}
			// nope, does not seem to be valid
			// the super-superclass (StringFieldEditor) will take care of
			// setting the error string
			showErrorMessage();
			return false;
		}
	}

	/**
	 * Extends the DirectoryFieldEditor Class to do some custom validation of
	 * the value.
	 */
	@SuppressWarnings("unused")
	private class AVRpdfDirectoryFieldEditor extends DirectoryFieldEditor {

		public AVRpdfDirectoryFieldEditor(String name, String labelText, Composite parent) {
			super(name, labelText, parent);
		}

		/**
		 * Test if the new text value points to the partdefinitionsfile folder
		 */
		@Override
		protected boolean doCheckState() {
			// the superclass checks if the directory actually exists
			if (!super.doCheckState())
				return false;
			String dirname = getStringValue();
			File pdfdir = new File(dirname);

			String[] filelist = pdfdir.list();
			for (int i = 0; i < filelist.length; i++) {
				if (filelist[i].endsWith(".xml")) {
					// TODO: could test if the xml file is actually a
					// partdescriptionfile
					// (the second line of the file is "<AVRPART>")
					return true;
				}
			}
			// nope, does not seem to be valid
			// the super-superclass (StringFieldEditor) will take care of
			// setting the error string
			return false;
		}
	}

	public PreferencePage() {
		super(GRID);

		// maybe the next lines should be moved to init(), but it works so far.
		setPreferenceStore(AVRPluginActivator.getDefault().getPreferenceStore());
		setDescription("Preferences for the AVR Eclipse Plugin");
	}

	/**
	 * Creates the field editors.
	 * 
	 */
	@Override
	public void createFieldEditors() {
		Composite parent = getFieldEditorParent();

		Group group = ControlFactory.createGroup(parent, "AVR Device View Sources", 1);

		// this composite is just to ensure a nice padding to the group border
		// haven't been able to get any results from changing the Layout /
		// LayoutData of the group Composite
		Composite composite = ControlFactory.createComposite(group, 1);
		

		// Create the avr/io.h file selector
		fAVR_io_h_file = new AVRiohFileFieldEditor(PREF_DEVICEVIEW_AVR_IO_H, "<avr/io.h> file:",
				composite);
		fAVR_io_h_file.getTextControl(composite).setToolTipText(
				"Set the location of the <avr/io.h> include file.");
		fAVR_io_h_file.setErrorMessage("Not a suitable <avr/io.h> file");

/*	commented out until this has actually been implemented
		// create the partdescriptionfiles Folder selector
		fAVRpdfFolder = new AVRpdfDirectoryFieldEditor(PREF_DEVICEVIEW_AVRPDF_PATH,
				"PartDescriptionFiles Folder:", composite);
		fAVRpdfFolder
				.getTextControl(composite)
				.setToolTipText(
						"Set the location of the PartDescriptionFiles folder. (Part of AVR Tools / AVR Studio from Atmel.com)");
		fAVRpdfFolder
				.setErrorMessage("PartDescriptionFiles Folder does not seem to contain PartDefinitionFiles");

*/
		// add the fields to the preference page
		addField(fAVR_io_h_file);
//		addField(fAVRpdfFolder);

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
