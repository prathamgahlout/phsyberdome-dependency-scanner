package com.gahloutsec.drona;

import java.nio.file.Path;

/**
 *
 * @author pgahl
 */
public interface ConfigurationInterface {
    
    public Path getBasePath();
    public void setBasePath(String path);
    public void setBasePath(Path path);
}
