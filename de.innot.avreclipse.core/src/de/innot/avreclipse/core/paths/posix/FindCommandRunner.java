/**
 * 
 */
package de.innot.avreclipse.core.paths.posix;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.operation.IRunnableWithProgress;

/**
 * @author Thomas Holland
 * 
 */
public class FindCommandRunner implements IRunnableWithProgress {

	private final static IPath fEmptyPath = new Path("");

	private String fFile = null;
	private String[] fSearchPaths = null;
	private IPath fPath = null;

	public FindCommandRunner(String file, String[] searchpaths) {
		fFile = file;
		fSearchPaths = searchpaths;
		fPath = fEmptyPath;
	}

	public IPath getPath() {
		return fPath;
	}

	public void run(IProgressMonitor monitor) throws InvocationTargetException,
	        InterruptedException {
		try {
			monitor.beginTask("Finding Path for '" + fFile + "'", fSearchPaths.length);
			for (String findpath : fSearchPaths) {
				IPath testpath = executeCommand("find " + findpath + " -path \"" + fFile + "\"");
				if (!testpath.isEmpty()) {
					fPath = testpath;
					monitor.done();
					return;
				}
				monitor.worked(1);
			}
		} finally {
			monitor.done();
		}
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
