import CodeGenerator.SymbolTable;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
public class TugaCompileAndRun {
    static boolean showLexerErrors = false;
    static boolean showParserErrors = false;
    static boolean showTypeCheckingErrors = true;
    public static void main(String[] args){
        String byteCodesFileName;
        List<String> argsList = new ArrayList<>(Arrays.asList(args));
        ArrayList<String> compilerArgsList = new ArrayList<>();
        // compiler
        // Case where there is no file to be read, will also have no flags
        if (args.length < 1) {
            tugaCompiler.run(showLexerErrors, showParserErrors, showTypeCheckingErrors);
            byteCodesFileName = "bytecodes.bc";
        }
        else{
            String inputFileName = args[0];
            compilerArgsList.add(inputFileName);
            if(argsList.contains("-asm")){
                compilerArgsList.add("-asm");
            }
            tugaCompiler.run(compilerArgsList.toArray(new String[compilerArgsList.size()]), showLexerErrors, showParserErrors, showTypeCheckingErrors);
            byteCodesFileName = inputFileName + "bc";
        }

        // vm
        ArrayList<String> VMArgsList = new ArrayList<>();
        VMArgsList.add(byteCodesFileName);
        if(argsList.contains("-trace")){
            VMArgsList.add("-trace");
        }
        tugaVM.run(VMArgsList.toArray(new String[VMArgsList.size()]));
    }
}
