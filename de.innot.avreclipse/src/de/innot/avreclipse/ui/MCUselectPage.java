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
 *	   Manuel Stahl - original idea and some remaining code fragments
 *     Thomas Holland - rewritten to be compatible with Eclipse 3.3 and the rest of the plugin
 *     
 * $Id: MCUselectPage.java 9 2007-11-25 21:51:59Z thomas $
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageData;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

import de.innot.avreclipse.PluginIDs;

/**
 * New Project Wizard Page to set the default target MCU and its frequency.
 * 
 * <p>
 * This Page takes the possible target MCU types and its default as well as the
 * target MCU frequency default directly from the winAVR toolchain as defined in
 * the <code>plugin.xml</code>.
 * </p>
 * <p>
 * If changed, the new type and MCU frequency are written back to the winAVR
 * toolchain as current value and as default value for this project.
 * </p>
 * 
 * @author Manuel Stahl (thymythos@web.de)
 * @author Thomas Holland (thomas@innot.de)
 * @since 1.0
 */
public class MCUselectPage extends MBSCustomPage implements Runnable {

	private final static String PAGE_ID = "de.innot.avreclipse.mcuselectpage";
	private final static String PROPERTY_MCU_TYPE = "mcutype";
	private final static String PROPERTY_MCU_FREQ = "mcufreq";

	private Composite top;

	private String[] fMCUTypeList;
	private String fMCUTypeDefaultName;

	// GUI Widgets
	private Combo comboMCUtype;
	private Text textMCUfreq;

	/**
	 * Constructor for the Wizard Page.
	 * 
	 * <p>
	 * Initialize stuff here, as the operation for this Page might be called
	 * without ever setting the page visible.
	 * </p>
	 * 
	 */
	public MCUselectPage() {
		this.pageID = PAGE_ID;

		// Get the winAVR toolchain and take the MCU list and the default
		// values from the toolchain options.
		
		// The values are stored as Page Properties to access them in the Operation for this page
		IToolChain tc = ManagedBuildManager
				.getExtensionToolChain(PluginIDs.PLUGIN_BASE_TOOLCHAIN);

		// get the target mcu values
		IOption optionTargetMCU = tc
				.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_MCU);
		fMCUTypeList = optionTargetMCU.getApplicableValues();
		try {
			fMCUTypeDefaultName = optionTargetMCU.getEnumName(optionTargetMCU
					.getDefaultValue().toString());
		} catch (BuildException e) {
			// something wrong with the plugin.xml
			e.printStackTrace();
		}
		MBSCustomPageManager.addPageProperty(PAGE_ID, PROPERTY_MCU_TYPE,
				fMCUTypeDefaultName);

		// get the target fcpu values
		IOption optionTargetFCPU = tc
				.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_FCPU);
		String mcufreq = optionTargetFCPU.getDefaultValue().toString();
		MBSCustomPageManager.addPageProperty(PAGE_ID, PROPERTY_MCU_FREQ,
				mcufreq);
	}

	public String getName() {
		return new String("AVR Cross Target Hardware Selection Page");
	}

	public void createControl(Composite parent) {
		// some general layout work
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalAlignment = GridData.END;
		top = new Composite(parent, SWT.NONE);
		top.setLayout(new GridLayout(2, false));
		top.setLayoutData(gridData);

		// The MCU Selection Combo Widget
		Label labelMCUtype = new Label(top, SWT.NONE);
		labelMCUtype.setText("MCU Type:");

		comboMCUtype = new Combo(top, SWT.READ_ONLY | SWT.DROP_DOWN);
		comboMCUtype.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		comboMCUtype
				.setToolTipText("Target MCU Type. Can be changed later via the project properties");
		comboMCUtype.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				String value = comboMCUtype.getText();
				MBSCustomPageManager.addPageProperty(PAGE_ID,
						PROPERTY_MCU_TYPE, value);
			}
		});
		comboMCUtype.setItems(fMCUTypeList);
		comboMCUtype.select(comboMCUtype.indexOf(fMCUTypeDefaultName));

		// The CPU Frequency Selection Text Widget
		Label labelMCUfreq = new Label(top, SWT.NONE);
		labelMCUfreq.setText("MCU Frequency (Hz):");

		textMCUfreq = new Text(top, SWT.BORDER | SWT.SINGLE);
		textMCUfreq.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textMCUfreq
				.setToolTipText("Target MCU Clock Frequency. Can be changed later via the project properties");
		textMCUfreq.addListener(SWT.FocusOut, new Listener() {
			public void handleEvent(Event e) {
				// TODO: allow only integer values
				String value = textMCUfreq.getText();
				MBSCustomPageManager.addPageProperty(PAGE_ID,
						PROPERTY_MCU_FREQ, value);
			}
		});
		textMCUfreq.setText((String) MBSCustomPageManager.getPageProperty(
				PAGE_ID, PROPERTY_MCU_FREQ));

	}

	public void dispose() {
		top.dispose();
	}

	public Control getControl() {
		return top;
	}

	public String getDescription() {
		return new String("Define the AVR target properties.");
	}

	public String getErrorMessage() {
		return null;
	}

	public Image getImage() {
		return wizard.getDefaultPageImage();
	}

	public String getMessage() {
		return null;
	}

	public String getTitle() {
		return new String("AVR Target Hardware Properties");
	}

	public void performHelp() {
	}

	public void setDescription(String description) {
	}

	public void setImageDescriptor(ImageDescriptor image) {
	}

	public void setTitle(String title) {
	}

	public void setVisible(boolean visible) {
		top.setVisible(visible);
	}

	protected boolean isCustomPageComplete() {
		// This page only changes default values, so it is always complete
		return true;
	}

	/**
	 * Operation for the MCUSelectPage.
	 * 
	 * This is called when the finish button of the new Project Wizard has been
	 * pressed. It will get the <code>Configuration(s)</code> of the new
	 * Project and set the options of their toolchains as selected by the user.
	 * 
	 */
	public void run() {

		// At this point the new project has been created and its
		// configuration(s) with their toolchains have been set up.
		// To get to the configurations does not seem to be easy. There
		// is probably a more elegant way (maybe via the workbench?)
		// but I have not found it. For now I use:
		// MBSCustomPageData -> CDTCommonProjectWizard -> IProject ->
		// ManagedBuildInfo -> IManagedProject -> IConfiguration

		MBSCustomPageData pagedata = MBSCustomPageManager
				.getPageData(this.pageID);
		CDTCommonProjectWizard wizz = (CDTCommonProjectWizard) pagedata
				.getWizardPage().getWizard();
		IProject proj = wizz.getLastProject();
		IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(proj);
		IManagedProject mproj = bi.getManagedProject();
		IConfiguration[] cfgs = mproj.getConfigurations();

		for (int i = 0; i < cfgs.length; i++) {
			IToolChain tc = (IToolChain) cfgs[i].getToolChain();
			try {
				// Change Target MCU option and set given value as default
				IOption optionTargetMCU = tc
						.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_MCU);
				String mcutype = (String) MBSCustomPageManager.getPageProperty(
						PAGE_ID, PROPERTY_MCU_TYPE);
				String mcutypeid = optionTargetMCU.getEnumeratedId(mcutype);
				optionTargetMCU = ManagedBuildManager.setOption(cfgs[i], tc,
						optionTargetMCU, mcutypeid);
				optionTargetMCU.setDefaultValue(mcutypeid);

				IOption optionTargetFCPU = tc
						.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_FCPU);
				String mcufreq = (String) MBSCustomPageManager.getPageProperty(
						PAGE_ID, PROPERTY_MCU_FREQ);
				optionTargetMCU = ManagedBuildManager.setOption(cfgs[i], tc,
						optionTargetFCPU, mcufreq);
				optionTargetFCPU.setDefaultValue(mcufreq);
			} catch (BuildException e) {
				// print stacktrace for debugging
				e.printStackTrace();
			}

		}

		// Save the modifications
		ManagedBuildManager.saveBuildInfo(proj, false);

	}
}
