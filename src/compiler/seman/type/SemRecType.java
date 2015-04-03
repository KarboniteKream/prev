package compiler.seman.type;

import java.util.*;

/**
 * Opis tipa zapisa.
 * 
 * @author sliva
 */
public class SemRecType extends SemType {

	/** Imena komponent. */
	private final String compNames[];

	/** Tipi komponent. */
	private final SemType compTypes[];

	/**
	 * Ustvari nov opis zapisa.
	 * 
	 * @param compNames
	 *            Imena komponent.
	 * @param compTypes
	 *            Tipi komponent.
	 */
	public SemRecType(Vector<String> compNames, Vector<SemType> compTypes) {
		this.compNames = new String[compNames.size()];
		for (int comp = 0; comp < compNames.size(); comp++)
			this.compNames[comp] = compNames.elementAt(comp);
		this.compTypes = new SemType[compTypes.size()];
		for (int comp = 0; comp < compTypes.size(); comp++)
			this.compTypes[comp] = compTypes.elementAt(comp);
	}

	/**
	 * Vrne stevilo komponent.
	 * 
	 * @return Stevilo komponent.
	 */
	public int getNumComps() {
		return compTypes.length;
	}

	/**
	 * Vrne ime zahtevane komponente.
	 * 
	 * @param index
	 *            Indeks zahtevane komponente.
	 * @return Ime zahtevane komponente.
	 */
	public String getCompName(int index) {
		return compNames[index];
	}

	/**
	 * Vrne tip zahtevane komponente.
	 * 
	 * @param index
	 *            Indeks zahtevane komponente.
	 * @return Tip zahtevane komponente.
	 */
	public SemType getCompType(int index) {
		return compTypes[index];
	}

	@Override
	public boolean sameStructureAs(SemType type) {
		if (type.actualType() instanceof SemRecType) {
			SemRecType recordType = (SemRecType) (type.actualType());
			if (this.getNumComps() != recordType.getNumComps())
				return false;
			for (int i = 0; i < getNumComps(); i++)
				if (!this.getCompType(i).sameStructureAs(
						recordType.getCompType(i)))
					return false;
			return true;
		} else
			return false;
	}

	@Override
	public String toString() {
		String str = "";
		str += "REC(";
		for (int comp = 0; comp < compTypes.length; comp++)
			str += (comp > 0 ? "," : "") + compTypes[comp].toString();
		str += ")";
		return str;
	}

}
