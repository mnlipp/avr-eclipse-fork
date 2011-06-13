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

import java.util.Set;

import de.innot.avreclipse.core.avrdude.AVRDudeException;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface ITargetConfigurationTool extends IAttributeProvider {

	public String getId();

	public String getName();

	public String getVersion() throws AVRDudeException;

	public Set<String> getMCUs() throws AVRDudeException;

	public Set<String> getProgrammers() throws AVRDudeException;

	public IProgrammer getProgrammer(String id) throws AVRDudeException;
}
