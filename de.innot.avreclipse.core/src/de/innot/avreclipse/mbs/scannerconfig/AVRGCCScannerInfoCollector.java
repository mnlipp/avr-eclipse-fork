package de.innot.avreclipse.mbs.scannerconfig;

import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector3;
import org.eclipse.cdt.make.internal.core.scannerconfig2.PerProjectSICollector;
import org.eclipse.cdt.managedbuilder.scannerconfig.IManagedScannerInfoCollector;

/** 
 * Gather built in compiler settings.
 * 
 * "Based" on DefaultGCCScannerInfoCollector, which unfortunatly is
 * not exposed from the org.eclipse.cdt.managedbuilder.core plugin.
 *
 */
public class AVRGCCScannerInfoCollector extends PerProjectSICollector implements IScannerInfoCollector3, IManagedScannerInfoCollector{

}
