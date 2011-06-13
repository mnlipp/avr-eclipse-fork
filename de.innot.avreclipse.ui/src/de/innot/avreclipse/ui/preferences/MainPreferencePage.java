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
package de.innot.avreclipse.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * Main Preference page of the AVR Eclipse plugin.
 * 
 * <p>
 * For the time being this page is empty and will only contain descriptions of the subpages. This is
 * against the recommendation of the Eclipse Style Guide, but I currently do not have any plugin
 * global settings which should be on the main page.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.3
 */

public class MainPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {

	public MainPreferencePage() {
		super();

		setDescription("AVR Eclipse Plugin Preferences");
	}

	@Override
	protected Control createContents(Composite parent) {

		Composite content = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		content.setLayout(layout);
		content.setFont(parent.getFont());

		Label filler = new Label(content, SWT.NONE);
		filler.setText("");

		Label label = createDescriptionLabel(content);
		label
				.setText("Please select one of the sub-pages to change the settings for the AVR plugin.");

		filler = new Label(content, SWT.NONE);
		filler.setText("");

		createNoteComposite(JFaceResources.getDialogFont(), content, "AVRDude:",
				"Manage the configuration of programmer devices for avrdude.\n");

		createNoteComposite(JFaceResources.getDialogFont(), content, "Paths:",
				"Manage the paths to the external tools and files used by the plugin.\n");

		return content;

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
