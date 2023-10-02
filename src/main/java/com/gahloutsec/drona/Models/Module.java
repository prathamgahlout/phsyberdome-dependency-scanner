package com.gahloutsec.drona.Models;

import java.util.ArrayList;

/**
 *
 * @author pgahl
 */


public class Module {
    private String name;
    private String version;
    private ArrayList<Module> dependencies;

    public Module(String name, String version) {
        this.name = name;
        this.version = version;
        dependencies = new ArrayList<>();
    }

    public Module(String name, String version, ArrayList<Module> dependencies) {
        this.name = name;
        this.version = version;
        this.dependencies = dependencies;
    }

    public ArrayList<Module> getDependencies() {
        return dependencies;
    }

    public void setDependencies(ArrayList<Module> dependencies) {
        this.dependencies = dependencies;
    }


    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }
    
    public void addToDependencies(Module m) {
        dependencies.add(m);
    }
        
}
