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

import org.eclipse.cdt.core.cdtvariables.CdtVariableException;
import org.eclipse.cdt.core.cdtvariables.ICdtVariable;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroStatus;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;

import de.innot.avreclipse.core.preferences.AVRTargetProperties;

/**
 * BuildMacro Supplier.
 * 
 * This class implements the {@link IConfigurationBuildMacroSupplier} interface
 * and can be used for the <code>configurationMacroSupplier</code> attribute
 * of a <code>toolChain</code> element.
 * 
 * Currently two build macros are handled by this class.
 * <ul>
 * <li>${AVRTARGETMCU} (see {@link BuildConstants#TARGET_MCU_NAME})</li>
 * <li>${AVRTARGETFCPU} (see {@link BuildConstants#TARGET_FCPU_NAME})</li>
 * </ul>
 * They have the value of the corresponding project properties.
 * 
 * @author Thomas Holland
 */
public class AVRTargetBuildMacroSupplier implements
		IConfigurationBuildMacroSupplier, BuildConstants {

	private IPreferenceStore fProps = null;

	/**
	 * This is a simple implementation of the <code>IBuildMacro</code>
	 * interface used internally by the AVRTargetBuildMacroSupplier. Only simple
	 * text macros are needed and supported. The macro takes its value directly
	 * from the project properties, to always represent the current value.
	 */
	private class InternalBuildMacro implements IBuildMacro {

		// name of the macro
		private String fName;
		
		/**
		 * Construct a new InternalBuildMacro with the given name.
		 * 
		 * @param name
		 *            Name of this BuildMacro
		 */
		public InternalBuildMacro(String name, IConfiguration config) {
			fName = name;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.cdtvariables.ICdtVariable#getName()
		 */
		public String getName() {
			return fName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.core.cdtvariables.ICdtVariable#getValueType()
		 */
		public int getValueType() {
			return ICdtVariable.VALUE_TEXT; // we only need simple text macros
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getMacroValueType()
		 */
		public int getMacroValueType() {
			return ICdtVariable.VALUE_TEXT; // we only need simple text macros
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringListValue()
		 */
		public String[] getStringListValue() throws BuildMacroException {
			throw new BuildMacroException(new CdtVariableException(
					IBuildMacroStatus.TYPE_MACRO_NOT_STRINGLIST, fName, null,
					null));
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.managedbuilder.macros.IBuildMacro#getStringValue()
		 */
		public String getStringValue() throws BuildMacroException {
			// If any of the supported macro names match return the
			// associated project property value.
			if (TARGET_MCU_NAME.equals(fName)) {
				// Target MCU
				String targetmcu = fProps
						.getString(AVRTargetProperties.KEY_MCUTYPE);
				return targetmcu;
			} else if (TARGET_FCPU_NAME.equals(fName)) {
				// Target F_CPU
				String fcpu = fProps.getString(AVRTargetProperties.KEY_FCPU);
				return fcpu;
			}

			// Should not happen because we construct this class only with valid
			// macro names (95 is just a unique random number)
			throw new BuildMacroException(new Status(IStatus.ERROR,
					"de.innot.avreclipse.core", 95, "internal error", null));
		}
	}

	/**
	 * Get the Macro with the given name.
	 * <p>
	 * If the passed macro name matches any of the macros handled by this class,
	 * it will return an <code>IBuildMacro</code> object which dynamically
	 * handles the Macro value
	 * </p>
	 * 
	 * @param macroName
	 *            Name of the macro the build system wants a
	 *            <code>IBuildMacro</code> for.
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

		if (fProps == null) {
			// set up property store
			fProps = AVRTargetProperties.getPropertyStore(configuration);
		}

		if (TARGET_MCU_NAME.equals(macroName)) {
			// Target MCU
			return new InternalBuildMacro(TARGET_MCU_NAME, configuration);
		} else if (TARGET_FCPU_NAME.equals(macroName)) {
			// Target F_CPU
			return new InternalBuildMacro(TARGET_FCPU_NAME, configuration);
		}

		// Unknown Macro Name
		return null;
	}

	/**
	 * Returns an array of Macros supported by this supplier.
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
	public IBuildMacro[] getMacros(IConfiguration configuration,
			IBuildMacroProvider provider) {
		// Get the supported macros from the getMacro() method
		IBuildMacro[] macros = new InternalBuildMacro[2];
		macros[0] = getMacro(TARGET_MCU_NAME, configuration, provider);
		macros[1] = getMacro(TARGET_FCPU_NAME, configuration, provider);
		return macros;
	}

}
