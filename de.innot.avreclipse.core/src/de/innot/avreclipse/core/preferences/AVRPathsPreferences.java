/*******************************************************************************
 * 
 * Copyright (c) 2007, 2008 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.core.preferences;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathManager;
import de.innot.avreclipse.core.paths.SystemPathHelper;

/**
 * This class handles access to the path properties.
 * 
 * These properties are stored per instance, overrideable per project.
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public class AVRPathsPreferences {

	public static final String						KEY_AVR_ID				= "AVRsettings";

	public static final String						KEY_PER_PROJECT			= "perProject";
	private static final boolean					DEFAULT_PER_PROJECT		= false;

	public static final String						KEY_NOSTARTUPSCAN		= "NoScanAtStartup";
	private static final Boolean					DEFAULT_NOSTARTUP_SCAN	= false;

	private static final String						CLASSNAME				= "avrpaths";
	private static final String						QUALIFIER				= AVRPlugin.PLUGIN_ID
																					+ "/"
																					+ CLASSNAME;

	private static IPreferenceStore					fInstanceStore			= null;
	private static Map<IProject, IPreferenceStore>	fProjectStoreMap		= new HashMap<IProject, IPreferenceStore>();

	/**
	 * Gets the instance Path preferences.
	 * 
	 * @return IPreferenceStore with the properties
	 */
	public static IPreferenceStore getPreferenceStore() {
		// The instance Path PreferenceStore is cached
		if (fInstanceStore != null) {
			return fInstanceStore;
		}

		IScopeContext scope = new InstanceScope();
		IPreferenceStore store = new ScopedPreferenceStore(scope, QUALIFIER);

		fInstanceStore = store;
		return store;
	}

	/**
	 * Gets the project Path properties.
	 * 
	 * The returned store is backed by the instance properties, so properties not set for the
	 * project will fall back to the instance settings.
	 * 
	 * @param project
	 *            The project for which to get the properties
	 * @return IPreferenceStore with the properties
	 */
	public static IPreferenceStore getPreferenceStore(IProject project) {
		Assert.isNotNull(project);

		IPreferenceStore cachedstore = fProjectStoreMap.get(project);
		if (cachedstore != null) {
			return cachedstore;
		}
		IScopeContext projectscope = new ProjectScope(project);
		ScopedPreferenceStore store = new ScopedPreferenceStore(projectscope, QUALIFIER);

		store.setSearchContexts(new IScopeContext[] { projectscope, new InstanceScope() });

		fProjectStoreMap.put(project, store);

		return store;
	}

	/**
	 * Saves the changed properties.
	 * 
	 * This has to be called to make any changes to the PreferenceStore persistent.
	 * 
	 * @param store
	 * @throws IOException
	 */
	public static void savePreferences(IPreferenceStore store) throws IOException {
		Assert.isTrue(store instanceof ScopedPreferenceStore);
		((ScopedPreferenceStore) store).save();
	}

	/**
	 * Gets the default Target Hardware properties
	 * 
	 * @return
	 */
	public static IEclipsePreferences getDefaultPreferences() {
		IScopeContext scope = new DefaultScope();
		return scope.getNode(QUALIFIER);
	}

	/**
	 * Check the value of the no startup path scan flag.
	 * <p>
	 * This flag is set in the preferences to indicate, that the plugin should not - at plugin
	 * startup - scan the system paths.
	 * </p>
	 * <p>
	 * Even with the flag set the Plugin will still search each system path once to fill the
	 * persistent cache.
	 * </p>
	 * 
	 * @return <code>true</code> if the system paths should not be searched.
	 */
	public static boolean noStartupPathScan() {
		IPreferenceStore store = getPreferenceStore();
		return store.getBoolean(KEY_NOSTARTUPSCAN);
	}

	public static void scanAllPaths() {

		if (noStartupPathScan()) {
			return;
		}

		Job scanjob = new Job("System Paths Scan") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					AVRPath[] allpaths = AVRPath.values();
					monitor.beginTask("Searching for System Paths", allpaths.length);
					for (AVRPath path : allpaths) {
						SystemPathHelper.getPath(path, true);
						monitor.worked(1);
					}
				} finally {
					monitor.done();
				}

				return Status.OK_STATUS;
			}
		};
		scanjob.setSystem(true);
		scanjob.setPriority(Job.LONG);
		scanjob.schedule();
	}

	/**
	 * Initialize the default property values.
	 * 
	 * This is called from {@link de.innot.avreclipse.core.preferences.PreferenceInitializer}
	 */
	public static void initializeDefaultPreferences() {
		IEclipsePreferences prefs = getDefaultPreferences();
		prefs.putBoolean(KEY_PER_PROJECT, DEFAULT_PER_PROJECT);
		prefs.putBoolean(KEY_NOSTARTUPSCAN, DEFAULT_NOSTARTUP_SCAN);

		// get all supported path and set the default to System
		// TODO: change this to bundle once bundles are supported
		AVRPath[] allpaths = AVRPath.values();
		for (AVRPath avrpath : allpaths) {
			prefs.put(avrpath.name(), AVRPathManager.SourceType.System.name());
		}
	}

}
