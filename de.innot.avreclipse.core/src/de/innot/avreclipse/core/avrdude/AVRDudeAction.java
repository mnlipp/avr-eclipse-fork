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
package de.innot.avreclipse.core.avrdude;

import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.macros.BuildMacroException;
import org.eclipse.cdt.managedbuilder.macros.IBuildMacroProvider;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Path;

/**
 * @author Thomas Holland
 * 
 */
public class AVRDudeAction {

	private final static String WINQUOTE = System.getProperty("os.name").toLowerCase().contains(
	        "windows") ? "\"" : "";

	public enum MemType {
		flash, eeprom, signature, fuse, lfuse, hfuse, efuse, lock, calibration;
	}

	public enum Action {
		read("r"), write("w"), verify("v");

		public String symbol;

		private Action(String op) {
			symbol = op;
		}
	}

	public enum FileType {
		iHex("i"), sRec("s"), raw("r"), immediate("m"), decimal("d"), hex("h"), octal("o"), binary(
		        "b"), auto("a");

		public String symbol;

		private FileType(String type) {
			symbol = type;
		}
	}

	private MemType fMemType;
	private Action fAction;
	private String fFilename;
	private FileType fFileType;
	private int fImmediateValue;

	public AVRDudeAction(MemType memtype, Action action, String filename, FileType filetype) {

		Assert.isTrue(filetype != FileType.immediate);

		fMemType = memtype;
		fAction = action;
		fFilename = filename;
		fFileType = filetype;
	}

	public AVRDudeAction(MemType memtype, Action action, int value) {
		fMemType = memtype;
		fAction = action;
		fImmediateValue = value;
		fFileType = FileType.immediate;
	}

	/**
	 * Get the avrdude action option without resolving the filename.
	 * 
	 * @return <code>String</code> with an avrdude option
	 */
	public String getArgument() {
		return getArgument(null);
	}

	/**
	 * Get the avrdude action option.
	 * <p>
	 * The filename (if set) will be resolved against the given
	 * <code>IConfiguration</code>. Also the filename is converted to the OS
	 * format.
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> context for resolving macros.
	 * @return <code>String</code> with an avrdude option
	 */
	public String getArgument(IConfiguration buildcfg) {

		StringBuffer sb = new StringBuffer("-U");
		sb.append(fMemType.name());
		sb.append(":");
		sb.append(fAction.symbol);
		sb.append(":");

		// Windows needs quotes around the filename
		sb.append(WINQUOTE);

		// Two types: immediate mode or file mode
		// in the first case the immediate value is added to the output
		// in the second case the filename is included in the output
		if (fFileType.equals(FileType.immediate)) {
			// Eye Candy. We could just pass the integer value.
			// However, because the user always sees hex values for the
			// fusebytes in th user interface, we convert the byte value to hex
			// here as well.
			String hexvalue = Integer.toHexString(fImmediateValue);
			sb.append("0x" + hexvalue);

		} else {
			// We insert the Filename
			// Resolve the filename if we have a IConfiguration to resolve
			// against. Also change to path to the OS format while we are at it
			String filename;
			if (buildcfg != null) {
				String resolvedfilename = resolveMacros(buildcfg, fFilename);
				filename = new Path(resolvedfilename).toOSString();
			} else {
				filename = fFilename;
			}

			sb.append(filename);
		}

		sb.append(WINQUOTE);

		sb.append(":");
		sb.append(fFileType.symbol);

		return sb.toString();
	}

	/**
	 * Resolve all CDT macros in the given string.
	 * <p>
	 * If the string did not contain macros or the macros could not be resolved,
	 * the original string is returned.
	 * </p>
	 * 
	 * @param buildcfg
	 *            <code>IConfiguration</code> for the macro context.
	 * @param value
	 *            The source <code>String</code> with macros
	 * @return The new <code>String</code> with all macros resolved.
	 */
	private String resolveMacros(IConfiguration buildcfg, String string) {

		String resolvedstring = string;

		IBuildMacroProvider provider = ManagedBuildManager.getBuildMacroProvider();

		try {
			resolvedstring = provider.resolveValue(string,
			        "", " ", IBuildMacroProvider.CONTEXT_CONFIGURATION, buildcfg); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (BuildMacroException e) {
			// Do nothing = return the original string
		}

		return resolvedstring;
	}
}
