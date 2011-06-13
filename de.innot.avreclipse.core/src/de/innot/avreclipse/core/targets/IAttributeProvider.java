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

import de.innot.avreclipse.core.targets.ITargetConfiguration.ValidationResult;

/**
 * @author Thomas Holland
 * @since
 * 
 */
public interface IAttributeProvider {

	public String[] getAttributes();

	public String getDefaultValue(String attribute);

	public ValidationResult validate(String attribute);

}
