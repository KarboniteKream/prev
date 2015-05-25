package compiler.tmpan;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.asmcode.*;

public class TmpAn
{
	private boolean dump;

	public TmpAn(boolean dump)
	{
		this.dump = dump;
	}

	public void analyze(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

				for(AsmInstr instr : codeChunk.asmcode)
				{
					instr.in = new LinkedList<FrmTemp>();
					instr.out = new LinkedList<FrmTemp>();
				}

				while(true)
				{
					int ok = 0;

					for(int i = codeChunk.asmcode.size() - 1; i >= 0; i--)
					{
						AsmInstr instr = codeChunk.asmcode.get(i);

						LinkedList<FrmTemp> oldIn = new LinkedList<FrmTemp>(instr.in);
						LinkedList<FrmTemp> oldOut = new LinkedList<FrmTemp>(instr.out);

						instr.in = new LinkedList<FrmTemp>(instr.uses);

						for(FrmTemp temp : instr.out)
						{
							if(instr.in.contains(temp) == false && instr.defs.contains(temp) == false)
							{
								instr.in.add(temp);
							}
						}

						LinkedList<AsmInstr> succ = new LinkedList<AsmInstr>();

						if(instr.mnemonic.equals("TRAP") == false && instr.mnemonic.equals("POP") == false && instr.mnemonic.equals("JMP") == false)
						{
							succ.add(codeChunk.asmcode.get(i + 1));
						}

						if(instr.mnemonic.equals("BNZ") == true || instr.mnemonic.equals("JMP") == true)
						{
							for(AsmInstr label : codeChunk.asmcode)
							{
								if(label instanceof AsmLABEL == true && ((AsmLABEL)label).labels.equals(instr.labels) == true)
								{
									succ.add(label);
									break;
								}
							}
						}

						instr.out = new LinkedList<FrmTemp>();

						for(AsmInstr succInstr : succ)
						{
							for(FrmTemp temp : succInstr.in)
							{
								if(instr.out.contains(temp) == false)
								{
									instr.out.add(temp);
								}
							}
						}

						if(instr.in.equals(oldIn) == true && instr.out.equals(oldOut) == true)
						{
							ok++;
						}
					}

					if(ok == codeChunk.asmcode.size())
					{
						break;
					}
				}
			}
		}
	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if(dump == false) return;
		if(Report.dumpFile() == null) return;
	}
}
