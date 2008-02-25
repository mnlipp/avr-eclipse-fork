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
package de.innot.avreclipse.ui.views.supportedmcu;

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import de.innot.avreclipse.PluginIDs;
import de.innot.avreclipse.core.preferences.AVRTargetProperties;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * This is the main part of the Supported MCUs View.
 * 
 * @author Thomas Holland
 * @since 2.2
 */

public class SupportedMCUs extends ViewPart {

	// The parent Composite of this Viewer
	private Composite fViewParent;

	private TableViewer fTable;
	private SupportedContentProvider fProvider;

	@SuppressWarnings("unused")
	private IMemento fMemento;

	private ISelectionListener fWorkbenchSelectionListener;

	private SupportedModel fModel = null;

	public enum LabelStyle {
		SHOW_STRING, SHOW_YESNO, SHOW_URL;
	}

	/**
	 * The constructor.
	 * 
	 * Nothing done here.
	 */
	public SupportedMCUs() {
	}

	/*
	 * (non-Javadoc) Method declared on IViewPart.
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		// Initialize the SuperClass and store the passed memento for use by
		// the individual methods.
		super.init(site, memento);
		fMemento = memento;
	}

	@Override
	public void saveState(IMemento memento) {
		// Save the current state of the viewer
		super.saveState(memento);

		// TODO: Save the Column Layout for each category

	}

	/**
	 * Create, layout and initialize the controls of this viewer
	 */
	@Override
	public void createPartControl(Composite parent) {

		fViewParent = parent;

		fModel = new SupportedModel();

		// All listeners that are need to unregistered on dispose()
		fWorkbenchSelectionListener = new WorkbenchSelectionListener();

		TableColumnLayout tcl = new TableColumnLayout();
		fViewParent.setLayout(tcl);

		fProvider = new SupportedContentProvider();

		fTable = new TableViewer(parent, SWT.BORDER | SWT.FULL_SELECTION);
		fTable.setContentProvider(fProvider);
		fTable.setUseHashlookup(true);
		fTable.getTable().setHeaderVisible(true);
		fTable.getTable().setLinesVisible(true);

		MCUProviderEnum[] providerlist = MCUProviderEnum.values();
		for (MCUProviderEnum provider : providerlist) {
			TableViewerColumn column = new TableViewerColumn(fTable, SWT.NONE);
			column.getColumn().setText(provider.getName());
			switch (provider.getLabelStyle()) {
			case SHOW_STRING:
				column.setLabelProvider(new StringColumnLabelProvider(provider));
				tcl.setColumnData(column.getColumn(), new ColumnWeightData(20, 60));
				break;
			case SHOW_YESNO:
				column.setLabelProvider(new BooleanColumnLabelProvider(provider));
				column.getColumn().setAlignment(SWT.CENTER);
				tcl.setColumnData(column.getColumn(), new ColumnWeightData(5, 60));
				break;
			case SHOW_URL:
				column.setLabelProvider(new URLColumnLabelProvider(provider));
				column.setEditingSupport(new URLEditingSupport(fTable, provider));
				tcl.setColumnData(column.getColumn(), new ColumnWeightData(20, 60));
				break;
			}
		}

		OwnerDrawLabelProvider.setUpOwnerDraw(fTable);
		ColumnViewerToolTipSupport.enableFor(fTable, ToolTip.NO_RECREATE);
		
		fTable.setInput(fModel);

		// Add the Table as a Workbench Selection provider
		getSite().setSelectionProvider(fTable);

		// Activate the Workbench selection listener
		getSite().getWorkbenchWindow().getSelectionService().addPostSelectionListener(
		        fWorkbenchSelectionListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
	 */
	@Override
	public void setFocus() {
		// This is a pure viewer, so nothing to set the focus to
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose() {
		// remove the listeners from their objects
		getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(
		        fWorkbenchSelectionListener);
		super.dispose();
	}

	/**
	 * Handle Model Change Events.
	 * <p>
	 * This is called from the current DeviceDescriptionProvider whenever its
	 * internal data has changed. For example, when the user changes the path to
	 * the source data.
	 * </p>
	 */
	/*
	 * private class ModelChangeListener implements IModelChangeListener {
	 * 
	 * public void modelChange() { // We don't know which Threat this comes
	 * from. // Assume its not the SWT Threat and act accordingly
	 * fViewParent.getDisplay().asyncExec(new Runnable() { public void run() {
	 * modelChanged(); } }); // Runnable } }
	 */
	/**
	 * Handle Selection Change Events.
	 * <p>
	 * This is called by the workbench selection services to inform this viewer,
	 * that something has been selected on the workbench. If something with an
	 * AVR MCU type has been selected (Project), then the viewer will show the
	 * description of the associated mcu.
	 * </p>
	 */
	private class WorkbenchSelectionListener implements ISelectionListener {

		public void selectionChanged(IWorkbenchPart part, final ISelection selection) {
			// we ignore our own selections
			if (part == SupportedMCUs.this) {
				return;
			}

			// To minimize the GUI impact run the rest of this method in a Job
			Job selectionjob = new Job("AVR Supported MCU List") {

				@Override
				protected IStatus run(IProgressMonitor monitor) {

					String newid = null;
					try {

						monitor.beginTask("Selection Change", 3);
						List<String> mculist = fModel.getMCUList();
						monitor.worked(1);

						// see if the selection is something that has an avr mcu
						// id
						if (selection instanceof IStructuredSelection) {
							// First: Projects
							IStructuredSelection ss = (IStructuredSelection) selection;
							newid = getMCUId(ss);
						} else if (selection instanceof ITextSelection) {
							// Second: Selected Text
							String text = ((ITextSelection) selection).getText();
							// Test if the text is an MCU name/id
							if (mculist.contains(AVRMCUidConverter.name2id(text))) {
								// yes: use it
								newid = AVRMCUidConverter.name2id(text);
							}
						}
						monitor.worked(1);
						if (newid != null) {
							// Test if it is a valid mcu id. Only set valid mcu
							// ids
							// as otherwise an error message would be displayed
							if (!mculist.contains(AVRMCUidConverter.name2id(newid))) {
								return Status.OK_STATUS;
							}

							// The next call will cause a SelectionChange Event
							// which handles the rest of the change
							final IStructuredSelection newselection = new StructuredSelection(newid);
							fViewParent.getDisplay().asyncExec(new Runnable() {
								public void run() {
									fTable.setSelection(newselection, true);
								}
							}); // Runnable
						}
						monitor.worked(1);
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				}
			};
			selectionjob.setSystem(true);
			selectionjob.setPriority(Job.SHORT);
			selectionjob.schedule();
		}

		/**
		 * Get the mcu id from the given structured selection.
		 * <p>
		 * If the first element of the selection is an AVR project, the mcu type
		 * is taken from the properties of the active build configuration.
		 * </p>
		 * <p>
		 * If the selection did not contain a valid mcu type <code>null</code>
		 * is returned
		 * 
		 * @param selection
		 *            <code>IStructuredSelection</code> from the Eclipse
		 *            Selection Services
		 * @return String with the mcu id or
		 *         <code>null</null> if no mcu id was found.
		 */
		private String getMCUId(IStructuredSelection selection) {

			Object item = selection.getFirstElement();
			if (item == null) {
				return null;
			}
			if (item instanceof IProject) {
				IProject project = (IProject) item;
				try {
					IProjectNature nature = project.getNature(PluginIDs.NATURE_ID);
					if (nature != null) {
						// This is an AVR Project
						// Get the selected build configuration then get the
						// PreferenceStore for it and read the MCU type.
						IManagedBuildInfo bi = ManagedBuildManager.getBuildInfo(project);
						IConfiguration cfg = bi.getDefaultConfiguration();

						IPreferenceStore propstore = AVRTargetProperties.getPropertyStore(cfg);
						return propstore.getString(AVRTargetProperties.KEY_MCUTYPE);

					}
				} catch (CoreException e) {
					return null;
				}
			} else if (item instanceof String) {
				String mcuname = (String) item;
				String mcuid = AVRMCUidConverter.name2id(mcuname);
				return mcuid;
			}

			// Selection does not contain a mcuid
			return null;
		}

	}

}