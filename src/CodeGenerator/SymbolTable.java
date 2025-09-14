package CodeGenerator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, SymbolTableValues> table;
    private static final int UNDEFINED = -10000;
    private static final String VOID = "vazio";
    private static final String VAR = "v";
    private static final String FUNC = "f";
    protected int nextAAddr;   // next available address
    private int nVars = 0;  // number of variables in table

    // first argument is next available addr (where it should start)
    public SymbolTable(int addr){
        this.table = new HashMap<>();
        this.nextAAddr = addr;
    }

    public void put(String symbol, String identifier){
        SymbolTableValues values = new SymbolTableValues();
        values.identifier = identifier;
        if(identifier.equals(VAR)){
            nVars++;
        }
        this.table.put(symbol, values);
    }

    public boolean containsSymbol(String symbol){
        return this.table.containsKey(symbol);
    }

    // returns number of variables in table
    public int getNVars(){
        return this.nVars;
    }

    public String getSymbolIdentifier(String symbol){
        return this.table.get(symbol).identifier;
    }

    public String getSymbolType(String symbol){
        return this.table.get(symbol).type;
    }

    public int getSymbolAddr(String symbol){
        return this.table.get(symbol).addr;
    }

    public int getSymbolNArgs(String symbol){
        return this.table.get(symbol).nArgs;
    }
    public ArrayList<String> getSymbolArgTypes(String symbol){
        return this.table.get(symbol).argTypes;
    }

    // We will only set type once
    public void setSymbolType(String symbol, String type){
        if(!this.table.containsKey(symbol)){
            System.out.println("There is no key with such symbol.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        String currentType = getSymbolType(symbol);
        if(currentType != null){
            System.out.println("Symbol type is already defined.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        if(type.equals(VOID) && this.table.get(symbol).identifier.equals(VAR)){
            System.out.println("Symbol type can't be void if symbol is a variable.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        this.table.get(symbol).type = type;
    }

    // Sets addr for of global var in globals array
    // This should only be called once if it's a variable
    // Should never be called if it's anything else
    public void setGlobalVarAddr(String symbol){
        if(!this.table.containsKey(symbol)){
            System.out.println("There is no key with such symbol.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(this.table.get(symbol).addr != UNDEFINED){
            System.out.println("Address for this global variable has already been set.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(!this.table.get(symbol).identifier.equals(VAR)){
            System.out.println("Should only set variable address of a variable.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        this.table.get(symbol).addr = this.nextAAddr++;
    }

    // Sets addr for of local var
    // This should only be called once if it's a variable
    // Should never be called if it's anything else
    public void setLocalVarAddr(String symbol){
        if(!this.table.containsKey(symbol)){
            System.out.println("There is no key with such symbol.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(this.table.get(symbol).addr != UNDEFINED){
            System.out.println("Address for this global variable has already been set.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(!this.table.get(symbol).identifier.equals(VAR)){
            System.out.println("Should only set variable address of a variable.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        // we don't use addr 0 or 1 because 0 is where FP is and 1 is where return address is
        if(this.nextAAddr == 0){
            this.nextAAddr += 2;
        }
        this.table.get(symbol).addr = this.nextAAddr++;
    }

    // Sets start addr of a function in code array
    // This should only be called once and if it's a function
    public void setFuncAddr(String symbol, int addr){
        if(!this.table.containsKey(symbol)){
            System.out.println("There is no key with such symbol.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(this.table.get(symbol).addr != UNDEFINED){
            System.out.println("Address for this function has already been set.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(!this.table.get(symbol).identifier.equals(FUNC)){
            System.out.println("Should only set function address of a function.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        this.table.get(symbol).addr = addr;
    }

    // Sets nArgs of a symbol
    // This should only happen once and if symbol is a function
    public void setSymbolNArgs(String symbol, int nArgs){
        if(!this.table.containsKey(symbol)){
            System.out.println("There is no key with such symbol.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(this.table.get(symbol).nArgs != UNDEFINED){
            System.out.println("Number of arguments for this symbol has already been set.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(!this.table.get(symbol).identifier.equals(FUNC)){
            System.out.println("To add number of arguments symbol has got to be a function.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        this.table.get(symbol).nArgs = nArgs;
    }

    // Sets argTypes of a symbol
    // This should only happen once and if symbol is a function
    public void setSymbolArgTypes(String symbol, ArrayList<String> argTypes){
        if(!this.table.containsKey(symbol)){
            System.out.println("There is no key with such symbol.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(this.table.get(symbol).argTypes != null){
            System.out.println("argTypes for this symbol has already been set.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(!this.table.get(symbol).identifier.equals(FUNC)){
            System.out.println("To add argTypes symbol has got to be a function.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        else if(this.table.get(symbol).nArgs == UNDEFINED){
            System.out.println("Can only set argTypes once nArgs has been set.");
            System.out.println("This should never happen.");
            System.exit(0);
        }
        this.table.get(symbol).argTypes = argTypes;
    }

    // This class is an auxiliary class for us to
    // save all the relevant information about each symbol
    // using the hashmap
    // If we need to add anything to all of the symbols we just need
    // to add it here
    private static class SymbolTableValues{
        protected String type;  // type of symbol (if it's inteiro, real, booleano, string or vazio)
        protected String identifier; // identifier of symbol (if it's a variable or a function)
        protected int addr = UNDEFINED; // address of the global variable; -1 means undefined
        protected int nArgs = UNDEFINED; // number of arguments if this is a function
        protected ArrayList<String> argTypes; // types of args of function

    }
}
