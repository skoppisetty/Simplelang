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
import cop5555sp15.Parser.SyntaxException;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.TokenStream.Token;
import cop5555sp15.ast.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Parser {

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
	List<SyntaxException> exceptionList = new ArrayList<SyntaxException>();
	

	Parser(TokenStream tokens) {
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
		SyntaxException e = new SyntaxException(t, "expected one of " + sb.toString());
		exceptionList.add(e);
		throw e;
	}
	
//	private Kind match(Kind... kinds) throws SyntaxException {
//		Kind kind = t.kind;
//		if (isKind(kinds)) {
//			consume();
//			return kind;
//		}
//		StringBuilder sb = new StringBuilder();
//		for (Kind kind1 : kinds) {
//			sb.append(kind1).append(kind1).append(" ");
//		}
//		throw new SyntaxException(t, "expected one of " + sb.toString());
//	}

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


	public Program parse(){
		Program p = null;
		try {
			p = Program();
			if (p != null)
				match(EOF);
		} catch (SyntaxException e) {
			exceptionList.add(e);
		}
		if (exceptionList.isEmpty())
			return p;
		else
			return null;
	}

	private Program Program() throws SyntaxException{
		List<QualifiedName> imports = ImportList();
		try {
			match(KW_CLASS);
		} catch (SyntaxException e) {
			exceptionList.add(e);
		}
		Token program_token = t;
		String program_name = t.getText();
		try {
			match(IDENT);
		} catch (SyntaxException e) {
			exceptionList.add(e);
		}
		Block block = Block();
		Program prog = new Program(t,imports,program_name,block);
		return prog;
	}

	private List<QualifiedName> ImportList() throws SyntaxException{
		List<QualifiedName> imports = new ArrayList<QualifiedName>();
		while(isKind(KW_IMPORT)){
			Token temp_import = t;
			match(KW_IMPORT);
			String s = t.getText();
			match(IDENT);
			while(isKind(DOT)){
					match(DOT);
					s += "/" + t.getText();
					match(IDENT);
			}
			QualifiedName qname = new QualifiedName(temp_import,s);
			imports.add(qname);
			match(SEMICOLON);
		}
		return imports;
	}

	private Block Block() throws SyntaxException{
		Token block_first = t;
		match(LCURLY);
		List<BlockElem> elems = new ArrayList<BlockElem>();
		while(!isKind(RCURLY)){
			BlockElem e;
			if(predict_declaration.contains(t.kind)){
				e = declaration();
				match(SEMICOLON);
			}
			else{
				e = statement();
				match(SEMICOLON);
			}
			if(e != null){
				elems.add(e);
			}
		}
		Block block = new Block(block_first, elems);
		match(RCURLY);
		return block;
	}
	

	private Declaration declaration() throws SyntaxException {
		Token defToken = t;
		match(KW_DEF);
		Token identToken = t;
		match(IDENT);
		Declaration d = null;
		if(isKind(ASSIGN)){
			d = closuredec(defToken,identToken);
		}
		else {
			d = vardec(defToken,identToken);
		}
		// ignore else as vardec can go to null
		return d;
	}

	private VarDec vardec(Token defToken, Token identToken) throws SyntaxException{
		Type vardecType = null;
		if(isKind(COLON)){
			match(COLON);
			vardecType = type();
		}
		else{
			vardecType = new UndeclaredType(defToken);
		}		
		VarDec var = new VarDec(defToken,identToken,vardecType);
		return var;
		// null case doesn't come to this function
	}


	private Type type() throws SyntaxException{
		Type type = null;
		if(isKind(KW_INT) || isKind(KW_BOOLEAN) || isKind(KW_STRING)){
			type = simpletype();
		}
		else if(isKind(AT)){
			Token first = t;
			match(AT);
			if(t.kind == AT){
				type = keyvaluetype(first);
			}
			else {
				type = listtype(first);
			}
		}
		else {
			throw new SyntaxException(t, "expected one of type");
		}
		return type;
	}

	private SimpleType simpletype()  throws SyntaxException {
		if(isKind(KW_INT) || isKind(KW_BOOLEAN) || isKind(KW_STRING)){
			Token simpletoken = t;
			SimpleType s = new SimpleType(t,t);
			consume();
			return s;
		}
		else{
			throw new SyntaxException(t, "expected one of simpletype");
		}
	}
	
	private KeyValueType keyvaluetype(Token first)  throws SyntaxException{
		// TODO Auto-generated method stub
		match(AT);
		match(LSQUARE);
		SimpleType type = simpletype();
		match(COLON);
		Type k_type = type();
		match(RSQUARE);
		KeyValueType k = new KeyValueType(first,type, k_type);
		return k;
	}

	private ListType listtype(Token first)  throws SyntaxException{
		// TODO Auto-generated method stub
		match(LSQUARE);
		Type l_type = type();
		match(RSQUARE);
		ListType k = new ListType(first, l_type);
		return k;
	}
	
	private ClosureDec closuredec(Token defToken, Token identToken) throws SyntaxException {
		match(ASSIGN);
		Closure c = closure();
		ClosureDec close = new ClosureDec(defToken,identToken,c);
		return close;
	}


	private Closure closure() throws SyntaxException{
		List<VarDec>formalArgList = new ArrayList<VarDec>();
		List<Statement> statementList = new ArrayList<Statement>();
		Token firsttoken = t;
		match(LCURLY);
		formalArgList = formalarglist();
		match(ARROW);
		while(!isKind(RCURLY)){
			Statement s  = statement();
			statementList.add(s);
			match(SEMICOLON);
		}
		match(RCURLY);
		
		Closure c = new Closure(firsttoken, formalArgList,statementList); 
		return c;
	}


	private List<VarDec> formalarglist() throws SyntaxException{
		List<VarDec>formalArgList = new ArrayList<VarDec>();
		if(isKind(IDENT)){
			Token ident = t;
			match(IDENT);
			VarDec v = vardec(ident, ident);
			formalArgList.add(v);
			while(isKind(COMMA)){
				match(COMMA);
				ident = t;
				match(IDENT);
				v = vardec(ident,ident);	
				formalArgList.add(v);
			}
		}
		return formalArgList;
	}

	private Statement statement() throws SyntaxException{
		Token firstToken = t;
		Statement stat = null;
		if(isKind(KW_PRINT)){
			match(KW_PRINT);
			Expression e = expression();
			stat = new PrintStatement(firstToken,e);
		}
		else if(isKind(KW_WHILE)){
			match(KW_WHILE);
			boolean times = false;
			boolean range = false;
			if(isKind(TIMES)){
				times = true;
				match(TIMES);
			}
			match(LPAREN);
			Expression e = expression();
			RangeExpression rangeEx = null;
			if(isKind(RANGE)){
				range = true;
				match(RANGE);
				Expression upper = expression();
				rangeEx = new RangeExpression(firstToken, e ,upper);
			}
			
			// merged all while loop conditions
			// removed range expression production as its used in only one place
			match(RPAREN);
			Block block = Block();
			if(!times && !range){
				stat = new WhileStatement(firstToken, e, block);
			}
			else if(range){
				stat = new WhileRangeStatement(firstToken,rangeEx, block);
			}
			else{
				stat = new WhileStarStatement(firstToken, e, block);
			}
			
		}
		else if(isKind(KW_IF)){
			
			match(KW_IF);
			match(LPAREN);
			Expression e = expression();
			match(RPAREN);
			Block ifblock = Block();
			stat = new IfStatement(firstToken,e, ifblock);
			if(isKind(KW_ELSE)){
				match(KW_ELSE);
				Block elseblock = Block();
				stat = new IfElseStatement(firstToken, e, ifblock, elseblock);
			}
			
		}
		else if(isKind(MOD)){
			Token first = t;
			match(MOD);
			Expression e = expression();
			stat = new ExpressionStatement(first, e);
		}
		else if(isKind(KW_RETURN)){
			match(KW_RETURN);
			Expression e = expression();
			stat = new ReturnStatement(firstToken, e);

		}
		else if(isKind(IDENT)){
			LValue val = lvalue();
			match(ASSIGN);
			Expression e = expression();
			stat = new AssignmentStatement(firstToken, val, e);
		}
		return stat;
	}
	
	private ClosureEvalExpression closureevalexpression(Token firsttoken) throws SyntaxException {
		List<Expression> e_List = new ArrayList<Expression>();
		match(LPAREN);
		e_List = expressionlist();
		match(RPAREN);
		ClosureEvalExpression c = new ClosureEvalExpression(firsttoken,firsttoken,e_List);
		return c;
	}
	
	private LValue lvalue() throws SyntaxException {
		Token firsttoken = t;
		match(IDENT);
		LValue lval = null;
		if(isKind(LSQUARE)){
			match(LSQUARE);
			Expression e = expression();
			match(RSQUARE);
			lval = new ExpressionLValue(firsttoken, firsttoken,e);
		}
		else{
			lval = new IdentLValue(firsttoken, firsttoken);
		}
		return lval;
	}
	
	private List<Expression> list() throws SyntaxException{
		// removed @ from list as it is making LL(2)
		// matched it in factor before this function gets called
		match(LSQUARE);
		List<Expression> elist = expressionlist();
		match(RSQUARE);
		return elist;
	}

	private List<Expression> expressionlist() throws SyntaxException {
		List<Expression> elist = new ArrayList<Expression>();
		if(first_factor.contains(t.kind)){
			Expression e = expression();
			elist.add(e);
			while(isKind(COMMA)){
				match(COMMA);
				e = expression();
				elist.add(e);
			}
		}
		return elist;
		
	}
	
	private KeyValueExpression keyvalueexpression() throws SyntaxException{
		Token firstToken = t;
		Expression key = expression();
		match(COLON);
		Expression value = expression();
		KeyValueExpression k = new KeyValueExpression(firstToken,key, value);
		return k;
	}
	
	private List<KeyValueExpression> keyvaluelist() throws SyntaxException{
		List<KeyValueExpression> klist = new ArrayList<KeyValueExpression>();
		KeyValueExpression k = null;
		if(first_factor.contains(t.kind)){
			k = keyvalueexpression();
			klist.add(k);
			while(isKind(COMMA)){
				match(COMMA);
				k = keyvalueexpression();
				klist.add(k);
			}
		}
		
		return klist;
	}
	
	private List<KeyValueExpression> maplist() throws SyntaxException {
		// already removed AT to make LL1 check LIST(). only one AT left
		match(AT);
		match(LSQUARE);
		List<KeyValueExpression> klist = keyvaluelist();
		match(RSQUARE);
		return klist;
	}

	private Expression expression() throws SyntaxException {
		Token first = t;
		Expression e1 = term();
		while(Arrays.asList(REL_OPS).contains(t.kind)){
			Token op = t;
			relop();
			Expression e2 = term();
			e1 = new BinaryExpression(first,e1,op, e2);	
		}
		return e1;
	}

	private Expression term() throws SyntaxException {
		Token first = t;
		Expression e1 = elem();
		while(Arrays.asList(WEAK_OPS).contains(t.kind)){
			Token op = t;
			weakop();
			Expression e2 = elem();
			e1 = new BinaryExpression(first,e1,op, e2);
		}		
		return e1;
	}

	private Expression elem() throws SyntaxException {
		Token first = t;
		Expression e1 = thing();
		while(Arrays.asList(STRONG_OPS).contains(t.kind)){
			Token op = t;
			strongop();
			Expression e2 = thing();
			e1 = new BinaryExpression(first,e1,op, e2);
		}
		return e1;
		
	}

	private Expression thing() throws SyntaxException {
		Token first = t;
		Expression e1 = factor();
		while(Arrays.asList(VERY_STRONG_OPS).contains(t.kind)){
			Token op = t;
			verystrongop();
			Expression e2 =  factor();
			e1 = new BinaryExpression(first,e1,op, e2);
		}
		return e1;
	}
	
	private Expression factor() throws SyntaxException {
		Token first = t;
		Expression fact = null;
		if(isKind(IDENT)){
			match(IDENT);
			if(isKind(LSQUARE)){
				match(LSQUARE);
				Expression e = expression();
				match(RSQUARE);
				fact = new ListOrMapElemExpression(first, first,e);
			}
			else if(isKind(LPAREN)){
				fact = closureevalexpression(first);
			}
			else{
				fact = new IdentExpression(first, first);
			}
		}
		else if(factor_list.contains(t.kind)){
			if(isKind(INT_LIT)){
				fact = new IntLitExpression(first, first.getIntVal());
			}
			else if(isKind(STRING_LIT)){
				fact = new StringLitExpression(first, first.getText());
			}
			else{
				fact = new BooleanLitExpression(first, Boolean.valueOf(first.getText()));
			}
			consume();
			// int_lit true false and string lit
			
		}
		else if(isKind(LPAREN)){
			match(LPAREN);
			fact = expression();
			match(RPAREN);
		}
		else if(isKind(NOT) || isKind(MINUS)){
			consume();
			Expression f = factor();
			fact = new UnaryExpression(first,first,f);
		}
		else if(isKind(KW_SIZE)){
			consume();
			match(LPAREN);
			Expression f = expression();
			match(RPAREN);
			fact = new SizeExpression(first,f);
		}
		else if(isKind(KW_KEY)){
			consume();
			match(LPAREN);
			Expression f = expression();
			match(RPAREN);
			fact = new KeyExpression(first,f);
		}
		else if(isKind(KW_VALUE)){
			consume();
			match(LPAREN);
			Expression f = expression();
			match(RPAREN);
			fact = new ValueExpression(first,f);
		}
		else if(isKind(LCURLY)){
			Closure close = closure();
			fact = new ClosureExpression(first,close);
		}
		else if(isKind(AT)){
			consume();
			if(isKind(AT)){
				List<KeyValueExpression> mapList  = maplist();
				fact = new MapListExpression(first, mapList);
			}
			else{
				List<Expression> elist = list();
				fact = new ListExpression(first, elist);
			}
		}
		else{
			throw new SyntaxException(t, "expected one of factor");
		}	
		return fact;
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


	public List<SyntaxException> getExceptionList() {
		// TODO Auto-generated method stub
		return null;
	}


	public String getErrors() {
		// TODO Auto-generated method stub
		return null;
	}


}
