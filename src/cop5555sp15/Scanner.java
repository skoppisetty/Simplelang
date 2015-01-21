package cop5555sp15;

import java.util.HashMap;

import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import static cop5555sp15.TokenStream.Kind.*;

public class Scanner {


	private TokenStream stream;
	private char[] inputChars;
	private int begin;
	private HashMap operators = new HashMap();
	private int linenumber;


	public Scanner(TokenStream stream) {
		this.stream = stream;
		this.begin = 0;
		this.linenumber = 0;
		this.inputChars = stream.inputChars;
		setoperators();
	}


	private void setoperators() {
		operators.put('=', ASSIGN);
		operators.put('|', BAR);
		operators.put('&', AND);
		operators.put("==", EQUAL);
		operators.put("!=", NOTEQUAL);
		operators.put('<', LT);
		operators.put('>', GT);
		operators.put("<=", LE);
		operators.put(">=", GE);
		operators.put('+', PLUS);
		operators.put('-', MINUS);
		operators.put('*', TIMES);
		operators.put('/', DIV);
		operators.put('%', MOD);
		operators.put('!', NOT);
		operators.put("<<", LSHIFT);
		operators.put(">>", RSHIFT);
		operators.put("->", ARROW);
		operators.put('@', AT);
	}


	// Fills in the stream.tokens list with recognized tokens 
     //from the input
	public void scan() {
		
		Token t;
//		for(int i=0;i< this.inputChars.length;i++){
//			t = getnext();
//			stream.tokens.add(t);
//		}
		do {
			t = getnext();
			stream.tokens.add(t);
		} while (!t.kind.equals(EOF));
	}


	private Token getnext() {
		// returns the next token after skipping white space
		skip_whitespaces();
		// need to take care of comments
		Token t;
		t = readtoken();
		begin++;
		//System.out.print(t.getText());
		return t;
	}
	
	


	private Token readtoken() {
		Token t = null;
		int character = getNextChar(begin);
		if(character == -1){
			t = stream.new Token(EOF, begin, begin, linenumber);
		}
		else if(character == '-'){
			if((char)getNextChar(begin+1) == '>'){
				t = stream.new Token(ARROW, begin, begin+1, linenumber);
				begin = begin+1;
			}
			else{
				t = stream.new Token(MINUS, begin, begin, linenumber);
			}
		}
		else if(character == '<'){
			if((char)getNextChar(begin+1) == '<'){
				t = stream.new Token(LSHIFT, begin, begin+1, linenumber);
				begin = begin+1;
			}
			else if((char)getNextChar(begin+1) == '='){
				t = stream.new Token(LE, begin, begin+1, linenumber);
				begin = begin+1;
			}
			else{
				t = stream.new Token(LT, begin, begin, linenumber);
			}
		}
		else if(character == '>'){
			if((char)getNextChar(begin+1) == '>'){
				t = stream.new Token(RSHIFT, begin, begin+1, linenumber);
				begin = begin+1;
			}
			else if((char)getNextChar(begin+1) == '='){
				t = stream.new Token(GE, begin, begin+1, linenumber);
				begin = begin+1;
			}
			else{
				t = stream.new Token(GT, begin, begin, linenumber);
			}
		}
		else if(character == '='){
			if((char)getNextChar(begin+1) == '='){
				t = stream.new Token(EQUAL, begin, begin+1, linenumber);
				begin = begin+1;
			}
			else{
				t = stream.new Token(ASSIGN, begin, begin, linenumber);
			}
		}
		else if(character == '!'){
			if((char)getNextChar(begin+1) == '='){
				t = stream.new Token(NOTEQUAL, begin, begin+1, linenumber);
				begin = begin+1;
			}
			else{
				t = stream.new Token(NOT, begin, begin, linenumber);
			}
		}
		else if(operators.containsKey((char)character)){
			t = stream.new Token((Kind)operators.get((char)character), begin, begin+1, linenumber);
		}
		else{
			System.out.print(operators.get((char)character));
		}
		return t;
		
	}


	private void skip_whitespaces() {
		int character = getNextChar(begin);
		if (character != -1){
			while(Character.isWhitespace((char)getNextChar(begin))){
				begin++;
			}
			return;
		}
		else{
			return;
		}
		
	}
	
	public int getNextChar(int index) {
		if (index >= inputChars.length) {
			return -1;
		} 
		return inputChars[index];
	}

}

