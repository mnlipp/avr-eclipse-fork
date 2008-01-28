/**
 * 
 */
package de.innot.avreclipse.core.paths;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import de.innot.avreclipse.core.paths.IPathManager.SourceType;

/**
 * @author U043192
 * 
 */
public class AVRPathManagerTest {

	/**
	 * Test method for {@link de.innot.avreclipse.core.paths.AVRPath}.
	 */
	@Test
	public void testAVRPath() {

		// Get all supported enum values
		AVRPath[] allpaths = AVRPath.values();
		assertTrue(allpaths.length > 5);

		for (AVRPath current : allpaths) {
			assertTrue(current.getName().length() > 1);
			assertTrue(current.getDescription().length() > 1);
			assertTrue(current.getTest().length() > 1);
		}
		
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.paths.AVRPathProvider}.
	 */
	@Test
	public void testAVRPathProvider() {

		// Get all supported enum values
		AVRPath[] allpaths = AVRPath.values();

		for (AVRPath current : allpaths) {
			IPathProvider provider = new AVRPathProvider(current);
			assertNotNull(provider.getPath());
			File file = provider.getPath().toFile();
			assertTrue(file.canRead());
			assertTrue(file.isDirectory());
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.paths.AVRPathProvider}.
	 */
	@Test
	public void testAVRPathManager() {

		IPathManager manager = new AVRPathManager(AVRPath.AVRGCC);
		assertTrue(manager.getName() + " invalid", manager.isValid());

		// Test Default and System Paths
		assertNotNull(manager.getDefaultPath());
		assertFalse("".equals(manager.getDefaultPath().toString()));
		assertFalse("".equals(manager.getSystemPath().toString()));
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.paths.AVRPathProvider.isValid}.
	 */
	@Test
	public void testIsValid() {
		// Test a required Path
		IPathManager manager = new AVRPathManager(AVRPath.AVRGCC);
		assertTrue(manager.isValid());
		// empty paths not allowed
		manager.setPath("", SourceType.Custom);
		assertFalse(manager.isValid());

		// Test an optional path
		manager = new AVRPathManager(AVRPath.PDFPATH);
		assertTrue(manager.isValid());
		// empty paths allowed
		manager.setPath("", SourceType.Custom);
		assertTrue(manager.isValid());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.paths.AVRPathProvider.setPath}.
	 */
	@Test
	public void testSetPath() {
		IPathManager manager = new AVRPathManager(AVRPath.MAKE);

		manager.setPath("foo/bar", IPathManager.SourceType.Custom);
		assertFalse(manager.isValid());
		assertTrue("foo/bar".equals(manager.getPath().toString()));

		manager.setPath(null, IPathManager.SourceType.System);
		assertTrue(manager.isValid());

		manager.setToDefault();
		assertTrue(manager.isValid());
		assertTrue(manager.getPath().equals(manager.getDefaultPath()));

		// Test value propagation
		IPathProvider provider = new AVRPathProvider(AVRPath.MAKE);
		manager.setPath("bar-baz", IPathManager.SourceType.Custom);
		assertFalse("bar-baz".equals(provider.getPath().toString()));
		manager.store();
		assertTrue("bar-baz".equals(provider.getPath().toString()));

		// cleanup
		manager.setToDefault();
		manager.store();
		assertTrue(provider.getPath().equals(manager.getDefaultPath()));
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.paths.AVRPathProvider.clone}.
	 */
	@Test
	public void testClone() {
		IPathManager manager = new AVRPathManager(AVRPath.AVRINCLUDE);
		IPathManager clone = manager.clone();
		
		assertTrue(manager.getPath().equals(clone.getPath()));
		assertTrue(manager.getSourceType().equals(clone.getSourceType()));
		
		clone.setPath("foo/bar", SourceType.Custom);
		assertFalse(manager.getPath().equals(clone.getPath()));
		assertFalse(manager.getSourceType().equals(clone.getSourceType()));

	}

}
