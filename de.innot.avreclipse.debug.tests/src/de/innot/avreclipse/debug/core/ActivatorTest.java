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

package de.innot.avreclipse.debug.core;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.junit.Test;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class ActivatorTest {

	/**
	 * Test method for {@link de.innot.avreclipse.debug.core.AVRDebugPlugin#getDefault()}.
	 */
	@Test
	public void testGetDefault() {
		AVRDebugPlugin plugin = AVRDebugPlugin.getDefault();
		assertNotNull(plugin);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.debug.core.AVRDebugPlugin#log(org.eclipse.core.runtime.IStatus)}.
	 */
	@Test
	public void testLog() {

		final List<IStatus> statuslog = new ArrayList<IStatus>();
		ILog log = AVRDebugPlugin.getDefault().getLog();
		log.addLogListener(new ILogListener() {

			public void logging(IStatus status, String plugin) {
				if (AVRDebugPlugin.PLUGIN_ID.equals(plugin)) {
					statuslog.add(status);
				}
			}
		});

		String message = "warning logentry by unit test";
		IStatus status = new Status(IStatus.WARNING, AVRDebugPlugin.PLUGIN_ID, message, null);
		AVRDebugPlugin.log(status);
		assertTrue("Warning log entry not written", statuslog.size() == 1);

		message = "ok logentry by unit test";
		status = new Status(IStatus.OK, AVRDebugPlugin.PLUGIN_ID, message, null);
		AVRDebugPlugin.log(status);
		assertTrue("OK log entry written", statuslog.size() == 1);

	}

}
