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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.HostInterface;
import de.innot.avreclipse.core.targets.ITargetConfigConstants;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class HostInterfaceSettingsPart extends SectionPart implements ITargetConfigChangeListener,
		ITargetConfigConstants {

	private final static String[]			BAUDRATES	= new String[] { "1200", "2400", "4800",
			"9600", "19200", "38400", "57600", "115200", "230400", "" };

	private ITargetConfigurationWorkingCopy	fTCWC;

	private Composite						fSectionClient;

	private String							fOldPort;
	private String							fOldBaudRate;
	private String							fOldBitBangDelay;
	private String							fOldExitSpecs;
	private String							fOldUSBDelay;

	private String							fExitSpecReset;
	private String							fExitSpecVcc;

	/**
	 * @param parent
	 * @param toolkit
	 */
	public HostInterfaceSettingsPart(Composite parent, FormToolkit toolkit) {
		super(parent, toolkit, Section.SHORT_TITLE_BAR | Section.TWISTIE | Section.EXPANDED
				| Section.CLIENT_INDENT);
		fSectionClient = toolkit.createComposite(getSection());
		TableWrapLayout layout = new TableWrapLayout();
		layout.numColumns = 2;
		fSectionClient.setLayout(layout);

		getSection().setClient(fSectionClient);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);

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

		fOldPort = fTCWC.getAttribute(ATTR_PROGRAMMER_PORT);
		fOldBaudRate = fTCWC.getAttribute(ATTR_PROGRAMMER_BAUD);
		fOldBitBangDelay = fTCWC.getAttribute(ATTR_BITBANGDELAY);
		fOldExitSpecs = fTCWC.getAttribute(ATTR_PAR_EXITSPEC);
		fOldUSBDelay = fTCWC.getAttribute(ATTR_USB_DELAY);

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
		if (ATTR_HOSTINTERFACE.equals(attribute)) {
			markStale();
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.AbstractFormPart#isDirty()
	 */
	@Override
	public boolean isDirty() {
		if (!fTCWC.getAttribute(ATTR_PROGRAMMER_PORT).equals(fOldPort)) {
			return true;
		}

		if (!fTCWC.getAttribute(ATTR_PROGRAMMER_BAUD).equals(fOldBaudRate)) {
			return true;
		}

		if (!fTCWC.getAttribute(ATTR_BITBANGDELAY).equals(fOldBitBangDelay)) {
			return true;
		}

		if (!fTCWC.getAttribute(ATTR_PAR_EXITSPEC).equals(fOldExitSpecs)) {
			return true;
		}

		if (!fTCWC.getAttribute(ATTR_USB_DELAY).equals(fOldUSBDelay)) {
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
		fOldPort = fTCWC.getAttribute(ATTR_PROGRAMMER_PORT);
		fOldBaudRate = fTCWC.getAttribute(ATTR_PROGRAMMER_BAUD);
		fOldBitBangDelay = fTCWC.getAttribute(ATTR_BITBANGDELAY);
		fOldExitSpecs = fTCWC.getAttribute(ATTR_PAR_EXITSPEC);
		fOldUSBDelay = fTCWC.getAttribute(ATTR_USB_DELAY);

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
		Control[] children = fSectionClient.getChildren();
		for (Control child : children) {
			child.dispose();
		}
		fSectionClient.layout(true, true);
		getManagedForm().reflow(true);

		// Set the title of the section
		String hostinterface = fTCWC.getAttribute(ATTR_HOSTINTERFACE);
		HostInterface currHI = HostInterface.valueOf(hostinterface);
		String title = MessageFormat.format("{0} Settings", currHI.toString());
		getSection().setText(title);

		// And rebuild the section
		FormToolkit toolkit = getManagedForm().getToolkit();

		Composite portCompo = toolkit.createComposite(fSectionClient);
		portCompo
				.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 2));
		portCompo.setLayout(new GridLayout(2, false));

		addPortCombo(portCompo, toolkit, currHI);

		Control section;

		switch (currHI) {
			case SERIAL:
				addBaudRateCombo(portCompo, toolkit);
				break;

			case SERIAL_BB:
				section = addBitBangDelaySection(fSectionClient, toolkit);
				section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP,
						1, 2));
				break;

			case PARALLEL:
				section = addBitBangDelaySection(fSectionClient, toolkit);
				section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP,
						1, 2));
				section = addExitSpecsSection(fSectionClient, toolkit);
				section.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP,
						1, 2));
				break;

			case USB:
				section = addUSBDelaySection(fSectionClient, toolkit);
				section
						.setLayoutData(new TableWrapData(TableWrapData.FILL, TableWrapData.TOP, 1,
								2));
				break;
		}

		// getSection().redraw();
		getManagedForm().reflow(true);

	}

	/**
	 * @param parent
	 * @param toolkit
	 * @param hi
	 */
	private void addPortCombo(Composite parent, FormToolkit toolkit, HostInterface hi) {

		Label label = toolkit.createLabel(parent, "Portname:");
		GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.widthHint = calcTextWidth(label, "Baudrate:");
		label.setLayoutData(gd);

		final Combo combo = new Combo(parent, SWT.NONE);
		toolkit.adapt(combo, true, true);
		gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.widthHint = 200;
		combo.setLayoutData(gd);
		combo
				.setToolTipText("The host system port the programmer is attached to, e.g. '/dev/ttyS0' or 'com1'.\n"
						+ "Leave empty to use the default port (may not work for usb devices)");

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String port = combo.getText();
				fTCWC.setAttribute(ATTR_PROGRAMMER_PORT, port);
				getManagedForm().dirtyStateChanged();
			}
		});

		// Set the current value for the port
		String port = fTCWC.getAttribute(ATTR_PROGRAMMER_PORT);
		combo.setText(port);
		switch (hi) {
			case SERIAL:
			case SERIAL_BB:
				if (port.contains("com") // Windows
						|| port.contains("cua") // FreeBSD
						|| port.contains("tty") // Linux & MacOSX
						|| port.contains("term") // Solaris)
				) {
					// leave as is
					break;
				}
				// the previous setting does not represent a serial port
				combo.setText("");
				fTCWC.setAttribute(ATTR_PROGRAMMER_PORT, "");
				break;

			case PARALLEL:
				if (port.contains("lpt") // Windows
						|| port.contains("ppi") // FreeBSD
						|| port.contains("parport") // Linux & MacOSX
						|| port.contains("printers") // Solaris)
				) {
					// leave as is
					break;
				}
				// the previous setting does not represent a parallel port
				combo.setText("");
				fTCWC.setAttribute(ATTR_PROGRAMMER_PORT, "");
				break;

			case USB:
				if (port.contains("usb")) {
					// leave as is
					break;
				}

				// the previous setting does not represent a usb port
				combo.setText("usb");
				fTCWC.setAttribute(ATTR_PROGRAMMER_PORT, "usb");
				break;

			default:
				// Unsupported HostInterface -- ignore
				combo.setText("unknown");
		}
	}

	/**
	 * @param parent
	 * @param toolkit
	 */
	private void addBaudRateCombo(Composite parent, FormToolkit toolkit) {
		Label label = toolkit.createLabel(parent, "Baudrate:");
		GridData gd = new GridData(SWT.BEGINNING, SWT.CENTER, false, false);
		gd.widthHint = calcTextWidth(label, "Baudrate:");
		label.setLayoutData(gd);

		final Combo combo = new Combo(parent, SWT.NONE);
		toolkit.adapt(combo, true, true);
		gd = new GridData(SWT.BEGINNING, SWT.NONE, false, false);
		combo.setLayoutData(gd);
		combo
				.setToolTipText("Override the RS-232 connection baud rate specified in the respective programmer's entry of the configuration file.\nLeave empty to use the default");
		combo.setItems(BAUDRATES);
		combo.setVisibleItemCount(BAUDRATES.length);

		combo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String port = combo.getText();
				fTCWC.setAttribute(ATTR_PROGRAMMER_BAUD, port);
				getManagedForm().dirtyStateChanged();
			}
		});

		// The verify listener to restrict the input to integers
		combo.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				String text = event.text;
				if (!text.matches("[0-9]*")) {
					event.doit = false;
				}
			}
		});

		// Set the current value for the port
		String port = fTCWC.getAttribute(ATTR_PROGRAMMER_BAUD);
		combo.setText(port);

	}

	/**
	 * @param parent
	 * @param toolkit
	 */
	private Section addBitBangDelaySection(Composite parent, FormToolkit toolkit) {

		Section section = toolkit.createSection(parent, Section.TWISTIE | Section.CLIENT_INDENT);

		section.setText("Bitbang delay");

		String desc = "For bitbang-type programmers, delay for the set number of microseconds between each bit state change. \n"
				+ "If the host system is very fast, or the target runs off a slow clock "
				+ "(like a 32 kHz crystal, or the 128 kHz internal RC oscillator), this "
				+ "can become necessary to satisfy the requirement that the ISP clock "
				+ "frequency must not be higher than 1/4 of the CPU clock frequency.";

		String delay = fTCWC.getAttribute(ATTR_BITBANGDELAY);
		if (delay.length() == 0) {
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
		content.setLayout(new GridLayout(3, false));

		toolkit.createLabel(content, "delay:");

		final Text text = toolkit.createText(content, delay, SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.widthHint = calcTextWidth(text, "8888888888");
		text.setLayoutData(gd);

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String port = text.getText();
				fTCWC.setAttribute(ATTR_BITBANGDELAY, port);
				getManagedForm().dirtyStateChanged();
			}
		});

		// The verify listener to restrict the input to integers
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				String text = event.text;
				if (!text.matches("[0-9]*")) {
					event.doit = false;
				}
			}
		});

		toolkit.createLabel(content, "µs");

		section.setClient(sectionClient);

		return section;
	}

	/**
	 * @param parent
	 * @param toolkit
	 */
	private Section addExitSpecsSection(Composite parent, FormToolkit toolkit) {

		Section section = toolkit.createSection(parent, Section.TWISTIE | Section.CLIENT_INDENT);

		section.setText("Parallel Port Exit Specs");
		String desc = "By default, AVRDUDE leaves the parallel port "
				+ "in the same state on exit as it has been found at startup. "
				+ "These options modify the state of the `/RESET' and `Vcc' lines "
				+ "the parallel port is left at.\nSee the avrdude manual for more details.";

		String exitspecs = fTCWC.getAttribute(ATTR_PAR_EXITSPEC);
		if (exitspecs.length() == 0) {
			// If there has been no value then collapse this section
			section.setExpanded(false);
		} else {
			section.setExpanded(true);
		}

		// Parse the exitspec string and set the
		// fExitSpecReset and fExitSpecVcc globals.
		// Not very elegant but robust.
		if (exitspecs.contains("noreset")) {
			fExitSpecReset = "noreset";
		} else if (exitspecs.contains("reset")) {
			fExitSpecReset = "reset";
		} else {
			fExitSpecReset = "";
		}

		if (exitspecs.contains("novcc")) {
			fExitSpecVcc = "novcc";
		} else if (exitspecs.contains("vcc")) {
			fExitSpecVcc = "vcc";
		} else {
			fExitSpecVcc = "";
		}

		Composite sectionClient = toolkit.createComposite(section);
		sectionClient.setLayout(new TableWrapLayout());

		Label description = toolkit.createLabel(sectionClient, desc, SWT.WRAP);
		description.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

		Composite content = toolkit.createComposite(sectionClient);
		content.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
		content.setLayout(new GridLayout(2, true));

		Group resetGroup = new Group(content, SWT.NONE);
		resetGroup.setText("/Reset Line");
		resetGroup.setLayout(new RowLayout(SWT.VERTICAL));

		SelectionListener resetlistener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = (String) e.widget.getData();
				fExitSpecReset = value;
				updateExitSpec();
			}
		};

		Button button = toolkit.createButton(resetGroup, "Restore to previous state", SWT.RADIO);
		button.setData("");
		button.addSelectionListener(resetlistener);
		if ("".equals(fExitSpecReset)) {
			button.setSelection(true);
		}

		button = toolkit.createButton(resetGroup, "Activate (low) on exit", SWT.RADIO);
		button.setData("reset");
		button.addSelectionListener(resetlistener);
		if ("reset".equals(fExitSpecReset)) {
			button.setSelection(true);
		}

		button = toolkit.createButton(resetGroup, "Deactivate (high) on exit", SWT.RADIO);
		button.setData("noreset");
		button.addSelectionListener(resetlistener);
		if ("noreset".equals(fExitSpecReset)) {
			button.setSelection(true);
		}

		Group vccGroup = new Group(content, SWT.NONE);
		vccGroup.setText("Vcc Lines");
		vccGroup.setLayout(new RowLayout(SWT.VERTICAL));

		SelectionListener vcclistener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String value = (String) e.widget.getData();
				fExitSpecVcc = value;
				updateExitSpec();
			}
		};

		button = toolkit.createButton(vccGroup, "Restore to previous state", SWT.RADIO);
		button.setData("");
		button.addSelectionListener(vcclistener);
		if ("".equals(fExitSpecVcc)) {
			button.setSelection(true);
		}

		button = toolkit.createButton(vccGroup, "Activate (high) on exit", SWT.RADIO);
		button.setData("vcc");
		button.addSelectionListener(vcclistener);
		if ("vcc".equals(fExitSpecVcc)) {
			button.setSelection(true);
		}

		button = toolkit.createButton(vccGroup, "Deactivate (low) on exit", SWT.RADIO);
		button.setData("novcc");
		button.addSelectionListener(vcclistener);
		if ("novcc".equals(fExitSpecVcc)) {
			button.setSelection(true);
		}

		section.setClient(sectionClient);

		return section;
	}

	private void updateExitSpec() {
		StringBuilder sb = new StringBuilder(16);
		if (fExitSpecReset.length() != 0) {
			sb.append(fExitSpecReset);
			if (fExitSpecVcc.length() != 0) {
				sb.append(",");
			}
		}
		if (fExitSpecVcc.length() != 0) {
			sb.append(fExitSpecVcc);
		}

		fTCWC.setAttribute(ATTR_PAR_EXITSPEC, sb.toString());
		getManagedForm().dirtyStateChanged();
	}

	/**
	 * @param parent
	 * @param toolkit
	 */
	private Section addUSBDelaySection(Composite parent, FormToolkit toolkit) {

		Section section = toolkit.createSection(parent, Section.TWISTIE | Section.CLIENT_INDENT);

		section.setText("USB access delay");
		String desc = "Some USB devices need a certain time to release the USB bus. "
				+ "As the AVR plugin sometimes accesses these devices in short succession "
				+ "a delay can be specified between two accesses. Set the delay when you "
				+ "get error messages that the selected usb port can't be opened.";

		String delay = fTCWC.getAttribute(ATTR_USB_DELAY);
		if (delay.length() == 0) {
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
		content.setLayout(new GridLayout(4, false));

		toolkit.createLabel(content, "delay:");

		final Text text = toolkit.createText(content, delay, SWT.RIGHT);
		GridData gd = new GridData(SWT.FILL, SWT.NONE, false, false);
		gd.widthHint = calcTextWidth(text, "8888888888");
		text.setLayoutData(gd);

		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String port = text.getText();
				fTCWC.setAttribute(ATTR_USB_DELAY, port);
				getManagedForm().dirtyStateChanged();
			}
		});

		// The verify listener to restrict the input to integers
		text.addVerifyListener(new VerifyListener() {
			public void verifyText(VerifyEvent event) {
				String text = event.text;
				if (!text.matches("[0-9]*")) {
					event.doit = false;
				}
			}
		});

		toolkit.createLabel(content, "ms");

		Hyperlink testlink = toolkit.createHyperlink(content, "Test delay", SWT.NONE);
		testlink.setUnderlined(true);
		testlink.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, true, false));

		section.setClient(sectionClient);

		return section;
	}

	private int calcTextWidth(Control control, String text) {
		GC gc = new GC(control);
		gc.setFont(control.getFont());
		int value = gc.stringExtent("8888888888").x;
		gc.dispose();

		return value;
	}
}
