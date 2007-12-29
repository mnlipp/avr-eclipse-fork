/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.runtime.IPath;

/**
 * @author U043192
 * 
 */
public abstract class BaseToolInfo implements IToolInfo {

	private String fCommandName = null;
	
	// override this in extension classes
	protected String[] toolinfotypes = {};

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
	 * @see de.innot.avreclipse.core.toolinfo.IToolInfo#getToolInfo(java.lang.String)
	 */
	public Map<String, String> getToolInfo(String type) {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.innot.avreclipse.core.toolinfo.IToolInfo#getToolInfoTypes()
	 */
	public List<String> getToolInfoTypes() {
		List<String> types = Arrays.asList(toolinfotypes);
		return types;
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
