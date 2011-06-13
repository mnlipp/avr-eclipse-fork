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

package de.innot.avreclipse.ui.editors.targets;

import java.text.MessageFormat;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import de.innot.avreclipse.core.targets.IGDBServerTool;
import de.innot.avreclipse.core.targets.IProgrammerTool;
import de.innot.avreclipse.core.targets.ITargetConfigConstants;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.tools.NoneToolFactory;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class SectionSummary extends AbstractTCSectionPart implements ITargetConfigConstants {

	private final static String[]	PART_ATTRS		= new String[] {};
	private final static String[]	PART_DEPENDS	= new String[] { ATTR_PROGRAMMER_ID,
			ATTR_HOSTINTERFACE, ATTR_PROGRAMMER_PORT, ATTR_PROGRAMMER_TOOL_ID, ATTR_GDBSERVER_ID };

	private FormEditor				fParentEditor;

	private FormText				fProgrammerText;
	private FormText				fProgrammerToolText;
	private FormText				fGDBServerText;

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getTitle()
	 */
	@Override
	protected String getTitle() {
		return "Summary";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getDescription()
	 */
	@Override
	protected String getDescription() {
		return "Overview of the settings for this hardware configuration";
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#getPartAttributes
	 * ()
	 */
	@Override
	public String[] getPartAttributes() {
		return PART_ATTRS;
	}

	/*
	 * (non-Javadoc)
	 * @seede.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#
	 * getDependentAttributes()
	 */
	@Override
	protected String[] getDependentAttributes() {
		return PART_DEPENDS;
	}

	/**
	 * Sets the editor of which this section is part of.
	 * <p>
	 * This is required so that clicking on the links in this section can set the focus on the
	 * appropriate controls on other pages of the multipage form editor.
	 * </p>
	 * 
	 * @param editor
	 */
	public void setEditor(FormEditor editor) {
		fParentEditor = editor;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#createSectionContent
	 * (org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	protected void createSectionContent(Composite parent, FormToolkit toolkit) {
		TableWrapLayout layout = new TableWrapLayout();
		parent.setLayout(layout);

		// Special listener that will take the href of the activated link, interpret it as a
		// hardware configuration attribute and set the focus on the control responsible for that
		// attribute (revealing the page the control is on)
		HyperlinkAdapter linklistener = new HyperlinkAdapter() {

			public void linkActivated(HyperlinkEvent e) {
				String ref = (String) e.getHref();
				if (fParentEditor != null) {
					fParentEditor.selectReveal(ref);
				}
			}
		};

		fProgrammerText = toolkit.createFormText(parent, true);
		fProgrammerText.addHyperlinkListener(linklistener);

		fProgrammerToolText = toolkit.createFormText(parent, true);
		fProgrammerToolText.addHyperlinkListener(linklistener);

		fGDBServerText = toolkit.createFormText(parent, true);
		fGDBServerText.addHyperlinkListener(linklistener);

	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.ui.editors.targets.AbstractTargetConfigurationEditorPart#updateSectionContent
	 * ()
	 */
	@Override
	protected void refreshSectionContent() {

		ITargetConfiguration tc = getTargetConfiguration();
		String content;
		String attr;

		//
		// Set the programmer tool content
		//
		IProgrammerTool progtool = tc.getProgrammerTool();
		attr = ATTR_PROGRAMMER_TOOL_ID;
		if (progtool.getId().equals(NoneToolFactory.ID)) {
			// 'None' programmer tool selected
			content = MessageFormat
					.format(
							"<form><p>Programming not possible, no <a href=\"{0}\">programmer tool</a> selected.</p></form>",
							attr);

		} else {
			// Normal programmer tool selected
			String name = progtool.getName();
			content = MessageFormat.format(
					"<form><p>Programming done with <a href=\"{0}\">{1}</a>.</p></form>", attr,
					name);
		}
		fProgrammerText.setText(content, true, true);

		//
		// Set the gdb server tool content
		//
		IGDBServerTool gdbserver = tc.getGDBServerTool();
		attr = ATTR_GDBSERVER_ID;
		String name = gdbserver.getName();
		if (gdbserver.getId().equals(NoneToolFactory.ID)) {
			// 'None' gdbserver selected => no debugging
			content = MessageFormat
					.format(
							"<form><p>Debugging not supported, no <a href=\"{0}\">gdbserver tool</a> selected.</p></form>",
							attr);
		} else {
			if (gdbserver.isSimulator()) {
				// Simulator
				content = MessageFormat.format(
						"<form><p>Debugging with Simulator <a href=\"{0}\">{1}</a>.</p></form>",
						attr, name);

			} else {
				// On Chip Debugging
				content = MessageFormat
						.format(
								"<form><p>On Chip Debugging done via <a href=\"{0}\">{1}</a> GDB server.</p></form>",
								attr, name);
			}
		}
		fGDBServerText.setText(content, true, true);

	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.ui.editors.targets.AbstractTCSectionPart#refreshMessages()
	 */
	@Override
	protected void refreshMessages() {
	}

}
