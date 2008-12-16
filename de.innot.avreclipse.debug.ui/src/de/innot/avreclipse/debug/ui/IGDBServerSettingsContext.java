/**
 * 
 */
package de.innot.avreclipse.debug.ui;

import org.eclipse.swt.widgets.Control;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface IGDBServerSettingsContext {

	public void setErrorMessage(String gdbserverid, String message);

	public void updateDialog();

	public Control getControl();

}
