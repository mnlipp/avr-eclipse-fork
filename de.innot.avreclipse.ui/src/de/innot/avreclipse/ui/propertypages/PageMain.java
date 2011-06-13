/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.ui.propertypages;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.AVRPlugin;

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

	private static final String	TEXT_PERCONFIG	= "Enable individual settings for Build Configurations";

	private Button				fPerConfigButton;

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
			fPropertiesManager.save();
		} catch (BackingStoreException e) {
			IStatus status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID,
					"Could not write \"per config\" flag to the preferences.", e);

			ErrorDialog.openError(this.getShell(), "AVR Main Properties Error", null, status);
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
