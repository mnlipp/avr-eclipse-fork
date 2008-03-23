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

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.cdt.managedbuilder.envvar.IConfigurationEnvironmentVariableSupplier;
import org.eclipse.cdt.managedbuilder.envvar.IEnvironmentVariableProvider;
import org.eclipse.core.resources.IProject;

import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathProvider;
import de.innot.avreclipse.core.paths.IPathProvider;
import de.innot.avreclipse.core.preferences.AVRProjectProperties;
import de.innot.avreclipse.core.preferences.ProjectPropertyManager;

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
public class AVRTargetEnvvarSupplier implements
		IConfigurationEnvironmentVariableSupplier, BuildConstants {

	static final String BUILDARTIFACT_NAME = "BUILDARTIFACT";

	private ProjectPropertyManager fProjProps = null;

	/**
	 * This is a trivial implementation of the
	 * <code>IBuildEnvironmentVariable</code> interface used internally by the
	 * AVRTargetEnvvarSupplier.
	 */

	private class SimpleBuildEnvVar implements IBuildEnvironmentVariable {

		private final String fName;
		private final int fOperation;
		private final IConfiguration fConfiguration;
		private final AVRProjectProperties fProps;

		public SimpleBuildEnvVar(String name, IConfiguration config) {
			this(name, config, ENVVAR_REPLACE);
		}

		public SimpleBuildEnvVar(String name, IConfiguration config,
				int operation) {
			fName = name;
			fConfiguration = config;
			fOperation = operation;
			fProps = fProjProps.getConfigurationProperties(config);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getDelimiter()
		 */
		public String getDelimiter() {
			// return Delimiter according to the Platform
			return System.getProperty("path.separator");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getName()
		 */
		public String getName() {
			return fName;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getOperation()
		 */
		public int getOperation() {
			return fOperation;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable#getValue()
		 */
		public String getValue() {
			if (TARGET_MCU_NAME.equals(fName)) {
				// Target MCU
				String targetmcu = fProps.getMCUId();
				return targetmcu;

			} else if (TARGET_FCPU_NAME.equals(fName)) {
				// Target F_CPU
				String fcpu = fProps.getFCPU();
				return fcpu;

			} else if (BUILDARTIFACT_NAME.equals(fName)) {
				String artifact = fConfiguration.getArtifactName() + "."
						+ fConfiguration.getArtifactExtension();
				return artifact;

			} else if ("PATH".equals(fName)) {
				// Prepend the path to the executables to the PATH variable
				IPathProvider gccpathprovider = new AVRPathProvider(AVRPath.AVRGCC);
				String gccpath = gccpathprovider.getPath().toOSString();
				IPathProvider makepathprovider = new AVRPathProvider(AVRPath.MAKE); 
				String makepath = makepathprovider.getPath().toOSString();
				
				if (makepath != null && !("".equals(makepath))) {
					gccpath += System.getProperty("path.separator");
					gccpath += makepath;
				}
				if (gccpath != null && !("".equals(gccpath))) {
					return gccpath;
				}
			}
			return null;
		}
	}

	/**
	 * Get the Build Environment Variable with the given name.
	 * <p>
	 * If the passed variable name matches any of the variables handled by this
	 * class, it will return an <code>IBuildEnvironmentVariable</code> object
	 * which dynamically handles the value.
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
	public IBuildEnvironmentVariable getVariable(String variableName,
			IConfiguration configuration, IEnvironmentVariableProvider provider) {

		if (variableName == null)
			return null;

		if (fProjProps == null) {
			fProjProps = ProjectPropertyManager.getPropertyManager((IProject)configuration.getOwner());
		}

		if (TARGET_MCU_NAME.equals(variableName)) {
			// Target MCU
			return new SimpleBuildEnvVar(TARGET_MCU_NAME, configuration);

		} else if (TARGET_FCPU_NAME.equals(variableName)) {
			// Target F_CPU
			return new SimpleBuildEnvVar(TARGET_FCPU_NAME, configuration);

		} else if (BUILDARTIFACT_NAME.equals(variableName)) {
			return new SimpleBuildEnvVar(BUILDARTIFACT_NAME, configuration);

		} else if ("PATH".equals(variableName)) {
			return new SimpleBuildEnvVar("PATH", configuration,
					IBuildEnvironmentVariable.ENVVAR_PREPEND);
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
	public IBuildEnvironmentVariable[] getVariables(
			IConfiguration configuration, IEnvironmentVariableProvider provider) {
		// Get the supported envvars from the getVariable() method
		IBuildEnvironmentVariable[] envvars = new SimpleBuildEnvVar[4];
		envvars[0] = getVariable(TARGET_MCU_NAME, configuration, provider);
		envvars[1] = getVariable(TARGET_FCPU_NAME, configuration, provider);
		envvars[2] = getVariable(BUILDARTIFACT_NAME, configuration, provider);
		envvars[3] = getVariable("PATH", configuration, provider);
		return envvars;
	}
}
