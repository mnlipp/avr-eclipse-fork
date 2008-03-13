/**
 * 
 */
package de.innot.avreclipse.core.preferences;

import java.io.IOException;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * @author U043192
 * 
 */
public class TargetConfiguration {

	private String fMCUid;

	private String fFCPU;

	private IConfiguration fConfig;

	private IProject fProject;

	IPreferenceStore fStore;
	
	private boolean fDirty;

	public TargetConfiguration(IConfiguration cfg) {
		fConfig = cfg;
		fProject = (IProject) cfg.getManagedProject().getOwner();
		fStore = AVRTargetProperties.getPropertyStore(fConfig);
		loadData();
	}
	
	public TargetConfiguration(IProject proj) {
		fConfig = null;
		fProject = proj;
		fStore = AVRTargetProperties.getPropertyStore(fProject);
		loadData();
	}

	public String getMCUId() {
		return fMCUid;
	}

	public void setMCUId(String mcuid) {
		if (!fMCUid.equals(mcuid)) {
			fMCUid = mcuid;
			fDirty = false;
		}
	}

	public String getFCPU() {
		return fFCPU;
	}

	public void setFCPU(String fcpu) {
		if (!fFCPU.equals(fcpu)) {
			fFCPU = fcpu;
			fDirty = true;
		}
	}

	private void loadData() {
		fMCUid = fStore.getString(AVRTargetProperties.KEY_MCUTYPE);
		fFCPU = fStore.getString(AVRTargetProperties.KEY_FCPU);
		fDirty = false;
	}
	
	public IPreferenceStore getPreferenceStore() {
		return fStore;
	}
	
	public void save() throws IOException {
		if (fDirty) {
			fDirty = false;
			fStore.setValue(AVRTargetProperties.KEY_MCUTYPE, fMCUid);
			fStore.setValue(AVRTargetProperties.KEY_FCPU, fFCPU);
			AVRTargetProperties.savePreferences(fStore);
		}
	}
	
	public String toString() {
		if (fConfig == null) {
			return "Project: " + fProject.getName()+ (fDirty ? "*":" ");
		} else {
			return "Configuration: " + fConfig.getName() + (fDirty ? "*":" ") +"("+fConfig.getId()+")";
		}
	}
}
