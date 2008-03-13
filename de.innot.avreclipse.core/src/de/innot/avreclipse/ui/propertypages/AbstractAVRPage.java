/**
 * 
 */
package de.innot.avreclipse.ui.propertypages;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.CDTPropertyManager;
import org.eclipse.cdt.ui.newui.ICPropertyTab;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;

import de.innot.avreclipse.core.preferences.AVRTargetProperties;
import de.innot.avreclipse.core.preferences.TargetConfiguration;

/**
 * @author Thomas Holland
 * 
 */
public abstract class AbstractAVRPage extends AbstractPage {

	private Group fConfigGroup;

	private static Boolean fPerConfigFlag = null;

	private/* static */Map<String, TargetConfiguration> fTargetCfgs;
	
	public AbstractAVRPage() {
		if (fPerConfigFlag == null) {
			boolean perconfigflag = getPreferenceStore().getBoolean(AVRTargetProperties.KEY_PER_CONFIG);
			fPerConfigFlag = Boolean.valueOf(perconfigflag);
		}

		if (fTargetCfgs == null) {
			fTargetCfgs = new HashMap<String, TargetConfiguration>();
		}
	}

	@Override
	protected void contentForCDT(Composite composite) {

		super.contentForCDT(composite);

		fConfigGroup = findFirstGroup(composite);
		
		// set the visibility to the current Setting
		setConfigSetting(isConfigSetting());
		
	}

	public TargetConfiguration getTargetConfiguration(ICResourceDescription resdesc) {
		if (fTargetCfgs == null) {
			fTargetCfgs = new HashMap<String, TargetConfiguration>();
		}
		if (resdesc == null) {
			String projectname = getProject().getName();
			if (fTargetCfgs.containsKey(projectname)) {
				return fTargetCfgs.get(projectname);
			}
			TargetConfiguration newcfg = new TargetConfiguration(getProject());
			fTargetCfgs.put(projectname, newcfg);
			return newcfg;
		} else {
			if (fTargetCfgs.containsKey(resdesc.getId())) {
				return fTargetCfgs.get(resdesc.getId());
			}
			ICConfigurationDescription cfgDes = resdesc.getConfiguration();
			IConfiguration conf = ManagedBuildManager.getConfigurationForDescription(cfgDes);
			TargetConfiguration newcfg = new TargetConfiguration(conf);
			fTargetCfgs.put(conf.getId(), newcfg);
			return newcfg;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performApply()
	 */
	@Override
	public void performApply() {
		doSave();
		super.performApply();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		return super.performCancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		fTargetCfgs.clear();
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performOk()
	 */
	@Override
	public boolean performOk() {
		doSave();
		return super.performOk();
	}

	private void doSave() {
		getPreferenceStore().setValue(AVRTargetProperties.KEY_PER_CONFIG, fPerConfigFlag);
		try {
			AVRTargetProperties.savePreferences(getPreferenceStore());
			ICConfigurationDescription[] cfgdescs = getCfgsEditable();

			// Save all edited TargetConfigurations
			for (ICConfigurationDescription desc : cfgdescs) {
				String cfgid = desc.getConfiguration().getId();
				if (fTargetCfgs.containsKey(cfgid)) {
					TargetConfiguration tc = fTargetCfgs.get(cfgid);
					tc.save();
				}
			}
			// Save project TargetSettings

			TargetConfiguration projectcfg = fTargetCfgs.get(getProject().getName());
			if (projectcfg != null) {
				projectcfg.save();
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	protected void setConfigSetting(boolean flag) {
		fPerConfigFlag = flag;
		if (fConfigGroup != null) {
			setEnabled(fConfigGroup, flag);
		}
		
		// Inform all our Tabs about the change
		if (flag) {
			forEach(ICPropertyTab.UPDATE, getResDesc());
		} else {
			forEach(ICPropertyTab.UPDATE, null);
		}


		// inform all other AVRAbstractPages about this change
		int pagecount = CDTPropertyManager.getPagesCount();
		for (int i = 0; i < pagecount; i++) {
			Object page = CDTPropertyManager.getPage(i);
			if ((page != null) && (page instanceof AbstractAVRPage)) {
				AbstractAVRPage ap = (AbstractAVRPage) page;
				// don't call ourself
				if (this == ap)
					continue;
				ap.setConfigSetting(flag);
			}
		}
	}

	public boolean isConfigSetting() {
		return fPerConfigFlag;
	}
	
	public int convertCharToPixel(int chars) {
		return convertWidthInCharsToPixels(chars);
	}

	private Group findFirstGroup(Composite parent) {
		Control[] children = parent.getChildren();
		if (children == null || children.length == 0) {
			return null;
		}
		for (Control child : children) {
			if (child instanceof Group) {
				return (Group) child;
			}
			if (child instanceof Composite) {
				Group recursive = findFirstGroup((Composite) child);
				if (recursive != null) {
					return recursive;
				}
			}
		}

		return null;
	}

	private void setEnabled(Group parent, boolean value) {
		Control[] children = parent.getChildren();
		for (Control child : children) {
			// TODO Change to setVisible
			child.setEnabled(value);
		}
	}
}
