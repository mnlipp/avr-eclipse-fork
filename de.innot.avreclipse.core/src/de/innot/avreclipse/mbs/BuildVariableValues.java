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
package de.innot.avreclipse.mbs;

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.envvar.IBuildEnvironmentVariable;
import org.eclipse.core.resources.IProject;

import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathProvider;
import de.innot.avreclipse.core.paths.IPathProvider;
import de.innot.avreclipse.core.properties.AVRProjectProperties;
import de.innot.avreclipse.core.properties.ProjectPropertyManager;

/**
 * This <code>Enum</code> contains a list of all available variable names.
 * <p>
 * Each Variable knows how to extract its current value from an
 * {@link AVRProjectProperties} object, respectively from an
 * {@link IConfiguration}.
 * </p>
 * <p>
 * Currently these Environment Variables are handled:
 * <ul>
 * <li><code>$(AVRTARGETMCU)</code>: The target MCU id value as selected by
 * the user</li>
 * <li><code>$(AVRTARGETFCPU)</code>: The target MCU FCPU value as selected
 * by the user</li>
 * <li><code>$(AVRDUDEOPTIONS)</code>: The command line options for avrdude,
 * except for any action options (<em>-U</em> options)</li>
 * <li><code>$(AVRDUDEACTIONOPTIONS)</code>: The command line options for
 * avrdude to execute all actions requested by the user. (<em>-U</em>
 * options)</li>
 * <li><code>$(BUILDARTIFACT)</code>: name of the target build artifact (the
 * .elf file)</li>
 * <li><code>$(PATH)</code>: The current path prepended with the paths to
 * the avr-gcc executable and the make executable. This, together with the
 * selection of the paths on the preference page, allows for multiple avr-gcc
 * toolchains on one computer</li>
 * </ul>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 */
public enum BuildVariableValues {

	AVRTARGETMCU() {
		@Override
		public String getValue(IConfiguration buildcfg) {
			AVRProjectProperties props = getPropsFromConfig(buildcfg);
			String targetmcu = props.getMCUId();
			return targetmcu;
		}
	},

	AVRTARGETFCPU() {
		@Override
		public String getValue(IConfiguration buildcfg) {
			AVRProjectProperties props = getPropsFromConfig(buildcfg);
			String fcpu = props.getFCPU();
			return fcpu;
		}
	},

	AVRDUDEOPTIONS() {
		@Override
		public String getValue(IConfiguration buildcfg) {
			AVRProjectProperties props = getPropsFromConfig(buildcfg);
			List<String> avrdudeoptions = props.getAVRDudeProperties().getArguments();
			StringBuffer sb = new StringBuffer();
			for (String option : avrdudeoptions) {
				sb.append(option + " ");
			}
			return sb.toString();
		}
	},

	AVRDUDEACTIONOPTIONS() {
		@Override
		public String getValue(IConfiguration buildcfg) {
			AVRProjectProperties props = getPropsFromConfig(buildcfg);
			List<String> avrdudeoptions = props.getAVRDudeProperties().getActionArguments(buildcfg);
			StringBuffer sb = new StringBuffer();
			for (String option : avrdudeoptions) {
				sb.append(option + " ");
			}
			return sb.toString();
		}
	},

	BUILDARTIFACT() {
		// This is only defined to export the BuildArtifact Build Macro as an
		// environment variable in case some makefile requires the path to the
		// .elf target file.
		@Override
		public String getValue(IConfiguration buildcfg) {
			String artifact = buildcfg.getArtifactName() + "." + buildcfg.getArtifactExtension();
			return artifact;
		}

		@Override
		public boolean isMacro() {
			// BUILDARTIFACT is not needed as a build macro, because CDT already
			// has a macro with this name.
			return false;
		}
	},

	PATH() {
		@Override
		public String getValue(IConfiguration buildcfg) {
			// Get the paths to "avr-gcc" and "make" from the PathProvider
			// and return the two paths, separated with a System specific path
			// separator
			IPathProvider gccpathprovider = new AVRPathProvider(AVRPath.AVRGCC);
			String gccpath = gccpathprovider.getPath().toOSString();
			IPathProvider makepathprovider = new AVRPathProvider(AVRPath.MAKE);
			String makepath = makepathprovider.getPath().toOSString();

			if (makepath != null && !("".equals(makepath))) {
				gccpath += PATH_SEPARATOR;
				gccpath += makepath;
			}
			if (gccpath != null && !("".equals(gccpath))) {
				return gccpath;
			}
			return null;
		}

		@Override
		public int getOperation() {
			// Prepend our paths to the System paths
			return IBuildEnvironmentVariable.ENVVAR_PREPEND;
		}

		@Override
		public boolean isMacro() {
			// PATH not supported as a BuildMacro
			return false;
		}

	};

	/** System default Path Separator. On Windows ";", on Posix ":" */
	private final static String PATH_SEPARATOR = System.getProperty("path.separator");

	/**
	 * Get the current variable value for the given Configuration
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> for which to get the variable
	 *            value.
	 * @return <code>String</code> with the current value of the variable.
	 */
	public abstract String getValue(IConfiguration buildcfg);

	/**
	 * @return <code>true</code> if this variable is supported as a build
	 *         macro.
	 */
	public boolean isMacro() {
		// This method is overridden in some Enum values
		return true;
	}

	/**
	 * @return <code>true</code> if this variable is supported as an
	 *         environment variable.
	 */
	public boolean isVariable() {
		// This method could be overridden in some Enum values.
		return true;
	}

	/**
	 * Get the Operation code for environment variables.
	 * <p>
	 * Most Variables will return
	 * {@link IBuildEnvironmentVariable#ENVVAR_REPLACE}. However the
	 * <code>PATH</code> environment variable will return
	 * {@link IBuildEnvironmentVariable#ENVVAR_PREPEND}.
	 * </p>
	 * 
	 * @see IBuildEnvironmentVariable#getOperation()
	 * 
	 * @return <code>int</code> with the operation code.
	 */
	public int getOperation() {
		// Default is REPLACE.
		// The PATH Variable, which requires ENVVAR_PREPEND, will override this
		// method.
		return IBuildEnvironmentVariable.ENVVAR_REPLACE;
	}

	/**
	 * Get the AVR Project properties for the given Configuration.
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> for which to get the properties.
	 * @return
	 */
	private static AVRProjectProperties getPropsFromConfig(IConfiguration buildcfg) {
		ProjectPropertyManager manager = ProjectPropertyManager
		        .getPropertyManager((IProject) buildcfg.getOwner());
		AVRProjectProperties props = manager.getConfigurationProperties(buildcfg);
		return props;
	}

}
