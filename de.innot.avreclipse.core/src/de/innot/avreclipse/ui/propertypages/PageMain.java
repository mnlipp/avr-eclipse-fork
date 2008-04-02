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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.BackingStoreException;

/**
 * This is the Main AVR Property Page.
 * <p>
 * Currently only one item handled by this page: the "per config" flag.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class PageMain extends AbstractAVRPage {

	private static final String TEXT_PERCONFIG = "Enable individual settings for Build Configurations";

	private Button fPerConfigButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPage#contentForCDT(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void contentForCDT(Composite composite) {

		// We don't call the superclass, because this page does not use the
		// configuration selection group.

		fPerConfigButton = new Button(composite, SWT.CHECK);
		fPerConfigButton.setText(TEXT_PERCONFIG);
		fPerConfigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				boolean newvalue = fPerConfigButton.getSelection();
				PageMain.super.setPerConfig(newvalue);
			}
		});

		fPerConfigButton.setSelection(super.isPerConfig());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performApply()
	 */
	@Override
	public void performApply() {

		// Save the current state of the "per Config flag", and only the flag.
		try {
			fPropertiesManager.savePerConfigFlag();
		} catch (BackingStoreException e) {
			// TODO Pop an error dialog
			e.printStackTrace();
		}

		// Let the superclass do any additional things.
		super.performApply();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.propertypages.AbstractAVRPage#contributeButtons(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void contributeButtons(Composite parent) {
		// Over-Override this method, because this page does not need the "Copy
		// from Project" Button
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#isSingle()
	 */
	@Override
	protected boolean isSingle() {
		// This page does not use any tabs
		return true;
	}

}
