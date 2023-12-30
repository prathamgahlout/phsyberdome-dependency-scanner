package com.phsyberdome.drona;

import java.nio.file.Path;

/**
 *
 * @author pgahl
 */
public interface ConfigurationInterface {
    
    public Path getBasePath();
    public void setBasePath(String path);
    public void setBasePath(Path path);
    public Path getCloneLocation();
    
    public String getUserId();
    public String getOrgId();
    public String getAPIEndpoint();
}
