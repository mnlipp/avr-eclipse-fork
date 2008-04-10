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
package de.innot.avreclipse.ui.propertypages;

import java.util.List;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import de.innot.avreclipse.core.preferences.AVRProjectProperties;

/**
 * The AVRDude property page.
 * <p>
 * This page is the container for all avrdude related tabs.
 * </p>
 * <p>
 * For make the avrdude settings more transparent for the user, this page adds a
 * avrdude command line preview below the tabs. Tabs based on this page should
 * call {@link #updatePreview(AVRProjectProperties)} whenever something is
 * modified in the current target properties. The preview will then be updated
 * accordingly.
 * </p>
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.2
 * 
 */
public class PageAVRDude extends AbstractAVRPage {

	/** The preview <code>Text</code> Control */
	private Text fPreviewText;

	/**
	 * Set up the page for avrdude tabs.
	 * <p>
	 * The page is set up as a <code>SashForm</code> with two areas: the main
	 * part for the normal tabs and a avrdude command line preview box below it.
	 * </p>
	 */
	@Override
	public void createWidgets(Composite c) {

		// Create the sash form
		SashForm sashform = new SashForm(c, SWT.VERTICAL);
		sashform.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		sashform.setLayout(new GridLayout(1, false));

		// Let the superclass draw the tabs
		super.createWidgets(sashform);

		// Now add the avrdude command preview box
		// This is a group...
		Group group = new Group(sashform, SWT.NONE);
		group.setLayoutData(new GridData(SWT.FILL, SWT.NONE, true, false));
		group.setLayout(new GridLayout(1, false));
		group.setText("avrdude command line preview");

		// ...with one Text control in it
		fPreviewText = new Text(group, SWT.MULTI | SWT.WRAP | SWT.V_SCROLL);
		fPreviewText.setEditable(false);
		GridData gd2 = new GridData(SWT.FILL, SWT.FILL, true, true);
		fPreviewText.setLayoutData(gd2);

		// Set the weights of the SashForm.
		// The tabs get 80%, while the preview has 20%.
		// These values have been determined empirically and look good on my
		// system.
		// TODO maybe the user modified setting can be saved when the page is
		// closed.
		sashform.setWeights(new int[] { 80, 20 });

	}

	/**
	 * Update the avrdude command line preview.
	 * 
	 * @param props
	 *            The <code>AVRProjectProperties</code> for which to display
	 *            the preview
	 */
	public void updatePreview(AVRProjectProperties props) {

		// Don't do anything until this page is drawn.
		if (fPreviewText == null) {
			return;
		}
		
		StringBuffer sb = new StringBuffer("avrdude ");

		// Get the standard AVRDude arguments as defined in the given
		// properties.
		List<String> allargs = props.getAVRDudeArguments();

		for (String arg : allargs) {
			sb.append(arg);
			sb.append(" ");
		}

		sb.append("\n");
		// Get the current configuration...
		IConfiguration buildcfg = ManagedBuildManager
				.getConfigurationForDescription(getResDesc().getConfiguration());

		// ...and all action arguments for the current configuration
		List<String> allactionargs = props.getAVRDudeActionArguments(buildcfg);

		// append all actions, one per line for better readabilty
		for (String arg : allactionargs) {
			sb.append(arg);
			sb.append("\n");
		}

		fPreviewText.setText(sb.toString());

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.ui.newui.AbstractPage#isSingle()
	 */
	@Override
	protected boolean isSingle() {

		// This page uses multiple tabs

		return false;
	}

}
