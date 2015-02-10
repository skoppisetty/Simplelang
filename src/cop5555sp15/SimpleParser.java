package cop5555sp15;

import static cop5555sp15.TokenStream.Kind.AND;
import static cop5555sp15.TokenStream.Kind.ARROW;
import static cop5555sp15.TokenStream.Kind.ASSIGN;
import static cop5555sp15.TokenStream.Kind.AT;
import static cop5555sp15.TokenStream.Kind.BAR;
import static cop5555sp15.TokenStream.Kind.BL_FALSE;
import static cop5555sp15.TokenStream.Kind.BL_TRUE;
import static cop5555sp15.TokenStream.Kind.COLON;
import static cop5555sp15.TokenStream.Kind.COMMA;
import static cop5555sp15.TokenStream.Kind.DIV;
import static cop5555sp15.TokenStream.Kind.DOT;
import static cop5555sp15.TokenStream.Kind.EOF;
import static cop5555sp15.TokenStream.Kind.EQUAL;
import static cop5555sp15.TokenStream.Kind.GE;
import static cop5555sp15.TokenStream.Kind.GT;
import static cop5555sp15.TokenStream.Kind.IDENT;
import static cop5555sp15.TokenStream.Kind.INT_LIT;
import static cop5555sp15.TokenStream.Kind.KW_BOOLEAN;
import static cop5555sp15.TokenStream.Kind.KW_CLASS;
import static cop5555sp15.TokenStream.Kind.KW_DEF;
import static cop5555sp15.TokenStream.Kind.KW_ELSE;
import static cop5555sp15.TokenStream.Kind.KW_IF;
import static cop5555sp15.TokenStream.Kind.KW_IMPORT;
import static cop5555sp15.TokenStream.Kind.KW_INT;
import static cop5555sp15.TokenStream.Kind.KW_PRINT;
import static cop5555sp15.TokenStream.Kind.KW_RETURN;
import static cop5555sp15.TokenStream.Kind.KW_STRING;
import static cop5555sp15.TokenStream.Kind.KW_WHILE;
import static cop5555sp15.TokenStream.Kind.LCURLY;
import static cop5555sp15.TokenStream.Kind.LE;
import static cop5555sp15.TokenStream.Kind.LPAREN;
import static cop5555sp15.TokenStream.Kind.LSHIFT;
import static cop5555sp15.TokenStream.Kind.LSQUARE;
import static cop5555sp15.TokenStream.Kind.LT;
import static cop5555sp15.TokenStream.Kind.MINUS;
import static cop5555sp15.TokenStream.Kind.MOD;
import static cop5555sp15.TokenStream.Kind.NOT;
import static cop5555sp15.TokenStream.Kind.NOTEQUAL;
import static cop5555sp15.TokenStream.Kind.PLUS;
import static cop5555sp15.TokenStream.Kind.RANGE;
import static cop5555sp15.TokenStream.Kind.RCURLY;
import static cop5555sp15.TokenStream.Kind.RPAREN;
import static cop5555sp15.TokenStream.Kind.RSHIFT;
import static cop5555sp15.TokenStream.Kind.RSQUARE;
import static cop5555sp15.TokenStream.Kind.SEMICOLON;
import static cop5555sp15.TokenStream.Kind.STRING_LIT;
import static cop5555sp15.TokenStream.Kind.TIMES;
import static cop5555sp15.TokenStream.Kind.KW_SIZE;
import static cop5555sp15.TokenStream.Kind.KW_KEY;
import static cop5555sp15.TokenStream.Kind.KW_VALUE;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;

import java.util.ArrayList;
import java.util.Arrays;

public class SimpleParser {

	@SuppressWarnings("serial")
	public class SyntaxException extends Exception {
		Token t;
		Kind[] expected;
		String msg;
		

		SyntaxException(Token t, Kind expected) {
			this.t = t;
			msg = "";
			this.expected = new Kind[1];
			this.expected[0] = expected;

		}

		public SyntaxException(Token t, String msg) {
			this.t = t;
			this.msg = msg;
		}

		public SyntaxException(Token t, Kind[] expected) {
			this.t = t;
			msg = "";
			this.expected = expected;
		}

		public String getMessage() {
			StringBuilder sb = new StringBuilder();
			sb.append(" error at token ").append(t.toString()).append(" ")
					.append(msg);
			sb.append(". Expected: ");
			for (Kind kind : expected) {
				sb.append(kind).append(" ");
			}
			return sb.toString();
		}
	}

	TokenStream tokens;
	Token t;
	ArrayList<Kind> predict_declaration = new ArrayList<Kind>();	
	ArrayList<Kind> factor_list = new ArrayList<Kind>();
	ArrayList<Kind> first_factor = new ArrayList<Kind>();
	
	
	
	
	

	SimpleParser(TokenStream tokens) {
		this.tokens = tokens;
		t = tokens.nextToken();
		setPredictsets();
	}


	private Kind match(Kind kind) throws SyntaxException {
		if (isKind(kind)) {
			consume();
			return kind;
		}
		throw new SyntaxException(t, kind);
	}

	private Kind match(Kind... kinds) throws SyntaxException {
		Kind kind = t.kind;
		if (isKind(kinds)) {
			consume();
			return kind;
		}
		StringBuilder sb = new StringBuilder();
		for (Kind kind1 : kinds) {
			sb.append(kind1).append(kind1).append(" ");
		}
		throw new SyntaxException(t, "expected one of " + sb.toString());
	}

	private boolean isKind(Kind kind) {
		return (t.kind == kind);
	}

	private void consume() {
		if (t.kind != EOF)
			t = tokens.nextToken();
	}

	private boolean isKind(Kind... kinds) {
		for (Kind kind : kinds) {
			if (t.kind == kind)
				return true;
		}
		return false;
	}

	//This is a convenient way to represent fixed sets of
	//token kinds.  You can pass these to isKind.
	static final Kind[] REL_OPS = { BAR, AND, EQUAL, NOTEQUAL, LT, GT, LE, GE };
	static final Kind[] WEAK_OPS = { PLUS, MINUS };
	static final Kind[] STRONG_OPS = { TIMES, DIV };
	static final Kind[] VERY_STRONG_OPS = { LSHIFT, RSHIFT };


	public void parse() throws SyntaxException {
		Program();
		match(EOF);
	}

	private void Program() throws SyntaxException {
		ImportList();
		match(KW_CLASS);
		match(IDENT);
		Block();
	}

	private void ImportList() throws SyntaxException {
		while(t.kind != KW_CLASS){
			match(KW_IMPORT);
			match(IDENT);
			while(t.kind != SEMICOLON){
				if(t.kind == DOT){
					match(DOT);
					match(IDENT);
				}
			}
			match(SEMICOLON);
		}
		
	}

	private void Block() throws SyntaxException {
		match(LCURLY);
		while(!isKind(RCURLY)){
			if(predict_declaration.contains(t.kind)){
				declaration();
				match(SEMICOLON);
			}
			else{
				statement();
				match(SEMICOLON);
			}
		}
		match(RCURLY);
	}
	

	private void declaration() throws SyntaxException {
		// TODO Auto-generated method stub
		match(KW_DEF);
		match(IDENT);
		if(t.kind == COLON){
			vardec();
		}
		else if(t.kind == ASSIGN){
			closuredec();
		}
		
	}

	private void vardec() throws SyntaxException{
		
			if(isKind(COLON)){
				match(COLON);
				type();
			}
	}


	private void type() throws SyntaxException{
		// TODO Auto-generated method stub
		if(t.kind == KW_INT || t.kind == KW_BOOLEAN || t.kind == KW_STRING){
			simpletype();
		}
		else if(isKind(AT)){
			match(AT);
			if(t.kind == AT){
				keyvaluetype();
			}
			else {
				listtype();
			}
		}
		else {
			throw new SyntaxException(t, "expected one of type");
		}		
	}

	private void simpletype()  throws SyntaxException {
		// TODO Auto-generated method stub
		if(t.kind == KW_INT || t.kind == KW_BOOLEAN || t.kind == KW_STRING){
			consume();
		}
		else{
			throw new SyntaxException(t, "expected one of simpletype");
		}
	}
	
	private void keyvaluetype()  throws SyntaxException{
		// TODO Auto-generated method stub
		match(AT);
		match(LSQUARE);
		simpletype();
		match(COLON);
		type();
		match(RSQUARE);
	}

	private void listtype()  throws SyntaxException{
		// TODO Auto-generated method stub
		match(LSQUARE);
		type();
		match(RSQUARE);
	}
	
	private void closuredec() throws SyntaxException {
		match(ASSIGN);
		closure();
		
	}


	private void closure() throws SyntaxException{
		match(LCURLY);
		formalarglist();
		match(ARROW);
		while(t.kind != RCURLY){
			statement();
			match(SEMICOLON);
		}
		match(RCURLY);
	}


	private void formalarglist() throws SyntaxException{
		System.out.println("check");
		if(t.kind == IDENT){
			match(IDENT);
			vardec();
			while(t.kind != ARROW){
				match(COMMA);
				match(IDENT);
				vardec();	
			}
		}
	}

	private void statement() throws SyntaxException{
		System.out.println("statement");
		if(t.kind == KW_PRINT){
			match(KW_PRINT);
			expression();
		}
		else if(t.kind == KW_WHILE){
			match(KW_WHILE);
			if(t.kind == TIMES){
				match(TIMES);
			}
			match(LPAREN);
			expression();
			if(t.kind == RANGE){
				match(RANGE);
				expression();
			}
			match(RPAREN);
			Block();
		}
		else if(t.kind == KW_IF){
			match(KW_IF);
			match(LPAREN);
			expression();
			match(RPAREN);
			Block();
			if(t.kind == KW_ELSE){
				match(KW_ELSE);
				Block();
			}
		}
		else if(t.kind == MOD){
			match(MOD);
			expression();
		}
		else if(t.kind == KW_RETURN){
			match(KW_RETURN);
			expression();
		}
		else if(t.kind == IDENT){
			lvalue();
			match(ASSIGN);
			expression();
		}
		
	}
	
	
	private void closureevalexpression() throws SyntaxException {
		match(LPAREN);
		expressionlist();
		match(RPAREN);
		
	}
	
	private void lvalue() throws SyntaxException {
		match(IDENT);
		if(t.kind == LSQUARE){
			match(LSQUARE);
			System.out.println("lvalue");
			expression();
			match(RSQUARE);
		}
		
	}
	
	private void list() throws SyntaxException{
		match(LSQUARE);
		expressionlist();
		match(RSQUARE);
	}

	private void expressionlist() throws SyntaxException {
		if(first_factor.contains(t.kind)){
			expression();
			while(t.kind == COMMA){
				match(COMMA);
				expression();
			}
		}
		
	}
	
	private void keyvalueexpression() throws SyntaxException{
		expression();
		match(COLON);
		expression();
	}
	
	private void keyvaluelist() throws SyntaxException{
		if(first_factor.contains(t.kind)){
			keyvalueexpression();
			while(t.kind == COMMA){
				match(COMMA);
				keyvalueexpression();
			}
		}
	}
	
	private void maplist() throws SyntaxException {
		match(AT);
		match(LSQUARE);
		keyvaluelist();
		match(RSQUARE);
		
	}

	private void expression() throws SyntaxException {
		term();
		while(Arrays.asList(REL_OPS).contains(t.kind)){
			relop();
			term();
		}
		
	}

	private void term() throws SyntaxException {
		elem();
		while(Arrays.asList(WEAK_OPS).contains(t.kind)){
			weakop();
			elem();
		}		
	}

	private void elem() throws SyntaxException {
		thing();
		while(Arrays.asList(STRONG_OPS).contains(t.kind)){
			strongop();
			thing();
		}
		
	}

	private void thing() throws SyntaxException {
		factor();
		while(Arrays.asList(VERY_STRONG_OPS).contains(t.kind)){
			verystrongop();
			factor();
		}
		
	}
	
	private void factor() throws SyntaxException {
		if(t.kind == IDENT){
			match(IDENT);
			if(t.kind == LSQUARE){
				match(LSQUARE);
				expression();
				match(RSQUARE);
			}
			else if(t.kind == LPAREN){
				closureevalexpression();
			}
		}
		else if(factor_list.contains(t.kind)){
			consume();
		}
		else if(t.kind == LPAREN){
			match(LPAREN);
			expression();
			match(RPAREN);
		}
		else if(t.kind == NOT || t.kind == MINUS){
			consume();
			factor();
		}
		else if(t.kind == KW_SIZE ||t.kind == KW_KEY | t.kind == KW_VALUE){
			consume();
			match(LPAREN);
			expression();
			match(RPAREN);
		}
		else if(t.kind == LCURLY){
			closure();
		}
		else if(t.kind == AT){
			consume();
			if(t.kind == AT){
				maplist();
			}
			else{
				list();
			}
		}
		else{
			throw new SyntaxException(t, "expected one of factor");
		}	
	}


	private void relop() throws SyntaxException {
		match(REL_OPS);
	}
	
	private void weakop() throws SyntaxException {
		match(WEAK_OPS);
	}
	
	private void strongop() throws SyntaxException {
		match(STRONG_OPS);
	}

	private void verystrongop() throws SyntaxException {
		match(VERY_STRONG_OPS);
	}

	private void setPredictsets() {
		// TODO Auto-generated method stub
		predict_declaration.add(KW_DEF);
				
		factor_list.add(INT_LIT);
		factor_list.add(BL_TRUE);
		factor_list.add(BL_FALSE);
		factor_list.add(STRING_LIT);
		
		first_factor.add(IDENT);
		first_factor.add(INT_LIT);
		first_factor.add(BL_TRUE);
		first_factor.add(BL_FALSE);
		first_factor.add(STRING_LIT);
		first_factor.add(LPAREN);
		first_factor.add(NOT);
		first_factor.add(MINUS);
		first_factor.add(KW_SIZE);
		first_factor.add(KW_VALUE);
		first_factor.add(KW_KEY);
		first_factor.add(AT);
		first_factor.add(LCURLY);
	}


}
