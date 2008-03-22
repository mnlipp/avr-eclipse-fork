/**
 * 
 */
package de.innot.avreclipse.ui.propertypages;

import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.innot.avreclipse.core.preferences.AVRProjectProperties;

/**
 * @author U043192
 * 
 */
public class TargetHardwareTab extends AbstractAVRPropertyTab {

	private static final String LABEL_MCUTYPE = "MCU Type";
	private static final String LABEL_FCPU = "MCU Clock Frequency";

	private static final String[] FCPU_VALUES = { "1000000", "1843200", "2000000", "3686400",
	        "4000000", "7372800", "8000000", "11059200", "14745600", "16000000", "18432000",
	        "20000000" };

	private AVRProjectProperties fTargetCfg;

	private Combo fMCUcombo;
	private Combo fFCPUcombo;
	private Set<String> fMCUids;
	private String[] fMCUNames;

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));

		addMCUSection(usercomp);
		addSeparator(usercomp);
	}

	private void addMCUSection(Composite parent) {

		GridData gd = new GridData();
		FontMetrics fm = getFontMetrics(parent);
		gd.widthHint = Dialog.convertWidthInCharsToPixels(fm, 12);

		// MCU Selection Combo
		Label comboLabel = new Label(parent, SWT.NONE);
		comboLabel.setText(LABEL_MCUTYPE);

		fMCUcombo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
		fMCUcombo.setLayoutData(gd);

		// FCPU Field
		Label fcpuLabel = new Label(parent, SWT.NONE);
		fcpuLabel.setText(LABEL_FCPU);

		fFCPUcombo = new Combo(parent, SWT.DROP_DOWN);
		fFCPUcombo.setLayoutData(gd);
		fFCPUcombo.setTextLimit(8); // max. 99 MHz
		fFCPUcombo.setToolTipText("Target Hardware Clock Frequency in Hz");
		fFCPUcombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				if (fTargetCfg != null) {
					fTargetCfg.setFCPU(fFCPUcombo.getText());
				}
			}
		});
		fFCPUcombo.addVerifyListener(new VerifyListener() {

			public void verifyText(VerifyEvent event) {
				String text = event.text;
				if (!text.matches("[0-9]*")) {
					event.doit = false;
				}
            }
		});
		fFCPUcombo.setVisibleItemCount(FCPU_VALUES.length);
		fFCPUcombo.setItems(FCPU_VALUES);
	}

	/* (non-Javadoc)
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performApply(de.innot.avreclipse.core.preferences.AVRConfigurationProperties, de.innot.avreclipse.core.preferences.AVRConfigurationProperties)
	 */
	@Override
	protected void performApply(AVRProjectProperties src, AVRProjectProperties dst) {
		dst.setMCUId(src.getMCUId());
		dst.setFCPU(src.getFCPU());
	}

	/* (non-Javadoc)
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
    protected void performDefaults(AVRProjectProperties defaults) {
		fTargetCfg.setMCUId(defaults.getMCUId());
		fTargetCfg.setFCPU(defaults.getFCPU());
		updateData(fTargetCfg);
	}

	/* (non-Javadoc)
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#updateData(de.innot.avreclipse.core.preferences.AVRConfigurationProperties)
	 */
	@Override
	protected void updateData(AVRProjectProperties cfg) {

		fTargetCfg = cfg;
		
		fFCPUcombo.setText(cfg.getFCPU());

	}

}
