package compiler.synan;

import java.util.Vector;

import compiler.*;
import compiler.lexan.*;
import compiler.abstr.tree.*;

public class SynAn
{
	private LexAn lexAn;
	private boolean dump;

	private Token prevToken;
	private Token currToken;

	public SynAn(LexAn lexAn, boolean dump)
	{
		this.lexAn = lexAn;
		this.dump = dump;

		prevToken = null;
		currToken = null;
	}

	private void prepareNext()
	{
		prevToken = currToken;
		currToken = null;
	}

	private void readNext()
	{
		readNext(false);
	}

	private void readNext(boolean allowEOF)
	{
		currToken = currToken == null ? lexAn.lexAn() : currToken;

		if(currToken.token == Token.EOF && allowEOF == false)
		{
			Report.error(currToken.position, "Unexpected end of file.");
		}
	}

	private void check(int token)
	{
		readNext();

		if(currToken.token != token)
		{
			Report.error(currToken.position, "Expected " + new Token(token, "", null).toString() + ", found " + currToken.toString() + ".");
		}

		prepareNext();
	}

	public AbsTree parse()
	{
		dump("source -> definitions");
		AbsTree tree = parse_definitions();
		readNext(true);

		if(currToken.token != Token.EOF)
		{
			Report.error(currToken.position, "Unparsable token.");
		}

		return tree;
	}

	private AbsDefs parse_definitions()
	{
		dump("definitions -> definition definitions'");
		readNext();
		Position position = currToken.position;
		Vector<AbsDef> defs = new Vector<AbsDef>();
		defs.add(parse_definition());
		parse_definitions_(defs);

		return new AbsDefs(new Position(position, prevToken.position), defs);
	}

	private void parse_definitions_(Vector<AbsDef> definitions)
	{
		readNext(true);

		if(currToken.token != Token.EOF)
		{
			dump("definitions' -> definition definitions'");
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
		Position position = currToken.position;

		if(currToken.token == Token.TYPEDEF)
		{
			dump("definition -> type_definition");
			prepareNext();
			dump("type_definition -> typedef type identifier;");
			AbsType type = parse_type();
			check(Token.IDENTIFIER);
			String name = prevToken.lexeme;
			check(Token.SEMICOLON);
			definition = new AbsTypeDef(new Position(position, prevToken.position), name, type);
		}
		else if(currToken.token == Token.INTEGER || currToken.token == Token.CHAR || currToken.token == Token.FLOAT || currToken.token == Token.BOOLEAN || currToken.token == Token.VOID || currToken.token == Token.CONST || currToken.token == Token.IDENTIFIER)
		{
			// prehitro izpise tip. ali naj sploh imamo izpis?
			AbsType type = parse_type();
			check(Token.IDENTIFIER);
			String name = prevToken.lexeme;
			readNext();

			if(currToken.token == Token.LPARENT)
			{
				dump("definition -> function_definition");
				prepareNext();
				dump("function_definition -> type identifier(parameters) compound_statement");
				Vector<AbsPar> parameters = parse_parameters();
				check(Token.RPARENT);
				Vector<AbsTree> statement = parse_compound_statement();
				// TODO: Position
				definition = new AbsFunDef(new Position(position, prevToken.position), name, parameters, type, new AbsExprs(new Position(0, 0), statement));
			}
			else
			{
				definition = parse_variable_definition(position, type, name);
			}
		}
		else
		{
			Report.error(currToken.position, "Not a definition.");
		}

		return definition;
	}

	private AbsDef parse_variable_definition(Position position, AbsType type, String name)
	{
		dump("definition -> variable_definition");
		readNext();

		AbsDef variable = null;

		if(currToken.token == Token.LBRACKET)
		{
			prepareNext();
			readNext();

			int length = 0;

			if(currToken.token == Token.INT_CONST)
			{
				prepareNext();
				length = Integer.parseInt(prevToken.lexeme);
			}

			readNext();

			if(currToken.token != Token.RBRACKET)
			{
				Report.error(currToken.position, "Not a variable definition.");
			}

			prepareNext();
			type = new AbsArrType(new Position(type.position, prevToken.position), length, type);
		}

		readNext();

		if(currToken.token == Token.SEMICOLON)
		{
			dump("variable_definition -> type identifier;");
			prepareNext();
			variable = new AbsVarDef(new Position(position, prevToken.position), name, type, null);
		}
		else if(currToken.token == Token.ASSIGN)
		{
			// TODO: ce je string, nastavi velikost.
			dump("variable_definition -> type identifier = expression;");
			prepareNext();
			AbsExpr expression = parse_expression();
			check(Token.SEMICOLON);
			variable = new AbsVarDef(new Position(position, prevToken.position), name, type, expression);
		}
		else
		{
			Report.error(currToken.position, "Not a variable definition.");
		}

		return variable;
	}

	private AbsType parse_type()
	{
		readNext();

		AbsType type = null;
		Position position = currToken.position;

		switch(currToken.token)
		{
			// TODO: save CONST to type. ali pa naredi posebej class AbsConstType
			case Token.CONST:
				dump("type -> const type");
				prepareNext();
				type = parse_type();
			break;

			case Token.IDENTIFIER:
				dump("type -> identifier");
				type = new AbsTypeName(new Position(position, currToken.position), currToken.lexeme);
				prepareNext();
			break;

			case Token.BOOLEAN:
				dump("type -> boolean");
				type = new AbsAtomType(new Position(position, currToken.position), AbsAtomType.BOOL);
				prepareNext();
			break;

			case Token.INTEGER:
				dump("type -> integer");
				type = new AbsAtomType(new Position(position, currToken.position), AbsAtomType.INT);
				prepareNext();
			break;

			case Token.FLOAT:
				dump("type -> float");
				type = new AbsAtomType(new Position(position, currToken.position), AbsAtomType.FLOAT);
				prepareNext();
			break;

			case Token.CHAR:
				dump("type -> char");
				type = new AbsAtomType(new Position(position, currToken.position), AbsAtomType.CHAR);
				prepareNext();
			break;

			case Token.VOID:
				dump("type -> void");
				type = new AbsAtomType(new Position(position, currToken.position), AbsAtomType.VOID);
				prepareNext();
			break;

			// case Token.STRUCT:
			// 	dump("type -> rec { components }");
			// 	prepareNext();
			// 	check(Token.LBRACE);
			// 	Vector<AbsComp> components = parse_components();
			// 	check(Token.RBRACE);
			// 	type = new AbsRecType(new Position(position, prevToken.position), components);
			// break;

			default:
				Report.error(currToken.position, "Not a type expression.");
			break;
		}

		return type;
	}

	// private Vector<AbsComp> parse_components()
	// {
	// 	dump("components -> component components'");
	// 	Vector<AbsComp> components = new Vector<AbsComp>();
	// 	components.add(parse_component());
	// 	parse_components_(components);

	// 	return components;
	// }

	// private void parse_components_(Vector<AbsComp> components)
	// {
	// 	readNext(true);

	// 	if(currToken.token != Token.COMMA)
	// 	{
	// 		dump("components' -> E");
	// 		return;
	// 	}

	// 	dump("components' -> , component components'");
	// 	prepareNext();
	// 	components.add(parse_component());
	// 	parse_components_(components);
	// }

	// private AbsComp parse_component()
	// {
	// 	dump("component -> identifier : type");
	// 	check(Token.IDENTIFIER);
	// 	Position position = prevToken.position;
	// 	String name = prevToken.lexeme;
	// 	// check(Token.COLON);
	// 	AbsType type = parse_type();

	// 	return new AbsComp(new Position(position, prevToken.position), name, type);
	// }

	private Vector<AbsPar> parse_parameters()
	{
		Vector<AbsPar> parameters = new Vector<AbsPar>();
		readNext();

		if(currToken.token == Token.RPARENT)
		{
			dump("parameters -> E");
			return parameters;
		}

		dump("parameters -> parameter parameters'");
		parameters.add(parse_parameter());
		parse_parameters_(parameters);

		return parameters;
	}

	private void parse_parameters_(Vector<AbsPar> parameters)
	{
		readNext(true);

		if(currToken.token != Token.COMMA)
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
		dump("parameter -> type identifier");
		AbsType type = parse_type();
		Position position = prevToken.position;
		check(Token.IDENTIFIER);
		String name = prevToken.lexeme;

		return new AbsPar(new Position(position, prevToken.position), name, type);
	}

	private Vector<AbsTree> parse_compound_statement()
	{
		check(Token.LBRACE);
		Vector<AbsTree> statement = null;
		readNext();

		if(currToken.token == Token.RBRACE)
		{
			dump("compound_statement -> { }");
			statement = new Vector<AbsTree>();
			prepareNext();
		}
		else
		{
			dump("compound_statement -> { block_items }");
			statement = parse_block_items();
			check(Token.RBRACE);
		}

		return statement;
	}

	private Vector<AbsTree> parse_block_items()
	{
		Vector<AbsTree> block_items = new Vector<AbsTree>();
		readNext();

		if(currToken.token == Token.RBRACE)
		{
			dump("block_items -> E");
			return block_items;
		}

		dump("block_items -> block_item block_items'");
		block_items.add(parse_block_item());
		parse_block_items_(block_items);

		return block_items;
	}

	private void parse_block_items_(Vector<AbsTree> block_items)
	{
		readNext(true);

		if(currToken.token == Token.RBRACE)
		{
			dump("block_items' -> E");
			return;
		}

		dump("block_items' -> block_item block_items'");
		block_items.add(parse_block_item());
		parse_block_items_(block_items);
	}

	private AbsTree parse_block_item()
	{
		readNext();
		AbsTree block_item = null;

		if(currToken.token == Token.INTEGER || currToken.token == Token.CHAR || currToken.token == Token.FLOAT || currToken.token == Token.BOOLEAN || currToken.token == Token.VOID || currToken.token == Token.CONST)
		{
			dump("block_item -> variable_definition");
			AbsType type = parse_type();
			check(Token.IDENTIFIER);
			String name = prevToken.lexeme;
			block_item = parse_variable_definition(new Position(0, 0), type, name);
		}
		else
		{
			dump("block_item -> statement");
			block_item = parse_statement();
		}

		return block_item;
	}

	private AbsExpr parse_statement()
	{
		readNext();
		AbsExpr statement = null;
		Position position = currToken.position;

		if(currToken.token == Token.IF)
		{
			dump("statement -> selection_statement");
			dump("selection_statement -> if(expression) statement selection_statement'");
			prepareNext();
			check(Token.LPARENT);
			AbsExpr ifExpression = parse_expression();
			check(Token.RPARENT);
			AbsExpr thenStatement = parse_statement();
			readNext();

			if(currToken.token == Token.ELSE)
			{
				dump("selection_statement' -> else statement");
				prepareNext();
				readNext();
				statement = new AbsIfElse(new Position(position, currToken.position), ifExpression, thenStatement, parse_statement());
			}
			else
			{
				dump("selection_statement' -> E");
				statement = new AbsIf(new Position(position, currToken.position), ifExpression, thenStatement);
			}
		}
		else if(currToken.token == Token.WHILE)
		{
			dump("statement -> iteration_statement");
			dump("iteration_statement -> while(expression) statement");
			prepareNext();
			check(Token.LPARENT);
			AbsExpr whileExpression = parse_expression();
			check(Token.RPARENT);
			AbsExpr doStatement = parse_statement();
			statement = new AbsWhile(new Position(position, prevToken.position), whileExpression, doStatement);
		}
		else if(currToken.token == Token.DO)
		{
			dump("statement -> iteration_statement");
			dump("iteration_statement -> do statement while(expression);");
			prepareNext();
			AbsExpr doStatement = parse_statement();
			check(Token.WHILE);
			check(Token.LPARENT);
			AbsExpr whileExpression = parse_expression();
			check(Token.RPARENT);
			check(Token.SEMICOLON);
			statement = new AbsDoWhile(new Position(position, prevToken.position), doStatement, whileExpression);
		}
		else if(currToken.token == Token.FOR)
		{
			dump("statement -> iteration_statement");
			prepareNext();
			check(Token.LPARENT);
			readNext();

			AbsTree init = null;

			if(currToken.token == Token.INTEGER || currToken.token == Token.CHAR || currToken.token == Token.FLOAT || currToken.token == Token.BOOLEAN || currToken.token == Token.VOID || currToken.token == Token.IDENTIFIER)
			{
				dump("iteration_statement -> for(variable_definition expression_statement iteration_statement') statement");
				AbsType type = parse_type();
				check(Token.IDENTIFIER);
				String name = prevToken.lexeme;
				init = parse_variable_definition(new Position(0, 0), type, name);
			}
			else if(currToken.token == Token.CONST)
			{
				// error?
				Report.error("TODO: CONST FOR");
			}
			else
			{
				dump("iteration_statement -> for(expression_statement expression_statement iteration_statement') statement");
				init = parse_expression_statement();
			}

			AbsExpr cond = parse_expression_statement();
			AbsExpr step = null;
			readNext();

			if(currToken.token == Token.RPARENT)
			{
				dump("iteration_statement' -> E");
				step = new AbsNop(new Position(0, 0));
			}
			else
			{
				dump("iteration_statement' -> expression");
				step = parse_expression();
			}

			check(Token.RPARENT);
			AbsExpr body = parse_statement();
			statement = new AbsFor(new Position(position, prevToken.position), init, cond, step, body);
		}
		else if(currToken.token == Token.RETURN)
		{
			// TODO: samo skok na label?
			dump("statement -> jump_statement");
			prepareNext();
			readNext();

			if(currToken.token == Token.SEMICOLON)
			{
				dump("jump_statement -> return;");
				prepareNext();
				statement = new AbsReturn(new Position(0, 0), new AbsNop(new Position(0, 0)));
			}
			else
			{
				dump("jump_statement -> return expression;");
				AbsExpr expression = parse_expression();
				check(Token.SEMICOLON);
				statement = new AbsReturn(new Position(0, 0), expression);
			}
		}
		else if(currToken.token == Token.BREAK || currToken.token == Token.CONTINUE)
		{
			// TODO: samo skok na label?
			dump("statement -> jump_statement");
			prepareNext();
			check(Token.SEMICOLON);
			// TODO: nov (skupni?) razred
			statement = new AbsNop(new Position(0, 0));
		}
		else if(currToken.token == Token.LBRACE)
		{
			dump("statement -> compound_statement");
			statement = new AbsExprs(new Position(0, 0), parse_compound_statement());
		}
		else
		{
			dump("statement -> expression_statement");
			statement = parse_expression_statement();
		}

		return statement;
	}

	private AbsExpr parse_expression_statement()
	{
		AbsExpr expression = null;
		readNext();

		if(currToken.token == Token.SEMICOLON)
		{
			prepareNext();
			dump("expression_statement -> ;");
			return new AbsNop(new Position(0, 0));
		}

		dump("expression_statement -> expression;");
		expression = parse_expression();
		check(Token.SEMICOLON);

		return expression;
	}

	private AbsExpr parse_expression()
	{
		dump("expression -> logical_or_expression expression'");
		return parse_expression_(parse_logical_or_expression());
	}

	private AbsExpr parse_expression_(AbsExpr left)
	{
		readNext(true);

		if(currToken.token == Token.ASSIGN)
		{
			dump("expression' -> = expression");
			prepareNext();
			AbsExpr right = parse_expression();
			left = new AbsBinExpr(new Position(0, 0), AbsBinExpr.ASSIGN, left, right);
		}
		else
		{
			dump("expression' -> E");
		}

		return left;
	}

	private AbsExpr parse_logical_or_expression()
	{
		dump("logical_or_expression -> logical_and_expression logical_or_expression'");
		return parse_logical_or_expression_(parse_logical_and_expression());
	}

	private AbsExpr parse_logical_or_expression_(AbsExpr left)
	{
		readNext(true);

		if(currToken.token == Token.OR)
		{
			dump("logical_or_expression' -> || logical_and_expression logical_or_expression'");
			prepareNext();
			AbsExpr right = parse_logical_and_expression();
			left = parse_logical_or_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.OR, left, right));
		}
		else
		{
			dump("logical_or_expression' -> E");
		}

		return left;
	}

	private AbsExpr parse_logical_and_expression()
	{
		dump("logical_and_expression -> bitwise_or_expression logical_and_expression'");
		return parse_logical_and_expression_(parse_bitwise_or_expression());
	}

	private AbsExpr parse_logical_and_expression_(AbsExpr left)
	{
		readNext(true);

		if(currToken.token == Token.AND)
		{
			dump("logical_and_expression' -> && bitwise_or_expression logical_and_expression'");
			prepareNext();
			AbsExpr right = parse_bitwise_or_expression();
			left = parse_logical_and_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.AND, left, right));
		}
		else
		{
			dump("logical_and_expression' -> E");
		}

		return left;
	}
	
	private AbsExpr parse_bitwise_or_expression()
	{
		dump("bitwise_or_expression -> bitwise_xor_expression bitwise_or_expression'");
		return parse_bitwise_or_expression_(parse_bitwise_xor_expression());
	}

	private AbsExpr parse_bitwise_or_expression_(AbsExpr left)
	{
		readNext(true);

		if(currToken.token == Token.BIT_OR)
		{
			dump("bitwise_or_expression' -> | bitwise_xor_expression bitwise_or_expression'");
			prepareNext();
			AbsExpr right = parse_bitwise_xor_expression();
			left = parse_bitwise_or_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.BIT_OR, left, right));
		}
		else
		{
			dump("bitwise_or_expression' -> E");
		}

		return left;
	}

	// TODO: Ali naj odstranim position? Bo veliko lazje.
	private AbsExpr parse_bitwise_xor_expression()
	{
		dump("bitwise_xor_expression -> bitwise_and_expression bitwise_xor_expression'");
		return parse_bitwise_xor_expression_(parse_bitwise_and_expression());
	}

	private AbsExpr parse_bitwise_xor_expression_(AbsExpr left)
	{
		readNext(true);

		if(currToken.token == Token.BIT_XOR)
		{
			dump("bitwise_xor_expression' -> ^ bitwise_and_expression bitwise_xor_expression'");
			prepareNext();
			AbsExpr right = parse_bitwise_and_expression();
			left = parse_bitwise_xor_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.BIT_XOR, left, right));
		}
		else
		{
			dump("bitwise_xor_expression' -> E");
		}

		return left;
	}

	private AbsExpr parse_bitwise_and_expression()
	{
		dump("bitwise_and_expression -> equality_expression bitwise_and_expression'");
		return parse_bitwise_and_expression_(parse_equality_expression());
	}

	private AbsExpr parse_bitwise_and_expression_(AbsExpr left)
	{
		readNext(true);

		if(currToken.token == Token.BIT_AND)
		{
			dump("bitwise_and_expression' -> & equality_expression bitwise_and_expression'");
			prepareNext();
			AbsExpr right = parse_equality_expression();
			left = parse_bitwise_and_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.BIT_AND, left, right));
		}
		else
		{
			dump("bitwise_and_expression' -> E");
		}

		return left;
	}

	private AbsExpr parse_equality_expression()
	{
		dump("equality_expression -> relational_expression equality_expression'");
		return parse_equality_expression_(parse_relational_expression());
	}

	private AbsExpr parse_equality_expression_(AbsExpr left)
	{
		readNext(true);

		AbsExpr right = null;
		int operation = 0;

		if(currToken.token == Token.EQU)
		{
			dump("equality_expression' -> == relational_expression");
			operation = AbsBinExpr.EQU;
			prepareNext();
			right = parse_relational_expression();
		}
		else if(currToken.token == Token.NEQ)
		{
			dump("equality_expression' -> != relational_expression");
			operation = AbsBinExpr.NEQ;
			prepareNext();
			right = parse_relational_expression();
		}
		else
		{
			dump("equality_expression' -> E");
		}

		if(right != null)
		{
			left = new AbsBinExpr(new Position(left.position, right.position), operation, left, right);
		}

		return left;
	}

	private AbsExpr parse_relational_expression()
	{
		dump("relational_expression -> shift_expression relational_expression'");
		return parse_relational_expression_(parse_shift_expression());
	}

	private AbsExpr parse_relational_expression_(AbsExpr left)
	{
		readNext(true);

		AbsExpr right = null;
		int operation = 0;

		if(currToken.token == Token.LTH)
		{
			dump("relational_expression' -> < shift_expression");
			operation = AbsBinExpr.LTH;
			prepareNext();
			right = parse_shift_expression();
		}
		else if(currToken.token == Token.GTH)
		{
			dump("relational_expression' -> > shift_expression");
			operation = AbsBinExpr.GTH;
			prepareNext();
			right = parse_shift_expression();
		}
		else if(currToken.token == Token.LEQ)
		{
			dump("relational_expression' -> >= shift_expression");
			operation = AbsBinExpr.LEQ;
			prepareNext();
			right = parse_shift_expression();
		}
		else if(currToken.token == Token.GEQ)
		{
			dump("relational_expression' -> >= shift_expression");
			operation = AbsBinExpr.GEQ;
			prepareNext();
			right = parse_shift_expression();
		}
		else
		{
			dump("relational_expression' -> E");
		}

		if(right != null)
		{
			left = new AbsBinExpr(new Position(left.position, right.position), operation, left, right);
		}

		return left;
	}

	private AbsExpr parse_shift_expression()
	{
		dump("shift_expression -> additive_expression shift_expression'");
		return parse_shift_expression_(parse_additive_expression());
	}

	private AbsExpr parse_shift_expression_(AbsExpr left)
	{
		readNext(true);

		AbsExpr right = null;
		int operation = 0;

		if(currToken.token == Token.LSHIFT)
		{
			dump("shift_expression' -> << additive_expression");
			operation = AbsBinExpr.LSHIFT;
			prepareNext();
			right = parse_additive_expression();
		}
		else if(currToken.token == Token.RSHIFT)
		{
			dump("shift_expression' -> >> additive_expression");
			operation = AbsBinExpr.RSHIFT;
			prepareNext();
			right = parse_additive_expression();
		}
		else
		{
			dump("shift_expression' -> E");
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

		if(currToken.token == Token.ADD)
		{
			dump("additive_expression' -> + multiplicative_expression additive_expression'");
			prepareNext();
			AbsExpr right = parse_multiplicative_expression();
			left = parse_additive_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.ADD, left, right));
		}
		else if(currToken.token == Token.SUB)
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
		dump("multiplicative_expression -> unary_expression multiplicative_expression'");
		return parse_multiplicative_expression_(parse_unary_expression());
	}

	private AbsExpr parse_multiplicative_expression_(AbsExpr left)
	{
		readNext(true);

		if(currToken.token == Token.MUL)
		{
			dump("multiplicative_expression' -> * unary_expression multiplicative_expression'");
			prepareNext();
			AbsExpr right = parse_unary_expression();
			left = parse_multiplicative_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.MUL, left, right));
		}
		else if(currToken.token == Token.DIV)
		{
			dump("multiplicative_expression' -> / unary_expression multiplicative_expression'");
			prepareNext();
			AbsExpr right = parse_unary_expression();
			left = parse_multiplicative_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.DIV, left, right));
		}
		else if(currToken.token == Token.MOD)
		{
			dump("multiplicative_expression' -> % unary_expression multiplicative_expression'");
			prepareNext();
			AbsExpr right = parse_unary_expression();
			left = parse_multiplicative_expression_(new AbsBinExpr(new Position(left.position, right.position), AbsBinExpr.MOD, left, right));
		}
		else
		{
			dump("multiplicative_expression' -> E");
		}

		return left;
	}

	private AbsExpr parse_unary_expression()
	{
		readNext();

		AbsExpr expression = null;
		Position position = currToken.position;

		if(currToken.token == Token.ADD)
		{
			dump("unary_expression -> +unary_expression");
			prepareNext();
			AbsExpr expr = parse_unary_expression();
			expression = new AbsUnExpr(new Position(position, prevToken.position), AbsUnExpr.ADD, expr);
		}
		else if(currToken.token == Token.SUB)
		{
			dump("unary_expression -> -unary_expression");
			prepareNext();
			AbsExpr expr = parse_unary_expression();
			expression = new AbsUnExpr(new Position(position, prevToken.position), AbsUnExpr.SUB, expr);
		}
		else if(currToken.token == Token.NOT)
		{
			dump("unary_expression -> !unary_expression");
			prepareNext();
			AbsExpr expr = parse_unary_expression();
			expression = new AbsUnExpr(new Position(position, prevToken.position), AbsUnExpr.NOT, expr);
		}
		else if(currToken.token == Token.BIT_NOT)
		{
			dump("unary_expression -> ~unary_expression");
			prepareNext();
			AbsExpr expr = parse_unary_expression();
			expression = new AbsUnExpr(new Position(position, prevToken.position), AbsUnExpr.BIT_NOT, expr);
		}
		// TODO: Zamenjaj z binop? ne bo ok, ker moramo tudi shraniti v spremenljivko
		else if(currToken.token == Token.INC)
		{
			dump("unary_expression -> ++unary_expression");
			prepareNext();
			AbsExpr expr = parse_unary_expression();
			expression = new AbsUnExpr(new Position(position, prevToken.position), AbsUnExpr.INC, expr);
		}
		else if(currToken.token == Token.DEC)
		{
			dump("unary_expression -> --unary_expression");
			prepareNext();
			AbsExpr expr = parse_unary_expression();
			expression = new AbsUnExpr(new Position(position, prevToken.position), AbsUnExpr.DEC, expr);
		}
		else
		{
			dump("unary_expression -> postfix_expression");
			expression = parse_postfix_expression();
		}

		return expression;
	}

	private AbsExpr parse_postfix_expression()
	{
		dump("postfix_expression -> primary_expression postfix_expression'");
		return parse_postfix_expression_(currToken.position, parse_primary_expression());
	}

	private AbsExpr parse_postfix_expression_(Position position, AbsExpr left)
	{
		readNext(true);

		AbsExpr expression = null;

		if(currToken.token == Token.DOT)
		{
			dump("postfix_expression' -> .identifier postfix_expression'");
			prepareNext();
			check(Token.IDENTIFIER);
			expression = parse_postfix_expression_(position, new AbsBinExpr(new Position(position, prevToken.position), AbsBinExpr.DOT, left, new AbsCompName(prevToken.position, prevToken.lexeme)));
		}
		else if(currToken.token == Token.LBRACKET)
		{
			dump("postfix_expression' -> [expression] postfix_expression'");
			prepareNext();
			AbsExpr array = parse_expression();
			check(Token.RBRACKET);
			expression = parse_postfix_expression_(position, new AbsBinExpr(new Position(position, prevToken.position), AbsBinExpr.ARR, left, array));
		}
		// kaj narediti z Inc in dec? je unary al binop? kako izvesti?
		else
		{
			dump("postfix_expression' -> E");
			expression = left;
		}

		return expression;
	}

	private AbsExpr parse_primary_expression()
	{
		readNext();

		AbsExpr expression = null;
		Position position = currToken.position;

		if(currToken.token == Token.BOOL_CONST)
		{
			dump("primary_expression -> boolean_constant");
			expression = new AbsAtomConst(currToken.position, AbsAtomConst.BOOL, currToken.lexeme);
			prepareNext();
		}
		else if(currToken.token == Token.INT_CONST)
		{
			dump("primary_expression -> int_constant");
			expression = new AbsAtomConst(currToken.position, AbsAtomConst.INT, currToken.lexeme);
			prepareNext();
		}
		else if(currToken.token == Token.FLOAT_CONST)
		{
			dump("primary_expression -> float_constant");
			expression = new AbsAtomConst(currToken.position, AbsAtomConst.FLOAT, currToken.lexeme);
			prepareNext();
		}
		else if(currToken.token == Token.CHAR_CONST)
		{
			dump("primary_expression -> char_constant");
			expression = new AbsAtomConst(currToken.position, AbsAtomConst.CHAR, currToken.lexeme);
			prepareNext();
		}
		else if(currToken.token == Token.STR_CONST)
		{
			dump("primary_expression -> string_constant");
			expression = new AbsAtomConst(currToken.position, AbsAtomConst.STR, currToken.lexeme);
			prepareNext();
		}
		else if(currToken.token == Token.IDENTIFIER)
		{
			prepareNext();
			readNext();

			if(currToken.token == Token.LPARENT)
			{
				dump("primary_expression -> identifier(arguments)");
				String name = prevToken.lexeme;
				prepareNext();
				Vector<AbsExpr> arguments = parse_arguments();
				check(Token.RPARENT);
				expression = new AbsFunCall(new Position(position, prevToken.position), name, arguments);
			}
			else
			{
				dump("primary_expression -> identifier");
				expression = new AbsVarName(prevToken.position, prevToken.lexeme);
			}
		}
		else if(currToken.token == Token.LPARENT)
		{
			// TODO: Prednost. block_items ne bo prov.
			// dump("atom_expression -> ( expressions )");
			// prepareNext();
			// Vector<AbsTree> expressions = parse_expressions();
			// check(Token.RPARENT);
			// expression = new AbsExprs(new Position(position, prevToken.position), expressions);
		}
		else
		{
			Report.error(currToken.position, "Not a primary expression.");
		}

		return expression;
	}

	private Vector<AbsExpr> parse_arguments()
	{
		Vector<AbsExpr> arguments = new Vector<AbsExpr>();
		readNext();

		if(currToken.token == Token.RPARENT)
		{
			dump("arguments -> E");
			return arguments;
		}

		// expression ali argument?
		dump("arguments -> expression arguments'");
		arguments.add(parse_expression());
		parse_arguments_(arguments);

		return arguments;
	}

	private void parse_arguments_(Vector<AbsExpr> arguments)
	{
		readNext(true);

		if(currToken.token == Token.RPARENT)
		{
			dump("arguments' -> E");
			return;
		}

		check(Token.COMMA);
		dump("arguments' -> , expression arguments'");
		arguments.add(parse_expression());
		parse_arguments_(arguments);
	}

	private void dump(String production)
	{
		if(dump == false)
		{
			return;
		}
		if(Report.dumpFile() == null)
		{
			return;
		}

		Report.dumpFile().println(production);
	}
}
