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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
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
 * <p>
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
	 * <p>
	 * The receiver should call <code>super.performCopy(srcprops)</code> to
	 * let intermediate classes do any additional handling.
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
	 * <p>
	 * The receiver should call <code>super.updateData(props)</code> to let
	 * intermediate classes do any additional handling.
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
		// This is implemented for the "Copy Project Settings" Button.
		switch (kind) {
		case COPY:
			AVRProjectProperties projectprops = (AVRProjectProperties) data;
			updateData(projectprops);
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
	protected void performApply(ICResourceDescription src,
			ICResourceDescription dst) {

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
		// to the extending class.
		AVRProjectProperties props = AVRPropertyPageManager
				.getConfigProperties(resdesc);
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
			ManagedBuildManager.getBuildInfo(getCfg().getOwner())
					.setRebuildState(rebuild);
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
			GridData gridData = new GridData(SWT.FILL, SWT.NONE, true, false,
					columns, 1);
			separator.setLayoutData(gridData);
		}
	}

	/**
	 * Returns the value of the "per config" flag for this project.
	 * 
	 * @return <code>true</code> if each build configuration has its own
	 *         properties.
	 */
	protected boolean isPerConfig() {
		if (page instanceof AbstractAVRPage) {
			AbstractAVRPage avrpage = (AbstractAVRPage) page;
			return avrpage.isPerConfig();
		}
		return true;
	}

	/**
	 * Create and return a "Workplace" browse Button.
	 * <p>
	 * Clicking the Button will open a Workplace file selector Dialog and the
	 * result is copied to the supplied <code>Text</code> Control.
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>, which needs to have
	 *            <code>GridLayout</code>
	 * @param text
	 *            Target <code>Text</code> Control
	 * @return <code>Button</code> Control with the created Button.
	 */
	protected Button setupWorkplaceButton(Composite parent, final Text text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(WORKSPACEBUTTON_NAME);
		GridData gd = new GridData(SWT.CENTER, SWT.NONE, false, false);
		// make all Buttons the same size
		gd.minimumWidth = BUTTON_WIDTH;
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String location = getWorkspaceFileDialog(text.getShell(),
						EMPTY_STR);
				if (location != null) {
					text.setText(location);
				}
			}
		});
		return button;
	}

	/**
	 * Create and return a "Filesystem" browse Button.
	 * <p>
	 * Clicking the Button will open a file selector Dialog and the result is
	 * copied to the supplied <code>Text</code> Control.
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>, which needs to have
	 *            <code>GridLayout</code>
	 * @param text
	 *            Target <code>Text</code> Control
	 * @param exts
	 *            <code>String[]</code> with all valid file extensions. Files
	 *            with other extensions will be filtered.
	 * @return <code>Button</code> Control with the created Button.
	 */
	protected Button setupFilesystemButton(Composite parent, final Text text,
			final String[] exts) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(FILESYSTEMBUTTON_NAME);
		GridData gd = new GridData(SWT.CENTER, SWT.NONE, false, false);
		// make all Buttons the same size
		gd.minimumWidth = BUTTON_WIDTH;
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String location = getFileSystemFileDialog(text.getShell(),
						EMPTY_STR, exts);
				if (location != null) {
					text.setText(location);
				}
			}
		});
		return button;
	}

	/**
	 * Create and return a "Variable" browse Button.
	 * <p>
	 * Clicking the Button will open a variable selector Dialog and the result
	 * is inserted into the supplied <code>Text</code> Control at the current
	 * cursor position.
	 * </p>
	 * 
	 * @param parent
	 *            Parent <code>Composite</code>, which needs to have
	 *            <code>GridLayout</code>
	 * @param text
	 *            Target <code>Text</code> Control
	 * @return <code>Button</code> Control with the created Button.
	 */
	protected Button setupVariableButton(Composite parent, final Text text) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText(VARIABLESBUTTON_NAME);
		GridData gd = new GridData(SWT.CENTER, SWT.NONE, false, false);
		// make all Buttons the same size
		gd.minimumWidth = BUTTON_WIDTH;
		button.setLayoutData(gd);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				String var = getVariableDialog(text.getShell(), getResDesc()
						.getConfiguration());
				if (var != null) {
					text.insert(var);
				}
			}
		});
		return button;
	}

	/**
	 * Open a FileSystem Dialog and return the selected file as a
	 * <code>String</code>.
	 * 
	 * @param shell
	 *            Shell in which to open the Dialog
	 * @param text
	 *            Root file name
	 * @param exts
	 *            <code>String[]</code> with all valid file extensions. Files
	 *            with other extensions will be filtered.
	 * @return <code>String</code> with the selected filename or <cod>null</code>
	 *         if the user has cancelled or an error occured.
	 */
	public static String getFileSystemFileDialog(Shell shell, String text,
			String[] exts) {

		// Why has the AbstractCPropertyTab.getFileSystemDialog() a hardcoded
		// list of extensions?
		// This is basically the same method, but with a list of valid
		// extensions as parameter.
		FileDialog dialog = new FileDialog(shell);
		if (text != null && text.trim().length() != 0)
			dialog.setFilterPath(text);
		dialog.setFilterExtensions(exts);
		dialog.setText(FILESYSTEM_FILE_DIALOG_TITLE);
		return dialog.open();
	}

}
