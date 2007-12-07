/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 * $Id: AVRDeviceView.java 21 2007-11-28 00:52:07Z thomas $
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui.views.avrdevice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.ViewPart;

import de.innot.avreclipse.devicedescription.ICategory;
import de.innot.avreclipse.devicedescription.IDeviceDescription;
import de.innot.avreclipse.devicedescription.IDeviceDescriptionProvider;
import de.innot.avreclipse.devicedescription.avrio.DeviceDescriptionProvider;

/**
 */

public class AVRDeviceView extends ViewPart {

	private Composite fTop;
	private ComboViewer fCombo;

	private Composite fSourcesComposite;
	private List<Label> fSourcesLabels = new ArrayList<Label>(0);
	private List<Text> fSourcesTexts = new ArrayList<Text>(0);

	private CTabFolder fTabFolder;
	private List<CTabItem> fTabs = new ArrayList<CTabItem>(0);
	private Map<String, List<TreeColumn>> fTreeColumns;

	private IMemento fMemento;

	private IDeviceDescriptionProvider dmprovider = DeviceDescriptionProvider.getDefault();

	/**
	 * The constructor.
	 */
	public AVRDeviceView() {
	}

	/*
	 * (non-Javadoc) Method declared on IViewPart.
	 */
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException {
		super.init(site, memento);
		fMemento = memento;
	}

	@Override
	public void saveState(IMemento memento) {
		// Save the current state of the viewer
		super.saveState(memento);

		// Save the currently selected device
		IStructuredSelection selection = (IStructuredSelection) fCombo.getSelection();
		memento.putString("combovalue", selection.getFirstElement().toString());

		// TODO: Save the Column Layoout for each category

	}

	/**
	 * Create, layout and initialize the controls of this viewer
	 */
	@Override
	public void createPartControl(Composite parent) {

		parent.setLayout(new GridLayout());

		// TODO: TreeColumn Map is dependent on devicemodelprovider
		fTreeColumns = new HashMap<String, List<TreeColumn>>();

		// Layout the top part, which consists of the MCU Type selection Combo
		// and a composite of the sources Labels and Texts
		// The Combo gets 33% of the space, the sources composite the other 67%
		GridLayout gl = new GridLayout(3, true);
		gl.marginHeight = 0;
		gl.marginWidth = 0;

		fTop = new Composite(parent, SWT.NONE);
		fTop.setLayout(gl);
		fTop.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));

		fCombo = new ComboViewer(fTop, SWT.READ_ONLY | SWT.DROP_DOWN);
		fCombo.getControl().setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		fCombo.setContentProvider(new DeviceListContentProvider());
		fCombo.setLabelProvider(new ComboLabelProvider());
		fCombo.addSelectionChangedListener(new ComboSelectionChangedListener());

		// pass the current DeviceDescriptionProvider to the
		// DeviceListContentProvider
		fCombo.setInput(dmprovider);

		fSourcesComposite = new Composite(fTop, SWT.NONE);
		fSourcesComposite.setLayout(new RowLayout());
		fSourcesComposite.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false, 2, 1));
		new Label(fSourcesComposite, SWT.NONE).setText("source:");

		// The bottom part consists of a TabFolder Control which will take all
		// space
		// not used by the top part.
		fTabFolder = new CTabFolder(parent, SWT.BORDER);
		fTabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fTabFolder.addSelectionListener(new TabFolderSelectionListener());

		String show = null;
		if (fMemento != null)
			show = fMemento.getString("combovalue");
		if (show == null) {
			show = (String) fCombo.getElementAt(0);
		}
		// This next step will cause a SelectionChangeEvent which in turn will
		// load the sources, tabs and treeviewers
		fCombo.setSelection(new StructuredSelection(show), true);

	}

	/**
	 * Passing the focus request to the treeviewer of the selected tab
	 */
	@Override
	public void setFocus() {
		TreeViewer tv = (TreeViewer) fTabFolder.getSelection().getData();
		tv.getControl().setFocus();
	}

	/**
	 * Update the controls which make up the sources composite.
	 * <p>
	 * The sources are shown in a "breadcrumb" fashion, with a ">>" between the
	 * names of the source files.
	 * </p>
	 * <p>
	 * This will handle arbitrary numbers of sources. While this is not really
	 * required today with maximum two sources, it is ready in case the
	 * structure of the include files is changed.</p
	 */
	private void updateSourcelist(Composite parent, IDeviceDescription device) {

		Text txt = null;
		Label lbl = null;
		List<String> sources = device.getSourcesList();

		// Strategy: iterate over all elements of the sources list, and try to
		// get a Text control from the existing text controls. If this fails
		// with an Exception a new Text control is created and added to the
		// internal list fSourcesTexts. After each Text control a Label with
		// ">>" is added, but the last of these labels is hidden again.
		//
		// After the list has been iterated, any Text and Label Controls that
		// are left over in the list will be disposed.
		int txtcounter = 0;
		for (String path : sources) {
			try {
				txt = fSourcesTexts.get(txtcounter);
				lbl = fSourcesLabels.get(txtcounter);
			} catch (IndexOutOfBoundsException ioobe) {
				txt = new Text(parent, SWT.NONE);
				txt.setEditable(false);
				// make it look like a link (grey background, dark blue color
				txt.setBackground(parent.getBackground());
				txt.setForeground(parent.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE));
				txt.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_ARROW));
				Listener srclistener = new SourceSelectionMouseHandler();
				txt.addListener(SWT.MouseEnter, srclistener);
				txt.addListener(SWT.MouseExit, srclistener);
				txt.addListener(SWT.MouseUp, srclistener);
				fSourcesTexts.add(txt);

				// create a ">>" label
				lbl = new Label(parent, SWT.NONE);
				lbl.setText(">>");
				fSourcesLabels.add(lbl);
			}
			txt.setText(path);
			lbl.setVisible(true);
			txtcounter++;
		}

		// hide the last label
		fSourcesLabels.get(txtcounter - 1).setVisible(false);

		// dispose and remove any remaining Texts / Labels
		// The loop will end with an Exception once the last list element has
		// been removed and we try to read the same index again.
		try {
			while (true) {
				txt = fSourcesTexts.get(txtcounter);
				txt.dispose();
				fSourcesTexts.remove(txtcounter); // all remaining items
				// are moved up

				lbl = fSourcesLabels.get(txtcounter);
				lbl.dispose();
				fSourcesLabels.remove(txtcounter);
			}
		} catch (IndexOutOfBoundsException ioobe) {
			// do nothing, as this exception is expected
		}

		// Redraw the parent
		parent.pack(true);
	}

	/**
	 * Update all tabs.
	 * <p>
	 * One tab for each Category of the device is created. Each tab also gets a
	 * <code>TreeViewer</code> associated to it, accessible via the
	 * <code>CTabItem.getData()</code> method.
	 * </p>
	 * <p>
	 * If a new tab has the same name as the previously selected tab, it will be
	 * selected as well
	 * </p>
	 */
	private void updateTabs(CTabFolder parent, IDeviceDescription device) {

		TreeViewer tv = null;
		CTabItem cti = null;
		String activetabname = null;

		// Remember the name of the active tab
		if (parent.getSelection() != null) {
			activetabname = parent.getSelection().getText();
		}

		List<ICategory> categories = device.getCategories();

		// Strategy: iterate over all elements of the category list, and try to
		// get a CTabItem control from the existing CTabItems. If this fails
		// with an Exception a new CTabItem control is created and added to the
		// internal list fTabs. Also a TreeViewer is created and linked to the
		// tab.
		// After the list has been iterated, any CTabItems that
		// are left over in the list will be disposed.
		int cticounter = 0;
		for (ICategory cat : categories) {
			try {
				cti = fTabs.get(cticounter);
			} catch (IndexOutOfBoundsException ioobe) {
				// Tab did not exist: create a new CTabItem and an associated
				// TreeViewer
				cti = new CTabItem(parent, SWT.NONE);
				tv = createTreeView(parent, cat);
				cti.setData(tv);
				cti.setControl(tv.getControl());
				fTabs.add(cti);
			}
			cti.setText(cat.getName());
			tv = (TreeViewer) cti.getData();
			updateTreeView(tv, cat);
			tv.setInput(cat);

			// Check if this tab should be made active
			if (cat.getName().equals(activetabname)) {
				parent.setSelection(cti);
			}
			cticounter++;
		}

		// dispose and remove any remaining CTabItems and associated TreeViewers
		// The loop will end with an Exception once the last list element has
		// been removed and we try to read the same index again.
		try {
			while (true) {
				cti = fTabs.get(cticounter);
				cti.getControl().dispose();
				cti.dispose();
				fTabs.remove(cticounter); // all remaining items are moved up
			}
		} catch (IndexOutOfBoundsException ioobe) {
			// do nothing, as this exception is expected
		}

		// If no Tab is active then activate the first one
		if (parent.getSelectionIndex() == -1) {
			parent.setSelection(0);
		}

	}

	/**
	 * Create a new TreeViewer for the given Category.
	 * 
	 * It is up to the caller to dispose the <code>TreeViewer</code> when it
	 * is no longer needed.
	 * 
	 * @return A newly created TreeViewer.
	 */
	private TreeViewer createTreeView(Composite parent, ICategory category) {

		TreeViewer tv = new TreeViewer(parent);
		tv.setContentProvider(new DeviceModelContentProvider());
		tv.setLabelProvider(new EntryLabelProvider());
		tv.getTree().setLayoutData(new GridData(GridData.FILL_BOTH));
		tv.getTree().setHeaderVisible(true);
		tv.getTree().setLinesVisible(true);

		// TODO: add an Action on the header for sorting

		tv.setInput(category);

		return tv;
	}

	/**
	 * Updates a TreeViewer to a new Category.
	 * <p>
	 * If the Category with this name does not yet exist the required
	 * <code>TreeColumns</code> will be created and initialized to their
	 * default values. If a Category with this name has already existed (even
	 * from a previous device>, then the <code>TreeColumns</code> will be
	 * reused. Therefore any changes by the user are preserved.
	 * </p>
	 * 
	 * @param parent
	 * @param cat
	 *            The Category which the parent will use as Content
	 */
	private void updateTreeView(TreeViewer parent, ICategory cat) {

		List<TreeColumn> columnlist;
		String catname = cat.getName();

		// have a somewhat reasonable layout regardless of font size.
		// Yes, I know that height is not the same as width, but with
		// proportional fonts its the next best thing.
		FontData curfd[] = parent.getTree().getFont().getFontData();
		int approxfontwidth = curfd[0].getHeight();

		// See if a column layout for this Category already exists
		columnlist = fTreeColumns.get(catname);
		if (columnlist == null) {
			// No: create the columns for this category name
			columnlist = new ArrayList<TreeColumn>(cat.getColumnCount());

			// TODO: pass more information from the Category, like alignment,
			// icon etc.
			String[] labels = cat.getColumnLabels();
			int[] widths = cat.getColumnDefaultWidths();
			for (int i = 0; i < labels.length; i++) {
				TreeColumn tc = new TreeColumn(parent.getTree(), SWT.LEFT);
				tc.setWidth(widths[i] * approxfontwidth);
				tc.setResizable(true);
				tc.setMoveable(true);
				tc.setText(labels[i]);
				columnlist.add(tc);
			}
			fTreeColumns.put(catname, columnlist);
		} else {
			// Yes:
			// TODO: restore the TreeColumns Layout from the memento
		}
	}

	@SuppressWarnings("unused")
	private void showMessage(String message) {
		MessageDialog.openInformation(fCombo.getControl().getShell(), "Register Explorer", message);
	}

	// When a different MCU Type is selected do the following:
	// - get the new device from the provider
	// - update the sources text elements
	// - Update the tabs of the TabFolder (which will update the treeviewers)
	// - Set the focus to the TreeViewer of the active tab.
	private class ComboSelectionChangedListener implements ISelectionChangedListener {

		public void selectionChanged(SelectionChangedEvent event) {
			String devicename = (String) ((StructuredSelection) event.getSelection())
			        .getFirstElement();
			IDeviceDescription device = dmprovider.getDevice(devicename);
			updateSourcelist(fSourcesComposite, device);
			updateTabs(fTabFolder, device);
			setFocus();
		}
	}

	// When a different tab is selected do the following:
	// - refresh the associated TreeViewer
	// - set the focus to this tabs TreeViewer
	private class TabFolderSelectionListener implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
			// not called for a CTabFolder
		}

		public void widgetSelected(SelectionEvent e) {
			System.out.println(e);
			CTabItem ti = (CTabItem) e.item;
			((TreeViewer) ti.getData()).refresh();
			((TreeViewer) ti.getData()).getControl().setFocus();
		}
	}

	/**
	 * Handle Mouse Events for the source file text widgets.
	 * 
	 * Three events are handled:
	 * <ul>
	 * <li><code>MouseEnter</code>: Change background color to show the user
	 * that this widget can be clicked on.</li>
	 * <li><code>MouseExit</code>: Restore the background color.</li>
	 * <li><code>MouseUp</code>: Open the sourcefile associated with the
	 * selected widget in an Editor.</li>
	 * </ul>
	 */
	private class SourceSelectionMouseHandler implements Listener {

		public void handleEvent(Event event) {
			Text txt = (Text) event.widget;
			switch (event.type) {
			case SWT.MouseEnter:
				// change background color to some lighter color
				txt.setBackground(txt.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				break;
			case SWT.MouseExit:
				// change background color back to normal
				txt.setBackground(txt.getParent().getBackground());
				break;
			case SWT.MouseUp:
				// Open the selected source file
				// get the basepath from the DeviceDescriptionProvider,
				// append the content of the selected text widget, get a
				// IFileStore for this file and open an Editor for this
				// file in the active Workbench page.
				// No error checking as this file should exist
				IPath srcfile = dmprovider.getBasePath();
				srcfile = srcfile.append(txt.getText());
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(srcfile);
				if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					        .getActivePage();
					try {
						@SuppressWarnings("unused")
						IEditorPart editor = IDE.openEditorOnFileStore(page, fileStore);
						// TODO: if any row in the treeview has been selected,
						// try
						// to scroll the Editor to the associated definition.
					} catch (PartInitException e) {
						// what can cause this?
						e.printStackTrace();
					}
				}
				break;
			} // switch
		} // handleevent
	}
}
