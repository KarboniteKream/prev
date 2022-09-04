package compiler.regalloc;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.asmcode.*;
import compiler.tmpan.*;

public class RegAlloc {
	private final boolean dump;
	private final int registers;

	private final TmpAn tmpan;
	private LinkedList<TmpNode> stack;

	public RegAlloc(boolean dump, int registers) {
		this.dump = dump;
		this.registers = registers;

		tmpan = new TmpAn(false);
		stack = null;
	}

	public void allocate(LinkedList<ImcChunk> chunks) {
		for (ImcChunk chunk : chunks) {
			if (!(chunk instanceof ImcCodeChunk)) {
				continue;
			}

			final ImcCodeChunk codeChunk = (ImcCodeChunk) chunk;

			do {
				build(codeChunk);
				stack = new LinkedList<>();

				do {
					simplify(codeChunk);
				} while (spill(codeChunk));
			} while (select(codeChunk));

			codeChunk.registers = new HashMap<>();
			codeChunk.registers.put(codeChunk.frame.FP, "$251");
			codeChunk.registers.put(codeChunk.frame.SP, "$250");

			for (TmpNode node : codeChunk.graph) {
				codeChunk.registers.put(node.temp, "$" + node.register);
			}

			final LinkedHashMap<FrmTemp, TmpNode> graph = tmpan.analyze(codeChunk);

			for (int i = 0; i < codeChunk.asmcode.size(); i++) {
				final AsmInstr instr = codeChunk.asmcode.get(i);

				if (instr.mnemonic.equals("PUSHJ")) {
					final AsmInstr next = codeChunk.asmcode.get(i + 1);
					final LinkedList<TmpNode> edges = graph.get(instr.defs.getFirst()).edges;

					int maxRegister = 0;
					for (TmpNode edge : edges) {
						final int register = Integer.parseInt(codeChunk.registers.get(edge.temp).substring(1));

						if (register > maxRegister) {
							maxRegister = register;
						}
					}

					codeChunk.registers.put(instr.defs.getFirst(), "$" + (edges.size() == 0 ? 0 : maxRegister + 1));

					if (next.mnemonic.equals("SET") && codeChunk.registers.get(next.defs.getFirst()).equals(codeChunk.registers.get(next.uses.getFirst()))) {
						codeChunk.asmcode.remove(i + 1);
					}
				}
			}
		}
	}

	private void build(ImcCodeChunk chunk) {
		tmpan.analyze(chunk);
	}

	private void simplify(ImcCodeChunk chunk) {
		boolean done = false;

		while (!done) {
			done = true;

			final Iterator<TmpNode> iterator = chunk.graph.iterator();

			while (iterator.hasNext()) {
				final TmpNode node = iterator.next();

				if (node.edges.size() < registers) {
					done = false;

					stack.push(node);

					for (TmpNode edge : chunk.graph) {
						edge.edges.remove(node);
					}

					iterator.remove();
				}
			}
		}
	}

	private boolean spill(ImcCodeChunk chunk) {
		if (chunk.graph.size() == 0) {
			return false;
		}

		TmpNode spill = null;
		int length = 0;

		for (TmpNode node : chunk.graph) {
			int def = 0;

			while (!chunk.asmcode.get(def).defs.contains(node.temp)) {
				def++;
			}

			int use = chunk.asmcode.size() - 1;

			while (!chunk.asmcode.get(use).uses.contains(node.temp)) {
				use--;
			}

			if (use - def > length) {
				spill = node;
				length = use - def;
			}
		}

		chunk.graph.remove(spill);
		spill.spill = TmpNode.POTENTIAL_SPILL;
		stack.push(spill);

		for (TmpNode edge : spill.edges) {
			edge.edges.remove(spill);
		}

		return true;
	}

	private boolean select(ImcCodeChunk chunk) {
		boolean repeat = false;

		while (stack.size() > 0) {
			final TmpNode node = stack.pop();
			chunk.graph.add(node);

			final int[] regs = new int[registers + 1];
			boolean ok = false;

			for (TmpNode edge : node.edges) {
				regs[edge.register] = 1;
			}

			for (int i = 0; i < registers; i++) {
				if (regs[i] == 0) {
					node.register = i;
					ok = true;
					break;
				}
			}

			for (AsmInstr instr : chunk.asmcode) {
				if (instr.mnemonic.equals("PUSHJ") && instr.defs.contains(node.temp)) {
					node.register = registers;
					ok = true;
					break;
				}
			}

			if (!ok) {
				if (node.spill == TmpNode.POTENTIAL_SPILL) {
					node.spill = TmpNode.ACTUAL_SPILL;
					repeat = true;
					break;
				} else {
					Report.error("Unable to allocate a register to " + node.temp.name() + ".");
				}
			}
		}

		if (repeat) {
			startOver(chunk);
		}

		return repeat;
	}

	private void startOver(ImcCodeChunk chunk) {
		for (TmpNode node : chunk.graph) {
			if (node.spill != TmpNode.ACTUAL_SPILL) {
				continue;
			}

			final long offset = chunk.frame.sizeArgs + chunk.frame.sizeTmps;
			chunk.frame.sizeTmps += 8;

			int def = 0;
			while (!chunk.asmcode.get(def++).defs.contains(node.temp)) {}

			chunk.asmcode.add(def, new AsmOPER("STO", "`s0,`s1," + offset, null, new LinkedList<>(Arrays.asList(node.temp, chunk.frame.SP))));

			for (int i = chunk.asmcode.size() - 1; i > def; i--) {
				if (chunk.asmcode.get(i).uses.contains(node.temp)) {
					chunk.asmcode.add(i, new AsmOPER("LDO", "`d0,`s0," + offset, new LinkedList<>(Arrays.asList(node.temp)), new LinkedList<>(Arrays.asList(chunk.frame.SP))));
				}
			}
		}
	}

	public void dump(LinkedList<ImcChunk> chunks) {
		if (!dump || Report.dumpFile() == null) {
			return;
		}

		int i = 0;

		for (ImcChunk chunk : chunks) {
			if (!(chunk instanceof ImcCodeChunk)) {
				continue;
			}

			if (i++ > 0) {
				Report.dump(0, "");
			}

			final ImcCodeChunk codeChunk = (ImcCodeChunk) chunk;

			for (AsmInstr instr : codeChunk.asmcode) {
				Report.dump(0, instr.format(codeChunk.registers));
			}
		}
	}
}
