package compiler.synan;

import compiler.Report;
import compiler.lexan.*;

/**
 * Sintaksni analizator.
 * 
 * @author sliva
 */
public class SynAn {

	/** Leksikalni analizator. */
	private LexAn lexAn;

	/** Ali se izpisujejo vmesni rezultati. */
	private boolean dump;

	/**
	 * Ustvari nov sintaksni analizator.
	 * 
	 * @param lexAn
	 *            Leksikalni analizator.
	 * @param dump
	 *            Ali se izpisujejo vmesni rezultati.
	 */
	public SynAn(LexAn lexAn, boolean dump) {
		this.lexAn = lexAn;
		this.dump = dump;
		// TODO
	}

	/**
	 * Opravi sintaksno analizo.
	 */
	public void parse() {
		// TODO
	}

	/**
	 * Izpise produkcijo v datoteko z vmesnimi rezultati.
	 * 
	 * @param production
	 *            Produkcija, ki naj bo izpisana.
	 */
	private void dump(String production) {
		if (!dump)
			return;
		if (Report.dumpFile() == null)
			return;
		Report.dumpFile().println(production);
	}

}
