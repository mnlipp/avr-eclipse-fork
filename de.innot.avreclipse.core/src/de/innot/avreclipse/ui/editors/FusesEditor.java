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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.part.FileEditorInput;

import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.FileByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;

/**
 * The FuseByte File Editor.
 * <p>
 * This editor has two pages
 * <ul>
 * <li>page 0 contains the form based editor</li>
 * <li>page 1 is a simple text editor to edit the raw file (not yet implemented)</li>
 * </ul>
 * </p>
 * 
 * @see LockbitsEditor
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class FusesEditor extends FormEditor implements IResourceChangeListener {

	private FuseByteEditorPage	fFuseEditorPage;

	private FileByteValues		fFileByteValues;

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
	 * Gets the type of fuse memory this Editor can edit.
	 * <p>
	 * Returns {@link FuseType#FUSE} for the <code>FusesEditor</code>. The {@link LockbitsEditor}
	 * class will override this method and return {@link FuseType#LOCKBITS}.
	 * </p>
	 * 
	 * @return The <code>FuseType</code> for this Editor.
	 */
	protected FuseType getType() {
		return FuseType.FUSE;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#addPages()
	 */
	@Override
	protected void addPages() {
		setPartName(getFileInput().getName());
		// setContentDescription("Edit Fuse Bytes");
		try {
			fFuseEditorPage = new FuseByteEditorPage(this, "editorFormPage", "BitFields");
			addPage(fFuseEditorPage);
			// TextEditor editor = new TextEditor();
			// addPage(editor, getEditorInput());
		} catch (PartInitException e) {
			// TODO: log the exception
		}
	}

	public IFile getFileInput() {
		IFile fileInput = (IFile) this.getEditorInput().getAdapter(IFile.class);
		if (fileInput == null) {
			throw new RuntimeException("Editor input is not file based: "
					+ this.getEditorInput().getName());
		}
		return fileInput;
	}

	public ByteValues getByteValuesFromInput() {
		if (fFileByteValues == null) {
			IFile file = getFileInput();
			try {
				fFileByteValues = new FileByteValues(file);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return fFileByteValues.getByteValues();

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.editor.FormEditor#dispose()
	 */
	@Override
	public void dispose() {
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
		fFuseEditorPage.doSave(monitor);

		IWorkspaceRunnable batchSave = new IWorkspaceRunnable() {

			public void run(IProgressMonitor monitor) throws CoreException {
				try {
					fFileByteValues.save(monitor);
					editorDirtyStateChanged();
				} catch (CoreException e) {
					// TODO: log exception
				}
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
		Shell shell = getSite().getShell();

		{
			SaveAsDialog dialog = new SaveAsDialog(shell);

			IFile original = fFileByteValues.getSourceFile();
			if (original != null)
				dialog.setOriginalFile(original);

			dialog.create();

			if (dialog.open() == Window.CANCEL) {
				return;
			}

			IPath filePath = dialog.getResult();
			if (filePath == null) {
				return;
			}

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IFile file = workspace.getRoot().getFile(filePath);
			try {
				fFileByteValues.saveAs(new NullProgressMonitor(), file);
				setPartName(file.getName());
				setInput(new FileEditorInput(file));
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	/*
	 * (non-Javadoc) Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
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

		try {
			fFileByteValues = new FileByteValues(file);
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

		// When a PRE_CLOSE event comes close all open editors (thereby asking the user if he wants
		// to save unsaved changes)
		if (event.getType() == IResourceChangeEvent.PRE_CLOSE) {
			Display.getDefault().asyncExec(new Runnable() {

				public void run() {
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i < pages.length; i++) {
						if (getFileInput().getProject().equals(event.getResource())) {
							IEditorPart editorPart = pages[i].findEditor(getEditorInput());
							pages[i].closeEditor(editorPart, true);
						}
					}
				}
			});
		}
	}

}
