package compiler.imcode;

import compiler.*;
import compiler.frames.*;

/**
 * Fragment podatkov.
 *
 * @author sliva
 */
public class ImcDataChunk extends ImcChunk {

	/** Naslov spremenljivke v pomnilniku.  */
	public FrmLabel label;

	/** Velikost spremenljivke v pomnilniku.  */
	public long size;

	public static final int BYTE = 1;
	public static final int WYDE = 2;
	public static final int TETRA = 4;
	public static final int OCTA = 8;

	public String data;

	/**
	 * Ustvari novfragment podatkov.
	 *
	 * @param label Labela podatka.
	 * @param size Velikost podatka.
	 */
	public ImcDataChunk(FrmLabel label, long size) {
		this(label, size, null);
	}

	public ImcDataChunk(FrmLabel label, long size, String data) {
		this.label = label;
		this.size = size;
		this.data = data;
	}

	@Override
	public void dump() {
		Report.dump(0, "DATA CHUNK: label=" + label.name() + " size=" + size);
	}

}
