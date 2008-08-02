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
package de.innot.avreclipse.ui.dialogs;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.FormDialog;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

import de.innot.avreclipse.core.toolinfo.fuses.BitFieldDescription;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
import de.innot.avreclipse.core.util.AVRMCUidConverter;
import de.innot.avreclipse.ui.editors.BitFieldEditorSectionPart;

/**
 * A Fuse Byte Editor as a Dialog.
 * <p>
 * This Dialog is called from the AVRDude Fuses Property Dialog to optionally edit fuse byte values
 * in the same fashion as the Editor for .fuses files.<br>
 * It will show all BitFields of the given ByteValues as {@link BitFieldEditorSectionPart}s for
 * editing.
 * </p>
 * To use this Dialog instantiate it with the <code>ByteValues</code> to edit and call
 * {@link #open()}. While this Dialog will only commit any modifications to the given
 * <code>ByteValues</code> if the <em>OK</em> was pressed, callers should not depend on this and
 * should discard the <code>ByteValues</code> object after the <em>Cancel</em> button has been
 * pressed.
 * </p>
 * <p>
 * A safe access pattern could look like this:
 * 
 * <pre>
 *     	ByteValues values = ....
 * 	   	ByteValuesEditorDialog dialog = new ByteValuesEditorDialog(getShell(), values);
 *     	dialog.create();
 *  	if (dialog.open() == Dialog.OK) {
 *  		ByteValues newvalues = dialog.getByteValues();
 *  		....
 *  	}
 * </pre>
 * 
 * <p>
 * This dialog has a tendency to grab more screen space than actually needed to display the content
 * without scrollbars. The reason is under investigation and might be a bug (or feature) in the
 * <code>ColumnLayout</code> used for this dialog. If this behaviour is not desired then a fixed
 * screen size can be set by calling <code>dialog.getShell().setSize(width, height)</code> before
 * the <code>open()</code> call.
 * </p>
 * <p>
 * Note: there is currently an unresolved bug that when the alphabetically first BitField shown is
 * of a Radio Button type and its value is undefined (<code>-1</code>) then the first radio
 * button gets set to true anyway. This is because SWT (or Windows) will set the first radio button
 * to true whenever the control gets the focus, and the first control on the screen will always get
 * the focus.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class ByteValuesEditorDialog extends FormDialog {

	/**
	 * The <code>ByteValues</code> this dialog works with. Its content will only be modified when
	 * the <em>OK</em> button is pressed.
	 */
	private final ByteValues	fByteValues;

	private IManagedForm		fForm;

	/**
	 * Instantiate a new Dialog.
	 * <p>
	 * Note that the Dialog will not be shown until the {@link #open()} method is called.
	 * </p>
	 * 
	 * @param parentShell
	 *            <code>Shell</code> to associate this Dialog with, so that it always stays on top
	 *            of the given Shell.
	 * @param values
	 *            The <code>ByteValues</code> to edit.
	 */
	public ByteValuesEditorDialog(Shell parentShell, ByteValues values) {
		super(parentShell);

		fByteValues = values;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		// Only commit the changes if any have been made and the OK button was pressed.
		if (fForm.isDirty()) {
			fForm.commit(false);
		}
		super.okPressed();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.FormDialog#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {

		// The general setup has already been done by the FormDialog superclass.
		// We only set the title for the dialog, grab the Form and the toolkit and
		// let fillBody() add the rest of the content.
		// Once it is finished we can tell the form about the ByteValues we are supposed to edit and
		// let it update the layout to cover changes in the width of the combos.

		this.getShell().setText(fByteValues.getType().toString() + " Editor");

		fForm = managedForm;

		ScrolledForm fRootForm = managedForm.getForm();
		FormToolkit toolkit = managedForm.getToolkit();
		fRootForm.setText(AVRMCUidConverter.id2name(fByteValues.getMCUId()) + " - "
				+ fByteValues.getType().toString() + " settings");
		fillBody(managedForm, toolkit);

		managedForm.setInput(fByteValues);
		managedForm.reflow(true);
		// managedForm.refresh();

	}

	/**
	 * Fill the managed Form with the SectionParts for all BitFields.
	 * 
	 * @param managedForm
	 * @param toolkit
	 */
	private void fillBody(IManagedForm managedForm, FormToolkit toolkit) {

		Composite body = managedForm.getForm().getBody();
		ColumnLayout layout = new ColumnLayout();
		layout.horizontalSpacing = 10;
		body.setLayout(layout);

		List<BitFieldDescription> allbfds = fByteValues.getBitfieldDescriptions();

		// Sort the bitfield descriptions according to their name
		Collections.sort(allbfds, new Comparator<BitFieldDescription>() {
			public int compare(BitFieldDescription o1, BitFieldDescription o2) {
				String name1 = o1.getName();
				String name2 = o2.getName();
				return name1.compareTo(name2);
			}
		});

		// Now go though all BitFieldDesriptions, create SectionParts for them and add them to the
		// correct client.
		for (BitFieldDescription bfd : allbfds) {
			Section section = toolkit.createSection(body, Section.TITLE_BAR);
			section.setLayoutData(new ColumnLayoutData(250));
			section.setText(bfd.getName() + " - " + bfd.getDescription());

			IFormPart part = new BitFieldEditorSectionPart(section, bfd);
			managedForm.addPart(part);

		}
	}

	/**
	 * Get the <code>ByteValues</code> this Dialog works on.
	 * 
	 * @return
	 */
	public ByteValues getByteValues() {
		// TODO: Do we really need this?
		// This is probably redundant, because we return a reference to an object that we got from
		// the caller. So unless the Dialog object has been passed around the caller should still
		// have the reference himself.
		// (This code is a leftover from when the Dialog would make a copy of the ByteValues. But
		// this is not a good idea because the ByteValues could be a subclass, like FileByteValues
		// and copying it would change the type - I know, stupid design, but I work on it some other
		// time.)
		return fByteValues;
	}

}
