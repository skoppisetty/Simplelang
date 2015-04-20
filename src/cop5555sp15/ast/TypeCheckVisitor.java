package cop5555sp15.ast;

import static cop5555sp15.TokenStream.Kind.AND;
import static cop5555sp15.TokenStream.Kind.BAR;
import static cop5555sp15.TokenStream.Kind.DIV;
import static cop5555sp15.TokenStream.Kind.EQUAL;
import static cop5555sp15.TokenStream.Kind.GE;
import static cop5555sp15.TokenStream.Kind.GT;
import static cop5555sp15.TokenStream.Kind.LE;
import static cop5555sp15.TokenStream.Kind.LSHIFT;
import static cop5555sp15.TokenStream.Kind.LT;
import static cop5555sp15.TokenStream.Kind.MINUS;
import static cop5555sp15.TokenStream.Kind.NOTEQUAL;
import static cop5555sp15.TokenStream.Kind.PLUS;
import static cop5555sp15.TokenStream.Kind.RSHIFT;
import static cop5555sp15.TokenStream.Kind.TIMES;

import java.util.Arrays;

import cop5555sp15.TypeConstants;
import cop5555sp15.TokenStream.Kind;
import cop5555sp15.symbolTable.SymbolTable;

public class TypeCheckVisitor implements ASTVisitor, TypeConstants {

	@SuppressWarnings("serial")
	public static class TypeCheckException extends Exception {
		ASTNode node;

		public TypeCheckException(String message, ASTNode node) {
			super(node.firstToken.lineNumber + ":" + message);
			this.node = node;
		}
	}

	SymbolTable symbolTable;
	static final Kind[] REL_OPS = { BAR, AND, EQUAL, NOTEQUAL, LT, GT, LE, GE };
	static final Kind[] WEAK_OPS = { PLUS, MINUS };
	static final Kind[] STRONG_OPS = { TIMES, DIV };
	static final Kind[] VERY_STRONG_OPS = { LSHIFT, RSHIFT };
	
	public TypeCheckVisitor(SymbolTable symbolTable) {
		this.symbolTable = symbolTable;
	}

	boolean check(boolean condition, String message, ASTNode node)
			throws TypeCheckException {
		if (condition)
			return true;
		throw new TypeCheckException(message, node);
	}

	/**
	 * Ensure that types on left and right hand side are compatible.
	 */
	@Override
	public Object visitAssignmentStatement(
			AssignmentStatement assignmentStatement, Object arg)
			throws Exception {
	
		assignmentStatement.expression.visit(this, arg);
		VarDec dec = (VarDec) symbolTable.lookup(assignmentStatement.lvalue.firstToken.getText());
		if(assignmentStatement.lvalue.visit(this, arg) == null){
			assignmentStatement.lvalue.setType(dec.type.getJVMType());
		}
		if( dec != null){
			System.out.println(assignmentStatement.lvalue.getType());
			System.out.println(assignmentStatement.expression.getType());
			if( assignmentStatement.lvalue.getType() == assignmentStatement.expression.getType()){
				return assignmentStatement.lvalue.getType();
			}
			else{
				if(assignmentStatement.lvalue.getType().equals(assignmentStatement.expression.getType()) || 
				assignmentStatement.expression.getType().equals("Ljava/util/List<>;")){
					return assignmentStatement.lvalue.getType();
				}
				else{
					throw new TypeCheckException("Type mismatch of assignment",assignmentStatement);
				}
			}
		}
		else{
			throw new TypeCheckException("Using variables without declaring",assignmentStatement);
		}
	}

	/**
	 * Ensure that both types are the same, save and return the result type
	 */
	@Override
	public Object visitBinaryExpression(BinaryExpression binaryExpression,
			Object arg) throws Exception {
		binaryExpression.expression0.visit(this, arg);
		binaryExpression.expression1.visit(this, arg);
		if(binaryExpression.expression0.getType() == binaryExpression.expression1.getType()){
			if(binaryExpression.expression0.getType() == intType){
				if(Arrays.asList(WEAK_OPS).contains(binaryExpression.op.kind) ||
				Arrays.asList(STRONG_OPS).contains(binaryExpression.op.kind)){
					binaryExpression.setType(intType);
					return intType;
				}
				else if(binaryExpression.op.kind == EQUAL || 
						binaryExpression.op.kind == NOTEQUAL ||
						binaryExpression.op.kind == LT ||
						binaryExpression.op.kind == LE ||
						binaryExpression.op.kind == GT ||
						binaryExpression.op.kind == GE ){
					binaryExpression.setType(booleanType);
					return booleanType;
				}
				else{
					throw new TypeCheckException("Int only supports +,-,* and /",binaryExpression);
				}
			}
			if(binaryExpression.expression0.getType() == stringType){
				if(binaryExpression.op.kind == PLUS){
					binaryExpression.setType(stringType);
					return stringType;
				}
				else if(binaryExpression.op.kind == EQUAL || binaryExpression.op.kind == NOTEQUAL){
					binaryExpression.setType(booleanType);
					return booleanType;
				}
				else{
					throw new TypeCheckException("String only supports + , != and ==",binaryExpression);
				}
			}
			if(binaryExpression.expression0.getType() == booleanType){
				if(binaryExpression.op.kind == EQUAL || binaryExpression.op.kind == NOTEQUAL  ||
						binaryExpression.op.kind == AND ||
						binaryExpression.op.kind == BAR ){
					binaryExpression.setType(booleanType);
					return booleanType;
				}
				else{
					throw new TypeCheckException("Boolean only supports != and ==",binaryExpression);
				}
			}
			else{
				throw new TypeCheckException("Unknown operand type",binaryExpression);
			}
			
		}
		else{
			throw new TypeCheckException("Mismatch in types of expression ",binaryExpression);
		}
	}

	/**
	 * Blocks define scopes. Check that the scope nesting level is the same at
	 * the end as at the beginning of block
	 */
	@Override
	public Object visitBlock(Block block, Object arg) throws Exception {
		int numScopes = symbolTable.enterScope();
		// visit children
		for (BlockElem elem : block.elems) {
			elem.visit(this, arg);
		}
		int numScopesExit = symbolTable.leaveScope();
		check(numScopesExit > 0 && numScopesExit == numScopes,
				"unbalanced scopes", block);
		return null;
	}

	/**
	 * Sets the expressionType to booleanType and returns it
	 * 
	 * @param booleanLitExpression
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object visitBooleanLitExpression(
			BooleanLitExpression booleanLitExpression, Object arg)
			throws Exception {
		booleanLitExpression.setType(booleanType);
		return booleanType;
	}

	/**
	 * A closure defines a new scope Visit all the declarations in the
	 * formalArgList, and all the statements in the statementList construct and
	 * set the JVMType, the argType array, and the result type
	 * 
	 * @param closure
	 * @param arg
	 * @return
	 * @throws Exception
	 */
	@Override
	public Object visitClosure(Closure closure, Object arg) throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Make sure that the name has not already been declared and insert in
	 * symbol table. Visit the closure
	 */
	@Override
	public Object visitClosureDec(ClosureDec closureDec, Object arg) {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Check that the given name is declared as a closure Check the argument
	 * types The type is the return type of the closure
	 */
	@Override
	public Object visitClosureEvalExpression(
			ClosureEvalExpression closureExpression, Object arg)
			throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitClosureExpression(ClosureExpression closureExpression,
			Object arg) throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitExpressionLValue(ExpressionLValue expressionLValue,
			Object arg) throws Exception {
		
		expressionLValue.expression.visit(this, arg);
		VarDec dec = (VarDec) symbolTable.lookup(expressionLValue.firstToken.getText());
		if(dec == null){
			throw new TypeCheckException("not declared ",expressionLValue );
		}
		else{
			if(expressionLValue.expression.getType() != intType){
				throw new TypeCheckException("Supporting only int indexes ",expressionLValue );
			}
		}
		System.out.println("lvalue exp" + dec.type.getJVMType().substring(16, dec.type.getJVMType().length()-2));
		expressionLValue.setType(dec.type.getJVMType().substring(16, dec.type.getJVMType().length()-2));
		return expressionLValue.getType();
	}

	@Override
	public Object visitExpressionStatement(
			ExpressionStatement expressionStatement, Object arg)
			throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * Check that name has been declared in scope Get its type from the
	 * declaration.
	 * 
	 */
	@Override
	public Object visitIdentExpression(IdentExpression identExpression,
			Object arg) throws Exception {
		VarDec dec = (VarDec) symbolTable.lookup(identExpression.identToken.getText());
		if(dec == null){
			throw new TypeCheckException("not declared ",identExpression );
		}
		else{
			identExpression.setType(dec.type.getJVMType());
			return identExpression.getType();
		}
	}

	@Override
	public Object visitIdentLValue(IdentLValue identLValue, Object arg)
			throws Exception {
		Declaration dec = symbolTable.lookup(identLValue.identToken.getText());
		if(dec == null){
			throw new TypeCheckException("Not declared variable",identLValue);
		}
//		identLValue.setType(identLValue.type);
		System.out.println(identLValue.type);
		return identLValue.type;
	}

	@Override
	public Object visitIfElseStatement(IfElseStatement ifElseStatement,
			Object arg) throws Exception {
		ifElseStatement.expression.visit(this, arg);
		if(ifElseStatement.expression.getType() == booleanType){
			ifElseStatement.ifBlock.visit(this, arg);
			ifElseStatement.elseBlock.visit(this, arg);
			return booleanType;
		}
		else{
			throw new TypeCheckException("guard not boolean",ifElseStatement);
		}
	}

	/**
	 * expression type is boolean
	 */
	@Override
	public Object visitIfStatement(IfStatement ifStatement, Object arg)
			throws Exception {
		ifStatement.expression.visit(this, arg);
		if(ifStatement.expression.getType() == booleanType){
			ifStatement.block.visit(this, arg);
			return booleanType;
		}
		else{
			throw new TypeCheckException("guard not boolean",ifStatement);
		}
	}

	/**
	 * expression type is int
	 */
	@Override
	public Object visitIntLitExpression(IntLitExpression intLitExpression,
			Object arg) throws Exception {
		intLitExpression.setType(intType);
		return intType;
	}

	@Override
	public Object visitKeyExpression(KeyExpression keyExpression, Object arg)
			throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitKeyValueExpression(
			KeyValueExpression keyValueExpression, Object arg) throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitKeyValueType(KeyValueType keyValueType, Object arg)
			throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	// visit the expressions (children) and ensure they are the same type
	// the return type is "Ljava/util/ArrayList<"+type0+">;" where type0 is the
	// type of elements in the list
	// this should handle lists of lists, and empty list. An empty list is
	// indicated by "Ljava/util/ArrayList;".
	@Override
	public Object visitListExpression(ListExpression listExpression, Object arg)
			throws Exception {
		String t = "empty";
		for (Expression exp : listExpression.expressionList) {
			exp.visit(this, arg);
			if(t.equals("empty")){
				t = exp.getType();
			}
			else{
				if(!t.equals(exp.getType())){
					throw new TypeCheckException("List should contain same type",listExpression);
				}
			}
			
		}
		if(t.equals("empty")){
			listExpression.setType("Ljava/util/List<>;");
			System.out.println(listExpression.getType());
			return listExpression.getType();
		}
		else{
			listExpression.setType("Ljava/util/List<"+ t + ">;");
			System.out.println(listExpression.getType());
			return listExpression.getType();
		}
		
	}

	/** gets the type from the enclosed expression */
	@Override
	public Object visitListOrMapElemExpression(
			ListOrMapElemExpression listOrMapElemExpression, Object arg)
			throws Exception {
		listOrMapElemExpression.expression.visit(this, arg);
		System.out.println(listOrMapElemExpression.expression.getType());
		VarDec dec = (VarDec) symbolTable.lookup(listOrMapElemExpression.firstToken.getText());
		if(dec == null){
			throw new TypeCheckException("not declared ",listOrMapElemExpression );
		}
		else{
			if(listOrMapElemExpression.expression.getType() != intType){
				throw new TypeCheckException("Only int index supported",listOrMapElemExpression);
			}
			
		}
		listOrMapElemExpression.setType(dec.type.getJVMType().substring(16, dec.type.getJVMType().length()-2));
		return listOrMapElemExpression.getType();
	}

	@Override
	public Object visitListType(ListType listType, Object arg) throws Exception {
		listType.type.visit(this, arg);
		return listType.type.getJVMType();
	}

	@Override
	public Object visitMapListExpression(MapListExpression mapListExpression,
			Object arg) throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitPrintStatement(PrintStatement printStatement, Object arg)
			throws Exception {
		printStatement.expression.visit(this, null);
		return null;
	}

	@Override
	public Object visitProgram(Program program, Object arg) throws Exception {
		if (arg == null) {
			program.JVMName = program.name;
		} else {
			program.JVMName = arg + "/" + program.name;
		}
		// ignore the import statement
		if (!symbolTable.insert(program.name, null)) {
			throw new TypeCheckException("name already in symbol table",
					program);
		}
		program.block.visit(this, true);
		return null;
	}

	@Override
	public Object visitQualifiedName(QualifiedName qualifiedName, Object arg) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Checks that both expressions have type int.
	 * 
	 * Note that in spite of the name, this is not in the Expression type
	 * hierarchy.
	 */
	@Override
	public Object visitRangeExpression(RangeExpression rangeExpression,
			Object arg) throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	// nothing to do here
	@Override
	public Object visitReturnStatement(ReturnStatement returnStatement,
			Object arg) throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitSimpleType(SimpleType simpleType, Object arg)
			throws Exception {
		return simpleType.type.getText();		
	}

	@Override
	public Object visitSizeExpression(SizeExpression sizeExpression, Object arg)
			throws Exception {
		sizeExpression.expression.visit(this, arg);
		return intType;
	}

	@Override
	public Object visitStringLitExpression(
			StringLitExpression stringLitExpression, Object arg)
			throws Exception {
		stringLitExpression.setType(stringType);
		return stringType;
	}

	/**
	 * if ! and boolean, then boolean else if - and int, then int else error
	 */
	@Override
	public Object visitUnaryExpression(UnaryExpression unaryExpression,
			Object arg) throws Exception {
		unaryExpression.expression.visit(this, arg);		
		if(unaryExpression.expression.getType() == intType){
			unaryExpression.setType(intType);
			return intType;
		}
		else if(unaryExpression.expression.getType() == booleanType){
			unaryExpression.setType(booleanType);
			return booleanType;
		}
		else{
			throw new TypeCheckException("Unknown operand type",unaryExpression);
		}
	}

	@Override
	public Object visitUndeclaredType(UndeclaredType undeclaredType, Object arg)
			throws Exception {
		throw new UnsupportedOperationException(
				"undeclared types not supported");
	}

	@Override
	public Object visitValueExpression(ValueExpression valueExpression,
			Object arg) throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	/**
	 * check that this variable has not already been declared in the same scope.
	 */
	@Override
	public Object visitVarDec(VarDec varDec, Object arg) throws Exception {
		
		varDec.type.visit(this, arg);
		Declaration dec1 = symbolTable.lookup(varDec.identToken.getText());
		if(dec1 == null){
			symbolTable.insert(varDec.identToken.getText(),varDec);
		}
		else{
			throw new TypeCheckException("Already declared "+ varDec.identToken.getText() ,varDec);
		}
		return varDec.type.getJVMType();
	}

	/**
	 * All checking will be done in the children since grammar ensures that the
	 * rangeExpression is a rangeExpression.
	 */
	@Override
	public Object visitWhileRangeStatement(
			WhileRangeStatement whileRangeStatement, Object arg)
			throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");

	}

	@Override
	public Object visitWhileStarStatement(
			WhileStarStatement whileStarStatement, Object arg) throws Exception {
		System.out.println("Unimplemented"); throw new UnsupportedOperationException("not yet implemented");
	}

	@Override
	public Object visitWhileStatement(WhileStatement whileStatement, Object arg)
			throws Exception {
		whileStatement.expression.visit(this, arg);
		if(whileStatement.expression.getType() == booleanType){
			whileStatement.block.visit(this, arg);
			return booleanType;
		}
		else{
			throw new TypeCheckException("Guard not boolean",whileStatement);
		}
	}

}
