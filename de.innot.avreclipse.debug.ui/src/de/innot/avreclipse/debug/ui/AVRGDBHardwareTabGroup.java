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

import org.eclipse.cdt.launch.ui.CMainTab;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.CommonTab;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.debug.ui.sourcelookup.SourceLookupTab;

/**
 * The TabGroup for the AVRDGB Hardware launch Configuration.
 * <p>
 * This TabGroup has 6 Tabs:
 * <ol>
 * <li>The Main Tab: uses the standard CDT tab to select the project</li>
 * <li>The Debugger Tab: Some general information incl. the gdb command (avr-gdb)</li>
 * <li>The Startup Tab: All settings to initialize the debug session</li>
 * <li>The Image Loader Tab: Selection of the image load method to use</li>
 * <li>The GDBServer Tab: Selection of and settings for the gdbserver (avarice or simulavr)</li>
 * <li>The Source Lookup Tab: Standard Eclipse Debug issue</li>
 * <li>The Common Tab: Again, standard Eclipse Debug style</li>
 * </ol>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AVRGDBHardwareTabGroup extends AbstractLaunchConfigurationTabGroup {

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTabGroup#createTabs(org.eclipse.debug.ui.
	 * ILaunchConfigurationDialog, java.lang.String)
	 */
	public void createTabs(ILaunchConfigurationDialog dialog, String mode) {

		ILaunchConfigurationTab[] tabs = new ILaunchConfigurationTab[] {
				new CMainTab(CMainTab.DONT_CHECK_PROGRAM), //
				new TabDebugger(), //
				new TabImageLoader(), //
				new TabGDBServer(), //
				new SourceLookupTab(), //
				new CommonTab() };
		setTabs(tabs);

	}

}
