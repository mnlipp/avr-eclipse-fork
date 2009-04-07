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

package de.innot.avreclipse.ui.editors.targets;

import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Test;

import de.innot.avreclipse.core.targets.tools.AvariceTool;
import de.innot.avreclipse.core.targets.tools.AvrdudeTool;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TestSettingsExtensionManager {

	private SettingsExtensionManager	manager;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		manager = SettingsExtensionManager.getDefault();
		assertNotNull("No toolmanager", manager);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.ui.editors.targets.SettingsExtensionManager#getSettingsPartForTool(java.lang.String)}
	 * .
	 */
	@Test
	public void testGetSettingsPartForTool() {
		ITCEditorPart avarice = manager.getSettingsPartForTool(AvariceTool.ID);
		assertNotNull("Could not load avarice settings part", avarice);

		ITCEditorPart avrdude = manager.getSettingsPartForTool(AvrdudeTool.ID);
		assertNotNull("Could not load avrdude settings part", avrdude);
	}

}
