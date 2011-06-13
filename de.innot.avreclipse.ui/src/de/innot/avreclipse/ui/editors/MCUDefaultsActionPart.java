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
package de.innot.avreclipse.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import de.innot.avreclipse.ui.actions.ActionType;

/**
 * A <code>IFormPart</code> that adds an action to the form toolbar to set the values to the
 * factory defaults.
 * 
 * @see AbstractActionPart
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class MCUDefaultsActionPart extends AbstractActionPart {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.editors.AbstractActionPart#getAction()
	 */
	@Override
	protected IAction[] getAction() {

		ActionType type = ActionType.DEFAULTS;

		Action defaultAction = new Action() {

			@Override
			public void run() {

				getByteValues().setDefaultValues();
				notifyForm();
				markDirty();
			}
		};

		type.setupAction(defaultAction);

		IAction[] allactions = new IAction[1];
		allactions[0] = defaultAction;

		return allactions;

	}

}
