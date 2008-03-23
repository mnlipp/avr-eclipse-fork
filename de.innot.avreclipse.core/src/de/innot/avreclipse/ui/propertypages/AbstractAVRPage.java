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
package de.innot.avreclipse.ui.propertypages;

import java.util.List;

import org.eclipse.cdt.ui.newui.AbstractPage;
import org.eclipse.cdt.ui.newui.ICPropertyTab;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.dialogs.PropertyPage;

import de.innot.avreclipse.core.preferences.ProjectPropertyManager;

/**
 * This is the parent for all AVR Project property pages.
 * <p>
 * This class extends CDT AbstractPage to participate in the build configuration
 * handling it provides.
 * </p>
 * 
 * @see AbstractPage
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public abstract class AbstractAVRPage extends AbstractPage {

	/** The configuration selection group from the AbstractPage class */
	private Group fConfigGroup;

	/** The ProjectPropertyManager for the current project */
	private ProjectPropertyManager fPropertiesManager = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#contentForCDT(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected void contentForCDT(Composite composite) {

		// We override this method to get a reference to the configuration
		// selection group.
		// This is a hack, but as far as I can see this is the only way to get
		// the group without reimplementing most of the AbstractPage class.

		super.contentForCDT(composite);

		// Get the configuration selection group and set its visibility to the
		// current setting of the "per config" flag.
		fConfigGroup = findFirstGroup(composite);
		loadPropertiesManager();
		setPerConfig(fPropertiesManager.isPerConfig());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		// First remove any modifications made to the AVR properties,
		// then let the superclass handle the CDT specific stuff.
		AVRPropertyPageManager.performCancel(this);
		return super.performCancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#performOk()
	 */
	@Override
	public boolean performOk() {
		// First save and sync the AVR specific Properties,
		// then let the superclass handle the CDT specific modifications.
		AVRPropertyPageManager.performOK(this, getCfgsEditable());
		return super.performOk();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		// I override this method just to make sure that the
		// ProjectPropertiesManager has been loaded.
		// There were some problems before.
		if (visible) {
			loadPropertiesManager();
		}
		super.setVisible(visible);
	}

	/**
	 * Returns the value of the "per config" flag for this project.
	 * 
	 * @return <code>true</code> if each build configuration has its own
	 *         properties.
	 */
	protected boolean isPerConfig() {
		loadPropertiesManager();
		return fPropertiesManager.isPerConfig();
	}

	/**
	 * Set the project "per config" flag.
	 * <p>
	 * This method will set the flag and inform all its tabs and all other AVR
	 * Property pages registered with the page manager about the change.
	 * </p>
	 * 
	 * @param flag
	 *            <code>true</code> to enable "per config" settings.
	 */
	protected void setPerConfig(boolean flag) {
		// Test if flag value has changed to avoid the overhead of informing
		// everyone for non-changes.
		if (flag == isPerConfig()) {
			return;
		}

		// inform all open AVRAbstractPages (including ourself) about the
		// changed "per config" flag.
		List<PropertyPage> allpages = AVRPropertyPageManager.getPages();
		for (PropertyPage page : allpages) {
			if ((page != null) && (page instanceof AbstractAVRPage)) {
				AbstractAVRPage ap = (AbstractAVRPage) page;
				ap.internalSetPerConfig(flag);
			}
		}
	}

	/**
	 * Set the "per config" flag for this page and inform all child tabs about
	 * the change.
	 * 
	 * @param flag
	 *            New value of the "per config" flag.
	 */
	private void internalSetPerConfig(boolean flag) {
		fPropertiesManager.setPerConfig(flag);
		if (fConfigGroup != null) {
			setEnabled(fConfigGroup, flag);
		}

		// Inform all our Tabs about the change.
		// We pass a ICResourceDescription, even if it is not used.
		forEach(ICPropertyTab.UPDATE, getResDesc());
	}

	/**
	 * get the Properties Manager from the page manager.
	 */
	private void loadPropertiesManager() {
		// This call makes sure that the internal value for the getProject()
		// call below has been initialized
		checkElement();

		// Get the Project Properties Manager (if it has not yet been loaded by
		// another page)
		fPropertiesManager = AVRPropertyPageManager.getProjectProperties(this, getProject());
	}

	/**
	 * Get the configuration selection group from the parent.
	 * <p>
	 * This is a hack to get a reference to the configuration selection group of
	 * a standard {@link AbstractPage}. The returned reference can be used to
	 * enable/disable the group as required.
	 * </p>
	 * 
	 * @param parent
	 *            a composite having the configuration selection as its first
	 *            group.
	 * @return A reference to the first group within the given composite
	 */
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

	/**
	 * Enable / Disable the given group.
	 * 
	 * @param group
	 *            A <code>Group</code> with some controls.
	 * @param value
	 *            <code>true</code> to enable, <code>false</code> to disable
	 *            the given group.
	 */
	private void setEnabled(Group group, boolean value) {
		Control[] children = group.getChildren();
		for (Control child : children) {
			// TODO Change to setVisible
			child.setEnabled(value);
		}
	}

}
