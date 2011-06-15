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

package de.innot.avreclipse.core.targets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import de.innot.avreclipse.core.targets.ClockValuesGenerator.ClockValuesType;

/**
 * Tests for the ClockRangeGenerator
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class ClockRangeGeneratorTest {

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.core.targets.ClockValuesGenerator#getValues(de.innot.avreclipse.core.targets.ClockValuesGenerator.ClockRangeType)}
	 * .
	 */
	@Test
	public void testGetValues() {
		for (ClockValuesType type : ClockValuesType.values()) {
			int[] values = ClockValuesGenerator.getValues(type);

			assertNotNull("getValues(" + type.toString() + ") returned null", values);
			assertTrue("Expected at least 4 entries", values.length >= 4);
			assertEquals("First value must be 0", 0, values[0]);
		}
	}

	// Not a real test
	// Just a convenience to get a first impression if the values are reasonable
	// @Test
	public void dumpValues() {
		for (ClockValuesType type : ClockValuesType.values()) {
			int[] values = ClockValuesGenerator.getValues(type);
			System.out.println("\n\nValues for " + type.toString() + " has " + values.length
					+ " entries.");
			System.out.print("{");
			int c = 0;
			for (int val : values) {
				System.out.print(val + ", ");
				if (c++ == 8) {
					System.out.println("");
					c = 0;
				}
			}
			System.out.println("}");
		}
	}
}
