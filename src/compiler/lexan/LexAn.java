package compiler.lexan;

import java.io.*;

import compiler.*;

public class LexAn {
	private final boolean dump;

	private FileReader input;
	private int currChar;
	private String lexeme;
	private int startLine, currLine;
	private int startColumn, currColumn;

	public LexAn(String sourceFileName, boolean dump) {
		this.dump = dump;

		try {
			input = new FileReader(sourceFileName);
		} catch (FileNotFoundException e) {
			Report.error("File " + sourceFileName + " could not be found.");
		}

		readNext(false);
		currLine = 1;
		currColumn = 1;
	}

	private void readNext(boolean append) {
		try {
			if (append) {
				lexeme += (char) currChar;
			}

			if (currChar == '\n') {
				currLine++;
				currColumn = 0;
			}

			currChar = input.read();
			currColumn++;
		} catch (IOException e) {
			Report.error(currLine, currColumn, "An error occurred while reading the file.");
		}
	}

	public Symbol lexAn() {
		int token = -1;
		lexeme = "";

		while (currChar == ' ' || currChar == '\t' || currChar == '\n' || currChar == '\r') {
			readNext(false);
		}

		while (token == -1) {
			final int temp = currChar;

			startLine = currLine;
			startColumn = currColumn;

			switch (currChar) {
				case '+': case '-': case '*': case '/':
				case '%': case '&': case '|': case '^':
				case '!': case '=': case '<': case '>':
				case '(': case ')': case '[': case ']':
				case '{': case '}': case ':': case ';':
				case '.': case ',':
					readNext(true);
				break;
			}

			switch (temp) {
				case -1: token = Token.EOF; break;

				case '+': token = Token.ADD; break;
				case '-': token = Token.SUB; break;
				case '*': token = Token.MUL; break;
				case '/': token = Token.DIV; break;
				case '%': token = Token.MOD; break;
				case '&': token = Token.AND; break;
				case '|': token = Token.IOR; break;
				case '^': token = Token.PTR; break;

				case '!':
					if (currChar == '=') {
						token = Token.NEQ;
						readNext(true);
					} else {
						token = Token.NOT;
					}
				break;

				case '=':
					if (currChar == '=') {
						token = Token.EQU;
						readNext(true);
					} else {
						token = Token.ASSIGN;
					}
				break;

				case '<':
					if (currChar == '=') {
						token = Token.LEQ;
						readNext(true);
					} else {
						token = Token.LTH;
					}
				break;

				case '>':
					if (currChar == '=') {
						token = Token.GEQ;
						readNext(true);
					} else {
						token = Token.GTH;
					}
				break;

				case '(': token = Token.LPARENT;  break;
				case ')': token = Token.RPARENT;  break;
				case '[': token = Token.LBRACKET; break;
				case ']': token = Token.RBRACKET; break;
				case '{': token = Token.LBRACE;   break;
				case '}': token = Token.RBRACE;   break;
				case ':': token = Token.COLON;    break;
				case ';': token = Token.SEMIC;    break;
				case '.': token = Token.DOT;      break;
				case ',': token = Token.COMMA;    break;

				case '\'':
					while (true) {
						readNext(true);

						if (currChar == '\'') {
							readNext(true);

							if (currChar != '\'') {
								token = Token.STR_CONST;
								break;
							}
						} else if (currChar == -1) {
							lexeme = "";
							token = Token.EOF;
							break;
						} else if (currChar < 32 || currChar > 126) {
							Report.error(currLine, currColumn, "Unexpected character.");
						}
					}
				break;

				case '#':
					while (currChar != '\n' && currChar != -1) {
						readNext(false);
					}

					readNext(false);
				break;

				case '\r': case '\n': case '\t': case ' ':
					readNext(false);
				break;

				default:
					if (currChar >= '0' && currChar <= '9') {
						while (currChar >= '0' && currChar <= '9') {
							readNext(true);
						}

						token = Token.INT_CONST;
					} else if ((currChar >= 'A' && currChar <= 'Z') ||
							   (currChar >= 'a' && currChar <= 'z') ||
							    currChar == '_') {
						while ((currChar >= 'A' && currChar <= 'Z') ||
							   (currChar >= 'a' && currChar <= 'z') ||
							   (currChar >= '0' && currChar <= '9') ||
							    currChar == '_') {
							readNext(true);
						}

						switch (lexeme) {
							case "true": case "false": token = Token.LOG_CONST; break;

							case "logical": token = Token.LOGICAL; break;
							case "integer": token = Token.INTEGER; break;
							case "string":  token = Token.STRING;  break;

							case "arr":   token = Token.KW_ARR;   break;
							case "else":  token = Token.KW_ELSE;  break;
							case "for":   token = Token.KW_FOR;   break;
							case "fun":   token = Token.KW_FUN;   break;
							case "if":    token = Token.KW_IF;    break;
							case "rec":   token = Token.KW_REC;   break;
							case "then":  token = Token.KW_THEN;  break;
							case "typ":   token = Token.KW_TYP;   break;
							case "var":   token = Token.KW_VAR;   break;
							case "where": token = Token.KW_WHERE; break;
							case "while": token = Token.KW_WHILE; break;

							default: token = Token.IDENTIFIER; break;
						}
					} else {
						Report.warning(currLine, currColumn, "Unexpected character.");
						token = Token.EOF;
					}
				break;
			}
		}

		final Symbol symbol = new Symbol(token, lexeme, startLine, startColumn, currLine, currColumn == 1 ? currColumn : currColumn - 1);
		dump(symbol);

		return symbol;
	}

	private void dump(Symbol symb) {
		if (!dump || Report.dumpFile() == null) {
			return;
		}

		if (symb.token == Token.EOF) {
			Report.dumpFile().println(symb);
		} else {
			Report.dumpFile().println("[" + symb.position.toString() + "] " + symb);
		}
	}
}
