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
import org.eclipse.core.runtime.Path;
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
 * <li><code>/etc/</code></li>
 * <li><code>~/</code></li>
 * <li><code>/home/</code></li>
 * </ul>
 * </li>
 * </ol>
 * <p>
 * As the values are fairly static they are cached to avoid expensive searches.
 * The cache can be cleared with the {@link #clearCache()} method.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.1
 */
public class SystemPathsPosix {

	private static SystemPathsPosix fInstance = null;
	
    private static ILock lock = Job.getJobManager().newLock();

	private Map<AVRPath, IPath> fPathCache = null;

	private final static IPath fEmptyPath = new Path("");

	private final static String[] fSearchPaths = { "/usr/local/", "/usr/",
			"/opt/", "/etc/", "~/", "/home/" };

	public static SystemPathsPosix getDefault() {
		if (fInstance == null) {
			fInstance = new SystemPathsPosix();
		}
		return fInstance;
	}

	private SystemPathsPosix() {
		// prevent instantiation
	}

	public void clearCache() {
		
		try {
			lock.acquire();
			if (fPathCache != null) {
				fPathCache.clear();
			}
		} finally {
			lock.release();
		}
	}
	
	
	public IPath getSystemPath(AVRPath pathcontext) {
		IPath path = null;

		// This method may be called from different threads. To prevent
		// an undefined cache this method locks itself while it
		// is looking for the path

		try {
			lock.acquire();
			if (fPathCache == null) {
				fPathCache = new HashMap<AVRPath, IPath>(AVRPath.values().length);
			}

			// Test if it is already in the cache
			path = fPathCache.get(pathcontext);
			if (path != null) {
				return path;
			}

			// not in cache, then try to find the path
			path = internalGetPath(pathcontext);
			fPathCache.put(pathcontext, path);
		} finally {
			lock.release();
		}
		return path;
		
	}

	private IPath internalGetPath(AVRPath pathcontext) {

		IPath path = fEmptyPath;
		String test = pathcontext.getTest();
		path = which(test);
		if (path.isEmpty()) {
			path = find("*/" + test);
		}
		if (!path.isEmpty()) {
			// remove the number of segments of the test from
			// the path. This makes a test like "avr/io.h" work
			path = path.removeLastSegments(new Path(test).segmentCount());
		}
		return path;
	}

	private IPath which(String file) {

		IPath path = executeCommand("which " + file);
		return path;
	}

	private IPath find(String file) {

		for (String findpath : fSearchPaths) {
			IPath testpath = executeCommand("find " + findpath + " -path "
					+ file);
			if (!testpath.isEmpty()) {
				return testpath;
			}
		}

		// nothing found: return an empty path
		return fEmptyPath;

	}

	public static IPath executeCommand(String command) {

		IPath path = fEmptyPath;

		Process cmdproc = null;
		InputStream is = null;
		InputStreamReader isr = null;
		BufferedReader br = null;

		try {
			cmdproc = ProcessFactory.getFactory().exec(command);
			is = cmdproc.getInputStream();
			isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

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
				if (br != null) br.close();
				if (isr != null) isr.close();
				if (is != null) is.close();
			} catch (IOException e) {
				// can't do anything about it
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
