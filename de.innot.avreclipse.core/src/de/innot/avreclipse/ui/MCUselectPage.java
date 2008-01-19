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

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageData;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.osgi.service.prefs.Preferences;

import de.innot.avreclipse.core.natures.AVRProjectNature;
import de.innot.avreclipse.core.preferences.AVRTargetProperties;
import de.innot.avreclipse.core.toolinfo.GCC;

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

	private Map<String, String> fMCUTypes = null;
	private String[] fMCUTypesList = null;

	private String fDefaultMCU = null;
	private String fDefaultFCPU = null;

	// GUI Widgets
	private Combo comboMCUtype;
	private Text textMCUfreq;

	/**
	 * Constructor for the Wizard Page.
	 * 
	 * <p>
	 * Gets the list of supported MCUs from the compiler and sets the default
	 * values.
	 * </p>
	 * 
	 */
	public MCUselectPage() {
		// If the user does not click on "next", this constructor is
		// the only thing called before the "run" method.
		// Therefore we'll set the defaults here. They are set as
		// page properties, as this seems to be the only way to pass
		// values to the run() method.

		this.pageID = PAGE_ID;

		Preferences defaults = AVRTargetProperties.getDefaultPreferences();

		// Get the list of supported MCU Types from the compiler
		fMCUTypes = GCC.getDefault().getToolInfo(GCC.TOOLINFOTYPE_MCUS);
		fMCUTypesList = new String[fMCUTypes.size()];
		fMCUTypesList = fMCUTypes.values().toArray(fMCUTypesList);
		Arrays.sort(fMCUTypesList);

		fDefaultMCU = defaults.get(AVRTargetProperties.KEY_MCUTYPE, null);

		// get the default target fcpu values
		fDefaultFCPU = defaults.get(AVRTargetProperties.KEY_FCPU, null);

		// Set the default values as page properties
		MBSCustomPageManager.addPageProperty(PAGE_ID, PROPERTY_MCU_TYPE,
				fDefaultMCU);

		MBSCustomPageManager.addPageProperty(PAGE_ID, PROPERTY_MCU_FREQ,
				fDefaultFCPU);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	public String getName() {
		return new String("AVR Cross Target Hardware Selection Page");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
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
		comboMCUtype.setItems(fMCUTypesList);
		comboMCUtype.select(comboMCUtype.indexOf(fMCUTypes.get(fDefaultMCU)));

		// The CPU Frequency Selection Text Widget
		Label labelMCUfreq = new Label(top, SWT.NONE);
		labelMCUfreq.setText("MCU Frequency (Hz):");

		textMCUfreq = new Text(top, SWT.BORDER | SWT.SINGLE);
		textMCUfreq.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		textMCUfreq
				.setToolTipText("Target MCU Clock Frequency. Can be changed later via the project properties");
		textMCUfreq.addListener(SWT.FocusOut, new Listener() {
			public void handleEvent(Event e) {
				String value = textMCUfreq.getText();
				MBSCustomPageManager.addPageProperty(PAGE_ID,
						PROPERTY_MCU_FREQ, value);
			}
		});
		// filter non-digits from the input
		textMCUfreq.addListener(SWT.Verify, new Listener() {
			public void handleEvent(Event event) {
				String text = event.text;
				for (int i = 0; i < text.length(); i++) {
					char ch = text.charAt(i);
					if (!('0' <= ch && ch <= '9')) {
						event.doit = false;
						return;
					}
				}
			}
		});
		textMCUfreq.setText(fDefaultFCPU);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		top.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
	 */
	public Control getControl() {
		return top;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		return new String("Define the AVR target properties.");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	public Image getImage() {
		return wizard.getDefaultPageImage();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	public String getMessage() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		return new String("AVR Target Hardware Properties");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
	 */
	public void performHelp() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor image) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		top.setVisible(visible);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete()
	 */
	@Override
	protected boolean isCustomPageComplete() {
		// We only change defaults, so this page is always complete
		return true;
	}

	/**
	 * Operation for the MCUSelectPage.
	 * 
	 * This is called when the finish button of the new Project Wizard has been
	 * pressed. It will get the new Project and set the project options as
	 * selected by the user (or to the default values).
	 * 
	 */
	public void run() {

		// At this point the new project has been created and its
		// configuration(s) with their toolchains have been set up.

		// Is there a more elegant way to get to the Project?
		MBSCustomPageData pagedata = MBSCustomPageManager
				.getPageData(this.pageID);
		CDTCommonProjectWizard wizz = (CDTCommonProjectWizard) pagedata
				.getWizardPage().getWizard();
		IProject project = wizz.getLastProject();

		IPreferenceStore prefs = AVRTargetProperties.getPropertyStore(project);

		// Set the Project properties according to the selected values

		// Get the id of the selected MCU and store it
		String mcutype = (String) MBSCustomPageManager.getPageProperty(PAGE_ID,
				PROPERTY_MCU_TYPE);
		prefs.setValue(AVRTargetProperties.KEY_MCUTYPE, mcutype);
		for (String mcuid : fMCUTypes.keySet()) {
			if (mcutype.equals(fMCUTypes.get(mcuid))) {
				prefs.setValue(AVRTargetProperties.KEY_MCUTYPE, mcuid);
				break;
			}
		}

		// Set the F_CPU and store it
		String mcufreq = (String) MBSCustomPageManager.getPageProperty(PAGE_ID,
				PROPERTY_MCU_FREQ);
		prefs.setValue(AVRTargetProperties.KEY_FCPU, mcufreq);

		try {
			AVRTargetProperties.savePreferences(prefs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Add the AVR Nature to the project
		try {
			AVRProjectNature.addAVRNature(project);
		} catch (CoreException ce) {
			// TODO: log exception
			ce.printStackTrace();
		}

	}
}
