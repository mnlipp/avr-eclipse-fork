/*******************************************************************************
 * 
 * Copyright (c) 2008 Thomas Holland (thomas@innot.de) and others
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

package de.innot.avreclipse.debug.ui;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;
import org.junit.Test;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class TestAVRGDBImages {

	private final static String[]	ALL_IMAGES	= new String[] { AVRGDBImages.TAB_DEBUGGER_IMG,
			AVRGDBImages.TAB_STARTUP_IMG		};

	/**
	 * Test method for {@link de.innot.avreclipse.debug.ui.AVRGDBImages#getImageRegistry()}.
	 */
	@Test
	public void testGetImageRegistry() {
		ImageRegistry registry = AVRGDBImages.getImageRegistry();
		assertNotNull("No ImageRegistry created", registry);
	}

	/**
	 * Test method for
	 * {@link de.innot.avreclipse.debug.ui.AVRGDBImages#getImageDescriptor(java.lang.String)}.
	 */
	@Test
	public void testGetImageDescriptor() {

		ImageDescriptor missingImgDesc = ImageDescriptor.getMissingImageDescriptor();

		for (String imgkey : ALL_IMAGES) {
			ImageDescriptor desc = AVRGDBImages.getImageDescriptor(imgkey);
			assertNotNull(imgkey + " ImageDescriptor returned null", desc);
			assertNotSame(imgkey + " replaced by MissingImage", missingImgDesc, desc);
		}
	}

	/**
	 * Test method for {@link de.innot.avreclipse.debug.ui.AVRGDBImages#getImage(java.lang.String)}.
	 */
	@Test
	public void testGetImage() {

		for (String imgkey : ALL_IMAGES) {
			Image img = AVRGDBImages.getImage(imgkey);
			assertNotNull(imgkey + " Image returned null", img);
		}
	}

}
