package compiler.tmpan;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.asmcode.*;

public class TmpAn {
	private final boolean dump;

	public TmpAn(boolean dump) {
		this.dump = dump;
	}

	public void analyze(LinkedList<ImcChunk> chunks) {
		if (!dump) {
			return;
		}

		for (ImcChunk chunk : chunks) {
			if (chunk instanceof ImcCodeChunk) {
				analyze((ImcCodeChunk) chunk);
			}
		}
	}

	public LinkedHashMap<FrmTemp, TmpNode> analyze(ImcCodeChunk chunk) {
		final LinkedHashMap<FrmTemp, TmpNode> graph = new LinkedHashMap<>();

		for (AsmInstr instr : chunk.asmcode) {
			instr.in = new LinkedList<>();
			instr.out = new LinkedList<>();
		}

		while (true) {
			int ok = 0;

			for (int i = chunk.asmcode.size() - 1; i >= 0; i--) {
				final AsmInstr instr = chunk.asmcode.get(i);

				final LinkedList<FrmTemp> oldIn = new LinkedList<>(instr.in);
				final LinkedList<FrmTemp> oldOut = new LinkedList<>(instr.out);

				instr.in = new LinkedList<>(instr.uses);

				for (FrmTemp temp : instr.out) {
					if (temp != chunk.frame.FP && temp != chunk.frame.SP && !instr.in.contains(temp) && !instr.defs.contains(temp)) {
						instr.in.add(temp);
					}
				}

				final LinkedList<AsmInstr> succ = new LinkedList<>();

				if (!instr.mnemonic.equals("JMP") && (i + 1) < chunk.asmcode.size()) {
					succ.add(chunk.asmcode.get(i + 1));
				}

				if (instr.mnemonic.equals("BNZ") || instr.mnemonic.equals("JMP")) {
					for (AsmInstr label : chunk.asmcode) {
						if (label instanceof AsmLABEL && label.labels.equals(instr.labels)) {
							succ.add(label);
							break;
						}
					}
				}

				instr.out = new LinkedList<>();

				for (AsmInstr succInstr : succ) {
					for (FrmTemp temp : succInstr.in) {
						if (temp != chunk.frame.FP && temp != chunk.frame.SP && !instr.out.contains(temp)) {
							instr.out.add(temp);
						}
					}
				}

				if (instr.in.equals(oldIn) && instr.out.equals(oldOut)) {
					ok++;
				}
			}

			if (ok == chunk.asmcode.size()) {
				break;
			}
		}

		for (AsmInstr instr : chunk.asmcode) {
			if (instr.defs.size() == 0) {
				continue;
			}

			final FrmTemp temp = instr.defs.getFirst();
			final TmpNode node = graph.get(temp) == null ? new TmpNode(temp) : graph.get(temp);

			for (FrmTemp out : instr.out) {
				if (!temp.equals(out) && (!(instr instanceof AsmMOVE) || !instr.uses.getFirst().equals(out))) {
					boolean ok = true;

					for (TmpNode edge : node.edges) {
						if (edge.temp == out) {
							ok = false;
							break;
						}
					}

					if (ok) {
						TmpNode edge = graph.get(out) == null ? new TmpNode(out) : graph.get(out);
						edge.edges.add(node);
						graph.put(out, edge);

						node.edges.add(edge);
					}
				}
			}

			graph.put(temp, node);
		}

		chunk.graph = new LinkedList<>(graph.values());

		return graph;
	}

	public void dump(LinkedList<ImcChunk> chunks) {
		if (!dump || Report.dumpFile() == null) {
			return;
		}

		for (ImcChunk chunk : chunks) {
			if (!(chunk instanceof ImcCodeChunk)) {
				continue;
			}

			for (TmpNode node : ((ImcCodeChunk) chunk).graph) {
				if (node.edges.size() > 0) {
					String output = node.temp.name() + ": ";

					for (TmpNode edge : node.edges) {
						output += edge.temp.name() + ", ";
					}

					Report.dump(0, output.substring(0, output.length() - 2));
				}
			}
		}
	}
}
