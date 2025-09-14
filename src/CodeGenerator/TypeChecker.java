package CodeGenerator;

import Tuga.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import java.util.*;

public class TypeChecker extends TugaBaseVisitor<String> {

	//Types
	static final String INT = "inteiro";
	static final String REAL = "real";
	static final String STRING = "string";
	static final String BOOLEAN = "booleano";
	static final String VOID = "vazio"; // void function type
	private boolean showErrors = true;

	private Scope currentScope;
	private String currentFuncType = VOID; // starts void because first one will always be void (principal)

	private final Identificator identificator;

	//Map to store the type of each expression
	private Map<TugaParser.ExprContext, String> typeMap = new HashMap<TugaParser.ExprContext, String>();

	public TypeChecker(Identificator identificator) {
		this.identificator = identificator;
	}

	//Does normal visit but without showing normal errors
	public void visitWithoutErrors(ParseTree tree){
		this.showErrors = false;
		this.visit(tree);
	}

	//Method to use with CodeGen
	public String getType(TugaParser.ExprContext ctx) {
        return typeMap.get(ctx);
	}

	private static boolean findReturnInsideIfElse(TugaParser.IfElseContext ctx){
		boolean containsElse = ctx.stat(1) != null;	// verifies if there is else
		// if there is no else we can't count it as return because we don't know if it will go inside if body
		if(!containsElse){
			return false;
		}
		// if there is else
		boolean foundIf = false;
		boolean foundElse = false;
		ParserRuleContext statIf = ctx.stat(0); // if body
		ParserRuleContext statElse = ctx.stat(1); // else body
		if(statIf instanceof TugaParser.BlockContext){
			foundIf = findReturn(((TugaParser.BlockContext) statIf).blck());
		}
		else if(statIf instanceof TugaParser.IfElseContext){
			foundIf = findReturnInsideIfElse((TugaParser.IfElseContext) statIf);
		}
		else if(statIf instanceof TugaParser.ReturnContext){
			foundIf = true;
		}

		if(statElse instanceof TugaParser.BlockContext){
			foundElse = findReturn(((TugaParser.BlockContext) statElse).blck());
		}
		else if(statElse instanceof TugaParser.IfElseContext){
			foundElse = findReturnInsideIfElse((TugaParser.IfElseContext) statElse);
		}
		else if(statElse instanceof TugaParser.ReturnContext){
			foundElse = true;
		}

		return foundIf && foundElse;
	}

	// Recursively searches for return statement inside blocks of blocks
	private static boolean findReturn(TugaParser.BlckContext ctx){
		boolean found = false;
		for(ParserRuleContext stat : ctx.stat()){
			if(stat instanceof TugaParser.BlockContext){
				found = findReturn(((TugaParser.BlockContext) stat).blck());
			}
			else if(stat instanceof TugaParser.IfElseContext){
				found = findReturnInsideIfElse((TugaParser.IfElseContext) stat);
			}
			else if(stat instanceof TugaParser.ReturnContext){
				found = true;
			}
			if(found){
				break;
			}
		}
		return found;
	}

	@Override
	public String visitProg(TugaParser.ProgContext ctx){
		this.currentScope = identificator.scopes.get(ctx); // set current scope to global scope in the beginning of the program
		visitChildren(ctx);
		if(identificator.hasErrors()){
			identificator.printErrors();
		}
		return null;
	}

	@Override
	public String visitFunc(TugaParser.FuncContext ctx){
		// we don't want to visit a function that has already given an error
		if(!identificator.isFlagged(ctx)) {
			String funcName = ctx.VARNAME().getText();
			String funcType = this.currentScope.getType(funcName);
			String oldType = this.currentFuncType;
			this.currentScope = identificator.scopes.get(ctx); // enter function scope
			this.currentFuncType = funcType;
			visitChildren(ctx);
			// we should also check if there is a return in the block statements
			// if the function is of type void, then we don't need to check, because it can have return or not
			// if the function is not of type void, we need to check if there is a return at the end
			// it doesn't matter if there are returns in the middle with conditions and whatnot, because it only needs to at least have one in the end
			// NOTE: THIS IS HOW I THOUGHT IT WAS, BUT IT TURNS OUT THE TEACHER ONLY WANTS TO SEE IF THERE IS AT LEAST ONE RETURN INSIDE THE FUNCTION NO MATTER WHERE (SEE EXAMPLE D)
			// I WILL LEAVE IT COMMENTED THOUGH, AS A WAY OF DOCUMENTING WHAT I'VE DONE
			if(!funcType.equals(VOID)){
				if(!findReturn(ctx.blck())){
					Token t = ctx.getStart();
					int line = t.getLine();
					String errorMsg = "erro na linha " + line + ": funcao '" + funcName +"' precisa de retornar algum valor";
					identificator.errors[line-1] = errorMsg;
				}
				/*
				ParserRuleContext lastStat = ctx.blck().stat().getLast();
				if(!(lastStat instanceof TugaParser.ReturnContext)){
					Token t = ctx.getStart();
					int line = t.getLine();
					String errorMsg = "erro na linha " + line + ": funcao '" + funcName +"' precisa de retornar algum valor";
					identificator.errors[line-1] = errorMsg;
				}
				*/
			}
			this.currentScope = this.currentScope.previous; // exit function scope
			this.currentFuncType = oldType;
		}
		return null;
	}

	@Override
	public String visitReturn(TugaParser.ReturnContext ctx){
		String exprType;
		if(ctx.expr() == null){
			exprType = VOID;
		}
		else{
			exprType = visit(ctx.expr());
		}

		if(exprType == null){
			return null;
		}

		if(!exprType.equals(this.currentFuncType) && !(exprType.equals(INT) && this.currentFuncType.equals(REAL))){
			Token t = ctx.getStart();
			int line = t.getLine();
			String errorMsg = "erro na linha " + line + ": funcao do tipo " + this.currentFuncType +" nao pode retornar valor do tipo " + exprType;
			identificator.errors[line-1] = errorMsg;
		}
		return null;
	}

	// this is normal block without function
	@Override
	public String visitBlock(TugaParser.BlockContext ctx){
		// block by itself never gives error
		this.currentScope = identificator.scopes.get(ctx); // change scope to local scope of independent block
		visitChildren(ctx);
		this.currentScope = this.currentScope.previous; // exit block scope
		return null;
	}

	// this will verify the argument types and function type
	// here only void functions should be called
	@Override
	public String visitStatFunctionCall(TugaParser.StatFunctionCallContext ctx){
		// skip function if ctx is flagged
		if(identificator.isFlagged(ctx)){
			return null;
		}
		// if any of the arguments has errors, then we skip function
		for(ParserRuleContext exprCtx : ctx.expr()){
			if(identificator.isFlagged(exprCtx)){
				return null;
			}
		}
		String funcName = ctx.VARNAME().getText();
		String funcType = currentScope.getType(funcName);
		if(!funcType.equals(VOID)){
			Token t = ctx.getStart();
			int line = t.getLine();
			// no need to check if it's flagged or not because if it was flagged it wouldn't be here
			String errorMsg = "erro na linha " + line + ": valor de '" + funcName +"' tem de ser atribuido a uma variavel";
			identificator.errors[line-1] = errorMsg;
		}
		// argument error checking (if there are any)
		else if(!ctx.expr().isEmpty()){
			// first get all types of given args
			ArrayList<String> givenArgTypes = new ArrayList<>();
			for(TugaParser.ExprContext givenArg : ctx.expr()){
				givenArgTypes.add(visit(givenArg));
			}
			// now get all types that should match the given ones
			ArrayList<String> correctArgTypes = currentScope.getArgTypes(funcName);
			// now we verify if they are the same type and give specific errors
			for(int i = 0; i < currentScope.getNArgs(funcName); i++){
				String givenArgName = ctx.expr(i).getText();
				String givenArgType = givenArgTypes.get(i);
				String correctArgType = correctArgTypes.get(i);
				// don't forget the case where an argument of type REAL is called with INT (this is allowed!)
				if(!givenArgType.equals(correctArgType) && !(givenArgType.equals(INT) && correctArgType.equals(REAL))){
					Token t = ctx.getStart();
					int line = t.getLine();
					// no need to check if it's flagged or not because if it was flagged it wouldn't be here
					String errorMsg = "erro na linha " + line + ": '" + givenArgName +"' devia ser do tipo " + correctArgType;
					identificator.errors[line-1] = errorMsg;
				}
			}
		}
		// no need to return type because this is a statement (only to call a function)
		return null;
	}

	// this will verify the argument types and function type
	// here only non-void functions should be called
	@Override
	public String visitExprFunctionCall(TugaParser.ExprFunctionCallContext ctx){
		// skip function if ctx is flagged
		if(identificator.isFlagged(ctx)){
			return null;
		}
		// if any of the arguments has errors, then we skip function
		for(ParserRuleContext exprCtx : ctx.expr()){
			if(identificator.isFlagged(exprCtx)){
				return null;
			}
		}
		String funcName = ctx.VARNAME().getText();
		String funcType = currentScope.getType(funcName);
		// I THOUGHT WE HAD TO CHECK THIS BUT APPARENTLY WE SHOULD DO THIS IN EVERY SPECIFIC EXPR INSTEAD OF GENERAL ERROR
		// if(funcType.equals(VOID)){
			// Token t = ctx.getStart();
			// int line = t.getLine();
			// if there still isn't an error on this line we can add one
			// no need to check if it's flagged or not because if it was flagged it wouldn't be here
			// String errorMsg = "erro na linha " + line + ": valor de '" + funcName +"' nao pode ser atribuido a uma variavel";
			// identificator.errors[line-1] = errorMsg;
		// }
		// argument error checking (if there are any)
		if(!ctx.expr().isEmpty()){
			// first get all types of given args
			ArrayList<String> givenArgTypes = new ArrayList<>();
			for(TugaParser.ExprContext givenArg : ctx.expr()){
				givenArgTypes.add(visit(givenArg));
			}
			// now get all types that should match the given ones
			ArrayList<String> correctArgTypes = currentScope.getArgTypes(funcName);
			// now we verify if they are the same type and give specific errors
			for(int i = 0; i < currentScope.getNArgs(funcName); i++){
				String givenArgName = ctx.expr(i).getText();
				String givenArgType = givenArgTypes.get(i);
				String correctArgType = correctArgTypes.get(i);
				// don't forget the case where an argument of type REAL is called with INT (this is allowed!)
				if(!givenArgType.equals(correctArgType) && !(givenArgType.equals(INT) && correctArgType.equals(REAL))){
					Token t = ctx.getStart();
					int line = t.getLine();
					// if there still isn't an error on this line we can add one
					if(identificator.errors[line-1] == null){
						String errorMsg = "erro na linha " + line + ": '" + givenArgName +"' devia ser do tipo " + correctArgType;
						identificator.errors[line-1] = errorMsg;
					}
				}
			}
		}
		// we need to specify which type this is because this can be used in expressions
		typeMap.put(ctx, funcType);
		return funcType;
	}

	@Override
	public String visitWrite(TugaParser.WriteContext ctx){
		String exprType = visit(ctx.expr());
		// if exprType is null means that an error was found and we should skip this
		if(exprType == null){
			return null;
		}
		else if(exprType.equals(VOID)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": nao se pode escrever expressoes do tipo vazio";
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		typeMap.put(ctx.expr(), exprType);
		return null;
	}

	@Override
	public String visitWhile(TugaParser.WhileContext ctx){
		String exprType = visit(ctx.expr());
		// if exprType is null means that an error was found and we should skip this
		if(exprType == null){
			return null;
		}
		if(!exprType.equals(BOOLEAN)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": expressao de 'enquanto' nao eh do tipo booleano";
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		else{
			visit(ctx.stat());
		}
		return null;
	}

	@Override
	public String visitIfElse(TugaParser.IfElseContext ctx){
		String exprType = visit(ctx.expr());
		// if exprType is null means that an error was found and we should skip this
		if(exprType == null){
			return null;
		}
		if(!exprType.equals(BOOLEAN)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": expressao de 'se' nao eh do tipo booleano";
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		else{
			for(TugaParser.StatContext stat : ctx.stat()){
				visit(stat);
			}
		}
		return null;
	}

	@Override
	public String visitAffect(TugaParser.AffectContext ctx){
		// skip function if ctx is flagged
		if(identificator.isFlagged(ctx)){
			return null;
		}
		String varType = this.currentScope.getType(ctx.VARNAME().getText());
		String exprType = visit(ctx.expr());
		// if exprType is null means that an error was found and we should skip this
		if(exprType == null){
			return null;
		}
		// should give error if they are different, with the exception of being able to pass int into real
		if(!exprType.equals(varType) && !(varType.equals(REAL) && exprType.equals(INT))){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '<-' eh invalido entre " + varType + " e " + exprType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		return null;
	}

	@Override
	public String visitEmpty(TugaParser.EmptyContext ctx){
		return null;
	}

	@Override
	public String visitUminusUnot(TugaParser.UminusUnotContext ctx) {
		String exprType = visit(ctx.expr());
		// if exprType is null means that an error was found and we should skip this
		if(exprType == null){
			return null;
		}

		// new verification to check if type is void
		if(exprType.equals(VOID)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador unario '-' eh invalido para " + exprType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			return null;
		}

		if (ctx.op.getText().equals("-") && (!exprType.equals(INT) && !exprType.equals(REAL))) {
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador unario '-' eh invalido para " + exprType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		else if(ctx.op.getText().equals("nao") && !exprType.equals(BOOLEAN)) {
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador unario 'nao' eh invalido para " + exprType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		typeMap.put(ctx, exprType);
		return exprType;
	}

	@Override
	public String visitMultDivMod(TugaParser.MultDivModContext ctx) {
		String leftType = visit(ctx.expr(0));
		String rightType = visit(ctx.expr(1));

		// if leftType or rightType is null means that an error was found and we should skip this
		if(leftType == null || rightType == null){
			return null;
		}

		if(leftType.equals(VOID) || rightType.equals(VOID)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
			return null;
		}

		String thisType = null;

		// If it has either boolean or string we can't do any operation
		if (leftType.equals(BOOLEAN) || rightType.equals(BOOLEAN) ||
				leftType.equals(STRING) || rightType.equals(STRING)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		// Case for mult or div
		else if (!ctx.op.getText().equals("%")) {
			if (leftType.equals(REAL) || rightType.equals(REAL)) {
				thisType = REAL;
			}
			else{
				thisType = INT;
			}
		}
		// Case for mod
		else{
			if (leftType.equals(INT) && rightType.equals(INT)) {
				thisType = INT;
			}
			else {
				if(this.showErrors){
					Token t = ctx.getStart();
					int line = t.getLine();
					// if there still isn't an error on this line we can add one
					if(identificator.errors[line-1] == null){
						String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
						identificator.errors[line-1] = errorMsg;
					}
				}
				else{
					giveSimpleTypeCheckingError();
				}
			}
		}

		typeMap.put(ctx, thisType);
		return thisType;
	}

	@Override
	public String visitAddSub(TugaParser.AddSubContext ctx) {
		String leftType = visit(ctx.expr(0));
		String rightType = visit(ctx.expr(1));

		// if leftType or rightType is null means that an error was found and we should skip this
		if(leftType == null || rightType == null){
			return null;
		}

		if(leftType.equals(VOID) || rightType.equals(VOID)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
			return null;
		}

		String thisType = null;

		// If both are boolean operations or if one of them is void
		if((leftType.equals(BOOLEAN) && rightType.equals(BOOLEAN)) || (leftType.equals(VOID) || rightType.equals(VOID))){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		// Will only have to concat if at least one is string
		else if(leftType.equals(STRING) || rightType.equals(STRING)){
			// Can only concat if it's +
			if(ctx.op.getText().equals("+")){
				thisType = STRING;
			}
			else{
				if(this.showErrors){
					Token t = ctx.getStart();
					int line = t.getLine();
					// if there still isn't an error on this line we can add one
					if(identificator.errors[line-1] == null){
						String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
						identificator.errors[line-1] = errorMsg;
					}
				}
				else{
					giveSimpleTypeCheckingError();
				}
			}
		}
		// only + and - operations remain, so any of them is boolean its a type check error
		else if(leftType.equals(BOOLEAN) || rightType.equals(BOOLEAN)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}
		// Now will check for all + and - left
		// If at least one of them is real, this will be real type
		else if((leftType.equals(REAL) || rightType.equals(REAL))){
			thisType = REAL;
		}
		// Only other case is INT, which is when both are INT
		else{
			thisType = INT;
		}
		typeMap.put(ctx, thisType);
		return thisType;
	}

	@Override
	public String visitLessGrtr(TugaParser.LessGrtrContext ctx) {
		// Valid boolean comparisons excluding equals and different
		// Int - Int; Int - Real; Real - Int; Real - Real
		String leftType = visit(ctx.expr(0));
		String rightType = visit(ctx.expr(1));

		// if leftType or rightType is null means that an error was found and we should skip this
		if(leftType == null || rightType == null){
			return null;
		}

		if(leftType.equals(VOID) || rightType.equals(VOID)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
			return null;
		}

		String thisType = null;

		// This condition takes care of any case
		if ((leftType.equals(REAL) && rightType.equals(INT)) || (leftType.equals(INT) && rightType.equals(REAL))
				|| (leftType.equals(INT) && rightType.equals(INT)) || (leftType.equals(REAL) && rightType.equals(REAL))) {
			String op = ctx.op.getText();
			if(op.equals("<") || op.equals("<=") || op.equals(">") || op.equals(">=")){
				thisType = BOOLEAN;
			}
			else{
				if(this.showErrors){
					Token t = ctx.getStart();
					int line = t.getLine();
					// if there still isn't an error on this line we can add one
					if(identificator.errors[line-1] == null){
						String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
						identificator.errors[line-1] = errorMsg;
					}
				}
				else{
					giveSimpleTypeCheckingError();
				}
			}
		} else {
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}

		typeMap.put(ctx, thisType);
		return thisType;
	}

	@Override
	public String visitEqDiff(TugaParser.EqDiffContext ctx) {
		// Valid comparisons
		// Int - Int; Int - Real; Real - Int; Real - Real; Boolean - Boolean; String - String
		String leftType = visit(ctx.expr(0));
		String rightType = visit(ctx.expr(1));

		// if leftType or rightType is null means that an error was found and we should skip this
		if(leftType == null || rightType == null){
			return null;
		}

		if(leftType.equals(VOID) || rightType.equals(VOID)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
			return null;
		}

		String thisType = null;

		// This condition takes care of any case
		if (leftType.equals(rightType) || (leftType.equals(REAL) && rightType.equals(INT)) || (leftType.equals(INT) && rightType.equals(REAL))) {
			thisType = BOOLEAN;

		} else {
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador '" + ctx.op.getText() + "' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}

		typeMap.put(ctx, thisType);
		return thisType;
	}

	@Override
	public String visitAnd(TugaParser.AndContext ctx) {
		String leftType = visit(ctx.expr(0));
		String rightType = visit(ctx.expr(1));

		// if leftType or rightType is null means that an error was found and we should skip this
		if(leftType == null || rightType == null){
			return null;
		}

		if(leftType.equals(VOID) || rightType.equals(VOID)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador 'e' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
			return null;
		}

		String thisType = null;

		if (leftType.equals(BOOLEAN) && rightType.equals(BOOLEAN)) {
			thisType = BOOLEAN;
		} else {
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador 'e' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}

		typeMap.put(ctx, thisType);
		return thisType;
	}

	@Override
	public String visitOr(TugaParser.OrContext ctx) {
		String leftType = visit(ctx.expr(0));
		String rightType = visit(ctx.expr(1));

		// if leftType or rightType is null means that an error was found and we should skip this
		if(leftType == null || rightType == null){
			return null;
		}

		if(leftType.equals(VOID) || rightType.equals(VOID)){
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador 'ou' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
			return null;
		}

		String thisType = null;

		if (leftType.equals(BOOLEAN) && rightType.equals(BOOLEAN)) {
			thisType = BOOLEAN;
		} else {
			if(this.showErrors){
				Token t = ctx.getStart();
				int line = t.getLine();
				// if there still isn't an error on this line we can add one
				if(identificator.errors[line-1] == null){
					String errorMsg = "erro na linha " + line + ": operador 'ou' eh invalido entre " + leftType + " e " + rightType;
					identificator.errors[line-1] = errorMsg;
				}
			}
			else{
				giveSimpleTypeCheckingError();
			}
		}

		typeMap.put(ctx, thisType);
		return thisType;
	}

	@Override
	public String visitVar(TugaParser.VarContext ctx){
		if(identificator.isFlagged(ctx)){
			return null;
		}
		String type = this.currentScope.getType(ctx.getText());
		typeMap.put(ctx, type);
		return type;
	}

	@Override
	public String visitInt(TugaParser.IntContext ctx) {
		typeMap.put(ctx, INT);
		return INT;
	}

	@Override
	public String visitReal(TugaParser.RealContext ctx) {
		typeMap.put(ctx, REAL);
		return REAL;
	}

	@Override
	public String visitString(TugaParser.StringContext ctx) {
		typeMap.put(ctx, STRING);
		return STRING;
	}

	@Override
	public String visitBoolean(TugaParser.BooleanContext ctx) {
		typeMap.put(ctx, BOOLEAN);
		return BOOLEAN;
	}

	@Override
	public String visitParens(TugaParser.ParensContext ctx) {
		String exprType = visit(ctx.expr());
		typeMap.put(ctx, exprType);
		return exprType;
	}

	// Gives type checking error and exits the program
	private static void giveSimpleTypeCheckingError(){
		System.out.println("Input has type checking errors");
		System.exit(0);
	}
}