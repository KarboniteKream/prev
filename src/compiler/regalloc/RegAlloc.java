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

					stack.add(node);

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

		TmpNode node = chunk.graph.removeFirst();
		node.spill = TmpNode.POTENTIAL_SPILL;
		stack.add(node);

		for(TmpNode edge : node.edges)
		{
			edge.edges.remove(node);
		}

		return true;
	}

	private boolean select(ImcCodeChunk chunk)
	{
		boolean repeat = false;
		Iterator<TmpNode> iterator = stack.descendingIterator();

		while(iterator.hasNext() == true)
		{
			TmpNode node = iterator.next();
			chunk.graph.add(node);

			int regs[] = new int[registers];

			for(TmpNode edge : node.edges)
			{
				regs[edge.register] = 1;
			}

			boolean ok = false;
			for(int i = 0; i < registers; i++)
			{
				if(regs[i] == 0)
				{
					node.register = i;
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
					Report.error("Unable to allocate a register to " + node.temp.name());
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

			int def = 0;

			while(def < chunk.asmcode.size())
			{
				if(chunk.asmcode.get(def++).defs.contains(node.temp) == true)
				{
					break;
				}
			}

			long offset = chunk.frame.sizeLocs + chunk.frame.sizeFPRA + chunk.frame.sizeTmps;
			chunk.frame.sizeTmps += 8;

			chunk.asmcode.add(def, new AsmOPER("STOI", "`s0, `s1, -" + offset, null, new LinkedList<FrmTemp>(Arrays.asList(node.temp, chunk.frame.FP))));
			chunk.asmcode.get(def).spill = true;

			boolean ok = false;

			for(int i = chunk.asmcode.size() - 1; i > def; i--)
			{
				AsmInstr instr = chunk.asmcode.get(i);

				if(instr.uses.contains(node.temp) == true && instr.spill == false)
				{
					ok = true;
					instr.spill = true;

					chunk.asmcode.add(i, new AsmOPER("LDOI", "`d0, `s0, -" + offset, new LinkedList<FrmTemp>(Arrays.asList(node.temp)), new LinkedList<FrmTemp>(Arrays.asList(chunk.frame.FP))));
					chunk.asmcode.get(i).spill = true;
					break;
				}
			}

			if(ok == false)
			{
				Report.error("Insufficient number of registers.");
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
