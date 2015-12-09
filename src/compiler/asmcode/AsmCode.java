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
				FrmTemp src = parse(move.src);
				FrmTemp dst = parse(((ImcMEM)move.dst).expr);

				chunk.asmcode.add(new AsmOPER("ST`s0", "ADDR", null, new LinkedList<FrmTemp>(Arrays.asList(dst))));
				chunk.asmcode.add(new AsmOPER("ST`s0", "@ADDR", null, new LinkedList<FrmTemp>(Arrays.asList(src))));
			}
			else if(move.dst instanceof ImcTEMP == true)
			{
				chunk.asmcode.add(new AsmMOVE("RMO", "`s0,`d0", parse(move.dst), parse(move.src)));
			}
		}
		else if(statement instanceof ImcCJUMP == true)
		{
			ImcCJUMP cjump = (ImcCJUMP)statement;
			labels.add(cjump.trueLabel);
			parse(cjump.cond);

			if(cjump.cond instanceof ImcBINOP == true)
			{
				switch(((ImcBINOP)cjump.cond).op)
				{
					case ImcBINOP.EQU:
						chunk.asmcode.add(new AsmOPER("JEQ", "`l0", null, null, labels));
						break;

					case ImcBINOP.NEQ:
						chunk.asmcode.add(new AsmOPER("JGT", "`l0", null, null, labels));
						chunk.asmcode.add(new AsmOPER("JLT", "`l0", null, null, labels));
						break;

					case ImcBINOP.LTH:
						chunk.asmcode.add(new AsmOPER("JLT", "`l0", null, null, labels));
						break;

					case ImcBINOP.LEQ:
						chunk.asmcode.add(new AsmOPER("JLT", "`l0", null, null, labels));
						chunk.asmcode.add(new AsmOPER("JEQ", "`l0", null, null, labels));
						break;

					case ImcBINOP.GTH:
						chunk.asmcode.add(new AsmOPER("JGT", "`l0", null, null, labels));
						break;

					case ImcBINOP.GEQ:
						chunk.asmcode.add(new AsmOPER("JGT", "`l0", null, null, labels));
						chunk.asmcode.add(new AsmOPER("JEQ", "`l0", null, null, labels));
						break;
				}
			}
			else if(cjump.cond instanceof ImcCONST == true)
			{
				chunk.asmcode.add(new AsmOPER("JGT", "`l0", null, null, labels));
			}
		}
		else if(statement instanceof ImcJUMP == true)
		{
			labels.add(((ImcJUMP)statement).label);
			chunk.asmcode.add(new AsmOPER("J", "`l0", null, null, labels));
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

			// TODO vse operacije
			switch(binop.op)
			{
				// teh dveh se ne da z registri
				// tole bi moralo biti COMPR, BIT_X pa navadno
				case ImcBINOP.OR:  oper = "OR";  break;
				case ImcBINOP.AND: oper = "AND"; break;

				case ImcBINOP.ADD: oper = "ADDR"; break;
				case ImcBINOP.SUB: oper = "SUBR"; break;
				case ImcBINOP.MUL: oper = "MULR"; break;
				case ImcBINOP.DIV: oper = "DIVR"; break;

				case ImcBINOP.EQU: case ImcBINOP.NEQ:
				case ImcBINOP.LTH: case ImcBINOP.LEQ:
				case ImcBINOP.GTH: case ImcBINOP.GEQ:
					oper = "COMPR";
				break;
			}

			FrmTemp left = parse(binop.limc);
			FrmTemp right = parse(binop.rimc);

			if(oper.equals("COMPR") == true)
			{
				chunk.asmcode.add(new AsmOPER("COMPR", "`s0,`s1", null, new LinkedList<FrmTemp>(Arrays.asList(left, right))));
			}
			else
			{
				defs.add(temp = new FrmTemp());
				chunk.asmcode.add(new AsmMOVE("RMO", "`s0,`d0", temp, left));
				chunk.asmcode.add(new AsmOPER(oper, "`s0,`d0", defs, new LinkedList<FrmTemp>(Arrays.asList(right))));
			}

		}
		else if(expression instanceof ImcTEMP == true)
		{
			temp = ((ImcTEMP)expression).temp;
		}
		else if(expression instanceof ImcCONST == true)
		{
			int constant = ((ImcCONST)expression).value;
			int value = Math.abs(constant);

			defs.add(temp = new FrmTemp());

			if(constant < 0)
			{
				LinkedList<FrmTemp> register = new LinkedList<FrmTemp>(Arrays.asList(new FrmTemp()));
				chunk.asmcode.add(new AsmOPER("CLEAR", "`d0", defs, null));
				chunk.asmcode.add(new AsmOPER("LD`d0", "#" + value, register, null));
				chunk.asmcode.add(new AsmOPER("SUBR", "`s0,`d0", defs, register));
			}
			else
			{
				chunk.asmcode.add(new AsmOPER("LD`d0", "#" + value, defs, null));
			}
		}
		else if(expression instanceof ImcMEM == true)
		{
			// tukaj moramo shraniti naslov, nato pa ponovno naloziti
			uses.add(parse(((ImcMEM)expression).expr));
			chunk.asmcode.add(new AsmOPER("ST`s0", "ADDR", null, uses));
			defs.add(temp = new FrmTemp());
			chunk.asmcode.add(new AsmOPER("LD`d0", "@ADDR", defs, null));
		}
		else if(expression instanceof ImcNAME == true)
		{
			// ali tukaj res potrebujem naslov ali bi bilo dovolj, da nalozim normalno? ne bi bilo dovolj, ker so lahko kjerkoli,
			// ampak bodo vecinoma na zacetku programa? lahko bi pa globalne spremenljivke prestavil na konec programa
			defs.add(temp = new FrmTemp());
			labels.add(((ImcNAME)expression).label);
			chunk.asmcode.add(new AsmOPER("+LD`d0", "#`l0", defs, null, labels));
		}
		// kam ze shranimo registre, ko ni prostora?
		else if(expression instanceof ImcCALL == true)
		{
			ImcCALL call = (ImcCALL)expression;
			LinkedList<FrmTemp> stackPtr = new LinkedList<FrmTemp>(Arrays.asList(new FrmTemp()));
			// kadar delamo s FP in SP, uporabimo +; popravi se drugje
			chunk.asmcode.add(new AsmOPER("+LD`d0", "`s0", stackPtr, new LinkedList<FrmTemp>(Arrays.asList(chunk.frame.SP))));
			// pravilna velikost!
			FrmTemp size = new FrmTemp();
			chunk.asmcode.add(new AsmOPER("LD`d0", "#" + call.args.size() * 3, new LinkedList<FrmTemp>(Arrays.asList(size)), null));
			chunk.asmcode.add(new AsmOPER("ADDR", "`s0,`d0", stackPtr, new LinkedList<FrmTemp>(Arrays.asList(size))));

			// kako je z argumenti razlicnih velikosti?
			for(int i = call.args.size() - 1; i >= 0; i--)
			{
				LinkedList<FrmTemp> argSize = new LinkedList<FrmTemp>(Arrays.asList(new FrmTemp()));
				// popravi velikost
				chunk.asmcode.add(new AsmOPER("LD`d0", "#" + 3, argSize, null));
				chunk.asmcode.add(new AsmOPER("SUBR", "`s0,`d0", stackPtr, argSize));
				chunk.asmcode.add(new AsmOPER("ST`s0", "ADDR", null, stackPtr));
				chunk.asmcode.add(new AsmOPER("ST`s0", "@ADDR", null, new LinkedList<FrmTemp>(Arrays.asList(((ImcTEMP)call.args.get(i)).temp))));
			}

			// remove defs; trenutno potrebujemo, ker drugace dobim nullptrex, verjetno, ker imamo kaksen move, kjer je funcall
			defs.add(temp = new FrmTemp());
			labels.add(call.label);
			chunk.asmcode.add(new AsmOPER("JSUB", "`l0", defs, null, labels));
			// premakni iz pomnilnika v temp
		}
		else if(expression instanceof ImcESEQ == true)
		{
			Report.error("Nested ImcESEQ is not allowed.");
		}

		return temp;
	}

	// public void optimize(LinkedList<ImcChunk> chunks)
	// {
	// 	for(ImcChunk chunk : chunks)
	// 	{
	// 		if(chunk instanceof ImcCodeChunk == true)
	// 		{
	// 			optimize((ImcCodeChunk)chunk);
	// 		}
	// 	}
	// }

	// private void optimize(ImcCodeChunk chunk)
	// {
	// 	for(int i = 0; i < chunk.asmcode.size() - 1; i++)
	// 	{
	// 		AsmInstr instr = chunk.asmcode.get(i);

	// 		if(instr.mnemonic.equals("SET") == true && instr.uses.size() == 0)
	// 		{
	// 			FrmTemp def = instr.defs.getFirst();
	// 			String constant = instr.assem.substring(instr.assem.indexOf(',') + 1);

	// 			boolean ok = false;

	// 			for(int j = 0; j < chunk.asmcode.size(); j++)
	// 			{
	// 				AsmInstr use = chunk.asmcode.get(j);
	// 				int idx = use.uses.indexOf(def);

	// 				if(use.defs.contains(def) == true && i != j)
	// 				{
	// 					break;
	// 				}
	// 				else if(idx == 1 && (ok = use.uses.remove(def)) == true)
	// 				{
	// 					use.assem = use.assem.substring(0, use.assem.lastIndexOf(',') + 1) + constant;
	// 				}
	// 				else if(use.mnemonic.equals("STO") == true && idx == 0 && (ok = use.uses.remove(def)) == true)
	// 				{
	// 					use.mnemonic = "STCO";
	// 					use.assem = constant + ",`s0" + use.assem.substring(use.assem.lastIndexOf(','));
	// 				}
	// 				else if(use instanceof AsmMOVE == true && idx == 0 && (ok = use.uses.remove(def)) == true)
	// 				{
	// 					chunk.asmcode.remove(j);
	// 					chunk.asmcode.add(j, new AsmOPER("SET", use.assem.substring(0, use.assem.indexOf(',') + 1) + constant, use.defs, use.uses));
	// 				}
	// 			}

	// 			if(ok == true)
	// 			{
	// 				chunk.asmcode.remove(i--);
	// 			}
	// 		}
	// 		else if(instr instanceof AsmLABEL == true)
	// 		{
	// 			AsmInstr next = chunk.asmcode.get(i + 1);

	// 			if(next instanceof AsmLABEL == true)
	// 			{
	// 				for(int j = 0; j < chunk.asmcode.size(); j++)
	// 				{
	// 					AsmInstr temp = chunk.asmcode.get(j);

	// 					if(temp instanceof AsmOPER == true && temp.labels.remove(next.labels.getFirst()) == true)
	// 					{
	// 						temp.labels.add(instr.labels.getFirst());
	// 					}
	// 				}

	// 				chunk.asmcode.remove(i + 1);
	// 			}
	// 		}
	// 		else if(instr.mnemonic.equals("PUSHJ") == false)
	// 		{
	// 			AsmInstr use = chunk.asmcode.get(i + 1);

	// 			if(use instanceof AsmMOVE == true && use.uses.contains(instr.defs.getFirst()) == true)
	// 			{
	// 				instr.defs.set(0, use.defs.getFirst());
	// 				chunk.asmcode.remove(i + 1);
	// 				i--;
	// 			}
	// 		}
	// 	}
	// }

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
