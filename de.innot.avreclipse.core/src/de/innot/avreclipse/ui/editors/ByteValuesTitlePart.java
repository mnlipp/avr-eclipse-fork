/**
 * 
 */
package de.innot.avreclipse.ui.editors;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.forms.AbstractFormPart;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IPartSelectionListener;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;
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
public class ByteValuesTitlePart extends AbstractFormPart implements IPartSelectionListener {

	/** Reference to the current <code>ByteValues</code>. */
	private ByteValues	fByteValues;

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
			setTitle();
			return true;
		}

		return false;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.IPartSelectionListener#selectionChanged(org.eclipse.ui.forms.IFormPart,
	 *      org.eclipse.jface.viewers.ISelection)
	 */
	public void selectionChanged(IFormPart part, ISelection selection) {
		// This is (indirectly) called by the "Change MCU Type" Action.

		// Set the title accordingly
		setTitle();
	}

}
