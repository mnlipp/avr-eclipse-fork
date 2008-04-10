/**
 * 
 */
package de.innot.avreclipse.core.avrdude;

import org.eclipse.core.runtime.IPath;

/**
 * @author Thomas Holland
 * 
 */
public class AVRDudeAction {

	private final static String WINQUOTE = System.getProperty("os.name")
			.toLowerCase().contains("windows") ? "\"" : "";

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
		iHex("i"), sRec("s"), raw("r"), immediate("m"), decimal("d"), hex("h"), octal(
				"o"), binary("b"), auto("a");

		public String symbol;

		private FileType(String type) {
			symbol = type;
		}
	}

	private MemType fMemType;
	private Action fAction;
	private IPath fFile;
	private FileType fFileType;
	private int fFuseByte;

	public AVRDudeAction(MemType memtype, Action action, IPath file,
			FileType filetype) {
		fMemType = memtype;
		fAction = action;
		fFile = file;
		fFileType = filetype;
	}

	public String getArgument() {
		StringBuffer sb = new StringBuffer("-U");
		sb.append(fMemType.name());
		sb.append(":");
		sb.append(fAction.symbol);
		sb.append(":");

		// Windows needs quotes around the filename
		sb.append(WINQUOTE);

		if (fFileType.equals(FileType.immediate)) {
			sb.append(fFuseByte);
		} else {
			sb.append(fFile.toOSString());
		}

		sb.append(WINQUOTE);

		sb.append(":");
		sb.append(fFileType.symbol);

		return sb.toString();
	}
}
