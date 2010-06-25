/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
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
package de.innot.avreclipse.core.paths.win32;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.utils.WindowsRegistry;

/**
 * This is an extension to the CDT WindowRegistry class.
 * <p>
 * Currently the original CDT WindowsRegistry class has some problems on 64bit windows systems,
 * where it (sometimes?) fails to load its associated dll.
 * </p>
 * <p>
 * This class, which is a (partial, see below) drop in replacement for <code>WindowsRegistry</code>
 * and will, whenever <code>WindowsRegistry</code> fails, use an alternative method to access the
 * registry.
 * </p>
 * <p>
 * Instead of using the JNI it will start the Windows <em>'reg query'</em> command and parse its output. In addition
 * to that it will also automatically look in the '\Wow6432Node' subnode if a key can not be found.
 * </p>
 * <p>
 * Currently only the methods {@link #getLocalMachineValue(String, String)} and
 * {@link #getLocalMachineValueName(String, int)} are implemented, because they are the only ones
 * used by the AVR Eclipse Plugin.
 * </p>
 * 
 * @see org.eclipse.cdt.utils.WindowsRegistry
 * @author Enrico Ehrich
 * @author Thomas Holland
 * @noextend This class is not intended to be subclassed by clients.
 * @noinstantiate This class is not intended to be instantiated by clients.
 * @since 2.3.2
 * 
 */
public class MyWindowsRegistry {

	/**
	 * Small container to return the results of a query.
	 */
	protected class RegistryKeyValue {
		public String	key;	// Key name
		public String	type;	// Registry type, e.g. REG_SZ or REG_EXPANDED_SZ
		public String	value;	// Key value
	}

	/**
	 * Small class that reads all incoming chars from the InputStream and stores them in a String.
	 */
	protected class StreamReader extends Thread {
		private InputStream		is;
		private StringBuilder	sb;

		protected StreamReader(InputStream is) {
			this.is = is;
			sb = new StringBuilder();
		}

		public void run() {
			try {
				int c;
				while ((c = is.read()) != -1)
					sb.append((char)c);
			} catch (IOException e) {
				;
			}
		}

		/**
		 * Gets the content of the given InputStream.
		 * <p>
		 * As a convenience the content is split into separate lines.
		 * </p>
		 * 
		 * @return All lines from the InputStream
		 */
		protected String[] getResult() {
			String result = sb.toString();

			// This works only on Windows, but this class is Window specific anyway so we can get
			// away with this simplistic method.
			return result.split("\r\n");
		}
	}

	/** The Windows executable to query the Registry */
	private static final String			REGQUERY_UTIL		= "reg query";

	/** Start of the Registry value type. */
	private static final String			REGTYPE_TOKEN		= "REG_";

	/** The Current User branch of the registry. */
	// Currently unused
	// private static final String KEY_CURRUSER = "HKCU\\";

	/** The local machine branch of the registry. */
	private static final String			KEY_LOCALMACHINE	= "HKLM\\";

	private static MyWindowsRegistry	fInstance;
	private static WindowsRegistry		fCDTRegistryInstance;

	/** Flag to inhibit calls to the CDT WindowsRegistry class. Used for test purposes. */
	private Boolean						fInhibitOriginal;

	/**
	 * Get the singleton instance of this class.
	 * 
	 * @return <code>MyWindowsRegistry</code> instance.
	 */
	public static MyWindowsRegistry getRegistry() {
		if (fInstance == null) {
			fInstance = new MyWindowsRegistry();
		}
		if (fCDTRegistryInstance == null) {
			fCDTRegistryInstance = WindowsRegistry.getRegistry();
		}
		return fInstance;
	}

	/**
	 * Inhibit usage of the original CDT WindowsRegistry class, always use the fallback method.
	 * <p>
	 * This call is intended only for testing this class.
	 * </p>
	 * 
	 * @param inhibit
	 *            When <code>true</code> only the fallback method is used (external call to the
	 *            'reg' executable.
	 */
	protected void setInhibitOriginal(boolean inhibit) {
		fInhibitOriginal = inhibit;
	}

	/**
	 * @see WindowsRegistry#getLocalMachineValue(String, String)
	 */
	public String getLocalMachineValue(String subkey, String name) {
		String result;

		// First try the CDT WindowsRegistry Class
		if (fCDTRegistryInstance != null && !fInhibitOriginal) {
			result = fCDTRegistryInstance.getLocalMachineValue(subkey, name);
			if (result != null) {
				// Original WindowsRegistry class was successful
				return result;
			}
		}

		// Original WindowsRegistry failed: Try the fallback
		result = getRegValue(KEY_LOCALMACHINE + subkey, name);
		return result;
	}

	/**
	 * @see WindowsRegistry#getLocalMachineValueName(String, int)
	 */
	public String getLocalMachineValueName(String subkey, int index) {
		String result;

		// First try the CDT WindowsRegistry Class
		if (fCDTRegistryInstance != null && !fInhibitOriginal) {
			result = fCDTRegistryInstance.getLocalMachineValueName(subkey, index);
			if (result != null) {
				// Original WindowsRegistry class was successful
				return result;
			}
		}

		// Original WindowsRegistry failed: Try the fallback
		result = getRegNames(KEY_LOCALMACHINE + subkey, index);
		return result;

	}

	private String getRegValue(String key, String pathName) {
		RegistryKeyValue[] results;

		results = getRegValueDefault(key, pathName);
		if (results.length == 0) {
			results = getRegValue6432(key, pathName);
		}

		if (results.length > 0) {
			return results[0].value;
		} else {
			return null;
		}
	}

	private RegistryKeyValue[] getRegValueDefault(String key, String pathName) {
		String parameters = "\"" + key + "\" /v " + pathName;
		return executeRegCommand(parameters);

	}

	private RegistryKeyValue[] getRegValue6432(String key, String pathName) {
		String key32 = key.replaceFirst("SOFTWARE", "SOFTWARE\\\\Wow6432Node");
		String parameters = "\"" + key32 + "\" /v " + pathName;
		return executeRegCommand(parameters);
	}

	private String getRegNames(String key, int index) {
		RegistryKeyValue[] results;

		results = getRegNamesDefault(key);
		if (results.length == 0) {
			results = getRegNames6432(key);
		}

		if (index < results.length) {
			return results[index].key;
		} else {
			return null;
		}
	}

	private RegistryKeyValue[] getRegNamesDefault(String key) {
		String parameters = "\"" + key + "\" /s";
		return executeRegCommand(parameters);

	}

	private RegistryKeyValue[] getRegNames6432(String key) {
		String key6432 = key.replaceFirst("SOFTWARE", "SOFTWARE\\\\Wow6432Node");
		String parameters = "\"" + key6432 + "\" /s";
		return executeRegCommand(parameters);
	}

	/**
	 * Executes "reg query" with the given parameter string, parses the output and returns an array
	 * of {@link RegistryKeyValue} objects. If the call fails in any way an empty array is returned.
	 * 
	 * @param parameter
	 *            for the "reg query" call
	 * @return array of Key/Value objects. The array may be empty, but never <code>null</code>.
	 */
	private RegistryKeyValue[] executeRegCommand(String parameter) {
		String command = REGQUERY_UTIL + " " + parameter;
		List<RegistryKeyValue> results = new ArrayList<RegistryKeyValue>();

		try {
			Process process = Runtime.getRuntime().exec(command);
			StreamReader reader = new StreamReader(process.getInputStream());
			reader.start();
			process.waitFor();
			reader.join();
			String[] alllines = reader.getResult();
			for (String line : alllines) {
				if (line.indexOf(REGTYPE_TOKEN) != -1) {
					// line contains "REG_"
					// split it into key, type, and value
					String[] items = line.split("\t");
					RegistryKeyValue keyvalue = new RegistryKeyValue();
					keyvalue.key = items[0].trim();
					keyvalue.type = items[1].trim();
					keyvalue.value = items[2].trim();
					results.add(keyvalue);
				}
			}
		} catch (Exception e) {
			// In case of an exception we return what we have found so far (which may be nothing =
			// empty array)
		}

		return results.toArray(new RegistryKeyValue[results.size()]);
	}

}
