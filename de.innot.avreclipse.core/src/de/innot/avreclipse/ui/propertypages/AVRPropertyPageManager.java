/*******************************************************************************
 * 
 * Copyright (c) 2008 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.ui.propertypages;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.core.properties.AVRProjectProperties;
import de.innot.avreclipse.core.properties.ProjectPropertyManager;

/**
 * Manages the list of {@link AbstractAVRPage} for a property dialog.
 * <p>
 * This manager keeps track of all open property pages. Each page registers
 * itself by calling the {@link #getPropertyManager(PropertyPage, IProject)}
 * method.
 * </p>
 * <p>
 * All data and all methods are static. This is no problem, because the property
 * dialog is modal, so only one property dialog = one session can be open at a
 * time.
 * </p>
 * <p>
 * This class is very similar to and supplements the
 * <code>CDTPropertyManager</class>, which manages the list of all CDT <code>AbstractPage</code>s.</p> 
 * 
 * @see CDTPropertyManager
 * 
 * @author Thomas Holland
 * @since 2.2
 *
 */
public class AVRPropertyPageManager {

	/** List of all open Property Pages */
	private static List<PropertyPage> fPages = new ArrayList<PropertyPage>();

	/** The Project for which the properties are edited */
	private static IProject fProject;

	/** The Project Property Manager for the current project */
	private static ProjectPropertyManager fProperties;

	/**
	 * Gets the the {@link ProjectPropertyManager} for the project and registers
	 * the given page in the list of property pages.
	 * <p>
	 * On the first call to this method for a new or a different project, a new
	 * session is initiated.
	 * </p>
	 * 
	 * @param page
	 *            <code>AbstractAVRPropertyPage</code> to register in this
	 *            manager.
	 * @param project
	 *            The current project.
	 * @return The <code>ProjectPropertyManager</code> for the given project.
	 */
	public static ProjectPropertyManager getPropertyManager(PropertyPage page, IProject project) {

		// If no pages registered start a new static session
		if (fPages.size() == 0) {
			fProject = null;
			fProperties = null;
		}

		// Remember the page and add dispose listener to the page so we know
		// when it is closed
		if (!fPages.contains(page)) {
			fPages.add(page);
			page.getControl().addDisposeListener(fDisposeListener);
		}

		// Check if a new project has been selected
		if (fProject == null || !project.equals(fProject)) {
			fProject = project;
			fProperties = null;
		}

		// Check if a new properties object is required
		if (fProperties == null) {
			fProperties = ProjectPropertyManager.getPropertyManager(project);
		}

		return fProperties;
	}

	/**
	 * Save all modifications to the properties to the properties storage and
	 * remove the page from the manager.
	 * <p>
	 * Also the list of "per config" properties is synchronized with the list of
	 * existing build configurations, so AVR properties for deleted build
	 * configurations will be deleted as well.
	 * </p>
	 * 
	 * @param page
	 *            Originating page.
	 * @param allconfigs
	 *            Array with all build configuration description objects.
	 */
	public static void performOK(PropertyPage page, ICConfigurationDescription[] allconfigs) {
		try {
			fProperties.save();
		} catch (BackingStoreException e) {
			// TODO Throw an Exception to the UI code to display an error
			// message.
			e.printStackTrace();
		}

		if (allconfigs != null) {
			// Convert the given array of ConfigurationDescriptions into a list
			// of
			// configuration id values and call the
			// ProjectPropertyManager.sync() method to
			// remove configuration properties for deleted build configurations.
			List<String> allcfgids = new ArrayList<String>(allconfigs.length);
			for (ICConfigurationDescription cfgd : allconfigs) {
				IConfiguration cfg = ManagedBuildManager.getConfigurationForDescription(cfgd);
				allcfgids.add(cfg.getId());
			}

			fProperties.sync(allcfgids);
		}

		removePage(page);

	}

	/**
	 * Cancel all modifications and remove the page from the manager.
	 * 
	 * @param page
	 *            Originating page.
	 */
	public static void performCancel(PropertyPage page) {
		fProperties.reload();
		removePage(page);
	}

	/**
	 * Remove the given <code>AbstractAVRPage</code> from the manager.
	 * <p>
	 * Once the last page has been removed from this manager, the current
	 * session is closed.
	 * </p>
	 * 
	 * @param page
	 *            Page to remove from the manager.
	 */
	private static void removePage(PropertyPage page) {

		if (fPages.contains(page)) {
			fPages.remove(page);
		}

		if (fPages.size() == 0) {
			// all pages have been disposed
			fProperties = null;
			fProject = null;
		}

	}

	/**
	 * @return A <code>List</code> of all managed pages.
	 */
	public static List<PropertyPage> getPages() {
		return fPages;
	}

	public static AVRProjectProperties getConfigProperties(ICResourceDescription resdesc) {
		IConfiguration buildcfg = getConfigFromConfigDesc(resdesc);
		return fProperties.getConfigurationProperties(buildcfg);
	}

	public static AVRProjectProperties getConfigPropertiesNoCache(ICResourceDescription resdesc) {
		IConfiguration buildcfg = getConfigFromConfigDesc(resdesc);
		return fProperties.getConfigurationProperties(buildcfg, false, true);
	}

	/**
	 * Get the current per project properties.
	 * 
	 * @return
	 */
	public static AVRProjectProperties getProjectProperties() {
		return fProperties.getProjectProperties();
	}

	/**
	 * Convenience method to get an <code>IConfiguration</code> from an
	 * <code>ICResourceDescription</code>
	 * 
	 * @param resdesc
	 *            An <code>ICResourceDescription</code>
	 * @return <code>IConfiguration</code> associated with the given
	 *         Description.
	 */
	private static IConfiguration getConfigFromConfigDesc(ICResourceDescription resdesc) {
		ICConfigurationDescription cfgDes = resdesc.getConfiguration();
		IConfiguration conf = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		return conf;
	}

	/**
	 * Listener to remove disposed pages from the manager.
	 */
	private static DisposeListener fDisposeListener = new DisposeListener() {
		public void widgetDisposed(DisposeEvent e) {
			Widget w = e.widget;
			for (PropertyPage page : fPages) {
				if (page.getControl().equals(w)) {
					fPages.remove(page);
					break;
				}
			}

			if (fPages.size() == 0) {
				// all pages have been disposed
				fProperties = null;
				fProject = null;
			}
		}
	};
}
