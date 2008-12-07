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
 * $Id: AVRDudeErrorDialogJob.java 428 2008-04-21 13:44:39Z innot $
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui.dialogs;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.progress.UIJob;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.avrdude.ProgrammerConfigManager;

/**
 * Displays an Error Message box for am <code>AVRDudeException</code>.
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class AVRDudeErrorDialogJob extends UIJob {

	private AVRDudeException fException;

	private ProgrammerConfig fProgConfig;

	/**
	 * Create a new Job to display an AVRDudeErrorDialog.
	 * 
	 * @param jobDisplay
	 *            The <code>Display</code> to show the message on.
	 * @param exception
	 *            The Exception for which to display the error dialog.
	 * @param programmerconfigid
	 *            The id of the programmer in use when the Exception was thrown.
	 *            Used for some error messages.
	 */
	public AVRDudeErrorDialogJob(Display jobDisplay,
			AVRDudeException exception, String programmerconfigid) {
		super(jobDisplay, "AVRDude Error");
		fException = exception;
		fProgConfig = ProgrammerConfigManager.getDefault().getConfig(
				programmerconfigid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.progress.UIJob#runInUIThread(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IStatus runInUIThread(IProgressMonitor monitor) {

		AVRDudeErrorDialog.openAVRDudeError(getDisplay().getActiveShell(),
				fException, fProgConfig);
		return Status.OK_STATUS;
	}
}