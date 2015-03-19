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
		Symbol symbol = new Symbol(token, "", null);
		String tokenName = symbol.toString().substring(0, symbol.toString().length() - 1);

		readNext();

		if(currSymbol.token != token)
		{
			Report.error(currSymbol.position, "Got " + currSymbol.toString().substring(0, currSymbol.toString().indexOf(':')) + ", expected " + tokenName + ".");
		}

		currSymbol = null;
	}

	/**
	 * Opravi sintaksno analizo.
	 */
	public void parse() {
		dump("source -> definitions");
		parse_definitions();
		readNext(true);

		if(currSymbol.token != Token.EOF)
		{
			Report.error(currSymbol.position, "Unparsable symbol.");
		}
	}

	private void parse_definitions()
	{
		dump("definitions -> definition definitions'");
		parse_definition();
		parse_definitions_();
	}

	private void parse_definitions_()
	{
		readNext(true);

		if(currSymbol.token == Token.SEMIC)
		{
			currSymbol = null;
			readNext(true);

			if(currSymbol.token == Token.EOF)
			{
				Report.warning(currSymbol.position, "Last definiton should not end with SEMIC.");
				dump("definitions' -> E");
				return;
			}

			dump("definitions' -> ; definitions");
			parse_definitions();
		}
		else if(currSymbol.token == Token.COMMA)
		{
			Report.warning(currSymbol.position, "Got COMMA, expected SEMIC.");
			currSymbol = null;
			parse_definitions();
		}
		else
		{
			dump("definitions' -> E");
		}
	}

	private void parse_definition()
	{
		readNext();

		switch(currSymbol.token)
		{
			case Token.KW_TYP:
				dump("definition -> type_definition");
				currSymbol = null;
				dump("type_definition -> typ identifier : type");
				check(Token.IDENTIFIER);
				check(Token.COLON);
				parse_type();
			break;

			case Token.KW_FUN:
				dump("definition -> function_definition");
				currSymbol = null;
				dump("function_definition -> fun identifier ( parameters ) : type = expression");
				check(Token.IDENTIFIER);
				check(Token.LPARENT);
				parse_parameters();
				check(Token.RPARENT);
				check(Token.COLON);
				parse_type();
				check(Token.ASSIGN);
				parse_expression();
			break;

			case Token.KW_VAR:
				dump("definition -> variable_definition");
				currSymbol = null;
				dump("variable_definition -> var identifier : type");
				check(Token.IDENTIFIER);
				check(Token.COLON);
				parse_type();
			break;

			default:
				Report.error(currSymbol.position, "Not a definition expression.");
			break;
		}
	}

	private void parse_type()
	{
		readNext();

		switch(currSymbol.token)
		{
			case Token.IDENTIFIER:
				dump("type -> identifier");
				currSymbol = null;
			break;

			case Token.LOGICAL:
				dump("type -> logical");
				currSymbol = null;
			break;

			case Token.INTEGER:
				dump("type -> integer");
				currSymbol = null;
			break;

			case Token.STRING:
				dump("type -> string");
				currSymbol = null;
			break;

			case Token.KW_ARR:
				dump("type -> arr [ int_const ] type");
				currSymbol = null;
				check(Token.LBRACKET);
				check(Token.INT_CONST);
				check(Token.RBRACKET);
				parse_type();
			break;

			case Token.KW_REC:
				dump("type -> rec { components }");
				currSymbol = null;
				check(Token.LBRACE);
				parse_components();
				check(Token.RBRACE);
			break;

			case Token.PTR:
				dump("type -> ^ type");
				currSymbol = null;
				parse_type();
			break;

			default:
				Report.error(currSymbol.position, "Not a type expression.");
			break;
		}
	}

	private void parse_components()
	{
		dump("components -> component components'");
		parse_component();
		parse_components_();
	}

	private void parse_components_()
	{
		readNext(true);

		if(currSymbol.token == Token.COMMA)
		{
			dump("components' -> , components");
			currSymbol = null;
			parse_components();
		}
		else
		{
			dump("components' -> E");
		}
	}

	private void parse_component()
	{
		dump("component -> identifier : type");
		check(Token.IDENTIFIER);
		check(Token.COLON);
		parse_type();
	}

	private void parse_parameters()
	{
		dump("parameters -> parameter parameters'");
		parse_parameter();
		parse_parameters_();
	}

	private void parse_parameters_()
	{
		readNext(true);

		if(currSymbol.token == Token.COMMA)
		{
			dump("parameters' -> , parameters");
			currSymbol = null;
			parse_parameters();
		}
		else
		{
			dump("parameters' -> E");
		}
	}

	private void parse_parameter()
	{
		dump("parameter -> identifier : type");
		check(Token.IDENTIFIER);
		check(Token.COLON);
		parse_type();
	}

	private void parse_expressions()
	{
		dump("expressions -> expression expressions'");
		parse_expression();
		parse_expressions_();
	}

	private void parse_expressions_()
	{
		readNext(true);

		if(currSymbol.token == Token.COMMA)
		{
			dump("expressions' -> , expressions");
			currSymbol = null;
			parse_expressions();
		}
		else
		{
			dump("expressions' -> E");
		}
	}

	private void parse_expression()
	{
		dump("expression -> logical_ior_expression expression'");
		parse_logical_ior_expression();
		parse_expression_();
	}

	private void parse_expression_()
	{
		readNext(true);

		if(currSymbol.token == Token.LBRACE)
		{
			dump("expression' -> { where definitions }");
			currSymbol = null;
			check(Token.KW_WHERE);
			parse_definitions();
			check(Token.RBRACE);
		}
		else
		{
			dump("expression' -> E");
		}
	}

	private void parse_logical_ior_expression()
	{
		dump("logical_ior_expression -> logical_and_expression logical_ior_expression'");
		parse_logical_and_expression();
		parse_logical_ior_expression_();
	}

	private void parse_logical_ior_expression_()
	{
		readNext(true);

		if(currSymbol.token == Token.IOR)
		{
			dump("logical_ior_expression' -> | logical_ior_expression");
			currSymbol = null;
			parse_logical_ior_expression();
		}
		else
		{
			dump("logical_ior_expression' -> E");
		}
	}

	private void parse_logical_and_expression()
	{
		dump("logical_and_expression -> comparative_expression logical_and_expression'");
		parse_comparative_expression();
		parse_logical_and_expression_();
	}

	private void parse_logical_and_expression_()
	{
		readNext(true);

		if(currSymbol.token == Token.AND)
		{
			dump("logical_and_expression' -> & logical_and_expression");
			currSymbol = null;
			parse_logical_and_expression();
		}
		else
		{
			dump("logical_and_expression' -> E");
		}
	}

	private void parse_comparative_expression()
	{
		dump("comparative_expression -> additive_expression comparative_expression'");
		parse_additive_expression();
		parse_comparative_expression_();
	}

	private void parse_comparative_expression_()
	{
		readNext(true);

		if(currSymbol.token == Token.EQU)
		{
			dump("comparative_expression' -> == additive_expression");
			currSymbol = null;
			parse_additive_expression();
		}
		else if(currSymbol.token == Token.NEQ)
		{
			dump("comparative_expression' -> != additive_expression");
			currSymbol = null;
			parse_additive_expression();
		}
		else if(currSymbol.token == Token.LEQ)
		{
			dump("comparative_expression' -> <= additive_expression");
			currSymbol = null;
			parse_additive_expression();
		}
		else if(currSymbol.token == Token.GEQ)
		{
			dump("comparative_expression' -> >= additive_expression");
			currSymbol = null;
			parse_additive_expression();
		}
		else if(currSymbol.token == Token.LTH)
		{
			dump("comparative_expression' -> < additive_expression");
			currSymbol = null;
			parse_additive_expression();
		}
		else if(currSymbol.token == Token.GTH)
		{
			dump("comparative_expression' -> > additive_expression");
			currSymbol = null;
			parse_additive_expression();
		}
		else
		{
			dump("comparative_expression' -> E");
		}
	}

	private void parse_additive_expression()
	{
		dump("additive_expression -> multiplicative_expression additive_expression'");
		parse_multiplicative_expression();
		parse_additive_expression_();
	}

	private void parse_additive_expression_()
	{
		readNext(true);

		if(currSymbol.token == Token.ADD)
		{
			dump("additive_expression' -> + multiplicative_expression additive_expression'");
			currSymbol = null;
			parse_multiplicative_expression();
			parse_additive_expression_();
		}
		else if(currSymbol.token == Token.SUB)
		{
			dump("additive_expression' -> - multiplicative_expression additive_expression'");
			currSymbol = null;
			parse_multiplicative_expression();
			parse_additive_expression_();
		}
		else
		{
			dump("additive_expression' -> E");
		}
	}

	private void parse_multiplicative_expression()
	{
		dump("multiplicative_expression -> prefix_expression multiplicative_expression'");
		parse_prefix_expression();
		parse_multiplicative_expression_();
	}

	private void parse_multiplicative_expression_()
	{
		readNext(true);

		if(currSymbol.token == Token.MUL)
		{
			dump("multiplicative_expression' -> * prefix_expression multiplicative_expression'");
			currSymbol = null;
			parse_prefix_expression();
			parse_multiplicative_expression_();
		}
		else if(currSymbol.token == Token.DIV)
		{
			dump("multiplicative_expression' -> / prefix_expression multiplicative_expression'");
			currSymbol = null;
			parse_prefix_expression();
			parse_multiplicative_expression_();
		}
		else if(currSymbol.token == Token.MOD)
		{
			dump("multiplicative_expression' -> % prefix_expression multiplicative_expression'");
			currSymbol = null;
			parse_prefix_expression();
			parse_multiplicative_expression_();
		}
		else
		{
			dump("multiplicative_expression' -> E");
		}
	}

	private void parse_prefix_expression()
	{
		readNext();

		if(currSymbol.token == Token.ADD)
		{
			dump("prefix_expression -> + prefix_expression");
			currSymbol = null;
			parse_prefix_expression();
		}
		else if(currSymbol.token == Token.SUB)
		{
			dump("prefix_expression -> - prefix_expression");
			currSymbol = null;
			parse_prefix_expression();
		}
		else if(currSymbol.token == Token.PTR)
		{
			dump("prefix_expression -> ^ prefix_expression");
			currSymbol = null;
			parse_prefix_expression();
		}
		else if(currSymbol.token == Token.NOT)
		{
			dump("prefix_expression -> ! prefix_expression");
			currSymbol = null;
			parse_prefix_expression();
		}
		else
		{
			dump("prefix_expression -> postfix_expression");
			parse_postfix_expression();
		}
	}

	private void parse_postfix_expression()
	{
		dump("postfix_expression -> atom_expression postfix_expression'");
		parse_atom_expression();
		parse_postfix_expression_();
	}

	private void parse_postfix_expression_()
	{
		readNext(true);

		if(currSymbol.token == Token.PTR)
		{
			dump("postfix_expression' -> ^ postfix_expression'");
			currSymbol = null;
			parse_postfix_expression_();
		}
		else if(currSymbol.token == Token.DOT)
		{
			dump("postfix_expression' -> . identifier postfix_expression'");
			currSymbol = null;
			check(Token.IDENTIFIER);
			parse_postfix_expression_();
		}
		else if(currSymbol.token == Token.LBRACKET)
		{
			dump("postfix_expression' -> [ expression ] postfix_expression'");
			currSymbol = null;
			parse_expression();
			check(Token.RBRACKET);
			parse_postfix_expression_();
		}
		else
		{
			dump("postfix_expression' -> E");
		}
	}

	private void parse_atom_expression()
	{
		readNext();

		if(currSymbol.token == Token.LOG_CONST)
		{
			dump("atom_expression -> log_constant");
			currSymbol = null;
		}
		else if(currSymbol.token == Token.INT_CONST)
		{
			dump("atom_expression -> int_constant");
			currSymbol = null;
		}
		else if(currSymbol.token == Token.STR_CONST)
		{
			dump("atom_expression -> str_constant");
			currSymbol = null;
		}
		else if(currSymbol.token == Token.IDENTIFIER)
		{
			dump("atom_expression -> identifier atom_expression_identifier");
			currSymbol = null;
			parse_atom_expression_identifier();
		}
		else if(currSymbol.token == Token.LBRACE)
		{
			dump("atom_expression -> { atom_expression_braces }");
			currSymbol = null;
			parse_atom_expression_braces();
			check(Token.RBRACE);
		}
		else if(currSymbol.token == Token.LPARENT)
		{
			dump("atom_expression -> ( expressions )");
			currSymbol = null;
			parse_expressions();
			check(Token.RPARENT);
		}
		else
		{
			Report.error(currSymbol.position, "Not an atom expression.");
		}
	}

	private void parse_atom_expression_identifier()
	{
		readNext(true);

		if(currSymbol.token == Token.LPARENT)
		{
			dump("atom_expression_identifier -> ( expressions )");
			currSymbol = null;
			parse_expressions();
			check(Token.RPARENT);
		}
		else
		{
			dump("atom_expression_identifier -> E");
		}
	}

	private void parse_atom_expression_braces()
	{
		readNext();

		if(currSymbol.token == Token.KW_IF)
		{
			dump("atom_expression_braces -> if expression then expression atom_expression_else");
			currSymbol = null;
			parse_expression();
			check(Token.KW_THEN);
			parse_expression();
			parse_atom_expression_else();
		}
		else if(currSymbol.token == Token.KW_WHILE)
		{
			dump("atom_expression_braces -> while expression : expression");
			currSymbol = null;
			parse_expression();
			check(Token.COLON);
			parse_expression();
		}
		else if(currSymbol.token == Token.KW_FOR)
		{
			dump("atom_expression_braces -> for identifier = expression, expression, expression : expression");
			currSymbol = null;
			check(Token.IDENTIFIER);
			check(Token.ASSIGN);
			parse_expression();
			check(Token.COMMA);
			parse_expression();
			check(Token.COMMA);
			parse_expression();
			check(Token.COLON);
			parse_expression();
		}
		else
		{
			dump("atom_expression_braces -> expression = expression");
			parse_expression();
			check(Token.ASSIGN);
			parse_expression();
		}
	}

	private void parse_atom_expression_else()
	{
		readNext(true);

		if(currSymbol.token == Token.KW_ELSE)
		{
			dump("atom_expression_else -> else expression");
			currSymbol = null;
			parse_expression();
		}
		else
		{
			dump("atom_expression_else -> E");
		}
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
