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

	public RegAlloc(boolean dump, int registers)
	{
		this.dump = dump;
		this.registers = registers;

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
				codeChunk.registers.put(codeChunk.frame.FP, "$251");
				codeChunk.registers.put(codeChunk.frame.SP, "$250");

				for(TmpNode node : codeChunk.graph)
				{
					codeChunk.registers.put(node.temp, "$" + node.register);
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

			int regs[] = new int[registers];
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
				if(instr.mnemonic.equals("PUSHJ") == true && instr.defs.contains(node.temp) == true)
				{
					for(int i = registers - 1; i >= 0; i--)
					{
						if(regs[i] == 1)
						{
							node.register = i + 1;
							break;
						}
					}

					ok = node.register < registers;
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

			long offset = -(8 + chunk.frame.sizeLocs + chunk.frame.sizeFPRA + chunk.frame.sizeTmps);
			chunk.frame.sizeTmps += 8;

			int def = 0;
			while(chunk.asmcode.get(def++).defs.contains(node.temp) == false) {}

			chunk.asmcode.add(def, new AsmOPER("STO", "`s0,`s1," + offset, null, new LinkedList<FrmTemp>(Arrays.asList(node.temp, chunk.frame.FP))));

			for(int i = chunk.asmcode.size() - 1; i > def; i--)
			{
				if(chunk.asmcode.get(i).uses.contains(node.temp) == true)
				{
					chunk.asmcode.add(i, new AsmOPER("LDO", "`d0,`s0," + offset, new LinkedList<FrmTemp>(Arrays.asList(node.temp)), new LinkedList<FrmTemp>(Arrays.asList(chunk.frame.FP))));
					break;
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
