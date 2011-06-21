/*******************************************************************************
 * Copyright (C) 2011, Chris Aniszczyk <caniszczyk@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package de.innot.avreclipse.tests.ui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCTabItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(SWTBotJunit4ClassRunner.class)
public class AVRDeviceViewerTest {

	private static final SWTWorkbenchBot bot = new SWTWorkbenchBot();

	@BeforeClass
	public static void closeWelcomePage() {
		try {
			bot.viewByTitle("Welcome").close();
		} catch (WidgetNotFoundException e) {
			// somebody else probably closed it, lets not feel bad about it.
		}
	}

	@After
	public void sleep() {
		// bot.sleep(2000);
	}

	@Test
	public void TestAVRDeviceViewer() {

		// Open the view via the views menu.

		// Show the Other... view selection dialog
		bot.menu("Window").menu("Show View").menu("Other...").click();
		SWTBotShell openViewShell = bot.shell("Show View");
		openViewShell.activate();

		// and select the AVR Device Explorer from the dialog
		bot.tree().getTreeItem("AVR").expand().getNode("AVR Device Explorer")
				.select();
		bot.button("OK").click();

		// The View is open and we can access it
		SWTBotView sbv = bot
				.viewById("de.innot.avreclipse.views.AVRDeviceView");
		assertTrue("AVRDeviceView missing", sbv.isActive());

		// Check that the source breadrumb works
		sbv.bot().comboBox().setSelection("ATmega16");
		assertEquals("Unexpected io.h source", "avr/iom16.h", sbv.bot().text(0)
				.getText());

		sbv.bot().comboBox().setSelection("ATmega1280");
		assertEquals("Unexpected io.h source/1", "avr/iom1280.h", sbv.bot()
				.text(0).getText());
		assertEquals("Unexpected io.h source/2", "avr/iomxx0_1.h", sbv.bot()
				.text(1).getText());

		// Check the three tabs: Registers - Ports - Interrupts
		// They all should contain some content
		//
		// Note that the Tabs might change for a different
		// DeviceDescriptionProvider, breaking these tests.
		SWTBotCTabItem item = sbv.bot().cTabItem(0);
		assertEquals("First tab not 'Registers'", "Registers", item.getText());
		item.activate();
		assertNotNull("Tab 'Registers' has no tree content", sbv.bot().tree());

		item = sbv.bot().cTabItem(1);
		assertEquals("First tab not 'Ports'", "Ports", item.getText());
		item.activate();
		assertNotNull("Tab 'Ports' has no tree content", sbv.bot().tree());

		item = sbv.bot().cTabItem(2);
		assertEquals("First tab not 'Interrupts'", "Interrupts", item.getText());
		item.activate();
		assertNotNull("Tab 'Interrupts' has no tree content", sbv.bot().tree());

	}

}
