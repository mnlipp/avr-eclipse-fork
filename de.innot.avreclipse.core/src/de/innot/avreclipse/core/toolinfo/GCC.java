/*******************************************************************************
 * 
 * Copyright (c) 2007 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.core.toolinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;

import de.innot.avreclipse.PluginIDs;

/**
 * This class provides some information about the used gcc compiler in the
 * toolchain.
 * 
 * It can return a list of all supported target mcus.
 * 
 * @author Thomas
 * @version 1.0
 * @since 2.1
 * 
 */
public class GCC extends BaseToolInfo {

	private static final String TOOL_ID = PluginIDs.PLUGIN_TOOLCHAIN_TOOL_COMPILER;

	protected String[] toolinfotypes = {TOOLINFOTYPE_MCUS};

	private static GCC instance = null;

	private Map<String, String> fMCUmap = null;

	/**
	 * Get an instance of this Tool.
	 */
	public static GCC getDefault() {
		if (instance == null)
			instance = new GCC();
		return instance;
	}

	private GCC() {
		// Let the superclass get the command name
		super(TOOL_ID);
		super.toolinfotypes = this.toolinfotypes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.IToolInfo#getToolPath()
	 */
	public IPath getToolPath() {
		// TODO Auto-generated method stub
		return null;
	}

	public Map<String, String> getToolInfo(String type) {

		if (TOOLINFOTYPE_MCUS.equals(type)) {
			return getMCUList();
		}

		return null;
	}

	public List<String> getToolInfoTypes() {
		List<String> types = new ArrayList<String>(1);
		types.add(TOOLINFOTYPE_MCUS);
		return types;
	}

	private Map<String, String> getMCUList() {

		if(fMCUmap != null) {
			return fMCUmap;
		}
		
		fMCUmap = new HashMap<String, String>();

		Process cmdproc = null;
		InputStream is = null;

		try {
			cmdproc = ProcessFactory.getFactory().exec(getCommandName() + " --target-help");
			is = cmdproc.getInputStream();
			BufferedReader br = new BufferedReader(new InputStreamReader(is));

			boolean start = false;
			String line;

			while ((line = br.readLine()) != null) {
				if ("Known MCU names:".equals(line)) {
					start = true;
					continue;
				}
				if (start && !line.startsWith(" ")) {
					// finished
					break;
				}
				if (start) {
					String[] names = line.split(" ");
					for (int i = 0; i < names.length; i++) {
						String mcuid = names[i];
						String mcuname = convertmcuname(mcuid);
						if (mcuname == null) {
							// some mcuid are generic and should not be included
							continue;
						}
						fMCUmap.put(mcuname, mcuid);
					}
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
			}
			try {
				if (cmdproc != null) {
					cmdproc.waitFor();
				}
			} catch (InterruptedException e) {
			}
		}

		return fMCUmap;
	}

	/**
	 * Change the lower case mcuid into the official Name.
	 * 
	 * @param mcuid
	 * @return Name of the MCU or null if it should not be included (e.g.
	 *         generic family names like 'avr2')
	 */
	private static String convertmcuname(String mcuid) {
		// remove invalid entries
		if ("".equals(mcuid.trim())) {
			return null;
		}
		// AVR Specific
		if (mcuid.startsWith("atmega")) {
			return "ATmega" + mcuid.substring(6).toUpperCase();
		}
		if (mcuid.startsWith("attiny")) {
			return "ATtiny" + mcuid.substring(6).toUpperCase();
		}
		if (mcuid.startsWith("at")) {
			return mcuid.toUpperCase();
		}
		if (mcuid.startsWith("avr")) {
			// don't include the generic familiy names
			return null;
		}

		return mcuid;
	}
}
