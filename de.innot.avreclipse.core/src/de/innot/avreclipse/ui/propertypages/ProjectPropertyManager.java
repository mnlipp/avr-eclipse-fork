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

import java.io.IOException;
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

import de.innot.avreclipse.core.preferences.AVRProjectProperties;
import de.innot.avreclipse.core.preferences.ProjectProperties;

/**
 * Manages the list of {@link AVRConfigurationProperties} for a project.
 * <p>
 * This manager is used in conjunction with the {@link AbstractAVRPage}
 * PropertyPages to maintain a list of the per Project and per Configuration
 * properties.
 * </p>
 * <p>
 * 
 * 
 * @author Thomas Holland
 * 
 */
public class ProjectPropertyManager {

	private static List<PropertyPage> fPages = new ArrayList<PropertyPage>();
	private static IProject fProject;
	private static ProjectProperties fProperties;

	public static ProjectProperties getProjectProperties(PropertyPage page, IProject project) {

		// If no pages registered start a new static session
		if (fPages.size() == 0) {
			fProject = null;
			fProperties = null;
		}

		// Remember the page and add dispose listener to the page so we know
		// when it is closed
		fPages.add(page);
		page.getControl().addDisposeListener(fDisposeListener);

		// Check if a new project has been selected
		if (fProject == null || !project.equals(fProject)) {
			fProject = project;
			fProperties = null;
		}

		// Check if a new properties object is required
		if (fProperties == null) {
			fProperties = new ProjectProperties(project);
		}

		return fProperties;
	}

	public static void performOK(PropertyPage page) {
		try {
			fProperties.save();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		removePage(page);
	}

	public static void performCancel(PropertyPage page) {
		removePage(page);
	}

	private static void removePage(PropertyPage page) {

		if (fPages.contains(page)) {
			fPages.remove(page);
		}

		if (fPages.size() == 0) {
			// all pages have been disposed
			fProject = null;
			fProperties = null;
		}

	}

	public static List<PropertyPage> getPages() {
		return fPages;
	}

	public static AVRProjectProperties getConfigProperties(ICResourceDescription resdesc) {

		IConfiguration buildcfg = getConfigFromConfigDesc(resdesc);
		return fProperties.getPropsForConfig(buildcfg);

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
				fProject = null;
				fProperties = null;
			}
		}
	};
}
