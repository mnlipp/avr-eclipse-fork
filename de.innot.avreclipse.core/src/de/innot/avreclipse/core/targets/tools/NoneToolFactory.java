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

package de.innot.avreclipse.core.targets.tools;

import java.util.Set;

import de.innot.avreclipse.core.avrdude.AVRDudeException;
import de.innot.avreclipse.core.targets.IGDBServerTool;
import de.innot.avreclipse.core.targets.IProgrammer;
import de.innot.avreclipse.core.targets.IProgrammerTool;
import de.innot.avreclipse.core.targets.ITargetConfiguration;
import de.innot.avreclipse.core.targets.ITargetConfigurationTool;
import de.innot.avreclipse.core.targets.IToolFactory;
import de.innot.avreclipse.core.targets.ITargetConfiguration.ValidationResult;

/**
 * Factory for the 'None' tool.
 * <p>
 * The 'None' tool stands for <em>no tool selected</em> and is a dummy tool that is used by the user
 * interface to handle the case where no tool is required without using <code>null</code> objects.
 * </p>
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public class NoneToolFactory implements IToolFactory {

	public final static String		ID			= "NONE";
	public final static String		NAME		= "None";

	private final static String[]	EMPTY_LIST	= new String[] {};

	private final NoneTool			fToolInstance;

	public NoneToolFactory() {
		fToolInstance = new NoneTool();
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * de.innot.avreclipse.core.targets.IToolFactory#createTool(de.innot.avreclipse.core.targets
	 * .ITargetConfiguration)
	 */
	public ITargetConfigurationTool createTool(ITargetConfiguration tc) {
		return fToolInstance;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.IToolFactory#getId()
	 */
	public String getId() {
		return ID;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.IToolFactory#getName()
	 */
	public String getName() {
		return NAME;
	}

	/*
	 * (non-Javadoc)
	 * @see de.innot.avreclipse.core.targets.IToolFactory#isType(java.lang.String)
	 */
	public boolean isType(String tooltype) {
		// The nonetool can represent all tool types
		return true;
	}

	/**
	 * This is a special virtual tool that represents no selected tool.
	 */
	private static class NoneTool implements IGDBServerTool, IProgrammerTool {
		/*
		 * (non-Javadoc)
		 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getId()
		 */
		public String getId() {
			return ID;
		}

		/*
		 * (non-Javadoc)
		 * @see de.innot.avreclipse.core.targets.ITargetConfigurationTool#getName()
		 */
		public String getName() {
			return NAME;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getVersion(de.innot.avreclipse.
		 * core.targets.ITargetConfiguration)
		 */
		public String getVersion() throws AVRDudeException {
			return getName();
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getMCUs(de.innot.avreclipse
		 * .core .targets.ITargetConfiguration)
		 */
		public Set<String> getMCUs() throws AVRDudeException {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammer(de.innot.avreclipse
		 * .core.targets.ITargetConfiguration, java.lang.String)
		 */
		public IProgrammer getProgrammer(String id) throws AVRDudeException {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#getProgrammers(de.innot.avreclipse
		 * .core.targets.ITargetConfiguration)
		 */
		public Set<String> getProgrammers() throws AVRDudeException {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see de.innot.avreclipse.core.targets.IAttributeProvider#getAttributes()
		 */
		public String[] getAttributes() {
			return EMPTY_LIST;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * de.innot.avreclipse.core.targets.IAttributeProvider#getDefaultValue(java.lang.String)
		 */
		public String getDefaultValue(String attribute) {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see
		 * de.innot.avreclipse.core.targets.ITargetConfigurationTool#validate(de.innot.avreclipse
		 * .core .targets.ITargetConfiguration, java.lang.String)
		 */
		public ValidationResult validate(String attr) {
			// TODO Auto-generated method stub
			return null;
		}

		/*
		 * (non-Javadoc)
		 * @see de.innot.avreclipse.core.targets.IGDBServerTool#isSimulator()
		 */
		public boolean isSimulator() {
			return false;
		}
	}
}
