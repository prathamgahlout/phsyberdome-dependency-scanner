

package com.phsyberdome.drona.Models;

/**
 *
 * @author Pratham Gahlout
 */
public class RunMode {
    
    private MODE mode;
    
    private String targetPath;
    
    private String destinationPath;

    public RunMode(MODE mode, String targetPath, String destinationPath) {
        this.mode = mode;
        this.targetPath = targetPath;
        this.destinationPath = destinationPath;
    }

    public MODE getMode() {
        return mode;
    }

    public void setMode(MODE mode) {
        this.mode = mode;
    }

    public String getTargetPath() {
        return targetPath;
    }

    public void setTargetPath(String targetPath) {
        this.targetPath = targetPath;
    }

    public String getDestinationPath() {
        return destinationPath;
    }

    public void setDestinationPath(String destinationPath) {
        this.destinationPath = destinationPath;
    }
    
    
    
    
}
