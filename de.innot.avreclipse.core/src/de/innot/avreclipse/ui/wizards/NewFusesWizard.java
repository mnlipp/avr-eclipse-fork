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
package de.innot.avreclipse.ui.wizards;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import de.innot.avreclipse.core.toolinfo.fuses.FileByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;

/**
 * This is a sample new wizard. Its role is to create a new file resource in the provided container.
 * If the container resource (a folder or a project) is selected in the workspace when the wizard is
 * opened, it will accept it as the target container. The wizard creates one file with the extension
 * "fuses". If a sample multi-fWizardPage editor (also available as a template) is registered for
 * the same extension, it will be able to open it.
 */

public class NewFusesWizard extends Wizard implements INewWizard {
	private FusesWizardPage	fWizardPage;
	private ISelection		fSelection;

	/**
	 * Constructor for NewFusesWizard.
	 */
	public NewFusesWizard() {
		super();
		setNeedsProgressMonitor(true);
	}

	/**
	 * Adding the fWizardPage to the wizard.
	 */
	@Override
	public void addPages() {
		fWizardPage = new FusesWizardPage(fSelection);
		addPage(fWizardPage);
	}

	/**
	 * This method is called when 'Finish' button is pressed in the wizard. We will create an
	 * operation and run it using the wizard as execution context.
	 */
	@Override
	public boolean performFinish() {

		final String containerName = fWizardPage.getContainerName();
		final String fileName = fWizardPage.getFileName();
		final String mcuid = fWizardPage.getMCUId();

		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, fileName, mcuid, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * The worker method. It will find the container, create the file if missing or just replace its
	 * contents, and open the editor on the newly created file.
	 */
	private void doFinish(String containerName, String fileName, String mcuid,
			IProgressMonitor monitor) throws CoreException {

		monitor.beginTask("Creating " + fileName, 2);

		// get the IContainer from the given containerName.
		// Throw an CoreException if the container does not exist
		// (Unlikely, because we checked this on the WizardPage already.

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;

		// Now get a handle for the new fuses file and write the default values for the given mcu to
		// the file.
		// If the file already exists we ask the user if it is OK to overwrite it.
		final IFile file = container.getFile(new Path(fileName));
		if (file.exists()) {
			// TODO: Ask the user if overwriting is OK
		}
		final FileByteValues newfile = FileByteValues.createNewFile(file.getLocation(), mcuid,
				FuseType.FUSE);
		newfile.save(new SubProgressMonitor(monitor, 1));

		// Now open the new file with the default editor
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {
				IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
						.getActivePage();
				try {
					IDE.openEditor(page, newfile.getSourceFile(), true);
				} catch (PartInitException e) {
					// TODO: Log error message
				}
			}
		});
		monitor.worked(1);
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status = new Status(IStatus.ERROR, "de.innot.avreclipse.fuseeditor", IStatus.OK,
				message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if we can initialize from it.
	 * 
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fSelection = selection;
	}
}