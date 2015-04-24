package compiler.lincode;

import java.util.*;

import compiler.*;
import compiler.imcode.*;

/**
 * Izracun lineariziranih fragmentov vmesne kode.
 * 
 * @author sliva
 */
public class LinCode {

	/** Ali se izpisujejo vmesni rezultati. */
	private boolean dump;

	/**
	 * Izracun lineariziranih fragmentov vmesne kode.
	 * 
	 * @param dump
	 *            Ali se izpisujejo vmesni rezultati.
	 */
	public LinCode(boolean dump) {
		this.dump = dump;
	}

	/**
	 * Izvede program s klicem funkcije 'main'.
	 * 
	 * @param chunks
	 *            Seznam fragmentov vmesne kode.
	 */
	public void run(LinkedList<ImcChunk> chunks) {
		// TODO
	}

	/**
	 * Izpise linearizirane fragmente vmesne kode na datoteko vmesnih
	 * rezultatov.
	 * 
	 * @param chunks
	 *            Seznam fragmentov vmesne kode.
	 */
	public void dump(LinkedList<ImcChunk> chunks) {
		if (!dump)
			return;
		if (Report.dumpFile() == null)
			return;
		for (int chunk = 0; chunk < chunks.size(); chunk++)
			chunks.get(chunk).dump();
	}

}
