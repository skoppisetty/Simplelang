package cop5555sp15;

import java.util.HashMap;

import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import static cop5555sp15.TokenStream.Kind.*;

public class Scanner {


	private TokenStream stream;
	private char[] inputChars;
	private int begin;
	private int start;
	private int linenumber;
	private HashMap operators = new HashMap<String, Kind>();
	private HashMap seperators = new HashMap<String, Kind>();
	private HashMap keywords = new HashMap<String, Kind>();
	
	
	private static enum State {
		Initial, Comment, Newline, Ident, Numlit, Done
	}

	private String[] seperator_keys= {".","..",";",",","(",")","[","]","{","}",":","?"};
	private Kind[] seperator_values= {DOT,RANGE,SEMICOLON,COMMA,LPAREN,RPAREN,LSQUARE,RSQUARE,LCURLY,RCURLY,COLON,QUESTION };

	private String[] keyword_keys= {"int" , "string" , "boolean" , "import" , "class" , "def" , "while" , "if" , "else" , "return" , "print"};
	private Kind[] keyword_values= {KW_INT, KW_STRING, KW_BOOLEAN, KW_IMPORT, KW_CLASS, KW_DEF, KW_WHILE, KW_IF, KW_ELSE, KW_RETURN, KW_PRINT,};

	
	private String[] operator_keys= {"=","|","&","==","!=","<",">","<=",">=","+","-","*","/","%","!","<<",">>","->","@"};
	private Kind[] operator_values= {ASSIGN,BAR,AND,EQUAL,NOTEQUAL,LT,GT,LE,GE,PLUS,MINUS,TIMES,DIV,MOD,NOT,LSHIFT,RSHIFT,ARROW,AT};

	private State state;
	
	public Scanner(TokenStream stream) {
		this.stream = stream;
		this.begin = 0;
		this.start = 0;
		this.linenumber = 1;
		this.state = State.Initial;
		setoperators();
	}


	private void setoperators() {
		for(int i=0;i<operator_keys.length;i++){
			operators.put(operator_keys[i], operator_values[i]);
		}
		for(int i=0;i<seperator_keys.length;i++){
			seperators.put(seperator_keys[i], seperator_values[i]);
		}
		for(int i=0;i<keyword_keys.length;i++){
			keywords.put(keyword_keys[i], keyword_values[i]);
		}

	}


	// Fills in the stream.tokens list with recognized tokens 
     //from the input
	public void scan() {
		Token t;
		do {
			t = readtoken();
			System.out.print(t);
			if(t != null){
				stream.tokens.add(t);
			}
		} while (!t.kind.equals(EOF));
	}

	private Token readtoken() {
		
		Token t = null;
		while(state != State.Done){
			int character = getNextChar(begin);
			//System.out.println(character);
			switch(state){
				case Initial:
					if (character == -1){
						t = stream.new Token(EOF, begin, begin, linenumber);
						state = State.Done;
					}
					else if(Character.isWhitespace((char)getNextChar(begin))){
						if(character == 10){
							state = State.Initial;
							linenumber++;
						}
						else if(character == 13){
							state = State.Newline;
						}
						else{
							state = State.Initial;
						}
					}
					else if(character == '.'){
						if((char)getNextChar(begin+1) == '.'){
							t = stream.new Token(RANGE, begin, begin+2, linenumber);
							begin++;
						}
						else{
							t = stream.new Token(DOT, begin, begin+1, linenumber);
						}
						state = State.Done;
					}
					else if(character == '/'){
						if((char)getNextChar(begin+1) == '*'){
							state = State.Comment;
							start= begin;
							begin++;
							
						}
					}
					else if(character == '-'){
						if((char)getNextChar(begin+1) == '>'){
							t = stream.new Token(ARROW, begin, begin+2, linenumber);
							begin++;
						}
						else{
							t = stream.new Token(MINUS, begin, begin+1, linenumber);
						}
						state = State.Done;
					}
					else if(character == '<'){
						if((char)getNextChar(begin+1) == '<'){
							t = stream.new Token(LSHIFT, begin, begin+2, linenumber);
							begin++;
						}
						else if((char)getNextChar(begin+1) == '='){
							t = stream.new Token(LE, begin, begin+2, linenumber);
							begin++;
						}
						else{
							t = stream.new Token(LT, begin, begin+1, linenumber);
						}
						state = State.Done;
					}
					else if(character == '>'){
						if((char)getNextChar(begin+1) == '>'){
							t = stream.new Token(RSHIFT, begin, begin+2, linenumber);
							begin++;
						}
						else if((char)getNextChar(begin+1) == '='){
							t = stream.new Token(GE, begin, begin+2, linenumber);
							begin++;
						}
						else{
							t = stream.new Token(GT, begin, begin+1, linenumber);
						}
						state = State.Done;
					}
					else if(character == '='){
						if((char)getNextChar(begin+1) == '='){
							t = stream.new Token(EQUAL, begin, begin+2, linenumber);
							begin++;
						}
						else{
							t = stream.new Token(ASSIGN, begin, begin+1, linenumber);
						}
						state = State.Done;
					}
					else if(character == '!'){
						if((char)getNextChar(begin+1) == '='){
							t = stream.new Token(NOTEQUAL, begin, begin+2, linenumber);
							begin++;
						}
						else{
							t = stream.new Token(NOT, begin, begin+1, linenumber);
						}
						state = State.Done;
					}
					else if(operators.containsKey(String.valueOf((char)character))){
						t = stream.new Token((Kind)operators.get(String.valueOf((char)character)), begin, begin+1, linenumber);
						state = State.Done;
					}
					else if(seperators.containsKey(String.valueOf((char)character))){
						t = stream.new Token((Kind)seperators.get(String.valueOf((char)character)), begin, begin+1, linenumber);
						state = State.Done;
					}
					else if(Character.isDigit(character)){
						state = State.Numlit;
						start = begin;
					}
					else if(Character.isJavaIdentifierStart(character)){
						state = State.Ident;
						start = begin;
					}
					else{
						System.out.println(operators.containsKey(String.valueOf((char)character)));
						System.out.println((char)character);
						state = State.Done;
					}
					break;
				case Newline:
					if(character == 10){
						state = State.Initial;
						linenumber++;
					}
					else{
						state = State.Initial;
						linenumber++;
						begin--;
					}
					break;
				case Comment:
					if(character == '*'){
						if((char)getNextChar(begin+1) == '/'){
							state = State.Initial;
							//t = stream.new Token(COMMENT, start, begin+1, linenumber);
							begin++;
							
						}
					}
					else if(character == -1){
						t = stream.new Token(UNTERMINATED_COMMENT, start, begin, linenumber);
//						t = stream.new Token(EOF, begin, begin, linenumber);
						state = State.Done;
					}
					break;
				case Numlit:
					if(!Character.isDigit(character)){
						t = stream.new Token(INT_LIT, start, begin, linenumber);
						state = State.Done;
						begin--;
					}
					break;
				case Ident:
					if(!Character.isJavaIdentifierPart(character)){
						String str = "";

						for(int i = start; i < begin; i++){
						    str += stream.inputChars[i];
						}
						//System.out.println(str);
						if(keywords.containsKey(str)){
							t = stream.new Token((Kind)keywords.get(str), start, begin, linenumber);
						}
						else{
							t = stream.new Token(IDENT, start, begin, linenumber);
						}
						
						state = State.Done;
						begin--;
					}
					break;
				case Done:
					break;
				default:
					break;
			}
			begin++;
		}

		state = State.Initial;
		return t;
		
		
	}


//	private void skip_whitespaces() {
//		int character = getNextChar(begin);
//		if (character != -1){
//			while(Character.isWhitespace((char)getNextChar(begin))){
//				System.out.println(getNextChar(begin));
//				begin++;
//			}
//			return;
//		}
//		else{
//			return;
//		}
//		
//	}
	
	public int getNextChar(int index) {
		if (index >= stream.inputChars.length) {
			return -1;
		} 
		return stream.inputChars[index];
	}

}

