package de.innot.avreclipse.debug.ui;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class AVRGDBUIPlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String		PLUGIN_ID	= "de.innot.avreclipse.debug.ui";

	// The shared instance
	private static AVRGDBUIPlugin	plugin;

	/**
	 * The constructor
	 */
	public AVRGDBUIPlugin() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
	 */
	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static AVRGDBUIPlugin getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * Returns the standard display to be used. The method first checks, if the thread calling this
	 * method has an associated display. If so, this display is returned. Otherwise the method
	 * returns the default display.
	 * 
	 */
	public static Display getStandardDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}

	/**
	 * @return The currently active Shell
	 */
	public static Shell getActiveShell() {
		return getStandardDisplay().getActiveShell();
	}

	/**
	 * Log the given status and print it to the err Stream if debugging is enabled.
	 * 
	 * @param status
	 */
	public static void log(IStatus status) {
		ILog log = getDefault().getLog();
		if (status.getSeverity() >= Status.WARNING) {
			log.log(status);
		}
		if (getDefault().isDebugging()) {
			System.err.print(PLUGIN_ID + ": " + status.getMessage());
			if (status.getCode() != 0) {
				System.err.print("(" + status.getCode() + ")");
			}
			System.out.println("");
			if (status.getException() != null) {
				status.getException().printStackTrace();
			}
		}
	}

}
