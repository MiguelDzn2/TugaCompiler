import CodeGenerator.Identificator;
import CodeGenerator.SymbolTable;
import VM.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class tugaVM {

    public static void run(String[] args) {
        if (args.length < 1) {
            System.out.println("Usage: java tugaVM <filename> [-trace]");
            System.exit(0);
        }
        boolean trace = args.length == 2 && args[1].equals("-trace");
        String filename = args[0];
        try {
            byte[] bytecodes = loadBytecodes(filename);
            if (trace) {
                System.out.println("Bytecodes");
                dumpBytecodes(bytecodes);
            }
            vm VM = new vm(bytecodes, trace);
            VM.run();
        }
        catch (java.io.IOException e) {
            System.out.println(e);
        }
    }

    public static void dumpBytecodes(byte [] bytecodes) {
        /*
        for (byte b : bytecodes)
            System.out.print(b + " ");
        System.out.println();
        */
        StringBuilder s = new StringBuilder();
        for (byte b : bytecodes)
            s.append(String.format("%02X ", b));
        System.out.println(s);
    }

    public static byte[] loadBytecodes(String filename) throws IOException {
        try {
            File file = new File(filename);
            byte [] bytecodes = new byte[(int) file.length()];
            try(FileInputStream fis = new FileInputStream(file)) {
                fis.read(bytecodes);
            }
            return bytecodes;
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

}
