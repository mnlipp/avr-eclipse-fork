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
		iHex("i"), sRec("s"), raw("r"), immediate("m"), decimal("d"), hex("h"), octal("o"), binary("b"), auto("a");
		
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

	public AVRDudeAction() {

	}

	public String getArgument() {
		StringBuffer sb = new StringBuffer("-U");
		sb.append(fMemType.name());
		sb.append(":");
		sb.append(fAction.symbol);
		sb.append(":");
		
		if (fFileType.equals(FileType.immediate)) {
			sb.append(fFuseByte);
		} else {
			sb.append(fFile.toOSString());
		}
		
		sb.append(":");
		sb.append(fFileType);

		return sb.toString();
	}
}
