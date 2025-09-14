package CodeGenerator;

import Tuga.TugaBaseVisitor;
import Tuga.TugaParser;
import org.antlr.v4.runtime.Token;

// This Identificator is used to REFERENCE
// This will run after the first Identificator
// After everything is defined, we just need to verify if every function that is being called exists
public class IdentificatorRef extends TugaBaseVisitor<Void> {
    Identificator identificator;
    Scope currentScope; // Current scope
    static final String VAR = "v";  // variable identifier for symbol table
    static final String FUNC = "f"; // function identifier for symbol table
    private int localNum = 0;   // since local scope has to have number, we will use this number and increment it accordingly

    public IdentificatorRef(Identificator identificator){
        this.identificator = identificator;
    }

    @Override public Void visitProg(TugaParser.ProgContext ctx){
        visitChildren(ctx);
        return null;
    }

    // We need to check all declarations to add them to the symbol table
    // and also to verify that each variable only is declared once
    @Override public Void visitDecl(TugaParser.DeclContext ctx){
        // ALL OF THIS WILL BE TREATED IN Identifier
        visitChildren(ctx);
        return null;
    }

    // Check declaration of arguments
    // This works just as variables because that's what arguments are
    @Override
    public Void visitArgdecl(TugaParser.ArgdeclContext ctx){
        // ALL OF THIS WILL BE TREATED IN Identifier
        visitChildren(ctx);
        return null;
    }

    // Check for function name availability and visit it's children accordingly
    @Override
    public Void visitFunc(TugaParser.FuncContext ctx){
        // ALL OF THIS WILL BE TREATED IN Identifier
        // we don't want to visit a function that has already given an error
        if(!identificator.isFlagged(ctx)) {
            this.currentScope = identificator.scopes.get(ctx);
            visitChildren(ctx);
            this.currentScope = this.currentScope.previous;
        }
        return null;
    }

    // case for block not in function
    @Override
    public Void visitBlock(TugaParser.BlockContext ctx){
        // ALL OF THIS WILL BE TREATED IN Identifier
        this.currentScope = identificator.scopes.get(ctx);
        visitChildren(ctx);
        this.currentScope = this.currentScope.previous;
        return null;
    }

    // THIS IS ONLY CHECKING FOR THE EXISTENCE OF THE SYMBOL AND NARGS VERIFICATION
    @Override
    public Void visitStatFunctionCall(TugaParser.StatFunctionCallContext ctx){
        String func = ctx.VARNAME().getText();
        if(currentScope.symbolAvailable(func)){
            Token t = ctx.getStart();
            int line = t.getLine();
            // if there still isn't an error on this line we can add one
            if(!identificator.isFlagged(ctx)){
                String errorMsg = "erro na linha " + line + ": '" + func + "' nao foi declarado";
                identificator.errors[line-1] = errorMsg;
                identificator.flaggedCtx.add(ctx);
            }
        }
        // if it exists, needs to check if it's a function and if it has the correct number of arguments
        else{
            int nCorrectArgs = currentScope.getNArgs(func);
            int nReferencedArgs = ctx.expr().size();
            String identifier = currentScope.getIdentifier(func);
            // if it's not a function
            if(!identifier.equals(FUNC)){
                Token t = ctx.getStart();
                int line = t.getLine();
                // if there still isn't an error on this line we can add one
                if(!identificator.isFlagged(ctx)){
                    String errorMsg = "erro na linha " + line + ": '" + func + "' nao eh funcao";
                    identificator.errors[line-1] = errorMsg;
                    identificator.flaggedCtx.add(ctx);
                }
            }
            // if it has the correct number of arguments
            else if(nCorrectArgs != nReferencedArgs){
                Token t = ctx.getStart();
                int line = t.getLine();
                // if there still isn't an error on this line we can add one
                if(!identificator.isFlagged(ctx)){
                    String errorMsg = "erro na linha " + line + ": '" + func + "' requer " + nCorrectArgs + " argumentos";
                    identificator.errors[line-1] = errorMsg;
                    identificator.flaggedCtx.add(ctx);
                }
            }
        }
        // only visit children if ctx isn't flagged
        if(!identificator.isFlagged(ctx)){
            visitChildren(ctx);
        }
        return null;
    }

    // THIS IS ONLY CHECKING FOR THE EXISTENCE OF THE SYMBOL AND NARGS VERIFICATION
    @Override
    public Void visitExprFunctionCall(TugaParser.ExprFunctionCallContext ctx){
        String func = ctx.VARNAME().getText();
        if(currentScope.symbolAvailable(func)){
            Token t = ctx.getStart();
            int line = t.getLine();
            // if there still isn't an error on this line we can add one
            if(identificator.errors[line-1] == null){
                String errorMsg = "erro na linha " + line + ": '" + func + "' nao foi declarado";
                identificator.errors[line-1] = errorMsg;
                identificator.flaggedCtx.add(ctx);
            }
        }
        // if it exists, needs to check if it's a function and if it has the correct number of arguments
        else{
            int nCorrectArgs = currentScope.getNArgs(func);
            int nReferencedArgs = ctx.expr().size();
            String identifier = currentScope.getIdentifier(func);
            // if it's not a function
            if(!identifier.equals(FUNC)){
                Token t = ctx.getStart();
                int line = t.getLine();
                // if there still isn't an error on this line we can add one
                if(!identificator.isFlagged(ctx)){
                    String errorMsg = "erro na linha " + line + ": '" + func + "' nao eh funcao";
                    identificator.errors[line-1] = errorMsg;
                    identificator.flaggedCtx.add(ctx);
                }
            }
            // if it has the correct number of arguments
            if(nCorrectArgs != nReferencedArgs){
                Token t = ctx.getStart();
                int line = t.getLine();
                // if there still isn't an error on this line we can add one
                if(!identificator.isFlagged(ctx)){
                    String errorMsg = "erro na linha " + line + ": '" + func + "' requer " + nCorrectArgs + " argumentos";
                    identificator.errors[line-1] = errorMsg;
                    identificator.flaggedCtx.add(ctx);
                }
            }
        }
        // only visit children if ctx isn't flagged
        if(!identificator.isFlagged(ctx)){
            visitChildren(ctx);
        }
        return null;
    }

    // Verify if variables that are being affectated are
    // already declared
    @Override
    public Void visitAffect(TugaParser.AffectContext ctx){
        String symbol = ctx.VARNAME().getText();
        // if symbol is available means the variable hasn't been declared in this scope
        if(currentScope.symbolAvailable(symbol)){
            Token t = ctx.getStart();
            int line = t.getLine();
            // if there still isn't an error on this line we can add one
            if(!identificator.isFlagged(ctx)){
                String errorMsg = "erro na linha " + line + ": '" + symbol + "' nao foi declarado";
                identificator.errors[line-1] = errorMsg;
                identificator.flaggedCtx.add(ctx);
            }
        }
        // need to check if symbol is actually a variable or not
        else if(!currentScope.getIdentifier(symbol).equals(VAR)){
            Token t = ctx.getStart();
            int line = t.getLine();
            // if there still isn't an error on this line we can add one
            if(!identificator.isFlagged(ctx)){
                String errorMsg = "erro na linha " + line + ": '" + symbol + "' nao eh variavel";
                identificator.errors[line-1] = errorMsg;
                identificator.flaggedCtx.add(ctx);
            }
        }
        // only visit children if ctx isn't flagged
        if(!identificator.isFlagged(ctx)){
            visitChildren(ctx);
        }
        return null;
    }

    // Need to check this because VARNAME could be referencing a function symbol without the parenthesis
    // which shouldn't be allowed
    // NOTE: Did we not have this for the second version of the language? If not we should have!
    @Override
    public Void visitVar(TugaParser.VarContext ctx){
        String symbol = ctx.VARNAME().getText();
        // Verify if it exists
        if(currentScope.symbolAvailable(symbol)){
            Token t = ctx.getStart();
            int line = t.getLine();
            // if there still isn't an error on this line we can add one
            if(!identificator.isFlagged(ctx)){
                String errorMsg = "erro na linha " + line + ": '" + symbol + "' nao foi declarado";
                identificator.errors[line-1] = errorMsg;
                identificator.flaggedCtx.add(ctx);
            }
        }
        // verify if symbol is actually a variable
        else{
            String identifier = currentScope.getIdentifier(symbol);
            if(!identifier.equals(VAR)){
                Token t = ctx.getStart();
                int line = t.getLine();
                // if there still isn't an error on this line we can add one
                if(!identificator.isFlagged(ctx)){
                    String errorMsg = "erro na linha " + line + ": '" + symbol + "' nao eh variavel";
                    identificator.errors[line-1] = errorMsg;
                    identificator.flaggedCtx.add(ctx);
                }
            }
        }
        return null;
    }
}
