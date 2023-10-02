

package com.gahloutsec.drona.Models;

import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author Pratham Gahlout
 */
public class Dependencies implements Serializable{
    private ArrayList<Module> modules;

    public Dependencies(ArrayList<Module> modules) {
        this.modules = modules;
    }
    
    public Dependencies() {
        modules = new ArrayList<>();
    }

    public ArrayList<Module> getModules() {
        return modules;
    } 
    
    public void addToDependencies(Module module) {
        modules.add(module);
    }
    
    
}
