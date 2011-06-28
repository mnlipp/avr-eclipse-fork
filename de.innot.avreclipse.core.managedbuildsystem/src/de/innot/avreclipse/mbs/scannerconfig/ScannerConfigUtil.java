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
package de.innot.avreclipse.mbs.scannerconfig;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.build.core.scannerconfig.ICfgScannerConfigBuilderInfo2Set;
import org.eclipse.cdt.build.internal.core.scannerconfig.CfgDiscoveredPathManager;
import org.eclipse.cdt.build.internal.core.scannerconfig2.CfgScannerConfigProfileManager;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.resources.ACBuilder;
import org.eclipse.cdt.make.core.scannerconfig.IScannerConfigBuilderInfo2;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollectorCleaner;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredPathInfo;
import org.eclipse.cdt.make.internal.core.scannerconfig.DiscoveredScannerInfoStore;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCProfileInstance;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.properties.ProjectPropertyManager;

/**
 * Some utility methods for working with the discovery process.
 * 
 * @author Thomas Holland
 * @since 2.5
 * 
 */
@SuppressWarnings("restriction")
public class ScannerConfigUtil {

	/**
	 * Clear all discovered symbols and paths for a configuration.
	 * 
	 * @param cfg
	 *            The <code>IConfiguration</code> to be cleaned.
	 * @throws CoreException
	 */
	public static void clearDiscovery(IConfiguration config) throws CoreException {

		// Get the/all CfgInfoContext objects for the given Configuration.
		// (Don't know when / if there are more than one CfgInfoContext objects for a single
		// Configuration)
		ICfgScannerConfigBuilderInfo2Set cbi = CfgScannerConfigProfileManager
				.getCfgScannerConfigBuildInfo(config);
		Map<CfgInfoContext, IScannerConfigBuilderInfo2> infoMap = cbi.getInfoMap();

		for (CfgInfoContext cfgInfoContext : infoMap.keySet()) {

			// This is (mostly) copied from the DiscoveryTab class of the CDT UI project.
			// I don't really understand everything in here, but it seems to work...
			// @see
			// org.eclipse.cdt.managedbuilder.ui.properties.DiscoveryTab#clearDiscoveredEntries()
			// for no additional information :-)

			if (cfgInfoContext == null) {
				Status status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
						"Unexpected cfgInfoContext=null while trying to clear discovery entries"); //$NON-NLS-1$
				throw new CoreException(status);
			}

			IConfiguration cfg = cfgInfoContext.getConfiguration();
			if (cfg == null) {
				cfg = cfgInfoContext.getResourceInfo().getParent();
			}
			if (cfg == null) {
				Status status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
						"Unexpected cfg=null while trying to clear discovery entries"); //$NON-NLS-1$
				throw new CoreException(status);
			}

			IProject project = (IProject) cfg.getOwner();

			DiscoveredPathInfo pathInfo = new DiscoveredPathInfo(project);
			InfoContext infoContext = cfgInfoContext.toInfoContext();

			// 1. Remove scanner info from
			// .metadata/.plugins/org.eclipse.cdt.make.core/Project.sc
			DiscoveredScannerInfoStore dsiStore = DiscoveredScannerInfoStore.getInstance();
			dsiStore.saveDiscoveredScannerInfoToState(project, infoContext, pathInfo);

			// 2. Remove scanner info from CfgDiscoveredPathManager cache and
			// from the Tool
			CfgDiscoveredPathManager cdpManager = CfgDiscoveredPathManager.getInstance();
			cdpManager.removeDiscoveredInfo(project, cfgInfoContext);

			// 3. Remove scanner info from SI collector
			ICfgScannerConfigBuilderInfo2Set info2 = CfgScannerConfigProfileManager
					.getCfgScannerConfigBuildInfo(cfg);
			Map<CfgInfoContext, IScannerConfigBuilderInfo2> infoMap2 = info2.getInfoMap();
			IScannerConfigBuilderInfo2 buildInfo2 = infoMap2.get(cfgInfoContext);
			if (buildInfo2 != null) {
				ScannerConfigProfileManager scpManager = ScannerConfigProfileManager.getInstance();
				String selectedProfileId = buildInfo2.getSelectedProfileId();
				SCProfileInstance profileInstance = scpManager.getSCProfileInstance(project,
						infoContext, selectedProfileId);

				IScannerInfoCollector collector = profileInstance.getScannerInfoCollector();
				if (collector instanceof IScannerInfoCollectorCleaner) {
					((IScannerInfoCollectorCleaner) collector).deleteAll(project);
				}
				buildInfo2 = null;
			}
		}
	}

	/**
	 * Run the CDT Scanner Config Builder and the indexer for a project to discover the build in
	 * paths/symbols and index them for the editor.
	 * <p>
	 * This method will check the perConfig flag of the AVR properties for the project and will run
	 * the scanner builder either on
	 * <ul>
	 * <li>the active configuration ( perConfig <code>true</code>)</li>
	 * <li>or all configurations ( perConfig <code>false</code>)</li>
	 * </ul>
	 * </p>
	 * <p>
	 * The actual build and the indexer is run in a background job and this method will return
	 * immediately. Errors will be logged by the job.
	 * 
	 * @param project
	 *            IProject with a <code>ScannerConfigNature</code> (normal CDT project)
	 * @return The build job. Used for unit testing.
	 */
	public static Job runDiscovery(final IProject project) {

		Job buildJob = new Job("Running Scanner Config Builder") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {

				// Check if the AVR properties apply to all configs.
				// If yes, then we want to run the ScannerConfigBuilder on all Configurations, while
				// by default it will only work on the active build configuration (unless changed by
				// the user in the preferences).
				// So we use a little "hack": Change the CDT Build preference property to
				// "Build all configurations" while we run the Scanner Config Builder and afterwards
				// restore the flag to the previous (user set) value.
				boolean isperconfig = ProjectPropertyManager.getPropertyManager(project)
						.isPerConfig();
				boolean needallflag = ACBuilder.needAllConfigBuild();
				if (!isperconfig) {
					// Set flag to build all configurations
					ACBuilder.setAllConfigBuild(true);
				}

				try {
					monitor.beginTask("Running scanner and indexer", 100);

					//
					// Run the Scanner Config Builder to re-discover paths and symbols
					//
					// Both parameters (kind and args) are unused by the ScannerConfigBuilder
					// We set them anyway for cleanness :-)
					int kind = IncrementalProjectBuilder.FULL_BUILD;
					Map<String, String> args = new HashMap<String, String>();

					project.build(kind, "org.eclipse.cdt.managedbuilder.core.ScannerConfigBuilder",
							args, new SubProgressMonitor(monitor, 30));

					monitor.worked(20);

					//
					// Now rebuild the index by the indexer, taking into account the new paths and
					// symbols.
					//
					// This may take some time, but as we are in a background job this is hopefully
					// not very noticeable to the user.
					//
					ICProject cproj = CoreModel.getDefault().create(project);
					CCorePlugin.getIndexManager().reindex(cproj);

					monitor.worked(100);

				} catch (CoreException e) {
					Status status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
							"Internal error while trying to run the ScannerConfigBuilder"); //$NON-NLS-1$
					AVRPlugin.getDefault().log(status);
					return status;
				} finally {
					monitor.done();
					if (!isperconfig) {
						// Restore the "Build all configurations" preference flag.
						ACBuilder.setAllConfigBuild(needallflag);
					}
				}

				return Status.OK_STATUS;
			}
		};

		// now set the Job properties and start it
		buildJob.setPriority(Job.SHORT); // Could be Job.BUILD, but I think the Scanner builder is
											// quick enough to not affect the user experience.
		buildJob.setUser(true);
		buildJob.setSystem(true);
		buildJob.schedule();

		return buildJob;
	}
}
