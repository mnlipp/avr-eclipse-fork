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

/**
 * @author Thomas Holland
 * @since
 * 
 */
public class TCValidator implements ITargetConfigConstants {

	public enum Problem {
		OK, WARN, ERROR
	};

	public static Problem validate(ITargetConfiguration config) {

		boolean hasWarning = false;
		boolean hasError = false;
		if (checkJTAGClock(config).equals(Problem.WARN))
			hasWarning = true;
		if (checkJTAGDaisyChainUnitsBefore(config).equals(Problem.ERROR))
			hasError = true;
		if (checkJTAGDaisyChainUnitsAfter(config).equals(Problem.ERROR))
			hasError = true;
		if (checkJTAGDaisyChainBitsBefore(config).equals(Problem.ERROR))
			hasError = true;
		if (checkJTAGDaisyChainBitsAfter(config).equals(Problem.ERROR))
			hasError = true;

		Problem result = Problem.OK;
		if (hasWarning)
			result = Problem.WARN;
		if (hasError)
			result = Problem.ERROR;

		return result;
	}

	/**
	 * Check the JTAG clock frequency.
	 * <p>
	 * This method will return {@link Problem#WARN} iff
	 * <ul>
	 * <li>the target interface supports settable clocks</li>
	 * <li>and the bitclock is not set to the default</li>
	 * <li>and the bitclock is greater than 1/4th of the current FCPU</li>
	 * </ul>
	 * In all other cases {@link Problem#OK} is returned.
	 * </p>
	 * 
	 * @param config
	 *            The target configuration to check.
	 * @return {@link Problem} with the current status
	 */
	public static Problem checkJTAGClock(ITargetConfiguration config) {

		// Check if the current configuration actually has a settable clock
		String programmerid = config.getAttribute(ATTR_PROGRAMMER_ID);
		IProgrammer programmer = config.getProgrammer(programmerid);
		int[] clocks = programmer.getTargetInterfaceClockFrequencies();
		if (clocks.length > 0) {

			// OK, the target interface has a selectable clock.
			// Now check if the default is set ( = ""). The warning is
			// inhibited with the default because we don't know what value the
			// default might have.
			String bitclock = config.getAttribute(ATTR_JTAG_CLOCK);
			if (bitclock.length() > 0) {

				// Not the default but an actual value.
				// Finally check if the selected clock is > 1/4th the target FCPU value
				int value = Integer.parseInt(bitclock);
				int targetfcpu = config.getFCPU();
				if (value > targetfcpu / 4) {

					return Problem.WARN;
				}
			}
		}

		// JTAG_CLOCk is valid
		return Problem.OK;
	}

	/**
	 * Check the JTAG daisy chain 'bits before' attribute.
	 * <p>
	 * This method will return {@link Problem#ERROR} iff
	 * <ul>
	 * <li>The current target interface is JTAG</li>
	 * <li>and daisy chaining is enabled</li>
	 * <li>and 'bits before' > 255</li>
	 * </ul>
	 * In all other cases {@link Problem#OK} is returned.
	 * </p>
	 * <p>
	 * Note: The current implementation of AVRDude only accepts instruction bit values < 32. But
	 * this does not seem to be correct because the JTAG protocol accepts an 8-bit field.<br/>
	 * AVaRICE accepts all values but uses only the lower 8 bits.
	 * </p>
	 * 
	 * @param config
	 *            The target configuration to check.
	 * @return {@link Problem} with the current status
	 */
	public static Problem checkJTAGDaisyChainBitsBefore(ITargetConfiguration config) {

		if (!isDaisyChainEnabled(config)) {
			return Problem.OK;
		}

		int bitsbefore = config.getIntegerAttribute(ATTR_DAISYCHAIN_BB);

		return (bitsbefore > 255) ? Problem.ERROR : Problem.OK;
	}

	/**
	 * Check the JTAG daisy chain 'bits after' attribute.
	 * <p>
	 * This method will return {@link Problem#ERROR} iff
	 * <ul>
	 * <li>The current target interface is JTAG</li>
	 * <li>and daisy chaining is enabled</li>
	 * <li>and 'bits after' > 255</li>
	 * </ul>
	 * In all other cases {@link Problem#OK} is returned.
	 * </p>
	 * <p>
	 * Note: The current implementation of AVRDude only accepts instruction bit values < 32. But
	 * this does not seem to be correct because the JTAG protocol accepts an 8-bit field.<br/>
	 * AVaRICE accepts all values but uses only the lower 8 bits.
	 * </p>
	 * 
	 * @param config
	 *            The target configuration to check.
	 * @return {@link Problem} with the current status
	 */
	public static Problem checkJTAGDaisyChainBitsAfter(ITargetConfiguration config) {

		if (!isDaisyChainEnabled(config)) {
			return Problem.OK;
		}

		int bitsafter = config.getIntegerAttribute(ATTR_DAISYCHAIN_BA);

		return (bitsafter > 255) ? Problem.ERROR : Problem.OK;
	}

	/**
	 * Check the JTAG daisy chain 'units before' attribute.
	 * <p>
	 * This method will return {@link Problem#ERROR} iff
	 * <ul>
	 * <li>The current target interface is JTAG</li>
	 * <li>and daisy chaining is enabled</li>
	 * <li>and 'units before' > 'bits before'</li>
	 * </ul>
	 * In all other cases {@link Problem#OK} is returned.
	 * </p>
	 * 
	 * @param config
	 *            The target configuration to check.
	 * @return {@link Problem} with the current status
	 */
	public static Problem checkJTAGDaisyChainUnitsBefore(ITargetConfiguration config) {

		if (!isDaisyChainEnabled(config)) {
			return Problem.OK;
		}

		int unitsbefore = config.getIntegerAttribute(ATTR_DAISYCHAIN_UB);
		int bitsbefore = config.getIntegerAttribute(ATTR_DAISYCHAIN_BB);

		return (unitsbefore > bitsbefore) ? Problem.ERROR : Problem.OK;
	}

	/**
	 * Check the JTAG daisy chain 'units after' attribute.
	 * <p>
	 * This method will return {@link Problem#ERROR} iff
	 * <ul>
	 * <li>The current target interface is JTAG</li>
	 * <li>and daisy chaining is enabled</li>
	 * <li>and 'units after' > 'bits after'</li>
	 * </ul>
	 * In all other cases {@link Problem#OK} is returned.
	 * </p>
	 * 
	 * @param config
	 *            The target configuration to check.
	 * @return {@link Problem} with the current status
	 */
	public static Problem checkJTAGDaisyChainUnitsAfter(ITargetConfiguration config) {

		if (!isDaisyChainEnabled(config)) {
			return Problem.OK;
		}

		int unitsafter = config.getIntegerAttribute(ATTR_DAISYCHAIN_UA);
		int bitsafter = config.getIntegerAttribute(ATTR_DAISYCHAIN_BA);

		return (unitsafter > bitsafter) ? Problem.ERROR : Problem.OK;
	}

	/**
	 * Checks if daisy chain is possible and enabled.
	 * <p>
	 * The implementation checks if the selected programmer is capable of daisy chaining and if the
	 * ATTR_DAISYCHAIN_ENABLED flag is set.
	 * </p>
	 * 
	 * @see #isDaisyChainCapable(ITargetConfiguration)
	 * 
	 * @param config
	 *            The target configuration to check.
	 * @return <code>true</code> if the programmer supports daisy chaining and it is enabled.
	 */
	private static boolean isDaisyChainEnabled(ITargetConfiguration config) {
		String programmerid = config.getAttribute(ATTR_PROGRAMMER_ID);
		IProgrammer programmer = config.getProgrammer(programmerid);

		if (programmer.isDaisyChainCapable()) {
			return config.getBooleanAttribute(ATTR_DAISYCHAIN_ENABLE);
		}

		return false;
	}

}
