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
package de.innot.avreclipse.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;

import de.innot.avreclipse.ui.actions.ActionType;
import de.innot.avreclipse.ui.dialogs.ChangeMCUDialog;

/**
 * A <code>IFormPart</code> that adds an action to the form toolbar to change the MCU type.
 * 
 * @see AbstractActionPart
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class MCUTypeActionPart extends AbstractActionPart {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.ui.editors.AbstractActionPart#getAction()
	 */
	@Override
	protected IAction[] getAction() {

		ActionType type = ActionType.CHANGE_MCU;

		Action changeAction = new Action() {

			@Override
			public void run() {

				// Open the "Change MCU" dialog
				ChangeMCUDialog changeMCUDialog = new ChangeMCUDialog(getManagedForm().getForm()
						.getShell(), getByteValues(), null);

				if (changeMCUDialog.open() == ChangeMCUDialog.OK) {

					String newmcuid = changeMCUDialog.getResult();

					// Commit all pending changes first, so that they can be converted correctly.
					getManagedForm().commit(false);

					// Now we can change the MCU to the new type, converting the old bitfield values
					// as far as possible.
					getByteValues().setMCUId(newmcuid, true);

					// And finally tell all form parts that the ByteValues have changed.
					notifyForm();

					// Mark the ByteValues dirty due to the changed MCU.
					markDirty();
				}
			}
		};

		type.setupAction(changeAction);

		IAction[] allactions = new IAction[1];
		allactions[0] = changeAction;

		return allactions;

	}
}
