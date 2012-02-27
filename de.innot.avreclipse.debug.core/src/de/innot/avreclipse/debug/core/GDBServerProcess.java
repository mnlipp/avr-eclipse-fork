/*******************************************************************************
 * Copyright (c) 2012 Michael Lipp (mnl@mnl.de) and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Michael Lipp - initial API and implementation
 *******************************************************************************/

package de.innot.avreclipse.debug.core;

import java.util.Map;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.RuntimeProcess;

public class GDBServerProcess extends RuntimeProcess {

	public GDBServerProcess(ILaunch launch, Process process, String name,
			Map<String, String> attributes) {
		super(launch, process, name, attributes);
	}
	
}
