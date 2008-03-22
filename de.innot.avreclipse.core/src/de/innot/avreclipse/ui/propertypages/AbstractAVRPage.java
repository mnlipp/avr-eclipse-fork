/**
 * 
 */
package de.innot.avreclipse.ui.propertypages;

import java.util.List;

import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PropertyPage;

import de.innot.avreclipse.core.preferences.ProjectProperties;

/**
 * @author Thomas Holland
 * 
 */
public abstract class AbstractAVRPage extends AbstractPage {

	private Group fConfigGroup;

	private static ProjectProperties fPropertiesManager = null;
	
	@Override
	protected void contentForCDT(Composite composite) {

		super.contentForCDT(composite);
		
		// Get the Project Properties (if they have not yet been loaded by another page)
		if ( fPropertiesManager == null) {
			fPropertiesManager = ProjectPropertyManager.getProjectProperties(this, getProject());
		}

		fConfigGroup = findFirstGroup(composite);
		
		// set the visibility to the current Setting
		setPerConfig(fPropertiesManager.isPerConfig());
		
	}


	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		ProjectPropertyManager.performCancel(this);
		return super.performCancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performDefaults()
	 */
	@Override
	public void performDefaults() {
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performOk()
	 */
	@Override
	public boolean performOk() {
		ProjectPropertyManager.performOK(this);
		return super.performOk();
	}
	
	protected ProjectProperties getProjectPropertiesManager() {
		return fPropertiesManager;
	}
	protected boolean isPerConfig() {
		return fPropertiesManager.isPerConfig();
	}
	
	protected void setPerConfig(boolean flag) {
		fPropertiesManager.setPerConfig(flag);
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
		List<PropertyPage> allpages = ProjectPropertyManager.getPages();
		for (PropertyPage page : allpages) {
			if ((page != null) && (page instanceof AbstractAVRPage)) {
				AbstractAVRPage ap = (AbstractAVRPage) page;
				// don't call ourself
				if (this == ap)
					continue;
				ap.setPerConfig(flag);
			}
		}
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
