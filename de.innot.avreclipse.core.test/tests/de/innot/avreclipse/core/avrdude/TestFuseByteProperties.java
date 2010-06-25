/**
 * 
 */
package de.innot.avreclipse.core.avrdude;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.osgi.service.prefs.BackingStoreException;

import de.innot.avreclipse.TestHelper;
import de.innot.avreclipse.core.properties.AVRDudeProperties;
import de.innot.avreclipse.core.properties.AVRProjectProperties;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.FuseType;

/**
 * @author Thomas Holland
 * 
 */
public class TestFuseByteProperties {

	private IEclipsePreferences fPrefs;
	private AVRDudeProperties fAVRDudeProps;

	// The name and the byte values of the fuses file '/TestData/test01.fuses'
	private final static String TEST01_FILE = "/TestData/test01.fuses";
	private final static int[] TEST01_VALUES = new int[] { 0x83, 0x83, 0xfd };

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		IScopeContext scope = new InstanceScope();
		fPrefs = scope.getNode("de.innot.avreclipse.tests");
		AVRProjectProperties projectProps = new AVRProjectProperties(fPrefs);
		fAVRDudeProps = new AVRDudeProperties(fPrefs, projectProps);
	}

	@After
	public void tearDown() throws Exception {
		// clear the preferences
		// fPrefs.removeNode();
		// fPrefs.flush();
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.FuseBytesProperties#FuseBytesProperties(org.osgi.service.prefs.Preferences, de.innot.avreclipse.core.properties.AVRDudeProperties)}
	 * .
	 */
	@Test
	public void testFuseBytesPropertiesPreferencesAVRDudeProperties() {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);
		assertNotNull("Null FuseBytesProperties Object", props);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.FuseBytesProperties#FuseBytesProperties(org.osgi.service.prefs.Preferences, de.innot.avreclipse.core.properties.AVRDudeProperties, de.innot.avreclipse.core.avrdude.FuseBytesProperties)}
	 * .
	 */
	@Test
	public void testFuseBytesPropertiesPreferencesAVRDudePropertiesFuseBytesProperties() {

		int[] testvalues = new int[] { 11, 22, 33 };

		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		// remember original values
		boolean oldusefile = props.getUseFile();
		boolean oldwrite = props.getWrite();

		// and change the values
		props.setMCUId("atmega128");
		props.setUseFile(!oldusefile);
		props.setWrite(!oldusefile);
		props.setValues(testvalues);
		props.setFileName("foobar");

		// Make the copy and check that the values transfered
		FuseBytesProperties cloneprops = new FuseBytesProperties(fPrefs,
				fAVRDudeProps, props);
		assertNotNull("Clone Constructor failed", cloneprops);

		assertEquals("UseFile flag was not cloned", !oldusefile, cloneprops
				.getUseFile());
		assertEquals("Write flag was not cloned", !oldwrite, cloneprops
				.getWrite());
		assertEquals("Filename was not cloned", "foobar", cloneprops
				.getFileName());

		// We need to reset the 'use file' flag so that the mcuid is returned
		// correctly
		cloneprops.setUseFile(false);

		assertEquals("MCU ID was not cloned", "atmega128", cloneprops
				.getMCUId());
		assertArrayEquals("ByteValues were not cloned", testvalues, cloneprops
				.getByteValues().getValues());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getMCUId()}.
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setMCUId(java.lang.String)}
	 * .
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testMCUId() throws IOException, CoreException {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);
		props.setUseFile(false); // don't use file as it would override
		// setMCUId()

		String defaultID = props.getMCUId();

		props.setMCUId(defaultID);
		assertFalse("setMCUId(previousID) changed dirty state", props.isDirty());
		assertEquals("get/set MCUId() failed", defaultID, props.getMCUId());

		props.setMCUId("foobar");
		assertTrue("setMCUId() did not change dirty state", props.isDirty());
		assertEquals("get/set MCUId() failed", "foobar", props.getMCUId());

		// Test with a non-existing file: returns null
		props.setUseFile(true);
		props.setFileName("foo/bar");
		assertNull(props.getMCUId());

		// Test with a real file
		IFile file = TestHelper.getPluginIFile(TEST01_FILE);
		String filename = file.getLocation().toOSString();
		props.setFileName(filename);
		assertEquals("atmega2560", props.getMCUId());

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getWrite()}.
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setWrite(boolean)}
	 * .
	 */
	@Test
	public void testWrite() {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		boolean oldvalue = props.getWrite();

		props.setWrite(oldvalue);
		assertFalse("setWrite(oldvalue) changed dirty state", props.isDirty());
		assertEquals("get/setWrite() failed", oldvalue, props.getWrite());

		props.setWrite(!oldvalue);
		assertTrue("setWrite(!oldvalue) did not change dirty state", props
				.isDirty());
		assertEquals("get/setWrite() failed", !oldvalue, props.getWrite());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getUseFile()}
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setUseFile(boolean)}
	 * .
	 */
	@Test
	public void testGetUseFile() {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		boolean oldvalue = props.getUseFile();

		props.setUseFile(oldvalue);
		assertFalse("setUseFile(oldvalue) changed dirty state", props.isDirty());
		assertEquals("get/setUseFile() failed", oldvalue, props.getUseFile());

		props.setUseFile(!oldvalue);
		assertTrue("setUseFile(!oldvalue) did not change dirty state", props
				.isDirty());
		assertEquals("get/setUseFile() failed", !oldvalue, props.getUseFile());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getFileNameResolved()}
	 * .
	 */
	@Test
	public void testGetFileNameResolved() {
		// Testing this method requires a build configuration, which in turn
		// requires a AVR project. So we test it at a higher level.
		// TODO: write high level tests
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setBuildConfig(org.eclipse.cdt.managedbuilder.core.IConfiguration)}
	 * .
	 */
	@Test
	public void testSetBuildConfig() {
		// Testing this method requires a build configuration, which in turn
		// requires a AVR project. So we test it at a higher level.
		// TODO: write high level tests
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getFileName()}
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setFileName(java.lang.String)}
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getFileNameResolved()}
	 * .
	 */
	@Test
	public void testFileName() {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		// Low level tests only (without an actual file)
		String testname = "foo/bar/baz";
		props.setFileName(testname);
		assertEquals("set/getFilename() failed", testname, props.getFileName());
		// without a build config the getFilenameResolved() method will return
		// the unchanged filename.
		assertEquals("getFilenameResolved() failed", testname, props
				.getFileNameResolved());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getByteValues()}
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setByteValues(de.innot.avreclipse.core.toolinfo.fuses.ByteValues)}
	 * .
	 */
	@Test
	public void testByteValues() {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		String mcuidprops = "atmega128";
		String mcuidnew = "atmega32";
		int[] testvalues = new int[] { 11, 22 };

		props.setMCUId(mcuidprops);
		props.setUseFile(false); // setByteValues() not yet implemented for
		// files

		ByteValues testbv = new ByteValues(FuseType.FUSE, mcuidnew);
		testbv.setValues(testvalues);

		props.setByteValues(testbv);

		// Check that all values from the new ByteValues were copied
		assertEquals("MCU not taken from new ByteValues", mcuidnew, props
				.getMCUId());
		assertArrayEquals(testvalues, props.getValues());

		assertTrue("Dirty flag not set", props.isDirty());

		// Test wrong type of byte values
		ByteValues wrongvalues = new ByteValues(FuseType.LOCKBITS, mcuidnew);
		try {
			props.setByteValues(wrongvalues);
			fail("setByteValues(LockbitValues) did not cause an Exception");
		} catch (IllegalArgumentException iae) {
			// continue
		} catch (Exception e) {
			fail("setByteValues(LockbitValues) caused an unexpected Exception");
		}

		// Test with non-existing file: returns null
		props.setUseFile(true);
		props.setFileName("foo/bar");
		assertNull(props.getByteValues());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#clearValues()}
	 * .
	 */
	@Test
	public void testClearValues() {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		props.setUseFile(false); // setByteValues() not yet implemented for
		// files

		// set some random value and clear them again
		for (int c = 0; c < props.getValues().length; c++) {
			props.setValue(c, 0xaa);
			assertEquals(0xaa, props.getValue(c));
		}

		props.clearValues();
		for (int c = 0; c < props.getValues().length; c++) {
			assertEquals(-1, props.getValue(c));
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setDefaultValues()}
	 * .
	 */
	@Test
	public void testSetDefaultValues() {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		props.setUseFile(false); // setByteValues() not yet implemented for
		// files

		String mcuidprops = props.getMCUId();
		ByteValues testvalues = new ByteValues(FuseType.FUSE, mcuidprops);
		testvalues.setDefaultValues();

		props.clearValues();
		props.setDefaultValues();
		assertArrayEquals(testvalues.getValues(), props.getValues());
		assertTrue("Dirty flag not set", props.isDirty());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getByteValuesFromFile()}
	 * .
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testGetByteValuesFromFile() throws CoreException, IOException {
		// Get the testfilename
		IFile file = TestHelper.getPluginIFile(TEST01_FILE);
		String filename = file.getLocation().toOSString();

		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);
		props.clearValues(); // Clear the immediate values
		props.setUseFile(true);
		props.setFileName(filename);

		ByteValues testvalues = props.getByteValuesFromFile();
		assertArrayEquals(TEST01_VALUES, testvalues.getValues());
		assertEquals("Test Fusefile for regression tests", testvalues
				.getComment());

		// Should get same result from getValues()
		assertArrayEquals(TEST01_VALUES, props.getValues());

		// Test failures
		// Non-Existing file
		props.setFileName("foo/bar");
		try {
			testvalues = props.getByteValuesFromFile();
			fail("No CoreException thrown for an invalid filename");
		} catch (Exception e) {
			assertEquals(CoreException.class, e.getClass());
		}

		// Empty filename --> CoreException
		props.setFileName("");
		try {
			testvalues = props.getByteValuesFromFile();
			fail("No CoreException thrown for an empty filename");
		} catch (Exception e) {
			assertEquals(CoreException.class, e.getClass());
		}

		// Invalid file (missing MCU field)--> CoreException
		IFile invalidfile = TestHelper
				.getPluginIFile("/TestData/invalid.fuses");
		filename = invalidfile.getLocation().toOSString();
		props.setFileName(filename);
		try {
			testvalues = props.getByteValuesFromFile();
			fail("No CoreException thrown for an invalid file");
		} catch (Exception e) {
			assertEquals(CoreException.class, e.getClass());
		}

		// Invalid file (Locks file)--> CoreException
		invalidfile = TestHelper.getPluginIFile("/TestData/test01.locks");
		filename = invalidfile.getLocation().toOSString();
		props.setFileName(filename);
		try {
			testvalues = props.getByteValuesFromFile();
			fail("No CoreException thrown for an invalid file");
		} catch (Exception e) {
			assertEquals(CoreException.class, e.getClass());
		}

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setValues(int[])}
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getValues()}.
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#setValue(int, int)}
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getValue(int)}
	 */
	@Test
	public void testSetGetValues() {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		// Set MCUid to atmega2560, which has three fuse bytes
		props.setMCUId("atmega2560");
		int FUSES = 3;

		// make sure that Atmel did not change the fusecount
		assertEquals(FUSES, props.getByteValues().getByteCount());

		// Simple tests
		int[] testvalues1 = new int[] { 0x11, 0x22, 0x33 };
		props.setValues(testvalues1);
		assertArrayEquals(testvalues1, props.getValues());

		int[] testvalues2 = new int[] { -1, -1, -1 };
		props.setValues(testvalues2);
		assertArrayEquals(testvalues2, props.getValues());

		// Excess Bytes are ignored
		int[] testvalues3 = new int[] { 0x11, 0x22, 0x33, 0x44, 0x55 };
		props.setValues(testvalues3);
		assertEquals(FUSES, props.getValues().length);
		assertArrayEquals(testvalues1, props.getValues());

		// Test invalid values
		try {
			props.setValues(new int[] { 256 });
			fail("Invalid value 256 did not throw IllegalArgumentException");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}

		try {
			props.setValues(new int[] { -2 });
			fail("Invalid value -2 did not throw IllegalArgumentException");
		} catch (Exception e) {
			assertEquals(IllegalArgumentException.class, e.getClass());
		}

		// Test the index version
		for (int i = 0; i < testvalues1.length; i++) {
			props.setValue(i, i);
		}
		for (int i = 0; i < testvalues2.length; i++) {
			assertEquals(i, props.getValue(i));
		}

		// setValue() with invalid index does nothing
		props.setValue(testvalues1.length, 0xdd);

		// getValue() with invalid index returns -1
		assertEquals(-1, props.getValue(testvalues1.length));

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getValuesFromFile()}
	 * .
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testGetValuesFromFile() throws IOException, CoreException {

		IFile file = TestHelper.getPluginIFile(TEST01_FILE);
		String filename = file.getLocation().toOSString();

		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);
		props.setFileName(filename);

		// Don't use file. getValues() should return the immediate values (all
		// -1)
		props.setUseFile(false);
		props.clearValues(); // Clear the immediate values
		int[] immvalues = props.getValues();
		for (int v : immvalues) {
			assertEquals(-1, v);
		}

		// getValuesFromFile() must return the values from the file,
		// regardless of the useFile flag
		assertArrayEquals(TEST01_VALUES, props.getValuesFromFile());

		// Check for non-existing file --> returns empty array
		props.setFileName("foo/bar");
		assertEquals(0, props.getValuesFromFile().length);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#getValuesFromImmediate()}
	 * .
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testGetValuesFromImmediate() throws IOException, CoreException {

		IFile file = TestHelper.getPluginIFile(TEST01_FILE);
		String filename = file.getLocation().toOSString();

		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);
		props.setFileName(filename);

		// Use file. getValues() should return the file values
		props.setUseFile(true);
		props.clearValues(); // Clear the immediate values

		assertArrayEquals(TEST01_VALUES, props.getValues());

		// But the immediate Values are still all at -1
		int[] immvalues = props.getValuesFromImmediate();
		for (int v : immvalues) {
			assertEquals(-1, v);
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#syncFromFile()}
	 * .
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testSyncFromFile() throws IOException, CoreException {
		IFile file = TestHelper.getPluginIFile(TEST01_FILE);
		String filename = file.getLocation().toOSString();

		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);
		props.setFileName(filename);

		// Use Immediate mode and set all values different than the file
		props.setUseFile(false);
		props.setMCUId("atmega32");
		props.setDefaultValues();

		// Now Sync and all properties should be the same as the file
		props.syncFromFile();

		assertFalse(props.getUseFile()); // Still using immediate mode
		assertEquals("atmega2560", props.getMCUId());
		assertArrayEquals(TEST01_VALUES, props.getValues());
		assertArrayEquals(TEST01_VALUES, props.getValuesFromImmediate());

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#save()}.
	 * 
	 * @throws BackingStoreException
	 */
	@Test
	public void testSave() throws BackingStoreException {
		FuseBytesProperties props1 = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		props1.setMCUId("atmega2560");
		props1.setValues(TEST01_VALUES);
		props1.setWrite(true);

		props1.save();
		assertFalse(props1.isDirty());

		// changes after save() should not be stored
		props1.setFileName("foo/bar");
		assertTrue(props1.isDirty());

		// get a new Properties object. As it is derived from the same
		// preference store, it should have the values we just saved.
		FuseBytesProperties props2 = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		assertEquals("atmega2560", props2.getMCUId());
		assertArrayEquals(TEST01_VALUES, props2.getValues());
		assertTrue(props2.getWrite());
		assertEquals("", props2.getFileName());
		assertFalse(props2.isDirty());
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#isCompatibleWith(java.lang.String)}
	 * .
	 * 
	 * @throws CoreException
	 * @throws IOException
	 */
	@Test
	public void testIsCompatibleWith() throws IOException, CoreException {
		FuseBytesProperties props = new FuseBytesProperties(fPrefs,
				fAVRDudeProps);

		props.setMCUId("atmega16");

		// Compatible with itself
		assertTrue(props.isCompatibleWith("atmega16"));

		assertTrue(props.isCompatibleWith("atmega16a"));

		assertFalse(props.isCompatibleWith("atmega64"));

		assertFalse(props.isCompatibleWith("foobar"));

		// Check with a file
		IFile file = TestHelper.getPluginIFile(TEST01_FILE);
		String filename = file.getLocation().toOSString();

		props.setFileName(filename);
		props.setUseFile(true);

		assertTrue(props.isCompatibleWith("atmega2561"));
		assertFalse(props.isCompatibleWith("atmega16"));

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.BaseBytesProperties#convertTo(java.lang.String, de.innot.avreclipse.core.toolinfo.fuses.ConversionResults)}
	 * .
	 */
	@Test
	public void testConvertTo() {
		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.avrdude.FuseBytesProperties#getArguments(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetArguments() {
		fail("Not yet implemented");
	}

}
