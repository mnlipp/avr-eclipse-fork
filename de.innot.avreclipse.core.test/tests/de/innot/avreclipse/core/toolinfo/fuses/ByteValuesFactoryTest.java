/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/
/**
 * 
 */
package de.innot.avreclipse.core.toolinfo.fuses;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.runtime.CoreException;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Thomas
 * 
 */
public class ByteValuesFactoryTest {

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValuesFactory#createByteValues(org.eclipse.core.resources.IFile)}
	 * .
	 */
	@Test
	@Ignore
	public void testCreateByteValues() {

		fail("Not yet implemented");
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.toolinfo.fuses.ByteValuesFactory#createByteValuesFromStream(java.io.InputStream, de.innot.avreclipse.core.toolinfo.fuses.FuseType)}
	 * .
	 */
	@Test
	public void testCreateByteValuesFromStream() {

		// Test valid Fuses property
		StringBuilder test = new StringBuilder("MCU=atmega169\n" + "CKDIV8=0x1\n"
				+ "BODLEVEL=0x7\n" + "	OCDEN=0x0\n" + "SUT_CKSEL=0x22\n" + "EESAVE=0x0\n"
				+ "RSTDISBL=0x1\n" + "BOOTRST=0x0\n" + "JTAGEN=0x0\n" + "summary=\n");

		InputStream teststream = new ByteArrayInputStream(test.toString().getBytes());
		ByteValues values = null;
		try {
			values = ByteValuesFactory.createByteValuesFromStream(teststream, FuseType.FUSE);
		} catch (CoreException e) {
			fail("Unexpected CoreException");
		}

		assertNotNull("Unexpected null return", values);
		assertEquals("wrong MCU", "atmega169", values.getMCUId());
		assertEquals("CKDIV8 wrong value", 0x1, values.getNamedValue("CKDIV8"));
		assertEquals("SUT_CKSEL wrong value", 0x22, values.getNamedValue("SUT_CKSEL"));
		assertEquals("JTAGEN wrong value", 0x0, values.getNamedValue("JTAGEN"));

		// Add two properties with invalid values
		test.append("BOOTSZ=0x23\n" + "SPIEN=x0x0\n");
		teststream = new ByteArrayInputStream(test.toString().getBytes());
		values = null;
		try {
			values = ByteValuesFactory.createByteValuesFromStream(teststream, FuseType.FUSE);
		} catch (CoreException e) {
			fail("Unexpected CoreException");
		}

		assertNotNull("Unexpected null return", values);
		assertEquals("BOOTSZ did not have default value", 0x3, values.getNamedValue("BOOTSZ"));
		assertEquals("SPIEN did not have default value", 0x1, values.getNamedValue("SPIEN"));

		// Add invalid tag -> should be skipped
		test.append("foo=bar\n");
		teststream = new ByteArrayInputStream(test.toString().getBytes());
		values = null;
		try {
			values = ByteValuesFactory.createByteValuesFromStream(teststream, FuseType.FUSE);
		} catch (CoreException e) {
			fail("Unexpected CoreException");
		}

		assertNotNull("Unexpected null return", values);

		// add two valid tag, should be read
		test.append("CKOUT=0x1\n" + "WDTON=0x0\n");
		teststream = new ByteArrayInputStream(test.toString().getBytes());
		values = null;
		try {
			values = ByteValuesFactory.createByteValuesFromStream(teststream, FuseType.FUSE);
		} catch (CoreException e) {
			fail("Unexpected CoreException");
		}

		assertNotNull("Unexpected null return", values);
		assertEquals("CKOUT wrong value", 0x1, values.getNamedValue("CKOUT"));
		assertEquals("WDTON wrong value", 0x0, values.getNamedValue("WDTON"));

		// Missing MCU -> should throw CoreException
		test = new StringBuilder("SUT_CKSEL=0x22\n" + "EESAVE=0x0\n" + "BOOTSZ=0x0\n"
				+ "SPIEN=0x0\n" + "RSTDISBL=0x1\n" + "BOOTRST=0x0\n" + "JTAGEN=0x0\n"
				+ "summary=\n");
		teststream = new ByteArrayInputStream(test.toString().getBytes());
		values = null;
		try {
			values = ByteValuesFactory.createByteValuesFromStream(teststream, FuseType.FUSE);
			fail("No Exception thrown for missing [MCU] tag");
		} catch (CoreException e) {
			// Expected -> Continue
		}

	}
}
