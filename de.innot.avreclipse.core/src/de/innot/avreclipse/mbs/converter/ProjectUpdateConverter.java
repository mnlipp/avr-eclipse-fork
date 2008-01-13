/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.mbs.converter;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;

import de.innot.avreclipse.PluginIDs;
import de.innot.avreclipse.core.natures.AVRProjectNature;

/**
 * @author Thomas
 * 
 */
public class ProjectUpdateConverter implements IConvertManagedBuildObject {

	final static String WRONG_DEBUG_ID = "de.innot.avreclipse.compiler.option.debug";

	/**
	 * Update a given Project to the latest AVR Eclipse Plugin settings
	 * 
	 * @author Thomas Holland
	 * 
	 */
	public ProjectUpdateConverter() {
		// TODO Auto-generated constructor stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IConvertManagedBuildObject#convert(org.eclipse.cdt.managedbuilder.core.IBuildObject,
	 *      java.lang.String, java.lang.String, boolean)
	 */
	public IBuildObject convert(IBuildObject buildObj, String fromId,
			String toId, boolean isConfirmed) {

		// This is currently only called from the CDT ConvertTargetDialog and
		// only for an existing AVR Eclipse Plugin project.
		
		if (fromId.equals(toId)) {
			// Bugfix 2.0.2 upgrade
			// go through all configurations of the selected Project and
			// check if it has the old compiler debug option
			// if yes, then delete the option.
			IManagedProject mproj = (IManagedProject) buildObj;
			IConfiguration[] cfgs = mproj.getConfigurations();
			if ((cfgs != null) && (cfgs.length > 0)) { // Sanity Check
				for (int i = 0; i < cfgs.length; i++) {
					IConfiguration currcfg = cfgs[i];
					ITool[] tools = currcfg
							.getToolsBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_TOOL_COMPILER);
					for (int n = 0; n < tools.length; n++) {
						IOption[] allopts = tools[n].getOptions();
						// Step thru all options and remove the offending one
						for (int k = 0; k < allopts.length; k++) {
							IOption curropt = allopts[k];
							if (curropt.getName().endsWith("(-g)")) {
								// tool has the corrupt option
								tools[n].removeOption(curropt);
							}
						} // for options
					} // for tools
				} // for configurations
				
				// Save the (modified) Buildinfo
				IProject project = (IProject) mproj.getOwner();
				ManagedBuildManager.saveBuildInfo(project, false);
			}
			
			// 2.1 Upgrade
			// Add AVR Nature to the project
			IProject project = (IProject)mproj.getOwner();
			try {
				AVRProjectNature.addAVRNature(project);
			} catch (CoreException ce) {
				// TODO: print stacktrace for debugging
				ce.printStackTrace();
			}
		}
		// Feature upgrade - none yet
		return buildObj;
	}

}
