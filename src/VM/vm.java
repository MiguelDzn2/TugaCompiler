package VM;

import CodeGenerator.SymbolTable;
import VM.Instruction.*;

import java.util.*;
import java.io.*;


public class vm {
    private final boolean trace;       // trace flag
    private final byte[] bytecodes;    // the bytecodes, storing just for displaying them. Not really needed
    private Instruction[] code;        // instructions (converted from the bytecodes)
    private int IP;                    // instruction pointer
    private int FP = 0;                    // frame pointer (always starts at 0)
    private final RuntimeStack stack = new RuntimeStack();    // runtime stack
    private final ArrayList<Object> constantPool = new ArrayList<>();   // constant pool
    private ArrayList<Object> globals = new ArrayList<>();  // array list of global variables

    // constantPool identifiers
    final byte realIdentifier = 1;
    final byte stringIdentifier = 3;

    final static byte NULO = 22;


    public vm( byte [] bytecodes, boolean trace) {
        this.trace = trace;
        this.bytecodes = bytecodes;
        decode(bytecodes);
        this.IP = 0;
    }

    //auxiliary functions
    //Retrieve string from constant pool given its index
    private String getString(int index) {
        return (String) constantPool.get(index);
    }

    //Retrieve double from constant pool given its index
    private double getDouble(int index) {
        return (double) constantPool.get(index);
    }

    // decode the bytecodes into instructions and store them in this.code
    private void decode(byte [] bytecodes) {
        ArrayList<Instruction> inst = new ArrayList<>();
        try {
            // feed the bytecodes into a data input stream
            DataInputStream din = new DataInputStream(new ByteArrayInputStream(bytecodes));

            //The first 4 bytes are the number of constants in the constant pool
            int nConstants = din.readInt(); //we will have to read n constants

            //Each constant can be a double or a string
            //Doubles are represented by 8 bytes (+1 type identifier, at the beginning)
            //Strings are represented by a length (4 bytes) and the string itself (each character is 2 bytes) (+1 type identifier, at the beginning)

            //double's first byte == 01
            //strings's first byte == 03
            for (int i = 0; i < nConstants; i++) {
                byte type = din.readByte();
                if (type == realIdentifier) {
                    double d = din.readDouble(); //readDouble returns the next 8 bytes as a double
                    constantPool.add(d);
                } else if (type == stringIdentifier) {
                    int length = din.readInt(); //readInt returns the next 4 bytes as an int, and its the length of the string
                    StringBuilder s = new StringBuilder();
                    for (int j = 0; j < length; j++) {
                        s.append(din.readChar()); //readChar returns the next 2 bytes as a char
                    }
                    constantPool.add(s.toString());
                }
            }

            // Now, i have the entirity of the constant pool populated.

            // convert them into intructions
            while (true) {
                byte b = din.readByte();
                OpCode opc = OpCode.convert(b);
                switch (opc.nArgs()) {
                    case 0:
                        inst.add(new Instruction(opc));
                        break;
                    case 1:
                        int val = din.readInt();
                        inst.add(new Instruction1Arg(opc, val));
                        break;
                    default:
                        System.out.println("This should never happen! In file vm.java, method decode(...)");
                        System.exit(1);
                }
            }
        }
        catch (java.io.EOFException e) {
            // System.out.println("reached end of input stream");
            // reached end of input stream, convert arraylist to array
            this.code = new Instruction[ inst.size() ];
            inst.toArray(this.code);
            if (trace) {
                System.out.println("Disassembled instructions");
                //dumpInstructions();
                dumpInstructionsAndBytecodes();
            }
        }
        catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    // dump the instructions, along with the corresponding bytecodes
    public void dumpInstructionsAndBytecodes() {
        int idx = 0;
        for (int i=0; i< code.length; i++) {
            StringBuilder s = new StringBuilder();
            s.append(String.format("%02X ", bytecodes[idx++]));
            if (code[i].nArgs() == 1)
                for (int k=0; k<4; k++)
                    s.append(String.format("%02X ", bytecodes[idx++]));
            System.out.println( String.format("%5s: %-15s // %s", i, code[i], s) );
        }
    }

    // dump the instructions to the screen
    public void dumpInstructions() {
        for (int i=0; i< code.length; i++)
            System.out.println( i + ": " + code[i] );
    }

    private void runtime_error(String msg) {
        System.out.println("runtime error: " + msg);
        if (trace)
            System.out.println( String.format("%22s Stack: %s", "", stack ) );
        System.exit(1);
    }


    private void exec_iconst(Integer v) {
        stack.push(v);
    }

    // NEW
    private void exec_dconst(Integer index) {
        double d = getDouble(index);
        stack.push(d);
    }

    // NEW
    private void exec_sconst(Integer index) {
        String s = getString(index);
        // remove "
        s = s.substring(1, s.length()-1);
        stack.push(s);
    }

    private void exec_iprint() {
        int v = (int) stack.pop();
        System.out.println(v);
    }

    private void exec_iuminus() {
        int v = (int) stack.pop();
        stack.push(-v);
    }

    private void exec_iadd() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        stack.push(left + right);
    }
    private void exec_isub() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        stack.push(left - right);
    }
    private void exec_imult() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        stack.push(left * right);
    }
    private void exec_idiv() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        if (right != 0)
            stack.push(left / right);
        else
            runtime_error("division by 0");
    }

    // NEW
    private void exec_imod() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        stack.push(left % right);
    }

    // NEW
    private void exec_ieq() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        boolean result = left == right;
        stack.push(result);
    }

    // NEW
    private void exec_ineq() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        boolean result = left != right;
        stack.push(result);
    }

    // NEW
    private void exec_ilt() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        boolean result = left < right;
        stack.push(result);
    }

    // NEW
    private void exec_ileq() {
        int right = (int) stack.pop();
        int left = (int) stack.pop();
        boolean result = left <= right;
        stack.push(result);
    }

    // NEW
    private void exec_itod() {
        int v = (int) stack.pop();
        stack.push((double) v);
    }

    // NEW
    private void exec_itos() {
        int v = (int) stack.pop();
        stack.push(Integer.toString(v));
    }

    // NEW
    private void exec_dprint() {
        double d = (double) stack.pop();
        System.out.println(d);
    }

    // NEW
    private void exec_duminus() {
        double d = (double) stack.pop();
        stack.push(-d);
    }

    // NEW
    private void exec_dadd() {
        double right = (double) stack.pop();
        double left = (double) stack.pop();
        stack.push(left + right);
    }

    // NEW
    private void exec_dsub() {
        double right = (double) stack.pop();
        double left = (double) stack.pop();
        stack.push(left - right);
    }

    // NEW
    private void exec_dmult() {
        double right = (double) stack.pop();
        double left = (double) stack.pop();
        stack.push(left * right);
    }

    // NEW
    private void exec_ddiv() {
        double right = (double) stack.pop();
        double left = (double) stack.pop();
        if (right != 0)
            stack.push(left / right);
        else
            runtime_error("division by 0");
    }

    // NEW
    private void exec_deq() {
        double right = (double) stack.pop();
        double left = (double) stack.pop();
        boolean result = left == right;
        stack.push(result);
    }

    // NEW
    private void exec_dneq() {
        double right = (double) stack.pop();
        double left = (double) stack.pop();
        boolean result = left != right;
        stack.push(result);
    }

    // NEW
    private void exec_dlt() {
        double right = (double) stack.pop();
        double left = (double) stack.pop();
        boolean result = left < right;
        stack.push(result);
    }

    // NEW
    private void exec_dleq() {
        double right = (double) stack.pop();
        double left = (double) stack.pop();
        boolean result = left <= right;
        stack.push(result);
    }

    // NEW
    private void exec_dtos() {
        double d = (double) stack.pop();
        stack.push(Double.toString(d));
    }

    // NEW
    private void exec_sprint() {
        String s = (String) stack.pop();
        System.out.println(s);
    }

    // NEW
    private void exec_sconcat() {
        String right = (String) stack.pop();
        String left = (String) stack.pop();
        stack.push(left + right);
    }

    // NEW
    private void exec_seq() {
        String right = (String) stack.pop();
        String left = (String) stack.pop();
        boolean result = left.equals(right);
        stack.push(result);
    }

    // NEW
    private void exec_sneq() {
        String right = (String) stack.pop();
        String left = (String) stack.pop();
        boolean result = !left.equals(right);
        stack.push(result);
    }

    // NEW
    private void exec_tconst() {
        stack.push(true);
    }

    // NEW
    private void exec_fconst() {
        stack.push(false);
    }

    // NEW
    private void exec_bprint() {
        boolean b = (boolean) stack.pop();
        if (b)
            System.out.println("verdadeiro");
        else
            System.out.println("falso");

    }

    // NEW
    private void exec_beq() {
        boolean right = (boolean) stack.pop();
        boolean left = (boolean) stack.pop();
        boolean result = left == right;
        stack.push(result);
    }

    // NEW
    private void exec_bneq() {
        boolean right = (boolean) stack.pop();
        boolean left = (boolean) stack.pop();
        boolean result = left != right;
        stack.push(result);
    }

    // NEW
    private void exec_and() {
        boolean right = (boolean) stack.pop();
        boolean left = (boolean) stack.pop();
        boolean result = left && right;
        stack.push(result);
    }

    // NEW
    private void exec_or() {
        boolean right = (boolean) stack.pop();
        boolean left = (boolean) stack.pop();
        boolean result = left || right;
        stack.push(result);
    }

    // NEW
    private void exec_not() {
        boolean b = (boolean) stack.pop();
        stack.push(!b);
    }

    // NEW
    private void exec_btos() {
        boolean b = (boolean) stack.pop();
        if(b){
            stack.push("verdadeiro");
        }
        else{
            stack.push("falso");
        }
    }

    // NEW
    // updates instruction pointer so that the next executed instruction is the one in addr
    private void exec_jump(int addr){
        // places it at addr-1 so that the next instruction will be addr
        this.IP = addr-1;
    }

    // NEW
    // pops stack and checks if it's value is false
    // if so, does the same as exec_jump
    private void exec_jumpf(int addr){
        boolean b = (boolean) stack.pop();
        if(!b){
            this.IP = addr-1;
        }
    }

    // NEW
    // Allocates memory for globals array
    private void exec_galloc(int n){
        for(int i = 0; i < n; i++){
            globals.add(NULO);
        }
    }

    // NEW
    // gets object in addr position of globals and places it on the stack
    private void exec_gload(int addr){
        Object value = globals.get(addr);
        // this means the variable still hasn't been initialized
        if(value instanceof Byte){
            System.out.println("erro de runtime: tentativa de acesso a valor NULO");
            System.exit(0);
        }
        stack.push(value);
    }

    // NEW
    // stores object from top of stack in addr position of globals
    private void exec_gstore(int addr){
        Object value = stack.pop();
        globals.set(addr, value);
    }

    // NEW
    // local memory allocatiton
    // alocates n positions on top of stack to store local variables
    // Those n positions start initialized with value NULO
    private void exec_lalloc(int n){
        for(int i = 0; i < n; i++){
            this.stack.push(NULO);
        }
    }

    // NEW
    // local variable load
    // pushes the value int FP+addr onto the stack
    private void exec_lload(int addr){
        this.stack.push(this.stack.get(FP+addr));
    }

    // NEW
    // local variable store
    // pops value on top of stack and stores it into FP+addr
    private void exec_lstore(int addr){
        Object value = stack.pop();
        this.stack.set(FP+addr, value);
    }

    // NEW
    // pops n elements of the stack
    private void exec_pop(int n){
        for(int i = 0; i < n; i++){
            this.stack.pop();
        }
    }

    // NEW
    // creates new frame in stack that will be current frame (stack)
    // saves the previous FP address in new position of FP, so that it knows where to go back when the program is done with current frame (stack)
    // places return address in stack, so that we know where to go in the instructions (instructions)
    // updates IP so that next instruction to be executed is the one belonging on next frame
    private void exec_call(int addr){
        stack.push(FP); // save position of current FP
        FP = stack.size()-1; // FP will now point to this position with the previous FP address
        stack.push(IP+1); // push instructions return address
        IP = addr-1; // points IP to correct instruction of new frame in instructions (-1 because it will be incremented)
    }

    // NEW
    // return from a non void function
    // saves value to return and pops it from stack
    // removes local variables if there are any
    // updates IP addr with value saved in return address
    // updates FP to go out of current frame and go back to previous one
    // removes n arguments if there are any
    // pushes previously saved return value onto the stack
    private void exec_retval(int n){
        Object returnValue = stack.pop(); // save return value
        // to remove all local variables, we first need to see if there are any
        // if FP is in position i, and the return address is int position i+1, then we only need to remove all elements
        // until stack is of size i+2, because this will leave the return address and FP on top, which is what we want
        while(stack.size() > FP+2){
            stack.pop();
        }
        // after taking care of the local variables, we want to pop and save the return address to update IP
        IP = (int) stack.pop() - 1; // -1 because IP will be incremented
        // now we update FP and pop it's current position
        FP = (int) stack.pop();
        // remove n arguments
        for(int i = 0; i < n; i++){
            stack.pop();
        }
        // place return value onto the stack
        stack.push(returnValue);
    }

    // NEW
    // same as previous one but without return value
    private void exec_ret(int n){
        // remove all local variables
        while(stack.size() > FP+2){
            stack.pop();
        }
        // update IP
        IP = (int) stack.pop() -1;
        // update FP
        FP = (int) stack.pop();
        // remove arguments
        for(int i = 0; i < n; i++){
            stack.pop();
        }
    }

    // NEW
    private void exec_halt() {
        System.exit(0);
    }

    private void exec_inst( Instruction inst ) {
        if (trace) {
            System.out.println( String.format("%5s: %-15s Stack: %s", IP, inst, stack ) );
        }
        OpCode opc = inst.getOpCode();
        int v;
        switch(opc) {
            case iconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_iconst( v ); break;
            case dconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_dconst( v ); break;
            case sconst:
                v = ((Instruction1Arg) inst).getArg();
                exec_sconst( v ); break;
            case iprint:
                exec_iprint(); break;
            case iuminus:
                exec_iuminus(); break;
            case iadd:
                exec_iadd(); break;
            case isub:
                exec_isub(); break;
            case imult:
                exec_imult(); break;
            case idiv:
                exec_idiv(); break;
            case imod:
                exec_imod(); break;
            case ieq:
                exec_ieq(); break;
            case ineq:
                exec_ineq(); break;
            case ilt:
                exec_ilt(); break;
            case ileq:
                exec_ileq(); break;
            case itod:
                exec_itod(); break;
            case itos:
                exec_itos(); break;
            case dprint:
                exec_dprint(); break;
            case duminus:
                exec_duminus(); break;
            case dadd:
                exec_dadd(); break;
            case dsub:
                exec_dsub(); break;
            case dmult:
                exec_dmult(); break;
            case ddiv:
                exec_ddiv(); break;
            case deq:
                exec_deq(); break;
            case dneq:
                exec_dneq(); break;
            case dlt:
                exec_dlt(); break;
            case dleq:
                exec_dleq(); break;
            case dtos:
                exec_dtos(); break;
            case sprint:
                exec_sprint(); break;
            case sconcat:
                exec_sconcat(); break;
            case seq:
                exec_seq(); break;
            case sneq:
                exec_sneq(); break;
            case tconst:
                exec_tconst(); break;
            case fconst:
                exec_fconst(); break;
            case bprint:
                exec_bprint(); break;
            case beq:
                exec_beq(); break;
            case bneq:
                exec_bneq(); break;
            case and:
                exec_and(); break;
            case or:
                exec_or(); break;
            case not:
                exec_not(); break;
            case btos:
                exec_btos(); break;
            case jump:
                v = ((Instruction1Arg) inst).getArg();
                exec_jump(v); break;
            case jumpf:
                v = ((Instruction1Arg) inst).getArg();
                exec_jumpf(v); break;
            case galloc:
                v = ((Instruction1Arg) inst).getArg();
                exec_galloc(v); break;
            case gload:
                v = ((Instruction1Arg) inst).getArg();
                exec_gload(v); break;
            case gstore:
                v = ((Instruction1Arg) inst).getArg();
                exec_gstore(v); break;
            case lalloc:
                v = ((Instruction1Arg) inst).getArg();
                exec_lalloc(v); break;
            case lload:
                v = ((Instruction1Arg) inst).getArg();
                exec_lload(v); break;
            case lstore:
                v = ((Instruction1Arg) inst).getArg();
                exec_lstore(v); break;
            case pop:
                v = ((Instruction1Arg) inst).getArg();
                exec_pop(v); break;
            case call:
                v = ((Instruction1Arg) inst).getArg();
                exec_call(v); break;
            case retval:
                v = ((Instruction1Arg) inst).getArg();
                exec_retval(v); break;
            case ret:
                v = ((Instruction1Arg) inst).getArg();
                exec_ret(v); break;
            case halt:
                exec_halt(); break;
            default:
                System.out.println("This should never happen! In file vm.java, method exec_inst()");
                System.exit(1);
        }
    }

    public void run() {
        if (trace) {
            System.out.println("Trace while running the code");
            System.out.println("Execution starts at instrution " + this.IP);
        }
        System.out.println("*** VM output ***");
        while (IP < code.length) {
            exec_inst( code[IP] );
            IP++;
        }
        if (trace)
            System.out.println( String.format("%22s Stack: %s", "", stack ) );
    }

}
