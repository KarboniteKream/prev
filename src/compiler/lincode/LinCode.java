package compiler.lincode;

import java.util.*;

import compiler.*;
import compiler.imcode.*;
import compiler.frames.*;

/**
 * Izracun lineariziranih fragmentov vmesne kode.
 * 
 * @author sliva
 */
public class LinCode
{
	private HashMap<Long, Long> memory;
	private HashMap<String, ImcCodeChunk> chunks;
	private HashMap<String, Long> temps;
	private HashMap<String, Long> labels;

	private Long SP;
	private Long FP;
	private Long HP;
	private Long RV;

	private Scanner in;

	/** Ali se izpisujejo vmesni rezultati. */
	private boolean dump;

	/**
	 * Izracun lineariziranih fragmentov vmesne kode.
	 * 
	 * @param dump
	 *            Ali se izpisujejo vmesni rezultati.
	 */
	public LinCode(boolean dump) {
		this.dump = dump;

		memory = new HashMap<Long, Long>();
		chunks = new HashMap<String, ImcCodeChunk>();
		temps = new HashMap<String, Long>();
		labels = new HashMap<String, Long>();

		SP = 65536L;
		FP = 65536L;
		HP = 1024L;
		RV = 0L;

		in = new Scanner(System.in);
	}

	/**
	 * Izvede program s klicem funkcije 'main'.
	 * 
	 * @param chunks
	 *            Seznam fragmentov vmesne kode.
	 */
	public void run(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;
				this.chunks.put(codeChunk.frame.label.name(), codeChunk);
			}
			else
			{
				ImcDataChunk dataChunk = (ImcDataChunk)chunk;
				labels.put(dataChunk.label.name(), HP);
				HP += dataChunk.size;
			}
		}

		executeFunction("_main");

		System.out.println();
		Report.report("Program returned " + RV + ".");
	}

	private Long executeFunction(String function)
	{
		if(function.equals("_get_int") == true)
		{
			return in.nextLong();
		}
		else if(function.equals("_put_int") == true)
		{
			System.out.print(load(SP + 8));
			return 0L;
		}
		else if(function.equals("_put_nl") == true)
		{
			for(int i = 0; i < load(SP + 8); i++)
			{
				System.out.println();
			}

			return 0L;
		}

		ImcCodeChunk chunk = chunks.get(function);
		LinkedList<ImcStmt> statements = ((ImcSEQ)(chunk.lincode)).stmts;
		FrmFrame frame = chunk.frame;

		HashMap<String, Long> oldTemps = temps;
		temps = new HashMap<String, Long>();

		store(SP - frame.sizeLocs - 4, FP);
		FP = SP;
		SP -= frame.size();
		store(frame.FP, FP);

		for(int i = 0; i < statements.size(); i++)
		{
			FrmLabel label = executeStatement(statements.get(i));

			if(label != null)
			{
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

	private FrmLabel executeStatement(ImcStmt statement)
	{
		if(statement instanceof ImcCJUMP == true)
		{
			ImcCJUMP cjump = (ImcCJUMP)statement;
			return (executeExpression(cjump.cond) != 0) ? cjump.trueLabel : cjump.falseLabel;
		}
		else if(statement instanceof ImcEXP == true)
		{
			executeExpression(((ImcEXP)statement).expr);
		}
		else if(statement instanceof ImcJUMP == true)
		{
			return ((ImcJUMP)statement).label;
		}
		else if(statement instanceof ImcMOVE == true)
		{
			ImcMOVE move = (ImcMOVE)statement;

			if(move.dst instanceof ImcTEMP == true)
			{
				FrmTemp destination = (FrmTemp)((ImcTEMP)move.dst).temp;
				Long source = executeExpression(move.src);
				store(destination, source);
			}
			else if(move.dst instanceof ImcMEM == true)
			{
				Long destination = executeExpression(((ImcMEM)move.dst).expr);
				Long source = executeExpression(move.src);
				store(destination, source);
			}
			else
			{
				Report.error("Invalid ImcMOVE statement.");
			}
		}
		else if(statement instanceof ImcSEQ == true)
		{
			Report.error("Nested ImcSEQ is not allowed.");
		}

		return null;
	}

	private Long executeExpression(ImcExpr expression)
	{
		if(expression instanceof ImcBINOP == true)
		{
			Long left = executeExpression(((ImcBINOP)expression).limc);
			Long right = executeExpression(((ImcBINOP)expression).rimc);

			switch(((ImcBINOP)expression).op)
			{
				case ImcBINOP.ADD: return left + right;
				case ImcBINOP.SUB: return left - right;
				case ImcBINOP.MUL: return left * right;
				case ImcBINOP.DIV: return left / right;
				case ImcBINOP.EQU: return (left == right) ? 1L : 0L;
				case ImcBINOP.NEQ: return (left != right) ? 1L : 0L;
				case ImcBINOP.LTH: return (left < right) ? 1L : 0L;
				case ImcBINOP.GTH: return (left > right) ? 1L : 0L;
				case ImcBINOP.LEQ: return (left <= right) ? 1L : 0L;
				case ImcBINOP.GEQ: return (left >= right) ? 1L : 0L;
				case ImcBINOP.AND: return (left * right != 0) ? 1L : 0L;
				case ImcBINOP.IOR: return (Math.abs(left) + Math.abs(right) > 0) ? 1L : 0L;
			}
		}
		else if(expression instanceof ImcCALL == true)
		{
			ImcCALL call = (ImcCALL)expression;

			for(int i = 0, j = 0; i < call.args.size(); i++, j += 8)
			{
				Long value = executeExpression(call.args.get(i));
				store(SP + j, value);
			}

			return executeFunction(call.label.name());
		}
		else if(expression instanceof ImcCONST == true)
		{
			return ((ImcCONST)expression).value;
		}
		else if(expression instanceof ImcMEM == true)
		{
			return load(executeExpression(((ImcMEM)expression).expr));
		}
		else if(expression instanceof ImcNAME == true)
		{
			return labels.get(((ImcNAME)expression).label.name());
		}
		else if(expression instanceof ImcTEMP == true)
		{
			return load(((ImcTEMP)expression).temp);
		}
		else if(expression instanceof ImcESEQ == true)
		{
			Report.error("Nested ImcESEQ is not allowed.");
		}

		return null;
	}

	private Long load(Long address)
	{
		Long value = memory.get(address / 8);
		return (value == null) ? 0 : value;
	}

	private Long load(FrmTemp temp)
	{
		Long value = temps.get(temp.name());
		return (value == null) ? 0 : value;
	}

	private void store(Long address, Long value)
	{
		memory.put(address / 8, value);
	}

	private void store(FrmTemp temp, Long value)
	{
		temps.put(temp.name(), value);
	}

	/**
	 * Izpise linearizirane fragmente vmesne kode na datoteko vmesnih
	 * rezultatov.
	 * 
	 * @param chunks
	 *            Seznam fragmentov vmesne kode.
	 */
	public void dump(LinkedList<ImcChunk> chunks) {
		if (!dump)
			return;
		if (Report.dumpFile() == null)
			return;
		for (int chunk = 0; chunk < chunks.size(); chunk++)
			chunks.get(chunk).dump();
	}

}
