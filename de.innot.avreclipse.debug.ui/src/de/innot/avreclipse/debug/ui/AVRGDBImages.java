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

package de.innot.avreclipse.debug.ui;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.swt.graphics.Image;

/**
 * Utility class to use the jface <code>ImageRegistry</code> to manage the images used by this
 * plugin, so we don't have to allocate and dispose the images all the time.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class AVRGDBImages {

	/** The image Registry */
	private static ImageRegistry	fImageRegistry;

	/** Key for the Debugger Tab image */
	public final static String		TAB_DEBUGGER_IMG	= "tab_debugger";
	private final static String		TAB_DEBUGGER_SRC	= "icons/eview16/debugger_tab.gif";

	/** Key for the Startup Tab image */
	public final static String		TAB_STARTUP_IMG		= "tab_startup";
	private final static String		TAB_STARTUP_SRC		= "icons/eview16/startup_tab.gif";

	/** Key for the GDBServer Tab image */
	public final static String		TAB_GDBSERVER_IMG	= "tab_gdbserver";
	private final static String		TAB_GDBSERVER_SRC	= "icons/eview16/startup_tab.gif";

	/**
	 * @return the <code>Image</code> identified by the given key, or <code>null</code> if it does
	 *         not exist.
	 */
	public static Image getImage(String key) {
		return getImageRegistry().get(key);
	}

	/**
	 * @return the <code>ImageDescriptor</code> identified by the given key, or <code>null</code> if
	 *         it does not exist.
	 */
	public static ImageDescriptor getImageDescriptor(String key) {
		return getImageRegistry().getDescriptor(key);
	}

	/**
	 * @return the ImageRegistry for this plugiin
	 */
	public static synchronized ImageRegistry getImageRegistry() {
		if (fImageRegistry == null) {
			fImageRegistry = new ImageRegistry(AVRGDBUIPlugin.getStandardDisplay());
			loadImages();
		}
		return fImageRegistry;
	}

	/**
	 * Register all images with our <code>ImageRegistry</code>
	 * 
	 */
	private final static void loadImages() {

		loadImageToRegistry(TAB_DEBUGGER_IMG, TAB_DEBUGGER_SRC);
		loadImageToRegistry(TAB_STARTUP_IMG, TAB_STARTUP_SRC);
		loadImageToRegistry(TAB_GDBSERVER_IMG, TAB_GDBSERVER_SRC);
	}

	/**
	 * Load an Image into the registry.
	 * <p>
	 * If the given path is invalid, the default "missing image" is registered instead.
	 * </p>
	 * 
	 * @param key
	 *            The registry key for the image
	 * @param path
	 *            The path to the image. This path is relative to the root of this plugin.
	 */
	private final static void loadImageToRegistry(String key, String path) {
		ImageDescriptor desc = AVRGDBUIPlugin.getImageDescriptor(path);
		if (desc == null) {
			desc = ImageDescriptor.getMissingImageDescriptor();
		}
		fImageRegistry.put(key, desc);
	}

}
