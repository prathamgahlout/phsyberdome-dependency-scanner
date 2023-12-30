package com.phsyberdome.drona;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.phsyberdome.drona.Models.DependencyManager;
import com.phsyberdome.drona.Models.DependencyScanResult;
import com.phsyberdome.drona.Models.DependencyTree;
import com.phsyberdome.drona.Models.MODE;
import com.phsyberdome.drona.Models.Pair;
import com.phsyberdome.drona.Models.RunMode;
import com.phsyberdome.drona.Utils.FileUtil;
import com.phsyberdome.drona.Utils.JSONHelper;
import com.phsyberdome.drona.Utils.NetworkHelper;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fusesource.jansi.Ansi.Color;

/**
 *
 * @author pgahl
 */
public class Drona {
            
    public static void main(String[] args) {
        
        CLIHelper.initialize();
               
        CLIHelper.printBrandAscii();
        
        RunMode mode = CLIHelper.parseCommands(args);
        if(mode == null){
            CLIHelper.printHelp();
            System.exit(0);
        }
        
        Configuration configuration = Configuration.getConfiguration();
        

        if(mode.getMode() == MODE.SCAN){
            printOrganizationalData();
            if(mode.getTargetPath()!=null)
                configuration.setBasePath(mode.getTargetPath());
            // Else we scan the current dir only
            else
            {
                CLIHelper.printLine("Unable to resolve given path\nScanning current director!",Color.CYAN);
                configuration.setBasePath(".");
            }
            DependencyExcavator excavator = new DependencyExcavator();

            DependencyScanResult result = excavator.excavate();

            printResult(result);
            
            /**
             *  TODO: Send data to services for processing & show the processed information
             */

            FileUtil.cleanup();
        }else if(mode.getMode() == MODE.MONITOR){
            return;
        }else if(mode.getMode() == MODE.HELP){
            CLIHelper.printHelp();
            return;
        }
    }
    
    
    private static void printResult(DependencyScanResult result) {
        for(DependencyManager manager: result.getResult()){
            String json = JSONHelper.convertToJson(manager);
            DependencyTree tree = new DependencyTree(json);
            tree.prettyPrintTree();
        }   
    }
    
    
    private static void printOrganizationalData() {
        if(
            Configuration.getConfiguration().getAPIEndpoint() == null ||
            Configuration.getConfiguration().getOrgId() == null ||
            Configuration.getConfiguration().getUserId() == null
          ) {
            return;
        }
        String endpoint = Configuration.getConfiguration().getAPIEndpoint();
        
        ArrayList<Pair<String,String>> params = new ArrayList<Pair<String,String>>();
        params.add(new Pair<String,String>("orgId",Configuration.getConfiguration().getOrgId()));
        String orgJsonParam = JSONHelper.createJSONObjectString(
                params
        );
        String orgData = NetworkHelper.postData(endpoint + "/acc/org",orgJsonParam);
        
        params.clear();
        params.add(new Pair<String,String>("userId",Configuration.getConfiguration().getUserId()));
        String userJsonParam = JSONHelper.createJSONObjectString(
                params
        );
        
        String userData = NetworkHelper.postData(endpoint + "/acc/user", userJsonParam);
        String userName = JSONHelper.getValue("/name", userData);
        String orgName = JSONHelper.getValue("/name", orgData);
        
        
        CLIHelper.print("ORG: "+orgName, Color.YELLOW);
        CLIHelper.printDivider(Color.YELLOW);
        CLIHelper.print("USER: "+userName, Color.YELLOW);
        CLIHelper.printDivider(Color.YELLOW);
    }
}
