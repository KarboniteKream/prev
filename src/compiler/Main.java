package compiler;

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
import compiler.build.*;

public class Main {
	private static final String allPhases = "(lexan|synan|ast|seman|frames|imcode|lincode|asmcode|tmpan|regalloc|build)";
	private static String sourceFileName;
	private static String execPhase = "build";
	private static String dumpPhases = "build";

	private static int registers = 8;

	public static void main(String[] args) {
		System.out.println("This is PREV compiler, v0.1:");

		for (String arg : args) {
			if (arg.startsWith("--")) {
				if (arg.startsWith("--phase=")) {
					final String phase = arg.substring("--phase=".length());
					if (phase.matches(allPhases)) {
						execPhase = phase;
					} else {
						Report.warning("Unknown exec phase '" + phase + "' ignored.");
					}
					continue;
				}

				if (arg.startsWith("--dump=")) {
					final String phases = arg.substring("--dump=".length());
					if (phases.matches(allPhases + "(," + allPhases + ")*")) {
						dumpPhases = phases;
					} else {
						Report.warning("Illegal dump phases '" + phases + "' ignored.");
					}
					continue;
				}

				if (arg.startsWith("--registers=")) {
					registers = Integer.parseInt(arg.substring("--registers=".length()));
					if (registers < 2 || registers > 250) {
						Report.warning("Invalid number of registers, defaulting to 8.");
						registers = 8;
					}
					continue;
				}

				Report.warning("Unrecognized switch in the command line.");
			} else {
				if (sourceFileName == null) {
					sourceFileName = arg;
				} else {
					Report.warning("Source file name '" + sourceFileName + "' ignored.");
				}
			}
		}

		if (sourceFileName == null) {
			Report.error("Source file name not specified.");
		}

		if (dumpPhases != null) {
			Report.openDumpFile(sourceFileName, dumpPhases.equals("build"));
		}

		while (true) {
			// Lexical analysis.
			final LexAn lexAn = new LexAn(sourceFileName, dumpPhases.contains("lexan"));
			if (execPhase.equals("lexan")) {
				while (lexAn.lexAn().token != Token.EOF) {}
				break;
			}

			// Syntax analysis.
			final SynAn synAn = new SynAn(lexAn, dumpPhases.contains("synan"));
			final AbsTree source = synAn.parse();
			if (execPhase.equals("synan")) {
				break;
			}

			// Abstract syntax tree.
			final Abstr ast = new Abstr(dumpPhases.contains("ast"));
			ast.dump(source);
			if (execPhase.equals("ast")) {
				break;
			}

			// Semantic analysis.
			final SemAn semAn = new SemAn(dumpPhases.contains("seman"));
			source.accept(new NameChecker());
			source.accept(new TypeChecker());
			semAn.dump(source);
			if (execPhase.equals("seman")) {
				break;
			}

			// Call frames.
			final Frames frames = new Frames(dumpPhases.contains("frames"));
			source.accept(new FrmEvaluator());
			frames.dump(source);
			if (execPhase.equals("frames")) {
				break;
			}

			// Intermediate code.
			final ImCode imcode = new ImCode(dumpPhases.contains("imcode"));
			final ImcCodeGen imcodegen = new ImcCodeGen();
			source.accept(imcodegen);
			imcode.dump(imcodegen.chunks);
			if (execPhase.equals("imcode")) {
				break;
			}

			// Code linearization.
			final LinCode lincode = new LinCode(dumpPhases.contains("lincode"));
			lincode.generate(imcodegen.chunks);
			lincode.dump(imcodegen.chunks);
			lincode.run(imcodegen.chunks);
			if (execPhase.equals("lincode")) {
				break;
			}

			// Assembly generation.
			final AsmCode asmcode = new AsmCode(dumpPhases.contains("asmcode"));
			asmcode.generate(imcodegen.chunks);
			asmcode.optimize(imcodegen.chunks);
			asmcode.dump(imcodegen.chunks);
			if (execPhase.equals("asmcode")) {
				break;
			}

			// Temporary variable analysis.
			final TmpAn tmpan = new TmpAn(dumpPhases.contains("tmpan"));
			tmpan.analyze(imcodegen.chunks);
			tmpan.dump(imcodegen.chunks);
			if (execPhase.equals("tmpan")) {
				break;
			}

			// Registry allocation.
			final RegAlloc regalloc = new RegAlloc(dumpPhases.contains("regalloc"), registers);
			regalloc.allocate(imcodegen.chunks);
			regalloc.dump(imcodegen.chunks);
			if (execPhase.equals("regalloc")) {
				break;
			}

			// Build phase.
			final Build build = new Build(dumpPhases.contains("build"));
			build.build(imcodegen.chunks);
			build.dump(imcodegen.chunks);
			if (execPhase.equals("build")) {
				break;
			}

			if (!execPhase.equals("")) {
				Report.warning("Unknown compiler phase specified.");
			}
		}

		if (dumpPhases != null) {
			Report.closeDumpFile();
		}

		System.out.println(":-) Done.");
		System.exit(0);
	}
}
