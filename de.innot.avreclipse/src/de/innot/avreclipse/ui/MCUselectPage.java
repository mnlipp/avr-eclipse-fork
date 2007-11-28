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
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
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
public class MCUselectPage extends MBSCustomPage {

	private Composite top;

	/** Option of the winAVR toolchain containing the target MCU Type */
	private IOption optionTargetMCU;
	/** Option of the winAVR toolchain containing the target MCU clock frequency */
	private IOption optionTargetFCPU;

	// GUI Widgets
	private Combo comboMCUtype;
	private Text textMCUfreq;

	public MCUselectPage() {
		this.pageID = "de.innot.avreclipse.wizard.targetoptionselectpage";
	}

	public String getName() {
		return new String("AVR Cross Target Hardware Selection Page");
	}

	/**
	 * Handle the selected target MCU.
	 * <p>
	 * This will cause an <code>optionHandler</code> event which in turn, via
	 * the {@link de.innot.avreclipse.mbs.TargetHardwareOptionHandler} - will
	 * set the options of the compiler, linker and any other tools requiring the
	 * MCU type.
	 * </p>
	 * <p>
	 * Also sets the selected MCU type as default (for this project)
	 * </p>
	 */
	private void handleMCUtypeSelection() {
		try {
			optionTargetMCU.setValue(comboMCUtype.getText());
			optionTargetMCU.setDefaultValue(comboMCUtype.getText());
		} catch (BuildException e) {
			// something wrong in the plugin.xml
			e.printStackTrace();
		}
	}

	/**
	 * Handle the selected target CPU Frequency.
	 * <p>
	 * This will cause an <code>optionHandler</code> event which in turn, via
	 * the {@link de.innot.avreclipse.mbs.TargetHardwareOptionHandler} - will
	 * set the option of the compiler.
	 * </p>
	 * <p>
	 * Also sets the selected MCU type as default (for this project)
	 * </p>
	 */
	private void handleMCUfreqSelection() {
		try {
			optionTargetFCPU.setValue(textMCUfreq.getText());
			optionTargetFCPU.setDefaultValue(textMCUfreq.getText());
		} catch (BuildException e) {
			// something wrong in the plugin.xml
			e.printStackTrace();
		}
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
				handleMCUtypeSelection();
			}
		});

		// The CPU Frequency Selection Text Widget
		Label labelMCUfreq = new Label(top, SWT.NONE);
		labelMCUfreq.setText("MCU Frequency (Hz):");

		textMCUfreq = new Text(top, SWT.BORDER | SWT.SINGLE);
		textMCUfreq.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textMCUfreq
				.setToolTipText("Target MCU Clock Frequency. Can be changed later via the project properties");
		textMCUfreq.addListener(SWT.FocusOut, new Listener() {
			public void handleEvent(Event e) {
				handleMCUfreqSelection();
			}
		});
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
		if (visible) {
			// Get the winAVR toolchain and take the MCU list and the default
			// values from the toolchain options and set the GUI elements
			// accordingly
			IToolChain tc = ManagedBuildManager
					.getExtensionToolChain(PluginIDs.PLUGIN_BASE_TOOLCHAIN);
			tc = ManagedBuildManager.getRealToolChain(tc);

			optionTargetMCU = tc.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_MCU);
			comboMCUtype.setItems(optionTargetMCU.getApplicableValues());
			try {
				comboMCUtype.select(comboMCUtype.indexOf(optionTargetMCU
						.getEnumName(optionTargetMCU.getDefaultValue().toString())));
			} catch (BuildException e) {
				// something wrong with the plugin.xml
				e.printStackTrace();
			}

			optionTargetFCPU = tc.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_FCPU);
			textMCUfreq.setText(optionTargetFCPU.getDefaultValue().toString());
		}

		top.setVisible(visible);
	}

	protected boolean isCustomPageComplete() {
		// This page only changes default values, so it is always complete
		return true;
	}
}
