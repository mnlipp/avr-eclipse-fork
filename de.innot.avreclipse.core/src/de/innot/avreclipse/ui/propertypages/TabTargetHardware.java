/*******************************************************************************
 * 
 * Copyright (c) 2008 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.ui.propertypages;

import java.util.Arrays;
import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

import de.innot.avreclipse.core.preferences.AVRProjectProperties;
import de.innot.avreclipse.core.toolinfo.AVRDude;
import de.innot.avreclipse.core.toolinfo.GCC;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * This tab handles setting of all target hardware related properties.
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class TabTargetHardware extends AbstractAVRPropertyTab {

	private static final String LABEL_MCUTYPE = "MCU Type";
	private static final String LABEL_FCPU = "MCU Clock Frequency";

	/** List of common MCU frequencies (taken from mfile) */
	private static final String[] FCPU_VALUES = { "1000000", "1843200", "2000000", "3686400",
	        "4000000", "7372800", "8000000", "11059200", "14745600", "16000000", "18432000",
	        "20000000" };

	/** The Properties that this page works with */
	private AVRProjectProperties fTargetCfg;

	private Combo fMCUcombo;
	private Composite fMCUWarningComposite;

	private Combo fFCPUcombo;

	private Set<String> fMCUids;
	private String[] fMCUNames;

	private static final Image IMG_WARN = PlatformUI.getWorkbench().getSharedImages().getImage(
	        ISharedImages.IMG_OBJS_WARN_TSK);

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(2, false));

		// Get the list of supported MCU id's from the compiler
		// The list is then converted into an array of MCU names
		//
		// If we ever implement per project paths this needs to be moved to the
		// updataData() method to reload the list of supported mcus every time
		// the paths change. The list is added to the combo in addMCUSection().
		if (fMCUids == null) {
			fMCUids = GCC.getDefault().getMCUList();
			String[] allmcuids = fMCUids.toArray(new String[fMCUids.size()]);
			fMCUNames = new String[fMCUids.size()];
			for (int i = 0; i < allmcuids.length; i++) {
				fMCUNames[i] = AVRMCUidConverter.id2name(allmcuids[i]);
			}
			Arrays.sort(fMCUNames);
		}

		addMCUSection(usercomp);
		addFCPUSection(usercomp);
		addSeparator(usercomp);

	}

	private void addMCUSection(Composite parent) {

		GridData gd = new GridData();
		FontMetrics fm = getFontMetrics(parent);
		gd.widthHint = Dialog.convertWidthInCharsToPixels(fm, 12);

		// MCU Selection Combo
		setupLabel(parent, LABEL_MCUTYPE, 1, SWT.NONE);

		fMCUcombo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
		fMCUcombo.setLayoutData(gd);
		fMCUcombo.setItems(fMCUNames);
		fMCUcombo.setVisibleItemCount(Math.min(fMCUNames.length, 20));

		fMCUcombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String mcuname = fMCUcombo.getItem(fMCUcombo.getSelectionIndex());
				String mcuid = AVRMCUidConverter.name2id(mcuname);
				fTargetCfg.setMCUId(mcuid);

				// Check if supported by avrdude and set the errorpane as
				// required
				checkAVRDude(mcuid);
			}
		});

		fMCUWarningComposite = new Composite(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		fMCUWarningComposite.setLayoutData(gd);
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 0;
		gl.marginWidth = 0;
		gl.verticalSpacing = 0;
		gl.horizontalSpacing = 0;
		fMCUWarningComposite.setLayout(gl);

		Label warnicon = new Label(fMCUWarningComposite, SWT.LEFT);
		warnicon.setLayoutData(new GridData(GridData.BEGINNING));
		warnicon.setImage(IMG_WARN);

		Label warnmessage = new Label(fMCUWarningComposite, SWT.LEFT);
		warnmessage.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		warnmessage.setText("This MCU is not supported by AVRDude");

		fMCUWarningComposite.setVisible(false);
	}

	private void addFCPUSection(Composite parent) {

		GridData gd = new GridData();
		FontMetrics fm = getFontMetrics(parent);
		gd.widthHint = Dialog.convertWidthInCharsToPixels(fm, 12);

		setupLabel(parent, LABEL_FCPU, 1, SWT.NONE);

		fFCPUcombo = new Combo(parent, SWT.DROP_DOWN);
		fFCPUcombo.setLayoutData(gd);
		fFCPUcombo.setTextLimit(8); // max. 99 MHz
		fFCPUcombo.setToolTipText("Target Hardware Clock Frequency in Hz");
		fFCPUcombo.setVisibleItemCount(FCPU_VALUES.length);
		fFCPUcombo.setItems(FCPU_VALUES);

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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performApply(de.innot.avreclipse.core.preferences.AVRConfigurationProperties,
	 *      de.innot.avreclipse.core.preferences.AVRConfigurationProperties)
	 */
	@Override
	protected void performApply(AVRProjectProperties dst) {
		dst.setMCUId(fTargetCfg.getMCUId());
		dst.setFCPU(fTargetCfg.getFCPU());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performDefaults(AVRProjectProperties defaults) {
		fTargetCfg.setMCUId(defaults.getMCUId());
		fTargetCfg.setFCPU(defaults.getFCPU());
		updateData(fTargetCfg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#updateData(de.innot.avreclipse.core.preferences.AVRConfigurationProperties)
	 */
	@Override
	protected void updateData(AVRProjectProperties cfg) {

		fTargetCfg = cfg;

		String mcuid = cfg.getMCUId();
		fMCUcombo.select(fMCUcombo.indexOf(AVRMCUidConverter.id2name(mcuid)));
		checkAVRDude(mcuid);

		fFCPUcombo.setText(cfg.getFCPU());

	}

	/**
	 * Check if the given MCU is supported by avrdude and set visibility of the
	 * MCU Warning Message accordingly.
	 * 
	 * @param mcuid The MCU id value to test
	 */
	private void checkAVRDude(String mcuid) {
		if (AVRDude.getDefault().hasMCU(mcuid)) {
			fMCUWarningComposite.setVisible(false);
		} else {
			fMCUWarningComposite.setVisible(true);
		}
	}
}
