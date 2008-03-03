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
package de.innot.avreclipse.core.toolinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;

import de.innot.avreclipse.AVRPlugin;
import de.innot.avreclipse.core.toolinfo.ICommandOutputListener.StreamSource;

/**
 * Launch external programs.
 * <p>
 * This is a wrapper around the <code>java.lang.ProcessBuilder</code> to
 * launch external programs and fetch their results.
 * </p>
 * <p>
 * The results of the program run are stored in two
 * <code>List&lt;String&gt;</code> arrays, one for the stdout and one for
 * stderr. Receivers can also register a {@link ICommandOutputListener} to get
 * the output line by line while it is generated, for example to update the user
 * interface.
 * </p>
 * <p>
 * Optionally an <code>IProgressMonitor</code> can be passed to the launch
 * method to cancel running commands
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class ExternalCommandLauncher {

	/** Lock for internal synchronization */
	private Object fRunLock;

	private ProcessBuilder fProcessBuilder;

	private List<String> fStdOut;
	private List<String> fStdErr;

	/** The listener to be informed about each new line of output */
	private ICommandOutputListener fLogEventListener = null;

	/**
	 * A runnable class that will read a Stream until EOF, storing each line in
	 * a List and also calling a listener for each line.
	 */
	private class LogStreamRunner implements Runnable {

		private BufferedReader fReader;
		private List<String> fLog;
		private StreamSource fSource;

		/**
		 * Construct a Streamrunner that will read the given InputStream and log
		 * all lines in the given List.
		 * 
		 * @param instream
		 *            <code>InputStream</code> to read
		 * @param log
		 *            <code>List&lt;String&gt;</code> where all lines of the
		 *            instream are stored
		 */
		public LogStreamRunner(InputStream instream, StreamSource source, List<String> log) {
			fReader = new BufferedReader(new InputStreamReader(instream));
			fSource = source;
			fLog = log;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			try {
				for (;;) {
					// Processes a new process output line.
					// If a Listener has been registered, call it
					String line = fReader.readLine();
					if (line != null) {
						synchronized (ExternalCommandLauncher.this) {
							if (fLogEventListener != null) {
								fLogEventListener.handleLine(line, fSource);
							}
						}
						// Add the line to the total output
						fLog.add(line);
					} else {
						break;
					}
				}
			} catch (IOException e) {
				// This is unlikely to happen, but log it nevertheless
				IStatus status = new Status(Status.ERROR, AVRPlugin.PLUGIN_ID,
				        "I/O Error reading output", e);
				AVRPlugin.getDefault().log(status);
			} finally {
				try {
					fReader.close();
				} catch (IOException e) {
					// can't do anything
				}
			}
			synchronized (fRunLock) {
				// Notify the caller that this thread is finished
				fRunLock.notifyAll();
			}
		}
	}

	/**
	 * Creates a new ExternalCommandLauncher for the given command and a list of
	 * arguments.
	 * 
	 * @param command
	 *            <code>String</code> with the command
	 * @param arguments
	 *            <code>List&lt;String&gt;</code> with all arguments or
	 *            <code>null</code> if no arguments
	 */
	public ExternalCommandLauncher(String command, List<String> arguments) {
		Assert.isNotNull(command);
		fRunLock = this;
		// make a new list suitable for ProcessBuilder, where
		// the command is the first entry and all other
		// arguments follow
		List<String> commandlist = new ArrayList<String>();
		commandlist.add(command);
		if (arguments != null) {
			commandlist.addAll(arguments);
		}
		fProcessBuilder = new ProcessBuilder(commandlist);
	}

	/**
	 * Launch the external program.
	 * <p>
	 * This method blocks until the external program has finished.
	 * <p>
	 * The output from <code>stdout</code> can be retrieved with
	 * {@link #getStdOut()}, the output from <code>stderr</code> likewise
	 * with {@link #getStdErr()}.
	 * </p>
	 * 
	 * @see java.lang.Process
	 * @see java.lang.ProcessBuilder#start()
	 * 
	 * @return Result code of the external program. Usually <code>0</code>
	 *         means successful.
	 * @throws IOException
	 *             An Exception from the underlying Process.
	 */
	public int launch() throws IOException {
		return launch(new NullProgressMonitor());
	}

	/**
	 * Launch the external program with a ProgressMonitor.
	 * <p>
	 * This method blocks until the external program has finished or the
	 * ProgressMonitor is canceled.
	 * <p>
	 * The output from <code>stdout</code> can be retrieved with
	 * {@link #getStdOut()}, the output from <code>stderr</code> likewise
	 * with {@link #getStdErr()}.
	 * </p>
	 * 
	 * @see java.lang.Process
	 * @see java.lang.ProcessBuilder#start()
	 * 
	 * @param monitor
	 *            A <code>IProgressMonitor</code> to cancel the running
	 *            external program
	 * @return Result code of the external program. Usually <code>0</code>
	 *         means successful. A canceled program will return <code>-1</code>
	 * @throws IOException
	 *             An Exception from the underlying Process.
	 */
	public int launch(IProgressMonitor monitor) throws IOException {
		Process process = null;
		try {
			monitor.beginTask("Launching " + fProcessBuilder.command().get(0), 100);

			fStdOut = new ArrayList<String>();
			fStdErr = new ArrayList<String>();

			process = fProcessBuilder.start();

			Thread stdoutRunner = new Thread(new LogStreamRunner(process.getInputStream(),
			        StreamSource.STDOUT, fStdOut));
			Thread stderrRunner = new Thread(new LogStreamRunner(process.getErrorStream(),
			        StreamSource.STDERR, fStdErr));

			synchronized (fRunLock) {
				// Wait either for the logrunners to terminate or the user to
				// cancel the job.
				// The monitor is polled 10 times / sec.
				stdoutRunner.start();
				stderrRunner.start();

				monitor.worked(5);

				while (stdoutRunner.isAlive() || stderrRunner.isAlive()) {
					fRunLock.wait(100);
					if (monitor.isCanceled() == true) {
						process.destroy();
						process.waitFor();
						return -1;
					}
				}
			}
			monitor.worked(95);
		} catch (InterruptedException e) {
			// This thread was interrupted from outside
			// consider this to be a failure of the external programm
			return -1;
		} finally {
			monitor.done();
		}
		// if we make it to here, the process has run without any Exceptions
		return process.exitValue();
	}

	/**
	 * Returns the <code>stdout</code> output from the last external Program
	 * launch.
	 * 
	 * @return <code>List&lt;String&gt;</code> with all lines or
	 *         <code>null</code> if the external program has never been
	 *         launched
	 */
	public List<String> getStdOut() {
		return fStdOut;
	}

	/**
	 * Returns the <code>stderr</code> output from the last external Program
	 * launch.
	 * 
	 * @return <code>List&lt;String&gt;</code> with all lines or
	 *         <code>null</code> if the external program has never been
	 *         launched
	 */
	public List<String> getStdErr() {
		return fStdErr;
	}

	/**
	 * Sets a listener that will receive all lines from the external program
	 * output as they are read.
	 * <p>
	 * The listener can be used to update the user interface according to the
	 * current output.
	 * </p>
	 * 
	 * @param listener
	 *            Object implementing the {@link ICommandOutputListener}
	 *            Interface.
	 */
	public synchronized void setCommandOutputListener(ICommandOutputListener listener) {
		Assert.isNotNull(listener);
		fLogEventListener = listener;
	}

	/**
	 * Removes the current command output listener.
	 * <p>
	 * While it is safe to call this while an external program is running, it is
	 * probably better to just ignore superfluous output.
	 * </p>
	 */
	public synchronized void removeCommandOutputListener() {
		fLogEventListener = null;
	}

	/**
	 * Redirects the <code>stderr</code> output to <code>stdout</code>.
	 * <p>
	 * Use this either when not sure which stream an external program writes
	 * its output to (some programs, like avr-size.exe write their help output
	 * to stderr), or when you like any error messages inserted into the normal output
	 * stream for analysis
	 * </p>
	 * <p>
	 * Note: The redirection takes place at system level, so a command output
	 * listener will only receive the mixed output.
	 * </p>
	 * 
	 * @see ProcessBuilder#redirectErrorStream(boolean)
	 * 
	 * @param redirect
	 *            <code>true</code> to redirect <code>stderr</code> to
	 *            <code>stdout</code>
	 */
	public void redirectErrorStream(boolean redirect) {
		fProcessBuilder.redirectErrorStream(redirect);
	}
}
