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

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.core.preferences.AVRProjectProperties;
import de.innot.avreclipse.core.preferences.ProjectPropertyManager;

/**
 * Abstract parent class for all AVR Property tabs.
 * <p>
 * This class is an interface between <code>ICPropertyTab</code>, which works
 * on ICResourceDescriptions, and the {@link AVRProjectProperties} where all AVR
 * specific settings are stored, either per project or - at user discretion -
 * per build configuration.
 * </p>
 * {@link #performApply(AVRProjectProperties)} and
 * {@link #updateData(AVRProjectProperties)} are almost identical to the methods
 * in <code>ICPropertyTab</code>, while <code>performDefaults()</code> is
 * replaced by {@link #performCopy(AVRProjectProperties)}, which enables this
 * class to send different default properties to the implementor.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public abstract class AbstractAVRPropertyTab extends AbstractCBuildPropertyTab {

	/**
	 * special Tab message to indicate that the given Properties should be
	 * copied. This is very similar to {@link ICPropertyTab#DEFAULTS} message.
	 */
	public final static int COPY = 200;

	/**
	 * Action for an Apply event.
	 * <p>
	 * The implementation must copy the values relevant to the current page to
	 * the given destination properties.
	 * </p>
	 * The given properties are fresh, unmodified props from the properties
	 * storage. They will be saved once this method returns.
	 * </p>
	 * 
	 * @param dstprops
	 *            Destination properties.
	 */
	protected abstract void performApply(AVRProjectProperties dstprops);

	/**
	 * Action for a Copy event.
	 * <p>
	 * The implementation must copy the values relevant to the current page from
	 * the given source properties.
	 * </p>
	 * <p>
	 * This method is called with either the default properties or with the
	 * project properties, depending on whether the "Defaults" or the "Copy from
	 * Project" Button has been clicked by the user.
	 * </p>
	 * 
	 * @param srcprops
	 *            Source properties.
	 */
	protected abstract void performCopy(AVRProjectProperties srcprops);

	/**
	 * Update the tab to the values of the given properties.
	 * <p>
	 * This method is called whenever a different build configuration is
	 * selected by the user or the "per Config Settings" flag has changed. The
	 * props parameter has the properties for the configuration / project.
	 * </p>
	 * <p>
	 * Implementing classes should update their controls to the values of the
	 * properties and can must make all future modifications directly to the
	 * given properties.
	 * </p>
	 * 
	 * @param props
	 *            <code>AVRProjectProperties</code> the tab must work with.
	 */
	protected abstract void updateData(AVRProjectProperties props);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#handleTabEvent(int,
	 *      java.lang.Object)
	 */
	@Override
	public void handleTabEvent(int kind, Object data) {
		// Override handleTabEvent to handle the COPY message.
		// This message is very similar to DEFAULTS, and from the viewpoint of
		// extending classes, identical, as both call the performCopy() of the
		// extending class.
		switch (kind) {
		case COPY:
			AVRProjectProperties projectprops = (AVRProjectProperties) data;
			performCopy(projectprops);
			break;
		default:
			super.handleTabEvent(kind, data);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription,
	 *      org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {

		// Apply should only save the values of this Tab.
		// To do this, we get a fresh Property Element, which is filled with the
		// values from the property storage.
		// Then this new Element is passed on to the extending class, which
		// modifies only its own values.
		// Finally the Element is saved again to the property storage.

		AVRProjectProperties freshprops;

		freshprops = AVRPropertyPageManager.getConfigPropertiesNoCache(src);

		performApply(freshprops);

		try {
			freshprops.save();
		} catch (BackingStoreException e) {
			// TODO Open a error dialog that the value could not be saved
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		// Get the default Properties and let the extending class copy the
		// relevant values from it.
		AVRProjectProperties tc = ProjectPropertyManager.getDefaultProperties();
		performCopy(tc);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateData(org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void updateData(ICResourceDescription resdesc) {

		// Translate ICResourceDescription to AVRProjectProperties and pass them
		// to the extening class.
		AVRProjectProperties props = AVRPropertyPageManager.getConfigProperties(resdesc);
		updateData(props);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateButtons()
	 */
	@Override
	protected void updateButtons() {
		// Why was this method made Abstract by the superclass?
		// As only few pages actually need this, it could have been declared as
		// an empty method.
		// Like we do here, to spare extending classes of implementing this
		// useless method.
	};

	/**
	 * Sets the rebuild flag for the current configuration or the complete
	 * project.
	 * <p>
	 * Passing <code>false</code> is not recommended, as it might prevent
	 * necessary rebuilds caused by changes outside of the AVR property world.
	 * </p>
	 * 
	 * @param rebuild
	 *            <code>true</code> if a complete rebuild is required.
	 */
	protected void setRebuildState(boolean rebuild) {

		// Check if we have per project or per config setting
		AbstractAVRPage avrpage = (AbstractAVRPage) page;
		if (avrpage.isPerConfig()) {
			// Set the rebuild flag for the current configuration
			getCfg().setRebuildState(rebuild);
		} else {
			// Set the rebuild flag for the complete project
			ManagedBuildManager.getBuildInfo(getCfg().getOwner()).setRebuildState(rebuild);
		}
	}

	/**
	 * Convenience method to add a separator bar to the composite.
	 * <p>
	 * The parent composite must have a <code>GridLayout</code>. The
	 * separator bar will span all columns of the parent grid layout.
	 * </p>
	 * 
	 * @param parent
	 *            <code>Composite</code>
	 */
	protected void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		Layout parentlayout = parent.getLayout();
		if (parentlayout instanceof GridLayout) {
			int columns = ((GridLayout) parentlayout).numColumns;
			GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false, columns, 1);
			separator.setLayoutData(gridData);
		}
	}

}
