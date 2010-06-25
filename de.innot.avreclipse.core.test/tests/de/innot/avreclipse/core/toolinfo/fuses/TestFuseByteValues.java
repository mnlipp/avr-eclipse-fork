/**
 * 
 */
package de.innot.avreclipse.core.toolinfo.fuses;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import de.innot.avreclipse.core.toolinfo.fuses.ConversionResults.ConversionStatus;

/**
 * @author U043192
 * 
 */
public class TestFuseByteValues {

	private ByteValues				testvalues;

	private final static FuseType	FUSE		= FuseType.FUSE;

	private static int[]			values6		= new int[] { 0xDE, 0xAD, 0xBE, 0xEF, 0xAA, 0xBB };
	private static int[]			values3		= new int[] { 0xAB, 0xAD, -1 };
	private static int[]			defaults6	= new int[] { -1, -1, -1, -1, -1, -1 };

	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.FuseByteValues#FuseByteValues(java.lang.String)}.
	 */
	@Test
	public void testFuseByteValuesString() {
		// Test the constructor with String parameter

		// 1. Test a valid mcuid
		testvalues = new ByteValues(FUSE, "atmega16");
		assertNotNull("FuseByteValue(\"atmega16\") returned null", testvalues);

		// 2. Test a null mcu id: This should throw an Assertion error
		try {
			testvalues = new ByteValues(FUSE, (String) null);
			fail("FuseByteValues((String)null) did not throw an error");
		} catch (Exception e) {
			// test passed
		}
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.FuseByteValues#FuseByteValues(de.innot.avreclipse.core.toolinfo.fuses.ByteValues)}.
	 */
	@Test
	public void testFuseByteValuesByteValues() {
		// Test the clone constructor

		// Create source object
		ByteValues source = new ByteValues(FUSE, "at90pwm2");
		source.setValues(values3);

		testvalues = new ByteValues(source);
		assertNotNull("FuseByteValue(source) returned null", testvalues);
		assertEquals("Cloning did not copy mcu id field", "at90pwm2", testvalues.getMCUId());
		assertArrayEquals("Cloning did not copy values", values3, testvalues.getValues());

		// Test a null source: This should throw an Assertion error
		try {
			testvalues = new ByteValues((ByteValues) null);
			fail("FuseByteValues((ByteValues)null) did not throw an error");
		} catch (Exception e) {
			// test passed
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.fuses.FuseByteValues#getByteCount()}.
	 */
	@Test
	public void testGetByteCount() {
		// test 6 fuse byte mcu
		testvalues = new ByteValues(FUSE, "atxmega64a1");
		assertEquals("Wrong bytecount for atxmega64a1", 6, testvalues.getByteCount());

		// test 3 fuse byte mcu
		testvalues = new ByteValues(FUSE, "at90pwm2");
		assertEquals("Wrong bytecount for at90pwm2", 3, testvalues.getByteCount());

		// test 2 fuse byte mcu
		testvalues = new ByteValues(FUSE, "atmega323");
		assertEquals("Wrong bytecount for atmega323", 2, testvalues.getByteCount());

		// test 1 fuse byte mcu
		testvalues = new ByteValues(FUSE, "attiny12");
		assertEquals("Wrong bytecount for attiny12", 1, testvalues.getByteCount());

		// test 0 fuse byte mcu
		testvalues = new ByteValues(FUSE, "at86rf401");
		assertEquals("Wrong bytecount for at86rf401", 0, testvalues.getByteCount());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValues#getMCUId()}.
	 */
	@Test
	public void testGetMCUId() {
		testvalues = new ByteValues(FUSE, "at86rf401");
		assertEquals("Wrong MCU id", "at86rf401", testvalues.getMCUId());

		testvalues = new ByteValues(FUSE, "atxmega128a1");
		assertEquals("Wrong MCU id", "atxmega128a1", testvalues.getMCUId());
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValues#setValue(int, int)}.
	 */
	@Test
	public void testSetValues() {
		// Test setting and getting byte arrays
		testvalues = new ByteValues(FUSE, "atxmega128a1");

		// Values should now be at the default value (-1)
		assertArrayEquals("Wrong default values", defaults6, testvalues.getValues());

		// Set new values and test them - all together and each one seperate
		testvalues.setValues(values6);
		int[] values = testvalues.getValues();
		assertEquals("Wrong number of bytes returned", 6, values.length);
		assertArrayEquals("Wrong values", values6, values);
		for (int i = 0; i < values6.length; i++) {
			assertEquals("Wrong value at [" + i + "]", values6[i], testvalues.getValue(i));
		}

		// Test an values array of a different length
		testvalues.setValues(values3);
		values = testvalues.getValues();
		assertEquals("Wrong number of bytes returned", 6, values.length);
		for (int i = 0; i < values3.length; i++) {
			assertEquals("Wrong value at [" + i + "]", values3[i], testvalues.getValue(i));
		}

		// test a longer array (should be truncated)
		testvalues = new ByteValues(FUSE, "atmega323");
		testvalues.setValues(values6);
		values = testvalues.getValues();
		assertEquals("Wrong number of bytes returned", 2, values.length);
		for (int i = 0; i < values.length; i++) {
			assertEquals("Wrong value at [" + i + "]", values6[i], testvalues.getValue(i));
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValues#setValue(int, int)}.
	 */
	@Test
	public void testSetValue() {
		// Test setting and getting single bytes

		testvalues = new ByteValues(FUSE, "atxmega128a1");

		for (int i = 0; i < values6.length; i++) {
			testvalues.setValue(i, values6[i]);
		}
		assertArrayEquals("Wrong values", values6, testvalues.getValues());

		// Test illegal index: should throw an exception
		try {
			testvalues.setValue(-1, 0);
			fail("setValue(-1,0) did not throw exception");
		} catch (IllegalArgumentException iae) {
		}

		try {
			testvalues.setValue(6, 0);
			fail("setValue(6,0) did not throw exception");
		} catch (IllegalArgumentException iae) {
		}

		// Test legal values
		testvalues.setValue(0, -1);
		testvalues.setValue(1, 0);
		testvalues.setValue(2, 255);

		assertEquals("getValue() for -1", -1, testvalues.getValue(0));
		assertEquals("getValue() for 0", 0, testvalues.getValue(1));
		assertEquals("getValue() for 255", 255, testvalues.getValue(2));

		// Test illegal values
		try {
			testvalues.setValue(0, -2);
			fail("setValue(0, -2) did not throw exception");
		} catch (IllegalArgumentException iae) {
		}
		try {
			testvalues.setValue(0, 256);
			fail("setValue(0, 256) did not throw exception");
		} catch (IllegalArgumentException iae) {
		}

	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValues#setNamedValue(java.lang.String, int)}.
	 */
	@Test
	public void testSetNamedValue() {

		testvalues = new ByteValues(FUSE, "atmega16");

		// Test a single bit without shifting
		// BOOTRST = byte 1, bit 0
		testvalues.setValue(1, 0xAA); // = 10101010 pattern

		// Set to 1
		testvalues.setNamedValue("BOOTRST", 0x01);
		assertEquals(bin(0xAB), bin(testvalues.getValue(1)));
		int value = testvalues.getNamedValue("BOOTRST");
		assertEquals("getNamedValue(\"BOOTRST\")", 0x01, value);

		// Set to 0
		testvalues.setNamedValue("BOOTRST", 0x00);
		assertEquals(bin(0xAA), bin(testvalues.getValue(1)));
		value = testvalues.getNamedValue("BOOTRST");
		assertEquals("getNamedValue(\"BOOTRST\")", 0x00, value);

		// Test a single bit with shifting
		// BODEN = byte 0, bit 7
		testvalues.setValue(0, 0x00);

		// Set to 1
		testvalues.setNamedValue("BODEN", 0x01);
		assertEquals(bin(0x40), bin(testvalues.getValue(0)));
		value = testvalues.getNamedValue("BODEN");
		assertEquals("getNamedValue(\"BODEN\")", 0x01, value);

		// Set to 0
		testvalues.setNamedValue("BODEN", 0x00);
		assertEquals(bin(0x00), bin(testvalues.getValue(0)));
		value = testvalues.getNamedValue("BODEN");
		assertEquals("getNamedValue(\"BODEN\")", 0x00, value);

		// Test multibit with shifting
		// BOOTSZ = byte 1, bit 1-2
		testvalues.setValue(1, 0x00);

		// Set to 0
		testvalues.setNamedValue("BOOTSZ", 0x00);
		assertEquals(bin(0x00), bin(testvalues.getValue(1)));
		value = testvalues.getNamedValue("BOOTSZ");
		assertEquals("getNamedValue(\"BOOTSZ\")", 0x00, value);

		// Set to 1
		testvalues.setNamedValue("BOOTSZ", 0x01);
		assertEquals(bin(0x02), bin(testvalues.getValue(1)));
		value = testvalues.getNamedValue("BOOTSZ");
		assertEquals("getNamedValue(\"BOOTSZ\")", 0x01, value);

		// Set to 2
		testvalues.setNamedValue("BOOTSZ", 0x02);
		assertEquals(bin(0x04), bin(testvalues.getValue(1)));
		value = testvalues.getNamedValue("BOOTSZ");
		assertEquals("getNamedValue(\"BOOTSZ\")", 0x02, value);

		// Set to 3
		testvalues.setNamedValue("BOOTSZ", 0x03);
		assertEquals(bin(0x06), bin(testvalues.getValue(1)));
		value = testvalues.getNamedValue("BOOTSZ");
		assertEquals("getNamedValue(\"BOOTSZ\")", 0x03, value);

		// Test Failures

		// Illegal values
		try {
			testvalues.setNamedValue("BOOTSZ", 0x04);
			fail("Did not throw with illegal value 0x04");
		} catch (IllegalArgumentException iae) {
			// Test passed
		}

		try {
			testvalues.setNamedValue("SUT_CKSEL", -1);
			fail("Did not throw with illegal value -1");
		} catch (IllegalArgumentException iae) {
			// Test passed
		}

		// Illegal names
		try {
			testvalues.setNamedValue("foobar", 0);
			fail("Did not throw with illegal name \"foobar\"");
		} catch (IllegalArgumentException iae) {
			// Test passed
		}

		try {
			testvalues.setNamedValue("", 0);
			fail("Did not throw with illegal empty name");
		} catch (IllegalArgumentException iae) {
			// Test passed
		}

		try {
			testvalues.setNamedValue(null, 0);
			fail("Did not throw with illegal null name");
		} catch (IllegalArgumentException iae) {
			// Test passed
		}

	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValues#clearValues()}.
	 */
	@Test
	public void testClearValues() {
		testvalues = new ByteValues(FUSE, "atxmega64a1");

		// Values should now be at the default value (-1)
		assertArrayEquals("Wrong default values", defaults6, testvalues.getValues());

		// Set new values, clear them and test - all together and each one seperate
		testvalues.setValues(values6);
		testvalues.clearValues();

		assertArrayEquals("Wrong values after clear", defaults6, testvalues.getValues());
		for (int i = 0; i < values6.length; i++) {
			assertEquals("Wrong value at [" + i + "] after clear", -1, testvalues.getValue(i));
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValues#convertTo()}.
	 */
	@Test
	public void testConvertTo() {

		// First test: convert from ATXmega64 to ATXmega128
		// these two have identical fuses,
		ConversionResults results = new ConversionResults();
		testvalues = new ByteValues(FUSE, "atxmega64a1");
		testvalues.setValues(values6);

		ByteValues targetvalues = testvalues.convertTo("atxmega128a1", results);

		assertEquals("Wrong MCU", "atxmega128a1", targetvalues.getMCUId());
		assertEquals("Successrate not as expected", 100, results.getSuccessRate());

		// Second test: convert ATmega16 to ATmega32
		// They differ by only one bitfield: SUT_CKSEL vs. CKSEL
		// But with the fixed description files they have identical fusebytes.
		testvalues = new ByteValues(FUSE, "atmega16");
		testvalues.setValues(values3);
		targetvalues = testvalues.convertTo("atmega32", results);

		assertEquals("Wrong MCU", "atmega32", targetvalues.getMCUId());
		assertEquals("Successrate not as expected", 100, results.getSuccessRate());

		// Third test: convert ATmega16 to ATmega161
		// They have only two common BitFields: SPIEN and BOOTRST
		// ATmega16 add. fields: OCDEN, JTAGEN, EESAVE, BOOTSZ, CKOPT, BODLEVEL, BODEN, SUT_CKSEL
		// ATmega161 add. fields: SUT, CKSEL
		targetvalues = testvalues.convertTo("atmega161", results);

		assertEquals("Wrong MCU", "atmega161", targetvalues.getMCUId());
		assertEquals("SPIEN not successful", ConversionStatus.SUCCESS, results
				.getStatusForName("SPIEN"));
		assertEquals("BOOTRST not successful", ConversionStatus.SUCCESS, results
				.getStatusForName("BOOTRST"));
		assertEquals("JTAGEN wrong status", ConversionStatus.NOT_IN_TARGET, results
				.getStatusForName("JTAGEN"));
		assertEquals("BODLEVEL wrong status", ConversionStatus.NOT_IN_TARGET, results
				.getStatusForName("BODLEVEL"));
		assertEquals("SUT wrong status", ConversionStatus.NOT_IN_SOURCE, results
				.getStatusForName("SUT"));
		assertEquals("CKSEL wrong status", ConversionStatus.NOT_IN_SOURCE, results
				.getStatusForName("CKSEL"));

		assertTrue("SUT_CKSEL illegally converted to CKSEL.",
				testvalues.getNamedValue("SUT_CKSEL") != targetvalues.getNamedValue("CKSEL"));

	}

	private ByteValueChangeEvent[]	fEvents	= null;

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValues#addByteValuesChangeListener}.
	 */
	@Test
	public void testListener() {
		testvalues = new ByteValues(FUSE, "atmega16");
		testvalues.setDefaultValues();
		testvalues.addChangeListener(new IByteValuesChangeListener() {
			public void byteValuesChanged(ByteValueChangeEvent[] events) {
				fEvents = events;
			}
		});

		// Cause a single BitField change event
		testvalues.setNamedValue("SPIEN", 0x01);
		assertNotNull("No Events fired", fEvents);
		assertEquals("Wrong number of event objects", 1, fEvents.length);
		ByteValueChangeEvent event = fEvents[0];
		assertEquals("Wrong BitField in event", "SPIEN", event.name);
		assertEquals("Wrong new value in event", 1, event.bitfieldvalue);
		assertEquals("Wrong byte index", 1, event.byteindex);
		assertEquals("Wrong byte value", 0xB9, event.bytevalue);

		// cause multiple BitField change events
		final String[] names = new String[] { "BODLEVEL", "BODEN", "SUT_CKSEL" };
		testvalues.setValue(0, 0xC1);
		assertNotNull("No Events fired", fEvents);
		assertEquals("Wrong number of event objects", 3, fEvents.length);
		for (int i = 0; i < fEvents.length; i++) {
			assertEquals("Wrong byte index in event" + event, 0, fEvents[i].byteindex);
			assertEquals("Wrong byte value in event" + event, 0xC1, fEvents[i].bytevalue);
			boolean foundname = false;
			for (String name : names) {
				if (name.equals(fEvents[i].name)) {
					foundname = true;
				}
			}
			if (!foundname)
				fail("None of the required names in the events");
		}

	}

	private String bin(int value) {
		return Integer.toBinaryString(value);
	}
}
