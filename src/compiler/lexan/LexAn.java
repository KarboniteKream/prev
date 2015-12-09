package compiler.lexan;

import java.io.*;

import compiler.*;

public class LexAn
{
	private boolean dump;

	private FileReader input;
	private int currChar;
	private String lexeme;
	private int startLine, currLine;
	private int startColumn, currColumn;

	public LexAn(String sourceFileName, boolean dump)
	{
		this.dump = dump;

		try
		{
			input = new FileReader(sourceFileName);
		}
		catch(FileNotFoundException __)
		{
			Report.error("File " + sourceFileName + " could not be found.");
		}

		readNext(false);
		currLine = 1;
		currColumn = 1;
	}

	// COMMENT: Prebere naslednji znak, append shrani trenutnega v lexeme.
	private void readNext(boolean append)
	{
		try
		{
			if(append == true)
			{
				lexeme += (char)currChar;
			}

			if(currChar == '\n')
			{
				currLine++;
				currColumn = 0;
			}

			currChar = input.read();
			currColumn++;
		}
		catch(IOException __)
		{
			Report.error(currLine, currColumn, "An error occurred while reading the file.");
		}
	}

	public Token lexAn()
	{
		int token = -1;
		lexeme = "";

		while(currChar == ' ' || currChar == '\t' || currChar == '\n' || currChar == '\r')
		{
			readNext(false);
		}

		while(token == -1)
		{
					// TODO: change from int to char
			int temp = currChar;

			startLine = currLine;
			startColumn = currColumn;

			switch(currChar)
			{
				case '+': case '-': case '*': case '/':
				case '%': case '&': case '|': case '^':
				case '!': case '=': case '<': case '>':
				case '(': case ')': case '[': case ']':
				case '{': case '}': case ':': case ';':
				case '.': case ',': case '\'':
					readNext(true);
				break;
			}

			switch(temp)
			{
				case -1: token = Token.EOF; break;

				// TODO: +=
				case '+':
					if(currChar == '+')
					{
						token = Token.INC;
						readNext(true);
					}
					else
					{
						token = Token.ADD;
					}
				break;

				case '-':
					if(currChar == '-')
					{
						token = Token.DEC;
						readNext(true);
					}
					else
					{
						token = Token.SUB;
					}
				break;

				case '*': token = Token.MUL; break;

				// TODO: /=
				case '/':
					if(currChar == '/')
					{
						// TODO: \r?
						while(currChar != '\n' && currChar != -1)
						{
							readNext(false);
						}

						readNext(false);
						lexeme = "";
					}
					// TODO: DIV_EQU
					else
					{
						token = Token.DIV;
					}
				break;

				// TODO: %=
				case '%': token = Token.MOD; break;

				case '&':
					if(currChar == '&')
					{
						token = Token.AND;
						readNext(true);
					}
					else
					{
						token = Token.BIT_AND;
					}
				break;

				case '|':
					if(currChar == '|')
					{
						token = Token.OR;
						readNext(true);
					}
					else
					{
						// Se tak OR splaca podpirati?
						token = Token.BIT_OR;
					}
				break;

				// TODO: Ali SIC podpira XOR?
				// To ni PTR!
				case '^': token = Token.BIT_XOR; break;

				case '~': token = Token.BIT_NOT; break;

				case '!':
					if(currChar == '=')
					{
						token = Token.NEQ;
						readNext(true);
					}
					else
					{
						token = Token.NOT;
					}
				break;

				case '=':
					if(currChar == '=')
					{
						token = Token.EQU;
						readNext(true);
					}
					else
					{
						token = Token.ASSIGN;
					}
				break;

				case '<':
					if(currChar == '=')
					{
						token = Token.LEQ;
						readNext(true);
					}
					else if(currChar == '<')
					{
						token = Token.LSHIFT;
						readNext(true);
					}
					else
					{
						token = Token.LTH;
					}
				break;

				case '>':
					if(currChar == '=')
					{
						token = Token.GEQ;
						readNext(true);
					}
					else if(currChar == '>')
					{
						token = Token.RSHIFT;
						readNext(true);
					}
					else
					{
						token = Token.GTH;
					}
				break;

				case '(': token = Token.LPARENT;   break;
				case ')': token = Token.RPARENT;   break;
				case '[': token = Token.LBRACKET;  break;
				case ']': token = Token.RBRACKET;  break;
				case '{': token = Token.LBRACE;    break;
				case '}': token = Token.RBRACE;    break;

				case '.': token = Token.DOT;       break;
				case ',': token = Token.COMMA;     break;
				case ';': token = Token.SEMICOLON; break;

				// char, string: ali naj dodam narekovaje?
				// TODO?
				case '\"':
					while(true)
					{
						readNext(true);

						if(currChar == '\"')
						{
							readNext(true);

							if(currChar != '\"')
							{
								token = Token.STR_CONST;
								break;
							}
						}
						else if(currChar == -1)
						{
							lexeme = "";
							token = Token.EOF;
							break;
						}
						else if(currChar < 32 || currChar > 126)
						{
							Report.error(currLine, currColumn, "Unexpected character.");
						}
					}
				break;

				case '\'':
					int c = currChar;
					readNext(true);

					if(c >= 32 && c <= 126 && currChar == '\'')
					{
						token = Token.CHAR_CONST;
						readNext(true);
					}
					else
					{
						Report.error(currLine, currColumn, "Unexpected character.");
					}
				break;

				case '\r': case '\n': case '\t': case ' ':
					readNext(false);
				break;

				default:
					// TODO: float constant.
					if(currChar >= '0' && currChar <= '9')
					{
						while(currChar >= '0' && currChar <= '9')
						{
							readNext(true);
						}

						if(currChar == '.')
						{
							readNext(true);

							while(currChar >= '0' && currChar <= '9')
							{
								readNext(true);
							}

							if(currChar == 'f')
							{
								readNext(false);
							}

							token = Token.FLOAT_CONST;
						}
						else
						{
							token = Token.INT_CONST;
						}
					}
					else if(currChar >= 'A' && currChar <= 'Z' || currChar >= 'a' && currChar <= 'z' || currChar == '_')
					{
						while(currChar >= 'A' && currChar <= 'Z' || currChar >= 'a' && currChar <= 'z' || currChar >= '0' && currChar <= '9' || currChar == '_')
						{
							readNext(true);
						}

						switch(lexeme)
						{
							case "true": case "false": token = Token.BOOL_CONST; break;

							case "bool": token = Token.BOOLEAN; break;
							case "int": token = Token.INTEGER; break;
							// TODO: Strings.
							case "char": token = Token.CHAR; break;
							// FIXME: float namesto double, popravi v dipl
							case "float": token = Token.FLOAT; break;
							case "void": token = Token.VOID; break;

							case "break": token = Token.BREAK; break;
							case "const": token = Token.CONST; break;
							case "continue": token = Token.CONTINUE; break;
							case "do": token = Token.DO; break;
							case "else": token = Token.ELSE; break;
							case "for": token = Token.FOR; break;
							case "if": token = Token.IF; break;
							case "return": token = Token.RETURN; break;

							// FIXME: (sizeof) v dipl
							// case "sizeof": token = Token.SIZEOF; break;

							case "struct": token = Token.STRUCT; break;
							case "typedef": token = Token.TYPEDEF; break;
							case "while": token = Token.WHILE; break;

							default: token = Token.IDENTIFIER; break;
						}
					}
					else
					{
						Report.warning(currLine, currColumn, "Unexpected character.");
						token = Token.EOF;
					}
				break;
			}
		}

		// Rename
		Token symbol = new Token(token, lexeme, startLine, startColumn, currLine, currColumn == 1 ? currColumn : currColumn - 1);
		// zakaj imamo ta dump?
		dump(symbol);

		return symbol;
	}

	// TODO: Remove dumps?
	private void dump(Token token)
	{
		if (! dump) return;
		if (Report.dumpFile() == null) return;
		if (token.token == Token.EOF)
			Report.dumpFile().println(token);
		else
			Report.dumpFile().println("[" + token.position.toString() + "] " + token.toString());
	}
}
