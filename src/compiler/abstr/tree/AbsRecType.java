package compiler.abstr.tree;

import java.util.*;

import compiler.*;
import compiler.abstr.*;

/**
 * Opis strukture.
 * 
 * @author sliva
 */
public class AbsRecType extends AbsType {

	/** Seznam komponent. */
	private final AbsComp comps[];

	/**
	 * Ustvari nov opis strukture.
	 * 
	 * @param pos
	 *            Polozaj stavcne oblike tega drevesa.
	 * @param comps
	 *            Seznam komponent.
	 */
	public AbsRecType(Position pos, Vector<AbsComp> comps) {
		super(pos);
		this.comps = new AbsComp[comps.size()];
		for (int comp = 0; comp < comps.size(); comp++)
			this.comps[comp] = comps.elementAt(comp);
	}

	/**
	 * Vrne izbrano komponento.
	 * 
	 * @param index
	 *            Indeks koponente.
	 * @return Komponenta na izbranem mestu v strukturi.
	 */
	public AbsComp comp(int index) {
		return comps[index];
	}

	/**
	 * Vrne stevilo komponent v strukturi.
	 * 
	 * @return Stevilo komponent v strukturi.
	 */
	public int numComps() {
		return comps.length;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }

}
