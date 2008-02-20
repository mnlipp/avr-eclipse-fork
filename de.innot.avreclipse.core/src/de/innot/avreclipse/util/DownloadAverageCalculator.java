/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.util;

/**
 * Utility class to calculate an average download rate.
 * <p>
 * Objects of this class calculate the average rate of a download. To smooth the
 * rate, the download rate of the last <code>n</code> blocks is averaged. The
 * sample size can be set and has a default of 20.
 * </p>
 * <p>
 * The time of the instantiation is taken as the start time for the download, so
 * it should be as close as possible to the actual start of the download.
 * </p>
 * Usage example with a sample size of 50:
 * 
 * <pre>
 * DownloadAverageCalculator dac = new DownloadAverageCalculator(50);
 * while (readbytes) {
 * 	int rate = dac.getRate(bytesread);
 * }
 * </pre>
 * 
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class DownloadAverageCalculator {

	private final static int DEFAULT_SAMPLE_SIZE = 20;

	// Ring buffer for the last rates
	private final float[] samples;

	// Index for the ring buffer
	private int samplesindex;

	// flag for the state of the ringbuffer
	private boolean samplesfull;

	// the start time of the download
	private long starttime;

	// the last time the getRate() method was called
	private long lasttime;

	/**
	 * Create a new DownloadAverageCalculator with the default sample size.
	 */
	public DownloadAverageCalculator() {
		this(DEFAULT_SAMPLE_SIZE);
	}

	/**
	 * Create a new DownloadAverageCalculator with the given sample size.
	 * <p>
	 * While high sample sizes will result in a smoother rate, they will also
	 * cause the average rate to lag behind the real download rate.
	 * </p>
	 * 
	 * @param samplesize
	 *            Size of the sample buffer.
	 */
	public DownloadAverageCalculator(final int samplesize) {
		samples = new float[samplesize];
		samplesindex = 0;
		samplesfull = false;

		starttime = System.currentTimeMillis();
		lasttime = starttime;
	}

	/**
	 * Return the current average download rate in <code>bytes per second</code>.
	 * <p>
	 * The returned rate is the average of the last <code>samplesize</code>
	 * rates.
	 * </p>
	 * 
	 * @param bytesread
	 *            The number of bytes read since the last call to this method
	 *            (or since instantiation)
	 * @return int with the current download rate
	 */
	public int getCurrentRate(final int bytesread) {
		long currenttime = System.currentTimeMillis();
		long timelastblock = (currenttime - lasttime);
		if (timelastblock == 0) {
			timelastblock = 1;
		}
		lasttime = currenttime;

		// calculate bytes per ms and add to the averaging array
		float currentrate = (float) bytesread / timelastblock;
		samples[samplesindex++] = currentrate;
		if (samplesindex == samples.length) {
			samplesindex = 0;
			samplesfull = true;
		}

		// now calculate the average for all rates in the sample buffer
		// if the buffer has not been filled yet, only the stored rates
		// are used.
		float sumrates = 0;
		int i;
		for (i = 0; i < (samplesfull ? samples.length : samplesindex); i++) {
			sumrates += samples[i];
		}
		float averagerate = sumrates / i;

		// convert bytes per ms to bytes per second
		return (int) (averagerate * 1000);

	}

}
