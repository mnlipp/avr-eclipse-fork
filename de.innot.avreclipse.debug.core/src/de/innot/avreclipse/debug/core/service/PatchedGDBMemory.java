/**
 * 
 */
package de.innot.avreclipse.debug.core.service;

import org.eclipse.cdt.dsf.debug.service.IMemory.IMemoryDMContext;
import org.eclipse.cdt.dsf.gdb.service.GDBMemory;
import org.eclipse.cdt.dsf.service.DsfSession;

/**
 * @author mnl
 *
 */
public class PatchedGDBMemory extends GDBMemory {

	/**
	 * @param session
	 */
	public PatchedGDBMemory(DsfSession session) {
		super(session);
	}

	@Override
	public int getAddressSize(IMemoryDMContext context) {
		return 8;
	}
}
