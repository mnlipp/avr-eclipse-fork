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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

import de.innot.avreclipse.core.toolinfo.fuses.BitFieldDescription;
import de.innot.avreclipse.core.toolinfo.fuses.BitFieldValueDescription;
import de.innot.avreclipse.core.toolinfo.fuses.ByteValues;

/**
 * 
 * @author Thomas Holland
 * @since 2.3
 * 
 */
public class BitFieldEditorSectionPart extends SectionPart {

	private final BitFieldDescription	fBFD;

	private ByteValues					fByteValues;

	private IOptionPart					fOptionPart;

	private int							fCurrentValue	= -1;

	/**
	 * @param section
	 * @param description
	 */
	public BitFieldEditorSectionPart(Section section, BitFieldDescription description) {
		super(section);

		fBFD = description;
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

		Composite client = form.getToolkit().createComposite(parent);
		parent.setClient(client);

		// Determine the number of possible options and the list of possible values
		int maxoptions = fBFD.getMaxValue();
		List<BitFieldValueDescription> allvalues = fBFD.getValuesEnumeration();

		if (maxoptions == 1 && allvalues.size() == 0) {
			fOptionPart = new OptionYesNo();
		} else if (maxoptions == 1) {
			fOptionPart = new OptionCheckbox();
		} else if (/* maxoptions > 1 && */allvalues.size() == 0) {
			fOptionPart = new OptionText();
		} else if (/* maxoptions > 1 && */allvalues.size() < 6) {
			fOptionPart = new OptionRadioButtons();
		} else {
			boolean splitValues = allvalues.size() > 16
					&& allvalues.get(0).getDescription().contains(";");
			if (splitValues) {
				fOptionPart = new OptionDualCombo();
			} else {
				fOptionPart = new OptionSingleCombo();
			}
		}
		fOptionPart.addControl(client, toolkit, fBFD);

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
		} else {
			return false;
		}

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
		int value = fByteValues.getNamedValue(fBFD.getName());
		fCurrentValue = value;
		fOptionPart.setValue(value);
	}

	@Override
	public void commit(boolean onSave) {
		fByteValues.setNamedValue(fBFD.getName(), fCurrentValue);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.forms.SectionPart#setFocus()
	 */
	@Override
	public void setFocus() {
		if (fCurrentValue == -1) {
			refresh();
			return;
		}
		// TODO Auto-generated method stub
		super.setFocus();
	}

	/**
	 * @param newvalue
	 */
	private void internalSetValue(int newvalue) {
		fCurrentValue = newvalue;
		markDirty();
	}

	/**
	 * @author U043192
	 * 
	 */
	private interface IOptionPart {

		public void addControl(Composite parent, FormToolkit toolkit, BitFieldDescription bfd);

		public void setValue(int value);
	}

	private class OptionYesNo implements IOptionPart {

		private Button	fYesButton;
		private Button	fNoButton;

		public void addControl(Composite parent, FormToolkit toolkit, BitFieldDescription bfd) {

			parent.setLayout(new RowLayout(SWT.HORIZONTAL));

			fYesButton = toolkit.createButton(parent, "Yes", SWT.RADIO);
			fNoButton = toolkit.createButton(parent, "No", SWT.RADIO);

			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					int value = (event.widget == fYesButton) ? 0x00 : 0x01;
					internalSetValue(value);
				}
			};
			fYesButton.addListener(SWT.Selection, listener);
			fNoButton.addListener(SWT.Selection, listener);
		}

		public void setValue(int value) {
			if (-1 > value || value > 1) {
				throw new IllegalArgumentException("value must be -1, 0 or 1, was " + value);
			}
			if (value == -1) {
				fYesButton.setSelection(false);
				fNoButton.setSelection(false);
			} else {
				boolean valueYes = value == 0 ? true : false;
				fYesButton.setSelection(valueYes);
				fNoButton.setSelection(!valueYes);
			}
		}

	}

	private class OptionCheckbox implements IOptionPart {

		private Button	fCheckButton;

		public void addControl(Composite parent, FormToolkit toolkit, BitFieldDescription bfd) {

			parent.setLayout(new FillLayout());

			String buttontext = bfd.getValuesEnumeration().get(0).getDescription();
			fCheckButton = toolkit.createButton(parent, buttontext, SWT.CHECK);

			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					int value = fCheckButton.getSelection() ? 0x00 : 0x01;
					internalSetValue(value);
				}
			};
			fCheckButton.addListener(SWT.Selection, listener);
		}

		public void setValue(int value) {
			if (-1 > value || value > 1) {
				throw new IllegalArgumentException("value must be -1, 0 or 1, was " + value);
			}
			if (value == -1) {
				// TODO: change this to a tristate checkbox when we change to SWT 3.4
				// for now we have to leave it unset (= 1)
				value = 1;
			}
			boolean valueYes = value == 0 ? true : false;
			fCheckButton.setSelection(valueYes);
		}

	}

	private class OptionRadioButtons implements IOptionPart {

		private Button[]	fButtons;
		private int[]		fValues;

		public void addControl(Composite parent, FormToolkit toolkit, BitFieldDescription bfd) {

			parent.setLayout(new RowLayout(SWT.VERTICAL));

			List<BitFieldValueDescription> allvalues = bfd.getValuesEnumeration();
			fButtons = new Button[allvalues.size()];
			fValues = new int[allvalues.size()];

			Listener listener = new Listener() {

				public void handleEvent(Event event) {
					int value = -1;
					for (int i = 0; i < fButtons.length; i++) {
						if (fButtons[i] == event.widget) {
							value = fValues[i];
						}
					}
					internalSetValue(value);
				}
			};

			int i = 0;

			for (BitFieldValueDescription desc : allvalues) {
				fButtons[i] = toolkit.createButton(parent, desc.getDescription(), SWT.RADIO);
				fButtons[i].addListener(SWT.Selection, listener);
				fValues[i] = desc.getValue();
				i++;
			}
		}

		public void setValue(int value) {
			boolean isValid = false;

			for (int i = 0; i < fValues.length; i++) {
				if (fValues[i] == value) {
					fButtons[i].setSelection(true);
					isValid = true;
				} else {
					fButtons[i].setSelection(false);
				}
			}
			if (!isValid && value != -1) {
				throw new IllegalArgumentException("Illegal value " + value);
			}
		}

	}

	private class OptionDualCombo implements IOptionPart {

		private Combo								fRootCombo;
		private Combo								fSubCombo;
		private String[]							fRootTexts;
		private int[][]								fReverseLookup;
		private final Map<String, List<String>>		fRootToSubnames	= new HashMap<String, List<String>>();
		private final Map<String, List<Integer>>	fRootToValues	= new HashMap<String, List<Integer>>();

		public void addControl(Composite parent, FormToolkit toolkit, BitFieldDescription bfd) {

			parent.setLayout(new RowLayout());

			List<BitFieldValueDescription> allvalues = bfd.getValuesEnumeration();

			// Collections.sort(allvalues, new Comparator<BitFieldValueDescription>() {
			// public int compare(BitFieldValueDescription o1, BitFieldValueDescription o2) {
			// return o1.getDescription().compareTo(o2.getDescription());
			// }
			// });

			int maxvalue = bfd.getMaxValue();
			fReverseLookup = new int[maxvalue + 1][];
			Arrays.fill(fReverseLookup, new int[] { -1, -1 });

			List<String> rootnames = new ArrayList<String>();

			String lastroottext = null;
			for (BitFieldValueDescription bfvd : allvalues) {
				String desc = bfvd.getDescription();
				int value = bfvd.getValue();

				// Split the current text
				int splitat = desc.indexOf(';');
				String firstpart = desc.substring(0, splitat).trim();
				if (!firstpart.equalsIgnoreCase(lastroottext)) {
					rootnames.add(firstpart);
					fRootToSubnames.put(firstpart, new ArrayList<String>());
					fRootToValues.put(firstpart, new ArrayList<Integer>());
					lastroottext = firstpart;
				}
				String subtext = desc.substring(splitat + 1).trim();
				List<String> subnames = fRootToSubnames.get(firstpart);
				subnames.add(subtext);
				List<Integer> subvalues = fRootToValues.get(firstpart);
				subvalues.add(value);

				// Map the index values of both name parts t the value, so we can get the
				// indices for a given value.
				// As we fill the arrays sequentially we can just take the size of the arrays to
				// get the index of the last addition (instead of the more expensive
				// list.indexOf())
				fReverseLookup[value] = new int[] { rootnames.size() - 1, subnames.size() - 1 };
			}

			// Convert the list(s) to arrays
			fRootTexts = rootnames.toArray(new String[rootnames.size()]);

			fRootCombo = new Combo(parent, SWT.READ_ONLY);
			toolkit.adapt(fRootCombo);
			fRootCombo.setItems(fRootTexts);
			fRootCombo.setVisibleItemCount(fRootTexts.length);
			fRootCombo.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					int index = fRootCombo.getSelectionIndex();
					String name = fRootTexts[index];
					List<String> subnames = fRootToSubnames.get(name);
					String[] subnamesarray = subnames.toArray(new String[subnames.size()]);
					int oldselection = fSubCombo.getSelectionIndex();
					fSubCombo.setItems(subnamesarray);
					fSubCombo.setVisibleItemCount(subnamesarray.length);
					if (0 <= oldselection && oldselection < subnamesarray.length) {
						fSubCombo.select(oldselection);
					} else {
						fSubCombo.select(0);
					}
					fSubCombo.notifyListeners(SWT.Selection, new Event());
				}
			});

			fSubCombo = new Combo(parent, SWT.READ_ONLY);
			toolkit.adapt(fSubCombo);
			fSubCombo.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {
					List<Integer> subvalues;
					int rootindex = fRootCombo.getSelectionIndex();
					String name = fRootTexts[rootindex];
					subvalues = fRootToValues.get(name);
					int subindex = fSubCombo.getSelectionIndex();
					int newvalue = subvalues.get(subindex);

					internalSetValue(newvalue);
				}
			});

		}

		public void setValue(int value) {
			if (value == -1) {
				fRootCombo.select(-1);
				fSubCombo.select(-1);
				return;
			}

			int[] indices = fReverseLookup[value];
			if (indices[0] == -1) {
				// invalid value: deselect both combos
				fRootCombo.select(-1);
				fSubCombo.select(-1);
				return;
			}

			fRootCombo.select(indices[0]);
			String name = fRootTexts[indices[0]];
			List<String> subnames = fRootToSubnames.get(name);
			String[] subnamesarray = subnames.toArray(new String[subnames.size()]);
			fSubCombo.setItems(subnamesarray);
			fSubCombo.setVisibleItemCount(subnamesarray.length);
			fSubCombo.select(indices[1]);

		}

	}

	private class OptionSingleCombo implements IOptionPart {

		private Combo		fCombo;
		private String[]	fTexts;
		private Integer[]	fValues;
		private int[]		fReverseLookup;

		public void addControl(Composite parent, FormToolkit toolkit, BitFieldDescription bfd) {

			parent.setLayout(new RowLayout(SWT.HORIZONTAL));

			List<BitFieldValueDescription> allvalues = bfd.getValuesEnumeration();

			int maxvalue = bfd.getMaxValue();
			fReverseLookup = new int[maxvalue + 1];
			Arrays.fill(fReverseLookup, -1);

			List<String> names = new ArrayList<String>();
			List<Integer> values = new ArrayList<Integer>();
			for (BitFieldValueDescription bfvd : allvalues) {
				String desc = bfvd.getDescription();
				int value = bfvd.getValue();

				names.add(desc);
				values.add(value);
				fReverseLookup[value] = names.size() - 1;
			}

			// Convert the list(s) to arrays
			fTexts = names.toArray(new String[names.size()]);
			fValues = values.toArray(new Integer[values.size()]);

			fCombo = new Combo(parent, SWT.READ_ONLY);
			toolkit.adapt(fCombo);
			fCombo.setItems(fTexts);
			fCombo.setVisibleItemCount(fTexts.length);
			fCombo.addListener(SWT.Selection, new Listener() {

				public void handleEvent(Event event) {

					int index = fCombo.getSelectionIndex();
					int newvalue = fValues[index];

					internalSetValue(newvalue);
				}
			});

		}

		public void setValue(int value) {
			if (value == -1) {
				fCombo.select(-1);
				return;
			}

			int index = fReverseLookup[value];
			fCombo.select(index);

		}

	}

	private class OptionText implements IOptionPart {

		private int		fMaxValue;

		private Text	fText;

		public void addControl(Composite parent, FormToolkit toolkit, BitFieldDescription bfd) {

			parent.setLayout(new RowLayout());

			fMaxValue = bfd.getMaxValue();

			fText = toolkit.createText(parent, "", SWT.NONE);
			fText.setTextLimit(5);
			fText.setToolTipText("Decimal, Hexadecimal (0x..) or Octal (0...)");
			fText.addListener(SWT.Modify, new Listener() {
				public void handleEvent(Event event) {
					try {
						int value = Integer.decode(fText.getText());
						if (value <= fMaxValue) {
							fText.setForeground(fText.getDisplay().getSystemColor(SWT.COLOR_BLACK));
							internalSetValue(value);
							return;
						}
					} catch (NumberFormatException nfe) {
					}
					fText.setForeground(fText.getDisplay().getSystemColor(SWT.COLOR_RED));
				}
			});
			// Add a verify listener to only accept hex digits and convert them to
			// upper case
			fText.addVerifyListener(new VerifyListener() {
				public void verifyText(VerifyEvent event) {
					String text = event.text.toUpperCase();
					text = text.replace('X', 'x');
					if (!text.matches("[0-9A-Fx]*")) {
						event.doit = false;
					}
					event.text = text;
				}
			});

		}

		public void setValue(int value) {
			if (-1 > value || value > fMaxValue) {
				throw new IllegalArgumentException("value must be -1, or between 0 and "
						+ fMaxValue + ", was " + value);
			}
			if (value == -1) {
				fText.setText("");
			} else {
				fText.setText("0x" + Integer.toHexString(value));
			}
		}

	}

}
