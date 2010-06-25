/**
 * 
 */
package de.innot.avreclipse.core.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import de.innot.avreclipse.util.URLDownloadException;
import de.innot.avreclipse.util.URLDownloadManager;

/**
 * @author U043192
 * 
 */
public class TestURLDownloadManager {

	@Before
	public void setupTest() {
		// clear the cache
		URLDownloadManager.clearCache();
	}

	/**
	 * Test method for {@link URLDownloadManager#download(URL, IProgressMonitor)}.
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	@Ignore
	public void testNormalDownload() throws MalformedURLException {
		URL testurl = new URL(
				"http://downloads.sourceforge.net/avr-eclipse/de.innot.avreclipse-2.1.0.20080210PRD.zip");

		try {
			long starttime = System.currentTimeMillis();
			File file = URLDownloadManager.download(testurl, new NullProgressMonitor());
			long endtime = System.currentTimeMillis();
			long runningtime = endtime - starttime;

			// This might fail on very fast internet connections with download
			// speeds > 8 Mytes/sec.
			assertFalse("First download to fast (out of the cache?) [actual:" + runningtime
					+ "ms < 100ms]", runningtime < 100);

			// Download should be OK
			assertNotNull("Returned null File", file);

			assertTrue("Downloaded file does not exist", file.isFile());
			assertTrue("Downloaded file cannot be read", file.canRead());
			// At least the extension should match the URL file
			String expectedname = "de.innot.avreclipse-2.1.0.20080210PRD.zip";
			assertEquals("Downloaded file has wrong extension", expectedname, file.getName());

			// Test the cache. A second call for the same URL should return very
			// quickly
			starttime = System.currentTimeMillis();
			file = URLDownloadManager.download(testurl, new NullProgressMonitor());
			endtime = System.currentTimeMillis();
			runningtime = endtime - starttime;

			// This might fail on very very slow systems.
			assertFalse("Download cached item took to long. [actual:" + runningtime + "ms > 10ms]",
					runningtime > 10);

			assertNotNull("Returned null File from cache", file);

			assertTrue("Cached file does not exist", file.isFile());
			assertTrue("Cahced file cannot be read", file.canRead());
			// At least the extension should match the URL file
			assertEquals("Cached file has wrong extension", expectedname, file.getName());
		} catch (URLDownloadException e) {
			fail("Unexpected error on Download: " + e.getLocalizedMessage());
		}
	}

	/**
	 * Test method for {@link URLDownloadManager#download(URL, IProgressMonitor)}.
	 * 
	 * @throws MalformedURLException
	 */
	@Test
	@Ignore
	public void testFailedDownload() throws MalformedURLException {
		// unknown file
		URL testurl = new URL("http://avr-eclipse.sourceforge.net/foobar.zip");
		try {
			URLDownloadManager.download(testurl, new NullProgressMonitor());
			fail("No Exception thrown for non-existing file");
		} catch (URLDownloadException ude) {
			assertNotNull(ude.getMessage());
			assertNotNull(ude.getCause());
			assertTrue(ude.getCause() instanceof FileNotFoundException);
		}

		// unknown host
		testurl = new URL("http://foo.bar/baz.zip");
		try {
			URLDownloadManager.download(testurl, new NullProgressMonitor());
			fail("No Exception thrown for non-existing host");
		} catch (URLDownloadException ude) {
			assertNotNull(ude.getMessage());
			assertNotNull(ude.getCause());
			assertTrue(ude.getCause() instanceof UnknownHostException);
		}
	}

	/**
	 * Test method for {@link URLDownloadManager#download(URL, IProgressMonitor)}.
	 * 
	 * @throws MalformedURLException
	 * @throws InterruptedException
	 */
	@Test
	@Ignore
	public void testCanceledDownload() throws MalformedURLException, InterruptedException {
		final URL testurl = new URL(
				"http://downloads.sourceforge.net/avr-eclipse/de.innot.avreclipse-2.1.0.20080210PRD.zip");

		final IProgressMonitor testmonitor = new NullProgressMonitor();

		Job job = new Job("CancelTest") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					URLDownloadManager.download(testurl, testmonitor);
				} catch (URLDownloadException e) {
					return new Status(Status.ERROR, "junit.test", "should not happen", e);
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		while (job.getState() != Job.RUNNING) {
			Thread.sleep(10);
		}
		assertTrue("Download not in internal list of downloads", URLDownloadManager
				.isDownloading(testurl));

		// cancel the Job and wait for it to finish
		testmonitor.setCanceled(true);
		job.join();
		assertTrue("Download did not cancel", job.getResult().equals(Status.CANCEL_STATUS));
	}

}
