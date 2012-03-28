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
package de.innot.avreclipse.debug.gdbservers.avarice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.cdt.core.CommandLauncher;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

import de.innot.avreclipse.debug.core.GDBServerProcess;
import de.innot.avreclipse.debug.gdbserver.IGDBServerFactory;

public class AvariceGDBServerFactory implements IGDBServerFactory,
	IGDBServerAvariceConstants {

	public void launchServer(ILaunchConfiguration config,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		CommandLauncher cl = new CommandLauncher();
		String cmd = config.getAttribute(ATTR_GDBSERVER_AVARICE_COMMAND,
				DEFAULT_GDBSERVER_AVARICE_COMMAND);
		String itf = config.getAttribute(ATTR_GDBSERVER_AVARICE_INTERFACE, 
				DEFAULT_GDBSERVER_AVARICE_INTERFACE);
		String port = config.getAttribute(ATTR_GDBSERVER_AVARICE_JTAGPORT,
				DEFAULT_GDBSERVER_AVARICE_JTAGPORT);
		String bitRate = config.getAttribute(ATTR_GDBSERVER_AVARICE_JTAGBITRATE,
				DEFAULT_GDBSERVER_AVARICE_JTAGBITRATE);
		boolean debWire = config.getAttribute(ATTR_GDBSERVER_AVARICE_DEBUGWIRE,
				DEFAULT_GDBSERVER_AVARICE_DEBUGWIRE);
		boolean ignoreIntr = config.getAttribute(ATTR_GDBSERVER_AVARICE_IGNOREINTR,
				DEFAULT_GDBSERVER_AVARICE_IGNOREINTR);
		int portNumber = config.getAttribute(ATTR_GDBSERVER_AVARICE_PORT,
				DEFAULT_GDBSERVER_AVARICE_PORT);
		
		List<String> args = new ArrayList<String>();
		args.add(itf);
		args.add("-j");
		args.add(port);
		if (bitRate.length() > 0) {
			args.add("-B");
			args.add(bitRate);
		}
		if (debWire) {
			args.add("-w");
		}
		if (ignoreIntr) {
			args.add("-I");
		}
		
		String oopts = config.getAttribute(ATTR_GDBSERVER_AVARICE_OTHEROPTIONS, 
				DEFAULT_GDBSERVER_AVARICE_OTHEROPTIONS);
		if (oopts.length() > 0) {
			String[] opts = oopts.split(" ");
			args.addAll(Arrays.asList(opts));
		}
		
		args.add(":" + portNumber);
		
		Process process = cl.execute(new Path(cmd), 
				args.toArray(new String[args.size()]), new String[] {}, new Path("/"), null);
		new GDBServerProcess(launch, process, "Avarice", null);
	}

}
