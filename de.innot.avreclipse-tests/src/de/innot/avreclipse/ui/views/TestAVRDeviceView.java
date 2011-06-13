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
package de.innot.avreclipse.ui.views;

import static org.junit.Assert.assertNotNull;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.junit.Test;

/**
 * @author Thomas
 * 
 */
public class TestAVRDeviceView {

	@Test
	public final void testShowView() throws Exception {
		IViewPart view = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage().showView(
						"de.innot.avreclipse.views.AVRDeviceView");
		assertNotNull(view);
	}
}
