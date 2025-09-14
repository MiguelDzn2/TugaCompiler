package CodeGenerator;

import Tuga.*;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.*;

import java.util.*;

// This Identificator is used to DEFINE
// This will be used to define all scopes, and it's respective variables and functions
// Here we give errors if something has already been defined inside it's current scope or previous ones
public class Identificator extends TugaBaseVisitor<String> {
    // Map to store all the symbols
    // We now have to worry about variables and functions
    // We will be no longer using a symbolTable directly, instead we will use the Scopes
    ParseTreeProperty<Scope> scopes = new ParseTreeProperty<>();    // With this, we will be able to give each ctx it's scope, so that we can work with it in the future
    Scope global;   // Global scope
    Scope currentScope; // Current scope
    static final String VAR = "v";  // variable identifier for symbol table
    static final String FUNC = "f"; // function identifier for symbol table
    static final String GLOBAL = "global"; // global name for global scope
    static final String BLOCK = "local"; // name for local scope of block
    static final String FUNCTION = "function"; // name for local scope of function
    static final String VOID = "vazio"; // void function type
    private int localNum = 0;   // since local scope has to have number, we will use this number and increment it accordingly
    protected String[] errors;
    protected ArrayList<ParserRuleContext> flaggedCtx = new ArrayList<>();  // this will have all of the ctx's that are flagged for having an error

    public Identificator(){
        this.global = new Scope(null, GLOBAL, 0); // creates first scope (global)
        this.currentScope = global;
    }

    public boolean hasErrors(){
        for(String s : this.errors){
            if(s != null){
               return true;
            }
        }
        return false;
    }

    public boolean isFlagged(ParserRuleContext ctx){
        if(flaggedCtx.contains(ctx)){
            return true;
        }
        return false;
    }

    public void printErrors(){
        for(String s : this.errors){
            if(s != null){
                System.out.println(s);
            }
        }
        System.exit(0);
    }

    @Override public String visitProg(TugaParser.ProgContext ctx){
        saveScope(ctx); // save global scope with prog ctx in first position of arraylist
        Token t = ctx.getStop();
        int line = t.getLine();
        this.errors = new String[line];
        visitChildren(ctx);
        if(currentScope.symbolAvailable("principal")){
            this.errors[line-1] = "erro na linha " + line + ": falta funcao principal()";
        }
        return null;
    }

    // We need to check all declarations to add them to the symbol table
    // and also to verify that each variable only is declared once
    @Override public String visitDecl(TugaParser.DeclContext ctx){
        for(TerminalNode var : ctx.VARNAME()){
            String newSymbol = var.getText();
            String type = ctx.op.getText();
            if(!currentScope.symbolAvailable(newSymbol)){
                Token t = ctx.getStart();
                int line = t.getLine();
                String errorMsg = "erro na linha " + line + ": '" + newSymbol + "' ja foi declarado";
                this.errors[line-1] = errorMsg;
                flaggedCtx.add(ctx);
            }
            else{
                // op here has possible values: "inteiro", "real", "string" and "booleano"
                currentScope.symbolTable.put(newSymbol, VAR);
                currentScope.symbolTable.setSymbolType(newSymbol, type);
                // only want to add addr if this is a global declaration
                if(currentScope.name.equals(GLOBAL)){
                    currentScope.symbolTable.setGlobalVarAddr(newSymbol);
                }
                else{
                    currentScope.symbolTable.setLocalVarAddr(newSymbol);
                }
            }
        }
        return null;
    }

    // Check declaration of arguments
    // This works just as variables because that's what arguments are
    @Override
    public String visitArgdecl(TugaParser.ArgdeclContext ctx){
        String newSymbol = ctx.VARNAME().getText();
        String type = ctx.op.getText();
        if(!currentScope.symbolAvailable(newSymbol)){
            Token t = ctx.getStart();
            int line = t.getLine();
            String errorMsg = "erro na linha " + line + ": '" + newSymbol + "' ja foi declarado";
            this.errors[line-1] = errorMsg;
            flaggedCtx.add(ctx);
        }
        else{
            // op here has possible values: "inteiro", "real", "string" and "booleano"
            currentScope.symbolTable.put(newSymbol, VAR);
            currentScope.symbolTable.setSymbolType(newSymbol, type);
            currentScope.symbolTable.setLocalVarAddr(newSymbol);
        }
        return type;
    }

    // Check for function name availability and visit it's children accordingly
    @Override
    public String visitFunc(TugaParser.FuncContext ctx){
        String newSymbol = ctx.VARNAME().getText(); // function name
        String type; // function type
        // if there is no op then the function should be void
        if(ctx.op == null){
            type = VOID;
        }
        else{
            type = ctx.op.getText();
        }
        if(!currentScope.symbolAvailable(newSymbol)){
            Token t = ctx.getStart();
            int line = t.getLine();
            String errorMsg = "erro na linha " + line + ": '" + newSymbol + "' ja foi declarado";
            this.errors[line-1] = errorMsg;
            flaggedCtx.add(ctx);
        }
        else{
            // op here has possible values: "inteiro", "real", "string", "booleano" and null
            currentScope.symbolTable.put(newSymbol, FUNC);
            currentScope.symbolTable.setSymbolType(newSymbol, type);
            currentScope.symbolTable.setSymbolNArgs(newSymbol, ctx.argdecl().size());
            ArrayList<String> argTypes = new ArrayList<>();
            // set new scope (function scope)
            String newScopeName = FUNCTION + localNum;
            localNum++;
            Scope newScope = new Scope(currentScope, newScopeName, ctx.argdecl().size());
            currentScope = newScope;
            // save new scope
            saveScope(ctx);
            // first visit all argument declarations if there are any and save it's types in array
            if(!ctx.argdecl().isEmpty()){
                for(TugaParser.ArgdeclContext argdecl: ctx.argdecl()){
                    argTypes.add(visit(argdecl));
                }
            }
            // then visit block content
            visit(ctx.blck());
            // go back to previous scope
            currentScope = currentScope.previous;
            // after coming back to normal scope, save all types of arguments in function
            // it's ok if it's empty, because it will start being empty and stop being undefined
            currentScope.symbolTable.setSymbolArgTypes(newSymbol, argTypes);
        }
        return null;
    }

    // case for block not in function
    @Override
    public String visitBlock(TugaParser.BlockContext ctx){
        // set new scope (normal block scope)
        String newScopeName = BLOCK + localNum;
        localNum++;
        Scope newScope = new Scope(currentScope, newScopeName, 0);
        currentScope = newScope;
        // save new scope
        saveScope(ctx);
        // visit block content
        visit(ctx.blck());
        // go back to previous scope
        currentScope = currentScope.previous;
        return null;
    }

    // THIS IS ONLY CHECKING FOR THE EXISTENCE OF THE SYMBOL AND NARGS VERIFICATION
    @Override
    public String visitStatFunctionCall(TugaParser.StatFunctionCallContext ctx){
        // ALL OF THIS WILL BE TREATED IN IdentifierRef
        visitChildren(ctx);
        return null;
    }

    // THIS IS ONLY CHECKING FOR THE EXISTENCE OF THE SYMBOL AND NARGS VERIFICATION
    @Override
    public String visitExprFunctionCall(TugaParser.ExprFunctionCallContext ctx){
        // ALL OF THIS WILL BE TREATED IN IdentifierRef
        visitChildren(ctx);
        return null;
    }

    // Verify if variables that are being affectated are
    // already declared
    @Override
    public String visitAffect(TugaParser.AffectContext ctx){
        // ALL OF THIS WILL BE TREATED IN IdentifierRef
        visitChildren(ctx);
        return null;
    }

    // Need to check this because VARNAME could be referencing a function symbol without the parenthesis
    // which shouldn't be allowed
    // NOTE: Did we not have this for the second version of the language? If not we should have!
    @Override
    public String visitVar(TugaParser.VarContext ctx){
        // ALL OF THIS WILL BE TREATED IN IdentifierRef
        return null;
    }

    // Save current scope to correspondent ctx
    // This ctx will be either from a function or from a normal block, which
    // are the only two ways of changing scope
    private void saveScope(ParserRuleContext ctx){
        scopes.put(ctx, currentScope);
    }
}
