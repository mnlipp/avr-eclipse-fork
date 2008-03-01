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
package de.innot.avreclipse.core.toolinfo;

/**
 * Listen to the output of a {@link ExternalCommandLauncher} line by line.
 * <p>
 * Implementors can listen to the output of a external program line by line to -
 * for example - update the user interface accordingly.
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
	 * @param line
	 *            The current line from the output of the external program.
	 * @param source
	 *            A <code>StreamSource</code> to indicate whether the line
	 *            came from {@link StreamSource#STDOUT} or from
	 *            {@link StreamSource#STDERR}.
	 */
	public void handleLine(String line, StreamSource source);

}
