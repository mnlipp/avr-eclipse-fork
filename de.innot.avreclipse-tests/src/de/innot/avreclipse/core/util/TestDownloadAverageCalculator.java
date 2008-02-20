package de.innot.avreclipse.core.util;

import static org.junit.Assert.*;

import org.junit.Test;

import de.innot.avreclipse.util.DownloadAverageCalculator;

public class TestDownloadAverageCalculator {

	@Test
	public synchronized void testGetCurrentRate() throws InterruptedException {

		// to improve accuracy first check the overhead from the Thread.sleep() method
		long starttime = System.currentTimeMillis();
		Thread.sleep(100);
		long endtime = System.currentTimeMillis();
		int overhead = (int)(endtime - starttime -100);

		DownloadAverageCalculator dac = new DownloadAverageCalculator(10);
		assertNotNull(dac);
		
		// First test with a approx rate of 1 byte per ms (= 1000 per second)
		Thread.sleep(100- overhead);
		int rate = dac.getCurrentRate(100);
		assertTrue("1. Downloadrate (" + rate + ") not within expected limits (900<rate<1100)",
		        (rate > 900) && (rate < 1100));

		Thread.sleep(200-overhead);
		rate = dac.getCurrentRate(200);
		assertTrue("2. Downloadrate (" + rate + ") not within expected limits (900<rate<1100)",
		        (rate > 900) && (rate < 1100));

		Thread.sleep(300-overhead);
		rate = dac.getCurrentRate(300);
		assertTrue("3. Downloadrate (" + rate + ") not within expected limits (900<rate<1100)",
		        (rate > 900) && (rate < 1100));

		// now fill up the ringbuffer
		int lastrate = rate;
		for (int i = 0; i < 20; i++) {
			Thread.sleep(100 - overhead);
			int currentrate = dac.getCurrentRate(100);
			float difference = difference(currentrate, lastrate);
			assertTrue("Intermediate Downloadrate(" + i + ":" + currentrate
			        + ") not within 5% of previous rate (" + rate + ")", difference < 5);
			lastrate = currentrate;
		}

		// Test for null value, new rate should be smaller than previous rate
		rate = dac.getCurrentRate(0);
		Thread.sleep(10);
		int newrate = dac.getCurrentRate(0);
		assertTrue("Downloadrate not decreasing for empty reads", newrate < rate);
	}

	@Test
	public synchronized void testTestInternals() {
		// do a quick test if the difference method is OK
		assertTrue(difference(90, 99) == 10);
		assertTrue(difference(100, 90) == 10);

	}

	private int difference(int previous, int current) {
		int percent = current *100 / previous;
		int difference = Math.abs(percent - 100);
		return difference;
	}
}
