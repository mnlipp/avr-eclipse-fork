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
package de.innot.avreclipse.core.toolinfo;

import org.eclipse.core.runtime.IProgressMonitor;

import de.innot.avreclipse.core.avrdude.AVRDudeException.Reason;

/**
 * Listen to the output of a {@link ExternalCommandLauncher} line by line.
 * <p>
 * Implementors can listen to the output of a external program line by line to - for example -
 * update the user interface accordingly.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public interface ICommandOutputListener {

	public enum StreamSource {
		STDOUT, STDERR;
	}

	/**
	 * Sets the progress monitor for the listener. The listener can use the monitor to abort the
	 * current launch when it detects errors.
	 * 
	 * @param monitor
	 */
	public void init(IProgressMonitor monitor);

	/**
	 * @param line
	 *            The current line from the output of the external program.
	 * @param source
	 *            A <code>StreamSource</code> to indicate whether the line came from
	 *            {@link StreamSource#STDOUT} or from {@link StreamSource#STDERR}.
	 */
	public void handleLine(String line, StreamSource source);

	/**
	 * Gets the last abort reason.
	 * 
	 * @return The last abort reason or <code>null</code> if no errors since init.
	 */
	public Reason getAbortReason();

	/**
	 * Returns the line from the output that caused the abort.
	 * 
	 * @return
	 */
	public String getAbortLine();

}
