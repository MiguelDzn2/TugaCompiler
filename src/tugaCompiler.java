import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Scanner;

import Tuga.*;
import CodeGenerator.*;

public class tugaCompiler {
    static boolean showAsm;    // flag for showing generated assembly

    // Run method for when there is a file to read
    public static void run(String[] args, boolean showLexerErrors, boolean showParserErrors, boolean showTypeCheckingErrors) {
        showAsm = args.length == 2 && args[1].equals("-asm");
        String inputFileName = args[0];
        if (!inputFileName.endsWith(".tuga")) {
            System.out.println("input file must have a '.tuga' extension");
            System.exit(0);
        }
        String outputFileName = inputFileName + "bc";
        try{
            InputStream is = new FileInputStream(inputFileName);
            CharStream input = CharStreams.fromStream(is);
            compile(input, outputFileName,showLexerErrors, showParserErrors, showTypeCheckingErrors);
        }
        catch(java.io.IOException e){
            System.out.println(e);
        }
    }

    // Run method for when there is no file to read
    public static void run(boolean showLexerErrors, boolean showParserErrors, boolean showTypeCheckingErrors) {
        Scanner sc = new Scanner(System.in);
        StringBuilder sb = new StringBuilder();
        while(sc.hasNextLine()){
            sb.append(sc.nextLine());
            sb.append("\n");
        }
        String s = sb.toString();
        CharStream input = CharStreams.fromString(s);
        String outputFilename = "bytecodes.bc";
        compile(input, outputFilename, showLexerErrors, showParserErrors, showTypeCheckingErrors);
    }

    // Aux method for both run methods
    private static void compile(CharStream input, String outputFileName, boolean showLexerErrors, boolean showParserErrors, boolean showTypeCheckingErrors){
        try {
            MyErrorListener errorListener = new MyErrorListener(showLexerErrors, showParserErrors);
            TugaLexer lexer = new TugaLexer(input);
            lexer.removeErrorListeners();
            lexer.addErrorListener(errorListener);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            TugaParser parser = new TugaParser(tokens);
            parser.removeErrorListeners();
            parser.addErrorListener(errorListener);
            ParseTree tree = parser.prog();
            if (errorListener.getNumLexerErrors() > 0){
                System.out.println("Input tem erros lexicais");
                System.exit(0);
            }
            else if(errorListener.getNumParsingErrors() > 0){
                System.out.println("Input tem erros de parsing");
                System.exit(0);
            }
            else {
                Identificator identificator = new Identificator();
                identificator.visit(tree);
                IdentificatorRef identificatorRef = new IdentificatorRef(identificator);
                identificatorRef.visit(tree);
                TypeChecker typeChecker = new TypeChecker(identificator);
                if(showTypeCheckingErrors){
                    typeChecker.visit(tree);
                }
                else{
                    typeChecker.visitWithoutErrors(tree);
                }
                CodeGen codeGen = new CodeGen(typeChecker, identificator);
                codeGen.visit(tree);
                codeGen.dumpCode();
                codeGen.saveBytecodes(outputFileName);
            }
        }
        catch (java.io.IOException e) {
            System.out.println(e);
        }
    }
}
