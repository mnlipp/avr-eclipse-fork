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
 * $Id: AVRTargetEnvvarSupplier.java 21 2007-11-28 00:52:07Z thomas $
 *     
 *******************************************************************************/
package de.innot.avreclipse.mbs;

import org.eclipse.cdt.managedbuilder.core.BuildException;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.jface.preference.IPreferenceStore;

import de.innot.avreclipse.AVRPluginActivator;
import de.innot.avreclipse.PluginIDs;
import de.innot.avreclipse.ui.preferences.PreferenceConstants;

/**
 * Envvar Supplier.
 * <p>
 * This class implements the {@link IConfigurationEnvironmentVariableSupplier}
 * interface and can be used for the
 * <code>configurationEnvironmentSupplier</code> attribute of a
 * <code>toolChain</code> element.
 * </p>
 * <p>
 * Currently four Environment Variables are handled by this class.
 * <ul>
 * <li><code>$(AVRTARGETMCU)</code>: (see
 * {@link BuildConstants#TARGET_MCU_NAME})</li>
 * <li><code>$(AVRTARGETFCPU)</code>: (see
 * {@link BuildConstants#TARGET_FCPU_NAME})</li>
 * <li><code>$(BUILDARTIFACT)</code>: name of the target build artefact (the
 * .elf file)</li>
 * </ul>
 * These Environment Variables have the value of the corresponding options of
 * the current toolchain and can be used for postbuild scripts.
 * </p>
 * <ul>
 * <li><code>$(PATH)</code>: The current path prepended with the paths to
 * the avr-gcc executable and the make executable. This, together with the
 * selection of the paths on the preference page, allows for multiple avr-gcc
 * toolchains on one computer</li>
 * </ul>
 * 
 * @author Thomas Holland
 * @version 1.1
 */
public class AVRTargetEnvvarSupplier implements IConfigurationEnvironmentVariableSupplier,
        BuildConstants {

	static final String BUILDARTIFACT_NAME = "BUILDARTIFACT";

	/**
	 * This is a trivial implementation of the
	 * <code>IBuildEnvironmentVariable</code> interface used internally by the
	 * AVRTargetEnvvarSupplier.
	 */

	private class SimpleBuildEnvVar implements IBuildEnvironmentVariable {

		private final String fName;
		private final String fValue;
		private final int fOperation;

		public SimpleBuildEnvVar(String name, String value) {
			fName = name;
			fValue = value;
			fOperation = ENVVAR_REPLACE;
		}

		public SimpleBuildEnvVar(String name, String value, int operation) {
			fName = name;
			fValue = value;
			fOperation = operation;
		}

		public String getDelimiter() {
			// return Delimiter according to the Platform
			return System.getProperty("path.separator");
		}

		public String getName() {
			return fName;
		}

		public int getOperation() {
			return fOperation;
		}

		public String getValue() {
			return fValue;
		}
	}

	/**
	 * Get the Build Environment Variable with the given name.
	 * <p>
	 * If the passed variable name matches any of the variables handled by this
	 * class, it will return an <code>IBuildEnvironmentVariable</code> object
	 * with the current value of this envvar.
	 * </p>
	 * <p>
	 * The envvar values are taken from the corresponding toolchain options.
	 * </p>
	 * 
	 * @param variableName
	 *            Name of the variable the build system wants a
	 *            <code>IBuidEnvironmentVariable</code> for.
	 * @param configuration
	 *            The current configuration. (e.g. "Debug" or "Release")
	 * @param provider
	 *            An envvar supplier to query already existing variables. Not
	 *            used.
	 * @return An <code>IBuildEnvironmentVariable</code> object representing
	 *         the value of the wanted macro or <code>null</code> if
	 *         <code>variableName</code> did not match any of the implemented
	 *         variable names.
	 */
	public IBuildEnvironmentVariable getVariable(String variableName, IConfiguration configuration,
	        IEnvironmentVariableProvider provider) {

		if (variableName == null)
			return null;

		IToolChain tc = configuration.getToolChain();

		if (tc != null) {
			if (TARGET_MCU_NAME.equals(variableName)) {
				IOption option = tc.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_MCU);
				try {
					String mcuid = option.getStringValue();
					if (mcuid != null) {
						// get the actual mcu type (the last part of the id)
						String targetmcu = mcuid.substring(mcuid.lastIndexOf('.') + 1)
						        .toLowerCase();
						return new SimpleBuildEnvVar(TARGET_MCU_NAME, targetmcu);
					}
				} catch (BuildException e) {
					// indicates an error in the plugin.xml
					e.printStackTrace();
				}
			} else if (TARGET_FCPU_NAME.equals(variableName)) {
				IOption option = tc.getOptionBySuperClassId(PluginIDs.PLUGIN_TOOLCHAIN_OPTION_FCPU);
				try {
					String fcpu = option.getStringValue();
					if (fcpu != null) {
						return new SimpleBuildEnvVar(TARGET_FCPU_NAME, fcpu);
					}
				} catch (BuildException e) {
					// indicates an error in the plugin.xml
					e.printStackTrace();
				}
			} else if (BUILDARTIFACT_NAME.equals(variableName)) {
				String artifact = configuration.getArtifactName() + "."
				        + configuration.getArtifactExtension();
				return new SimpleBuildEnvVar(BUILDARTIFACT_NAME, artifact);
			} else if ("PATH".equals(variableName)) {
				// Prepend the path to the executables to the PATH variable
				IPreferenceStore store = AVRPluginActivator.getDefault().getPreferenceStore();
				String gccpath = store.getString(PreferenceConstants.PREF_AVRGCCPATH);
				String makepath = store.getString(PreferenceConstants.PREF_AVRMAKEPATH);
				if (makepath != null && !("".equals(makepath))) {
					gccpath += System.getProperty("path.separator");
					gccpath += makepath;
				}
				if (gccpath != null && !("".equals(gccpath))) {
					return new SimpleBuildEnvVar("PATH", gccpath,
					        IBuildEnvironmentVariable.ENVVAR_PREPEND);
				}
			}
		}
		return null;
	}

	/**
	 * Returns an array of Environment Variables supported by this supplier.
	 * 
	 * @param configuration
	 *            The current configuration.
	 * @param provider
	 *            An Environment Variable supplier to query already existing
	 *            envvars. Not used.
	 * @return An array of IBuildMacros supported by this supplier.
	 * 
	 * @see #getVariable(String, IConfiguration, IEnvironmentVariableProvider)
	 */
	public IBuildEnvironmentVariable[] getVariables(IConfiguration configuration,
	        IEnvironmentVariableProvider provider) {
		// Get the supported envvars from the getVariable() method
		IBuildEnvironmentVariable[] envvars = new SimpleBuildEnvVar[4];
		envvars[0] = getVariable(TARGET_MCU_NAME, configuration, provider);
		envvars[1] = getVariable(TARGET_FCPU_NAME, configuration, provider);
		envvars[2] = getVariable(BUILDARTIFACT_NAME, configuration, provider);
		envvars[3] = getVariable("PATH", configuration, provider);
		return envvars;
	}
}
