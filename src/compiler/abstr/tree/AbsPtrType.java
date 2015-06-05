package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

/**
 * Opis kazalcnega tipa.
 *
 * @author sliva
 */
public class AbsPtrType extends AbsType {

	/** Osnovni tip. */
	public final AbsType type;

	/**
	 * Ustvari nov opis kazalcnega tipa.
	 *
	 * @param pos
	 *            Polozaj stavcne oblike tega drevesa.
	 * @param type
	 *            Osnovni tip.
	 */
	public AbsPtrType(Position pos, AbsType type) {
		super(pos);
		this.type = type;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }

}
