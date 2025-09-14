package VM;

/*
  Instruction codes of the virtual machine
*/

public enum OpCode {
    // single-byte instructions, just the OpCode: no arguments
    iconst   (1),   // int constant
    dconst (1),     // real constant
    sconst(1),      // string constant
    iprint(0),       // int print
    iuminus(0),      // int unary minus
    iadd(0),         // int add
    isub(0),         // int sub
    imult(0),        // int mult
    idiv(0),         // int div
    imod(0),         // int mod
    ieq(0),          // int equal
    ineq(0),         // int not equal
    ilt(0),          // int less than
    ileq(0),         // int less or equal than
    itod(0),         // int to double
    itos(0),         // int to string
    dprint(0),       // double print
    duminus(0),      // double unary minus
    dadd(0),         // double add
    dsub(0),         // double sub
    dmult(0),        // double mult
    ddiv(0),         // double div
    deq(0),          // double equal
    dneq(0),         // double not equal
    dlt(0),          // double less than
    dleq(0),         // double less or equal than
    dtos(0),         // double to string
    sprint(0),       // string print
    sconcat(0),      // string concat
    seq(0),          // string equal
    sneq(0),         // string not equal
    tconst(0),       // adds true value to the stack
    fconst(0),       // adds false value to the stack
    bprint(0),       // boolean print (VER DEPOIS)
    beq(0),          // boolean equal
    bneq(0),         // boolean not equal
    and(0),          // and operator
    or(0),           // or operator
    not(0),          // boolean not
    btos(0),         // boolean to string
    halt(0),         // ends program execution
    jump(1),         // unconditional jump to addr
    jumpf(1),        // jump to addr if false
    galloc(1),       // global memory allocation of n positions in globals array
    gload(1),        // loads from array to stack
    gstore(1),        // stores from stack to array
    lalloc(1),        // local memory allocation of n positions in stack
    lload(1),         // local load in stack FP + addr
    lstore(1),        // local store in stack FP + addr
    pop(1),           // pops n elements from stack
    call(1),          // creates new frame in stack
    retval(1),        // return from non void function
    ret(1),           // return from void function
    ;

    private final int nArgs;    // number of arguments
    OpCode(int nArgs) {
        this.nArgs = nArgs;
    }
    public int nArgs() { return nArgs; }

    // convert byte value into an OpCode
    public static OpCode convert(byte value) {
        return OpCode.values()[value];
    }
}
