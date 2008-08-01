/**
 * 
 */
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
 * @author U043192
 * 
 */
public class ByteValuesEditorDialog extends FormDialog {

	private final ByteValues	fByteValues;

	private IManagedForm		fForm;

	public ByteValuesEditorDialog(Shell parentShell, ByteValues values) {
		super(parentShell);

		fByteValues = values;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.FormDialog#close()
	 */
	@Override
	public boolean close() {
		if (fForm.isDirty()) {
			fForm.commit(false);
		}
		return super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.FormDialog#createFormContent(org.eclipse.ui.forms.IManagedForm)
	 */
	@Override
	protected void createFormContent(IManagedForm managedForm) {

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

	private void fillBody(final IManagedForm managedForm, FormToolkit toolkit) {

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

	public ByteValues getByteValues() {
		return fByteValues;
	}

}
