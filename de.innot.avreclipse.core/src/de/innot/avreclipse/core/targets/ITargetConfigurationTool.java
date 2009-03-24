/*******************************************************************************
 * 
 * Copyright (c) 2009 Thomas Holland (thomas@innot.de) and others
 * 
 * This program and the accompanying materials are made
 * available under the terms of the GNU Public License v3
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl.html
 * 
 * Contributors:
 *     Thomas Holland - initial API and implementation
 *     
 * $Id$
 *     
 *******************************************************************************/

package de.innot.avreclipse.core.targets;

import java.util.Map;
import java.util.Set;

/**
 * @author Thomas Holland
 * @since 2.4
 * 
 */
public interface ITargetConfigurationTool {

	public Map<String, String> getDefaults();

	public String getId();

	public String getName();

	public Set<String> getMCUs(ITargetConfiguration tc);

	public Set<IProgrammer> getProgrammers(ITargetConfiguration tc);

}
