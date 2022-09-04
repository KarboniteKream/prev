package compiler.lincode;

import java.util.*;

import compiler.*;
import compiler.imcode.*;
import compiler.frames.*;

public class LinCode {
	private final HashMap<Long, Long> memory;
	private final HashMap<String, ImcCodeChunk> chunks;
	private HashMap<String, Long> temps;
	private final HashMap<String, Long> labels;

	private Long SP;
	private Long FP;
	private Long HP;
	private Long RV;

	private final Scanner in;

	private final boolean dump;

	public LinCode(boolean dump) {
		this.dump = dump;

		memory = new HashMap<>();
		chunks = new HashMap<>();
		temps = new HashMap<>();
		labels = new HashMap<>();

		SP = 65536L;
		FP = 65536L;
		HP = 1024L;
		RV = 0L;

		in = new Scanner(System.in);
	}

	public void generate(LinkedList<ImcChunk> chunks) {
		for (ImcChunk chunk : chunks) {
			if (chunk instanceof ImcCodeChunk) {
				final ImcCodeChunk codeChunk = (ImcCodeChunk) chunk;
				codeChunk.lincode = codeChunk.imcode.linear();
			}
		}
	}

	public void run(LinkedList<ImcChunk> chunks) {
		if (!dump) {
			return;
		}

		for (ImcChunk chunk : chunks) {
			if (chunk instanceof ImcCodeChunk) {
				final ImcCodeChunk codeChunk = (ImcCodeChunk) chunk;
				this.chunks.put(codeChunk.frame.label.name(), codeChunk);
			} else {
				final ImcDataChunk dataChunk = (ImcDataChunk) chunk;
				labels.put(dataChunk.label.name(), HP);
				HP += dataChunk.data == null ? dataChunk.size : dataChunk.data.length() - 1;
			}
		}

		if (this.chunks.get("_main") == null) {
			Report.error("Function main() could not be found.");
		}

		executeFunction("_main");
		System.out.printf("Program returned %d.\n", RV);
	}

	private Long executeFunction(String function) {
		if (function.equals("_get_int")) {
			return in.nextLong();
		}

		if (function.equals("_put_int")) {
			System.out.print(load(SP + 8));
			return 0L;
		}

		if (function.equals("_put_nl")) {
			for (int i = 0; i < load(SP + 8); i++) {
				System.out.println();
			}

			return 0L;
		}

		if (function.equals("_get_str")) {
			// TODO
		}

		if (function.equals("_put_str")) {
			// TODO
		}

		if (function.equals("_get_char_at")) {
			// TODO
		}

		if (function.equals("_put_char_at")) {
			// TODO
		}

		final ImcCodeChunk chunk = chunks.get(function);
		final LinkedList<ImcStmt> statements = ((ImcSEQ)(chunk.lincode)).stmts;
		final FrmFrame frame = chunk.frame;

		final HashMap<String, Long> oldTemps = temps;
		temps = new HashMap<>();

		store(SP - frame.sizeLocs - 4, FP);
		FP = SP;
		SP -= frame.size();
		store(frame.FP, FP);

		for (int i = 0; i < statements.size(); i++) {
			final FrmLabel label = executeStatement(statements.get(i));

			if (label != null) {
				i = statements.indexOf(new ImcLABEL(label)) - 1;
			}
		}

		SP += frame.size();
		FP = load(SP - frame.sizeLocs - 4);

		Long returnValue = load(frame.RV);
		RV = returnValue;
		temps = oldTemps;

		return returnValue;
	}

	private FrmLabel executeStatement(ImcStmt statement) {
		if (statement instanceof ImcCJUMP) {
			final ImcCJUMP cjump = (ImcCJUMP) statement;
			return (executeExpression(cjump.cond) != 0) ? cjump.trueLabel : cjump.falseLabel;
		} else if (statement instanceof ImcEXP) {
			executeExpression(((ImcEXP) statement).expr);
		} else if (statement instanceof ImcJUMP) {
			return ((ImcJUMP) statement).label;
		} else if (statement instanceof ImcMOVE) {
			final ImcMOVE move = (ImcMOVE) statement;

			if (move.dst instanceof ImcTEMP) {
				final FrmTemp destination = ((ImcTEMP) move.dst).temp;
				final Long source = executeExpression(move.src);
				store(destination, source);
			} else if (move.dst instanceof ImcMEM) {
				final Long destination = executeExpression(((ImcMEM) move.dst).expr);
				final Long source = executeExpression(move.src);
				store(destination, source);
			} else {
				Report.error("Invalid ImcMOVE statement.");
			}
		} else if (statement instanceof ImcSEQ) {
			Report.error("Nested ImcSEQ is not allowed.");
		}

		return null;
	}

	private Long executeExpression(ImcExpr expression) {
		if (expression instanceof ImcBINOP) {
			final Long left = executeExpression(((ImcBINOP) expression).limc);
			final Long right = executeExpression(((ImcBINOP) expression).rimc);

			switch (((ImcBINOP) expression).op) {
				case ImcBINOP.ADD: return left + right;
				case ImcBINOP.SUB: return left - right;
				case ImcBINOP.MUL: return left * right;
				case ImcBINOP.DIV: return left / right;
				case ImcBINOP.EQU: return (left == right) ? 1L : 0L;
				case ImcBINOP.NEQ: return (left != right) ? 1L : 0L;
				case ImcBINOP.LTH: return (left < right)  ? 1L : 0L;
				case ImcBINOP.GTH: return (left > right)  ? 1L : 0L;
				case ImcBINOP.LEQ: return (left <= right) ? 1L : 0L;
				case ImcBINOP.GEQ: return (left >= right) ? 1L : 0L;
				case ImcBINOP.AND: return (left * right != 0) ? 1L : 0L;
				case ImcBINOP.IOR: return (Math.abs(left) + Math.abs(right) > 0) ? 1L : 0L;
			}
		} else if (expression instanceof ImcCALL) {
			final ImcCALL call = (ImcCALL) expression;

			for (int i = 0, j = 0; i < call.args.size(); i++, j += 8) {
				final Long value = executeExpression(call.args.get(i));
				store(SP + j, value);
			}

			return executeFunction(call.label.name());
		} else if (expression instanceof ImcCONST) {
			return ((ImcCONST) expression).value;
		} else if (expression instanceof ImcMEM) {
			return load(executeExpression(((ImcMEM) expression).expr));
		} else if (expression instanceof ImcNAME) {
			return labels.get(((ImcNAME) expression).label.name());
		} else if (expression instanceof ImcTEMP) {
			return load(((ImcTEMP) expression).temp);
		} else if (expression instanceof ImcESEQ) {
			Report.error("Nested ImcESEQ is not allowed.");
		}

		return null;
	}

	private Long load(Long address) {
		final Long value = memory.get(address / 8);
		return (value == null) ? 0 : value;
	}

	private Long load(FrmTemp temp) {
		final Long value = temps.get(temp.name());
		return (value == null) ? 0 : value;
	}

	private void store(Long address, Long value) {
		memory.put(address / 8, value);
	}

	private void store(FrmTemp temp, Long value) {
		temps.put(temp.name(), value);
	}

	public void dump(LinkedList<ImcChunk> chunks) {
		if (!dump || Report.dumpFile() == null) {
			return;
		}

		for (ImcChunk imcChunk : chunks) {
			imcChunk.dump();
		}
	}
}
