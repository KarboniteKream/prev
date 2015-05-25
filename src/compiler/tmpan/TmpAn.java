package compiler.tmpan;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.asmcode.*;

public class TmpAn
{
	private boolean dump;
	private HashMap<FrmTemp, LinkedList<FrmTemp>> graph;

	public TmpAn(boolean dump)
	{
		this.dump = dump;
		graph = new HashMap<FrmTemp, LinkedList<FrmTemp>>();
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

				for(AsmInstr instr : codeChunk.asmcode)
				{
					if(instr.defs.size() == 0)
					{
						continue;
					}

					FrmTemp temp = instr.defs.getFirst();
					LinkedList<FrmTemp> temps = graph.get(temp) == null ? new LinkedList<FrmTemp>() : graph.get(temp);

					for(FrmTemp out : instr.out)
					{
						if(temp.equals(out) == false && temps.contains(out) == false && (instr instanceof AsmMOVE == false || (instr instanceof AsmMOVE == true && instr.uses.getFirst().equals(out) == false)))
						{
							LinkedList<FrmTemp> outTemps = graph.get(out) == null ? new LinkedList<FrmTemp>() : graph.get(out);
							outTemps.add(temp);
							graph.put(out, outTemps);

							temps.add(out);
						}
					}

					graph.put(temp, temps);
				}
			}
		}
	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if(dump == false) return;
		if(Report.dumpFile() == null) return;

		for(Map.Entry<FrmTemp, LinkedList<FrmTemp>> entry : graph.entrySet())
		{
			LinkedList<FrmTemp> temps = entry.getValue();

			if(temps != null && temps.size() > 0)
			{
				String output = entry.getKey().name() + ": ";

				for(FrmTemp temp : entry.getValue())
				{
					output += temp.name() + ", ";
				}

				Report.dump(0, output.substring(0, output.length() - 2));
			}
		}
	}
}
