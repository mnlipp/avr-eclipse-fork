/**
 * 
 */
package de.innot.avreclipse.ui.propertypages;

import java.util.Set;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.innot.avreclipse.core.preferences.TargetConfiguration;

/**
 * @author U043192
 * 
 */
public class TargetHardwareTab extends AbstractCBuildPropertyTab {

	private static final String LABEL_MCUTYPE = "MCU Type";
	private static final String LABEL_FCPU = "MCU Clock Frequency";

	private static final String[] FCPU_VALUES = { "1000000", "1843200", "2000000", "3686400",
	        "4000000", "7372800", "8000000", "11059200", "14745600", "16000000", "18432000",
	        "20000000" };

	private AbstractAVRPage fPage;

	private TargetConfiguration fTargetCfg;

	private Combo fMCUcombo;
	private Combo fFCPUcombo;
	private Set<String> fMCUids;
	private String[] fMCUNames;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.cdt.ui.newui.ICPropertyProvider)
	 */
	@Override
	public void createControls(Composite parent, ICPropertyProvider provider) {
		if (provider instanceof AbstractAVRPage) {
			fPage = (AbstractAVRPage) provider;
		} else {
			return;
		}
		super.createControls(parent, provider);
	}

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));

		addMCUSection(usercomp);
		addSeparator(usercomp);

		updateData(null);
	}

	private void addMCUSection(Composite parent) {
		// MCU Selection Combo
		Label comboLabel = new Label(parent, SWT.NONE);
		comboLabel.setText(LABEL_MCUTYPE);

		fMCUcombo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);

		// FCPU Field
		Label fcpuLabel = new Label(parent, SWT.NONE);
		fcpuLabel.setText(LABEL_FCPU);

		fFCPUcombo = new Combo(parent, SWT.DROP_DOWN);
		GridData gd = new GridData();
		gd.widthHint = fPage.convertCharToPixel(10);
		fFCPUcombo.setLayoutData(gd);
		fFCPUcombo.setTextLimit(8); // max. 99 MHz
		// TODO: Add a ModifyListener to warn on illegal values
		fFCPUcombo.setToolTipText("Target Hardware Clock Frequency in Hz");
		fFCPUcombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (fTargetCfg != null) {
					fTargetCfg.setFCPU(fFCPUcombo.getText());
				}
			}
		});
		fFCPUcombo.setVisibleItemCount(FCPU_VALUES.length);
		fFCPUcombo.setItems(FCPU_VALUES);
	}

	private void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1);
		separator.setLayoutData(gridData);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription,
	 *      org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {
		// Do nothing
		// Saving is handled in the AbstractAVRPage class
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateButtons()
	 */
	@Override
	protected void updateButtons() {
		// We don't use any buttons
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateData(org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void updateData(ICResourceDescription cfg) {

		if (fPage.isConfigSetting()) {
			// We are on a per config setting
			if (cfg != null) {
				fTargetCfg = fPage.getTargetConfiguration(cfg);
			}
		} else {
			// We have a per Project Setting
			fTargetCfg = fPage.getTargetConfiguration(null);
		}

		fFCPUcombo.setText(fTargetCfg.getFCPU());

	}

}
