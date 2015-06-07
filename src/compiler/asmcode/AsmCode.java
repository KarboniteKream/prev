package compiler.asmcode;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;

public class AsmCode
{
	private boolean dump;
	private ImcCodeChunk chunk;

	public AsmCode(boolean dump)
	{
		this.dump = dump;
		chunk = null;
	}

	public void generate(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk temp : chunks)
		{
			if(temp instanceof ImcCodeChunk == true)
			{
				chunk = (ImcCodeChunk)temp;
				chunk.asmcode = new LinkedList<AsmInstr>(Arrays.asList(new AsmLABEL("`l0", chunk.frame.label)));

				for(ImcStmt statement : ((ImcSEQ)chunk.lincode).stmts)
				{
					parse(statement);
				}
			}
		}
	}

	private void parse(ImcStmt statement)
	{
		LinkedList<FrmTemp> uses = new LinkedList<FrmTemp>();
		LinkedList<FrmLabel> labels = new LinkedList<FrmLabel>();

		if(statement instanceof ImcMOVE == true)
		{
			ImcMOVE move = (ImcMOVE)statement;

			if(move.dst instanceof ImcMEM == true)
			{
				uses.add(parse(move.src));
				uses.add(parse(((ImcMEM)move.dst).expr));
				chunk.asmcode.add(new AsmOPER("STO", "`s0,`s1,0", null, uses));
			}
			else if(move.dst instanceof ImcTEMP == true)
			{
				chunk.asmcode.add(new AsmMOVE("SET", "`d0,`s0", parse(move.dst), parse(move.src)));
			}
		}
		else if(statement instanceof ImcCJUMP == true)
		{
			uses.add(parse(((ImcCJUMP)statement).cond));
			labels.add(((ImcCJUMP)statement).trueLabel);
			chunk.asmcode.add(new AsmOPER("BNZ", "`s0,`l0", null, uses, labels));
		}
		else if(statement instanceof ImcJUMP == true)
		{
			labels.add(((ImcJUMP)statement).label);
			chunk.asmcode.add(new AsmOPER("JMP", "`l0", null, null, labels));
		}
		else if(statement instanceof ImcLABEL == true)
		{
			chunk.asmcode.add(new AsmLABEL("`l0", ((ImcLABEL)statement).label));
		}
		else if(statement instanceof ImcEXP == true)
		{
		}
		else if(statement instanceof ImcSEQ == true)
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
				case ImcBINOP.IOR: oper = "OR";  break;
				case ImcBINOP.AND: oper = "AND"; break;

				case ImcBINOP.ADD: oper = "ADD"; break;
				case ImcBINOP.SUB: oper = "SUB"; break;
				case ImcBINOP.MUL: oper = "MUL"; break;
				case ImcBINOP.DIV: oper = "DIV"; break;

				case ImcBINOP.EQU: case ImcBINOP.NEQ:
				case ImcBINOP.LTH: case ImcBINOP.LEQ:
				case ImcBINOP.GTH: case ImcBINOP.GEQ:
					oper = "CMP";
				break;
			}

			uses.add(parse(binop.limc));
			uses.add(parse(binop.rimc));
			defs.add(temp = new FrmTemp());

			chunk.asmcode.add(new AsmOPER(oper, "`d0,`s0,`s1", defs, uses));

			if(oper.equals("CMP") == true)
			{
				switch(binop.op)
				{
					case ImcBINOP.EQU: chunk.asmcode.add(new AsmOPER("ZSZ",  "`d0,`s0,1", defs, defs)); break;
					case ImcBINOP.NEQ: chunk.asmcode.add(new AsmOPER("ZSNZ", "`d0,`s0,1", defs, defs)); break;
					case ImcBINOP.LTH: chunk.asmcode.add(new AsmOPER("ZSN",  "`d0,`s0,1", defs, defs)); break;
					case ImcBINOP.LEQ: chunk.asmcode.add(new AsmOPER("ZSNP", "`d0,`s0,1", defs, defs)); break;
					case ImcBINOP.GTH: chunk.asmcode.add(new AsmOPER("ZSP",  "`d0,`s0,1", defs, defs)); break;
					case ImcBINOP.GEQ: chunk.asmcode.add(new AsmOPER("ZSNN", "`d0,`s0,1", defs, defs)); break;
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

			if(constant < 0 && value <= 0xFFFFL)
			{
				chunk.asmcode.add(new AsmOPER("NEG", "`d0,0," + value, defs, null));
				return temp;
			}

			chunk.asmcode.add(new AsmOPER("SET", "`d0," + (value & 0xFFFFL), defs, null));

			LinkedList<FrmTemp> setDef = new LinkedList<FrmTemp>();

			if(value > 0xFFFFL)
			{
				setDef.add(new FrmTemp());

				uses.add(setDef.getFirst());
				uses.add(temp);

				chunk.asmcode.removeLast();
				chunk.asmcode.add(new AsmOPER("SETL", "`d0," + (value & 0xFFFFL), defs, null));
				chunk.asmcode.add(new AsmOPER("SETML", "`d0," + ((value & 0xFFFF0000L) >> 16), setDef, null));
				chunk.asmcode.add(new AsmOPER("OR", "`d0,`s0,`s1", defs, uses));
			}

			if(value > 0xFFFFFFFFL)
			{
				chunk.asmcode.add(new AsmOPER("SETMH", "`d0," + ((value & 0xFFFF00000000L) >> 32), setDef, null));
				chunk.asmcode.add(new AsmOPER("OR", "`d0,`s0,`s1", defs, uses));
			}

			if(value > 0xFFFFFFFFFFFFL)
			{
				chunk.asmcode.add(new AsmOPER("SETH", "`d0," + ((value & 0xFFFF000000000000L) >> 48), setDef, null));
				chunk.asmcode.add(new AsmOPER("OR", "`d0,`s0,`s1", defs, uses));
			}

			if(constant < 0)
			{
				uses = new LinkedList<FrmTemp>(Arrays.asList(temp));
				defs = new LinkedList<FrmTemp>(Arrays.asList(temp = new FrmTemp()));
				chunk.asmcode.add(new AsmOPER("NEG", "`d0,0,`s0", defs, uses));
			}
		}
		else if(expression instanceof ImcMEM == true)
		{
			uses.add(parse(((ImcMEM)expression).expr));
			defs.add(temp = new FrmTemp());
			chunk.asmcode.add(new AsmOPER("LDO", "`d0,`s0,0", defs, uses));
		}
		else if(expression instanceof ImcNAME == true)
		{
			defs.add(temp = new FrmTemp());
			labels.add(((ImcNAME)expression).label);
			chunk.asmcode.add(new AsmOPER("LDA", "`d0,`l0", defs, null, labels));
		}
		else if(expression instanceof ImcCALL == true)
		{
			ImcCALL call = (ImcCALL)expression;

			for(int i = call.args.size() - 1; i >= 0; i--)
			{
				chunk.asmcode.add(new AsmOPER("STO", "`s0,`s1," + (i * 8), null, new LinkedList<FrmTemp>(Arrays.asList(((ImcTEMP)call.args.get(i)).temp, chunk.frame.SP))));
			}

			defs.add(temp = new FrmTemp());
			labels.add(call.label);
			chunk.asmcode.add(new AsmOPER("PUSHJ", "`d0,`l0", defs, null, labels));
		}
		else if(expression instanceof ImcESEQ == true)
		{
			Report.error("Nested ImcESEQ is not allowed.");
		}

		return temp;
	}

	public void optimize(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				optimize((ImcCodeChunk)chunk);
			}
		}
	}

	private void optimize(ImcCodeChunk chunk)
	{
		for(int i = 0; i < chunk.asmcode.size() - 1; i++)
		{
			AsmInstr instr = chunk.asmcode.get(i);

			if(instr instanceof AsmOPER == true && instr.mnemonic.equals("SET") == true && instr.uses.size() == 0)
			{
				FrmTemp def = instr.defs.getFirst();
				String constant = instr.assem.substring(instr.assem.indexOf(',') + 1);

				boolean ok = false;

				for(int j = i + 1; j < chunk.asmcode.size(); j++)
				{
					AsmInstr use = chunk.asmcode.get(j);
					int idx = use.uses.indexOf(def);

					if(idx == 1 && (ok = use.uses.remove(def)) == true)
					{
						use.assem = use.assem.substring(0, use.assem.lastIndexOf(',') + 1) + constant;
					}
					else if(use.mnemonic.equals("STO") == true && idx == 0 && (ok = use.uses.remove(def)) == true)
					{
						use.mnemonic = "STCO";
						use.assem = constant + ",`s0" + use.assem.substring(use.assem.lastIndexOf(','));
					}
					else if(use instanceof AsmMOVE == true && idx == 0 && (ok = use.uses.remove(def)) == true)
					{
						chunk.asmcode.remove(j);
						chunk.asmcode.add(j, new AsmOPER("SET", use.assem.substring(0, use.assem.indexOf(',') + 1) + constant, use.defs, use.uses));
					}
				}

				if(ok == true)
				{
					chunk.asmcode.remove(i--);
				}
			}
			else if(instr instanceof AsmLABEL == true)
			{
				AsmInstr next = chunk.asmcode.get(i + 1);

				if(next instanceof AsmLABEL == true)
				{
					for(int j = i + 2; j < chunk.asmcode.size(); j++)
					{
						AsmInstr temp = chunk.asmcode.get(j);

						if(temp instanceof AsmOPER == true && temp.labels.remove(next.labels.getFirst()) == true)
						{
							temp.labels.add(instr.labels.getFirst());
						}
					}

					chunk.asmcode.remove(i + 1);
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
					Report.dump(0, instr.format(null));
				}
			}
		}
	}
}
