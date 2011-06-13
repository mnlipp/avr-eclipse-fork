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
package de.innot.avreclipse.ui.wizards;

import de.innot.avreclipse.core.toolinfo.fuses.FuseType;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the provided container.
 * If the container resource (a folder or a project) is selected in the workspace when the wizard is
 * opened, it will accept it as the target container. The wizard creates one file with the extension
 * "fuses". If a sample multi-fWizardPage editor (also available as a template) is registered for
 * the same extension, it will be able to open it.
 */

public class NewLockbitsWizard extends NewFusesWizard {
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.wizards.NewFusesWizard#getType()
	 */
	@Override
	protected FuseType getType() {
		return FuseType.LOCKBITS;
	}
}