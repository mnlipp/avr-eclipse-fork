/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IPath;

/**
 * @author U043192
 * 
 */
public abstract class BaseToolInfo {

	private String fCommandName = null;
	
	protected BaseToolInfo(String toolid) {
		// First: Get the command name from the toolchain
		ITool tool = ManagedBuildManager
        	.getExtensionTool(toolid);
		if (tool != null) {
			fCommandName = tool.getToolCommand();
			if (fCommandName.startsWith("-")) {
				// remove leading "-" in command name
				// (used to suppress "make" exit on errors)
				fCommandName = fCommandName.substring(1);
			}
		}
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
	
	public String getCommandName(){
		return fCommandName;
	}
}
