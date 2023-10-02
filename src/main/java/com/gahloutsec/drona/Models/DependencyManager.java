

package com.gahloutsec.drona.Models;

import java.util.ArrayList;

/**
 *
 * @author Pratham Gahlout
 */
public class DependencyManager {

    private String name;
    private String version;
    private Dependencies dependencies;

    public DependencyManager(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public Dependencies getDependencies(){
        return dependencies;
    }

    public void setDependencies(Dependencies modules) {
        this.dependencies = modules;
    }
    
    public void put(Module m) {
        dependencies.addToDependencies(m);
    }
    
   

    
    
}
