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
import java.nio.file.Path;
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
        
        switch(mode.getMode()) {
            case SCAN:
                scan(mode.getTargetPath(),mode.getDestinationPath());
                break;
            case MONITOR:
                monitor(mode.getTargetPath());
                break;
            case HELP:
                CLIHelper.printHelp();
                break;
            default:
                CLIHelper.printHelp();
        }
    }
    
    private static void scan(String targetPath, String destPath){
        printOrganizationalData();
        if(targetPath!=null)
            Configuration.getConfiguration().setBasePath(targetPath);
        // Else we scan the current dir only
        else
        {
            CLIHelper.printLine("Unable to resolve given path\nScanning current director!",Color.CYAN);
            Configuration.getConfiguration().setBasePath(".");
        }
        DependencyExcavator excavator = new DependencyExcavator();

        DependencyScanResult result = excavator.excavate();

        printResult(result);

        /**
         *  TODO: Send data to services for processing & show the processed information
         */
        
        /**
         * TODO: Save the report at destPath
         */

        FileUtil.cleanup();
    }
    
    private static void monitor(String url) {
        if(url==null || !NetworkHelper.isValidURL(url)){
            CLIHelper.printLine("INVALID URL TO SCAN!", Color.RED);
            return;
        }
        Path targetPath = FileUtil.getFilePathFromURL(url, Configuration.getConfiguration().getCloneLocation().toString());
        scan(targetPath.toString(), null);
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
            Configuration.getConfiguration().getFeedEndpoint()== null ||
            Configuration.getConfiguration().getOrgId() == null ||
            Configuration.getConfiguration().getUserId() == null ||
            Configuration.getConfiguration().getUserDataEndpoint() == null ||
            Configuration.getConfiguration().getOrgDataEndpoint() == null ||
            Configuration.getConfiguration().getAuthToken() == null
          ) {
            return;
        }
        
        ArrayList<Pair<String,String>> params = new ArrayList<Pair<String,String>>();
        params.add(new Pair<String,String>("orgId",Configuration.getConfiguration().getOrgId()));
        String orgJsonParam = JSONHelper.createJSONObjectString(
                params
        );
        String orgData = NetworkHelper.postData(Configuration.getConfiguration().getOrgDataEndpoint(),orgJsonParam);
        
        params.clear();
        params.add(new Pair<String,String>("userId",Configuration.getConfiguration().getUserId()));
        String userJsonParam = JSONHelper.createJSONObjectString(
                params
        );
        
        String userData = NetworkHelper.postData(Configuration.getConfiguration().getUserDataEndpoint(), userJsonParam);
        String userName = JSONHelper.getValue("/name", userData);
        String orgName = JSONHelper.getValue("/name", orgData);
        
        
        CLIHelper.print("ORG: "+orgName, Color.YELLOW);
        CLIHelper.printDivider(Color.YELLOW);
        CLIHelper.print("USER: "+userName, Color.YELLOW);
        CLIHelper.printDivider(Color.YELLOW);
    }
}
