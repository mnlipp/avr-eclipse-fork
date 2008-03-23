/**
 * 
 */
package de.innot.avreclipse.ui.propertypages;

import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.core.preferences.AVRProjectProperties;
import de.innot.avreclipse.core.preferences.ProjectPropertyManager;

/**
 * @author U043192
 * 
 */
public abstract class AbstractAVRPropertyTab extends AbstractCBuildPropertyTab {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription,
	 *      org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {

		// Apply should only save the values of this Tab.
		// To do this, we get a new Property Element, which is filled with the
		// values from the store.
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

	/**
	 * Action for an Apply event.
	 * <p>
	 * 
	 * @param dst
	 */
	protected abstract void performApply(AVRProjectProperties dst);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		AVRProjectProperties tc = ProjectPropertyManager.getDefaultProperties();
		performDefaults(tc);
	}

	protected abstract void performDefaults(AVRProjectProperties defaults);

	@Override
	protected void updateButtons() {
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateData(org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void updateData(ICResourceDescription resdesc) {

		AVRProjectProperties props = AVRPropertyPageManager.getConfigProperties(resdesc);

		updateData(props);

	}

	protected abstract void updateData(AVRProjectProperties props);

	protected void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1);
		separator.setLayoutData(gridData);
	}

}
