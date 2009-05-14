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

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class SectionAvarice extends AbstractTCSectionPart {

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTCSectionPart#createSectionContent(org.eclipse
	 * .swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createSectionContent(Composite parent, FormToolkit toolkit) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTCSectionPart#getPartAttributes()
	 */
	@Override
	protected String[] getPartAttributes() {
		// TODO Auto-generated method stub
		return new String[] {};
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTCSectionPart#getTitle()
	 */
	@Override
	protected String getTitle() {
		return "AVaRICE";
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTCSectionPart#refreshSectionContent()
	 */
	@Override
	protected void refreshSectionContent() {
		// TODO Auto-generated method stub

	}

}
