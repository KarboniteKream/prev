package compiler.lincode;

import java.util.*;

import compiler.*;
import compiler.imcode.*;

/**
 * Izracun lineariziranih fragmentov vmesne kode.
 * 
 * @author sliva
 */
public class LinCode
{
	private HashMap<String, ImcCodeChunk> chunks;

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

		chunks = new HashMap<String, ImcCodeChunk>();
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
				// TODO
			}
		}

		executeFunction("main");
	}

	private Integer executeFunction(String function)
	{
		ImcCodeChunk chunk = chunks.get(function);
		return 0;
	}

	private Integer executeStatement(ImcStmt statement)
	{
		if(statement instanceof ImcCJUMP == true)
		{

		}
		else if(statement instanceof ImcEXP == true)
		{

		}
		else if(statement instanceof ImcJUMP == true)
		{

		}
		else if(statement instanceof ImcLABEL == true)
		{

		}
		else if(statement instanceof ImcMOVE == true)
		{

		}
		else
		{
			Report.error("Nested ImcSEQ is not allowed.");
		}

		return null;
	}

	private Integer executeExpression(ImcExpr expression)
	{
		if(expression instanceof ImcBINOP == true)
		{
			Integer left = executeExpression(((ImcBINOP)expression).limc);
			Integer right = executeExpression(((ImcBINOP)expression).rimc);

			switch(((ImcBINOP)expression).op)
			{
				case ImcBINOP.ADD: return left + right;
				case ImcBINOP.SUB: return left - right;
				case ImcBINOP.MUL: return left * right;
				case ImcBINOP.DIV: return left / right;
				case ImcBINOP.EQU: return (left == right) ? 1 : 0;
				case ImcBINOP.NEQ: return (left != right) ? 1 : 0;
				case ImcBINOP.LTH: return (left < right) ? 1 : 0;
				case ImcBINOP.GTH: return (left > right) ? 1 : 0;
				case ImcBINOP.LEQ: return (left <= right) ? 1 : 0;
				case ImcBINOP.GEQ: return (left >= right) ? 1 : 0;
				// TODO: Test.
				case ImcBINOP.AND: return (left * right != 0) ? 1 : 0;
				case ImcBINOP.IOR: return (Math.abs(left) + Math.abs(right) > 0) ? 1 : 0;
			}
		}
		else if(expression instanceof ImcCALL == true)
		{

		}
		else if(expression instanceof ImcCONST == true)
		{
			return ((ImcCONST)expression).value;
		}
		else if(expression instanceof ImcMEM == true)
		{

		}
		else if(expression instanceof ImcNAME == true)
		{

		}
		else if(expression instanceof ImcTEMP == true)
		{

		}
		else
		{
			Report.error("Nested ImcESEQ is not allowed.");
		}

		return null;
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
