package compiler.build;

import java.util.*;

import compiler.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.asmcode.*;

public class Build
{
	private boolean dump;

	public Build(boolean dump)
	{
		this.dump = dump;
	}

	public void build(LinkedList<ImcChunk> chunks)
	{
		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;
				LinkedList<AsmInstr> prologue = new LinkedList<AsmInstr>();
				LinkedList<AsmInstr> epilogue = new LinkedList<AsmInstr>();

				if(codeChunk.frame.sizeLocs + 16 < 256)
				{
					prologue.add(new AsmOPER("SUB", "$0,$250," + (codeChunk.frame.sizeLocs + 16)));
				}
				else
				{
					prologue.add(new AsmOPER("SET", "$0," + (codeChunk.frame.sizeLocs + 16)));
					prologue.add(new AsmOPER("SUB", "$0,$250,$0"));
				}

				prologue.add(new AsmOPER("STO", "$251,$0,0"));
				prologue.add(new AsmOPER("GET", "$1,rJ"));
				prologue.add(new AsmOPER("STO", "$1,$0,8"));
				prologue.add(new AsmMOVE("SET", "`d0,`s0", codeChunk.frame.FP, codeChunk.frame.SP));

				if(codeChunk.frame.size() < 256)
				{
					prologue.add(new AsmOPER("SUB", "$250,$250," + codeChunk.frame.size()));
				}
				else
				{
					prologue.add(new AsmOPER("SET", "$0," + codeChunk.frame.size()));
					prologue.add(new AsmOPER("SUB", "$250,$250,$0"));
				}

				if(codeChunk.registers.get(codeChunk.frame.RV).equals("$0") == false)
				{
					epilogue.add(new AsmOPER("SET", "$0,`s0", null, new LinkedList<FrmTemp>(Arrays.asList(codeChunk.frame.RV))));
				}

				epilogue.add(new AsmMOVE("SET", "`d0,`s0", codeChunk.frame.SP, codeChunk.frame.FP));

				if(codeChunk.frame.sizeLocs + 16 < 256)
				{
					epilogue.add(new AsmOPER("SUB", "$1,$251," + (codeChunk.frame.sizeLocs + 16)));
				}
				else
				{
					epilogue.add(new AsmOPER("SET", "$1," + (codeChunk.frame.sizeLocs + 16)));
					epilogue.add(new AsmOPER("SUB", "$1,$251,$1"));
				}

				epilogue.add(new AsmOPER("LDO", "$251,$1,0"));
				epilogue.add(new AsmOPER("LDO", "$1,$1,8"));
				epilogue.add(new AsmOPER("PUT", "rJ,$1"));
				epilogue.add(new AsmOPER("POP", "1,0"));

				codeChunk.asmcode.addAll(1, prologue);
				codeChunk.asmcode.addAll(epilogue);
			}
		}

		LinkedList<String> functions = new LinkedList<String>();

		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				for(AsmInstr instr : ((ImcCodeChunk)chunk).asmcode)
				{
					if(instr.mnemonic.equals("PUSHJ") == true)
					{
						functions.add(instr.labels.getFirst().name());
					}
				}
			}
		}

		if(functions.contains("_get_int") == true)
		{
			ImcCodeChunk get_int = new ImcCodeChunk(null, null);
			get_int.asmcode = new LinkedList<AsmInstr>();

			get_int.asmcode.add(new AsmLABEL("`l0", new FrmLabel("_get_int")));
			get_int.asmcode.add(new AsmOPER("LDA", "$255,PIN"));
			get_int.asmcode.add(new AsmOPER("TRAP", "0,Fgets,StdIn"));
			get_int.asmcode.add(new AsmOPER("LDA", "$255,INT"));
			get_int.asmcode.add(new AsmOPER("LDB", "$1,$255,0"));
			get_int.asmcode.add(new AsmOPER("CMP", "$3,$1,45"));
			get_int.asmcode.add(new AsmOPER("BNZ", "$3,LGINT1"));
			get_int.asmcode.add(new AsmOPER("ADD", "$255,$255,1"));
			get_int.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LGINT1")));
			get_int.asmcode.add(new AsmOPER("LDB", "$1,$255,0"));
			get_int.asmcode.add(new AsmOPER("CMP", "$2,$1,10"));
			get_int.asmcode.add(new AsmOPER("BZ", "$2,LGINT2"));
			get_int.asmcode.add(new AsmOPER("SUB", "$1,$1,48"));
			get_int.asmcode.add(new AsmOPER("MUL", "$0,$0,10"));
			get_int.asmcode.add(new AsmOPER("ADD", "$0,$0,$1"));
			get_int.asmcode.add(new AsmOPER("ADD", "$255,$255,1"));
			get_int.asmcode.add(new AsmOPER("JMP", "LGINT1"));
			get_int.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LGINT2")));
			get_int.asmcode.add(new AsmOPER("BNZ", "$3,LGINT3"));
			get_int.asmcode.add(new AsmOPER("NEG", "$0,0,$0"));
			get_int.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LGINT3")));
			get_int.asmcode.add(new AsmOPER("POP", "1,0"));

			chunks.addFirst(get_int);
		}

		if(functions.contains("_put_int") == true)
		{
			ImcCodeChunk put_int = new ImcCodeChunk(null, null);
			put_int.asmcode = new LinkedList<AsmInstr>();

			put_int.asmcode.add(new AsmLABEL("`l0", new FrmLabel("_put_int")));
			put_int.asmcode.add(new AsmOPER("LDA", "$255,INT+20"));
			put_int.asmcode.add(new AsmOPER("LDO", "$0,$250,8"));
			put_int.asmcode.add(new AsmOPER("CMP", "$2,$0,0"));
			put_int.asmcode.add(new AsmOPER("BP", "$2,LPINT1"));
			put_int.asmcode.add(new AsmOPER("NEG", "$0,0,$0"));
			put_int.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LPINT1")));
			put_int.asmcode.add(new AsmOPER("SUB", "$255,$255,1"));
			put_int.asmcode.add(new AsmOPER("DIV", "$0,$0,10"));
			put_int.asmcode.add(new AsmOPER("GET", "$1,rR"));
			put_int.asmcode.add(new AsmOPER("ADD", "$1,$1,48"));
			put_int.asmcode.add(new AsmOPER("STB", "$1,$255,0"));
			put_int.asmcode.add(new AsmOPER("BNZ", "$0,LPINT1"));
			put_int.asmcode.add(new AsmOPER("BNN", "$2,LPINT2"));
			put_int.asmcode.add(new AsmOPER("SUB", "$255,$255,1"));
			put_int.asmcode.add(new AsmOPER("SET", "$0,45"));
			put_int.asmcode.add(new AsmOPER("STB", "$0,$255,0"));
			put_int.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LPINT2")));
			put_int.asmcode.add(new AsmOPER("TRAP", "0,Fputs,StdOut"));
			put_int.asmcode.add(new AsmOPER("POP", "0,0"));

			chunks.addFirst(put_int);
		}

		if(functions.contains("_put_nl") == true)
		{
			ImcCodeChunk put_nl = new ImcCodeChunk(null, null);
			put_nl.asmcode = new LinkedList<AsmInstr>();

			put_nl.asmcode.add(new AsmLABEL("`l0", new FrmLabel("_put_nl")));
			put_nl.asmcode.add(new AsmOPER("LDO", "$0,$250,8"));
			put_nl.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LNL1")));
			put_nl.asmcode.add(new AsmOPER("BZ", "$0,LNL2"));
			put_nl.asmcode.add(new AsmOPER("SUB", "$0,$0,1"));
			put_nl.asmcode.add(new AsmOPER("LDA", "$255,NL"));
			put_nl.asmcode.add(new AsmOPER("TRAP", "0,Fputs,StdOut"));
			put_nl.asmcode.add(new AsmOPER("JMP", "LNL1"));
			put_nl.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LNL2")));
			put_nl.asmcode.add(new AsmOPER("POP", "0,0"));

			chunks.addFirst(put_nl);
			chunks.addFirst(new ImcDataChunk(new FrmLabel("NL"), ImcDataChunk.BYTE, "10,0"));
		}

		if(functions.contains("_put_int") == true || functions.contains("_get_int") == true)
		{
			chunks.addFirst(new ImcDataChunk(new FrmLabel("INT"), ImcDataChunk.OCTA, "0,0,0"));
		}

		if(functions.contains("_get_int") == true)
		{
			chunks.addFirst(new ImcDataChunk(new FrmLabel("PIN"), ImcDataChunk.OCTA, "INT,21"));
		}

		if(functions.contains("_get_str") == true)
		{
			ImcCodeChunk get_str = new ImcCodeChunk(null, null);
			get_str.asmcode = new LinkedList<AsmInstr>();

			get_str.asmcode.add(new AsmLABEL("`l0", new FrmLabel("_get_str")));
			get_str.asmcode.add(new AsmOPER("LDA", "$255,SIN"));
			get_str.asmcode.add(new AsmOPER("TRAP", "0,Fgets,StdIn"));
			get_str.asmcode.add(new AsmOPER("LDA", "$255,STR"));
			get_str.asmcode.add(new AsmOPER("SET", "$0,$252"));
			get_str.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LGSTR1")));
			get_str.asmcode.add(new AsmOPER("LDB", "$1,$255"));
			get_str.asmcode.add(new AsmOPER("ADD", "$255,$255,1"));
			get_str.asmcode.add(new AsmOPER("CMP", "$2,$1,10"));
			get_str.asmcode.add(new AsmOPER("BZ", "$2,LGSTR2"));
			get_str.asmcode.add(new AsmOPER("STB", "$1,$252"));
			get_str.asmcode.add(new AsmOPER("ADD", "$252,$252,1"));
			get_str.asmcode.add(new AsmOPER("JMP", "LGSTR1"));
			get_str.asmcode.add(new AsmLABEL("`l0", new FrmLabel("LGSTR2")));
			get_str.asmcode.add(new AsmOPER("SET", "$1,0"));
			get_str.asmcode.add(new AsmOPER("STB", "$1,$252"));
			get_str.asmcode.add(new AsmOPER("ADD", "$252,$252,1"));
			get_str.asmcode.add(new AsmOPER("POP", "1,0"));

			chunks.addFirst(get_str);
		}

		if(functions.contains("_get_str") == true)
		{
			chunks.addFirst(new ImcDataChunk(new FrmLabel("STR"), ImcDataChunk.OCTA, "0,0,0,0,0,0,0,0,0,0"));
			chunks.addFirst(new ImcDataChunk(new FrmLabel("SIN"), ImcDataChunk.OCTA, "STR,80"));
		}

		if(functions.contains("_put_str") == true)
		{
			ImcCodeChunk put_str = new ImcCodeChunk(null, null);
			put_str.asmcode = new LinkedList<AsmInstr>();

			put_str.asmcode.add(new AsmLABEL("`l0", new FrmLabel("_put_str")));
			put_str.asmcode.add(new AsmOPER("LDO", "$255,$250,8"));
			put_str.asmcode.add(new AsmOPER("TRAP", "0,Fputs,StdOut"));
			put_str.asmcode.add(new AsmOPER("POP", "0,0"));

			chunks.addFirst(put_str);
		}

		if(functions.contains("_get_char_at") == true)
		{
			ImcCodeChunk get_char_at = new ImcCodeChunk(null, null);
			get_char_at.asmcode = new LinkedList<AsmInstr>();

			get_char_at.asmcode.add(new AsmLABEL("`l0", new FrmLabel("_get_char_at")));
			get_char_at.asmcode.add(new AsmOPER("LDO", "$0,$250,8"));
			get_char_at.asmcode.add(new AsmOPER("LDO", "$1,$250,16"));
			get_char_at.asmcode.add(new AsmOPER("LDB", "$0,$0,$1"));
			get_char_at.asmcode.add(new AsmOPER("POP", "1,0"));

			chunks.addFirst(get_char_at);
		}

		if(functions.contains("_put_char_at") == true)
		{
			ImcCodeChunk put_char_at = new ImcCodeChunk(null, null);
			put_char_at.asmcode = new LinkedList<AsmInstr>();

			put_char_at.asmcode.add(new AsmLABEL("`l0", new FrmLabel("_put_char_at")));
			put_char_at.asmcode.add(new AsmOPER("LDO", "$0,$250,8"));
			put_char_at.asmcode.add(new AsmOPER("LDO", "$1,$250,24"));
			put_char_at.asmcode.add(new AsmOPER("LDO", "$2,$250,16"));
			put_char_at.asmcode.add(new AsmOPER("STB", "$2,$0,$1"));
			put_char_at.asmcode.add(new AsmOPER("POP", "0,0"));

			chunks.addFirst(put_char_at);
		}

		ImcCodeChunk main = new ImcCodeChunk(null, null);
		main.asmcode = new LinkedList<AsmInstr>();

		main.asmcode.add(new AsmOPER("LOC", "#100"));
		main.asmcode.add(new AsmLABEL("`l0", new FrmLabel("Main")));
		main.asmcode.add(new AsmOPER("PUT", "rG,250"));
		main.asmcode.add(new AsmOPER("SET", "$250,7"));
		main.asmcode.add(new AsmOPER("SL", "$250,$250,60"));
		main.asmcode.add(new AsmOPER("SUB", "$250,$250,1"));
		main.asmcode.add(new AsmOPER("SET", "$251,$250"));
		main.asmcode.add(new AsmOPER("SET", "$252,4"));
		main.asmcode.add(new AsmOPER("SL", "$252,$252,60"));
		main.asmcode.add(new AsmOPER("PUSHJ", "$0,_main"));
		main.asmcode.add(new AsmOPER("TRAP", "0,Halt,0"));

		chunks.addFirst(main);
	}

	public void dump(LinkedList<ImcChunk> chunks)
	{
		if(dump == false) return;
		if(Report.dumpFile() == null) return;

		int labelLength = 4;
		boolean dataSegment = false;
		int i = 0;

		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcDataChunk == true)
			{
				int length = ((ImcDataChunk)chunk).label.name().length();

				if(length > labelLength)
				{
					labelLength = length;
				}

				dataSegment = true;
			}
			else if(chunk instanceof ImcCodeChunk == true)
			{
				for(AsmInstr instr : ((ImcCodeChunk)chunk).asmcode)
				{
					if(instr.labels.size() > 0)
					{
						int length = instr.labels.getFirst().name().length();

						if(length > labelLength)
						{
							labelLength = length;
						}
					}
				}
			}
		}

		if(dataSegment == true)
		{
			Report.dump(labelLength + 1, "LOC   Data_Segment");
			Report.dump(labelLength + 1, "GREG  @");

			for(ImcChunk chunk : chunks)
			{
				if(chunk instanceof ImcDataChunk == true)
				{
					ImcDataChunk dataChunk = (ImcDataChunk)chunk;

					if(dataChunk.data == null)
					{
						String data = "";

						for(long j = 0; j < (dataChunk.size / 8) - 1; j++)
						{
							data += "0,";
						}

						Report.dump(0, String.format("%-" + labelLength + "s ", dataChunk.label.name()) + "OCTA  " + data + "0");
					}
					else
					{
						String size = null;

						switch((int)dataChunk.size)
						{
							case ImcDataChunk.BYTE:  size = "BYTE "; break;
							case ImcDataChunk.WYDE:  size = "WYDE "; break;
							case ImcDataChunk.TETRA: size = "TETRA"; break;
							case ImcDataChunk.OCTA:  size = "OCTA "; break;
						}

						Report.dump(0, String.format("%-" + labelLength + "s ", dataChunk.label.name()) + size + " " + dataChunk.data);
					}
				}
			}

			Report.dump(0, "");
		}

		for(ImcChunk chunk : chunks)
		{
			if(chunk instanceof ImcCodeChunk == true)
			{
				if(i++ > 0)
				{
					Report.dump(0, "");
				}

				ImcCodeChunk codeChunk = (ImcCodeChunk)chunk;

				for(int j = 0; j < codeChunk.asmcode.size(); j++)
				{
					AsmInstr instr = codeChunk.asmcode.get(j);

					if(instr instanceof AsmLABEL == true)
					{
						if((j + 1) >= codeChunk.asmcode.size())
						{
							codeChunk.asmcode.add(new AsmOPER("SWYM", ""));
						}

						Report.dump(0, String.format("%-" + labelLength + "s %s", instr.labels.getFirst().name(), codeChunk.asmcode.get(++j).format(codeChunk.registers)));
					}
					else
					{
						Report.dump(labelLength + 1, instr.format(codeChunk.registers));
					}
				}
			}
		}
	}
}
