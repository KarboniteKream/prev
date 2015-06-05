package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

/**
 * Opis komponente strukture.
 *
 * @author sliva
 *
 */
public class AbsComp extends AbsDef {

	/** Ime komponente. */
	public final String name;

	/** Tip komponente. */
	public final AbsType type;

	/**
	 * Ustvari nov opis komponente.
	 *
	 * @param pos
	 *            Polozaj stavcne oblike tega drevesa.
	 * @param name
	 *            Ime komponente.
	 * @param type
	 *            Tip komponente.
	 */
	public AbsComp(Position pos, String name, AbsType type) {
		super(pos);
		this.name = name;
		this.type = type;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }

}
