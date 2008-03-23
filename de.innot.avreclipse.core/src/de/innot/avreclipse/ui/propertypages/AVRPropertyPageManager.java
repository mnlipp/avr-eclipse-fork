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
import org.eclipse.core.resources.IProject;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.dialogs.PropertyPage;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.core.preferences.AVRProjectProperties;
import de.innot.avreclipse.core.preferences.ProjectPropertyManager;

/**
 * Manages the list of {@link AbstractAVRPage} for a property dialog.
 * <p>
 * This manager keeps track of all open property pages. Each page registers
 * itself by calling the {@link #getProjectProperties(PropertyPage, IProject)}
 * method.
 * </p>
 * <p>
 * All data and all methods are static. This is no problem, because the property
 * dialog is modal, so only one property dialog = one session can be open at a
 * time.
 * </p>
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

	public static ProjectPropertyManager getProjectProperties(PropertyPage page, IProject project) {

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

	public static void performOK(PropertyPage page, ICConfigurationDescription[] allconfigs) {
		try {
			fProperties.save();
		} catch (BackingStoreException e) {
			// TODO Auto-generated catch block
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

	public static void performCancel(PropertyPage page) {
		fProperties.reload();
		removePage(page);
	}

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

	private static IConfiguration getConfigFromConfigDesc(ICResourceDescription resdesc) {
		ICConfigurationDescription cfgDes = resdesc.getConfiguration();
		IConfiguration conf = ManagedBuildManager.getConfigurationForDescription(cfgDes);
		return conf;
	}

	// Removes disposed items from list
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
