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
		if(dump == true)
		{
			for(ImcChunk chunk : chunks)
			{
				if(chunk instanceof ImcCodeChunk == true)
				{
					analyze((ImcCodeChunk)chunk);
				}
			}
		}
	}

	public LinkedHashMap<FrmTemp, TmpNode> analyze(ImcCodeChunk chunk)
	{
		LinkedHashMap<FrmTemp, TmpNode> graph = new LinkedHashMap<FrmTemp, TmpNode>();

		for(AsmInstr instr : chunk.asmcode)
		{
			instr.in = new LinkedList<FrmTemp>();
			instr.out = new LinkedList<FrmTemp>();
		}

		while(true)
		{
			int ok = 0;

			for(int i = chunk.asmcode.size() - 1; i >= 0; i--)
			{
				AsmInstr instr = chunk.asmcode.get(i);

				LinkedList<FrmTemp> oldIn = new LinkedList<FrmTemp>(instr.in);
				LinkedList<FrmTemp> oldOut = new LinkedList<FrmTemp>(instr.out);

				instr.in = new LinkedList<FrmTemp>(instr.uses);

				for(FrmTemp temp : instr.out)
				{
					if(temp != chunk.frame.FP && temp != chunk.frame.SP && instr.in.contains(temp) == false && instr.defs.contains(temp) == false)
					{
						instr.in.add(temp);
					}
				}

				LinkedList<AsmInstr> succ = new LinkedList<AsmInstr>();

				if(instr.mnemonic.equals("J") == false && (i + 1) < chunk.asmcode.size())
				{
					succ.add(chunk.asmcode.get(i + 1));
				}

				if(instr.mnemonic.equals("JEQ") == true || instr.mnemonic.equals("JGT") == true || instr.mnemonic.equals("JLT") == true || instr.mnemonic.equals("J") == true)
				{
					for(AsmInstr label : chunk.asmcode)
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
						if(temp != chunk.frame.FP && temp != chunk.frame.SP && instr.out.contains(temp) == false)
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

			if(ok == chunk.asmcode.size())
			{
				break;
			}
		}

		for(AsmInstr instr : chunk.asmcode)
		{
			if(instr.defs.size() == 0)
			{
				continue;
			}

			FrmTemp temp = instr.defs.getFirst();
			TmpNode node = graph.get(temp) == null ? new TmpNode(temp) : graph.get(temp);

			for(FrmTemp out : instr.out)
			{
				if(temp.equals(out) == false && (instr instanceof AsmMOVE == false || (instr instanceof AsmMOVE == true && instr.uses.getFirst().equals(out) == false)))
				{
					boolean ok = true;

					for(TmpNode edge : node.edges)
					{
						if(edge.temp == out)
						{
							ok = false;
							break;
						}
					}

					if(ok == true)
					{
						TmpNode edge = graph.get(out) == null ? new TmpNode(out) : graph.get(out);
						edge.edges.add(node);
						graph.put(out, edge);

						node.edges.add(edge);
					}
				}
			}

			graph.put(temp, node);
		}

		chunk.graph = new LinkedList<TmpNode>(graph.values());

		return graph;
	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if(dump == false || Report.dumpFile() == null) return;

		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				for(TmpNode node : ((ImcCodeChunk)chunk).graph)
				{
					if(node.edges.size() > 0)
					{
						String output = node.temp.name() + ": ";

						for(TmpNode edge : node.edges)
						{
							output += edge.temp.name() + ", ";
						}

						Report.dump(0, output.substring(0, output.length() - 2));
					}
				}
			}
		}
	}
}
