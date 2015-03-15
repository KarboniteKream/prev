package compiler.lexan;

/**
 * Definicije vrst besed.
 * 
 * @author sliva
 */
public class Token {

	/** Vrsta simbola: konec datoteke. */							public static final int EOF 		= 0;
	
	/** Vrsta simbola: ime. */										public static final int IDENTIFIER 	= 1;

	/** Vrsta simbola: logicna konstanta. */						public static final int LOG_CONST 	= 2;
	/** Vrsta simbola: celo stevilo. */								public static final int INT_CONST 	= 3;
	/** Vrsta simbola: niz. */										public static final int STR_CONST 	= 4;

	/** Vrsta simbola: logicni in. */								public static final int AND 		= 5;
	/** Vrsta simbola: logicni ali. */								public static final int IOR 		= 6;
	/** Vrsta simbola: logicni ne. */								public static final int NOT 		= 7;
	
	/** Vrsta simbola: je-enako. */									public static final int EQU 		= 8;
	/** Vrsta simbola: ni-enako. */									public static final int NEQ 		= 9;
	/** Vrsta simbola: manjse-kot. */								public static final int LTH 		= 10;
	/** Vrsta simbola: vecje-kot. */								public static final int GTH 		= 11;
	/** Vrsta simbola: manjse-ali-enako. */							public static final int LEQ 		= 12;
	/** Vrsta simbola: vecje-ali-enako. */							public static final int GEQ 		= 13;
	
	/** Vrsta simbola: celostevilsko mnozenje. */					public static final int MUL 		= 14;
	/** Vrsta simbola: celostevilsko deljenje. */					public static final int DIV 		= 15;
	/** Vrsta simbola: ostanek po celostevilskem deljenju. */		public static final int MOD 		= 16;
	/** Vrsta simbola: celostevilsko sestevanje ali predznak. */	public static final int ADD 		= 17;
	/** Vrsta simbola: celostevilsko odstevanje ali predznak. */	public static final int SUB 		= 18;
	
	/** Vrsta simbola: kazalec. */									public static final int PTR 		= 19;
	
	/** Vrsta simbola: levi oklepaj. */								public static final int LPARENT 	= 20;
	/** Vrsta simbola: desni oklepaj. */							public static final int RPARENT 	= 21;
	/** Vrsta simbola: levi oglati oklepaj. */						public static final int LBRACKET 	= 22;
	/** Vrsta simbola: desni oglati oklepaj. */						public static final int RBRACKET 	= 23;
	/** Vrsta simbola: levi zaviti oklepaj. */						public static final int LBRACE 		= 24;
	/** Vrsta simbola: desni zaviti oklepaj. */						public static final int RBRACE 		= 25;
	
	/** Vrsta simbola: pika. */										public static final int DOT 		= 26;
	/** Vrsta simbola: dvopicje. */									public static final int COLON 		= 27;
	/** Vrsta simbola: podpicje. */									public static final int SEMIC 		= 28;
	/** Vrsta simbola: vejica. */                                   public static final int COMMA 		= 29;
	
	/** Vrsta simbola: prirejanje. */								public static final int ASSIGN 		= 30;
	
	/** Vrsta simbola: tip logical.  */								public static final int LOGICAL 	= 31;
	/** Vrsta simbola: tip integer.  */								public static final int INTEGER 	= 32;
	/** Vrsta simbola: tip string.  */								public static final int STRING 		= 33;
	
	/** Vrsta simbola: kljucna beseda arr.  */						public static final int KW_ARR 		= 34;
	/** Vrsta simbola: kljucna beseda else.  */						public static final int KW_ELSE 	= 35;
	/** Vrsta simbola: kljucna beseda for.  */						public static final int KW_FOR 		= 36;
	/** Vrsta simbola: kljucna beseda fun.  */						public static final int KW_FUN 		= 37;
	/** Vrsta simbola: kljucna beseda if.  */						public static final int KW_IF 		= 38;
	/** Vrsta simbola: kljucna beseda rec.  */						public static final int KW_REC 		= 39;
	/** Vrsta simbola: kljucna beseda then.  */						public static final int KW_THEN 	= 40;
	/** Vrsta simbola: kljucna beseda typ.  */						public static final int KW_TYP 		= 41;
	/** Vrsta simbola: kljucna beseda var.  */						public static final int KW_VAR 		= 42;
	/** Vrsta simbola: kljucna beseda where.  */					public static final int KW_WHERE 	= 43;
	/** Vrsta simbola: kljucna beseda while.  */					public static final int KW_WHILE 	= 44;

}
