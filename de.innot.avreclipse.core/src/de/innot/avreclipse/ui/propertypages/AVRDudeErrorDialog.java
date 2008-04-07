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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Shell;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.avrdude.ProgrammerConfig;
import de.innot.avreclipse.core.preferences.AVRDudePreferences;

/**
 * Display an AVRDude Error.
 * <p>
 * This Dialog knows how interpret an {@link AVRDudeException} and will display
 * a human readable message for the reason of the Exception.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public class AVRDudeErrorDialog extends ErrorDialog {

	/**
	 * Instatiate a new AVRDudeErrorDialog.
	 * <p>
	 * The Dialog is not shown until the <code>open()</code> method has been
	 * called.
	 * </p>
	 * 
	 * @param parentShell
	 *            <code>Shell</code> in which the dialog is opened.
	 * @param message
	 *            The message of the Dialog.
	 * @param status
	 *            The Status with the root cause.
	 */
	protected AVRDudeErrorDialog(Shell parentShell, String message,
			IStatus status) {
		super(parentShell, "AVRDude Error", message, status, IStatus.OK
				| IStatus.INFO | IStatus.WARNING | IStatus.ERROR);
	}

	/**
	 * Open an Error Dialog for an AVRDudeException.
	 * <p>
	 * This method will take the Exception reason from the given
	 * {@link AVRDudeException} and display a human readable message.
	 * </p>
	 * <p>
	 * This Dialog is modal and will block until OK is clicked or the dialog is
	 * closed with ESC or the window close button.
	 * </P>
	 * 
	 * @param parent
	 *            the parent shell of the dialog, or <code>null</code> if none
	 * @param exc
	 *            The <code>AVRDudeException</code> that contains the root
	 *            cause.
	 * @param config
	 *            The <code>ProgrammerConfig</code> in use while the Exception
	 *            occured. Used for more detailed error messages and may be
	 *            <code>null</code> if not available.
	 */
	public static void openAVRDudeError(Shell parentShell, Throwable exc,
			ProgrammerConfig config) {
		String message;

		if (exc instanceof AVRDudeException) {
			AVRDudeException avrdudeexc = (AVRDudeException) exc;

			// Get the Programmer id and the port from the given
			// ProgrammerConfig (if not null)
			// These are used below for more detailed error messages
			String programmer = "";
			String port = "";
			if (config != null) {
				programmer = "\"" + config.getProgrammer() + "\"";
				port = config.getPort().equals("") ? "" : "\""
						+ config.getPort() + "\"";
			}

			// Also a custom avrdude configfile might be the cause of errors.
			String customconfig = "";
			IPreferenceStore avrdudestore = AVRDudePreferences
					.getPreferenceStore();
			if (avrdudestore.getBoolean(AVRDudePreferences.KEY_USECUSTOMCONFIG)) {
				customconfig = avrdudestore
						.getString(AVRDudePreferences.KEY_CONFIGFILE);
			}

			// The nice thing about enums: using them in a switch statement!
			switch (avrdudeexc.getReason()) {
			case UNKNOWN:
				message = "An error occured while accessing AVRDude.\n\n"
						+ "See below for details.";
				break;

			case NO_AVRDUDE_FOUND:
				message = "AVRDude executable can not be found.\n\n"
						+ "Check in the AVR Preferences if the path to AVRDude is correct.";
				break;

			case CANT_ACCESS_AVRDUDE:
				message = "AVRDude executable can not be accessed.\n\n"
						+ "Check in the AVR Preferences if the path to AVRDude is correct\n"
						+ "(Window > Preferences... -> AVR -> Paths)";
				break;

			case CONFIG_NOT_FOUND:
				if (customconfig.length() == 0) {
					message = "AVRDude can not find its default configuration file.\n\n"
							+ "Check your avrdude setup.";
				} else {
					message = "AVRDude can not find configuration file ["
							+ customconfig
							+ "].\n\n"
							+ "Check in the AVRDude Preferences if the path to the custom avrdude configuration file is correct\n"
							+ "(Window > Preferences... -> AVR -> AVRDude)";
				}
				break;

			case UNKNOWN_PROGRAMMER:
				message = "AVRDude does not recognize the selected programmer id "
						+ programmer
						+ ".\n\n"
						+ "Check the current Programmer Configuration.";
				break;

			case UNKNOWN_MCU:
				message = "AVRDude does not recognize the selected MCU type.\n\n"
						+ "Check the AVR Target Hardware settings if the selected MCU is supported by AVRDude.";
				break;

			case TIMEOUT:
				message = "Operation timed out while trying to access the avrdude programmer "
						+ programmer
						+ ".\n\n"
						+ "Check that the Programmer is connected and switched on.";
				break;

			case PORT_BLOCKED:
				message = "The port "
						+ port
						+ " for the Programmer "
						+ programmer
						+ " is blocked.\n\n"
						+ "Check that no other instances of AVRDude or any other programm is using the port";
				break;

			default:
				message = "An unhandled Error occured while accessing AVRDude.\n\n"
						+ "See below for details and report this error the the Plugin author";
			}

		} else {
			// The throwable is not an instance of AVRDudeException
			// Why does the caller think this class is called
			// AVRDudeErrorDialog?
			// Nevertheless we just display the message from the Throwable
			message = exc.getLocalizedMessage();
		}

		// Set the status for the dialog
		IStatus status = new Status(IStatus.ERROR, AVRPlugin.PLUGIN_ID, exc
				.getLocalizedMessage(), exc.getCause());

		// Now open the Dialog.
		// while dialog.open() will return something, we don't care if the user
		// has pressed OK or ESC or the window close button.
		ErrorDialog dialog = new AVRDudeErrorDialog(parentShell, message,
				status);
		dialog.open();
		return;
	}
}
