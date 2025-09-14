package CodeGenerator;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;
import Tuga.*;
import VM.OpCode;
import VM.Instruction.*;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.TerminalNode;

public class CodeGen extends TugaBaseVisitor<Void> {
    /*
        Basicaly, in this CodeGen we will have to implement the visit methods for the rules we defined on Tuga.g4
        However, we for example on the AddSub, will have to emit either the iadd/isub or dadd/dsub depending on the type of the operands
        Therefore, even though this is not where the type checking is done, we will have to have some sort of type checking to know which emit to call
    */

    // The constant pool - doubles, strings, booleans
    // They need to be all on the same pool because the indexes are used to reference the constants

    private final ArrayList<Object> constantPool = new ArrayList<>();
    private final TypeChecker typeChecker;
    private final Identificator identificator;
    private Scope globalScope;
    private Scope currentScope;
    // currentFuncType is useful to know which function type we are in so that we know what to return
    private String currentFuncType = VOID; // starts void because first one will always be void (principal)
    private String currentFuncName = "principal"; // starts principal because that's the name of first one
    static final String INT = "inteiro";
    static final String REAL = "real";
    static final String STRING = "string";
    static final String BOOLEAN = "booleano";
    static final String VOID = "vazio";

    // the target code
    private final ArrayList<Instruction> code = new ArrayList<>();

    private HashMap<Integer, String> funcAddresses = new HashMap<>();
    // This hashmap will save which function the command call is trying to call
    // with the addr of call in code as the Key, and name of the function it wants to call as Value
    static final int UNDEFINED = -10000;

    public CodeGen(TypeChecker typeChecker, Identificator identificator) {
        this.typeChecker = typeChecker;
        this.identificator = identificator;
    }

    // Function to check or add if necessary a constant to the constant pool
    // Returns the index of the constant in the constant pool
    public int poolConstantIndex(Object constant){
        int i = constantPool.indexOf(constant);
        if(i == -1){
            constantPool.add(constant);
            i = constantPool.size() - 1;
        }
        return i;
    }

    // here we will set all the addresses correctly into the call addresses in code
    private void setCallAddresses(){
        for(int i = 0; i < this.code.size(); i++){
            if(funcAddresses.containsKey(i)){
                String funcName = funcAddresses.get(i);
                int addr = globalScope.getAddr(funcName);
                this.code.set(i, new Instruction1Arg(OpCode.call, addr));
            }
        }
    }

    @Override public Void visitProg(TugaParser.ProgContext ctx){
        this.globalScope = identificator.global;
        this.currentScope = globalScope;
        visitChildren(ctx);
        this.setCallAddresses();
        // halt in second position
        this.code.addFirst(new Instruction(OpCode.halt));
        // call function principal in first position
        int addr = this.currentScope.getAddr("principal");    // we can do this without worries because in previous passages on the tree we verified that it exists
        this.code.addFirst(new Instruction1Arg(OpCode.call, addr));
        return null;
    }

    @Override
    public Void visitDecl(TugaParser.DeclContext ctx){
        int n = ctx.VARNAME().size();
        // Allocate space in globals array for n variables
        // or allocate space for local variables
        if(this.currentScope == this.globalScope){
            emit(OpCode.galloc, n);
        }
        else{
            emit(OpCode.lalloc, n);
        }
        return null;
    }

    @Override
    public Void visitArgdecl(TugaParser.ArgdeclContext ctx){
        // don't really need to do anything because everything here has already been verified, and just declaring
        // what arguments a function has doesn't require any code instructions
        // all the args that will be declared will be later done on function calls
        return null;
    }

    @Override
    public Void visitFunc(TugaParser.FuncContext ctx){
        // save addr of start of function
        String funcName = ctx.VARNAME().getText();
        String funcType = this.currentScope.getType(funcName);
        String oldType = this.currentFuncType;
        String oldName = this.currentFuncName;
        this.currentScope.symbolTable.setFuncAddr(funcName, this.code.size()+2); // addr will be next position of code array +2 because of the 2 instructions added in the beginning at the end

        this.currentScope = identificator.scopes.get(ctx); // enter function scope
        this.currentFuncType = funcType;
        this.currentFuncName = funcName;
        // only need to visit block
        visit(ctx.blck());
        this.currentScope = this.currentScope.previous; // exit function scope

        // in case a function of type void having no return at the end, we should emit ret from here
        ParserRuleContext lastStat = ctx.blck().stat().getLast();
        // IN TEACHER EXAMPLES THERE ARE SOME POP SITUATIONS HAPPENING IN FUNCTIONS BUT I'M NOT SURE IF YOU NEED IT
        // BECAUSE IM DOING IT IN THE VM AUTOMATICALLY SO WE'LL NEED TO SEE IF WE NEED TO CHANGE THAT
        if(funcType.equals(VOID) && !(lastStat instanceof TugaParser.ReturnContext)){
            int nArgs = this.currentScope.getNArgs(funcName);
            emit(OpCode.ret, nArgs);
        }

        this.currentFuncType = oldType;
        this.currentFuncName = oldName;
        return null;
    }

    @Override
    public Void visitReturn(TugaParser.ReturnContext ctx){
        if(ctx.expr() != null){
            visit(ctx.expr());
        }
        int nArgs = this.currentScope.getNArgs(currentFuncName);;
        if(currentFuncType.equals(VOID)){
            emit(OpCode.ret, nArgs);
        }
        else{
            String exprType = typeChecker.getType(ctx.expr());
            if(currentFuncType.equals(REAL) && exprType.equals(INT)){
                emit(OpCode.itod);
            }
            emit(OpCode.retval, nArgs);
        }
        return null;
    }

    @Override
    public Void visitBlock(TugaParser.BlockContext ctx){
        this.currentScope = identificator.scopes.get(ctx);
        int nLocalVars = currentScope.getNLocalVars();
        visitChildren(ctx);
        // before exiting current scope, we need to pop all local variables from the stack
        emit(OpCode.pop, nLocalVars);
        this.currentScope = this.currentScope.previous;
        return null;
    }

    @Override
    public Void visitStatFunctionCall(TugaParser.StatFunctionCallContext ctx){
        // first we need to visit the arguments and do itod if it's a int and should be double
        String funcName = ctx.VARNAME().getText();
        int funcNArgs = currentScope.getNArgs(funcName);
        ArrayList<String> argTypes = currentScope.getArgTypes(funcName);
        for(int i = 0; i < funcNArgs; i++){
            String exprType = typeChecker.getType(ctx.expr(i));
            String argType = argTypes.get(i);
            visit(ctx.expr(i)); // visit
            // itod if necessary
            if(exprType.equals(INT) && argType.equals(REAL)){
                emit(OpCode.itod);
            }
        }
        // then we need to call the function
        funcAddresses.put(code.size(), funcName); // save where call will be
        emit(OpCode.call, UNDEFINED); // save call with temporary undefined address
        return null;
    }

    @Override
    public Void visitExprFunctionCall(TugaParser.ExprFunctionCallContext ctx){
        // first we need to visit the arguments and do itod if it's a int and should be double
        String funcName = ctx.VARNAME().getText();
        int funcNArgs = currentScope.getNArgs(funcName);
        ArrayList<String> argTypes = currentScope.getArgTypes(funcName);
        for(int i = 0; i < funcNArgs; i++){
            String exprType = typeChecker.getType(ctx.expr(i));
            String argType = argTypes.get(i);
            visit(ctx.expr(i)); // visit
            // itod if necessary
            if(exprType.equals(INT) && argType.equals(REAL)){
                emit(OpCode.itod);
            }
        }
        // then we need to call the function
        funcAddresses.put(code.size(), funcName); // save where call will be
        emit(OpCode.call, UNDEFINED); // save call with temporary undefined address
        // after that, the value of the return should be on top of the stack!
        return null;
    }

    // WRITE expr PTCOMMA                             # Write
    @Override public Void visitWrite(TugaParser.WriteContext ctx){
        String type = typeChecker.getType(ctx.expr());
        if(type.equals(INT)){
            visit(ctx.expr());
            emit(OpCode.iprint);
        }
        else if(type.equals(REAL)){
            visit(ctx.expr());
            emit(OpCode.dprint);
        }
        else if(type.equals(STRING)){
            visit(ctx.expr());
            emit(OpCode.sprint);
        }
        else if(type.equals(BOOLEAN)){
            visit(ctx.expr());
            emit(OpCode.bprint);
        }
        else{
            System.out.println("Compiler error on Stat");
            System.exit(0);
        }
        return null;
    }

    // WHILE LPAREN expr RPAREN stat                  # While
    @Override
    public Void visitWhile(TugaParser.WhileContext ctx){
        int whileStartAddr = this.code.size();  // while starts in this position
        visit(ctx.expr());
        int jumpFAddr = this.code.size();   // addr of jumpf instruction
        emit(OpCode.jumpf, -1); // set jumpf with temporary value
        visit(ctx.stat());
        emit(OpCode.jump, whileStartAddr+2); // set pointer to beginning of while after generating code for while body (+2 is because of instructions at the end)
        // backpatch (goes back and replaces the temporary addr value of the jumpf instruction with the real one)
        this.code.set(jumpFAddr, new Instruction1Arg(OpCode.jumpf, this.code.size()+2)); // (+2 is because of instructions at the end)
        // this way, if the expr is false, it will jump to the end of the while
        // and if it's true, it will run the body and jump back up to the beginning of while
        return null;
    }

    ///*
    // IF LPAREN expr RPAREN stat (ELSE stat)?        # IfElse
    @Override
    public Void visitIfElse(TugaParser.IfElseContext ctx){
        visit(ctx.expr());
        int jumpFAddr = this.code.size();
        emit(OpCode.jumpf, -1); // will want to jump to start of else or to end of if (if there is no else statement)
        visit(ctx.stat(0));
        // if there is an else statement
        if(ctx.stat(1) != null){
            int ifEndAddr = this.code.size();
            emit(OpCode.jump, -1);  // if if is true, we will want to jump over else
            int elseStartAddr = this.code.size();
            visit(ctx.stat(1));
            this.code.set(jumpFAddr, new Instruction1Arg(OpCode.jumpf, elseStartAddr + 2)); // (+2 is because of instructions at the end)
            this.code.set(ifEndAddr, new Instruction1Arg(OpCode.jump, this.code.size() + 2)); // (+2 is because of instructions at the end)
        }
        else{
            this.code.set(jumpFAddr, new Instruction1Arg(OpCode.jumpf, this.code.size() + 2)); // (+2 is because of instructions at the end)
        }
        return null;
    }


    // VARNAME AFFECT expr PTCOMMA                    # Affect
    @Override
    public Void visitAffect(TugaParser.AffectContext ctx){
        String symbol = ctx.VARNAME().getText();
        String varType = currentScope.getType(symbol);
        String exprType = typeChecker.getType(ctx.expr());
        visitChildren(ctx);
        if(varType.equals(REAL) && exprType.equals(INT)){
            emit(OpCode.itod);
        }
        // case where it's a global variable so you just need to store the value in it's addr
        if(currentScope.equals(globalScope)){
            int addr = currentScope.getAddr(symbol);
            emit(OpCode.gstore, addr);
        }
        // if it isn't global variable
        else{
            int addr = currentScope.getAddr(symbol);
            emit(OpCode.lstore, addr);
        }
        return null;
    }

    // PTCOMMA                                        # Empty
    @Override
    public Void visitEmpty(TugaParser.EmptyContext ctx){
        return null;
    }

    // op=(MINUS|NOT) expr                            # UminusUnot
    // Uminus can be int or real and Unot is for boolean only
    @Override public Void visitUminusUnot(TugaParser.UminusUnotContext ctx) {
        String type = typeChecker.getType(ctx.expr());
        if(ctx.op.getText().equals("nao")){
            if(type.equals(BOOLEAN)){
                visit(ctx.expr());
                emit(OpCode.not);
            }
            else{
                System.out.println("Compiler error on Unot");
                System.exit(0);
            }
        }
        else if(ctx.op.getText().equals("-")){
            if (type.equals(INT)) {
                visit(ctx.expr());
                emit(OpCode.iuminus);
            } else if (type.equals(REAL)) {
                visit(ctx.expr());
                emit(OpCode.duminus);
            } else {
                System.out.println("Compiler error on Uminus");
                System.exit(0);
            }
        }
        return null;
    }

    // expr op=(MULT|DIV|MOD) expr                    # MultDivMod
    // MultDivMod can be imult/idiv/imod or dmult/ddiv
    @Override public Void visitMultDivMod(TugaParser.MultDivModContext ctx) {
        String leftType = typeChecker.getType(ctx.expr(0));
        String rightType = typeChecker.getType(ctx.expr(1));

        if (leftType.equals(INT) && rightType.equals(INT)) {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            if (ctx.op.getText().equals("*")) {
                emit(OpCode.imult);
            } else if (ctx.op.getText().equals("/")) {
                emit(OpCode.idiv);
            } else if (ctx.op.getText().equals("%")) {
                emit(OpCode.imod);
            }
        } else if (leftType.equals(REAL) && rightType.equals(REAL)) {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            if (ctx.op.getText().equals("*")) {
                emit(OpCode.dmult);
            } else if (ctx.op.getText().equals("/")) {
                emit(OpCode.ddiv);
            }
        } else if (leftType.equals(INT) && rightType.equals(REAL)) {
            visit(ctx.expr(0));
            emit(OpCode.itod);
            visit(ctx.expr(1));
            if (ctx.op.getText().equals("*")) {
                emit(OpCode.dmult);
            } else if (ctx.op.getText().equals("/")) {
                emit(OpCode.ddiv);
            }
        } else if (leftType.equals(REAL) && rightType.equals(INT)) {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            emit(OpCode.itod);
            if (ctx.op.getText().equals("*")) {
                emit(OpCode.dmult);
            } else if (ctx.op.getText().equals("/")) {
                emit(OpCode.ddiv);
            }
        } else {
            System.out.println("Compiler error on MultDivMod");
            System.exit(0);
        }
        return null;
    }

    // expr op=(PLUS|MINUS) expr                      # AddSub
    // AddSub can be iadd/isub or dadd/dsub
    @Override public Void visitAddSub(TugaParser.AddSubContext ctx) {
        String leftType = typeChecker.getType(ctx.expr(0));
        String rightType = typeChecker.getType(ctx.expr(1));

        //If one of them is a string and op=+, its string concatenation
        //However, it can be:
        // String - int
        // String - real
        // int - string
        // real - string
        // String - String
        // Boolean - String
        // String Boolean

        if(ctx.op.getText().equals("+") && (leftType.equals(STRING) || rightType.equals(STRING))){
            if (leftType.equals(STRING) && rightType.equals(STRING)) {
                //String - String
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.sconcat);
            } else if (leftType.equals(STRING) && rightType.equals(INT)) {
                //String - int
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.itos);
                emit(OpCode.sconcat);
            } else if (leftType.equals(STRING) && rightType.equals(REAL)) {
                //String - real
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.dtos);
                emit(OpCode.sconcat);
            } else if (leftType.equals(INT)) {
                //int - String
                visit(ctx.expr(0));
                emit(OpCode.itos);
                visit(ctx.expr(1));
                emit(OpCode.sconcat);
            } else if (leftType.equals(REAL)) {
                //real - String
                visit(ctx.expr(0));
                emit(OpCode.dtos);
                visit(ctx.expr(1));
                emit(OpCode.sconcat);
            } else if (leftType.equals(BOOLEAN)){
                // Boolean - String
                visit(ctx.expr(0));
                emit(OpCode.btos);
                visit(ctx.expr(1));
                emit(OpCode.sconcat);
            } else if(rightType.equals(BOOLEAN)){
                // String - Boolean
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.btos);
                emit(OpCode.sconcat);
            }
        // Now it can be
        // int +/- int
        // real +/- real
        // int +/- real
        // real +/- int
        }
        else if (leftType.equals(INT) && rightType.equals(INT)) {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            if (ctx.op.getText().equals("+")) {
                emit(OpCode.iadd);
            } else if (ctx.op.getText().equals("-")) {
                emit(OpCode.isub);
            }
        }
        else if (leftType.equals(REAL) && rightType.equals(REAL)) {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            if (ctx.op.getText().equals("+")) {
                emit(OpCode.dadd);
            } else if (ctx.op.getText().equals("-")) {
                emit(OpCode.dsub);
            }
        }
        else if (leftType.equals(INT) && rightType.equals(REAL)) {
            visit(ctx.expr(0));
            emit(OpCode.itod);
            visit(ctx.expr(1));
            if (ctx.op.getText().equals("+")) {
                emit(OpCode.dadd);
            } else if (ctx.op.getText().equals("-")) {
                emit(OpCode.dsub);
            }
        }
        else if (leftType.equals(REAL) && rightType.equals(INT)) {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            emit(OpCode.itod);
            if (ctx.op.getText().equals("+")) {
                emit(OpCode.dadd);
            } else if (ctx.op.getText().equals("-")) {
                emit(OpCode.dsub);
            }
        }
        else {
            System.out.println("Compiler error on AddSub");
            System.exit(0);
        }
        return null;
    }

    // expr op=(LESS|LESSEQ|GRTR|GRTREQ) expr # LessGrtr
    @Override public Void visitLessGrtr(TugaParser.LessGrtrContext ctx) {
        String leftType = typeChecker.getType(ctx.expr(0));
        String rightType = typeChecker.getType(ctx.expr(1));

        if (leftType.equals(INT) && rightType.equals(INT)) {
            if (ctx.op.getText().equals("<")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.ilt);
            } else if (ctx.op.getText().equals("<=")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.ileq);
            } else if (ctx.op.getText().equals(">")) {
                visit(ctx.expr(1));
                visit(ctx.expr(0));
                emit(OpCode.ilt);
            } else if (ctx.op.getText().equals(">=")) {
                visit(ctx.expr(1));
                visit(ctx.expr(0));
                emit(OpCode.ileq);
            }
            else {
                System.out.println("Compiler error on LessGrtr");
                System.exit(0);
            }
        } else if (leftType.equals(REAL) && rightType.equals(REAL)) {
            if (ctx.op.getText().equals("<")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.dlt);
            } else if (ctx.op.getText().equals("<=")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.dleq);
            } else if (ctx.op.getText().equals(">")) {
                visit(ctx.expr(1));
                visit(ctx.expr(0));
                emit(OpCode.dlt);
            } else if (ctx.op.getText().equals(">=")) {
                visit(ctx.expr(1));
                visit(ctx.expr(0));
                emit(OpCode.dleq);
            }
            else {
                System.out.println("Compiler error on LessGrtr");
                System.exit(0);
            }
        } else if (leftType.equals(INT) && rightType.equals(REAL)) {
            if (ctx.op.getText().equals("<")) {
                visit(ctx.expr(0));
                emit(OpCode.itod);
                visit(ctx.expr(1));
                emit(OpCode.dlt);
            } else if (ctx.op.getText().equals("<=")) {
                visit(ctx.expr(0));
                emit(OpCode.itod);
                visit(ctx.expr(1));
                emit(OpCode.dleq);
            } else if (ctx.op.getText().equals(">")) {
                visit(ctx.expr(1));
                visit(ctx.expr(0));
                emit(OpCode.itod);
                emit(OpCode.dlt);
            } else if (ctx.op.getText().equals(">=")) {
                visit(ctx.expr(1));
                visit(ctx.expr(0));
                emit(OpCode.itod);
                emit(OpCode.dleq);
            }
            else {
                System.out.println("Compiler error on LessGrtr");
                System.exit(0);
            }
        } else if (leftType.equals(REAL) && rightType.equals(INT)) {
            if (ctx.op.getText().equals("<")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.itod);
                emit(OpCode.dlt);
            } else if (ctx.op.getText().equals("<=")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.itod);
                emit(OpCode.dleq);
            } else if (ctx.op.getText().equals(">")) {
                visit(ctx.expr(1));
                emit(OpCode.itod);
                visit(ctx.expr(0));
                emit(OpCode.dlt);
            } else if (ctx.op.getText().equals(">=")) {
                visit(ctx.expr(1));
                emit(OpCode.itod);
                visit(ctx.expr(0));
                emit(OpCode.dleq);
            }
            else {
                System.out.println("Compiler error on LessGrtr");
                System.exit(0);
            }
        } else {
            System.out.println("Compiler error on LessGrtr");
            System.exit(0);
        }
        return null;
    }

    // expr op=(EQ|DIFF) expr                         # EqDiff
    @Override public Void visitEqDiff(TugaParser.EqDiffContext ctx) {
        String leftType = typeChecker.getType(ctx.expr(0));
        String rightType = typeChecker.getType(ctx.expr(1));

        //If both are strings, its string comparison
        if (leftType.equals(STRING) && rightType.equals(STRING)) {
            if (ctx.op.getText().equals("igual")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.seq);
            } else if (ctx.op.getText().equals("diferente")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.sneq);
            }
        } else if (leftType.equals(INT) && rightType.equals(INT)) {
            if (ctx.op.getText().equals("igual")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.ieq);
            } else if (ctx.op.getText().equals("diferente")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.ineq);
            }
        } else if (leftType.equals(REAL) && rightType.equals(REAL)) {
            if (ctx.op.getText().equals("igual")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.deq);
            } else if (ctx.op.getText().equals("diferente")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.dneq);
            }
        } else if (leftType.equals(INT) && rightType.equals(REAL)) {
            if (ctx.op.getText().equals("igual")) {
                visit(ctx.expr(0));
                emit(OpCode.itod);
                visit(ctx.expr(1));
                emit(OpCode.deq);
            } else if (ctx.op.getText().equals("diferente")) {
                visit(ctx.expr(0));
                emit(OpCode.itod);
                visit(ctx.expr(1));
                emit(OpCode.dneq);
            }
        } else if (leftType.equals(REAL) && rightType.equals(INT)) {
            if (ctx.op.getText().equals("igual")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.itod);
                emit(OpCode.deq);
            } else if (ctx.op.getText().equals("diferente")) {
                visit(ctx.expr(0));
                visit(ctx.expr(1));
                emit(OpCode.itod);
                emit(OpCode.dneq);
            }
        } else {
            System.out.println("Compiler error on EqDiff");
            System.exit(0);
        }
        return null;
    }

    // expr AND expr                          # And
    @Override public Void visitAnd(TugaParser.AndContext ctx) {
        String leftType = typeChecker.getType(ctx.expr(0));
        String rightType = typeChecker.getType(ctx.expr(1));

        if (leftType.equals(BOOLEAN) && rightType.equals(BOOLEAN)) {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            emit(OpCode.and);
        } else {
            System.out.println("Compiler error on visitAnd");
            System.exit(0);
        }
        return null;
    }

    // expr Or expr                          # Or
    @Override public Void visitOr(TugaParser.OrContext ctx) {
        String leftType = typeChecker.getType(ctx.expr(0));
        String rightType = typeChecker.getType(ctx.expr(1));

        if (leftType.equals(BOOLEAN) && rightType.equals(BOOLEAN)) {
            visit(ctx.expr(0));
            visit(ctx.expr(1));
            emit(OpCode.or);
        } else {
            System.out.println("Compiler error on visitOr");
            System.exit(0);
        }
        return null;
    }

    // VARNAME                                        # Var
    @Override
    public Void visitVar(TugaParser.VarContext ctx){
        String symbol = ctx.getText();
        int addr = currentScope.getAddr(symbol);
        // if this was declared in global scope means we need to load a global variable
        if(!globalScope.symbolAvailable(symbol)){
            emit(OpCode.gload, addr);
        }
        // if it's not in global table, it means it's a local variable, so we can load it!
        else{
            emit(OpCode.lload, addr);
        }
        return null;
    }

    // INT                                            # Int
    @Override public Void visitInt(TugaParser.IntContext ctx) {
        emit(OpCode.iconst, Integer.valueOf(ctx.INT().getText()));
        return null;
    }

    // REAL                                           # Real
    @Override public Void visitReal(TugaParser.RealContext ctx) {
        String value = ctx.REAL().getText();
        double d = Double.parseDouble(value);
        int i = poolConstantIndex(d);
        emit(OpCode.dconst, i);
        return null;
    }

    // STRING                                         # String
    @Override public Void visitString(TugaParser.StringContext ctx) {
        String value = ctx.STRING().getText();

        int i = poolConstantIndex(value);
        emit(OpCode.sconst, i);
        return null;
    }

    // BOOLEAN
    @Override public Void visitBoolean(TugaParser.BooleanContext ctx) {
        if (ctx.BOOLEAN().getText().equals("verdadeiro")) {
            emit(OpCode.tconst);
        } else {
            emit(OpCode.fconst);
        }
        return null;
    }

    // LPAREN expr RPAREN                             # Parens
    @Override public Void visitParens(TugaParser.ParensContext ctx) {
        return visit(ctx.expr());
    }

    //*/

   /*
        Utility functions
    */

    public void emit(OpCode opc) {
        code.add( new Instruction(opc) );
    }

    public void emit(OpCode opc, int val) {
        code.add( new Instruction1Arg(opc, val) );
    }

    // dump the code to the screen in "assembly" format
    public void dumpCode() {
        System.out.println("*** Constant pool ***");
        for (int i=0; i < constantPool.size(); i++){
            System.out.println(i + ": " + constantPool.get(i));
        }
        System.out.println("*** Instructions ***");
        for (int i=0; i< code.size(); i++)
            System.out.println( i + ": " + code.get(i));
    }

    // save the generated bytecodes to file filename
    public void saveBytecodes(String filename) throws IOException {
        try ( DataOutputStream dout =
                      new DataOutputStream(new FileOutputStream(filename)) ) {
            // write number of constants in constant pool
            dout.writeInt(this.constantPool.size());

            // write constants in constant pool
            for (Object constant : constantPool){
                if(constant instanceof Double){
                    dout.writeByte(1);
                    dout.writeDouble((Double) constant);
                }
                else if(constant instanceof String){
                    dout.writeByte(3);
                    dout.writeInt(((String) constant).length());
                    dout.writeChars((String) constant);
                }
            }
            for (Instruction inst : code){
                // the instructions
                inst.writeTo(dout);
            }

            // System.out.println("Saving the bytecodes to " + filename);
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
    }

}