/**
 * 
 */
package de.innot.avreclipse.debug.core.service;

import org.eclipse.cdt.dsf.debug.service.IDisassembly.IDisassemblyDMContext;
import org.eclipse.cdt.dsf.debug.service.command.ICommand;
import org.eclipse.cdt.dsf.mi.service.command.CommandFactory;
import org.eclipse.cdt.dsf.mi.service.command.commands.MIDataDisassemble;
import org.eclipse.cdt.dsf.mi.service.command.output.MIDataDisassembleInfo;

/**
 * Patched command factory to work around a long standing bug in gdb
 * (https://sourceware.org/bugzilla/show_bug.cgi?format=multiple&id=13519).
 * 
 * @author mnl
 */
public class PatchedCommandFactory extends CommandFactory {

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

}
