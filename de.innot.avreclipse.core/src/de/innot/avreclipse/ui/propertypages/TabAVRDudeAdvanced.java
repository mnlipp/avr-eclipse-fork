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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import de.innot.avreclipse.core.properties.AVRDudeProperties;

/**
 * The AVRDude Actions Tab page.
 * <p>
 * On this tab, the following properties are edited:
 * <ul>
 * <li>The automatic verify check</li>
 * <li>The Signature check</li>
 * <li>The JTAG BitClock</li>
 * </ul>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class TabAVRDudeAdvanced extends AbstractAVRDudePropertyTab {

	// The GUI texts
	private final static String GROUP_NOVERIFY = "Verify Check";
	private final static String LABEL_NOVERIFY = "Disabling the automatic verify check will improve upload time at the risk of unnoticed upload errors.";
	private final static String TEXT_NOVERIFY = "Disable automatic verify check";

	private final static String GROUP_NOSIGCHECK = "Device Signature Check";
	private final static String LABEL_NOSIGCHECK = "Enable this if the target MCU has a broken (erased or overwritten) device signature\n"
	        + "but is otherwise operating normally.";
	private final static String TEXT_NOSIGCHECK = "Disable device signature check";

	private final static String GROUP_BITCLOCK = "JTAG ICE BitClock";
	private final static String LABEL_BITCLOCK = "Specify the bit clock period in microseconds for the JTAG interface or the ISP clock (JTAG ICE only).\n"
	        + "Set this to > 1.0 for target MCUs running with less than 4MHz on a JTAG ICE.\n"
	        + "Leave the field empty to use the preset bit clock period of the selected Programmer.";
	private final static String TEXT_BITCLOCK = "JTAG ICE bitclock";
	private final static String LABEL_BITCLOCK_UNIT = "µs";

	private final static String GROUP_DELAY = "BitBang Programmer Bit State Change Delay";
	private final static String LABEL_DELAY = "Specify the delay in microseconds for each bit change on bitbang-type programmers.\n"
	        + "Set this when the the host system is very fast, or the target runs off a slow clock\n"
	        + "Leave the field empty to run the ISP connection at max speed.";
	private final static String TEXT_DELAY = "Bit state change delay";
	private final static String LABEL_DELAY_UNIT = "µs";

	// The GUI widgets
	private Button fNoVerifyButton;
	private Button fNoSigCheckButton;
	private Text fBitClockText;
	private Text fBitBangDelayText;

	/** The Properties that this page works with */
	private AVRDudeProperties fTargetProps;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControls(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		addNoVerifySection(parent);

		addNoSignatureSection(parent);

		addBitClockSection(parent);

		addBitBangDelaySection(parent);

	}

	private void addNoVerifySection(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout(1, false));
		group.setText(GROUP_NOVERIFY);

		Label label = new Label(group, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		label.setText(LABEL_NOVERIFY);
		fNoVerifyButton = setupCheck(group, TEXT_NOVERIFY, 1, SWT.FILL);
	}

	private void addNoSignatureSection(Composite parent) {
		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout(1, false));
		group.setText(GROUP_NOSIGCHECK);

		Label label = new Label(group, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		label.setText(LABEL_NOSIGCHECK);
		fNoSigCheckButton = setupCheck(group, TEXT_NOSIGCHECK, 1, SWT.FILL);
	}

	private void addBitClockSection(Composite parent) {

		// TODO this could be replaced by a combo to select standard values.
		// Also this could be implemented as a frequency selector like in AVR
		// Studio

		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout(3, false));
		group.setText(GROUP_BITCLOCK);

		Label label = new Label(group, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		label.setText(LABEL_BITCLOCK);

		setupLabel(group, TEXT_BITCLOCK, 1, SWT.NONE);

		fBitClockText = new Text(group, SWT.BORDER | SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		FontMetrics fm = getFontMetrics(parent);
		gd.widthHint = Dialog.convertWidthInCharsToPixels(fm, 12);
		fBitClockText.setLayoutData(gd);
		fBitClockText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fTargetProps.setBitclock(fBitClockText.getText());
				updatePreview(fTargetProps);
			}
		});
		fBitClockText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				// Accept only digits and -at most- one dot '.'
				int dotcount = 0;
				if (fBitClockText.getText().contains(".")) {
					dotcount++;
				}
				String text = event.text;
				for (int i = 0; i < text.length(); i++) {
					char ch = text.charAt(i);
					if (ch == '.') {
						dotcount++;
						if (dotcount > 1) {
							event.doit = false;
							return;
						}
					} else if (!('0' <= ch && ch <= '9')) {
						event.doit = false;
						return;
					}
				}
			}
		});

		// Label with the units (microseconds)
		setupLabel(group, LABEL_BITCLOCK_UNIT, 1, SWT.FILL);
	}

	private void addBitBangDelaySection(Composite parent) {

		Group group = new Group(parent, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		group.setLayout(new GridLayout(3, false));
		group.setText(GROUP_DELAY);

		Label label = new Label(group, SWT.WRAP);
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 3, 1));
		label.setText(LABEL_DELAY);

		setupLabel(group, TEXT_DELAY, 1, SWT.NONE);

		fBitBangDelayText = new Text(group, SWT.BORDER | SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, false, false);
		FontMetrics fm = getFontMetrics(parent);
		gd.widthHint = Dialog.convertWidthInCharsToPixels(fm, 12);
		fBitBangDelayText.setLayoutData(gd);
		fBitBangDelayText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fTargetProps.setBitBangDelay(fBitBangDelayText.getText());
				updatePreview(fTargetProps);
			}
		});
		fBitBangDelayText.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				// Accept only digits
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

		// Label with the units (microseconds)
		setupLabel(group, LABEL_DELAY_UNIT, 1, SWT.FILL);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#checkPressed(org.eclipse.swt.events.SelectionEvent)
	 */
	@Override
	protected void checkPressed(SelectionEvent e) {
		// This is called for all checkbuttons / tributtons which have been set
		// up with the setupXXX() calls
		Control source = (Control) e.widget;
		if (source.equals(fNoVerifyButton)) {
			// No Verify checkbox selected
			boolean noverify = fNoVerifyButton.getSelection();
			fTargetProps.setNoVerify(noverify);
		} else if (source.equals(fNoSigCheckButton)) {
			// No Signature checkbox selected
			boolean nosigcheck = fNoSigCheckButton.getSelection();
			fTargetProps.setNoSigCheck(nosigcheck);
		}

		updatePreview(fTargetProps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performApply(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performApply(AVRDudeProperties dstprops) {

		if (fTargetProps == null) {
			// updataData() has not been called and this tab has no (modified)
			// settings yet.
			return;
		}

		// Copy the currently selected values of this tab to the given, fresh
		// Properties.
		// The caller of this method will handle the actual saving
		dstprops.setNoVerify(fTargetProps.getNoVerify());
		dstprops.setNoSigCheck(fTargetProps.getNoSigCheck());
		dstprops.setBitclock(fTargetProps.getBitclock());
		dstprops.setBitBangDelay(fTargetProps.getBitBangDelay());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#performDefaults(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void performCopy(AVRDudeProperties srcprops) {

		// Reload the items on this page
		fTargetProps.setNoVerify(srcprops.getNoVerify());
		fTargetProps.setNoSigCheck(srcprops.getNoSigCheck());
		fTargetProps.setBitclock(srcprops.getBitclock());
		fTargetProps.setBitBangDelay(srcprops.getBitBangDelay());
		updateData(fTargetProps);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPropertyTab#updateData(de.innot.avreclipse.core.preferences.AVRProjectProperties)
	 */
	@Override
	protected void updateData(AVRDudeProperties props) {

		fTargetProps = props;

		// Update the GUI widgets on this Tab.
		fNoVerifyButton.setSelection(fTargetProps.getNoVerify());
		fNoSigCheckButton.setSelection(fTargetProps.getNoSigCheck());
		fBitClockText.setText(fTargetProps.getBitclock());
		fBitBangDelayText.setText(fTargetProps.getBitBangDelay());
	}
}
