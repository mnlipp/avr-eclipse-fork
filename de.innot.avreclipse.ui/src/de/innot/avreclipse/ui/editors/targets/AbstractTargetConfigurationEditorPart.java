/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.ui.editors.targets;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IMessageManager;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationWorkingCopy;
import de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public abstract class AbstractTargetConfigurationEditorPart implements IFormPart, ITCEditorPart {

	private IManagedForm						fManagedForm;

	private IMessageManager						fMessageManager;

	private Composite							fParent;

	private Section								fSection;
	private Composite							fContentCompo;

	private ITargetConfigurationWorkingCopy		fTCWC;

	private final Map<String, String>			fLastValues				= new HashMap<String, String>();
	private final Map<String, String>			fLastDependentValues	= new HashMap<String, String>();

	private final ITargetConfigChangeListener	fListener				= new ChangeListener();

	private boolean								fIsCreated				= false;
	private boolean								fIsStale				= false;

	public final static String[]				EMPTY_LIST				= new String[] {};

	private class ChangeListener implements ITargetConfigChangeListener {

		/*
		 * (non-Javadoc)
		 * @seede.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener#
		 * attributeChange(de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String,
		 * java.lang.String, java.lang.String)
		 */
		public void attributeChange(ITargetConfiguration config, String attribute, String oldvalue,
				String newvalue) {

			if (fLastDependentValues.containsKey(attribute)) {
				String lastValue = fLastDependentValues.get(attribute);
				// the attribute is on the list of dependent values.
				// The part is stale when the nevalue is different from the last saved value
				if (lastValue == null) {
					fIsStale = true;
				} else {
					fIsStale = !lastValue.equals(newvalue);
				}

				fManagedForm.staleStateChanged();
				fLastDependentValues.put(attribute, newvalue);

				refreshWarnings();
			}
		}

	}

	/**
	 * Returns a list of attributes that are managed by this form part.
	 * <p>
	 * This list is used to manage the dirty state of the form part.
	 * </p>
	 * 
	 * @return Array with attributes. May be empty but never <code>null</code>
	 */
	public abstract String[] getPartAttributes();

	/**
	 * Returns a list of attributes whose changes cause the form part to become stale.
	 * <p>
	 * Changes to any of these attributes will cause {@link #refresh()} to be called, although the
	 * call may be delayed if the part is not currently visible.
	 * </p>
	 * 
	 * @return Array with attributes. May be empty but never <code>null</code>
	 */
	String[] getDependentAttributes() {
		return EMPTY_LIST;
	}

	abstract String getTitle();

	String getDescription() {
		return null;
	}

	abstract void createSectionContent(Composite parent, FormToolkit toolkit);

	abstract void refreshSectionContent();

	public void refreshWarnings() {
		// empty default.
	}

	int getSectionStyle() {
		return Section.EXPANDED | Section.TITLE_BAR | Section.CLIENT_INDENT;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.ITCEditorPart#createContent(org.eclipse.swt.widgets
	 * .Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	private Control createContent(Composite parent, FormToolkit toolkit) {
		fSection = toolkit.createSection(parent, getSectionStyle());
		fSection.setText(getTitle());

		Composite sectionClient = toolkit.createComposite(fSection);
		String description = getDescription();

		if (description != null) {
			sectionClient.setLayout(new TableWrapLayout());
			Label label = toolkit.createLabel(sectionClient, description, SWT.WRAP);
			label.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));

			Composite compo = toolkit.createComposite(sectionClient);
			compo.setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB));
			fContentCompo = compo;
		} else {
			fContentCompo = sectionClient;
		}

		createSectionContent(fContentCompo, toolkit);
		fSection.setClient(sectionClient);

		if ((fSection.getExpansionStyle() & Section.TWISTIE) != 0
				|| (fSection.getExpansionStyle() & Section.TREE_NODE) != 0) {
			fSection.addExpansionListener(new ExpansionAdapter() {
				public void expansionStateChanging(ExpansionEvent e) {
					// Do nothing
				}

				public void expansionStateChanged(ExpansionEvent e) {
					getManagedForm().getForm().reflow(false);
				}
			});
		}

		fIsCreated = true;

		return fSection;

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	public void initialize(IManagedForm form) {
		fManagedForm = form;

		if (fParent == null) {
			fParent = form.getForm().getBody();
		}

		if (!fIsCreated) {
			createContent(fParent, form.getToolkit());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#setFormInput(java.lang.Object)
	 */
	public boolean setFormInput(Object input) {
		if (input instanceof ITargetConfigurationWorkingCopy) {
			fTCWC = (ITargetConfigurationWorkingCopy) input;

			// Add the listener to the config to do the stale state management
			fTCWC.addPropertyChangeListener(fListener);

			// Save the current values for dirty state tracking
			String[] managedAttributes = getPartAttributes();
			for (String attr : managedAttributes) {
				String currValue = fTCWC.getAttribute(attr);
				fLastValues.put(attr, currValue);
			}

			// Save the current values for stale state tracking
			String[] dependentAttributes = getDependentAttributes();
			for (String attr : dependentAttributes) {
				String currValue = fTCWC.getAttribute(attr);
				fLastDependentValues.put(attr, currValue);
			}

			// Allow the form to redraw its dynamic parts.
			refresh();

			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#refresh()
	 */
	public void refresh() {
		fIsStale = false;
		refreshSectionContent();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#commit(boolean)
	 */
	public void commit(boolean onSave) {
		// if the form is actually saved (not just a page change),
		// then we take all managed attributes of the subclass and store their current
		// value. This is used to check if the form part is dirty or not.
		if (onSave) {
			String[] managedAttributes = getPartAttributes();
			for (String attr : managedAttributes) {
				String newvalue = fTCWC.getAttribute(attr);
				fLastValues.put(attr, newvalue);
			}
		}
		getManagedForm().dirtyStateChanged();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#dispose()
	 */
	public void dispose() {

		// Remove the listener.
		// overriding classes need to call super.dispose();

		if (fTCWC != null) {
			fTCWC.removePropertyChangeListener(fListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#isDirty()
	 */
	public boolean isDirty() {

		// Compare the current values of the target configuration with the last saved values. If at
		// least on of them is different, then the part is dirty.
		boolean isDirty = false;
		for (String attr : fLastValues.keySet()) {
			String currValue = fTCWC.getAttribute(attr);
			String lastValue = fLastValues.get(attr);
			if (!currValue.equals(lastValue)) {
				isDirty = true;
				break; // no need to compare further
			}
		}

		return isDirty;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#isStale()
	 */
	public boolean isStale() {
		return fIsStale;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.forms.IFormPart#setFocus()
	 */
	public void setFocus() {
		if (fSection != null) {
			Control client = fSection.getClient();
			if (client != null) {
				client.setFocus();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.ITCEditorPart#setFocus(java.lang.String)
	 */
	public boolean setFocus(String attribute) {
		return false;
	}

	public IManagedForm getManagedForm() {
		Assert.isNotNull(fManagedForm, "getManagedForm() called before initialize()");
		return fManagedForm;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.ITCEditorPart#getControl()
	 */
	public Section getControl() {
		return fSection;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.ITCEditorPart#setParent(org.eclipse.swt.widgets.Composite
	 * )
	 */
	public void setParent(Composite parent) {
		fParent = parent;
	}

	public void setMessageManager(IMessageManager manager) {
		fMessageManager = manager;
	}

	public IMessageManager getMessageManager() {
		if (fMessageManager == null) {
			fMessageManager = fManagedForm.getMessageManager();
		}
		return fMessageManager;
	}

	public ITargetConfigurationWorkingCopy getTargetConfiguration() {
		Assert.isNotNull(fTCWC, "getTargetConfiguration() called before setFormInput()");
		return fTCWC;
	}

}
