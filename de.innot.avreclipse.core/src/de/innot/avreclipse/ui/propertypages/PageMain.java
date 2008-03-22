/**
 * 
 */
package de.innot.avreclipse.ui.propertypages;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import de.innot.avreclipse.core.preferences.AVRTargetProperties;

/**
 * @author U043192
 *
 */
public class PageMain extends AbstractAVRPage {

	private static final String TEXT_PERCONFIG = "Enable individual settings for Build Configurations";

	private Button fPerConfigButton;

	@Override
	protected void contentForCDT(Composite composite) {

		fPerConfigButton = new Button(composite, SWT.CHECK);
		fPerConfigButton.setText(TEXT_PERCONFIG);
		fPerConfigButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				perConfigSettingsAction();
			}
		});

		fPerConfigButton.setSelection(super.isPerConfig());
		perConfigSettingsAction();

	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#isSingle()
	 */
	@Override
	protected boolean isSingle() {
		return true;
	}
	
	public IPreferenceStore getPreferenceStore() {
		return AVRTargetProperties.getPropertyStore(getProject());
	}

	private void perConfigSettingsAction() {
		boolean newvalue = fPerConfigButton.getSelection();
		super.setPerConfig(newvalue);
	}


}
