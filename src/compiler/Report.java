package compiler;

import java.io.FileNotFoundException;
import java.io.PrintStream;

public class Report {
	public static final boolean reporting = true;
	private static PrintStream dumpFile = null;

	public static void report(String message) {
		if (reporting) {
			System.err.println(":-) " + message);
		}
	}

	public static void report(Position pos, String message) {
		report("[" + pos.toString() + "] " + message);
	}

	public static void warning(String message) {
		System.err.println(":-o " + message);
	}

	public static void warning(Position pos, String message) {
		warning("[" + pos.toString() + "] " + message);
	}

	public static void warning(int line, int column, String message) {
		warning(new Position(line, column), message);
	}

	public static void error(String message) {
		System.err.println(":-( " + message);
		System.exit(1);
	}

	public static void error(Position pos, String message) {
		error("[" + pos.toString() + "] " + message);
	}

	public static void error(int line, int column, String message) {
		error(new Position(line, column), message);
	}

	public static void openDumpFile(String sourceFileName, boolean mms) {
		final String dumpFileName = sourceFileName.replaceFirst("\\.prev$", "") + (mms ? ".mms" : ".log");

		try {
			dumpFile = new PrintStream(dumpFileName);
		} catch (FileNotFoundException e) {
			Report.warning("Cannot produce dump file '" + dumpFileName + "'.");
		}
	}

	public static void closeDumpFile() {
		dumpFile.close();
		dumpFile = null;
	}

	public static PrintStream dumpFile() {
		if (dumpFile == null) {
			Report.error("Internal error: compiler.Report.dumpFile().");
		}

		return dumpFile;
	}

	public static void dump(int indent, String line) {
		for (int i = 0; i < indent; i++) {
			dumpFile.print(" ");
		}
		dumpFile.println(line);
	}
}
