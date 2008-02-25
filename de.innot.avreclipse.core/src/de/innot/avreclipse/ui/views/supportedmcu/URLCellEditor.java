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
 * $Id$
 *     
 *******************************************************************************/
package de.innot.avreclipse.ui.views.supportedmcu;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

import de.innot.avreclipse.AVRPluginActivator;
import de.innot.avreclipse.util.URLDownloadException;
import de.innot.avreclipse.util.URLDownloadManager;

/**
 * A CellEditor for clickable URL links.
 * <p>
 * This Class is used to make cells in a TableViewer clickable to open the URL
 * provided by the model.
 * </p>
 * <p>
 * This is a hack class to get clickable Cells within a TableViewer, which
 * otherwise does not support any active cells.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class URLCellEditor extends CellEditor {

	private String fValue;

	private Text fURLText;

	final static Font fBoldFont = JFaceResources.getFontRegistry().getBold(
	        JFaceResources.DIALOG_FONT);

	final static Display fDisplay = PlatformUI.getWorkbench().getDisplay();

	public URLCellEditor(Composite parent) {
		super(parent, SWT.NONE);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createControl(Composite parent) {
		// Set up a text control that looks (almost) the same as
		// the Text from the TableItem underneath.
		fURLText = new Text(parent, SWT.NONE);

		// This Listener will get the MouseUp Event from the initial click onto
		// the cell. This is a little bit dodgy, as sometimes this event seems
		// go get eaten before it makes it to here.
		fURLText.addMouseListener(new MouseAdapter() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see org.eclipse.swt.events.MouseAdapter#mouseUp(org.eclipse.swt.events.MouseEvent)
			 */
			@Override
			public void mouseUp(MouseEvent e) {
				// Give the user a little visual indication that
				// his click has been registered and something will
				// be happening shortly.
				fURLText.setFont(fBoldFont);
				String urlstring = (String) fValue;
				openURL(urlstring);
			}
		});

		fURLText.setEditable(false);
		fURLText.setBackground(parent.getBackground());
		fURLText.setForeground(parent.getForeground());
		fURLText.setCursor(parent.getDisplay().getSystemCursor(SWT.CURSOR_HAND));

		return fURLText;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#doGetValue()
	 */
	@Override
	protected Object doGetValue() {
		// not needed, but required for the CellEditor API
		return fValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#doSetFocus()
	 */
	@Override
	protected void doSetFocus() {
		fURLText.setFocus();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor#doSetValue(java.lang.Object)
	 */
	@Override
	protected void doSetValue(Object value) {
		// The value comes from the IMCUProvider via the
		// URLEditingSupport#getValue() method and should be a URL
		// To keep the cell small, only the actual file name is shown, host and
		// path are discarded.
		// Note: this is only for the cell value when the cell is clicked. The
		// non-selected cell value is provided by the
		// URLColumnLabelProvider#getText() method.
		fValue = value.toString();
		fURLText.setText(getFilenameFromURL(fValue));
	}

	/**
	 * Load and Display the given URL.
	 * <p>
	 * The File from the URL is first downloaded via the
	 * {@link URLDownloadManager} and then opened using the default Editor
	 * registered for this filetype.
	 * </p>
	 * <p>
	 * The download and the opening of the file is done in a Job, so this method
	 * returns immediatly.
	 * </p>
	 * <p>
	 * If a download of the same URL is still in progress, this method does
	 * nothing to avoid multiple parallel downloads of the same file by nervous
	 * users. </p
	 * 
	 * @param urlstring
	 *            A String with an URL.
	 */
	private void openURL(final String urlstring) {
		try {
			if ("".equals(urlstring)) {
				// nothing to do for null or empty values
				return;
			}
			final URL url = new URL(urlstring);

			// Test if a download of this file is already in progress.
			// If yes: do nothing and return, assuming that the user has clicked
			// on the url twice accidentally
			if (URLDownloadManager.isDownloading(url)) {
				return;
			}

			// The actual download is done in this Job.
			// For any Exception during the download an ErrorDialog is displayed
			// with the cause(s)
			// The Job also returns an IStatus result, but by the time this is
			// returned, the openURL() method has long finished and there is
			// no one there to actually read this message :-)
			Job loadandopenJob = new Job("Download and Open") {
				@Override
				protected IStatus run(final IProgressMonitor monitor) {
					try {
						monitor.beginTask("Download " + url.toExternalForm(), 100);

						// Download the file and...
						final File file = URLDownloadManager.download(url, new SubProgressMonitor(
						        monitor, 95));

						// ...open the file in an editor.
						monitor.subTask("Opening Editor for " + file.getName());
						if (fDisplay == null || fDisplay.isDisposed()) {
							return new Status(Status.ERROR, AVRPluginActivator.PLUGIN_ID,
							        "Cannot open Editor: no Display found", null);
						}
						openFileInEditor(file);

						monitor.worked(5);
					} catch (URLDownloadException ude) {
						final URLDownloadException exc = ude;
						// ErrorDialog for all Exceptions, in an
						// Display.syncExec() to run in the UI Thread.
						fDisplay.syncExec(new Runnable() {
							public void run() {
								Shell shell = fDisplay.getActiveShell();
								String title = "Download Failed";
								String message = "The requested file could not be downloaded\nFile:  "
								        + url.getPath() + "\nHost:  " + url.getHost();
								String reason = exc.getMessage();
								MultiStatus status = new MultiStatus(AVRPluginActivator.PLUGIN_ID,
								        0, reason, null);
								Throwable cause = exc.getCause();
								// in case there are multiple root causes
								// (unlikely, but who knows?)
								while (cause != null) {
									status.add(new Status(Status.ERROR,
									        AVRPluginActivator.PLUGIN_ID, cause.getClass()
									                .getSimpleName(), cause));
									cause = cause.getCause();
								}

								ErrorDialog.openError(shell, title, message, status, Status.ERROR);
							}
						}); // fDisplay.asyncExec
					} finally {
						monitor.done();
					}
					return Status.OK_STATUS;
				} // run
			}; // new Job()

			// set some options and start the Job.
			loadandopenJob.setUser(true);
			loadandopenJob.setPriority(Job.LONG);
			loadandopenJob.schedule();

			return;
		} catch (MalformedURLException e) {
			Shell shell = fDisplay.getActiveShell();
			String title = "Invalid URL";
			String message = "The URL associated with the selected Item is not valid\nURL: "
			        + urlstring;
			IStatus status = new Status(Status.ERROR, AVRPluginActivator.PLUGIN_ID, e
			        .getLocalizedMessage(), e);
			ErrorDialog.openError(shell, title, message, status);
		}
	}

	/**
	 * Opens the given file with the standard editor.
	 * <p>
	 * An ErrorDialog is shown when the opening of the file fails.
	 * </p>
	 * 
	 * @param file
	 *            <code>java.io.File</code> with the file to open
	 * @return
	 */
	private IStatus openFileInEditor(final File file) {

		// Because this is called from a Job (which is not running in the UI
		// Thread, the opening is delegated to a Display.syncExec()
		fDisplay.syncExec(new Runnable() {
			public void run() {
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(new Path(file.toString()));
				if (!fileStore.fetchInfo().isDirectory() && fileStore.fetchInfo().exists()) {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					        .getActivePage();
					try {
						IDE.openEditorOnFileStore(page, fileStore);
					} catch (PartInitException e) {
						IStatus status = new Status(Status.ERROR, AVRPluginActivator.PLUGIN_ID,
						        "Could not open " + file.toString(), e);
						Shell shell = fDisplay.getActiveShell();
						String title = "Can't open File";
						String message = "The File " + file.toString() + " could not be opened";
						ErrorDialog.openError(shell, title, message, status);

					}
				}
			}
		});

		return Status.OK_STATUS;
	}

	/**
	 * Extract the actual filename from the URL, which is the part after the
	 * last '/' of the URL path section.
	 * 
	 * @param url
	 *            A String with the URL
	 * @return String
	 */
	private String getFilenameFromURL(String url) {
		return url.substring(url.lastIndexOf('/') + 1);
	}

}
