/**
 * 
 */
package de.innot.avreclipse.mbs.converter;

import java.lang.reflect.Method;
import java.util.Vector;

import org.eclipse.cdt.managedbuilder.core.IBuildObject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IHoldsOptions;
import org.eclipse.cdt.managedbuilder.core.IManagedProject;
import org.eclipse.cdt.managedbuilder.core.IOption;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;

import de.innot.avreclipse.core.natures.AVRProjectNature;
import de.innot.avreclipse.core.preferences.AVRTargetProperties;

/**
 * @author Thomas
 * 
 */
public class Convert21 {

	private final static String OLD_AVRTARGET_ID = "avrtarget";

	private static IPreferenceStore fProps = null;

	public static IBuildObject convert(IBuildObject buildObj, String fromId) {

		IManagedProject mproj = (IManagedProject) buildObj;

		// get the project property store
		fProps = AVRTargetProperties.getPropertyStore((IProject)mproj.getOwner());

		// go through all configurations of the selected Project and
		// check the options needing an update
		IConfiguration[] cfgs = mproj.getConfigurations();
		if ((cfgs != null) && (cfgs.length > 0)) { // Sanity Check
			for (int i = 0; i < cfgs.length; i++) {
				IConfiguration currcfg = cfgs[i];

				// remove deprecated toolchain options
				IToolChain tc = currcfg.getToolChain();
				checkOptions(tc);

				// Check all tools for deprecated options
				ITool[] tools = currcfg.getTools();
				for (int n = 0; n < tools.length; n++) {
					checkOptions(tools[n]);
				}

			} // for configurations

			// Save the (modified) Buildinfo
			IProject project = (IProject) mproj.getOwner();
			ManagedBuildManager.saveBuildInfo(project, true);
		}

		// Add AVR Nature to the project
		IProject project = (IProject) mproj.getOwner();
		try {
			AVRProjectNature.addAVRNature(project);
		} catch (CoreException ce) {
			// TODO: log Exception
			ce.printStackTrace();
		}
		return buildObj;
	}

	/**
	 * @param tools
	 */
	@SuppressWarnings("unchecked")
	private static void checkOptions(IHoldsOptions optionholder) {

		// we need to use reflections to call the private method
		// "getOptionsList" because getOptions filters all invalid
		// options, which are just the ones we need for removal
		Vector optionlist = new Vector(0);
		Class<?> c = optionholder.getClass().getSuperclass();
		try {
			Method getoptionlist = c.getDeclaredMethod("getOptionList", (Class<?>[])null);
			getoptionlist.setAccessible(true);
			Object returnvalue = getoptionlist.invoke(optionholder, (Object[])null);
			if (returnvalue instanceof Vector) {
				optionlist = (Vector) returnvalue;
			}
		} catch (Exception e) {
			return;
		}
		
		Object[] allopts = optionlist.toArray();
		// Step thru all options and remove the deprecated ones
		for (int k = 0; k < allopts.length; k++) {
			IOption curropt = (IOption) allopts[k];

			// remove 2.0.x toolchain options
			if (curropt.getId().startsWith(
					"de.innot.avreclipse.toolchain.options.target.mcutype")) {
				// get the selected target mcu and set the project property
				// accordingly
				String selectedmcuid = (String) curropt.getValue();
				String mcutype = selectedmcuid.substring(selectedmcuid
						.lastIndexOf(".") + 1);
				fProps.setValue(AVRTargetProperties.KEY_MCUTYPE, mcutype);
				optionholder.removeOption(curropt);
				continue;
			}

			if (curropt.getId().startsWith(
					"de.innot.avreclipse.toolchain.options.target.fcpu")) {
				// get the selected fcpu and set the project property
				// accordingly
				String selectedfcpu = (String) curropt.getValue();
				fProps.setValue(AVRTargetProperties.KEY_FCPU, selectedfcpu);
				optionholder.removeOption(curropt);
				continue;
			}

			// remove 2.0.x avrtarget options
			if (curropt.getId().indexOf(OLD_AVRTARGET_ID) != -1) {
				optionholder.removeOption(curropt);
				continue;
			}

			// remove old debug option from 2.0.0
			if (curropt.getName().endsWith("(-g)")) {
				optionholder.removeOption(curropt);
				continue;
			}
			
			// remove any other invalid options
			if (!curropt.isValid()) {
				optionholder.removeOption(curropt);
				continue;
			}

		} // for options
	}

}
