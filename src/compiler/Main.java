package compiler;

import java.util.LinkedList;

import compiler.lexan.*;
import compiler.synan.*;
import compiler.abstr.*;
import compiler.abstr.tree.*;
import compiler.seman.*;
import compiler.frames.*;
import compiler.imcode.*;
import compiler.lincode.*;
import compiler.asmcode.*;
import compiler.tmpan.*;
import compiler.regalloc.*;

/**
 * Osnovni razred prevajalnika, ki vodi izvajanje celotnega procesa prevajanja.
 *
 * @author sliva
 */
public class Main {

	/** Ime izvorne datoteke. */
	private static String sourceFileName;

	/** Seznam vseh faz prevajalnika. */
	private static String allPhases = "(lexan|synan|ast|seman|frames|imcode|lincode|asmcode|tmpan|regalloc)";

	/** Doloca zadnjo fazo prevajanja, ki se bo se izvedla. */
	private static String execPhase = "regalloc";

	/** Doloca faze, v katerih se bodo izpisali vmesni rezultati. */
	private static String dumpPhases = "regalloc";

	private static int registers = 8;

	/**
	 * Metoda, ki izvede celotni proces prevajanja.
	 *
	 * @param args
	 *            Parametri ukazne vrstice.
	 */
	public static void main(String[] args) {
		System.out.printf("This is PREV compiler, v0.1:\n");

		// Pregled ukazne vrstice.
		for (int argc = 0; argc < args.length; argc++) {
			if (args[argc].startsWith("--")) {
				// Stikalo v ukazni vrstici.
				if (args[argc].startsWith("--phase=")) {
					String phase = args[argc].substring("--phase=".length());
					if (phase.matches(allPhases))
						execPhase = phase;
					else
						Report.warning("Unknown exec phase '" + phase + "' ignored.");
					continue;
				}
				if (args[argc].startsWith("--dump=")) {
					String phases = args[argc].substring("--dump=".length());
					if (phases.matches(allPhases + "(," + allPhases + ")*"))
						dumpPhases = phases;
					else
						Report.warning("Illegal dump phases '" + phases + "' ignored.");
					continue;
				}
				if (args[argc].startsWith("--registers=")) {
					registers = Integer.parseInt(args[argc].substring("--registers=".length()));
					if (registers < 2 || registers > 250) {
						Report.warning("Invalid number of registers, defaulting to 8.");
						registers = 8;
					}
					continue;
				}
				// Neznano stikalo.
				Report.warning("Unrecognized switch in the command line.");
			} else {
				// Ime izvorne datoteke.
				if (sourceFileName == null)
					sourceFileName = args[argc];
				else
					Report.warning("Source file name '" + sourceFileName + "' ignored.");
			}
		}
		if (sourceFileName == null)
			Report.error("Source file name not specified.");

		// Odpiranje datoteke z vmesnimi rezultati.
		if (dumpPhases != null) Report.openDumpFile(sourceFileName);

		// Izvajanje faz prevajanja.
		while (true) {
			// Leksikalna analiza.
			LexAn lexAn = new LexAn(sourceFileName, dumpPhases.contains("lexan"));
			if (execPhase.equals("lexan")) {
				while (lexAn.lexAn().token != Token.EOF) {
				}
				break;
			}
			// Sintaksna analiza.
			SynAn synAn = new SynAn(lexAn, dumpPhases.contains("synan"));
			AbsTree source = synAn.parse();
			if (execPhase.equals("synan")) break;
			// Abstraktna sintaksa.
			Abstr ast = new Abstr(dumpPhases.contains("ast"));
			ast.dump(source);
			if (execPhase.equals("ast")) break;
			// Semanticna analiza.
			SemAn semAn = new SemAn(dumpPhases.contains("seman"));
			source.accept(new NameChecker());
			source.accept(new TypeChecker());
			semAn.dump(source);
			if (execPhase.equals("seman")) break;
			// Klicni zapisi.
			Frames frames = new Frames(dumpPhases.contains("frames"));
			source.accept(new FrmEvaluator());
			frames.dump(source);
			if (execPhase.equals("frames")) break;
			// Vmesna koda.
			ImCode imcode = new ImCode(dumpPhases.contains("imcode"));
			ImcCodeGen imcodegen = new ImcCodeGen();
			source.accept(imcodegen);
			imcode.dump(imcodegen.chunks);
			if (execPhase.equals("imcode")) break;
			// Linearizacija kode.
			LinCode lincode = new LinCode(dumpPhases.contains("lincode"));
			lincode.generate(imcodegen.chunks);
			lincode.dump(imcodegen.chunks);
			lincode.run(imcodegen.chunks);
			if (execPhase.equals("lincode")) break;
			// Generiranje strojnih ukazov.
			AsmCode asmcode = new AsmCode(dumpPhases.contains("asmcode"));
			asmcode.generate(imcodegen.chunks);
			asmcode.optimize(imcodegen.chunks);
			asmcode.dump(imcodegen.chunks);
			if (execPhase.equals("asmcode")) break;
			// Analiza spremenljivk.
			TmpAn tmpan = new TmpAn(dumpPhases.contains("tmpan"));
			tmpan.analyze(imcodegen.chunks);
			tmpan.dump(imcodegen.chunks);
			if (execPhase.equals("tmpan")) break;
			// Dodeljevanje registrov.
			RegAlloc regalloc = new RegAlloc(dumpPhases.contains("regalloc"), registers);
			regalloc.allocate(imcodegen.chunks);
			regalloc.dump(imcodegen.chunks);
			if (execPhase.equals("regalloc")) break;

			// Neznana faza prevajanja.
			if (! execPhase.equals(""))
				Report.warning("Unknown compiler phase specified.");
		}

		// Zapiranje datoteke z vmesnimi rezultati.
		if (dumpPhases != null) Report.closeDumpFile();

		System.out.printf(":-) Done.\n");
		System.exit(0);
	}
}
