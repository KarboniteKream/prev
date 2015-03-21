package compiler.abstr.tree;

import compiler.*;
import compiler.abstr.*;

/**
 * Ime komponente strukture v izrazu.
 * 
 * @author sliva
 */
public class AbsCompName extends AbsExpr {
	
	/** Ime komponente. */
	public final String name;

	/**
	 * Ustvari nov opis imena komponente v izrazu.
	 * 
	 * @param pos
	 *            Polozaj stavcne oblike tega drevesa.
	 * @param name
	 *            Ime komponente.
	 */
	public AbsCompName(Position pos, String name) {
		super(pos);
		this.name = name;
	}

	@Override public void accept(Visitor visitor) { visitor.visit(this); }

}
