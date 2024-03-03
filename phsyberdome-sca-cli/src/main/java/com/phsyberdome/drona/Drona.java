package com.phsyberdome.drona;

import com.phsyberdome.common.utils.CLIHelper;
import com.phsyberdome.common.utils.models.DependencyManager;
import com.phsyberdome.common.utils.models.DependencyScanResult;
import com.phsyberdome.common.utils.models.DependencyTree;
import com.phsyberdome.common.utils.models.Pair;
import com.phsyberdome.common.utils.models.RunMode;
import com.phsyberdome.common.utils.FileUtil;
import com.phsyberdome.common.utils.JSONHelper;
import com.phsyberdome.common.utils.NetworkHelper;
import java.nio.file.Path;
import java.util.ArrayList;
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
            case SCAN -> scan(mode.getTargetPath(),mode.getDestinationPath());
            case MONITOR -> monitor(mode.getTargetPath());
            case HELP -> CLIHelper.printHelp();
            default -> CLIHelper.printHelp();
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
        CLIHelper.updateCurrentLine("Downloading repository/package...", Color.YELLOW);
        Path targetPath = FileUtil.getFilePathFromURL(url, Configuration.getConfiguration().getCloneLocation().toString());
        if(targetPath==null){
            return;
        }
        CLIHelper.updateCurrentLine("", Color.BLUE);
        CLIHelper.printLine("Downloaded repository/package", Color.BLUE);
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
        
        ArrayList<Pair<String,String>> params = new ArrayList<>();
        params.add(new Pair<>("orgId",Configuration.getConfiguration().getOrgId()));
        String orgJsonParam = JSONHelper.createJSONObjectString(
                params
        );
        String orgData = NetworkHelper.postData(Configuration.getConfiguration().getOrgDataEndpoint(),orgJsonParam);
        
        params.clear();
        params.add(new Pair<>("userId",Configuration.getConfiguration().getUserId()));
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
