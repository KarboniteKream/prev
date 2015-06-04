package compiler.regalloc;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.asmcode.*;
import compiler.tmpan.*;

public class RegAlloc
{
	private boolean dump;
	private int R;

	public RegAlloc(boolean dump, int R)
	{
		this.dump = dump;
		this.R = R;
	}

	public void allocate(LinkedList<ImcChunk> chunks)
	{
	}

	private void build(ImcCodeChunk chunk)
	{
	}

	private void simplify(ImcCodeChunk chunk)
	{
	}

	private void spill()
	{
	}

	private void select()
	{
	}

	private void startOver()
	{

	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if(dump == false) return;
		if(Report.dumpFile() == null) return;

		int i = 0;

		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				if(i++ > 0)
				{
					Report.dump(0, "");
				}

				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

				for(AsmInstr instr : codeChunk.asmcode)
				{
					Report.dump(0, instr.format(codeChunk.registers));
				}
			}
		}
	}
}
