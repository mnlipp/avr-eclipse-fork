/*******************************************************************************
 * Copyright (c) 2007 - 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson - initial API and implementation this class is based on
 *     QNX Software Systems - Initial implementation for Jtag debugging
 *     Sage Electronic Engineering, LLC - bug 305943
 *              - API generalization to become transport-independent (allow
 *                connections via serial ports and pipes).
 *     John Dallaway - Wrong groupId during initialization (Bug 349736)           
 *******************************************************************************/
package de.innot.avreclipse.debug.core;

/**
 * @author Andy Jin
 *
 */

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConstants;
import org.eclipse.cdt.debug.gdbjtag.core.Messages;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContribution;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.GDBJtagDeviceContributionFactory;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.IGDBJtagDevice;
import org.eclipse.cdt.dsf.concurrent.CountingRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DataRequestMonitor;
import org.eclipse.cdt.dsf.concurrent.DsfExecutor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitor;
import org.eclipse.cdt.dsf.concurrent.RequestMonitorWithProgress;
import org.eclipse.cdt.dsf.concurrent.Sequence;
import org.eclipse.cdt.dsf.datamodel.DMContexts;
import org.eclipse.cdt.dsf.datamodel.DataModelInitializedEvent;
import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.gdb.IGDBLaunchConfigurationConstants;
import org.eclipse.cdt.dsf.gdb.launching.GdbLaunch;
import org.eclipse.cdt.dsf.gdb.launching.LaunchMessages;
import org.eclipse.cdt.dsf.gdb.launching.LaunchUtils;
import org.eclipse.cdt.dsf.gdb.service.IGDBBackend;
import org.eclipse.cdt.dsf.gdb.service.SessionType;
import org.eclipse.cdt.dsf.gdb.service.command.IGDBControl;
import org.eclipse.cdt.debug.gdbjtag.core.IGDBJtagConnection;
import org.eclipse.cdt.debug.gdbjtag.core.jtagdevice.DefaultGDBJtagConnectionImpl;
import org.eclipse.cdt.dsf.mi.service.IMIContainerDMContext;
import org.eclipse.cdt.dsf.mi.service.IMIProcesses;
import org.eclipse.cdt.dsf.mi.service.MIBreakpointsManager;
import org.eclipse.cdt.dsf.mi.service.MIProcesses;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.CLICommand;
import org.eclipse.cdt.dsf.mi.service.command.output.MIInfo;
import org.eclipse.cdt.dsf.service.DsfServicesTracker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.variables.VariablesPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * The final launch sequence for the Jtag hardware debugging using the
 * DSF/GDB debugger framework.
 * <p>
 * This class is based on the implementation of the standard DSF/GDB debugging
 * <code>org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence</code>
 * <p>
 * It adds Jtag hardware debugging specific steps to initialize remote target
 * and start the remote Jtag debugging.
 * <p>
 * @since 7.0
 */
public class AVRGDBFinalLaunchSequence extends Sequence {

	/** utility method; cuts down on clutter */
	private void queueCommands(List<String> commands, RequestMonitor rm) {
		if (!commands.isEmpty()) { 
			fCommandControl.queueCommand(
	    		new CLICommand<MIInfo>(fCommandControl.getContext(), composeCommand(commands)),
	        	new DataRequestMonitor<MIInfo>(getExecutor(), rm));
		}
		else {
			rm.done();
		}
	}

	private Step[] fSteps = new Step[] {
		/*
		 * Create the service tracker for later use
		 */
		new Step() { 
        	@Override
            public void execute(RequestMonitor requestMonitor) {
                fTracker = new DsfServicesTracker(AVRDebugPlugin.getBundleContext(), fLaunch.getSession().getId());
                requestMonitor.done();
            }
            @Override
            public void rollBack(RequestMonitor requestMonitor) {
                if (fTracker != null) fTracker.dispose();
                fTracker = null;
                requestMonitor.done();
            }},
        /*
         * Fetch the GDBBackend service for later use
         */
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                fGDBBackend = fTracker.getService(IGDBBackend.class);
                if (fGDBBackend == null) {
            		requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot obtain GDBBackend service", null)); //$NON-NLS-1$
                }

                requestMonitor.done();
            }},
        /*
         * Fetch the control service for later use
         */
        new Step() {
            @Override
            public void execute(RequestMonitor requestMonitor) {
                fCommandControl = fTracker.getService(IGDBControl.class);
                if (fCommandControl == null) {
            		requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot obtain control service", null)); //$NON-NLS-1$
                }
                else {
                	fCommandFactory = fCommandControl.getCommandFactory();
                }
                
                requestMonitor.done();
            }},
        /*
         * Fetch the process service for later use
         */
        new Step() { 
            @Override
            public void execute(RequestMonitor requestMonitor) {
                fProcService = fTracker.getService(IMIProcesses.class);
                if (fProcService == null) {
            		requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot obtain process service", null)); //$NON-NLS-1$
                }

                requestMonitor.done();
            }},
        /*
         * Specify GDB's working directory
         */
        new Step() { 
        	@Override
        	public void execute(final RequestMonitor requestMonitor) {
        		IPath dir = null;
        		try {
        			dir = fGDBBackend.getGDBWorkingDirectory();
        		} catch (CoreException e) {
        			requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot get working directory", e)); //$NON-NLS-1$
        			requestMonitor.done();
        			return;
        		}

        		if (dir != null) {
        			fCommandControl.queueCommand(
        					fCommandFactory.createMIEnvironmentCD(fCommandControl.getContext(), dir.toPortableString()), 
        					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        		} else {
        			requestMonitor.done();
        		}
        	}},
	/*
    	 * Source the gdbinit file specified in the launch
    	 */
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	try {
            		final String gdbinitFile = fGDBBackend.getGDBInitFile();
            		
            		if (gdbinitFile != null && gdbinitFile.length() > 0) {
            			fCommandControl.queueCommand(
            					fCommandFactory.createCLISource(fCommandControl.getContext(), gdbinitFile), 
            					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
            						@Override
            						protected void handleCompleted() {
            							// If the gdbinitFile is the default, then it may not exist and we
            							// should not consider this an error.
            							// If it is not the default, then the user must have specified it and
            							// we want to warn the user if we can't find it.
            							if (!gdbinitFile.equals(IGDBLaunchConfigurationConstants.DEBUGGER_GDB_INIT_DEFAULT )) {
            								requestMonitor.setStatus(getStatus());
            							}
            							requestMonitor.done();
            						}
            					});
            		} else {
            			requestMonitor.done();
            		}
            	} catch (CoreException e) {
            		requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot get gdbinit option", e)); //$NON-NLS-1$
            		requestMonitor.done();
            	}
            }},
    	/*
    	 * Specify the arguments to the executable file
    	 */
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
        		try {
        			String args = fGDBBackend.getProgramArguments();
        			
            		if (args != null) {
        				String[] argArray = args.replaceAll("\n", " ").split(" ");  //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        				IMIContainerDMContext containerDmc = fProcService.createContainerContextFromGroupId(fCommandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID);
            			fCommandControl.queueCommand(
            					fCommandFactory.createMIGDBSetArgs(containerDmc, argArray), 
            					new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
            		} else {
            			requestMonitor.done();
            		}
        		} catch (CoreException e) {
        			requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot get inferior arguments", e)); //$NON-NLS-1$
        			requestMonitor.done();
        		}    		
            }},
    	/*
    	 * Specify environment variables if needed
    	 */
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	boolean clear = false;
       			Properties properties = new Properties();
           		try {
           			clear = fGDBBackend.getClearEnvironment();
           			properties = fGDBBackend.getEnvironmentVariables();
           		} catch (CoreException e) {
           			requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot get environment information", e)); //$NON-NLS-1$
           			requestMonitor.done();
           			return;
          		}

            	if (clear == true || properties.size() > 0) {
            		fCommandControl.setEnvironment(properties, clear, requestMonitor);
            	} else {
            		requestMonitor.done();
            	}
            }},
    	/*
    	 * Enable non-stop mode if necessary
    	 */
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	boolean isNonStop = false;
        		try {
        			isNonStop = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_NON_STOP,
                                                                              LaunchUtils.getIsNonStopModeDefault());
            	} catch (CoreException e) {    		
            	}

            	// GDBs that don't support non-stop don't allow you to set it to false.
            	// We really should set it to false when GDB supports it though.
            	// Something to fix later.
            	if (isNonStop) {
            		// The raw commands should not be necessary in the official GDB release
            		fCommandControl.queueCommand(
            			fCommandFactory.createMIGDBSetTargetAsync(fCommandControl.getContext(), true),
           				new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
           					@Override
           					protected void handleSuccess() {
           						fCommandControl.queueCommand(
           							fCommandFactory.createMIGDBSetPagination(fCommandControl.getContext(), false), 
       								new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
       									@Override
       									protected void handleSuccess() {
       										fCommandControl.queueCommand(
       											fCommandFactory.createMIGDBSetNonStop(fCommandControl.getContext(), true), 
    											new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
          									}
          								});
           					}
    					});
            	} else {
            		requestMonitor.done();
            	}
            }},
        /*
         * Tell GDB to automatically load or not the shared library symbols
         */
        new Step() { 
            public void execute(RequestMonitor requestMonitor) {
        		try {
        			boolean autolib = fLaunch.getLaunchConfiguration().getAttribute(IGDBLaunchConfigurationConstants.ATTR_DEBUGGER_AUTO_SOLIB,
        					                                                        IGDBLaunchConfigurationConstants.DEBUGGER_AUTO_SOLIB_DEFAULT);
                    fCommandControl.queueCommand(
                    	fCommandFactory.createMIGDBSetAutoSolib(fCommandControl.getContext(), autolib), 
                    	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
        		} catch (CoreException e) {
        			requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot set shared library option", e)); //$NON-NLS-1$
        			requestMonitor.done();
        		}
            }},
        /*
         * Set the shared library paths
         */
        new Step() { 
            @Override
            public void execute(final RequestMonitor requestMonitor) {
          		try {
        			List<String> p = fGDBBackend.getSharedLibraryPaths();
          		    
       				if (p.size() > 0) {
       					String[] paths = p.toArray(new String[p.size()]);
       	                fCommandControl.queueCommand(
       	                	fCommandFactory.createMIGDBSetSolibSearchPath(fCommandControl.getContext(), paths), 
       	                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor) {
       	                		@Override
       	                		protected void handleSuccess() {
    // Sysroot is not available in GDB6.6 and will make the launch fail in that case.
    // Let's remove it for now
       	                			requestMonitor.done();
//	       	                			// If we are able to set the solib-search-path,
//	       	                			// we should disable the sysroot variable, as indicated
//	       	                			// in the GDB documentation.  This is to avoid the sysroot
//	       	                			// variable finding libraries that were not meant to be found.
//	       	        	                fCommandControl.queueCommand(
//	       	        	   	                	new MIGDBSetSysroot(fCommandControl.getContext()), 
//	       	        	   	                	new DataRequestMonitor<MIInfo>(getExecutor(), requestMonitor));
       	                		};
       	                	});
       				} else {
       	                requestMonitor.done();
       				}
        		} catch (CoreException e) {
                    requestMonitor.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot set share library paths", e)); //$NON-NLS-1$
                    requestMonitor.done();
        		}
        	}},

// -environment-directory with a lot of paths could
// make setting breakpoint incredibly slow, which makes
// the debug session un-workable.  We simply stop
// using it because it's usefulness is unclear.
// Bug 225805
//    	/*
//    	 * Setup the source paths
//    	 */
//        new Step() { 
//    		@Override
//            public void execute(RequestMonitor requestMonitor) {
//                CSourceLookup sourceLookup = fTracker.getService(CSourceLookup.class);
//                CSourceLookupDirector locator = (CSourceLookupDirector)fLaunch.getSourceLocator();
//        		ISourceLookupDMContext sourceLookupDmc = (ISourceLookupDMContext)fCommandControl.getContext();
//
//                sourceLookup.setSourceLookupPath(sourceLookupDmc, locator.getSourceContainers(), requestMonitor);
//            }},
  
            // Below steps are specific to JTag hardware debugging
            
        /*
         * Retrieve the IGDBJtagDevice instance
         */
        new Step() {
			@Override
        	public void execute(RequestMonitor rm) {
				fGdbJtagDevice = new DefaultGDBJtagConnectionImpl();
				rm.done();
            }},
        /*
         * Execute symbol loading
         */
        new Step() {
			@Override
			public void execute(RequestMonitor rm) {
				ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
				try {
					String symbolsFileName = null;
					IPath programFile = CDebugUtils.verifyProgramPath(config);
					if (programFile != null) {
						symbolsFileName = programFile.toOSString();
					}

					if (symbolsFileName == null) {
						rm.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, Messages.getString("GDBJtagDebugger.err_no_img_file"), null)); //$NON-NLS-1$
						rm.done();
						return;
					}

					// Escape windows path separator characters TWICE, once for Java and once for GDB.						
					symbolsFileName = symbolsFileName.replace("\\", "\\\\"); //$NON-NLS-1$ //$NON-NLS-2$

					String symbolsOffset = config.getAttribute(IGDBJtagConstants.ATTR_SYMBOLS_OFFSET, IGDBJtagConstants.DEFAULT_SYMBOLS_OFFSET);
					if (symbolsOffset.length() > 0) {
						symbolsOffset = "0x" + symbolsOffset;					
					}
					List<String> commands = new ArrayList<String>();
					fGdbJtagDevice.doLoadSymbol(symbolsFileName, symbolsOffset, commands);
					queueCommands(commands, rm);									
					rm.done();
				} catch (CoreException e) {
        			rm.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot load symbol", e)); //$NON-NLS-1$
        			rm.done();
				}
			}},

        /*
         * Hook up to remote target
         */
        new Step() {
			@Override
			public void execute(RequestMonitor rm) {
				ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
				try {
					List<String> commands = new ArrayList<String>();
					if (fGdbJtagDevice instanceof IGDBJtagConnection) {
						String ipAddress = config.getAttribute(IAVRGDBConstants.ATTR_GDBSERVER_IP_ADDRESS, IAVRGDBConstants.DEFAULT_GDBSERVER_IP_ADDRESS);
						int portNumber = config.getAttribute(IAVRGDBConstants.ATTR_GDBSERVER_PORT_NUMBER, IAVRGDBConstants.DEFAULT_GDBSERVER_PORT_NUMBER);
						((IGDBJtagConnection)fGdbJtagDevice).doRemote(ipAddress + ":" + portNumber, commands);
						queueCommands(commands, rm);
					} else {
						rm.done();
					}
				} catch (CoreException e) {
        			rm.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot connect to remote target", e)); //$NON-NLS-1$
        			rm.done();
				}
			}},
        /* 
         * Start tracking the breakpoints once we know we are connected to the target (necessary for remote debugging) 
         */
        new Step() { 
        	@Override
	        public void execute(final RequestMonitor requestMonitor) {
	           	if (fSessionType != SessionType.CORE) {
	           		MIBreakpointsManager bpmService = fTracker.getService(MIBreakpointsManager.class);
	    			IMIContainerDMContext containerDmc = fProcService.createContainerContextFromGroupId(fCommandControl.getContext(), MIProcesses.UNIQUE_GROUP_ID);
	    			IBreakpointsTargetDMContext bpTargetDmc = DMContexts.getAncestorOfType(containerDmc, IBreakpointsTargetDMContext.class);
	
	           		bpmService.startTrackingBreakpoints(bpTargetDmc, requestMonitor);
	           	} else {
	           		requestMonitor.done();
	           	}
	        }},
        /*
         * Execute the stop script
         */
        new Step() {
			@Override
			public void execute(RequestMonitor rm) {
				ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
				try {
					if (config.getAttribute(IGDBJtagConstants.ATTR_SET_STOP_AT, IGDBJtagConstants.DEFAULT_SET_STOP_AT)) {
						String stopAt = config.getAttribute(IGDBJtagConstants.ATTR_STOP_AT, IGDBJtagConstants.DEFAULT_STOP_AT); //$NON-NLS-1$
						List<String> commands = new ArrayList<String>();
						fGdbJtagDevice.doStopAt(stopAt, commands);
						queueCommands(commands, rm);								
					} else {
						rm.done();
					}
				} catch (CoreException e) {
        			rm.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot run the stop script", e)); //$NON-NLS-1$
        			rm.done();
				}
			}
        },
        /*
         * Execute the resume script
         */
        new Step() {
			@Override
			public void execute(RequestMonitor rm) {
				ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
				try {
					if (config.getAttribute(IGDBJtagConstants.ATTR_SET_RESUME, IGDBJtagConstants.DEFAULT_SET_RESUME)) {
						List<String> commands = new ArrayList<String>();
						fGdbJtagDevice.doContinue(commands);
						queueCommands(commands, rm);									
					} else {
						rm.done();
					}
				} catch (CoreException e) {
        			rm.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot resume the remote target", e)); //$NON-NLS-1$
        			rm.done();
				}
			}},
        /*
         * Run any user defined commands to start debugging
         */
        new Step() {
			@Override
			public void execute(RequestMonitor rm) {
				ILaunchConfiguration config = fLaunch.getLaunchConfiguration();
				try {
					String userCmd = config.getAttribute(IGDBJtagConstants.ATTR_RUN_COMMANDS, IGDBJtagConstants.DEFAULT_RUN_COMMANDS); //$NON-NLS-1$
					if (userCmd.length() > 0) {
						userCmd = VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(userCmd);
						String[] commands = userCmd.split("\\r?\\n"); //$NON-NLS-1$
						
						CountingRequestMonitor crm = new CountingRequestMonitor(getExecutor(), rm);
						crm.setDoneCount(commands.length);
						for (int i = 0; i < commands.length; ++i) {
							fCommandControl.queueCommand(
							    new CLICommand<MIInfo>(fCommandControl.getContext(), commands[i]),
							    new DataRequestMonitor<MIInfo>(getExecutor(), crm));
						}
					}
					else {
						rm.done();
					}
				} catch (CoreException e) {
        			rm.setStatus(new Status(IStatus.ERROR, AVRDebugPlugin.PLUGIN_ID, -1, "Cannot run user defined run commands", e)); //$NON-NLS-1$
        			rm.done();
				}
			}},

        /*
         * Indicate that the Data Model has been filled.  This will trigger the Debug view to expand.
         */
        new Step() {
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	fLaunch.getSession().dispatchEvent(new DataModelInitializedEvent(fCommandControl.getContext()),
            			                           fCommandControl.getProperties());
            	requestMonitor.done();
            }},
        /*
         * Cleanup
         */
        new Step() {
            @Override
            public void execute(final RequestMonitor requestMonitor) {
            	fTracker.dispose();
                fTracker = null;
                requestMonitor.done();
            }},
	};
	     
	GdbLaunch fLaunch;
    SessionType fSessionType;
    boolean fAttach;

    private IGDBControl fCommandControl;
    private IGDBBackend	fGDBBackend;
    private IMIProcesses fProcService;
    private CommandFactory fCommandFactory;
    private IGDBJtagDevice fGdbJtagDevice;

    DsfServicesTracker fTracker;
        
    /**
	 * @since 8.0
	 */
    public AVRGDBFinalLaunchSequence(DsfExecutor executor, GdbLaunch launch, SessionType sessionType, boolean attach, RequestMonitorWithProgress rm) {
        super(executor, rm, LaunchMessages.getString("FinalLaunchSequence.0"), LaunchMessages.getString("FinalLaunchSequence.1"));     //$NON-NLS-1$ //$NON-NLS-2$
        fLaunch = launch;
        fSessionType = sessionType;
        fAttach = attach;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.dsf.gdb.launching.FinalLaunchSequence#getSteps()
	 */
	@Override
	public Step[] getSteps() {
		return fSteps;
	}
	
	/**
	 * @param commands
	 * @return String commands in String format
	 */
	private String composeCommand(Collection<String> commands) {
		if (commands.isEmpty())
			return null;
		StringBuffer sb = new StringBuffer();
		Iterator<String> it = commands.iterator();
		while (it.hasNext()) {
			sb.append(it.next());
		}
		return sb.toString();
	}
}
