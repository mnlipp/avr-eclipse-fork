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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;

/**
 * A <code>SectionPart</code> that can edit the comment field of a <code>ByteValues</code>
 * source object.
 * <p>
 * This class automatically creates a <code>Section</code> and adds it to the parent composite.
 * After the class has been instantiated it must be added to a <code>IManagedForm</code> to
 * participate in the lifecycle management of the managed form.
 * 
 * <pre>
 *     Composite parent = ...
 *     FormToolkit toolkit = ...
 *     IManagedForm managedForm = ...
 * 
 *     IFormPart part = new ByteValuesCommentPart(parent, toolkit, Section.TITLE_BAR);
 *     managedForm.addPart(part);
 * </pre>
 * 
 * </p>
 * <p>
 * This class implements the {@link IFormPart} interface to participate in the lifecycle management
 * of a managed form. To set the value of the BitField use
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
public class ByteValuesCommentPart extends SectionPart {

	//
	// This stuff could be integrated into the ByteValuesMainPart class.
	// I have made it separate to
	//
	// 1. do not show this in the ByteValueEditor dialog for immediate fuse byte values, which
	// currently does not know anything about comments.
	//
	// 2. have more control in the layout of the FuseByteEditorPage class.
	//

	/**
	 * The model for this <code>SectionPart</code>. Will only be written to in the
	 * {@link #commit(boolean)} method.
	 */
	private ByteValues	fByteValues;

	/** The text control of this section. */
	private Text		fText;

	/** Current comment. Stores any changes until it is commited to the model. */
	private String		fCurrentComment;

	/**
	 * Create a new <code>SectionPart</code> to handle the comment of a <code>ByteValues</code>
	 * object.
	 * <p>
	 * This constructor automatically creates a new section part inside the provided parent and
	 * using the provided toolkit.
	 * </p>
	 * 
	 * @param parent
	 *            the parent
	 * @param toolkit
	 *            the toolkit to use for the section
	 * @param style
	 *            the section widget style
	 */
	public ByteValuesCommentPart(Composite parent, FormToolkit toolkit, int style) {
		super(parent, toolkit, style);
		getSection().setText("Notes");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#initialize(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	public void initialize(IManagedForm form) {
		super.initialize(form);

		Section parent = getSection();
		FormToolkit toolkit = form.getToolkit();
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.heightHint = 54; // TODO: make it 4 lines according to the used font.

		// Create the Section client area.
		Composite client = form.getToolkit().createComposite(parent);
		parent.setClient(client);
		client.setLayout(new GridLayout());

		// And add the single Text control to it.
		fText = toolkit.createText(client, "", SWT.BORDER | SWT.MULTI);
		fText.setLayoutData(gd);
		fText.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				fCurrentComment = fText.getText();
				markDirty();
			}
		});

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

		updateComment();

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#refresh()
	 */
	@Override
	public void refresh() {
		updateComment();
		super.refresh();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.AbstractFormPart#commit(boolean)
	 */
	@Override
	public void commit(boolean onSave) {
		fByteValues.setComment(fCurrentComment);

		super.commit(onSave);
	}

	/**
	 * Set the comment text control from the ByteValues.
	 * 
	 */
	private void updateComment() {
		if (fByteValues == null) {
			return;
		}

		String comment = fByteValues.getComment();
		if (comment == null) {
			comment = "";
		}
		fText.setText(comment);
		fCurrentComment = comment;
	}

}
