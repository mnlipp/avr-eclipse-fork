/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.core.targets;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.tools.AvariceProgrammers;
import de.innot.avreclipse.core.toolinfo.AVRDude;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TestAvariceProgrammers {

	private static String[]	allValues	= new String[] { "dragon_jtag", "dragon_dw", "jtag1",
			"jtag2", "jtag2dw"			};

	@Test
	public void testEnumerationValues() {
		// Check that the enum contains at least the 5 known programmer configs.
		for (String id : allValues) {
			AvariceProgrammers programmer = AvariceProgrammers.valueOf(id);
			assertNotNull("Programmer " + id + " MIA", programmer);
		}
	}

	@Test
	public void testIProgrammerInterface() throws AVRDudeException {

		// We test this class by going through all values of the enumeration, calling all methods of
		// the IProgrammer interface and check that they return the same values as the IProgrammer
		// returned by avrdude.

		for (AvariceProgrammers progger : AvariceProgrammers.values()) {
			// Only test if the same id exists within avrdude
			String id = progger.getId();
			IProgrammer avrdudeprogger = AVRDude.getDefault().getProgrammer(id);

			if (avrdudeprogger != null) {
				assertEquals("Difference in Description: ", avrdudeprogger.getDescription(),
						progger.getDescription());
				assertArrayEquals("Difference in HostInterfaces: ", avrdudeprogger
						.getHostInterfaces(), progger.getHostInterfaces());
				assertEquals("Difference in TargetInterfaces: ", avrdudeprogger
						.getTargetInterface(), progger.getTargetInterface());
				assertArrayEquals("Difference in Target clocks: ", avrdudeprogger
						.getTargetInterfaceClockFrequencies(), progger
						.getTargetInterfaceClockFrequencies());
				assertEquals("Difference in Daisy Chain: ", avrdudeprogger.isDaisyChainCapable(),
						progger.isDaisyChainCapable());
			}

		}

	}

}
