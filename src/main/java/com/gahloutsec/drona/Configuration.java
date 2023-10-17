

package com.gahloutsec.drona;

import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 *
 * @author Pratham Gahlout
 */
public class Configuration implements ConfigurationInterface{
    
    private Path basePath;
    
    private static Configuration _instance = null;
    private final Path cloneLocation = FileSystems.getDefault().getPath(".").resolve(".drona/temp/remote_clones/");
    
    private Configuration(){
        if(!cloneLocation.toFile().exists()){
            cloneLocation.toFile().mkdirs();
        }
    }
    
    public static Configuration getConfiguration() {
        if(_instance == null) {
            _instance = new Configuration();
        }
        return _instance;
    }
    
    
    
    @Override
    public Path getBasePath() {
        return basePath;
    }

    @Override
    public void setBasePath(String p) {
        this.basePath = Paths.get(p);
    }

    @Override
    public void setBasePath(Path path) {
        this.basePath = path;
    }
    
    @Override
    public Path getCloneLocation() {
        return cloneLocation;
    }
    
}
