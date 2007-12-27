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
 * $Id: AVRTargetBuildMacroSupplier.java 21 2007-11-28 00:52:07Z thomas $
 *     
 *******************************************************************************/
package de.innot.avreclipse.mbs;

import de.innot.avreclipse.PluginIDs;

import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;

/**
 * BuildMacro Supplier.
 * 
 * This class implements the {@link IConfigurationBuildMacroSupplier} interface
 * and can be used for the <code>configurationMacroSupplier</code> attribute
 * of a <code>toolChain</code> element.
 * 
 * Currently two build macros are handled by this class.
 * <ul>
 * <li>${AVR_TARGET_MCU} (see {@link BuildConstants#TARGET_MCU_NAME})</li>
 * <li>${AVR_TARGET_FCPU} (see {@link BuildConstants#TARGET_FCPU_NAME})</li>
 * </ul>
 * They have the value of the corresponding options of the current toolchain.
 * 
 * @author Thomas Holland
 * @version 1.0
 */
public class AVRTargetBuildMacroSupplier implements IConfigurationBuildMacroSupplier, BuildConstants {

	/**
	 * This is a trivial implementation of the <code>IBuildMacro</code> interface used
	 * internally by the AVRTargetBuildMacroSupplier. Only simple text macros are needed
	 * and supported.
	 */
	private class InternalBuildMacro implements IBuildMacro {

		private String fName;
		private String fValue;

		public InternalBuildMacro(String name, String value) {
			fName = name;
			fValue = value;
		}

		public String getName() {
			return fName;
		}

		public int getValueType() {
			return ICdtVariable.VALUE_TEXT; // we only need simple text macros
		}

		public int getMacroValueType() {
			return ICdtVariable.VALUE_TEXT; // we only need simple text macros
		}

		public String[] getStringListValue() {
			return null;
		}

		public String getStringValue() {
			return fValue;
		}
	}

	/**
	 * Get the Macro with the given name.
	 * <p>
	 * If the passed macro name matches any of the macros handled by this class,
	 * it will return an <code>IBuildMacro</code> object with the current
	 * value of this build macro.
	 * </p>
	 * <p>
	 * The macro values are taken from the corresponding toolchain options.
	 * </p>
	 * 
	 * @param macroName
	 *            Name of the macro the build system wants a
	 *            <code>IBuidMacro</code> for.
	 * @param configuration
	 *            The current configuration. (e.g. "Debug" or "Release")
	 * @param provider
	 *            A buildMacro supplier to query already existing build macros.
	 *            Not used.
	 * @return An IBuildMacro object representing the value of the wanted macro
	 *         or <code>null</code> if <code>macroName</code> did not match
	 *         any of the implemented macro names.
	 */
	public IBuildMacro getMacro(String macroName, IConfiguration configuration,
	        IBuildMacroProvider provider) {

		IToolChain tc = configuration.getToolChain();

		if (tc != null) {
			if (TARGET_MCU_NAME.equals(macroName)) {
				IOption option = tc.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_MCU);
				try {
					String mcuid = option.getStringValue();
					if (mcuid != null) {
						// get the actual mcu type (the last part of the id)
						String targetmcu = mcuid.substring(mcuid.lastIndexOf('.') + 1);
						return new InternalBuildMacro(TARGET_MCU_NAME, targetmcu);
					}
				} catch (BuildException e) {
					// indicates an error in the plugin.xml
					e.printStackTrace();
				}
			} else if (TARGET_FCPU_NAME.equals(macroName)) {
				IOption option = tc.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_FCPU);
				try {
					String fcpu = option.getStringValue();
					if (fcpu != null) {
						return new InternalBuildMacro(TARGET_FCPU_NAME, fcpu);
					}
				} catch (BuildException e) {
					// indicates an error in the plugin.xml
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	/**
	 * Returns an array of Macros supported by this supplier.
	 * 
	 * 
	 * @param configuration
	 *            The current configuration.
	 * @param provider
	 *            A buildMacro supplier to query already existing build macros.
	 *            Not used.
	 * @return An array of IBuildMacros supported by this supplier.
	 * 
	 * @see #getMacro(String, IConfiguration, IBuildMacroProvider)
	 */
	public IBuildMacro[] getMacros(IConfiguration configuration, IBuildMacroProvider provider) {
		// Get the supported macros from the getMacro() method
		IBuildMacro[] macros = new InternalBuildMacro[2];
		macros[0] = getMacro(TARGET_MCU_NAME, configuration, provider);
		macros[1] = getMacro(TARGET_FCPU_NAME, configuration, provider);
		return macros;
	}

}
