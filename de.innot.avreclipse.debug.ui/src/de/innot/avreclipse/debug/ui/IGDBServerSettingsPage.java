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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.swt.widgets.Composite;

/**
 * Interface for gdbserver settings pages.
 * <p>
 * Implemented by "settingspage" extension points.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface IGDBServerSettingsPage {

	/**
	 * Get the description of the settings page.
	 * <p>
	 * By default this is the value of the "description" attribute in the extension point. It is
	 * used for the selection of the gdbserver.
	 * </p>
	 * </p>
	 * 
	 * @return Description string
	 */
	public String getDescription();

	/**
	 * Set the description for the settings page.
	 * <p>
	 * This method should only be called when reading the extension point.
	 * </p>
	 * 
	 * @param description
	 *            String
	 */
	public void setDescription(String description);

	/**
	 * Get the id of the GDBServer for which this settings page applies.
	 * <p>
	 * The default value this is the value of the "id" attribute in the extension point.
	 * </p>
	 * 
	 * @return gdbserver id value
	 */
	public String getGDBServerID();

	/**
	 * Set the gdbserver launch configuration id associated with this settings page.
	 * <p>
	 * This method should only be called when reading the extension point.
	 * </p>
	 * 
	 * @param gdbserverid
	 */
	public void setGDBServerID(String gdbserverid);

	/**
	 * Initializes the given launch configuration with default settings for this gdbserver. This
	 * method is called when a new launch configuration is created such that the configuration can
	 * be initialized with meaningful values. This method may be called before this pages control is
	 * created.
	 * 
	 * @param configuration
	 *            launch configuration
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration);

	/**
	 * Sets the context of this page.
	 * <p>
	 * This is the first method called on a GDBServer settings page, and marks the beginning of this
	 * tab's lifecycle. The given context can be used to communicate with the parent tab, e.g. to
	 * set error messages for the user.
	 * </p>
	 * 
	 * @param parent
	 *            the host <code>ILaunchConfigurationTab</code> tab.
	 */
	public void setContext(IGDBServerSettingsContext parent);

	/**
	 * Creates the top level control for the gdbserver settings page under the given parent
	 * composite.
	 * 
	 * @param parent
	 *            a <code>PageBook</code> control as part of the GDBServer settings tab.
	 */
	public void createSettingsPage(Composite parent);

	/**
	 * Initializes this gdbserver settings page with values from the given launch configuration.
	 * <p>
	 * This method is called when a configuration is selected to view or edit, after this pages
	 * control has been created.
	 * </p>
	 * 
	 * @param configuration
	 *            launch configuration
	 */
	public void initializeFrom(ILaunchConfiguration configuration);

	/**
	 * Copies values from this page into the given launch configuration.
	 * 
	 * @param configuration
	 *            launch configuration
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration);

	/**
	 * Returns whether this page is in a valid state in the context of the specified launch
	 * configuration.
	 * 
	 * This information is typically used by the launch configuration dialog to decide when it is
	 * okay to launch.
	 * 
	 * @param configuration
	 * @return
	 */
	public boolean isValid(ILaunchConfiguration configuration);

	/**
	 * Notifies this gdbserver settings page that it has been disposed.
	 * <p>
	 * Marks the end of this page's lifecycle, allowing this tab to perform any cleanup required.
	 * </p>
	 */
	public void dispose();

}
