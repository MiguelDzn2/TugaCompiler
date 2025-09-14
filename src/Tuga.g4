grammar Tuga;

//prog is a sequence of zero or more declarations and one or more instructions
prog   : decl* func+ EOF;

//decl is a delcaration of a variable
decl   : VARNAME (COMMA VARNAME)* TWOPOINT op=('inteiro'|'real'|'string'|'booleano') PTCOMMA ;

//decl of arguments of a function
argdecl: VARNAME TWOPOINT op=('inteiro'|'real'|'string'|'booleano') ;

//func is function declaration
func   : FUNCTION VARNAME LPAREN (argdecl (COMMA argdecl)*)?  RPAREN (TWOPOINT op=('inteiro'|'real'|'string'|'booleano'))? blck;

//stat is an instruction
stat   : WRITE expr PTCOMMA                             # Write
       | blck                                           # Block
       | WHILE LPAREN expr RPAREN stat                  # While
       | IF LPAREN expr RPAREN stat (ELSE stat)?        # IfElse
       | VARNAME AFFECT expr PTCOMMA                    # Affect
       | RETURN expr? PTCOMMA                           # Return
       | VARNAME LPAREN (expr (COMMA expr)*)? RPAREN PTCOMMA    # StatFunctionCall
       | PTCOMMA                                        # Empty
       ;

//had to separate block from stat because we also need to use it in function
blck  : START decl* stat* END ;

expr   : LPAREN expr RPAREN                             # Parens
       | op=(MINUS|NOT) expr                            # UminusUnot
       | expr op=(MULT|DIV|MOD) expr                    # MultDivMod
       | expr op=(PLUS|MINUS) expr                      # AddSub
       | expr op=(LESS|LESSEQ|GRTR|GRTREQ) expr         # LessGrtr
       | expr op=(EQ|DIFF) expr                         # EqDiff
       | expr AND expr                                  # And
       | expr OR expr                                   # Or
       | VARNAME LPAREN (expr (COMMA expr)*)? RPAREN    # ExprFunctionCall
       | INT                                            # Int
       | REAL                                           # Real
       | STRING                                         # String
       | BOOLEAN                                        # Boolean
       | VARNAME                                        # Var
       ;

INT      : DIGIT+ ;
REAL     : DIGIT+ POINT DIGIT+ ;
BOOLEAN  : TRUE
         | FALSE ;
STRING   : '"' ~["]* '"' ; // ~ is any character but whatever is inside the brackets
SL_COMMENT : '//' .*? (EOF|'\n') -> skip; // single-line comment
ML_COMMENT : '/*' .*? '*/' -> skip ; // multi-line comment
LPAREN   : '(' ;
RPAREN   : ')' ;
LESS     : '<' ;
LESSEQ   : '<=' ;
GRTR     : '>' ;
GRTREQ   : '>=' ;
PLUS     : '+' ;
MINUS    : '-' ;
MULT     : '*' ;
DIV      : '/' ;
MOD      : '%' ;
POINT    : '.' ;
COMMA    : ',' ;
PTCOMMA  : ';' ;
UNDERSCORE : '_' ;
TWOPOINT : ':';
AFFECT   : '<-' ;
NOT      : 'nao' ;
AND      : 'e' ;
OR       : 'ou' ;
RETURN   : 'retorna' ;
FUNCTION : 'funcao' ;
EQ       : 'igual' ;
DIFF     : 'diferente' ;
WRITE    : 'escreve' ;
START    : 'inicio' ;
END      : 'fim' ;
WHILE    : 'enquanto' ;
IF       : 'se' ;
ELSE     : 'senao' ;
TRUE     : 'verdadeiro' ;
FALSE    : 'falso' ;
VARNAME : (LETTER|UNDERSCORE) (LETTER|DIGIT|UNDERSCORE)* ; //any varname has to start with a letter or underscore, and then can have any sequence of letters, digits and underscores
WS       : [ \t\r\n]+ -> skip ;

fragment
DIGIT    : [0-9] ;
LETTER   : [a-zA-Z];