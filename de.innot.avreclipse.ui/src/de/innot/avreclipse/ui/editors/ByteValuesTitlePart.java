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
package de.innot.avreclipse.ui.editors;

import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import de.innot.avreclipse.core.toolinfo.fuses.ByteValueChangeEvent;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.toolinfo.fuses.IByteValuesChangeListener;
import de.innot.avreclipse.core.util.AVRMCUidConverter;

/**
 * A simple <code>IFormPart</code> to set the title of the managed form according to the current
 * <code>ByteValues</code> MCU id.
 * <p>
 * This class is implemented as an <code>IFormPart</code> to participate in the life cycle
 * management of the form, listening to any change in the source <code>ByteValues</code> model.
 * </p>
 * <p>
 * It does not modify the <code>ByteValues</code> model.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class ByteValuesTitlePart extends AbstractFormPart implements IByteValuesChangeListener {

	/** Reference to the current <code>ByteValues</code>. */
	private ByteValues	fByteValues;

	/**
	 * The current MCU of the ByteValues. If this is different than {@link #fLastCleanMCU} then this
	 * part is stale and needs to be redrawn on the next refresh.
	 */
	private String		fCurrentMCU	= null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#dispose()
	 */
	@Override
	public void dispose() {
		if (fByteValues != null) {
			fByteValues.removeChangeListener(this);
		}
		super.dispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#setFormInput(java.lang.Object)
	 */
	@Override
	public boolean setFormInput(Object input) {
		if (!(input instanceof ByteValues)) {
			return false;
		}

		fByteValues = (ByteValues) input;
		fByteValues.addChangeListener(this);

		refresh();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
	 */
	@Override
	public void refresh() {

		// This is called when the source is stale, which is probably cause by a change to the MCU
		// id. Update the form title header.
		setTitle();

		fCurrentMCU = fByteValues.getMCUId();

		super.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#isStale()
	 */
	@Override
	public boolean isStale() {
		if (!fByteValues.getMCUId().equals(fCurrentMCU)) {
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.fuses.IByteValuesChangeListener#byteValuesChanged(de.innot.avreclipse.core.toolinfo.fuses.ByteValueChangeEvent[])
	 */
	public void byteValuesChanged(ByteValueChangeEvent[] events) {
		// go through all events and if any event changes the MCU,
		// then mark this part as stale.
		for (ByteValueChangeEvent event : events) {
			if (event.name.equals(ByteValues.MCU_CHANGE_EVENT)) {
				// Our stale state might have changed, depending on the new MCU value.
				// Inform the parent ManagedForm - it will call our isStale() implementation to get
				// the actual state and later refresh() if we are actually stale.
				getManagedForm().staleStateChanged();
			}
		}
	}

	/**
	 * Change the header text of this form to show the current MCU type.
	 * 
	 */
	private void setTitle() {

		String newtitle = "";

		if (fByteValues != null) {
			newtitle = AVRMCUidConverter.id2name(fByteValues.getMCUId()) + " - "
					+ fByteValues.getType().toString() + " settings";
		}

		ScrolledForm form = getManagedForm().getForm();
		form.setText(newtitle);

	}

}
