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

import java.util.EventListener;

/**
 * Listener for Target Configuration changes.
 * 
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface ITargetConfigChangeListener extends EventListener {

	/**
	 * Notification that a Target Configuration attribute has changed.
	 * <p>
	 * This method gets called when any attribute of the observed target configuration is modified.
	 * </p>
	 * 
	 * @param config
	 *            The <code>TargetConfiguration</code> which has changed
	 * @param name
	 *            the name of the changed attribute
	 * @param oldValue
	 *            the old value, or <code>null</code> if not known or not relevant
	 * @param newValue
	 *            the new value, or <code>null</code> if not known or not relevant
	 */
	public void attributeChange(ITargetConfiguration config, String attribute, String oldvalue,
			String newvalue);

}
