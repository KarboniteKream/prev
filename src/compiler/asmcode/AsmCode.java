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

	private void parse(ImcStmt statement)
	{
		LinkedList<FrmTemp> defs = new LinkedList<FrmTemp>();
		LinkedList<FrmTemp> uses = new LinkedList<FrmTemp>();
		LinkedList<FrmLabel> labels = new LinkedList<FrmLabel>();

		if(statement instanceof ImcMOVE == true)
		{
			ImcMOVE move = (ImcMOVE)statement;

			if(move.dst instanceof ImcMEM == true)
			{
				defs.add(parse(((ImcMEM)move.dst).expr));
				uses.add(parse(move.src));
				asmcode.add(new AsmOPER("STOI `s0, `d0, 0", defs, uses));
			}
			else if(move.dst instanceof ImcTEMP == true)
			{
				asmcode.add(new AsmMOVE("ADDI `d0, `s0, 0", parse(move.dst), parse(move.src)));
			}
		}
		else if(statement instanceof ImcCJUMP == true)
		{
			uses.add(parse(((ImcCJUMP)statement).cond));
			labels.add(((ImcCJUMP)statement).trueLabel);

			asmcode.add(new AsmOPER("BNZ `s0, `l0", null, uses, labels));
		}
		else if(statement instanceof ImcJUMP == true)
		{
			labels.add(((ImcJUMP)statement).label);
			asmcode.add(new AsmOPER("JMP `l0", null, null, labels));
		}
		else if(statement instanceof ImcLABEL == true)
		{
			asmcode.add(new AsmLABEL("`l0:", ((ImcLABEL)statement).label));
		}
		else if(statement instanceof ImcEXP == true)
		{
		}
		else
		{
			Report.error("Nested ImcSEQ is not allowed.");
		}
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

			uses.add(parse(binop.limc));
			uses.add(parse(binop.rimc));
			defs.add(temp = new FrmTemp());

			asmcode.add(new AsmOPER(oper + " `d0, `s0, `s1", defs, uses));

			if(oper.equals("CMP") == true)
			{
				switch(binop.op)
				{
					case ImcBINOP.EQU:
						asmcode.add(new AsmOPER("ZSZI `d0, `s0, 1", defs, defs));
					break;

					case ImcBINOP.NEQ:
						asmcode.add(new AsmOPER("ZSNZI `d0, `s0, 1", defs, defs));
					break;

					case ImcBINOP.LTH:
						asmcode.add(new AsmOPER("ZSNI `d0, `s0, 1", defs, defs));
					break;

					case ImcBINOP.LEQ:
						asmcode.add(new AsmOPER("ZSNPI `d0, `s0, 1", defs, defs));
					break;

					case ImcBINOP.GTH:
						asmcode.add(new AsmOPER("ZSPI `d0, `s0, 1", defs, defs));
					break;

					case ImcBINOP.GEQ:
						asmcode.add(new AsmOPER("ZSNNI `d0, `s0, 1", defs, defs));
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
			long constant = ((ImcCONST)expression).value;
			long value = Math.abs(constant);

			defs.add(temp = new FrmTemp());
			asmcode.add(new AsmOPER("SETL `d0, " + (value & 0xFFFFL), defs, null));

			if(value > 0xFFFFL)
			{
				uses.add(new FrmTemp());
				uses.add(temp);

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

			if(constant < 0)
			{
				defs = new LinkedList<FrmTemp>(Arrays.asList(new FrmTemp(), temp));

				asmcode.add(new AsmOPER("SETL `d0, 0", defs, null));
				asmcode.add(new AsmOPER("SUB `d0, `s0, `s1", defs, defs));

				temp = defs.getFirst();
			}
		}
		else if(expression instanceof ImcMEM == true)
		{
			uses.add(parse(((ImcMEM)expression).expr));
			defs.add(temp = new FrmTemp());
			asmcode.add(new AsmOPER("LDOI `d0, `s0, 0", defs, uses));
		}
		else if(expression instanceof ImcNAME == true)
		{
			defs.add(temp = new FrmTemp());
			labels.add(((ImcNAME)expression).label);
			asmcode.add(new AsmOPER("LDA `d0, `l0", defs, null, labels));
		}
		else if(expression instanceof ImcCALL == true)
		{
		}
		else
		{
			Report.error("Nested ImcESEQ is not allowed.");
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
