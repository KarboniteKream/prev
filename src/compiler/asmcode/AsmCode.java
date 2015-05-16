package compiler.asmcode;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;

public class AsmCode
{
	private boolean dump;

	private LinkedList<AsmInstr> asmcode = null;

	public AsmCode(boolean dump)
	{
		this.dump = dump;
	}

	public void generate(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;
				asmcode = new LinkedList<AsmInstr>();

				for(ImcStmt statement : ((ImcSEQ)codeChunk.lincode).stmts)
				{
					parse(statement);
				}

				codeChunk.asmcode = asmcode;
			}
		}
	}

	private FrmTemp parse(ImcStmt statement)
	{
		return null;
	}

	private FrmTemp parse(ImcExpr expression)
	{
		return null;
	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if(dump == false)
		{
			return;
		}

		if(Report.dumpFile() == null)
		{
			return;
		}

		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

				for(AsmInstr instr : codeChunk.asmcode)
				{
					Report.dump(0, instr.format(null));
				}
			}
		}
	}
}
