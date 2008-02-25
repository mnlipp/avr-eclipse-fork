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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.innot.avreclipse.core.IMCUProvider;

public class SupportedModel {

	private List<String> fMCUList = null;
	private Set<MCUProviderEnum> fProviders = null;

	public SupportedModel() {

		getProviderList();
		loadMCULists();

	}

	public List<String> getMCUList() {
		return fMCUList;
	}

	private Set<MCUProviderEnum> getProviderList() {
		if (fProviders == null) {
			fProviders = new HashSet<MCUProviderEnum>();
			for (MCUProviderEnum provider : MCUProviderEnum.values()) {
				fProviders.add(provider);
			}
		}
		return fProviders;
	}

	private void loadMCULists() {
		// build a "master" mcu id list from all available sources
		// TODO: could be implemented via an extension.

		Set<String> masterlist = new HashSet<String>();

		for (IMCUProvider provider : getProviderList()) {
			masterlist.addAll(provider.getMCUList());
		}
		List<String> sortedlist = new ArrayList<String>(masterlist);
		Collections.sort(sortedlist);
		fMCUList = sortedlist;
	}

}
