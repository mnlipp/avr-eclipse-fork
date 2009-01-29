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

package de.innot.avreclipse.ui.editors.targets;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPersistableElement;

import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetConfigurationManager;

/**
 * IEditorInput implementation for Target Configurations.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TCEditorInput implements IEditorInput {

	private TargetConfigurationManager		fTCManager;

	private String							fTargetConfigID;

	private ITargetConfigurationWorkingCopy	fTargetConfigWC;

	public TCEditorInput(String targetConfigID) {
		fTCManager = TargetConfigurationManager.getDefault();
		fTargetConfigID = targetConfigID;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#exists()
	 */
	public boolean exists() {
		return fTCManager.exists(fTargetConfigID);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getImageDescriptor()
	 */
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.getMissingImageDescriptor();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getName()
	 */
	public String getName() {
		return fTCManager.getConfig(fTargetConfigID).getName();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getPersistable()
	 */
	public IPersistableElement getPersistable() {
		// TODO: Is persistance required?
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.IEditorInput#getToolTipText()
	 */
	public String getToolTipText() {
		String text = "Target Configuration: \"" + fTCManager.getConfig(fTargetConfigID).getName()
				+ "\"";
		return text;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public Object getAdapter(Class adapter) {
		// This might be a misuse of the adapter philosophy, but it saves some casts for the
		// users of this editor input.
		// If asked to adapt to a String, then the target configuration id is returned.
		if (String.class.equals(adapter)) {
			return fTargetConfigID;
		}

		// Adapt to a target configuration working copy
		if (ITargetConfigurationWorkingCopy.class.equals(adapter)) {
			if (fTargetConfigWC == null) {
				fTargetConfigWC = fTCManager.getWorkingCopy(fTargetConfigID);
			}
			return fTargetConfigWC;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		// Two Target Configuration Editor Inputs are equal if
		// they have the same Target Configuration ID.
		if (this == obj) {
			return true;
		}
		if (obj instanceof TCEditorInput) {
			TCEditorInput other = (TCEditorInput) obj;
			if (fTargetConfigID.equals(other.fTargetConfigID)) {
				return true;
			}
		}
		return false;
	}

}
