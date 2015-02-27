/**
 * 
 */
package de.innot.avreclipse.debug.ui;

import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;

/**
 * @author mnl
 *
 */
public interface IGdbJtagMapper {

	void updateGdbJagAttributes(ILaunchConfigurationWorkingCopy configuration);
}
