package compiler.asmcode;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;

public class AsmCode
{
	private boolean dump;

	private LinkedList<AsmInstr> asmcode = null;

	public AsmCode(boolean dump)
	{
		this.dump = dump;
	}

	public void generate(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;
				asmcode = new LinkedList<AsmInstr>();

				for(ImcStmt statement : ((ImcSEQ)codeChunk.lincode).stmts)
				{
					parse(statement);
				}

				codeChunk.asmcode = asmcode;
			}
		}
	}

	private FrmTemp parse(ImcStmt statement)
	{
		return null;
	}

	private FrmTemp parse(ImcExpr expression)
	{
		FrmTemp temp = null;

		LinkedList<FrmTemp> defs = new LinkedList<FrmTemp>();
		LinkedList<FrmTemp> uses = new LinkedList<FrmTemp>();
		LinkedList<FrmLabel> labels = new LinkedList<FrmLabel>();

		if(expression instanceof ImcBINOP == true)
		{
			ImcBINOP binop = (ImcBINOP)expression;
			FrmTemp right = null;

			uses.add(parse(binop.limc));

			if(binop.rimc instanceof ImcCONST == false)
			{
				right = parse(binop.rimc);
				uses.add(right);
			}

			defs.add(temp = new FrmTemp());

			String oper = null;

			switch(binop.op)
			{
				case ImcBINOP.ADD:
					oper = "ADD";
				break;

				case ImcBINOP.SUB:
					oper = "SUB";
				break;

				case ImcBINOP.MUL:
					oper = "MUL";
				break;

				case ImcBINOP.DIV:
					oper = "DIV";
				break;

				case ImcBINOP.EQU: case ImcBINOP.NEQ:
				case ImcBINOP.LTH: case ImcBINOP.LEQ:
				case ImcBINOP.GTH: case ImcBINOP.GEQ:
					oper = "CMP";
				break;

				case ImcBINOP.IOR:
					oper = "OR";
				break;

				case ImcBINOP.AND:
					oper = "AND";
				break;
			}

			if(right == null)
			{
				asmcode.add(new AsmOPER(oper + "I `d0, `s0, " + ((ImcCONST)binop.rimc).value, defs, uses));
			}
			else
			{
				asmcode.add(new AsmOPER(oper + " `d0, `s0, `s1", defs, uses));
			}

			if(oper.equals("CMP") == true)
			{
				uses = defs;
				defs = new LinkedList<FrmTemp>(Arrays.asList(temp = new FrmTemp()));

				switch(binop.op)
				{
					case ImcBINOP.EQU:
						asmcode.add(new AsmOPER("ZSZI `d0, `s0, 1", defs, uses));
					break;

					case ImcBINOP.NEQ:
						asmcode.add(new AsmOPER("ZSNZI `d0, `s0, 1", defs, uses));
					break;

					case ImcBINOP.LTH:
						asmcode.add(new AsmOPER("ZSNI `d0, `s0, 1", defs, uses));
					break;

					case ImcBINOP.LEQ:
						asmcode.add(new AsmOPER("ZSNPI `d0, `s0, 1", defs, uses));
					break;

					case ImcBINOP.GTH:
						asmcode.add(new AsmOPER("ZSPI `d0, `s0, 1", defs, uses));
					break;

					case ImcBINOP.GEQ:
						asmcode.add(new AsmOPER("ZSNNI `d0, `s0, 1", defs, uses));
					break;
				}
			}
		}
		else if(expression instanceof ImcTEMP == true)
		{
			temp = ((ImcTEMP)expression).temp;
		}
		else if(expression instanceof ImcCONST == true)
		{
			long value = ((ImcCONST)expression).value;

			defs.add(temp = new FrmTemp());
			uses.add(new FrmTemp());
			uses.add(temp);

			asmcode.add(new AsmOPER("SETL `d0, " + (value & 0xFFFFL), defs, null));

			if(value > 0xFFFFL)
			{
				asmcode.add(new AsmOPER("SETML `d0, " + ((value & 0xFFFF0000L) >> 16), uses, null));
				asmcode.add(new AsmOPER("OR `d0, `s0, `s1", defs, uses));
			}

			if(value > 0xFFFFFFFFL)
			{
				asmcode.add(new AsmOPER("SETMH `d0, " + ((value & 0xFFFF00000000L) >> 32), uses, null));
				asmcode.add(new AsmOPER("OR `d0, `s0, `s1", defs, uses));
			}

			if(value > 0xFFFFFFFFFFFFL)
			{
				asmcode.add(new AsmOPER("SETH `d0, " + ((value & 0xFFFF000000000000L) >> 48), uses, null));
				asmcode.add(new AsmOPER("OR `d0, `s0, `s1", defs, uses));
			}
		}
		else if(expression instanceof ImcNAME == true)
		{
			defs.add(temp = new FrmTemp());
			labels.add(((ImcNAME)expression).label);
			asmcode.add(new AsmOPER("GETA `d0, `l0", defs, uses, labels));
		}

		return temp;
	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if(dump == false)
		{
			return;
		}

		if(Report.dumpFile() == null)
		{
			return;
		}

		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

				for(AsmInstr instr : codeChunk.asmcode)
				{
					Report.dump(0, instr.format(null));
				}
			}
		}
	}
}
