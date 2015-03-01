/**
 * 
 */
package de.innot.avreclipse.debug.core.service;

import org.eclipse.cdt.dsf.gdb.service.GDBMemory_7_6;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @author mnl
 *
 */
public class PatchedGDBMemory_7_6 extends GDBMemory_7_6 {

	/**
	 * @param session
	 */
	public PatchedGDBMemory_7_6(DsfSession session) {
		super(session);
	}

	@Override
	public int getAddressSize(IMemoryDMContext context) {
		return 8;
	}
}
