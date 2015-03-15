package compiler.lexan;

import java.io.*;

import compiler.*;

/**
 * Leksikalni analizator.
 * 
 * @author sliva
 */
public class LexAn {
	
	/** Ali se izpisujejo vmesni rezultati. */
	private boolean dump;

	/**
	 * Ustvari nov leksikalni analizator.
	 * 
	 * @param sourceFileName
	 *            Ime izvorne datoteke.
	 * @param dump
	 *            Ali se izpisujejo vmesni rezultati.
	 */
	public LexAn(String sourceFileName, boolean dump) {		
		this.dump = dump;
		// TODO
	}
	
	/**
	 * Vrne naslednji simbol iz izvorne datoteke. Preden vrne simbol, ga izpise
	 * na datoteko z vmesnimi rezultati.
	 * 
	 * @return Naslednji simbol iz izvorne datoteke.
	 */
	public Symbol lexAn() {
		// TODO
	}

	/**
	 * Izpise simbol v datoteko z vmesnimi rezultati.
	 * 
	 * @param symb
	 *            Simbol, ki naj bo izpisan.
	 */
	private void dump(Symbol symb) {
		if (! dump) return;
		if (Report.dumpFile() == null) return;
		if (symb.token == Token.EOF)
			Report.dumpFile().println(symb.toString());
		else
			Report.dumpFile().println("[" + symb.position.toString() + "] " + symb.toString());
	}

}
