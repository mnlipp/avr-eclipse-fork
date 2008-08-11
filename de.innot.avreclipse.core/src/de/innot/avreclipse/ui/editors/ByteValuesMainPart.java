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
package de.innot.avreclipse.ui.editors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.IPartSelectionListener;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.Section;

import de.innot.avreclipse.core.toolinfo.fuses.BitFieldDescription;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.ui.dialogs.ByteValuesEditorDialog;

/**
 * /** A <code>IFormPart</code> that can edit all BitFields of a <code>ByteValues</code> source
 * object.
 * <p>
 * This class automatically creates a <code>Composite</code> and adds it to the parent. The
 * composite is given a <code>ColumnLayout</code> and then all {@link BitFieldEditorSectionPart}s
 * are added. See {@link ByteValuesEditorDialog#optimizeSize()} for a discussion about the problems
 * of the <code>ColumnLayout</code> in unconstrained layouts
 * </p>
 * <p>
 * After the class has been instantiated it must be added to a <code>IManagedForm</code> to
 * participate in the lifecycle management of the managed form.
 * 
 * <pre>
 *     Composite parent = ...
 *     ByteValues bytevalues = ...
 *     IManagedForm managedForm = ...
 * 
 *     IFormPart part = new ByteValuesMainPart(parent, bytevalues);
 *     managedForm.addPart(part);
 * </pre>
 * 
 * </p>
 * <p>
 * This class implements the {@link IFormPart} interface to participate in the lifecycle management
 * of a managed form. To set the value of all BitFields use
 * 
 * <pre>
 *     ByteValues bytevalues = ...
 *     managedForm.setInput(bytevalues);
 * </pre>
 * 
 * The <code>ByteValues</code> passed to the managedForm is the model for this
 * <code>SectionPart</code>. It will remain untouched until the {@link #commit(boolean)} method
 * is called, which will write the user modified comment back to the <code>ByteValues</code>
 * model.
 * 
 * <pre>
 *     managedForm.commit(...);
 * </pre>
 * 
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class ByteValuesMainPart extends AbstractFormPart implements IPartSelectionListener {

	/** The parent composite. */
	private final Composite			fParent;

	/**
	 * List of all child <code>Sections</code>. Used to clear the parent composite for a redraw
	 * and to determine the widest section.
	 */
	private final List<Control>		fControls	= new ArrayList<Control>();

	/**
	 * List of all child <code>IFormPart</code>. Used to clear to ManagedForm for a redraw.
	 */
	private final List<IFormPart>	fPFormParts	= new ArrayList<IFormPart>();

	/**
	 * The model for this <code>IFormPart</code>. Will not be modified in this class, but in the
	 * {@link #commit(boolean)} method of the child <code>SectionPart</code>s.
	 */
	private ByteValues				fByteValues;

	/**
	 * Create a new <code>IFormPart</code> to handle all BitFields in a <code>ByteValues</code>.
	 * <p>
	 * The <code>ByteValues</code> model is required to determine all
	 * <code>BitFieldDescriptions</code> rendered in this part. This is required because
	 * {@link #initialize(IManagedForm)} is called before {@link #setFormInput(Object)} and this
	 * class needs to now the ByteValues structure to render itself.
	 * </p>
	 * <p>
	 * The actual content of the <code>ByteValues</code> is not used until the
	 * <code>setInput(ByteValues)</code> method of the ManagedForm is called.
	 * </p>
	 * 
	 * @param parent
	 *            the parent. Layout of the parent will be set to <code>ColumnLayout</code>.
	 * @param model
	 *            the <code>ByteValues</code> to use for the first rendering of this part.
	 */
	public ByteValuesMainPart(Composite parent, ByteValues model) {

		fParent = parent;
		fByteValues = model;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);

		ColumnLayout layout = new ColumnLayout();
		layout.horizontalSpacing = 10;
		fParent.setLayout(layout);

		initForm(form);
	}

	/**
	 * (Re)draw the Form.
	 * 
	 * @param form
	 *            <code>IManagedForm</code>
	 */
	private void initForm(IManagedForm form) {

		List<BitFieldDescription> allbfds = fByteValues.getBitfieldDescriptions();

		// Sort the bitfield descriptions according to their name
		// The ColumnLayout will mess this up in a two column layout but this is better than
		// nothing.
		Collections.sort(allbfds, new Comparator<BitFieldDescription>() {
			public int compare(BitFieldDescription o1, BitFieldDescription o2) {
				String name1 = o1.getName();
				String name2 = o2.getName();
				return name1.compareTo(name2);
			}
		});

		// Now go though all BitFieldDesriptions, create SectionParts for them, add them to the
		// ManagedForm and remember them for later access.
		for (BitFieldDescription bfd : allbfds) {
			SectionPart part = new BitFieldEditorSectionPart(fParent, form.getToolkit(),
					Section.TITLE_BAR, bfd);
			form.addPart(part);
			fPFormParts.add(part);
			fControls.add(part.getSection());
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
	 */
	@Override
	public void refresh() {
		// No need to do anything because this form part does not access the ByteValues model
		// directly. The BitField section parts get refreshed directly from the ManagedForm.
		super.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#setFormInput(java.lang.Object)
	 */
	@Override
	public boolean setFormInput(Object input) {
		if (input instanceof ByteValues) {
			fByteValues = (ByteValues) input;

			// clear this form if it has been initialized before for a different MCU type.
			clearForm();

			initForm(getManagedForm());

			getManagedForm().reflow(true);
			return true;
		}

		// Input not applicable
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IPartSelectionListener#selectionChanged(org.eclipse.ui.forms.IFormPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		// This is (indirectly) called by the "Change MCU Type" Action.

		// Clear the form and redraw it for the new type
		clearForm();
		initForm(getManagedForm());

		// and inform the new BitField sections about the input model.
		for (IFormPart sectionpart : fPFormParts) {
			sectionpart.setFormInput(fByteValues);
		}

		// Update the layout
		getManagedForm().reflow(true);
	}

	/**
	 * Clear the form by removing all child <code>SectionPart</code>s from the parent composite
	 * and all <code>IFormParts</code> from the parent ManagedForm.
	 * <p>
	 * Used to prepare this form for a redraw for a different MCU.
	 * </p>
	 * 
	 */
	private void clearForm() {
		// First remove the parts from the form
		IManagedForm form = getManagedForm();
		for (IFormPart part : fPFormParts) {
			form.removePart(part);
		}
		fPFormParts.clear();

		// Then remove the sections from the composite
		for (Control control : fControls) {
			control.dispose();
		}
		fControls.clear();
	}

	/**
	 * @return The width of the widest child BitField section.
	 */
	public int getMaxWidth() {
		int maxwidth = 0;

		for (Control control : fControls) {
			Point cSize = control.computeSize(SWT.DEFAULT, SWT.DEFAULT, true);
			if (cSize.x > maxwidth) {
				maxwidth = cSize.x;
			}
		}

		return maxwidth;
	}

}
