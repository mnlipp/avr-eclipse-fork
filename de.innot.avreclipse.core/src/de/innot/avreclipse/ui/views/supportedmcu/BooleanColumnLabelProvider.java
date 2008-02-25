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
package de.innot.avreclipse.ui.views.supportedmcu;

import org.eclipse.jface.viewers.OwnerDrawLabelProvider;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.TableItem;

import de.innot.avreclipse.AVRPluginActivator;

/**
 * @author Thomas Holland
 * 
 */
public class BooleanColumnLabelProvider extends OwnerDrawLabelProvider {

	private MCUProviderEnum fProvider = null;

	private Image fYesImage = null;
	private Image fNoImage = null;

	public BooleanColumnLabelProvider(MCUProviderEnum provider) {
		fProvider = provider;

		// Create the Images if they have not been created yet by another
		// instantiation
		if (fYesImage == null) {
			fYesImage = AVRPluginActivator.getImageDescriptor("icons/viewer16/yes.png")
			        .createImage();
		}
		if (fNoImage == null) {
			fNoImage = AVRPluginActivator.getImageDescriptor("icons/viewer16/no.png").createImage();
		}
	}

	public void dispose() {
		if (fYesImage != null) {
			fYesImage.dispose();
		}
		if (fNoImage != null) {
			fNoImage.dispose();
		}
	}

	@Override
    protected void measure(Event event, Object element) {
		// Set the bounds of the icon(s)
		event.width = fYesImage.getBounds().width;
		event.height = fYesImage.getBounds().height;
    }

	@Override
    protected void paint(Event event, Object element) {
		
		String mcuid = (String) element;
		
		Image img = fProvider.hasMCU(mcuid) ? fYesImage : fNoImage;

		if (img != null) {
			Rectangle bounds = ((TableItem) event.item)
					.getBounds(event.index);
			Rectangle imgBounds = img.getBounds();
			bounds.width /= 2;
			bounds.width -= imgBounds.width / 2;
			bounds.height /= 2;
			bounds.height -= imgBounds.height / 2;

			int x = bounds.width > 0 ? bounds.x + bounds.width : bounds.x;
			int y = bounds.height > 0 ? bounds.y + bounds.height : bounds.y;

			event.gc.drawImage(img, x, y);
		}	    
    }

}
