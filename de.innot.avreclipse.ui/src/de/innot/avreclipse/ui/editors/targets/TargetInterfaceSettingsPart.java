/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.ui.editors.targets;

import java.text.MessageFormat;
import java.util.Arrays;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scale;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.ITargetConfigConstants;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetInterface;
import de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TargetInterfaceSettingsPart extends SectionPart implements
		ITargetConfigChangeListener, ITargetConfigConstants {

	private ITargetConfigurationWorkingCopy	fTCWC;

	private Composite						fContent;

	private Label							fFreqText;

	private Composite						fWarningCompo;

	private String							fOldJTAGRate;
	private String							fOldJTAGDaisyChain;

	private Text[]							fDaisyChainTexts	= new Text[4];
	private String[]						fDaisyChainSettings	= new String[4];

	private final static int				UNITS_BEFORE		= 0;
	private final static int				UNITS_AFTER			= 1;
	private final static int				BITS_BEFORE			= 2;
	private final static int				BITS_AFTER			= 3;

	private int[]							fClockValues;

	/**
	 * @param parent
	 * @param toolkit
	 */
	public TargetInterfaceSettingsPart(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.SHORT_TITLE_BAR | Section.TWISTIE | Section.EXPANDED
				| Section.CLIENT_INDENT);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);

		FormToolkit toolkit = form.getToolkit();
		fContent = toolkit.createComposite(getSection());
		fContent.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		TableWrapLayout layout = new TableWrapLayout();
		fContent.setLayout(layout);

		getSection().setClient(fContent);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#setFormInput(java.lang.Object)
	 */
	@Override
	public boolean setFormInput(Object input) {
		if (!(input instanceof ITargetConfigurationWorkingCopy)) {
			return false;
		}

		fTCWC = (ITargetConfigurationWorkingCopy) input;

		// Add a listener for attribute changes.
		// If the ATTR_PROGRAMMER_ID is changed then
		// mark the form as stale which will cause a call to refresh() which
		// in turn will redraw this section with the appropriate options
		fTCWC.addPropertyChangeListener(this);

		fOldJTAGRate = fTCWC.getAttribute(ATTR_JTAG_CLOCK);
		fOldJTAGDaisyChain = fTCWC.getAttribute(ATTR_JTAG_DAISYCHAIN);

		refresh();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener#attributeChange
	 * (de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	public void attributeChange(ITargetConfiguration config, String attribute, String oldvalue,
			String newvalue) {
		// Check if the image loader or the gdbserver have changed
		if (ATTR_PROGRAMMER_ID.equals(attribute)) {
			markStale();
		}

		// Check if the FCPU value has changed
		if (ATTR_FCPU.equals(attribute)) {
			updateBitClockWarning();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (!fTCWC.getAttribute(ATTR_JTAG_CLOCK).equals(fOldJTAGRate)) {
			return true;
		}

		if (!fTCWC.getAttribute(ATTR_JTAG_DAISYCHAIN).equals(fOldJTAGDaisyChain)) {
			return true;
		}

		return super.isDirty();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave) {
		// The actual saving is done somewhere upstream.

		// But remember the current settings for the
		// dirty state tracking
		fOldJTAGRate = fTCWC.getAttribute(ATTR_JTAG_CLOCK);
		fOldJTAGDaisyChain = fTCWC.getAttribute(ATTR_JTAG_DAISYCHAIN);

		super.commit(onSave);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
	 */
	@Override
	public void dispose() {
		// remove the listener
		fTCWC.removePropertyChangeListener(this);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
	 */
	@Override
	public void refresh() {
		super.refresh();
		if (fTCWC == null) {
			return; // not initialized yet
		}

		// remove all previous controls from the section
		Control[] children = fContent.getChildren();
		for (Control child : children) {
			child.dispose();
		}
		// and clear warnings
		if (fFreqText != null) {
			getManagedForm().getMessageManager().removeMessage(fFreqText, fFreqText);
		}

		// This is required because without it the layout gets
		fContent.update();
		getManagedForm().reflow(true);

		// Set the title of the section
		String programmerid = fTCWC.getAttribute(ATTR_PROGRAMMER_ID);
		IProgrammer programmer = fTCWC.getProgrammer(programmerid);
		TargetInterface currTI = programmer.getTargetInterface();
		String title = MessageFormat.format("{0} Settings", currTI.toString());
		getSection().setText(title);
		getSection().redraw();

		fClockValues = programmer.getTargetInterfaceClockFrequencies();

		// And rebuild the section
		FormToolkit toolkit = getManagedForm().getToolkit();

		Section section = null;

		if (fClockValues.length != 0) {
			section = addClockSection(fContent, toolkit);
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		}

		if (currTI.equals(TargetInterface.JTAG)) {
			section = addJTAGDaisyChainSection(fContent, toolkit);
			section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		}

		if (section == null) {
			// the selected target interface has no options
			Label label = toolkit.createLabel(fContent,
					"The selected progrmmer has no user changeable settings for the "
							+ currTI.toString() + " target interface", SWT.WRAP);
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		}
		getManagedForm().reflow(true);

	}

	/**
	 * @param parent
	 * @param toolkit
	 */
	private Section addClockSection(Composite parent, FormToolkit toolkit) {

		// First check if the programmer supports settable clock frequencies

		Section section = toolkit.createSection(parent, Section.TWISTIE | Section.CLIENT_INDENT);
		section.setLayout(new TableWrapLayout());
		section.setText("Clock Frequency");

		String desc = "The clock frequency must not be higher that 1/4 of "
				+ "the target MCU clock frequency. The default value depends on the "
				+ "selected tool, but is usually 1 MHz, suitable for target MCUs running "
				+ "at 4 MHz or above.";

		String jtagclock = fTCWC.getAttribute(ATTR_JTAG_CLOCK);
		if (jtagclock.length() == 0) {
			// If there has been no value then collapse this section
			section.setExpanded(false);
		} else {
			section.setExpanded(true);
		}

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new TableWrapLayout());

		Label description = toolkit.createLabel(sectionClient, desc, SWT.WRAP);
		description.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite content = toolkit.createComposite(sectionClient);
		content.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		GridLayout gl = new GridLayout(2, false);
		gl.horizontalSpacing = 12;
		content.setLayout(gl);

		final Scale scale = new Scale(content, SWT.HORIZONTAL);
		toolkit.adapt(scale, true, true);
		scale.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		scale.addSelectionListener(new SelectionAdapter() {

			/*
			 * (non-Javadoc)
			 * @seeorg.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.
			 * SelectionEvent)
			 */
			@Override
			public void widgetSelected(SelectionEvent e) {
				int index = scale.getSelection();
				int value = fClockValues[index];
				updateBitClockValue(value);
			}

		});

		// 
		int units = fClockValues.length;
		scale.setMaximum(units - 1);
		scale.setMinimum(0);
		scale.setIncrement(1);
		scale.setPageIncrement(units < 25 ? 1 : 10);

		//
		// The frequency display.
		// This is just a label with an optimized width.
		// To get the correct width we create a Graphics Context (GC) for the label and then use the
		// stringExtend() method to calculate the size of a (hopefully) maximum length content.
		//
		// Instantiating a new GC might be a bit expensive, but this will only be executed when
		// the programmer is changed, i.e not too often.
		fFreqText = toolkit.createLabel(content, "default", SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		GC gc = new GC(fFreqText);
		gd.widthHint = gc.stringExtent("8.888 MHz").x;
		gc.dispose();
		fFreqText.setLayoutData(gd);

		//
		// The BitClock > 1/4 FCPU warning display
		//
		fWarningCompo = toolkit.createComposite(content);
		fWarningCompo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false, 2, 1));
		fWarningCompo.setLayout(new GridLayout(2, false));
		Label image = toolkit.createLabel(fWarningCompo, "");
		image.setImage(PlatformUI.getWorkbench().getSharedImages().getImage(
				ISharedImages.IMG_OBJS_WARN_TSK));
		image.setLayoutData(new GridData(SWT.BEGINNING, SWT.NONE, false, false));

		Label warning = toolkit.createLabel(fWarningCompo,
				"The selected BitClock Frequency is greater than 1/4th of the target MCU Clock");
		warning.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		fWarningCompo.setVisible(false);

		// Finally set the scale and the label to the current setting (or the next lower if a
		// different ClockValues table is used)
		String valuetxt = fTCWC.getAttribute(ATTR_JTAG_CLOCK);
		int value = 0;
		if (valuetxt.length() > 0) {
			value = Integer.parseInt(valuetxt);
		}

		// find next lower value
		int lastv = 0;
		int index = 0;
		for (; index < fClockValues.length; index++) {
			if (fClockValues[index] <= value) {
				lastv = fClockValues[index];
			} else {
				break;
			}
		}

		scale.setSelection(index - 1);
		updateBitClockValue(lastv);

		// Now tell the section about its content.
		section.setClient(sectionClient);

		return section;
	}

	/**
	 * @param value
	 */
	private void updateBitClockValue(int value) {

		// Set the attribute
		if (value != 0) {
			fTCWC.setAttribute(ATTR_JTAG_CLOCK, Integer.toString(value));
		} else {
			fTCWC.setAttribute(ATTR_JTAG_CLOCK, "");
		}
		getManagedForm().dirtyStateChanged();

		// update the frequency display label

		fFreqText.setText(convertValueToFrequency(value));

		updateBitClockWarning();

	}

	private String convertValueToFrequency(int value) {
		String text;
		if (value == 0) {
			text = "default";
		} else if (value < 1000) {
			text = value + " Hz";
		} else if (value < 1000000) {
			float newvalue = value / 1000.0F;
			text = newvalue + " KHz";
		} else {
			float newvalue = value / 1000000.0F;
			text = newvalue + " MHz";
		}
		return text;
	}

	private void updateBitClockWarning() {
		// Check if the selected clock is over 1/4th the target FCPU value
		String bitclock = fTCWC.getAttribute(ATTR_JTAG_CLOCK);
		if (bitclock.length() > 0) {

			int value = Integer.parseInt(bitclock);
			int targetfcpu = fTCWC.getFCPU();
			if (value > targetfcpu / 4) {

				fWarningCompo.setVisible(value > targetfcpu / 4);

				String msg = MessageFormat
						.format(
								"selected BitClock Frequency of {0} is greater than 1/4th of the target MCU Clock",
								convertValueToFrequency(value));
				getManagedForm().getMessageManager().addMessage(fFreqText, msg, null,
						IMessageProvider.WARNING, fFreqText);
				return;
			}

		}
		getManagedForm().getMessageManager().removeMessage(fFreqText, fFreqText);
		fWarningCompo.setVisible(false);

	}

	/**
	 * @param parent
	 * @param toolkit
	 */
	private Section addJTAGDaisyChainSection(Composite parent, FormToolkit toolkit) {

		String desc = "These settings are required if the target MCU is part of a JTAG daisy chain.\n"
				+ "Set the number of devices before and after the target MCU in the chain "
				+ "and the accumulated number of instruction bits they use. AVR devices use "
				+ "4 instruction bits, but other JTAG devices may differ. \n"
				+ "Note: JTAG daisy chains are only supported by some Programmers.";

		Section section = toolkit.createSection(parent, Section.TWISTIE | Section.CLIENT_INDENT);
		section.setText("Daisy Chain");

		String daisychain = fTCWC.getAttribute(ATTR_JTAG_DAISYCHAIN);
		if (daisychain.length() == 0 || daisychain.equals("0,0,0,0")) {
			// If there has been no value then collapse this section
			section.setExpanded(false);
		} else {
			section.setExpanded(true);
		}

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new TableWrapLayout());

		Label description = toolkit.createLabel(sectionClient, desc, SWT.WRAP);
		description.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite content = toolkit.createComposite(sectionClient);
		content.setLayoutData(new TableWrapData(TableWrapData.FILL));
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = 12;
		content.setLayout(layout);

		// Parse the daisychain string and set the
		// globals.
		if (daisychain.length() > 0) {
			fDaisyChainSettings = daisychain.split(",");
			if (fDaisyChainSettings.length != 4) {
				// the attribute has been corrupted -- restore to defaults
				fDaisyChainSettings = new String[4];
				Arrays.fill(fDaisyChainSettings, "0");
			}
		} else {
			Arrays.fill(fDaisyChainSettings, "0");
		}

		createDCTextField(content, "Devices before:", UNITS_BEFORE);
		createDCTextField(content, "Instruction bits before:", BITS_BEFORE);

		createDCTextField(content, "Devices after:", UNITS_AFTER);
		createDCTextField(content, "Instruction bits after:", BITS_AFTER);

		section.setClient(sectionClient);

		updateDaisyChainValues();

		return section;
	}

	private void createDCTextField(Composite parent, String labeltext, int index) {

		FormToolkit toolkit = getManagedForm().getToolkit();

		final ModifyListener modifylistener = new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// Get the index of the text field
				int index = (Integer) e.widget.getData();

				// and its value
				String value = ((Text) e.widget).getText();
				if (value.length() == 0) {
					value = "0";
				}

				fDaisyChainSettings[index] = value;
				updateDaisyChainValues();
			}
		};

		// The verify listener to restrict the input to integers
		final VerifyListener verifylistener = new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				String text = event.text;
				if (!text.matches("[0-9]*")) {
					event.doit = false;
				}
			}
		};

		toolkit.createLabel(parent, labeltext);

		Text text = toolkit.createText(parent, fDaisyChainSettings[index]);
		GridData gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.widthHint = calcTextWidth(text, "888");
		text.setLayoutData(gd);
		text.setTextLimit(3);
		text.setData(Integer.valueOf(index));
		text.addModifyListener(modifylistener);
		text.addVerifyListener(verifylistener);

		fDaisyChainTexts[index] = text;

	}

	private void updateDaisyChainValues() {
		int values[] = new int[4];
		StringBuilder sb = new StringBuilder(16);
		boolean isEmpty = true;
		for (int i = 0; i < 4; i++) {
			String value = fDaisyChainSettings[i];
			if (value.length() == 0) {
				value = "0";
			} else {
				isEmpty = false;
			}

			values[i] = Integer.parseInt(value);

			sb.append(value);
			if (i < 3) {
				sb.append(",");
			}
		}

		String result = isEmpty ? "" : sb.toString();
		fTCWC.setAttribute(ATTR_JTAG_DAISYCHAIN, result);
		validateDaisyChainSettings(values);
		getManagedForm().dirtyStateChanged();
	}

	private void validateDaisyChainSettings(int values[]) {
		IMessageManager mmngr = getManagedForm().getMessageManager();

		Text textctrl = fDaisyChainTexts[BITS_BEFORE];
		if (values[BITS_BEFORE] > 255) {
			mmngr.addMessage(textctrl, "Daisy chain 'bits before' out of range (0 - 255)", null,
					IMessageProvider.ERROR, textctrl);
		} else {
			mmngr.removeMessage(textctrl, textctrl);
		}

		textctrl = fDaisyChainTexts[BITS_AFTER];
		if (values[BITS_AFTER] > 255) {
			mmngr.addMessage(textctrl, "Daisy chain 'bits after' out of range (0 - 255)", null,
					IMessageProvider.ERROR, textctrl);
		} else {
			mmngr.removeMessage(textctrl, textctrl);
		}

		textctrl = fDaisyChainTexts[UNITS_BEFORE];
		if (values[UNITS_BEFORE] > values[BITS_BEFORE]) {
			mmngr.addMessage(textctrl, "Daisy chain 'Devices before' greater than 'bits before'",
					null, IMessageProvider.ERROR, textctrl);
		} else {
			mmngr.removeMessage(textctrl, textctrl);
		}

		textctrl = fDaisyChainTexts[UNITS_AFTER];
		if (values[UNITS_AFTER] > values[BITS_AFTER]) {
			mmngr.addMessage(textctrl, "Daisy chain 'Devices after' greater than 'bits after'",
					null, IMessageProvider.ERROR, textctrl);
		} else {
			mmngr.removeMessage(textctrl, textctrl);
		}

	}

	private int calcTextWidth(Control control, String text) {
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		int value = gc.stringExtent("8888888888").x;
		gc.dispose();

		return value;
	}

}
