

package com.phsyberdome.drona;

import com.phsyberdome.drona.Models.CLI_FLAGS;
import com.phsyberdome.drona.Models.MODE;
import com.phsyberdome.drona.Models.Pair;
import com.phsyberdome.drona.Models.RunMode;
import com.phsyberdome.drona.Utils.FileUtil;
import java.util.ArrayList;
import java.util.List;
import org.fusesource.jansi.Ansi.*;
import org.fusesource.jansi.Ansi.Color.*;
import static org.fusesource.jansi.Ansi.ansi;
import org.fusesource.jansi.AnsiConsole;

/**
 *
 * @author Pratham Gahlout
 */

public class CLIHelper {
    
    public static final String CARRIAGE_RETURN = "\r";
    public static final String CROSS = " ├─";
    public static final String CORNER = " └─";
    public static final String VERTICAL = " │ ";
    public static final String SPACE_2 = "  ";
    
    public static void initialize() {
       AnsiConsole.systemInstall();
    }
    
    private static boolean isValidFlag(String flag){
        if(flag.equals("-"+CLI_FLAGS.src.toString())
                || flag.equals("-"+CLI_FLAGS.dest.toString()))
            return true;
        return false;
    }
    
    public static boolean validateCommands(String[] args){
        if(args.length == 0){
            return false;
        }
        String desiredMode = args[0];
        
        if(!(desiredMode.equals(MODE.SCAN.toString().toLowerCase())
                ||desiredMode.equals(MODE.MONITOR.toString().toLowerCase())
                ||desiredMode.equals(MODE.HELP.toString().toLowerCase()))){
            return false;
        }
        
        int NO_OF_ARGS = args.length;
        
        if(NO_OF_ARGS == 1){
            return true;
        }
        
        if(NO_OF_ARGS % 2 == 0){
            return false;
        }
        
        int i = 1;
        for(;i<NO_OF_ARGS;i+=2){
            String flag = args[i];
            String value = args[i+1];
            
            if(!isValidFlag(flag)) return false;
            
        }
        
        return true;
    }
    
    private static MODE getMode(String[] args){
        
        MODE[] values = MODE.values();
        for(MODE value: values){
            if(args[0].equals(value.toString().toLowerCase())){
                return value;
            }
        }
        return MODE.HELP;
    }
    
    private static List<Pair<CLI_FLAGS,String>> getFlags(String[] args){
        
        int i = 1;
        List<Pair<CLI_FLAGS,String>> flags = new ArrayList<>();
        for(;i<args.length;i+=2){
            flags.add(new Pair<>(CLI_FLAGS.valueOf(args[i].substring(1)),args[i+1]));
        }
        return flags;
    }
    
    public static RunMode parseCommands(String[] args){
        if(!validateCommands(args)){
            return null;
        }
        
        MODE mode = getMode(args);
        List<Pair<CLI_FLAGS,String>> flags = getFlags(args);
        
        RunMode runMode = new RunMode(mode,null,null);
        
        for(Pair<CLI_FLAGS,String> flag : flags){
            if(flag.first == CLI_FLAGS.src){
                if(FileUtil.verifyIfDirectoryExists(flag.second))
                    runMode.setTargetPath(flag.second);
            }else if(flag.first == CLI_FLAGS.dest){
                if(FileUtil.verifyIfDirectoryExists(flag.second))
                    runMode.setDestinationPath(flag.second);
            }
        }
        return runMode;
    }

    public static void printHelp(){
        System.out.println("````````````````````````````````````````````````````");
        System.out.println("DRONA-CLI");
        System.out.println("````````````````````````````````````````````````````");
        System.out.println();
        System.out.println("COMMANDS:");
        System.out.println("````````````````````````````````````````````````````");

        System.out.println("scan: Scan for open source component analysis");
        System.out.println("-src: Set directory to scan");
        System.out.println("-dest: Set directory to save the generated report");
    }
    
    
    public static void printBrandAscii() {
        final String ascii = """
                             ################################################################################
                                      __                   __                     __                         
                              .-----.|  |--..-----..--.--.|  |--..-----..----..--|  |.-----..--------..-----.
                              |  _  ||     ||__ --||  |  ||  _  ||  -__||   _||  _  ||  _  ||        ||  -__|
                              |   __||__|__||_____||___  ||_____||_____||__|  |_____||_____||__|__|__||_____|
                              |__|                 |_____|                                                   
                             ################################################################################
                             """;
        printLine(ascii, Color.GREEN);
    }
    
    public static void printDivider(Color color) {
        final String toPrint = "\n--------------------------------------------------------------------------------";
        System.out.println(ansi().fg(color).a(toPrint).reset());
        
    }
    
    public static void printLine(String data, Color color) {
        System.out.println(ansi().fg(color).a(data).reset());
    }
    
    public static void print(String data, Color color) {
        System.out.print(ansi().fg(color).a(data).reset());
    }
    
    
    public static void newLine() {
        System.out.println();
    }
    
    public static void updateCurrentLine(String updatedText, Color color) {
        System.out.print(ansi().eraseLine(Erase.FORWARD).fg(color).a(updatedText + CARRIAGE_RETURN).reset());
    }
    

    @Override
    protected void finalize() throws Throwable {
        AnsiConsole.systemUninstall();
        super.finalize();
    }
    
    
    
}
