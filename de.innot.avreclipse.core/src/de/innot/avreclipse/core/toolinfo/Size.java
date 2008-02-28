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
package de.innot.avreclipse.core.toolinfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.spawner.ProcessFactory;
import org.eclipse.core.runtime.IPath;

import de.innot.avreclipse.PluginIDs;
import de.innot.avreclipse.core.paths.AVRPath;
import de.innot.avreclipse.core.paths.AVRPathProvider;
import de.innot.avreclipse.core.paths.IPathProvider;

/**
 * This class provides some information about the used size tool in the
 * toolchain.
 * 
 * It can return a list of all supported format options.
 * 
 * @author Thomas Holland
 * @since 2.1
 * 
 */
public class Size extends BaseToolInfo {

	private static final String TOOL_ID = PluginIDs.PLUGIN_TOOLCHAIN_TOOL_SIZE;

	private Map<String, String> fOptionsMap = null;

	private static Size instance = null;

	private IPathProvider fPathProvider = new AVRPathProvider(AVRPath.AVRGCC);

	/**
	 * Get an instance of this Tool.
	 */
	public static Size getDefault() {
		if (instance == null)
			instance = new Size();
		return instance;
	}

	private Size() {
		// Let the superclass get the command name
		super(TOOL_ID);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.IToolInfo#getToolPath()
	 */
	@Override
	public IPath getToolPath() {
		IPath path = fPathProvider.getPath();
		return path.append(getCommandName());
	}

	/**
	 * @return true if this size tool supports the -format=avr option.
	 */
	public boolean hasAVROption() {

		return getSizeOptions().containsValue("avr");
	}

	/**
	 * @return Map &lt;UI-name, option-name&gt; with all supported size options.
	 */
	private Map<String, String> getSizeOptions() {

		if (fOptionsMap != null) {
			return fOptionsMap;
		}

		fOptionsMap = new HashMap<String, String>();

		Process cmdproc = null;
		InputStream es = null;
		InputStreamReader esr = null;
		BufferedReader br = null;


		try {
			// Execute the size command with the help option and parse its
			// output
			cmdproc = ProcessFactory.getFactory().exec(getToolPath().toOSString() + " -h");
			es = cmdproc.getErrorStream();
			esr = new InputStreamReader(es);
			br = new BufferedReader(esr);
			String line;

			while ((line = br.readLine()) != null) {
				if (line.contains("--format=")) {
					// this is the line we are looking for
					// extract the format options
					int start = line.indexOf('{');
					int end = line.lastIndexOf('}');
					String options = line.substring(start + 1, end);
					// next line does not work and i am no regex expert
					// to know how to split at a "|"
					// String[] allopts = options.split("|");
					int splitter = 0;
					while ((splitter = options.indexOf('|')) != -1) {
						String opt = options.substring(0, splitter);
						fOptionsMap.put(convertOption(opt), opt);
						options = options.substring(splitter + 1);
					}
					fOptionsMap.put(convertOption(options), options);
					break;
				}
			}
		} catch (IOException e) {
		} finally {
			try {
				if (br != null) br.close();
				if (esr!= null) esr.close();
				if (es != null) es.close();
			} catch (IOException e) {
			}
			try {
				if (cmdproc != null) {
					cmdproc.waitFor();
				}
			} catch (InterruptedException e) {
			}
		}

		return fOptionsMap;
	}

	/**
	 * Get a better name for known format options.
	 * 
	 * @param option
	 * @return String with the UI name of the Option
	 */
	private static String convertOption(String option) {
		if ("avr".equals(option)) {
			return "AVR Specific Format";
		}
		if ("berkeley".equals(option)) {
			return "Berkeley Format";
		}
		if ("sysv".equals(option)) {
			return "SysV Format";
		}

		// unknown option
		// TODO: log a message telling the user to report this
		// new option for inclusion into the list above
		return option;
	}
}
