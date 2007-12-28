/**
 * 
 */
package de.innot.avreclipse.core.toolinfo;

import java.util.List;
import java.util.Map;

/**
 * Interface for getting informations about a tool
 * 
 * <p>
 * </p>
 * 
 * @author Thomas Holland
 * @version 1.0
 */
public interface IToolInfo {

	public final static String TOOLINFOTYPE_MCUS = "mcus";
	public final static String TOOLINFOTYPE_PROGRAMMERS = "programmers";
		
	/** 
	 * Gets the OS specific Path to the parent folder of the tool
	 * 
	 * @return
	 */
	public String getToolPath();
	
	/**
	 * Gets a List of types supported by the {@link #getToolInfo(String)}.
	 * 
	 * @return
	 */
	public List<String> getToolInfoTypes();

	/**
	 * Gets a Map with the selected info about the tool.
	 * 
	 * The format of the returned map may depend on the selected type but is
	 * generally of the format <code>UI Description, internal value</code>
	 * 
	 * @param type
	 *            String with a type as returned by {@link #getToolInfoTypes()}
	 * @return
	 */
	public Map<String, String> getToolInfo(String type);

}
