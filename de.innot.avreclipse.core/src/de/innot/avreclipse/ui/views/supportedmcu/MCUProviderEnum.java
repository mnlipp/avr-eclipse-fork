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

import java.util.Set;

import de.innot.avreclipse.core.IMCUProvider;
import de.innot.avreclipse.core.toolinfo.Datasheets;
import de.innot.avreclipse.core.toolinfo.GCC;
import de.innot.avreclipse.core.toolinfo.MCUNames;
import de.innot.avreclipse.core.toolinfo.Signatures;
import de.innot.avreclipse.devicedescription.avrio.AVRiohDeviceDescriptionProvider;
import de.innot.avreclipse.ui.views.supportedmcu.SupportedMCUs.LabelStyle;

public enum MCUProviderEnum implements IMCUProvider {

	NAMES("Name", LabelStyle.SHOW_STRING) {
		protected IMCUProvider getDefault() {
			return MCUNames.getDefault();
		}
	},
	DATASHEET("Datasheet", LabelStyle.SHOW_URL) {
		protected IMCUProvider getDefault() {
			return Datasheets.getDefault();
		}
	},
	SIGNATURE("Signature", LabelStyle.SHOW_STRING) {
		protected IMCUProvider getDefault() {
			return Signatures.getDefault();
		}
	},
	AVRSTUDIO("AVR Studio", LabelStyle.SHOW_YESNO) {
		protected IMCUProvider getDefault() {
			return Signatures.getDefault();
		}
	},
	AVRGCC("AVR-GCC", LabelStyle.SHOW_YESNO) {
		protected IMCUProvider getDefault() {
			return GCC.getDefault();
		}
	},
	AVRINCLUDE("<avr/io.h>", LabelStyle.SHOW_YESNO) {
		protected IMCUProvider getDefault() {
			return AVRiohDeviceDescriptionProvider.getDefault();
		}
	};
/*
	AVRDUDE("AVRDude", LabelStyle.SHOW_YESNO) {
		protected IMCUProvider getDefault() {
			return null;
		}
	},
	
*/
	private IMCUProvider fMCUProvider;
	private LabelStyle fLabelStyle;
	private String fName;
	

	private MCUProviderEnum(String name, LabelStyle style) {
		fName = name;
		fLabelStyle = style;
		fMCUProvider = getDefault();
	}

	protected abstract IMCUProvider getDefault();

/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUInfo(java.lang.String)
	 */
	public String getMCUInfo(String mcuid) {
		return fMCUProvider.getMCUInfo(mcuid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#getMCUList()
	 */
	public Set<String> getMCUList() {
		return fMCUProvider.getMCUList();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.IMCUProvider#hasMCU(java.lang.String)
	 */
	public boolean hasMCU(String mcuid) {
		return fMCUProvider.hasMCU(mcuid);
	}

	public LabelStyle getLabelStyle() {
		return fLabelStyle;
	}

	public String getName() {
		return fName;
	}
}
