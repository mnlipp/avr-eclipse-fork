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
/**
 * 
 */
package de.innot.avreclipse.core.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Thomas
 * 
 */
public class AVRProjectNature implements IProjectNature {

	private IProject	fProject	= null;

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#configure()
	 */
	public void configure() throws CoreException {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#deconfigure()
	 */
	public void deconfigure() throws CoreException {

	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.core.resources.IProjectNature#getProject()
	 */
	public IProject getProject() {
		return fProject;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.eclipse.core.resources.IProjectNature#setProject(org.eclipse.core.resources.IProject)
	 */
	public void setProject(IProject project) {
		fProject = project;

	}

	public static void addAVRNature(IProject project) throws CoreException {
		final String natureid = "de.innot.avreclipse.core.avrnature";

		IProjectDescription description = project.getDescription();
		String[] oldnatures = description.getNatureIds();

		// Check if the project already has an AVR nature
		for (int i = 0; i < oldnatures.length; i++) {
			if (natureid.equals(oldnatures[i]))
				return; // return if AVR nature already set
		}
		String[] newnatures = new String[oldnatures.length + 1];
		System.arraycopy(oldnatures, 0, newnatures, 0, oldnatures.length);
		newnatures[oldnatures.length] = natureid;
		description.setNatureIds(newnatures);
		project.setDescription(description, new NullProgressMonitor());
	}
}
