/*******************************************************************************
 * Copyright (c) 2004, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Thomas Holland - Added AVR specific stuff
 *******************************************************************************/
package de.innot.avreclipse.mbs.scannerconfig;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.ScannerInfoTypes;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerConfigUtil;
import org.eclipse.cdt.make.internal.core.scannerconfig2.DefaultRunSIProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathManager;
import de.innot.avreclipse.core.paths.IPathProvider;
import de.innot.avreclipse.core.properties.AVRProjectProperties;
import de.innot.avreclipse.core.properties.ProjectPropertyManager;

/**
 * @author innot
 * 
 */
@SuppressWarnings({ "restriction" })
public class AVRGCCSpecsRunSIProvider extends DefaultRunSIProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.internal.core.scannerconfig2.DefaultRunSIProvider
	 * #initialize()
	 */
	@Override
	protected boolean initialize() {
		boolean rc = super.initialize();

		if (rc) {
			String targetFile = "dummy"; //$NON-NLS-1$
			IProject project = resource.getProject();
			try {
				if (project.hasNature(CCProjectNature.CC_NATURE_ID)) {
					targetFile = GCCScannerConfigUtil.CPP_SPECS_FILE;
				} else if (project.hasNature(CProjectNature.C_NATURE_ID)) {
					targetFile = GCCScannerConfigUtil.C_SPECS_FILE;
				}
				// replace string variables in compile arguments
				// TODO Vmir - use string variable replacement
				for (int i = 0; i < fCompileArguments.length; ++i) {
					fCompileArguments[i] = fCompileArguments[i].replaceAll(
							"\\$\\{plugin_state_location\\}", //$NON-NLS-1$ 
							MakeCorePlugin.getWorkingDirectory().toString());
					fCompileArguments[i] = fCompileArguments[i].replaceAll(
							"\\$\\{specs_file\\}", targetFile); //$NON-NLS-1$
				}
			} catch (CoreException e) {
				// TODO VMIR better error handling
				MakeCorePlugin.log(e.getStatus());
				rc = false;
			}
		}
		return rc;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.make.internal.core.scannerconfig2.DefaultRunSIProvider
	 * #prepareArguments(boolean)
	 */
	@Override
	protected String[] prepareArguments(boolean isDefaultCommand) {

		// Convert the preset compile arguments to a list for easier management
		List<String> compargs = new ArrayList<String>();
		if (fCompileArguments != null) {
			compargs.addAll(Arrays.asList(fCompileArguments));
		}

		// Get the current target MCU and FCPU and add the
		// appropriate compiler arguments: -mmcu=... & -DFCPU=....
		IProject project = (IProject) resource;

		ProjectPropertyManager projprops = ProjectPropertyManager
				.getPropertyManager(project);
		AVRProjectProperties props = projprops.getActiveProperties();
		String targetmcu = props.getMCUId();
		String fcpu = props.getFCPU();

		if ((targetmcu != null) && (targetmcu.length() > 0)) {
			compargs.add("-mmcu=" + targetmcu);
		}
		if ((fcpu != null) && (fcpu.length() != 0)) {
			compargs.add("-DF_CPU=" + fcpu + "UL");
		}

		if (collector == null) {
			return compargs.toArray(new String[compargs.size()]);
		}

		// Check if a "-mmcu" option has already been colloced from somewhere
		// else. If the TARGET_SPECIFIC_OPTION of the underlying collector
		// already has a -mmcu flag we remove it to avoid (possibly
		// inconsistent) duplicates.
		@SuppressWarnings("unchecked")
		List<String> tso = collector.getCollectedScannerInfo(
				resource.getProject(), ScannerInfoTypes.TARGET_SPECIFIC_OPTION);
		if (tso != null && tso.size() > 0) {
			for(String s : tso) {
				if (!s.startsWith("-mmcu")) {
					compargs.add(s);
				}
			}
		}

		return compargs.toArray(new String[compargs.size()]);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.internal.core.scannerconfig2.DefaultRunSIProvider#getCommandToLaunch()
	 */
	@Override
	protected IPath getCommandToLaunch() {
		IPath command = super.fCompileCommand;
		// Don't prepend the avr-gcc path if the command already has a path
		if (command.isAbsolute()) {
			return command;
		}

		// TODO: Add some code to get the path from the project once we have
		// project/configuration specific paths
		IPathProvider pp = new AVRPathManager(AVRPath.AVRGCC);
		
		IPath gccparentdir = pp.getPath();
		
		IPath gccpath = gccparentdir.append(command);
		
		return gccpath;
	}

	
}
