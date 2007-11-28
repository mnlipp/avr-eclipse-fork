/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 * $Id: TargetHardwareOptionsHandler.java 21 2007-11-28 00:52:07Z thomas $
 *     
 *******************************************************************************/

package de.innot.avreclipse.mbs;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler;

/**
 * Handle changes of target hardware options.
 * 
 * <p>
 * This class is registered as a <code>valueHandler</code> by the options of
 * the base toolchain in the AVR Eclipse plugin. All changes to the option
 * implementing this handler (currently "Target MCU" and "CPU Clock Frequency")
 * will cause a call to the <code>handleValue</code> method of this class.
 * </p>
 * <p>
 * All options of all tools of the toolchain are examined and if the last part
 * of their id is equal to <code>valueHandlerExtraArgument</code>, then their
 * value is set to the value of this option.
 * </p>
 * <p>
 * The value of the <code>valueHandlerExtraArgument</code> attribute in the
 * option element is used as the name of the buildMacro / Configuration
 * environment variable to be set, while the value field of the option is used
 * as the value.
 * </p>
 * <p>
 * The {@link BuildConstants#TARGET_MCU_NAME} name is handled specially by
 * extracting the MCU Type from the id in the value field.
 * </p>
 * 
 * <p>
 * This class is extended from
 * {@link org.eclipse.cdt.managedbuilder.core.ManagedOptionValueHandler}, which
 * covers the other methods of the
 * {@link org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler}
 * interface.
 * </p>
 * 
 * @author Thomas Holland
 * @version 1.0
 * 
 * @see de.innot.avreclipse.mbs.AVRTargetBuildMacroSupplier
 * @see de.innot.avreclipse.mbs.AVRTargetEnvvarSupplier
 * 
 */
public class TargetHardwareOptionsHandler extends ManagedOptionValueHandler implements
        BuildConstants {

	/**
	 * Handle Option Change events.
	 * 
	 * <p>
	 * The different kinds of event are all ignored and any change of the this
	 * option is immediately passed onto all other options with an id that ends
	 * with <code>valueHandlerExtraArgument</code>
	 * </p>
	 * <p>
	 * Also a environment variable with the name
	 * <code>valueHandlerExtraArgument</code> and the value of this option is
	 * created to pass the Target MCU onto any postbuild steps.
	 * </p>
	 * 
	 * @see org.eclipse.cdt.managedbuilder.core.IManagedOptionValueHandler#handleValue(org.eclipse.cdt.managedbuilder.core.IBuildObject,
	 *      org.eclipse.cdt.managedbuilder.core.IHoldsOptions,
	 *      org.eclipse.cdt.managedbuilder.core.IOption, java.lang.String, int)
	 */
	@Override
	public boolean handleValue(IBuildObject configuration, IHoldsOptions holder, IOption option,
	        String extraArgument, int event) {

		String name = extraArgument;
		String value = null;

		try {
			value = option.getStringValue();

			// Special handling for the MCU Type, as value will be just the id
			// of the selected <enumeratedOptionValue>
			if (TARGET_MCU_NAME.equals(name)) {
				String tmp = option.getStringValue();
				if (tmp != null) {
					// get the actual mcu type (the last part of the id)
					value = tmp.substring(tmp.lastIndexOf('.') + 1);
				}
			}
			
			AVROptionsManager.setOption(name, value);

		} catch (BuildException e) {
			// This indicates an error in the plugin.xml (no / wrong value for
			// the option)
			e.printStackTrace();
			return false;
		}

		IToolChain toolchain = null;
		if (holder instanceof IToolChain) {
			toolchain = (IToolChain) holder;
		} else if (holder instanceof ITool) {
			toolchain = (IToolChain) ((ITool) holder).getParent();
		}

		// change the value of all options of all tools that have the last part
		// of their id the same as the given valueHandlerExtraArgument.

		int i = 0, n = 0;

		ITool tctools[] = toolchain.getTools();

		for (; i < tctools.length; i++) {
			IOption toolopts[] = tctools[i].getOptions();
			for (n = 0; n < toolopts.length; n++) {
				if (name.equalsIgnoreCase(lastpart(toolopts[n].getId()))) {
					try {
						toolopts[n].setValue(value);
					} catch (BuildException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}


		// The return value is currently ignored in the calling class
		// org.eclipse.cdt.managedbuilder.core.ManagedBuildManager#performValueHandlerEvent()
		// So we always return false, as it is not clear what a true return
		// value will do in future CDT versions.
		return false;
	}

	private String lastpart(String str) {
		if (str.indexOf('.') != 0) {
			return str.substring(str.lastIndexOf('.') + 1);
		}
		return str;
	}
}
