/*******************************************************************************
 * Copyright (c) 2008, 2011 Thomas Holland (thomas@innot.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *******************************************************************************/

package de.innot.avreclipse.core.targets;

/**
 * A tool factory can produce hardware configuration tools for a hardware configuration.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface IToolFactory {

	/**
	 * Get the id of the tools created by this factory.
	 * 
	 * @return tool id
	 */
	public String getId();

	/**
	 * Get the name of the tool created by this factory.
	 * <p>
	 * This is the same as <code>createTool().getName()</code>, but without needing a target
	 * configuration.
	 * </p>
	 * 
	 * @return Name of the tool.
	 */
	public String getName();

	/**
	 * Checks if the factory can produce tools of the given type.
	 * 
	 * @param tooltype
	 * @return
	 */
	public boolean isType(String tooltype);

	/**
	 * Create a new tool for the given hardware configuration.
	 * 
	 * @param tc
	 *            Reference to the hardware configuration that the tool belongs to.
	 * @return
	 */
	public ITargetConfigurationTool createTool(ITargetConfiguration tc);

}
