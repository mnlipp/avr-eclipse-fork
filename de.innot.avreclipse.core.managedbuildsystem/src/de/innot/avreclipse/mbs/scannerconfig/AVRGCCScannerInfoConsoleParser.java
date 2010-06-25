
package de.innot.avreclipse.mbs.scannerconfig;

import org.eclipse.cdt.build.core.scannerconfig.CfgInfoContext;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.InfoContext;
import org.eclipse.cdt.make.internal.core.scannerconfig.gnu.GCCScannerInfoConsoleParser;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;

/** 
 * Parse make output for usable info.
 * 
 * "Based" on ManagedGCCScannerInfoConsoleParser, which unfortunately is
 * not exposed from the org.eclipse.cdt.managedbuilder.core plugin.
 */
@SuppressWarnings("restriction")
public class AVRGCCScannerInfoConsoleParser extends GCCScannerInfoConsoleParser {
	Boolean fManagedBuildOnState;

	@Override
	public boolean processLine(String line) {
		if (isManagedBuildOn())
			return false;
		return super.processLine(line);
	}

	@Override
	public void shutdown() {
		if (!isManagedBuildOn()) {
			super.shutdown();
		}
		fManagedBuildOnState = null;
	}

	@Override
	public void startup(IProject project, IScannerInfoCollector collector) {
		if (isManagedBuildOn())
			return;
		super.startup(project, collector);
	}

	protected boolean isManagedBuildOn() {
		if (fManagedBuildOnState == null)
			fManagedBuildOnState = Boolean.valueOf(doCalcManagedBuildOnState());
		return fManagedBuildOnState.booleanValue();
	}

	protected boolean doCalcManagedBuildOnState() {
		IScannerInfoCollector cr = getCollector();
		InfoContext c;
		if (cr instanceof PerProjectSICollector) {
			c = ((PerProjectSICollector) cr).getContext();
		} else {
			return false;
		}

		IProject project = c.getProject();
		ICProjectDescription des = CoreModel.getDefault().getProjectDescription(project, false);
		CfgInfoContext cc = CfgInfoContext.fromInfoContext(des, c);
		if (cc != null) {
			IConfiguration cfg = cc.getConfiguration();
			return cfg.isManagedBuildOn();
		}
		return false;
	}
}
