/**
 * 
 */
package de.innot.avreclipse.debug.core.service;

import org.eclipse.cdt.dsf.debug.service.IBreakpoints.IBreakpointsTargetDMContext;
import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.gdb.service.command.CommandFactory_6_8;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIBreakInsert;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataDisassemble;
import org.eclipse.cdt.dsf.mi.service.command.output.MIBreakInsertInfo;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataDisassembleInfo;

/**
 * Patched command factory to work around a long standing bug in gdb
 * (https://sourceware.org/bugzilla/show_bug.cgi?format=multiple&id=13519).
 * 
 * @author mnl
 */
public class PatchedCommandFactory_6_8 extends CommandFactory_6_8 {

	@Override
	public ICommand<MIDataDisassembleInfo> createMIDataDisassemble(IDisassemblyDMContext ctx, String start, String end, boolean mode) {
		start = "(void (*)())" + start;
		end = "(void (*)())" + end;
		return new MIDataDisassemble(ctx, start, end, mode);
	}

	@Override
	public ICommand<MIDataDisassembleInfo> createMIDataDisassemble(IDisassemblyDMContext ctx, String start, String end, int mode) {
		start = "(void (*)())" + start;
		end = "(void (*)())" + end;
		return new MIDataDisassemble(ctx, start, end, mode);
	}

	@Override
	public ICommand<MIBreakInsertInfo> createMIBreakInsert(IBreakpointsTargetDMContext ctx, boolean isTemporary,
			boolean isHardware, String condition, int ignoreCount, String location, int tid, boolean disabled, boolean isTracepoint) {
		if (location.startsWith("*")) {
			location = location.substring(0, 1)
					+ "(void (*)())"
					+ location.substring(1);
		}
		return new MIBreakInsert(ctx, isTemporary, isHardware, condition, ignoreCount, location, tid, disabled, isTracepoint, true);
	}

}
