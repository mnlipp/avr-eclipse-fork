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

package de.innot.avreclipse.core.paths.posix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.ILock;
import org.eclipse.core.runtime.jobs.Job;

import de.innot.avreclipse.core.paths.AVRPath;

/**
 * Gets the actual system paths to the AVR-GCC Toolchain and some config files.
 * 
 * As these path can be almost everywhere (or not exist at all), this class
 * tries to get the location with the following methods:
 * <ol>
 * <li><code>which</code> command to look in the current $PATH</li>
 * <li><code>find</code> command to search certain parts of the filesystem.
 * Currently the following paths are checked (in this order)
 * <ul>
 * <li><code>/usr/local/</code></li>
 * <li><code>/usr/</code></li>
 * <li><code>/opt/</code></li>
 * <li><code>~/</code></li>
 * <li><code>/home/</code></li>
 * </ul>
 * </li>
 * </ol>
 * <p>
 * As the values are fairly static they are cached to avoid expensive searches. The cache is initialized on the first call to {@link #getDefault()}.
 * The cache can be cleared with the {@link #initCache()} method.
 * </p>
 * @author Thomas Holland
 * @since 2.1
 */
public class SystemPathsPosix {

	private static SystemPathsPosix fInstance = null;

	private static Map<AVRPath, IPath> fPathCache = null;

	private final static ILock fCacheInitLock = Job.getJobManager().newLock();;

	private final static IPath fEmptyPath = new Path("");

	private final static String[] fSearchPaths = { "/usr/local/", "/usr/", "/opt/", "~/", "/home/" };

	public static SystemPathsPosix getDefault() {
		if (fInstance == null) {
			fInstance = new SystemPathsPosix();
			fInstance.initCache();
		}
		return fInstance;
	}

	private SystemPathsPosix() {
	}

	public IPath getSystemPath(AVRPath pathcontext) {

		// Test if the cacheing is still in progress and
		// block until finished
		if (fPathCache == null) {
			// caching still in progress
			// block until it is finished, but max 10 seconds to
			// avoid deadlocks
			boolean lock;
            try {
	            lock = fCacheInitLock.acquire(10 * 1000);
            } catch (InterruptedException e) {
            	return fEmptyPath;
            }
			if (lock) {
				fCacheInitLock.release();
			} else {
				// timeout: return an empty path
				return fEmptyPath;
			}

		}
		// Test if file has already been cached
		IPath cachedpath = fPathCache.get(pathcontext);
		return cachedpath;
	}

	public void initCache() {
		// acquire a lock. The lock will be released when
		// the cache init job has finished.
		fCacheInitLock.acquire();

		Job job = new InitCacheJob("Find System Paths");
		job.schedule();
	}

	private class InitCacheJob extends Job {

		public InitCacheJob(String name) {
			super(name);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {

			AVRPath[] allpaths = AVRPath.values();

			Map<AVRPath, IPath> cachemap = new HashMap<AVRPath, IPath>(allpaths.length);

			SubMonitor progress = SubMonitor.convert(monitor, allpaths.length * 2);

			for (AVRPath avrpath : allpaths) {
				IPath path = fEmptyPath;
				if(!progress.isCanceled()) {
					String test = avrpath.getTest();
					path = which(test, progress.newChild(1));
					if (!path.isEmpty()) {
						// found a path. Keep it and skip the find test
						progress.worked(1);
					} else {
						path = find("*/" + test, progress.newChild(1));
						if (!path.isEmpty()) {
							// remove the number of segments of the test from the
							// path. This makes a test like "avr/io.h" work
							path = path.removeLastSegments(new Path(test).segmentCount());
						}
					}
				}
				// store the path (even when empty) in the cache
				cachemap.put(avrpath, path);
			}

			fPathCache = cachemap;
			fCacheInitLock.release();

			if (progress.isCanceled()) {
				return Status.CANCEL_STATUS;
			}
			return Status.OK_STATUS;
		}
	}

	private IPath which(String file, SubMonitor progress) {

		progress.beginTask("", 1);
		IPath path = executeCommand("which " + file);
		progress.worked(1);
		return path;
	}

	private IPath find(String file, SubMonitor progress) {

		progress.beginTask("", fSearchPaths.length);

		for (String findpath : fSearchPaths) {
			if (progress.isCanceled()) {
				return fEmptyPath;
			}
			IPath testpath = executeCommand("find.exe " + findpath + " -path \"" + file + "\"");
			if (!testpath.isEmpty()) {
				progress.setWorkRemaining(0);
				return testpath;
			}
			progress.worked(1);
		}

		// nothing found: return an empty path
		return new Path("");

	}

	public static IPath executeCommand(String command) {

		IPath path = fEmptyPath;

		Process cmdproc = null;
		InputStream is = null;

		try {
			cmdproc = ProcessFactory.getFactory().exec(command);
			is = cmdproc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			String line;

			while ((line = br.readLine()) != null) {
				if (line.length() > 1) {
					// non-empty line should have the path + file
					if (path.isValidPath(line)) {
						path = new Path(line);
						break;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
			try {
				if (cmdproc != null) {
					cmdproc.waitFor();
				}
			} catch (InterruptedException e) {
			}
		}

		return path;
	}

}
