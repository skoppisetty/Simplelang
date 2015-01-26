package cop5555sp15;

import static org.junit.Assert.*;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;

import org.junit.Test;

import cop5555sp15.TokenStream;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import static cop5555sp15.TokenStream.Kind.*;

public class TestScanner {

	@Test
	public void emptyInput() {
		System.out.println("Test: emptyInput");
		String input = "";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		assertEquals(1, stream.tokens.size()); // creates EOF token
		assertEquals(EOF, stream.nextToken().kind);

	}

	@Test
	public void noWhiteSpace() {
		System.out.println("Test: noWhitespace");
		String input = "@%";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		assertEquals(3, stream.tokens.size()); // one each for @ and %, plus the
												// eof
												// token
		assertEquals(AT, stream.nextToken().kind);
		assertEquals(MOD, stream.nextToken().kind);
		assertEquals(EOF, stream.nextToken().kind);

	}

	@Test
	public void errorToken() {
		System.out.println("Test: noWhitespace");
		String input = "@#  *";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		assertEquals(4, stream.tokens.size()); // one each for @,#, and *, plus
												// the eof token
		assertEquals(AT, stream.nextToken().kind);
		assertEquals(ILLEGAL_CHAR, stream.nextToken().kind);
		assertEquals(TIMES, stream.nextToken().kind);
		assertEquals(EOF, stream.nextToken().kind);

	}

	@Test
	public void onlySpaces() {
		System.out.println("Test: onlySpaces");
		String input = "     "; // five spaces
		System.out.println("input is five spaces");
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		assertEquals(1, stream.tokens.size()); // creates EOF token
		Token t = stream.nextToken();
		System.out.println(stream);
		assertEquals(EOF, t.kind);
		assertEquals(5, t.beg);
	}

	@Test
	public void skipWhiteSpace() {
		System.out.println("skipWhiteSpace");
		String input = "   ;;;   %@%\n  \r   \r\n ;;;";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		assertEquals(SEMICOLON, stream.nextToken().kind);
		assertEquals(SEMICOLON, stream.nextToken().kind);
		assertEquals(SEMICOLON, stream.nextToken().kind);
		assertEquals(MOD, stream.nextToken().kind);
		assertEquals(AT, stream.nextToken().kind);
		assertEquals(MOD, stream.nextToken().kind);
		assertEquals(SEMICOLON, stream.nextToken().kind);
		assertEquals(SEMICOLON, stream.nextToken().kind);
		Token t = stream.nextToken();
		assertEquals(SEMICOLON, t.kind);
		assertEquals(4,t.getLineNumber());
	}

	@Test
	public void dotsAndRanges() {
		System.out.println("dotsAndRanges");
		String input = ".\n..\n.. . . ..\n...\n";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		assertEquals(DOT, stream.nextToken().kind);
		assertEquals(RANGE, stream.nextToken().kind);
		assertEquals(RANGE, stream.nextToken().kind);
		assertEquals(DOT, stream.nextToken().kind);
		assertEquals(DOT, stream.nextToken().kind);
		assertEquals(RANGE, stream.nextToken().kind);
		assertEquals(RANGE, stream.nextToken().kind);
		assertEquals(DOT, stream.nextToken().kind);
		assertEquals(EOF, stream.nextToken().kind);
		assertEquals(3, stream.tokens.get(5).getLineNumber());// 5th token is on
																// line 3
	}

	@Test
	public void firstPartAtEndOfInput() {
		System.out.println("firstPartATEndOfInput");
		String input = "!";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		assertEquals(NOT, stream.nextToken().kind);
		assertEquals(EOF, stream.nextToken().kind);
	}

	@Test
	public void twoStateTokens() {
		System.out.println("twoStateTokens");
		String input = "= == =\n= ! != - -> -! =!!";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		assertEquals(ASSIGN, stream.nextToken().kind);
		assertEquals(EQUAL, stream.nextToken().kind);
		assertEquals(ASSIGN, stream.nextToken().kind);
		assertEquals(ASSIGN, stream.nextToken().kind);
		assertEquals(NOT, stream.nextToken().kind);
		assertEquals(NOTEQUAL, stream.nextToken().kind);
		assertEquals(MINUS, stream.nextToken().kind);
		assertEquals(ARROW, stream.nextToken().kind);
		assertEquals(MINUS, stream.nextToken().kind);
		assertEquals(NOT, stream.nextToken().kind);
		assertEquals(ASSIGN, stream.nextToken().kind);
		assertEquals(NOT, stream.nextToken().kind);
		assertEquals(NOT, stream.nextToken().kind);
		assertEquals(EOF, stream.nextToken().kind);
	}

	// This test constructs the exptected token list and compares to the one
	// created by the Scanner
	@Test
	public void compareTokenList() {
		System.out.println("compareTokenList");
		String input = "= ==";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Token t0 = stream.new Token(ASSIGN, 0, 1, 1);
		Token t1 = stream.new Token(EQUAL, 2, 4, 1);
		Token t2 = stream.new Token(EOF, 4, 4, 1);
		ArrayList<Token> expected_tokens = new ArrayList<Token>();
		expected_tokens.add(t0);
		expected_tokens.add(t1);
		expected_tokens.add(t2);
		assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}

	@Test
	public void lessAndGreater() {
		System.out.println("lessAndGreater");
		String input = " < << <= > >> >= -> <>";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		assertEquals(LT, stream.nextToken().kind);
		assertEquals(LSHIFT, stream.nextToken().kind);
		assertEquals(LE, stream.nextToken().kind);
		assertEquals(GT, stream.nextToken().kind);
		assertEquals(RSHIFT, stream.nextToken().kind);
		assertEquals(GE, stream.nextToken().kind);
		assertEquals(ARROW, stream.nextToken().kind);
		assertEquals(LT, stream.nextToken().kind);
		assertEquals(GT, stream.nextToken().kind);
		assertEquals(EOF, stream.nextToken().kind);
	}

	@Test
	public void intLiterals() {
		System.out.println("lessAndGreater");
		String input = "0 1 23 45+ 67<=9";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { INT_LIT, INT_LIT, INT_LIT, INT_LIT, PLUS,
				INT_LIT, LE, INT_LIT, EOF };
		String[] expectedTexts = { "0", "1", "23", "45", "+", "67", "<=", "9",
				"" }; // need empty string for eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}

	@Test
	public void stringLiterals() {
		System.out.println("stringLiterals");
		String input = " \"abc\" \"def\" \"ghijk\" \"123\" \"&^%$\" ";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { STRING_LIT, STRING_LIT, STRING_LIT,
				STRING_LIT, STRING_LIT, EOF };
		String[] expectedTexts = { "abc", "def", "ghijk", "123", "&^%$", "" }; // need
																				// empty
																				// string
																				// for
																				// eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}

	@Test
	public void identifiers() {
		System.out.println("identifiers");
		String input = " abc ddef ghijk 123 a234 32a";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { IDENT, IDENT, IDENT, INT_LIT, IDENT, INT_LIT,
				IDENT, EOF };
		String[] expectedTexts = { "abc", "ddef", "ghijk", "123", "a234", "32",
				"a", "" }; // need empty string for eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}

	@Test
	public void keywords() {
		System.out.println("keywords");
		String input = " int  string  boolean import  class  def  while if  else  return  print aaa";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { KW_INT, KW_STRING, KW_BOOLEAN, KW_IMPORT,
				KW_CLASS, KW_DEF, KW_WHILE, KW_IF, KW_ELSE, KW_RETURN,
				KW_PRINT, IDENT, EOF };
		String[] expectedTexts = { "int", "string", "boolean", "import",
				"class", "def", "while", "if", "else", "return", "print",
				"aaa", "" }; // need empty string for eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}

	@Test
	public void boolAndNullLiterals() {
		System.out.println("boolAndNullLiterals");
		String input = " true false\n null";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { BL_TRUE, BL_FALSE, NL_NULL, EOF };
		String[] expectedTexts = { "true", "false", "null", "" }; // need empty
																	// string
																	// for eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}

	@Test
	public void multiLineString() {
		System.out.println("multiLineString");
		String input = " \"true false\n null\" ";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { STRING_LIT, EOF };
		String[] expectedTexts = { "true false\n null", "" }; // need empty
																// string for
																// eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));

	}

	@Test
	public void comments() {
		System.out.println("comments");
		String input = "/**/ 0 1 45+ 67<=9";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { INT_LIT, INT_LIT, INT_LIT, PLUS, INT_LIT, LE,
				INT_LIT, EOF };
		String[] expectedTexts = { "0", "1", "45", "+", "67", "<=", "9", "" }; // need
																				// empty
																				// string
																				// for
																				// eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}

	@Test
	public void comments2() {
		System.out.println("comments2");
		String input = "/**/ 0 1 /** ***/ 45+ 67<=9";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { INT_LIT, INT_LIT, INT_LIT, PLUS, INT_LIT, LE,
				INT_LIT, EOF };
		String[] expectedTexts = { "0", "1", "45", "+", "67", "<=", "9", "" }; // need
																				// empty
																				// string
																				// for
																				// eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}

	@Test
	public void comments3() {
		System.out.println("comments3");
		String input = "/**/ 0 1 /** ***/ 45+ 67<=9/*";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { INT_LIT, INT_LIT, INT_LIT, PLUS, INT_LIT, LE,
				INT_LIT, UNTERMINATED_COMMENT, EOF };
		String[] expectedTexts = { "0", "1", "45", "+", "67", "<=", "9", "/*",
				"" }; // need empty string for eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}

	@Test
	public void onlyComment() {
		System.out.println("onlyComment");
		String input = "/**/";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Kind[] expectedKinds = { EOF };
		String[] expectedTexts = { "" }; // need empty string for eof
		assertArrayEquals(expectedKinds, makeKindArray(stream));
		assertArrayEquals(expectedTexts, makeTokenTextArray(stream));
	}
	
	@Test
	public void singleSlash(){
		System.out.println("singleSlash");
		String input = "/";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
	}
	
	@Test
	public void line_num(){
		System.out.println("checking linenumber, beg and end values");
		String input = "abc def";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Token t0 = stream.new Token(IDENT, 0, 3, 1);
		Token t1 = stream.new Token(KW_DEF, 4, 7, 1);
		Token t2 = stream.new Token(EOF, 7, 7, 1);
		ArrayList<Token> expected_tokens = new ArrayList<Token>();
		expected_tokens.add(t0);
		expected_tokens.add(t1);
		expected_tokens.add(t2);
		assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}
	
	@Test
	public void zero_start(){
		System.out.println("numbers starting with zero");
		String input = "0123a4";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Token t0 = stream.new Token(INT_LIT, 0, 1, 1);
		Token t1 = stream.new Token(INT_LIT, 1, 4, 1);
		Token t2 = stream.new Token(IDENT, 4, 6, 1);
		Token t3 = stream.new Token(EOF, 6, 6, 1);
		ArrayList<Token> expected_tokens = new ArrayList<Token>();
		expected_tokens.add(t0);
		expected_tokens.add(t1);
		expected_tokens.add(t2);
		expected_tokens.add(t3);
		assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}
	
	
	@Test
	public void range_dot(){
		System.out.println("Checking for Range and Dot");
		String input = "...";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Token t0 = stream.new Token(RANGE, 0, 2, 1);
		Token t1 = stream.new Token(DOT, 2, 3, 1);
		Token t2 = stream.new Token(EOF, 3, 3, 1);
		ArrayList<Token> expected_tokens = new ArrayList<Token>();
		expected_tokens.add(t0);
		expected_tokens.add(t1);
		expected_tokens.add(t2);
		assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}
	
	
	@Test
	public void EOF_check_strlit(){
		System.out.println("Checking for EOF after unterminated str lit");
		String input = "\"test";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Token t0 = stream.new Token(UNTERMINATED_STRING, 0, 5, 1);
		Token t1 = stream.new Token(EOF, input.length(), input.length(), 1);
		ArrayList<Token> expected_tokens = new ArrayList<Token>();
		expected_tokens.add(t0);
		expected_tokens.add(t1);
		assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}
	
	@Test
	public void EOF_check_comment(){
		System.out.println("Checking for EOF after unterminated comment");
		String input = "/*test";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Token t0 = stream.new Token(UNTERMINATED_COMMENT, 0, 6, 1);
		Token t1 = stream.new Token(EOF, input.length(), input.length(), 1);
		ArrayList<Token> expected_tokens = new ArrayList<Token>();
		expected_tokens.add(t0);
		expected_tokens.add(t1);
		assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}
	
	@Test
	public void comment_with_quote(){
		System.out.println("Checking for comment with quote and newline ");
		String input = "/*test \n\r \n\r \r\n \" */\"";
		System.out.println(input);
		
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Token t0 = stream.new Token(UNTERMINATED_STRING, 20, 21, 6);
		Token t1 = stream.new Token(EOF, input.length(), input.length(), 6);
		ArrayList<Token> expected_tokens = new ArrayList<Token>();
		expected_tokens.add(t0);
		expected_tokens.add(t1);
		assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}
	
	
	@Test
	public void ident_start(){
		System.out.println("Checking for identifier start for $ and _");
		String input = "$san_test _s34\n \r 4a4#/*\"";
		System.out.println(input);
		TokenStream stream = new TokenStream(input);
		Scanner scanner = new Scanner(stream);
		scanner.scan();
		System.out.println(stream);
		Token t0 = stream.new Token(IDENT, 0, 9, 1);
		Token t1 = stream.new Token(IDENT, 10, 14, 1);
		Token t2 = stream.new Token(INT_LIT, 18, 19, 3);
		Token t3 = stream.new Token(IDENT, 19, 21, 3);
		Token t4 = stream.new Token(ILLEGAL_CHAR, 21, 22, 3);
		Token t5 = stream.new Token(UNTERMINATED_COMMENT, 22, 25, 3);
		Token t6 = stream.new Token(EOF, input.length(), input.length(), 3);
//		System.out.println(input.length());
		ArrayList<Token> expected_tokens = new ArrayList<Token>();
		expected_tokens.add(t0);
		expected_tokens.add(t1);
		expected_tokens.add(t2);
		expected_tokens.add(t3);
		expected_tokens.add(t4);
		expected_tokens.add(t5);
		expected_tokens.add(t6);
		
		assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}
	

	@Test
	public void multilinefile() throws FileNotFoundException{
		  System.out.println("Checking for comment with quote and newline ");
			Reader reader = new FileReader("/home/suresh/workspace/compiler/test/multiline_string.txt");
			TokenStream stream = new TokenStream(reader);
			Scanner scanner = new Scanner(stream);
			scanner.scan();
			System.out.println(stream);
			Token t0 = stream.new Token(IDENT, 19, 20, 5);
			Token t1 = stream.new Token(ASSIGN, 21, 22, 5);
			Token t2 = stream.new Token(STRING_LIT, 23, 36, 5);
			Token t3 = stream.new Token(SEMICOLON, 36, 37, 6);
			Token t4 = stream.new Token(EOF, stream.inputChars.length, stream.inputChars.length, 6);
			ArrayList<Token> expected_tokens = new ArrayList<Token>();
			expected_tokens.add(t0);
			expected_tokens.add(t1);
			expected_tokens.add(t2);
			expected_tokens.add(t3);
			expected_tokens.add(t4);
			assertArrayEquals(expected_tokens.toArray(), stream.tokens.toArray());
	}
	
	
	
	
	// Creates an array containing the kinds of the tokens in the token list
	Kind[] makeKindArray(TokenStream stream) {
		Kind[] kinds = new Kind[stream.tokens.size()];
		for (int i = 0; i < stream.tokens.size(); ++i) {
			kinds[i] = stream.tokens.get(i).kind;
			System.out.println(kinds[i]);
		}
		return kinds;
		
	}

	// Creates an array containing the texts of the tokens in the token list
	String[] makeTokenTextArray(TokenStream stream) {
		String[] kinds = new String[stream.tokens.size()];
		for (int i = 0; i < stream.tokens.size(); ++i) {
			kinds[i] = stream.tokens.get(i).getText();
			System.out.println(kinds[i]);
		}
		return kinds;
	}

	
}
