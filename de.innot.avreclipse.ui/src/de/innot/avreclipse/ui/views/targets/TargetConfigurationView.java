/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
package de.innot.avreclipse.ui.views.targets;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.plugin.AbstractUIPlugin;

import de.innot.avreclipse.core.targets.ITargetConfigChangeListener;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.TargetConfigurationManager;
import de.innot.avreclipse.ui.AVRUIPlugin;
import de.innot.avreclipse.ui.editors.targets.TCEditorInput;

/**
 * This sample class demonstrates how to plug-in a new workbench view. The view shows data obtained
 * from the model. The sample creates a dummy model on the fly, but a real implementation would
 * connect to the model available either in this or another plug-in (e.g. the workspace). The view
 * is connected to the model using a content provider.
 * <p>
 * The view uses a label provider to define how model objects should be presented in the view. Each
 * view can present the same model objects using different labels and icons, if needed.
 * Alternatively, a single label provider can be shared between views in order to ensure that
 * objects of the same type are presented in the same way everywhere.
 * <p>
 */

public class TargetConfigurationView extends ViewPart implements ITargetConfigChangeListener {

	private TableViewer							viewer;
	private Action								actionAddNew;
	private Action								actionEdit;
	private Action								actionDelete;

	private TCViewerLabelProvider				fLabelProvider;

	private final static String					EDITOR_ID	= "de.innot.avreclipse.ui.editors.TargetConfigurationEditor";

	/**
	 * The target configuration manager. Used as a convenience replacement for
	 * {@link TargetConfigurationManager#getDefault()}
	 */
	private static TargetConfigurationManager	fTCManager	= null;

	/*
	 * The content provider class is responsible for providing objects to the view. It can wrap
	 * existing objects in adapters or simply return objects as-is. These objects may be sensitive
	 * to the current input of the view, or ignore it and always show the same content (like Task
	 * List, for example).
	 */

	class ViewContentProvider implements IStructuredContentProvider {

		/*
		 * (non-Javadoc)
		 * @see
		 * org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer,
		 * java.lang.Object, java.lang.Object)
		 */
		public void inputChanged(Viewer v, Object oldInput, Object newInput) {
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
		 */
		public void dispose() {
			// Remove the view as listener from all target configurations
			List<String> allIDs = fTCManager.getConfigurationIDs();
			for (String configid : allIDs) {
				try {
					ITargetConfiguration config = fTCManager.getConfig(configid);
					config.removePropertyChangeListener(TargetConfigurationView.this);
				} catch (IOException ioe) {
					// ignore exception, we are closing shop anyway.
					// This is unlikely to happen anyway
					continue;
				}
			}
		}

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
		 */
		public Object[] getElements(Object parent) {
			List<String> allIDs = fTCManager.getConfigurationIDs();
			ITargetConfiguration[] targets = new ITargetConfiguration[allIDs.size()];
			for (int i = 0; i < allIDs.size(); i++) {
				try {
					targets[i] = fTCManager.getConfig(allIDs.get(i));
					targets[i].addPropertyChangeListener(TargetConfigurationView.this);
				} catch (IOException ioe) {
					// ignore configs that can't be loaded
					continue;
				}
			}
			return targets;
		}
	}

	private static class NameSorter extends ViewerSorter {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.part.ViewPart#init(org.eclipse.ui.IViewSite)
	 */
	@Override
	public void init(IViewSite site) throws PartInitException {

		// Get the Target Configuration manager for easier access.
		if (fTCManager == null) {
			fTCManager = TargetConfigurationManager.getDefault();
		}
		super.init(site);
	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize it.
	 */
	public void createPartControl(Composite parent) {

		TableColumnLayout tcl = new TableColumnLayout();
		parent.setLayout(tcl);

		fLabelProvider = new TCViewerLabelProvider();

		viewer = new TableViewer(parent, SWT.FULL_SELECTION | SWT.SINGLE | SWT.H_SCROLL
				| SWT.V_SCROLL);
		viewer.setContentProvider(new ViewContentProvider());
		viewer.setLabelProvider(fLabelProvider);
		viewer.setSorter(new NameSorter());
		viewer.getTable().setHeaderVisible(true);
		viewer.getTable().setLinesVisible(true);

		// Create and initialize the columns
		fLabelProvider.initColumns(viewer, tcl);

		viewer.setInput(getViewSite());

		// Create the help context id for the viewer's control
		// PlatformUI.getWorkbench().getHelpSystem().setHelp(viewer.getControl(),
		// "test_View.viewer");
		makeActions();
		hookContextMenu();
		hookDoubleClickAction();
		contributeToActionBars();
	}

	private void hookContextMenu() {
		MenuManager menuMgr = new MenuManager("#PopupMenu");
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager) {
				TargetConfigurationView.this.fillContextMenu(manager);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		getSite().registerContextMenu(menuMgr, viewer);
	}

	private void contributeToActionBars() {
		IActionBars bars = getViewSite().getActionBars();
		fillLocalPullDown(bars.getMenuManager());
		fillLocalToolBar(bars.getToolBarManager());
	}

	private void fillLocalPullDown(IMenuManager manager) {
		manager.add(actionAddNew);
		manager.add(new Separator());
		manager.add(actionEdit);
		manager.add(actionDelete);
	}

	private void fillContextMenu(IMenuManager manager) {
		manager.add(actionAddNew);
		manager.add(actionEdit);
		manager.add(actionDelete);
		// Other plug-ins can contribute there actions here
		manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
	}

	private void fillLocalToolBar(IToolBarManager manager) {
		manager.add(actionAddNew);
		manager.add(actionEdit);
	}

	private void makeActions() {
		// //////////////////////////////////////////////
		// 
		// The Add New Action
		//
		// //////////////////////////////////////////////
		actionAddNew = new Action() {
			public void run() {
				try {
					ITargetConfiguration newtc = fTCManager.createNewConfig();
					viewer.refresh();
					newtc.addPropertyChangeListener(TargetConfigurationView.this);

					String tcid = newtc.getId();
					IEditorInput ei = new TCEditorInput(tcid);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage();
					page.openEditor(ei, EDITOR_ID);
				} catch (PartInitException pie) {
					showErrorDialog("Could not open Hardware Configuration editor", pie);
				} catch (IOException ioe) {
					showErrorDialog("Could not create new Hardware Configuration", ioe);
				}

			}
		};
		actionAddNew.setText("Add new");
		actionAddNew.setToolTipText("Add a new target configuration");
		ImageDescriptor addID = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui",
				"$nl$/icons/full/obj16/add_obj.gif");
		actionAddNew.setImageDescriptor(addID);

		// //////////////////////////////////////////////
		// 
		// The Delete Action
		//
		// //////////////////////////////////////////////
		actionDelete = new Action() {
			public void run() {

				ITargetConfiguration tc = getTargetConfigurationFromSelection();
				if (tc != null) {
					// First close the editor (if it is open)
					String tcid = tc.getId();
					IEditorInput ei = new TCEditorInput(tcid);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage();
					IEditorPart part = page.findEditor(ei);
					page.closeEditor(part, false);

					// now remove the target configuration. This will automatically delete the
					// listener reference to this view.
					try {
						fTCManager.deleteConfig(tcid);
					} catch (IOException ioe) {
						showErrorDialog("Could not delete Hardware Configuration", ioe);
					}

					// and finally update the table
					viewer.refresh();
				}

			}
		};

		actionDelete.setText("Delete");
		actionDelete.setToolTipText("Delete selected target configuration");
		ImageDescriptor deleteID = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui",
				"$nl$/icons/full/obj16/delete_obj.gif");
		actionDelete.setImageDescriptor(deleteID);

		// //////////////////////////////////////////////
		// 
		// The Edit Action
		//
		// //////////////////////////////////////////////
		actionEdit = new Action() {
			public void run() {
				ITargetConfiguration tc = getTargetConfigurationFromSelection();
				if (tc != null) {
					String tcid = tc.getId();
					IEditorInput ei = new TCEditorInput(tcid);
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.getActivePage();
					try {
						page.openEditor(ei, EDITOR_ID);
					} catch (PartInitException e) {
						// what can cause this?
						e.printStackTrace();
					}
				}
			}
		};
		actionEdit.setText("Edit");
		actionEdit.setToolTipText("Edit target configruation");
		ImageDescriptor editID = AbstractUIPlugin.imageDescriptorFromPlugin("org.eclipse.ui",
				"$nl$/icons/full/etool16/editor_area.gif");
		actionEdit.setImageDescriptor(editID);
	}

	private void hookDoubleClickAction() {
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			public void doubleClick(DoubleClickEvent event) {
				actionEdit.run();
			}
		});
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		viewer.getControl().setFocus();
	}

	private ITargetConfiguration getTargetConfigurationFromSelection() {
		ISelection selection = viewer.getSelection();
		Object obj = ((IStructuredSelection) selection).getFirstElement();
		if (obj instanceof ITargetConfiguration) {
			ITargetConfiguration tc = (ITargetConfiguration) obj;
			return tc;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.TargetConfiguration.ITargetConfigChangeListener#attributeChange
	 * (de.innot.avreclipse.core.targets.ITargetConfiguration, java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	public void attributeChange(ITargetConfiguration config, String attribute, String oldvalue,
			String newvalue) {

		String[] props = new String[1];
		props[0] = attribute;
		viewer.update(config, props);
	}

	/**
	 * Show a standard error message dialog with the given message and exception.
	 * 
	 * @param message
	 * @param exception
	 *            may be <code>null</code>
	 */
	private void showErrorDialog(String message, Throwable exception) {
		IStatus status = new Status(IStatus.ERROR, AVRUIPlugin.PLUGIN_ID, message, exception);
		ErrorDialog dialog = new ErrorDialog(viewer.getControl().getShell(),
				"Hardware Config Error", null, status, IStatus.ERROR);
		dialog.open();
	}
}