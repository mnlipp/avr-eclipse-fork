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

import org.eclipse.cdt.debug.mi.core.IMILaunchConfigurationConstants;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

import de.innot.avreclipse.debug.core.IAVRGDBConstants;

/**
 * The LaunchConfigurationTab for general settings of the debugger
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TabImageLoader extends AbstractLaunchConfigurationTab implements IAVRGDBConstants,
		IMILaunchConfigurationConstants {

	private static final String	TAB_NAME	= "Image Loader";

	// The GUI Elements

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
		return AVRGDBImages.getImage(AVRGDBImages.TAB_DEBUGGER_IMG);
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.
	 * ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
	}

	/*
	 * (non-Javadoc)
	 * @seeorg.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.
	 * ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
	}

}
