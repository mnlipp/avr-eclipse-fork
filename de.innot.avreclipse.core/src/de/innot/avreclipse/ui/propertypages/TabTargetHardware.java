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

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.IProgressService;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
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
	private static final String TEXT_LOADBUTTON = "Load from Programmer";

	/** List of common MCU frequencies (taken from mfile) */
	private static final String[] FCPU_VALUES = { "1000000", "1843200",
			"2000000", "3686400", "4000000", "7372800", "8000000", "11059200",
			"14745600", "16000000", "18432000", "20000000" };

	/** The Properties that this page works with */
	private AVRProjectProperties fTargetCfg;

	private Combo fMCUcombo;
	private Composite fMCUWarningComposite;

	private Combo fFCPUcombo;

	private Set<String> fMCUids;
	private String[] fMCUNames;

	private String fOldMCUid;
	private String fOldFCPU;

	private static final Image IMG_WARN = PlatformUI.getWorkbench()
			.getSharedImages().getImage(ISharedImages.IMG_OBJS_WARN_TSK);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		usercomp.setLayout(new GridLayout(4, false));

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

		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
		FontMetrics fm = getFontMetrics(parent);
		gd.widthHint = Dialog.convertWidthInCharsToPixels(fm, 12);

		// MCU Selection Combo
		Label label = new Label(parent, SWT.NONE);
		label.setText(LABEL_MCUTYPE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));

		fMCUcombo = new Combo(parent, SWT.READ_ONLY | SWT.DROP_DOWN);
		fMCUcombo.setLayoutData(gd);
		fMCUcombo.setItems(fMCUNames);
		fMCUcombo.setVisibleItemCount(Math.min(fMCUNames.length, 20));

		fMCUcombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String mcuname = fMCUcombo.getItem(fMCUcombo
						.getSelectionIndex());
				String mcuid = AVRMCUidConverter.name2id(mcuname);
				fTargetCfg.setMCUId(mcuid);

				// Check if supported by avrdude and set the errorpane as
				// required
				checkAVRDude(mcuid);

				// Set the rebuild flag for the configuration
				getCfg().setRebuildState(true);
			}
		});

		// Load from Device Button
		Button loadbutton = setupButton(parent, TEXT_LOADBUTTON, 1, SWT.NONE);
		loadbutton
				.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		loadbutton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadComboFromDevice();
			}
		});

		// Dummy Label for Padding
		label = new Label(parent, SWT.NONE);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));

		// The Warning Composite
		fMCUWarningComposite = new Composite(parent, SWT.NONE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
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

		// Ensure that only integer values are entered
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

		if (fTargetCfg == null) {
			// Do nothing if the Target properties do not exist.
			return;
		}
		String newMCUid = fTargetCfg.getMCUId();
		String newFCPU = fTargetCfg.getFCPU();

		dst.setMCUId(newMCUid);
		dst.setFCPU(newFCPU);

		// Check if a rebuild is required
		checkRebuildRequired();

		fOldMCUid = newMCUid;
		fOldFCPU = newFCPU;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performCopy(AVRProjectProperties defaults) {
		fTargetCfg.setMCUId(defaults.getMCUId());
		fTargetCfg.setFCPU(defaults.getFCPU());
		updateData(fTargetCfg);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performOK()
	 */
	@Override
	protected void performOK() {
		// We override this to set the rebuild state as required
		checkRebuildRequired();
		super.performOK();
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

		String fcpu = cfg.getFCPU();
		fFCPUcombo.setText(fcpu);

		// Save the original values, so we can set the rebuild flag when any
		// changes are applied.
		fOldMCUid = mcuid;
		fOldFCPU = fcpu;

	}

	/**
	 * Check if the given MCU is supported by avrdude and set visibility of the
	 * MCU Warning Message accordingly.
	 * 
	 * @param mcuid
	 *            The MCU id value to test
	 */
	private void checkAVRDude(String mcuid) {
		if (AVRDude.getDefault().hasMCU(mcuid)) {
			fMCUWarningComposite.setVisible(false);
		} else {
			fMCUWarningComposite.setVisible(true);
		}
	}

	/**
	 * Checks if the current target values are different from the original ones
	 * and set the rebuild flag for the configuration / project if yes.
	 */
	private void checkRebuildRequired() {
		if (fOldMCUid != null) {
			if (!(fTargetCfg.getMCUId().equals(fOldMCUid))
					|| !(fTargetCfg.getFCPU().equals(fOldFCPU))) {
				setRebuildState(true);
			}
		}
	}

	/**
	 * Load the actual MCU from the currently selected Programmer and set the
	 * MCU combo accordingly.
	 * <p>
	 * This method shows a busy cursor or a progress dialog as required
	 * </p>
	 */
	private void loadComboFromDevice() {
		final ProgrammerConfig config = fTargetCfg.getAVRDudeProgrammer();
		if (config == null) {
			// TODO Display Dialog to select a Programmer
			return;
		}

		IProgressService progressService = PlatformUI.getWorkbench()
				.getProgressService();
		MCUFromDeviceRunner runner = new MCUFromDeviceRunner(config);
		try {
			progressService.busyCursorWhile(runner);
		} catch (InvocationTargetException e) {
			// This is an wrapper for an AVRDudeException
			// Get the root Exception and display an Error Message
			Throwable cause = e.getCause();
			AVRDudeErrorDialog.openAVRDudeError(usercomp.getShell(), cause,
					config);
			return;
		} catch (InterruptedException e) {
			// User has canceled - do nothing
			return;
		}
		String mcuid = runner.getMCU();
		if (mcuid == null) {
			// TODO Open Error dialog
			return;
		}

		fMCUcombo.select(fMCUcombo.indexOf(AVRMCUidConverter.id2name(mcuid)));
		checkAVRDude(mcuid);
	}

	private final class MCUFromDeviceRunner implements IRunnableWithProgress {

		private ProgrammerConfig fConfig;
		private String fMCU;

		public MCUFromDeviceRunner(ProgrammerConfig config) {
			fConfig = config;
		}

		public void run(IProgressMonitor monitor)
				throws InvocationTargetException {
			try {
				fMCU = AVRDude.getDefault().getAttachedMCU(fConfig);
			} catch (AVRDudeException e) {
				throw new InvocationTargetException(e);
			}
		}

		public String getMCU() {
			return fMCU;
		}
	}
}
