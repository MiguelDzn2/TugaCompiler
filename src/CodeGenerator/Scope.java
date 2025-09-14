package CodeGenerator;

import java.util.ArrayList;

// Class that defines the scope
// Each scope has it's own symbol table
public class Scope {
    protected final Scope previous; // Previous scope from current one, null if there is none
    protected String name;  // Name can be global or localx, with x being a number
    protected SymbolTable symbolTable; // Symbol table of current scope
    private static final int UNDEFINED = -10000;
    static final String BLOCK = "local"; // name for local scope of block
    static final String FUNCTION = "function"; // name for local scope of function
    static final String VAR = "v";  // variable identifier for symbol table
    static final String FUNC = "f"; // function identifier for symbol table

    // Creates a new scope within referred scope
    // I KNOW THIS IS A VERY STUPID WAY OF DOING IT, I SHOULD HAVE 2 CONSTRUCTORS, ONE FOR FUNCTIONS AND ONE FOR NORMAL SCOPES
    // IT IS WHAT IT IS
    public Scope(Scope previous, String name, int nArgs){
        this.previous = previous;
        this.name = name;
        // also checks if it's the global scope so that it uses 0
        if(this.previous == null){
            this.symbolTable = new SymbolTable(0);
        }
        // we do this so that the FP on the vm works correctly
        // ex case: if there are blocks inside a function, next available address shouldn't start at 0, but at whatever
        // number the previous scope table nextAAddr is
        else if(this.name.contains(FUNCTION)){
            this.symbolTable = new SymbolTable(-nArgs);
        }
        else{
            this.symbolTable = new SymbolTable(this.previous.symbolTable.nextAAddr);
        }
    }

    // Verifies if a certain symbol is allowed to be created or not
    public boolean symbolAvailable(String symbol){
        // verify if it's in current scope
        boolean foundSymbol = symbolTable.containsSymbol(symbol);
        // if not in current scope and has previous scope, we need to check previous one
        if(!foundSymbol && this.previous != null){
            // if symbol is available, it means we didn't find it
            return this.previous.symbolAvailable(symbol);
        }
        return !foundSymbol;
    }

    // Finds symbolTable with symbol
    private SymbolTable findSymbol(String symbol){
        if(symbolAvailable(symbol)){
            System.out.println("Symbol does not exist in current scope or in previous ones!");
            System.out.println("This should never happen!");
            System.exit(0);
        }
        if(this.symbolTable.containsSymbol(symbol)){
            return this.symbolTable;
        }
        else{
            return previous.findSymbol(symbol);
        }
    }

    // Gets nArgs of symbol
    public int getNArgs(String symbol){
        SymbolTable table = findSymbol(symbol);
        int nArgs = table.getSymbolNArgs(symbol);
        if(nArgs == UNDEFINED){
            System.out.println("Symbol does not have nArgs defined!");
            System.out.println("This should never happen!");
            System.exit(0);
        }
        return nArgs;
    }

    // Gets argTypes of symbol
    public ArrayList<String> getArgTypes(String symbol){
        SymbolTable table = findSymbol(symbol);
        ArrayList<String> argTypes = table.getSymbolArgTypes(symbol);
        if(argTypes == null){
            System.out.println("Symbol does not have argTypes defined!");
            System.out.println("This should never happen!");
            System.exit(0);
        }
        return argTypes;
    }

    // Gets identifier of symbol
    public String getIdentifier(String symbol){
        SymbolTable table = findSymbol(symbol);
        String identifier = table.getSymbolIdentifier(symbol);
        if(identifier == null){
            System.out.println("Symbol does not have identifier defined!");
            System.out.println("This should never happen!");
            System.exit(0);
        }
        return identifier;
    }

    // Gets type of symbol
    public String getType(String symbol){
        SymbolTable table = findSymbol(symbol);
        String type = table.getSymbolType(symbol);
        if(type == null){
            System.out.println("Symbol does not have type defined!");
            System.out.println("This should never happen!");
            System.exit(0);
        }
        return type;
    }

    // Returns address of symbol
    public int getAddr(String symbol){
        SymbolTable table = findSymbol(symbol);
        int addr = table.getSymbolAddr(symbol);
        if(addr == UNDEFINED){
            System.out.println("Symbol does not have addr defined!");
            System.out.println("This should never happen!");
            System.exit(0);
        }
        return addr;
    }

    // Returns number of local variables of this scope
    // (includes function variables)
    public int getNLocalVars(){
        return this.symbolTable.getNVars();
    }
}
