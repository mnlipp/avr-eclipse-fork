/**
 * 
 */
package de.innot.avreclipse.debug.core.service;

import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.gdb.service.GDBMemory_7_0;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @author mnl
 *
 */
public class PatchedGDBMemory_7_0 extends GDBMemory_7_0 {

	/**
	 * @param session
	 */
	public PatchedGDBMemory_7_0(DsfSession session) {
		super(session);
	}
	
	@Override
	public int getAddressSize(IMemoryDMContext context) {
		return 8;
	}
}
