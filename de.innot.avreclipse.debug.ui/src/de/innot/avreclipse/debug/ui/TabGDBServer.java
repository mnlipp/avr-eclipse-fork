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
package de.innot.avreclipse.debug.ui;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.PageBook;

import de.innot.avreclipse.debug.core.IAVRGDBConstants;

/**
 * The LaunchConfigurationTab for for selection of and settings for the AVR gdbserver.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TabGDBServer extends AbstractLaunchConfigurationTab implements IAVRGDBConstants,
		IMILaunchConfigurationConstants, IGDBServerSettingsContext {

	private static final String					TAB_NAME	= "GDBServer";

	// The GUI Elements
	private Combo								fGDBServerSelector;

	private PageBook							fPageBook;
	private Map<String, Composite>				fPages;

	// The data from the extension points
	private Map<String, IGDBServerSettingsPage>	fSettingPages;
	private Map<String, String>					fNameToIdMap;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return TAB_NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return AVRGDBImages.getImage(AVRGDBImages.TAB_GDBSERVER_IMG);
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#setLaunchConfigurationDialog(org.eclipse
	 * .debug.ui.ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);

		// get the contributing extension points, store them and fill the reverse mapping map.
		fSettingPages = AVRGDBUIPlugin.getDefault().getGDBServerSettingsPages();
		fNameToIdMap = new HashMap<String, String>(fSettingPages.size());

		for (String pageid : fSettingPages.keySet()) {
			String name = fSettingPages.get(pageid).getDescription();
			fNameToIdMap.put(name, pageid);
		}
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(ATTR_GDBSERVER_ID, DEFAULT_GDBSERVER_ID);
		configuration.setAttribute(ATTR_GDBSERVER_IP_ADDRESS, DEFAULT_GDBSERVER_IP_ADDRESS);
		configuration.setAttribute(ATTR_GDBSERVER_PORT_NUMBER, DEFAULT_GDBSERVER_PORT_NUMBER);

		// pass the call to all subpages
		for (IGDBServerSettingsPage settingspage : fSettingPages.values()) {
			settingspage.setDefaults(configuration);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {

		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(),
		// IAVRDebugContextIds.GDBSERVER_TAB);
		comp.setLayout(new GridLayout());
		comp.setFont(parent.getFont());

		createSelectorCombo(comp);

		createSeparator(comp, 1);

		createPageBook(comp);

	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.
	 * ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			// First pass the call to all subpages
			for (IGDBServerSettingsPage settingspage : fSettingPages.values()) {
				settingspage.initializeFrom(configuration);
			}

			// Now update the tab:
			// - Change the combo to the selected gdbserver
			// - Show the correct subpage
			String gdbserverID = configuration
					.getAttribute(ATTR_GDBSERVER_ID, DEFAULT_GDBSERVER_ID);
			IGDBServerSettingsPage settingspage = fSettingPages.get(gdbserverID);
			if (settingspage != null) {
				String name = settingspage.getDescription();
				fGDBServerSelector.select(fGDBServerSelector.indexOf(name));

				Composite page = fPages.get(gdbserverID);
				fPageBook.showPage(page);
			}

		} catch (CoreException e) {
			AVRGDBUIPlugin.log(e.getStatus());
		}

	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {

		// Tell all subpages to apply their current settings
		for (IGDBServerSettingsPage settingspage : fSettingPages.values()) {
			settingspage.performApply(configuration);
		}

		String gdbservername = fGDBServerSelector.getText();
		String gdbserverid = fNameToIdMap.get(gdbservername);
		configuration.setAttribute(ATTR_GDBSERVER_ID, gdbserverid);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#dispose()
	 */
	@Override
	public void dispose() {
		// We don't have anything to dispose ourself but maybe the subpages have
		for (IGDBServerSettingsPage settingspage : fSettingPages.values()) {
			settingspage.dispose();
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.
	 * ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {

		// pass the call onto the selected settings page
		if (fGDBServerSelector != null) {
			String name = fGDBServerSelector.getText();
			String id = fNameToIdMap.get(name);
			IGDBServerSettingsPage settingspage = fSettingPages.get(id);
			return settingspage.isValid(launchConfig);
		}

		return true;
	}

	/**
	 * Add the "GDBServer" selector to the parent composite.
	 * 
	 * @param parent
	 */
	private void createSelectorCombo(Composite parent) {

		Composite compo = new Composite(parent, SWT.NONE);
		compo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		compo.setLayout(new GridLayout(2, false));

		// /////////////////////////////////////////////////////////
		//
		// The "GDBServer" Compo
		//
		// /////////////////////////////////////////////////////////

		Label label = new Label(compo, SWT.NONE);
		label.setText("GDBServer type:");
		label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		fGDBServerSelector = new Combo(compo, SWT.READ_ONLY);
		fGDBServerSelector.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// add all gdbservers from the extension points to the list
		Set<String> allnames = fNameToIdMap.keySet();
		String[] entries = allnames.toArray(new String[allnames.size()]);
		fGDBServerSelector.setItems(entries);

		fGDBServerSelector.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				// Show the associated settings page
				String name = fGDBServerSelector.getText();
				String id = fNameToIdMap.get(name);
				Composite page = fPages.get(id);
				fPageBook.showPage(page);

			}
		});

	}

	/**
	 * Add the "GDBServer settings" pagebook to the parent composite.
	 * 
	 * @param parent
	 */
	private void createPageBook(Composite parent) {

		fPageBook = new PageBook(parent, SWT.NONE);
		fPageBook.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Now get all subpages and add them to the PageBook and to the internal page map.
		// If there are no subpages (no extensions found), then show an error page instead.
		if (fSettingPages.size() > 0) {
			fPages = new HashMap<String, Composite>(fSettingPages.size());
			for (String gdbserverid : fSettingPages.keySet()) {
				IGDBServerSettingsPage settingspage = fSettingPages.get(gdbserverid);
				settingspage.setContext(this);
				Composite page = new Composite(fPageBook, SWT.NONE);
				page.setLayout(new GridLayout());
				settingspage.createSettingsPage(page);
				fPages.put(gdbserverid, page);
			}
		} else {
			createErrorPage(fPageBook);
		}
	}

	private void createErrorPage(Composite parent) {
		Composite page = new Composite(parent, SWT.NONE);
		page.setLayout(new GridLayout());

		Label label = new Label(page, SWT.NONE);
		label.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, true, true));
		label.setText("No AVR GDBServers found");
		fPageBook.showPage(page);
		setErrorMessage("No AVR GDBServers found");

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#setErrorMessage(java.lang.String)
	 */
	public void setErrorMessage(String gdbserverid, String message) {

		// Check if the gdbserver is actually selected.
		// If yes then pass the message to the parent and
		// mark the dialog as invalid.
		String name = fGDBServerSelector.getText();
		String id = fNameToIdMap.get(name);
		if (id == null || id.equals(gdbserverid)) {
			// the settings are visible
			super.setErrorMessage(message);

		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getShell()
	 */
	public Control getControl() {
		return super.getControl();
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.debug.ui.IGDBServerSettingsContext#updateDialog()
	 */
	public void updateDialog() {
		updateLaunchConfigurationDialog();
	}
}
