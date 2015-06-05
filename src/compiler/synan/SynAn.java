package compiler.synan;

import java.util.Vector;

import compiler.*;
import compiler.lexan.*;
import compiler.abstr.tree.*;

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

	private Symbol prevSymbol;
	private Symbol currSymbol;

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

		prevSymbol = null;
		currSymbol = null;
	}

	private void prepareNext()
	{
		prevSymbol = currSymbol;
		currSymbol = null;
	}

	private void readNext()
	{
		readNext(false);
	}

	private void readNext(boolean allowEOF)
	{
		currSymbol = currSymbol == null ? lexAn.lexAn() : currSymbol;

		if(currSymbol.token == Token.EOF && allowEOF == false)
		{
			Report.error(currSymbol.position, "Unexpected end of file.");
		}
	}

	private void check(int token)
	{
		readNext();

		if(currSymbol.token != token)
		{
			Symbol symbol = new Symbol(token, "", null);
			String tokenName = symbol.toString().substring(0, symbol.toString().length() - 1);
			Report.error(currSymbol.position, "Expected " + tokenName + ", found " + currSymbol.toString().substring(0, currSymbol.toString().indexOf(':')) + ".");
		}

		prepareNext();
	}

	/**
	 * Opravi sintaksno analizo.
	 */
	public AbsTree parse() {
		dump("source -> definitions");
		AbsTree tree = parse_definitions();
		readNext(true);

		if(currSymbol.token != Token.EOF)
		{
			Report.error(currSymbol.position, "Unparsable symbol.");
		}

		return tree;
	}

	private AbsDefs parse_definitions()
	{
		dump("definitions -> definition definitions'");
		readNext();
		Position position = currSymbol.position;
		Vector<AbsDef> defs = new Vector<AbsDef>();
		defs.add(parse_definition());
		parse_definitions_(defs);

		return new AbsDefs(new Position(position, prevSymbol.position), defs);
	}

	private void parse_definitions_(Vector<AbsDef> definitions)
	{
		readNext(true);

		if(currSymbol.token == Token.SEMIC)
		{
			Symbol temp = prevSymbol;
			prepareNext();
			readNext(true);

			if(currSymbol.token == Token.EOF)
			{
				Report.warning(currSymbol.position, "Last definiton should not end with SEMIC.");
				prevSymbol = temp;
				dump("definitions' -> E");
				return;
			}

			dump("definitions' -> ; definition definitions'");
			definitions.add(parse_definition());
			parse_definitions_(definitions);
		}
		else if(currSymbol.token == Token.COMMA)
		{
			dump("definitions' -> ; definition definitions'");
			Report.warning(currSymbol.position, "Expected SEMIC, found COMMA.");
			prepareNext();
			definitions.add(parse_definition());
			parse_definitions_(definitions);
		}
		else
		{
			dump("definitions' -> E");
		}
	}

	private AbsDef parse_definition()
	{
		readNext();

		AbsDef definition = null;
		Position position = currSymbol.position;

		if(currSymbol.token == Token.KW_TYP)
		{
			dump("definition -> type_definition");
			prepareNext();
			dump("type_definition -> typ identifier : type");
			check(Token.IDENTIFIER);
			String name = prevSymbol.lexeme;
			check(Token.COLON);
			AbsType type = parse_type();
			definition = new AbsTypeDef(new Position(position, prevSymbol.position), name, type);
		}
		else if(currSymbol.token == Token.KW_FUN)
		{
			dump("definition -> function_definition");
			prepareNext();
			dump("function_definition -> fun identifier ( parameters ) : type = expression");
			check(Token.IDENTIFIER);
			String name = prevSymbol.lexeme;
			check(Token.LPARENT);
			Vector<AbsPar> parameters = parse_parameters();
			check(Token.RPARENT);
			check(Token.COLON);
			AbsType type = parse_type();
			check(Token.ASSIGN);
			AbsExpr expression = parse_expression();
			definition = new AbsFunDef(new Position(position, prevSymbol.position), name, parameters, type, expression);
		}
		else if(currSymbol.token == Token.KW_VAR)
		{
			dump("definition -> variable_definition");
			prepareNext();
			dump("variable_definition -> var identifier : type");
			check(Token.IDENTIFIER);
			String name = prevSymbol.lexeme;
			check(Token.COLON);
			AbsType type = parse_type();
			definition = new AbsVarDef(new Position(position, prevSymbol.position), name, type);
		}
		else
		{
			Report.error(currSymbol.position, "Not a definition expression.");
		}

		return definition;
	}

	private AbsType parse_type()
	{
		readNext();

		AbsType type = null;
		Position position = currSymbol.position;

		switch(currSymbol.token)
		{
			case Token.IDENTIFIER:
				dump("type -> identifier");
				type = new AbsTypeName(new Position(position, currSymbol.position), currSymbol.lexeme);
				prepareNext();
			break;

			case Token.LOGICAL:
				dump("type -> logical");
				type = new AbsAtomType(new Position(position, currSymbol.position), AbsAtomType.LOG);
				prepareNext();
			break;

			case Token.INTEGER:
				dump("type -> integer");
				type = new AbsAtomType(new Position(position, currSymbol.position), AbsAtomType.INT);
				prepareNext();
			break;

			case Token.STRING:
				dump("type -> string");
				type = new AbsAtomType(new Position(position, currSymbol.position), AbsAtomType.STR);
				prepareNext();
			break;

			case Token.KW_ARR:
				dump("type -> arr [ int_const ] type");
				prepareNext();
				check(Token.LBRACKET);
				check(Token.INT_CONST);
				String size = prevSymbol.lexeme;
				check(Token.RBRACKET);
				AbsType arrType = parse_type();
				type = new AbsArrType(new Position(position, prevSymbol.position), Long.parseLong(size), arrType);
			break;

			case Token.KW_REC:
				dump("type -> rec { components }");
				prepareNext();
				check(Token.LBRACE);
				Vector<AbsComp> components = parse_components();
				check(Token.RBRACE);
				type = new AbsRecType(new Position(position, prevSymbol.position), components);
			break;

			case Token.PTR:
				dump("type -> ^ type");
				prepareNext();
				AbsType ptrType = parse_type();
				type = new AbsPtrType(new Position(position, prevSymbol.position), ptrType);
			break;

			default:
				Report.error(currSymbol.position, "Not a type expression.");
			break;
		}

		return type;
	}

	private Vector<AbsComp> parse_components()
	{
		dump("components -> component components'");
		Vector<AbsComp> components = new Vector<AbsComp>();
		components.add(parse_component());
		parse_components_(components);

		return components;
	}

	private void parse_components_(Vector<AbsComp> components)
	{
		readNext(true);

		if(currSymbol.token != Token.COMMA)
		{
			dump("components' -> E");
			return;
		}

		dump("components' -> , component components'");
		prepareNext();
		components.add(parse_component());
		parse_components_(components);
	}

	private AbsComp parse_component()
	{
		dump("component -> identifier : type");
		check(Token.IDENTIFIER);
		Position position = prevSymbol.position;
		String name = prevSymbol.lexeme;
		check(Token.COLON);
		AbsType type = parse_type();

		return new AbsComp(new Position(position, prevSymbol.position), name, type);
	}

	private Vector<AbsPar> parse_parameters()
	{
		dump("parameters -> parameter parameters'");
		Vector<AbsPar> parameters = new Vector<AbsPar>();
		parameters.add(parse_parameter());
		parse_parameters_(parameters);

		return parameters;
	}

	private void parse_parameters_(Vector<AbsPar> parameters)
	{
		readNext(true);

		if(currSymbol.token != Token.COMMA)
		{
			dump("parameters' -> E");
			return;
		}

		dump("parameters' -> , parameter parameters'");
		prepareNext();
		parameters.add(parse_parameter());
		parse_parameters_(parameters);
	}

	private AbsPar parse_parameter()
	{
		dump("parameter -> identifier : type");
		check(Token.IDENTIFIER);
		Position position = prevSymbol.position;
		String name = prevSymbol.lexeme;
		check(Token.COLON);
		AbsType type = parse_type();

		return new AbsPar(new Position(position, prevSymbol.position), name, type);
	}

	private Vector<AbsExpr> parse_expressions()
	{
		dump("expressions -> expression expressions'");
		Vector<AbsExpr> expressions = new Vector<AbsExpr>();
		expressions.add(parse_expression());
		parse_expressions_(expressions);

		return expressions;
	}

	private void parse_expressions_(Vector<AbsExpr> expressions)
	{
		readNext(true);

		if(currSymbol.token != Token.COMMA)
		{
			dump("expressions' -> E");
			return;
		}

		prepareNext();
		readNext(true);

		if(currSymbol.token == Token.RPARENT)
		{
			Report.warning(prevSymbol.position, "Last expression should not end with COMMA.");
			dump("expressions' -> E");
			return;
		}

		dump("expressions' -> , expression expressions'");
		expressions.add(parse_expression());
		parse_expressions_(expressions);
	}

	private AbsExpr parse_expression()
	{
		dump("expression -> logical_ior_expression expression'");
		AbsExpr expression = parse_logical_ior_expression();
		AbsDefs definitions = parse_expression_();

		if(definitions != null)
		{
			expression = new AbsWhere(new Position(expression.position, prevSymbol.position), expression, definitions);
		}

		return expression;
	}

	private AbsDefs parse_expression_()
	{
		readNext(true);

		if(currSymbol.token != Token.LBRACE)
		{
			dump("expression' -> E");
			return null;
		}

		dump("expression' -> { where definitions }");
		prepareNext();
		check(Token.KW_WHERE);
		AbsDefs definitions = parse_definitions();
		check(Token.RBRACE);

		return definitions;
	}

	private AbsExpr parse_logical_ior_expression()
	{
		dump("logical_ior_expression -> logical_and_expression logical_ior_expression'");
		return parse_logical_ior_expression_(parse_logical_and_expression());
	}

	private AbsExpr parse_logical_ior_expression_(AbsExpr left)
	{
		readNext(true);

		if(currSymbol.token == Token.IOR)
		{
			dump("logical_ior_expression' -> | logical_and_expression logical_ior_expression'");
			prepareNext();
			AbsExpr right = parse_logical_and_expression();
			left = parse_logical_ior_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.IOR, left, right));
		}
		else
		{
			dump("logical_ior_expression' -> E");
		}

		return left;
	}

	private AbsExpr parse_logical_and_expression()
	{
		dump("logical_and_expression -> comparative_expression logical_and_expression'");
		return parse_logical_and_expression_(parse_comparative_expression());
	}

	private AbsExpr parse_logical_and_expression_(AbsExpr left)
	{
		readNext(true);

		if(currSymbol.token == Token.AND)
		{
			dump("logical_and_expression' -> & comparative_expression logical_and_expression'");
			prepareNext();
			AbsExpr right = parse_comparative_expression();
			left = parse_logical_and_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.AND, left, right));
		}
		else
		{
			dump("logical_and_expression' -> E");
		}

		return left;
	}

	private AbsExpr parse_comparative_expression()
	{
		dump("comparative_expression -> additive_expression comparative_expression'");
		return parse_comparative_expression_(parse_additive_expression());
	}

	private AbsExpr parse_comparative_expression_(AbsExpr left)
	{
		readNext(true);

		AbsExpr right = null;
		int operation = 0;

		if(currSymbol.token == Token.EQU)
		{
			dump("comparative_expression' -> == additive_expression");
			operation = AbsBinExpr.EQU;
			prepareNext();
			right = parse_additive_expression();
		}
		else if(currSymbol.token == Token.NEQ)
		{
			dump("comparative_expression' -> != additive_expression");
			operation = AbsBinExpr.NEQ;
			prepareNext();
			right = parse_additive_expression();
		}
		else if(currSymbol.token == Token.LEQ)
		{
			dump("comparative_expression' -> <= additive_expression");
			operation = AbsBinExpr.LEQ;
			prepareNext();
			right = parse_additive_expression();
		}
		else if(currSymbol.token == Token.GEQ)
		{
			dump("comparative_expression' -> >= additive_expression");
			operation = AbsBinExpr.GEQ;
			prepareNext();
			right = parse_additive_expression();
		}
		else if(currSymbol.token == Token.LTH)
		{
			dump("comparative_expression' -> < additive_expression");
			operation = AbsBinExpr.LTH;
			prepareNext();
			right = parse_additive_expression();
		}
		else if(currSymbol.token == Token.GTH)
		{
			dump("comparative_expression' -> > additive_expression");
			operation = AbsBinExpr.GTH;
			prepareNext();
			right = parse_additive_expression();
		}
		else
		{
			dump("comparative_expression' -> E");
		}

		if(right != null)
		{
			left = new AbsBinExpr(new Position(left.position, right.position), operation, left, right);
		}

		return left;
	}

	private AbsExpr parse_additive_expression()
	{
		dump("additive_expression -> multiplicative_expression additive_expression'");
		return parse_additive_expression_(parse_multiplicative_expression());
	}

	private AbsExpr parse_additive_expression_(AbsExpr left)
	{
		readNext(true);

		if(currSymbol.token == Token.ADD)
		{
			dump("additive_expression' -> + multiplicative_expression additive_expression'");
			prepareNext();
			AbsExpr right = parse_multiplicative_expression();
			left = parse_additive_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.ADD, left, right));
		}
		else if(currSymbol.token == Token.SUB)
		{
			dump("additive_expression' -> - multiplicative_expression additive_expression'");
			prepareNext();
			AbsExpr right = parse_multiplicative_expression();
			left = parse_additive_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.SUB, left, right));
		}
		else
		{
			dump("additive_expression' -> E");
		}


		return left;
	}

	private AbsExpr parse_multiplicative_expression()
	{
		dump("multiplicative_expression -> prefix_expression multiplicative_expression'");
		return parse_multiplicative_expression_(parse_prefix_expression());
	}

	private AbsExpr parse_multiplicative_expression_(AbsExpr left)
	{
		readNext(true);

		if(currSymbol.token == Token.MUL)
		{
			dump("multiplicative_expression' -> * prefix_expression multiplicative_expression'");
			prepareNext();
			AbsExpr right = parse_prefix_expression();
			left = parse_multiplicative_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.MUL, left, right));
		}
		else if(currSymbol.token == Token.DIV)
		{
			dump("multiplicative_expression' -> / prefix_expression multiplicative_expression'");
			prepareNext();
			AbsExpr right = parse_prefix_expression();
			left = parse_multiplicative_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.DIV, left, right));
		}
		else if(currSymbol.token == Token.MOD)
		{
			dump("multiplicative_expression' -> % prefix_expression multiplicative_expression'");
			prepareNext();
			AbsExpr right = parse_prefix_expression();
			left = parse_multiplicative_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.MOD, left, right));
		}
		else
		{
			dump("multiplicative_expression' -> E");
		}

		return left;
	}

	private AbsExpr parse_prefix_expression()
	{
		readNext();

		AbsExpr expression = null;
		Position position = currSymbol.position;

		if(currSymbol.token == Token.ADD)
		{
			dump("prefix_expression -> + prefix_expression");
			prepareNext();
			AbsExpr expr = parse_prefix_expression();
			expression = new AbsUnExpr(new Position(position, prevSymbol.position), AbsUnExpr.ADD, expr);
		}
		else if(currSymbol.token == Token.SUB)
		{
			dump("prefix_expression -> - prefix_expression");
			prepareNext();
			AbsExpr expr = parse_prefix_expression();
			expression = new AbsUnExpr(new Position(position, prevSymbol.position), AbsUnExpr.SUB, expr);
		}
		else if(currSymbol.token == Token.PTR)
		{
			dump("prefix_expression -> ^ prefix_expression");
			prepareNext();
			AbsExpr expr = parse_prefix_expression();
			expression = new AbsUnExpr(new Position(position, prevSymbol.position), AbsUnExpr.MEM, expr);
		}
		else if(currSymbol.token == Token.NOT)
		{
			dump("prefix_expression -> ! prefix_expression");
			prepareNext();
			AbsExpr expr = parse_prefix_expression();
			expression = new AbsUnExpr(new Position(position, prevSymbol.position), AbsUnExpr.NOT, expr);
		}
		else
		{
			dump("prefix_expression -> postfix_expression");
			expression = parse_postfix_expression();
		}

		return expression;
	}

	private AbsExpr parse_postfix_expression()
	{
		dump("postfix_expression -> atom_expression postfix_expression'");
		readNext();

		return parse_postfix_expression_(currSymbol.position, parse_atom_expression());
	}

	private AbsExpr parse_postfix_expression_(Position position, AbsExpr left)
	{
		readNext(true);

		AbsExpr expression = null;

		if(currSymbol.token == Token.PTR)
		{
			dump("postfix_expression' -> ^ postfix_expression'");
			prepareNext();
			expression = parse_postfix_expression_(position, new AbsUnExpr(new Position(position, prevSymbol.position), AbsUnExpr.VAL, left));
		}
		else if(currSymbol.token == Token.DOT)
		{
			dump("postfix_expression' -> . identifier postfix_expression'");
			prepareNext();
			check(Token.IDENTIFIER);
			expression = parse_postfix_expression_(position, new AbsBinExpr(new Position(position, prevSymbol.position), AbsBinExpr.DOT, left, new AbsCompName(prevSymbol.position, prevSymbol.lexeme)));
		}
		else if(currSymbol.token == Token.LBRACKET)
		{
			dump("postfix_expression' -> [ expression ] postfix_expression'");
			prepareNext();
			AbsExpr array = parse_expression();
			check(Token.RBRACKET);
			expression = parse_postfix_expression_(position, new AbsBinExpr(new Position(position, prevSymbol.position), AbsBinExpr.ARR, left, array));
		}
		else
		{
			dump("postfix_expression' -> E");
			expression = left;
		}

		return expression;
	}

	private AbsExpr parse_atom_expression()
	{
		readNext();

		AbsExpr expression = null;
		Position position = currSymbol.position;

		if(currSymbol.token == Token.LOG_CONST)
		{
			dump("atom_expression -> log_constant");
			expression = new AbsAtomConst(currSymbol.position, AbsAtomConst.LOG, currSymbol.lexeme);
			prepareNext();
		}
		else if(currSymbol.token == Token.INT_CONST)
		{
			dump("atom_expression -> int_constant");
			expression = new AbsAtomConst(currSymbol.position, AbsAtomConst.INT, currSymbol.lexeme);
			prepareNext();
		}
		else if(currSymbol.token == Token.STR_CONST)
		{
			dump("atom_expression -> str_constant");
			expression = new AbsAtomConst(currSymbol.position, AbsAtomConst.STR, currSymbol.lexeme);
			prepareNext();
		}
		else if(currSymbol.token == Token.IDENTIFIER)
		{
			dump("atom_expression -> identifier atom_expression_identifier");
			expression = new AbsVarName(currSymbol.position, currSymbol.lexeme);
			String name = currSymbol.lexeme;
			prepareNext();
			Vector<AbsExpr> expressions = parse_atom_expression_identifier();

			if(expressions != null)
			{
				expression = new AbsFunCall(new Position(position, prevSymbol.position), name, expressions);
			}
		}
		else if(currSymbol.token == Token.LBRACE)
		{
			dump("atom_expression -> { atom_expression_braces }");
			prepareNext();
			expression = parse_atom_expression_braces();
			check(Token.RBRACE);
		}
		else if(currSymbol.token == Token.LPARENT)
		{
			dump("atom_expression -> ( expressions )");
			prepareNext();
			Vector<AbsExpr> expressions = parse_expressions();
			check(Token.RPARENT);
			expression = new AbsExprs(new Position(position, prevSymbol.position), expressions);
		}
		else
		{
			Report.error(currSymbol.position, "Not an atom expression.");
		}

		return expression;
	}

	private Vector<AbsExpr> parse_atom_expression_identifier()
	{
		readNext(true);

		Position position = currSymbol.position;

		if(currSymbol.token != Token.LPARENT)
		{
			dump("atom_expression_identifier -> E");
			return null;
		}

		dump("atom_expression_identifier -> ( expressions )");
		prepareNext();
		Vector<AbsExpr> expressions = parse_expressions();
		check(Token.RPARENT);

		return expressions;
	}

	private AbsExpr parse_atom_expression_braces()
	{
		readNext();

		AbsExpr expression = null;
		Position position = prevSymbol.position;

		if(currSymbol.token == Token.KW_IF)
		{
			dump("atom_expression_braces -> if expression then expression atom_expression_else");
			prepareNext();
			AbsExpr ifExpression = parse_expression();
			check(Token.KW_THEN);
			AbsExpr thenExpression = parse_expression();
			AbsExpr elseExpression = parse_atom_expression_else();

			readNext();

			if(elseExpression == null)
			{
				expression = new AbsIfThen(new Position(position, currSymbol.position), ifExpression, thenExpression);
			}
			else
			{
				expression = new AbsIfThenElse(new Position(position, currSymbol.position), ifExpression, thenExpression, elseExpression);
			}
		}
		else if(currSymbol.token == Token.KW_WHILE)
		{
			dump("atom_expression_braces -> while expression : expression");
			prepareNext();
			AbsExpr condition = parse_expression();
			check(Token.COLON);
			AbsExpr body = parse_expression();
			readNext();
			expression = new AbsWhile(new Position(position, currSymbol.position), condition, body);
		}
		else if(currSymbol.token == Token.KW_FOR)
		{
			dump("atom_expression_braces -> for expression = expression, expression, expression : expression");
			prepareNext();
			AbsExpr count = parse_expression();
			check(Token.ASSIGN);
			AbsExpr lo = parse_expression();
			check(Token.COMMA);
			AbsExpr hi = parse_expression();
			check(Token.COMMA);
			AbsExpr step = parse_expression();
			check(Token.COLON);
			AbsExpr body = parse_expression();
			readNext();
			expression = new AbsFor(new Position(position, currSymbol.position), count, lo, hi, step, body);
		}
		else
		{
			dump("atom_expression_braces -> expression = expression");
			AbsExpr left = parse_expression();
			check(Token.ASSIGN);
			AbsExpr right = parse_expression();
			readNext();
			expression = new AbsBinExpr(new Position(position, currSymbol.position), AbsBinExpr.ASSIGN, left, right);
		}

		return expression;
	}

	private AbsExpr parse_atom_expression_else()
	{
		readNext(true);

		if(currSymbol.token != Token.KW_ELSE)
		{
			dump("atom_expression_else -> E");
			return null;
		}

		dump("atom_expression_else -> else expression");
		prepareNext();

		return parse_expression();
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
