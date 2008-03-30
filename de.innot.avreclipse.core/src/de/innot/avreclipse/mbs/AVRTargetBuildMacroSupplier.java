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
 * $Id$
 *     
 *******************************************************************************/
package de.innot.avreclipse.mbs;

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacro;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.cdt.managedbuilder.macros.IConfigurationBuildMacroSupplier;

/**
 * BuildMacro Supplier.
 * <p>
 * This class implements the {@link IConfigurationBuildMacroSupplier} interface
 * and can be used for the <code>configurationMacroSupplier</code> attribute
 * of a <code>toolChain</code> element.
 * </p>
 * <p>
 * See {@link BuildVariableValues} for a list of variables actually supported.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.0
 */
public class AVRTargetBuildMacroSupplier implements IConfigurationBuildMacroSupplier {

	/** A list of all known macro names this supplier supports */
	private final static List<String> fAllMacroNames = BuildMacro.getMacroNames();

	/**
	 * Get the Macro with the given name.
	 * <p>
	 * If the passed macro name matches any of the macros handled by this
	 * plugin, it will return an <code>IBuildMacro</code> object which handles
	 * the Macro value dynamically.
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

		if (macroName == null)
			return null;

		if (fAllMacroNames.contains(macroName)) {
			return new BuildMacro(macroName, configuration);
		}
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
	public IBuildMacro[] getMacros(IConfiguration configuration, IBuildMacroProvider provider) {

		IBuildMacro[] macros = new BuildMacro[fAllMacroNames.size()];
		for (int i = 0; i < fAllMacroNames.size(); i++) {
			macros[i] = new BuildMacro(fAllMacroNames.get(i), configuration);
		}
		return macros;
	}

}
