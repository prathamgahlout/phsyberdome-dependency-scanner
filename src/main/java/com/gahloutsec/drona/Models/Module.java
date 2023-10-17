package com.gahloutsec.drona.Models;

import java.util.ArrayList;

/**
 *
 * @author pgahl
 */


public class Module {
    private String name;
    private String version;
    private String license;
    private String analyzedContent;
    private String supplier;
    private ArrayList<Module> dependencies;
    private String dependencyType;

    public Module(String name, String version) {
        this.name = name;
        this.version = version;
        dependencies = new ArrayList<>();
        this.license = "null";
        this.supplier = "null";
        this.analyzedContent = "null";
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

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public String getSupplier() {
        return supplier;
    }

    public void setSupplier(String supplier) {
        this.supplier = supplier;
    }

    public String getAnalyzedContent() {
        return analyzedContent;
    }

    public void setAnalyzedContent(String analyzedContent) {
        this.analyzedContent = analyzedContent;
    }
        
    
    
    
}
