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
	private int registers;

	private TmpAn tmpan;
	private LinkedList<TmpNode> stack;

	public RegAlloc(boolean dump)
	{
		this.dump = dump;
		// remove
		// na voljo imamo 5 registrov, ampak moramo nekam shraniti se SP in FP. FP bi bilo zelo dobro imeti v registrih,
		// ker se veliko uporablja, SP pa lahko pustimo v pomnilniku
		this.registers = 5;

		tmpan = new TmpAn(false);
		stack = null;
	}

	public void allocate(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

				do
				{
					build(codeChunk);
					stack = new LinkedList<TmpNode>();

					do
					{
						simplify(codeChunk);
					} while(spill(codeChunk) == true);
				} while(select(codeChunk) == true);

				codeChunk.registers = new HashMap<FrmTemp, String>();
				codeChunk.registers.put(codeChunk.frame.FP, "X");
				codeChunk.registers.put(codeChunk.frame.SP, "250");

				for(TmpNode node : codeChunk.graph)
				{
					codeChunk.registers.put(node.temp, "" + node.register);
				}

				LinkedHashMap<FrmTemp, TmpNode> graph = tmpan.analyze(codeChunk);

				for(int i = 0; i < codeChunk.asmcode.size(); i++)
				{
					AsmInstr instr = codeChunk.asmcode.get(i);

					if(instr.mnemonic.equals("JSUB") == true)
					{
						AsmInstr next = codeChunk.asmcode.get(i + 1);
						LinkedList<TmpNode> edges = graph.get(instr.defs.getFirst()).edges;
						int maxRegister = 0;

						for(TmpNode edge : edges)
						{
							int register = Integer.parseInt(codeChunk.registers.get(edge.temp));

							if(register > maxRegister)
							{
								maxRegister = register;
							}
						}

						// toString
						codeChunk.registers.put(instr.defs.getFirst(), "" + (edges.size() == 0 ? 0 : maxRegister + 1));

						if(next.mnemonic.equals("SET") == true && codeChunk.registers.get(next.defs.getFirst()).equals(codeChunk.registers.get(next.uses.getFirst())) == true)
						{
							codeChunk.asmcode.remove(i + 1);
						}
					}
				}
			}
		}
	}

	private void build(ImcCodeChunk chunk)
	{
		tmpan.analyze(chunk);
	}

	private void simplify(ImcCodeChunk chunk)
	{
		boolean done = false;

		while(done == false)
		{
			done = true;

			Iterator<TmpNode> iterator = chunk.graph.iterator();

			while(iterator.hasNext() == true)
			{
				TmpNode node = iterator.next();

				if(node.edges.size() < registers)
				{
					done = false;

					stack.push(node);

					for(TmpNode edge : chunk.graph)
					{
						edge.edges.remove(node);
					}

					iterator.remove();
				}
			}
		}
	}

	private boolean spill(ImcCodeChunk chunk)
	{
		if(chunk.graph.size() == 0)
		{
			return false;
		}

		TmpNode spill = null;
		int length = 0;

		for(TmpNode node : chunk.graph)
		{
			int def = 0;

			while(chunk.asmcode.get(def).defs.contains(node.temp) == false)
			{
				def++;
			}

			int use = chunk.asmcode.size() - 1;

			while(chunk.asmcode.get(use).uses.contains(node.temp) == false)
			{
				use--;
			}

			if(use - def > length)
			{
				spill = node;
				length = use - def;
			}
		}

		chunk.graph.remove(spill);
		spill.spill = TmpNode.POTENTIAL_SPILL;
		stack.push(spill);

		for(TmpNode edge : spill.edges)
		{
			edge.edges.remove(spill);
		}

		return true;
	}

	private boolean select(ImcCodeChunk chunk)
	{
		boolean repeat = false;

		while(stack.size() > 0)
		{
			TmpNode node = stack.pop();
			chunk.graph.add(node);

			int regs[] = new int[registers + 1];
			boolean ok = false;

			for(TmpNode edge : node.edges)
			{
				regs[edge.register] = 1;
			}

			for(int i = 0; i < registers; i++)
			{
				if(regs[i] == 0)
				{
					node.register = i;
					ok = true;
					break;
				}
			}

			for(AsmInstr instr : chunk.asmcode)
			{
				if(instr.mnemonic.equals("JSUB") == true && instr.defs.contains(node.temp) == true)
				{
					node.register = registers;
					ok = true;
					break;
				}
			}

			if(ok == false)
			{
				if(node.spill == TmpNode.POTENTIAL_SPILL)
				{
					node.spill = TmpNode.ACTUAL_SPILL;
					repeat = true;
					break;
				}
				else
				{
					Report.error("Unable to allocate a register to " + node.temp.name() + ".");
				}
			}
		}

		if(repeat == true)
		{
			startOver(chunk);
		}

		return repeat;
	}

	private void startOver(ImcCodeChunk chunk)
	{
		for(TmpNode node : chunk.graph)
		{
			if(node.spill != TmpNode.ACTUAL_SPILL)
			{
				continue;
			}

			long offset = chunk.frame.sizeArgs + chunk.frame.sizeTmps;
			chunk.frame.sizeTmps += 8;

			int def = 0;
			while(chunk.asmcode.get(def++).defs.contains(node.temp) == false) {}

			// TODO
			chunk.asmcode.add(def, new AsmOPER("STO", "`s0,`s1," + offset, null, new LinkedList<FrmTemp>(Arrays.asList(node.temp, chunk.frame.SP))));

			for(int i = chunk.asmcode.size() - 1; i > def; i--)
			{
				if(chunk.asmcode.get(i).uses.contains(node.temp) == true)
				{
					chunk.asmcode.add(i, new AsmOPER("LDO", "`d0,`s0," + offset, new LinkedList<FrmTemp>(Arrays.asList(node.temp)), new LinkedList<FrmTemp>(Arrays.asList(chunk.frame.SP))));
				}
			}
		}
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
