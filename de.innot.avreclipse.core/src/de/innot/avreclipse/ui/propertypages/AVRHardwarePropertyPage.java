package de.innot.avreclipse.ui.propertypages;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import de.innot.avreclipse.core.preferences.AVRTargetProperties;
import de.innot.avreclipse.core.toolinfo.GCC;

public class AVRHardwarePropertyPage extends PropertyPage {

	private static final String TEXT_PERCONFIG = "Enable individual settings for Build Configurations";
	private static final String LABEL_MCUTYPE = "MCU Type";
	private static final String LABEL_FCPU = "MCU Clock Frequency";

	private Composite fMCUSection = null;
	private Button fPerConfigSelector = null;
	private Combo fMCUcombo = null;
	private Text fFCPUtext = null;
	private Map<String, String> fMCUTypes = null;
	private String[] fMCUTypesList = null;

	private IPreferenceStore fPrefs = null;

	/**
	 * Constructor for AVRHardwarePropertyPage.
	 */
	public AVRHardwarePropertyPage() {
		super();
	}

	@SuppressWarnings("unused")
	private void addConfigurationSection(Composite parent) {
		Composite composite = createDefaultComposite(parent);

		fPerConfigSelector = new Button(composite, SWT.TOGGLE);
		fPerConfigSelector.setText(TEXT_PERCONFIG);
		fPerConfigSelector.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}

			public void widgetSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
			}
		});
		
	}

	private void addMCUSection(Composite parent) {
		fMCUSection = createDefaultComposite(parent);

		// MCU Selection Combo
		Label comboLabel = new Label(fMCUSection, SWT.NONE);
		comboLabel.setText(LABEL_MCUTYPE);

		fMCUcombo = new Combo(fMCUSection, SWT.READ_ONLY | SWT.DROP_DOWN);
		fMCUcombo.setItems(fMCUTypesList);
		String currMCU = fPrefs.getString(AVRTargetProperties.KEY_MCUTYPE);
		fMCUcombo.select(fMCUcombo.indexOf(fMCUTypes.get(currMCU)));

		// FCPU Field
		Label fcpuLabel = new Label(fMCUSection, SWT.NONE);
		fcpuLabel.setText(LABEL_FCPU);

		fFCPUtext = new Text(fMCUSection, SWT.SINGLE | SWT.BORDER);
		GridData gd = new GridData();
		gd.widthHint = convertWidthInCharsToPixels(10);
		fFCPUtext.setLayoutData(gd);
		fFCPUtext.setTextLimit(8); // max. 99 MHz
		// TODO: Add a ModifyListener to warn on illegal values
		fFCPUtext
				.setText(fPrefs.getString(AVRTargetProperties.KEY_FCPU));
		fFCPUtext.setToolTipText("Target Hardware Clock Frequency in Hz");
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData();
		gridData.horizontalAlignment = GridData.FILL;
		gridData.grabExcessHorizontalSpace = true;
		separator.setLayoutData(gridData);
	}

	private void addSecondSection(Composite parent) {
		// Composite composite = createDefaultComposite(parent);

		// TODO: more hardware options
	}

	/**
	 * @see PreferencePage#createContents(Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {

		// Get the referenced project
		IProject project = null;
		IAdaptable element = getElement();
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof ICProject) {
			project = ((ICProject)element).getProject();
		} else {
			// Unknown resource type
			// TODO: Log Error
			return null;
		}
		
		// init the property store
		fPrefs = AVRTargetProperties.getPropertyStore(project);

		// Get the list of supported MCU Types
		fMCUTypes = GCC.getDefault().getToolInfo(GCC.TOOLINFOTYPE_MCUS);
		fMCUTypesList = new String[fMCUTypes.size()];
		fMCUTypesList = fMCUTypes.values().toArray(fMCUTypesList);
		Arrays.sort(fMCUTypesList);

		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		GridData data = new GridData(GridData.FILL);
		data.grabExcessHorizontalSpace = true;
		composite.setLayoutData(data);

		addMCUSection(composite);
		addSeparator(composite);
		addSecondSection(composite);
		return composite;
	}

	private Composite createDefaultComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		GridData data = new GridData();
		data.verticalAlignment = GridData.FILL;
		data.horizontalAlignment = GridData.FILL;
		composite.setLayoutData(data);

		return composite;
	}

	@Override
	protected void performDefaults() {
		// Restore the default values
		fMCUcombo.select(fMCUcombo.indexOf(fMCUTypes.get(fPrefs
				.getDefaultString(AVRTargetProperties.KEY_MCUTYPE))));
		fFCPUtext.setText(fPrefs
				.getDefaultString(AVRTargetProperties.KEY_FCPU));
	}

	@Override
	public boolean performOk() {
		// Get the id of the selected MCU and store it
		String selectedmcu = fMCUTypesList[fMCUcombo.getSelectionIndex()];
		for (String mcuid : fMCUTypes.keySet()) {
			if (selectedmcu.equals(fMCUTypes.get(mcuid))) {
				fPrefs.setValue(AVRTargetProperties.KEY_MCUTYPE, mcuid);
				break;
			}
		}

		// store the FCPU value
		fPrefs
				.setValue(AVRTargetProperties.KEY_FCPU, fFCPUtext
						.getText());
		
		try {
			AVRTargetProperties.savePreferences(fPrefs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// mark the project as dirty for a rebuild
		IManagedBuildInfo mbi = ManagedBuildManager
				.getBuildInfo((IResource) getElement());
		mbi.setDirty(true);
		mbi.setRebuildState(true);
		return true;
	}

}