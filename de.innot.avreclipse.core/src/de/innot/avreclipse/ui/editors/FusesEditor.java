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
package de.innot.avreclipse.ui.editors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IElementStateListener;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;

/**
 * The FuseByte File Editor.
 * <p>
 * This editor has two pages
 * <ul>
 * <li>page 0 contains the form based editor</li>
 * <li>page 1 is a simple text editor to edit the raw file</li>
 * </ul>
 * </p>
 * 
 * @see LockbitsEditor
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class FusesEditor extends FormEditor implements IResourceChangeListener,
		IElementStateListener {

	private ByteValuesFormEditor		fFuseEditor;

	private ByteValuesSourceEditor		fSourceEditor;

	private ByteValues					fByteValues;

	private IFile						fSourceFile;

	private FuseFileDocumentProvider	fDocumentProvider;

	/**
	 * Creates a fuse bytes editor.
	 * <p>
	 * Registers this editor as an <code>ResourceChangeListener</code> to be informed about
	 * changes of the file edited and about workbench closure.
	 * </p>
	 */
	public FusesEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}

	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method checks that the input
	 * is an instance of <code>IFileEditorInput</code>.
	 */
	@Override
	public void init(IEditorSite site, IEditorInput editorInput) throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);

		IFile file = (IFile) editorInput.getAdapter(IFile.class);
		if (file == null) {
			throw new PartInitException("Invalid Input: Must be a IFile");
		}

		if (!file.exists()) {
			throw new PartInitException("Invalid Input: File does not exist.");
		}

		try {

			fSourceFile = file;
			fDocumentProvider = FuseFileDocumentProvider.getDefault();
			fDocumentProvider.addElementStateListener(this);
			fDocumentProvider.connect(getEditorInput());
			fByteValues = fDocumentProvider.getByteValues(getEditorInput());
		} catch (CoreException ce) {
			// Should not happen if the file exists, but log it anyway:
			IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID, "Could not open file ["
					+ file.getFullPath() + "]", ce);
			AVRPlugin.getDefault().log(status);
			throw new PartInitException("Invalid Input: Could not open file");
		}

	}

	/**
	 * Gets the type of fuse memory this Editor can edit.
	 * <p>
	 * Returns {@link FuseType#FUSE} for the <code>FusesEditor</code>. The {@link LockbitsEditor}
	 * class will override this method and return {@link FuseType#LOCKBITS}.
	 * </p>
	 * 
	 * @return The <code>FuseType</code> for this Editor.
	 */
	// protected FuseType getType() {
	// return FuseType.FUSE;
	// }
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		setPartName(getSourceFile().getName());
		// setContentDescription("Edit Fuse Bytes");
		try {
			fFuseEditor = new ByteValuesFormEditor(this, "editorFormPage", "BitFields");
			addPage(fFuseEditor);
			fSourceEditor = new ByteValuesSourceEditor(this, "sourceEditorPage", "Source");
			addPage(fSourceEditor, getEditorInput());
		} catch (PartInitException e) {
			// TODO: log the exception
		}
	}

	public IFile getSourceFile() {
		return fSourceFile;
	}

	public ByteValues getByteValues() {
		return fByteValues;
	}

	public String getSourceFilename() {
		return getSourceFile().getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#dispose()
	 */
	@Override
	public void dispose() {
		fDocumentProvider.disconnect(getEditorInput());
		fDocumentProvider.removeElementStateListener(this);
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSave(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void doSave(IProgressMonitor monitor) {

		IWorkspaceRunnable batchSave = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				// Commit the pages
				fFuseEditor.doSave(monitor);
				fSourceEditor.doSave(monitor);
				editorDirtyStateChanged();
			}
		};
		try {
			ResourcesPlugin.getWorkspace().run(batchSave, null, IWorkspace.AVOID_UPDATE, monitor);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.EditorPart#doSaveAs()
	 */
	@Override
	public void doSaveAs() {
		{
			IEditorInput oldinput = getEditorInput();
			fSourceEditor.doSaveAs();
			IEditorInput newinput = fSourceEditor.getEditorInput();
			if (!newinput.equals(oldinput)) {
				try {
					fDocumentProvider.disconnect(oldinput);
					fDocumentProvider.connect(newinput);
					fByteValues = fDocumentProvider.getByteValues(newinput);
					fFuseEditor.selectReveal(fByteValues);
				} catch (CoreException ce) {
				}
			}
			setInput(newinput);
			editorDirtyStateChanged();
			IFileEditorInput newfileinput = (IFileEditorInput) newinput;
			setPartName(newfileinput.getName());
		}

	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		// Change to the source editor and goto to the marker
		setActivePage(1);
		IDE.gotoMarker(getEditor(1), marker);
	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart.
	 */
	@Override
	public boolean isSaveAsAllowed() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#pageChange(int)
	 */
	@Override
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.resources.IResourceChangeListener#resourceChanged(org.eclipse.core.resources.IResourceChangeEvent)
	 */
	public void resourceChanged(final IResourceChangeEvent event) {

		switch (event.getType()) {
			case IResourceChangeEvent.PRE_CLOSE:
				handleCloseEvent(event);
				break;
		}
	}

	private void handleCloseEvent(final IResourceChangeEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
				for (int i = 0; i < pages.length; i++) {
					if (getSourceFile().getProject().equals(event.getResource())) {
						IEditorPart editorPart = pages[i].findEditor(getEditorInput());
						pages[i].closeEditor(editorPart, true);
					}
				}
			}
		});
	}

	// ---- DocumentProvider Element Change Listener Methods ------

	public void elementContentAboutToBeReplaced(Object element) {
		// TODO Auto-generated method stub

	}

	public void elementContentReplaced(Object element) {
		// TODO Auto-generated method stub

	}

	public void elementDeleted(Object element) {
		if (element != null && element.equals(getEditorInput())) {
			close(true);
		}
	}

	public void elementDirtyStateChanged(Object element, boolean isDirty) {
		// TODO Auto-generated method stub

	}

	public void elementMoved(Object originalElement, Object movedElement) {
		// TODO Auto-generated method stub

	}

}
