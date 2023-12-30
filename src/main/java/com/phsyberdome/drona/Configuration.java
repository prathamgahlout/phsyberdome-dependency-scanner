

package com.phsyberdome.drona;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pratham Gahlout
 */
public class Configuration implements ConfigurationInterface{
    
    private Path basePath;
    private String licenseDataURL;
    private static Configuration _instance = null;
    private final Path cloneLocation = FileSystems.getDefault().getPath(".").resolve(".drona/temp/remote_clones/");
    
    /*
    TODO: Create a separate model for Config File Data
    */
    private String USER_ID;
    private String ORG_ID;
    private String USER_DATA_ENDPOINT;
    private String ORG_DATA_ENDPOINT;
    private String AUTH_TOKEN;
    private String FEED_ENDPOINT;
    
    private Configuration(){
        if(!cloneLocation.toFile().exists()){
            cloneLocation.toFile().mkdirs();
        }
        
        // Read all these configurations from the configuration file
        licenseDataURL = "https://spdx.org/licenses/";
        
        readConfigFile("phsyberdome-cli.config");
    }
    
    public static Configuration getConfiguration() {
        if(_instance == null) {
            _instance = new Configuration();
        }
        return _instance;
    }
    
    
    private void readConfigFile(String filename) {
        File configFile = FileSystems.getDefault().getPath(".").resolve(filename).toFile();
        if(configFile.exists()) {
            try {
                FileReader fr = new FileReader(configFile);
                BufferedReader br = new BufferedReader(fr);
                String line="";
                while((line=br.readLine())!=null) {
                    String[] config = line.split("=");
                    if(config.length != 2){
                        throw new IOException("INVALID CONFIGURATION FILE!");
                    }
                    switch(config[0]){
                        case "ORG_ID":
                            ORG_ID = config[1];
                            break;
                        case "USER_ID":
                            USER_ID = config[1];
                            break;
                        case "SERVICE_ENDPOINT":
                            FEED_ENDPOINT = stripOfSlashes(config[1]);
                            break;
                        case "USER_DATA_ENDPOINT":
                            USER_DATA_ENDPOINT = stripOfSlashes(config[1]);
                            break;
                        case "ORG_DATA_ENDPOINT":
                            ORG_DATA_ENDPOINT = stripOfSlashes(config[1]);
                            break;
                        case "AUTH_TOKEN":
                            AUTH_TOKEN = config[1];
                            break;
                    }
                   
                }
                br.close();
                fr.close();
            } catch (FileNotFoundException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "UNABLE TO READ CONFIG FILE", ex);
            } catch (IOException ex) {
                Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "UNABLE TO READ CONFIG FILE", ex);
            }
        }else {
            Logger.getLogger(Configuration.class.getName()).log(Level.SEVERE, "No config file found!");
        }
    }
    
    
    @Override
    public Path getBasePath() {
        return basePath;
    }

    @Override
    public void setBasePath(String p) {
        this.basePath = FileSystems.getDefault().getPath(p);
    }

    @Override
    public void setBasePath(Path path) {
        this.basePath = path;
    }
    
    @Override
    public Path getCloneLocation() {
        return cloneLocation;
    }

    public String getLicenseDataURL() {
        return licenseDataURL;
    }

    @Override
    public String getUserId() {
        return USER_ID;
    }

    @Override
    public String getOrgId() {
        return ORG_ID;
    }

    @Override
    public String getFeedEndpoint() {
        return FEED_ENDPOINT;
    }

    @Override
    public String getUserDataEndpoint() {
        return USER_DATA_ENDPOINT;
    }

    @Override
    public String getOrgDataEndpoint() {
        return ORG_DATA_ENDPOINT;
    }

    @Override
    public String getAuthToken() {
        return AUTH_TOKEN;
    }
    

    private String stripOfSlashes(String url) {
        while(url.endsWith("/")){
            url = url.substring(0,url.length()-1);
        }
        return url;
    }
    
    
    
}
