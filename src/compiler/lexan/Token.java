package compiler.lexan;

public class Token {
	public static final int EOF 		= 0;

	public static final int IDENTIFIER 	= 1;

	public static final int LOG_CONST 	= 2;
	public static final int INT_CONST 	= 3;
	public static final int STR_CONST 	= 4;

	public static final int AND 		= 5;
	public static final int IOR 		= 6;
	public static final int NOT 		= 7;

	public static final int EQU 		= 8;
	public static final int NEQ 		= 9;
	public static final int LTH 		= 10;
	public static final int GTH 		= 11;
	public static final int LEQ 		= 12;
	public static final int GEQ 		= 13;

	public static final int MUL 		= 14;
	public static final int DIV 		= 15;
	public static final int MOD 		= 16;
	public static final int ADD 		= 17;
	public static final int SUB 		= 18;

	public static final int PTR 		= 19;

	public static final int LPARENT 	= 20;
	public static final int RPARENT 	= 21;
	public static final int LBRACKET 	= 22;
	public static final int RBRACKET 	= 23;
	public static final int LBRACE 		= 24;
	public static final int RBRACE 		= 25;

	public static final int DOT 		= 26;
	public static final int COLON 		= 27;
	public static final int SEMIC 		= 28;
	public static final int COMMA 		= 29;

	public static final int ASSIGN 		= 30;

	public static final int LOGICAL 	= 31;
	public static final int INTEGER 	= 32;
	public static final int STRING 		= 33;

	public static final int KW_ARR 		= 34;
	public static final int KW_ELSE 	= 35;
	public static final int KW_FOR 		= 36;
	public static final int KW_FUN 		= 37;
	public static final int KW_IF 		= 38;
	public static final int KW_REC 		= 39;
	public static final int KW_THEN 	= 40;
	public static final int KW_TYP 		= 41;
	public static final int KW_VAR 		= 42;
	public static final int KW_WHERE 	= 43;
	public static final int KW_WHILE 	= 44;
}
