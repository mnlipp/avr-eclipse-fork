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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.debug.core.CDebugCorePlugin;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.ICDebugConfiguration;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

import de.innot.avreclipse.debug.core.IAVRGDBConstants;

/**
 * Launch Shortcut to Debug AVR Hardware
 * <p>
 * This Shortcut will get a list of all applicable binaries for the given <code>ISelection</code>/
 * <code>IEditorPart</code> and launch a debugging session.
 * </p>
 * If a previous lauch configuration already exists for the binary it is used, otherwise a new
 * <code>ILaunchConfiguration</code> is created and saved.</p>
 * <p>
 * The class will prompt the user to select:
 * <ul>
 * <li>if more than one binary is applicable</li>
 * <li>If more than one launch configuration is applicable</li>
 * </ul>
 * If no binaries can be found for the given input then an error message is shown.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class LaunchShortcutDebugHardware implements ILaunchShortcut {

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.jface.viewers.ISelection,
	 * java.lang.String)
	 */
	public void launch(ISelection selection, String mode) {
		if (!isModeApplicable(mode)) {
			// Should not happen => Bug in our Plugin
			IStatus status = new Status(IStatus.ERROR, AVRGDBUIPlugin.PLUGIN_ID,
					"LaunchShortcutDebugHardware: Wrong mode '" + mode + "'", null);
			AVRGDBUIPlugin.log(status);
			return;
		}

		if (selection instanceof IStructuredSelection) {
			IBinary binary = searchBinary(((IStructuredSelection) selection).toArray(), mode);
			launch(binary, mode);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchShortcut#launch(org.eclipse.ui.IEditorPart,
	 * java.lang.String)
	 */
	public void launch(IEditorPart editor, String mode) {
		if (!isModeApplicable(mode)) {
			// Should not happen => Bug in our Plugin
			IStatus status = new Status(IStatus.ERROR, AVRGDBUIPlugin.PLUGIN_ID,
					"LaunchShortcutDebugHardware: Wrong mode '" + mode + "'", null);
			AVRGDBUIPlugin.log(status);
			return;
		}

		IBinary binary = searchBinary(new Object[] { editor.getEditorInput() }, mode);
		launch(binary, mode);
	}

	/**
	 * Internal launch method.
	 * <p>
	 * It takes the binary, gets an existing launch configuration for this binary (or creates a new
	 * launch configuration) and hands this launch configuration over to the CDT Debug plugin for
	 * launching the debugger.
	 * </p>
	 * 
	 * @param binary
	 *            The binary file to launch
	 * @param mode
	 *            The mode to launch in. Currently only "debug" is supported by this class
	 */
	protected void launch(IBinary binary, String mode) {

		if (binary == null) {
			return;
		}

		ILaunchConfiguration config = findLaunchConfiguration(binary, mode);
		if (config != null) {
			DebugUITools.launch(config, mode);
		}

	}

	/**
	 * Searches the given IResource and/or IProject elements for any executable binary file.
	 * <p>
	 * The method will collect a list of all applicable binaries. To determine if a binary is
	 * applicable the {@link LaunchShortcutDebugHardware#isBinaryApplicable(IBinary)} method is
	 * called for each discovered binary.
	 * </p>
	 * <p>
	 * If more than one applicable binary is found, then the user is asked to choose one via a
	 * dialog.
	 * </p>
	 * 
	 * @param elements
	 *            Array of IResource, IProject or IBinary elements.
	 * @param mode
	 * @return One <code>IBinary</code>, or <code>null</code> if no applicable binaries could be
	 *         found or if the user has canceled the selection dialog.
	 */
	protected IBinary searchBinary(final Object[] elements, String mode) {

		// Sanity check. If nothing was selected pop an error message
		if (elements == null || elements.length == 0) {
			Shell shell = AVRGDBUIPlugin.getActiveShell();
			String title = "Debug launcher";
			String message = "Launch failed - no project or binary selected";
			MessageDialog.openError(shell, title, message);
		}

		// Check if a single binary (*.elf) has been selected
		// This is a quick exit if the user has selected an *.elf file.
		if (elements.length == 1 && elements[0] instanceof IBinary) {
			IBinary binary = (IBinary) elements[0];
			if (isBinaryApplicable(binary)) {
				return binary;
			} else {
				return null;
			}
		}

		// The elements are something else.
		// Go through all elements. If they are IResources then get their CProject.
		// Then get all binaries for the project and filter them.
		// All valid binaries are collected.
		List<IBinary> allbins = new ArrayList<IBinary>();
		for (Object element : elements) {
			if (element instanceof IAdaptable) {
				IResource res = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
				if (res == null) {
					continue;
				}

				ICProject cproject = CoreModel.getDefault().create(res.getProject());
				if (cproject == null) {
					continue;
				}

				try {
					IBinary[] projectbins = cproject.getBinaryContainer().getBinaries();

					for (IBinary binary : projectbins) {
						if (isBinaryApplicable(binary)) {
							allbins.add(binary);
						}
					}
				} catch (CModelException e) {
					// project can't deliver binaries - ignore
				}
			}
		}

		// No check how many binaries we have found
		// 0: pop error message
		// 1: return the single binary
		// >1: Ask the user to select one
		switch (allbins.size()) {
			case 0:
				Shell shell = AVRGDBUIPlugin.getActiveShell();
				String title = "Debug launcher";
				String message = "Launch failed - no binary found";
				MessageDialog.openError(shell, title, message);
				return null;

			case 1:
				return allbins.get(0);

			default:
				return selectBinary(allbins, mode);
		}
	}

	/**
	 * Locate a configuration to relaunch for the given type. If one cannot be found, create one.
	 * <p>
	 * If more than one launch configurations already exists for the given binary, then the user is
	 * prompted to choose one with a dialog.
	 * </p>
	 * 
	 * @return a re-usable config or <code>null</code> if no launch configuration could be created
	 *         or when the user has canceled the selection dialog.
	 */
	protected ILaunchConfiguration findLaunchConfiguration(IBinary bin, String mode) {

		// ILaunchConfigurationType is ID_LAUNCH_C_APP, an C application
		ILaunchConfigurationType configType = getCLaunchConfigType();

		List<ILaunchConfiguration> matchingConfigs = new ArrayList<ILaunchConfiguration>();

		try {
			IPath binaryname = bin.getResource().getProjectRelativePath();
			// Get a list of all C type LaunchConfigurations.
			// Then collect all configs that have the right path and project
			ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager()
					.getLaunchConfigurations(configType);

			for (ILaunchConfiguration config : configs) {
				IPath programPath = CDebugUtils.getProgramPath(config);
				String projectName = CDebugUtils.getProjectName(config);
				if (programPath != null && programPath.equals(binaryname)) {
					if (projectName != null
							&& projectName.equals(bin.getCProject().getProject().getName())) {
						matchingConfigs.add(config);
					}
				}
			}
		} catch (CoreException ce) {
			AVRGDBUIPlugin.log(ce.getStatus());
		}

		// No check how many LaunchConfigurations we have found
		// 0: create a new one
		// 1: return the single config
		// >1: Ask the user to select one
		switch (matchingConfigs.size()) {
			case 0:
				return createNewConfiguration(bin, mode);

			case 1:
				return matchingConfigs.get(0);

			default:
				return selectConfiguration(bin, matchingConfigs, mode);
		}
	}

	/**
	 * Create a new launch configuration for the given binary.
	 * <p>
	 * The new launch configuration will have only the minimum number of attributes set. Everything
	 * else is, especially the AVR specific attributes, are not set so that their default value will
	 * be used by the debugger.
	 * </p>
	 * 
	 * @param binary
	 *            the <code>IBinary</code> for which the new launch configuration is created
	 * @param mode
	 *            Currently "debug" is the only supported mode.
	 * @return the new launch configuration
	 */
	protected ILaunchConfiguration createNewConfiguration(IBinary binary, String mode) {

		ILaunchConfiguration config = null;
		try {

			String projectName = binary.getResource().getProjectRelativePath().toString();
			ILaunchConfigurationType configType = getCLaunchConfigType();

			// Instantiate a new LaunchConfiguration
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(null, getLaunchManager()
					.generateLaunchConfigurationName(binary.getElementName()));

			// Set the project parameters
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME, projectName);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, binary
					.getCProject().getElementName());
			wc.setMappedResources(new IResource[] { binary.getResource(),
					binary.getResource().getProject() });
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_WORKING_DIRECTORY, (String) null);

			// Get the active build configuration id and store it as attribute
			ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(
					binary.getCProject().getProject());
			if (projDes != null) {
				String buildConfigID = projDes.getActiveConfiguration().getId();
				wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_PROJECT_BUILD_CONFIG_ID,
						buildConfigID);
			}

			// Set the parameters for the startup tab
			// TODO: see if we can replace them with default values
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_STOP_AT_MAIN, true);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_START_MODE,
					ICDTLaunchConfigurationConstants.DEBUGGER_MODE_RUN);

			// We use the AVRGDBDebugger for the debugger configuration.
			ICDebugConfiguration debugconfig = CDebugCorePlugin.getDefault().getDebugConfiguration(
					IAVRGDBConstants.DEBUGGER_ID);
			wc.setAttribute(ICDTLaunchConfigurationConstants.ATTR_DEBUGGER_ID, debugconfig.getID());

			// And now save the config so it can be reused and/or edited.
			config = wc.doSave();
		} catch (CoreException ce) {
			// Unlikely, but we need to inform the user that something did not work as expected.
			AVRGDBUIPlugin.log(ce.getStatus());
			MessageDialog.openError(AVRGDBUIPlugin.getActiveShell(), "Error", ce.getStatus()
					.getMessage());
			return null;
		}
		return config;
	}

	/**
	 * Prompts the user to select a configuration.
	 * 
	 * @param bin
	 *            The <code>IBinary</code> for which to select a Launch Configuration. Only used to
	 *            display the name in the dialog message.
	 * @param configs
	 *            List of all applicable launch configurations.
	 * @param mode
	 *            the launch configuration mode. Only used for the message dialog.
	 * @return the selected binary or <code>null</code> if none (= cancel).
	 */
	protected ILaunchConfiguration selectConfiguration(IBinary bin,
			List<ILaunchConfiguration> configs, String mode) {

		IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(AVRGDBUIPlugin
				.getActiveShell(), labelProvider);
		dialog.setElements(configs.toArray());
		dialog.setTitle("Launch Configuration Selection");

		String text = "Multiple {0} configurations exist for the binary file '{1}'.\n\n"
				+ "Please select the configuration to use";
		String message = MessageFormat.format(text, mode, bin.getPath().lastSegment().toString());
		dialog.setMessage(message);
		dialog.setMultipleSelection(false);
		int result = dialog.open();
		labelProvider.dispose();
		if (result == Window.OK) {
			return (ILaunchConfiguration) dialog.getFirstResult();
		}
		return null;

	}

	/**
	 * Prompts the user to select a binary
	 * 
	 * @param binList
	 *            list of all matching <code>IBinary</code> elements.
	 * @param mode
	 *            the launch configuration mode. Only used for the message dialog.
	 * @return the selected binary or <code>null</code> if none (= cancel).
	 */
	protected IBinary selectBinary(List<IBinary> binList, String mode) {

		ILabelProvider programLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary) element;
					StringBuilder name = new StringBuilder();
					name.append(bin.getPath().lastSegment());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		ILabelProvider contextLabelProvider = new CElementLabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof IBinary) {
					IBinary bin = (IBinary) element;
					StringBuilder name = new StringBuilder();
					// This will basically show the name of the project and configuration.
					// I am not sure if this is the best way to display this.
					// Better would be just the configuration - but what happens if the user selects
					// multiple project for this LaunchConfigurationShortcut.
					name.append(bin.getPath().removeLastSegments(0).toString());
					return name.toString();
				}
				return super.getText(element);
			}
		};

		TwoPaneElementSelector dialog = new TwoPaneElementSelector(AVRGDBUIPlugin.getActiveShell(),
				programLabelProvider, contextLabelProvider);
		dialog.setElements(binList.toArray());

		dialog.setTitle("AVR Application");

		String text = "Choose an AVR application binary to {0}";
		String message = MessageFormat.format(text, mode);
		dialog.setMessage(message);
		dialog.setUpperListLabel("Binaries:");
		dialog.setLowerListLabel("Context:");
		dialog.setMultipleSelection(false);
		if (dialog.open() == Window.OK) {
			return (IBinary) dialog.getFirstResult();
		}

		return null;
	}

	/**
	 * Test if the a binary is applicable for this LaunchShortcut.
	 * <p>
	 * This method is used to filter all found binaries of the project(s) down to those that are
	 * applicable for this LaunchConfiguration. It will test if the given binary is
	 * <ul>
	 * <li>an executable</li>
	 * <li>has debugging info</li>
	 * <li>contains avr code</li>
	 * </ul>
	 * </p>
	 * <p>
	 * Subclasses can override to implement a different filter.
	 * </p>
	 * 
	 * @param binary
	 *            The <code>IBinary</code> to test.
	 * @return <code>true</code> if the binary matches.
	 */
	protected boolean isBinaryApplicable(IBinary binary) {
		boolean result = binary.isExecutable() && "avr".equalsIgnoreCase(binary.getCPU())
				&& binary.hasDebug();
		return result;
	}

	/**
	 * Checks if the given mode is applicable for this launch shortcut.
	 * <p>
	 * Default implementation is <code>true</code> for '<code>debug</code>' and <code>false</code>
	 * for all other modes.
	 * </p>
	 * <p>
	 * Subclasses can override.
	 * </p>
	 * 
	 * @param mode
	 *            The
	 * @return
	 */
	protected boolean isModeApplicable(String mode) {
		return ILaunchManager.DEBUG_MODE.equalsIgnoreCase(mode);
	}

	/**
	 * Method getCLaunchConfigType.
	 * 
	 * @return ILaunchConfigurationType
	 */
	private ILaunchConfigurationType getCLaunchConfigType() {
		return getLaunchManager().getLaunchConfigurationType(IAVRGDBConstants.LAUNCH_TYPE_ID);
	}

	/**
	 * Convenience method to get the Eclipse debug manager.
	 * 
	 * @return The Eclipse debug manager class.
	 */
	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}

}
