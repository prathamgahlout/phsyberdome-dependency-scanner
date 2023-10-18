package com.gahloutsec.drona;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gahloutsec.drona.Models.DependencyScanResult;
import com.gahloutsec.drona.Models.MODE;
import com.gahloutsec.drona.Models.RunMode;
import com.gahloutsec.drona.Utils.FileUtil;
import com.gahloutsec.drona.Utils.JSONHelper;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pgahl
 */
public class Drona {
    
    
    public static void main(String[] args) {
        
        RunMode mode = CLIHelper.parseCommands(args);
        if(mode == null){
            CLIHelper.printHelp();
            System.exit(0);
        }
        
        Configuration configuration = Configuration.getConfiguration();
        
        if(mode.getMode() == MODE.SCAN){
        
            if(mode.getTargetPath()!=null)
                configuration.setBasePath(mode.getTargetPath());
            // Else we scan the current dir only
            else
            {
                System.out.println("Unable to resolve given path\nScanning current director!");
                configuration.setBasePath(".");
            }
            DependencyExcavator excavator = new DependencyExcavator();

            DependencyScanResult result = excavator.excavate();

            try {
                String s = JSONHelper.scanResultToJSONObject(result);
                System.out.println(s);
            } catch (JsonProcessingException ex) {
                Logger.getLogger(Drona.class.getName()).log(Level.SEVERE, null, ex);
            }

            FileUtil.cleanup();
        }else if(mode.getMode() == MODE.MONITOR){
            return;
        }else if(mode.getMode() == MODE.HELP){
            CLIHelper.printHelp();
            return;
        }
    }
    
}
