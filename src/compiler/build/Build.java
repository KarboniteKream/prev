package compiler.build;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.asmcode.*;

public class Build
{
	private boolean dump;

	public Build(boolean dump)
	{
		this.dump = dump;
	}

	public void build(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

				if(codeChunk.frame.size() < 256)
				{
					codeChunk.asmcode.add(1, new AsmOPER("SUB", "$250,$250," + codeChunk.frame.size(), null, null));
				}
				else
				{
					codeChunk.asmcode.add(1, new AsmOPER("SUB", "$250,$250,$0", null, null));
					codeChunk.asmcode.add(1, new AsmOPER("SET", "$0," + codeChunk.frame.size(), null, null));
				}

				codeChunk.asmcode.add(1, new AsmMOVE("SET", "`d0,`s0", codeChunk.frame.FP, codeChunk.frame.SP));
				codeChunk.asmcode.add(1, new AsmOPER("STO", "$1,$0,8", null, null));
				codeChunk.asmcode.add(1, new AsmOPER("GET", "$1,rJ", null, null));
				codeChunk.asmcode.add(1, new AsmOPER("STO", "$251,$0,0", null, null));

				if(codeChunk.frame.sizeLocs + 16 < 256)
				{
					codeChunk.asmcode.add(1, new AsmOPER("SUB", "$0,$250," + (codeChunk.frame.sizeLocs + 16), null, null));
				}
				else
				{
					codeChunk.asmcode.add(1, new AsmOPER("SUB", "$0,$250,$0", null, null));
					codeChunk.asmcode.add(1, new AsmOPER("SET", "$0," + (codeChunk.frame.sizeLocs + 16), null, null));
				}

				if(codeChunk.registers.get(codeChunk.frame.RV).equals("$0") == false)
				{
					codeChunk.asmcode.add(new AsmOPER("SET", "$0,`s0", null, new LinkedList<FrmTemp>(Arrays.asList(codeChunk.frame.RV))));
				}

				codeChunk.asmcode.add(new AsmMOVE("SET", "`d0,`s0", codeChunk.frame.SP, codeChunk.frame.FP));

				if(codeChunk.frame.sizeLocs + 16 < 256)
				{
					codeChunk.asmcode.add(new AsmOPER("SUB", "$1,$251," + (codeChunk.frame.sizeLocs + 16), null, null));
				}
				else
				{
					codeChunk.asmcode.add(new AsmOPER("SET", "$1," + (codeChunk.frame.sizeLocs + 16), null, null));
					codeChunk.asmcode.add(new AsmOPER("SUB", "$1,$251,$1", null, null));
				}

				codeChunk.asmcode.add(new AsmOPER("LDO", "$251,$1,0", null, null));
				codeChunk.asmcode.add(new AsmOPER("LDO", "$1,$1,8", null, null));
				codeChunk.asmcode.add(new AsmOPER("PUT", "rJ,$1", null, null));
				codeChunk.asmcode.add(new AsmOPER("POP", "1,0", null, null));
			}
		}

		ImcCodeChunk main = new ImcCodeChunk(null, null);
		main.asmcode = new LinkedList<AsmInstr>();

		main.asmcode.add(new AsmOPER("LOC", "#100", null, null));
		main.asmcode.add(new AsmLABEL("`l0", new FrmLabel("Main")));
		main.asmcode.add(new AsmOPER("PUT", "rG,250", null, null));
		main.asmcode.add(new AsmOPER("SET", "$251,8", null, null));
		main.asmcode.add(new AsmOPER("SL", "$251,$251,60", null, null));
		main.asmcode.add(new AsmOPER("SUB", "$251,$251,1", null, null));
		main.asmcode.add(new AsmOPER("SET", "$250,$251", null, null));
		main.asmcode.add(new AsmOPER("PUSHJ", "$0,_main", null, null));
		main.asmcode.add(new AsmOPER("TRAP", "0,Halt,0", null, null));

		chunks.addFirst(main);
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

				for(int j = 0; j < codeChunk.asmcode.size(); j++)
				{
					AsmInstr instr = codeChunk.asmcode.get(j);

					if(instr instanceof AsmLABEL == true)
					{
						if((j + 1) >= codeChunk.asmcode.size())
						{
							codeChunk.asmcode.add(new AsmOPER("SWYM", "", null, null));
						}

						Report.dump(0, String.format("%-8s", instr.format(codeChunk.registers)) + codeChunk.asmcode.get(++j).format(codeChunk.registers));
					}
					else
					{
						Report.dump(8, instr.format(codeChunk.registers));
					}
				}
			}
		}
	}
}
