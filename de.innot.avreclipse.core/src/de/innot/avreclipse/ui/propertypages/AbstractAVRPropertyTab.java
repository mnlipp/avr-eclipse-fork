/**
 * 
 */
package de.innot.avreclipse.ui.propertypages;

import java.io.IOException;

import org.eclipse.cdt.core.settings.model.ICConfigurationDescription;
import org.eclipse.cdt.core.settings.model.ICResourceDescription;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.properties.AbstractCBuildPropertyTab;
import org.eclipse.cdt.ui.newui.ICPropertyProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import de.innot.avreclipse.core.preferences.AVRProjectProperties;

/**
 * @author U043192
 * 
 */
public abstract class AbstractAVRPropertyTab extends AbstractCBuildPropertyTab {

	private AbstractAVRPage fPage;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#createControls(org.eclipse.swt.widgets.Composite,
	 *      org.eclipse.cdt.ui.newui.ICPropertyProvider)
	 */
	@Override
	public void createControls(Composite parent, ICPropertyProvider provider) {
		if (provider instanceof AbstractAVRPage) {
			fPage = (AbstractAVRPage) provider;
		} else {
			return;
		}
		super.createControls(parent, provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performApply(org.eclipse.cdt.core.settings.model.ICResourceDescription,
	 *      org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void performApply(ICResourceDescription src, ICResourceDescription dst) {

		AVRProjectProperties sourceprops;

		if (fPage.isPerConfig()) {
			// We are on a per config setting
			sourceprops = new AVRProjectProperties(fPage.getProject());
		} else {
			// We have a per Project Setting
			ICConfigurationDescription cfgDes = src.getConfiguration();
			IConfiguration conf = ManagedBuildManager.getConfigurationForDescription(cfgDes);
			sourceprops = new AVRProjectProperties(conf);
		}

		AVRProjectProperties destinationprops = new AVRProjectProperties(sourceprops);
		performApply(sourceprops, destinationprops);

		try {
			fPage.getProjectPropertiesManager().save(destinationprops);
		} catch (IOException e) {
			// TODO Open a error dialog that the value could not be saved
			e.printStackTrace();
		}

	}

	protected abstract void performApply(AVRProjectProperties src, AVRProjectProperties dst);

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		AVRProjectProperties tc = new AVRProjectProperties();
		performDefaults(tc);
	}
	
	protected abstract void performDefaults(AVRProjectProperties defaults);

	@Override
	protected void updateButtons() {};
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractCPropertyTab#updateData(org.eclipse.cdt.core.settings.model.ICResourceDescription)
	 */
	@Override
	protected void updateData(ICResourceDescription resdesc) {

		AVRProjectProperties props = ProjectPropertyManager.getConfigProperties(resdesc);
		
		updateData(props);

	}

	protected abstract void updateData(AVRProjectProperties props);

	protected void addSeparator(Composite parent) {
		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1);
		separator.setLayoutData(gridData);
	}


}
