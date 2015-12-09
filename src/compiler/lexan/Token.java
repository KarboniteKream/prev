package compiler.lexan;

// TODO: Importaj samo Token in Position?
import compiler.*;

public class Token
{
	public static final int EOF = 0;

	public static final int IDENTIFIER = 1;

	public static final int BOOL_CONST = 2;
	public static final int INT_CONST = 3;
	public static final int CHAR_CONST = 105;
	public static final int STR_CONST = 4;
	public static final int FLOAT_CONST = 104;

	public static final int AND = 5;
	public static final int OR = 6;
	public static final int NOT = 7;

	public static final int BIT_AND = 100;
	public static final int BIT_OR = 101;
	public static final int BIT_XOR = 102;
	public static final int BIT_NOT = 111;
	// TODO: BIT_NOT (dodaj v dipl, ce podpiramo)

	public static final int EQU = 8;
	public static final int NEQ = 9;
	public static final int LTH = 10;
	public static final int GTH = 11;
	public static final int LEQ = 12;
	public static final int GEQ = 13;

	public static final int ADD = 17;
	public static final int SUB = 18;
	public static final int MUL = 14;
	public static final int DIV = 15;
	public static final int MOD = 16;

	public static final int INC = 107;
	public static final int DEC = 108;

	public static final int LSHIFT = 109;
	public static final int RSHIFT = 110;

	// TODO!
	// public static final int PTR = 19;

	public static final int LPARENT = 20;
	public static final int RPARENT = 21;
	public static final int LBRACKET = 22;
	public static final int RBRACKET = 23;
	public static final int LBRACE = 24;
	public static final int RBRACE = 25;

	public static final int DOT = 26;
	public static final int COMMA = 29;
	public static final int SEMICOLON = 28;
	// dvopicja rabimo za tipe v prev. potrebujemo se vprasaj?
	// public static final int COLON = 27;

	public static final int ASSIGN = 30;

	public static final int BOOLEAN = 31;
	public static final int INTEGER = 32;
	public static final int CHAR = 103;
	public static final int FLOAT = 106;
	public static final int VOID = 33;

	public static final int BREAK = 34;
	public static final int CONST = 35;
	public static final int CONTINUE = 36;
	public static final int DO = 37;
	public static final int ELSE = 38;
	public static final int FOR = 39;
	public static final int IF = 40;
	public static final int RETURN = 41;
	// public static final int SIZEOF = 42;
	public static final int STRUCT = 43;
	public static final int TYPEDEF = 44;
	public static final int WHILE = 45;

	public final int token;
	/** Znakovna predstavitev simbola. */
	public final String lexeme;
	public final Position position;

	public Token(int token, String lexeme, int begLine, int begColumn, int endLine, int endColumn)
	{
		this.token = token;
		this.lexeme = lexeme;
		this.position = new Position(begLine, begColumn, endLine, endColumn);
	}

	public Token(int token, String lexeme, Position position)
	{
		this.token = token;
		this.lexeme = lexeme;
		this.position = position;
	}

	public String toString()
	{
		String name = "";

		switch(token)
		{
			// TODO: PREVERI VSE TOKENE
			case EOF: name = "EOF"; break;

			case IDENTIFIER: name = "IDENTIFIER"; break;

			case BOOL_CONST: name = "BOOL_CONST"; break;
			// int == short, odstrani iz dipl
			case INT_CONST: name = "INT_CONST"; break;
			case CHAR_CONST: name = "CHAR_CONST"; break;
			// TODO: char == int?
			// TODO: char[] == str
			case STR_CONST: name = "STR_CONST"; break;
			case FLOAT_CONST: name = "FLOAT_CONST"; break;

			case AND: name = "AND"; break;
			case OR: name = "OR"; break;
			case NOT: name = "NOT"; break;

			case BIT_AND: name = "BIT_AND"; break;
			case BIT_OR: name = "BIT_OR"; break;
			case BIT_XOR: name = "BIT_XOR"; break;
			case BIT_NOT: name = "BIT_NOT"; break;

			case EQU: name = "EQU"; break;
			case NEQ: name = "NEQ"; break;
			case LTH: name = "LTH"; break;
			case GTH: name = "GTH"; break;
			case LEQ: name = "LEQ"; break;
			case GEQ: name = "GEQ"; break;

			case ADD: name = "ADD"; break;
			case SUB: name = "SUB"; break;
			case MUL: name = "MUL"; break;
			case DIV: name = "DIV"; break;
			case MOD: name = "MOD"; break;

			case INC: name = "INC"; break;
			case DEC: name = "DEC"; break;

			case LSHIFT: name = "LSHIFT"; break;
			case RSHIFT: name = "RSHIFT"; break;

			// case PTR: name = "PTR"; break;

			case LPARENT: name = "LPARENT"; break;
			case RPARENT: name = "RPARENT"; break;
			case LBRACKET: name = "LBRACKET"; break;
			case RBRACKET: name = "RBRACKET"; break;
			case LBRACE: name = "LBRACE"; break;
			case RBRACE: name = "RBRACE"; break;

			case DOT: name = "DOT"; break;
			// case COLON: name = "COLON"; break;
			case SEMICOLON: name = "SEMICOLON"; break;
			case COMMA: name = "COMMA"; break;

			case ASSIGN: name = "ASSIGN"; break;

			case BOOLEAN: name = "BOOLEAN"; break;
			case INTEGER: name = "INTEGER"; break;
			// char
			// float/double
			// case STRING: name = "STRING"; break;
			case CHAR: name = "CHAR"; break;
			case FLOAT: name = "FLOAT"; break;
			case VOID: name = "VOID"; break;

			case BREAK: name = "BREAK"; break;
			case CONST: name = "CONST"; break;
			case CONTINUE: name = "CONTINUE"; break;
			case DO: name = "DO"; break;
			case ELSE: name = "ELSE"; break;
			case FOR: name = "FOR"; break;
			case IF: name = "IF"; break;
			case RETURN: name = "RETURN"; break;
			// case SIZEOF: name = "SIZEOF"; break;
			case STRUCT: name = "STRUCT"; break;
			case TYPEDEF: name = "TYPEDEF"; break;
			case WHILE: name = "WHILE"; break;

			// TODO: Remove internal errors?
			default:
				Report.error("Internal error: token=" + token + " in compiler.lexan.Symbol.toString().");
			break;
		}

		if(token == IDENTIFIER || token == BOOL_CONST || token == INT_CONST || token == CHAR_CONST || token == STR_CONST || token == FLOAT_CONST)
		{
			return name + "(" + lexeme + ")";
		}
		else
		{
			return name;
		}
	}
}
