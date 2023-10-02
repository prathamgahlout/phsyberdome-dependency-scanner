

package com.gahloutsec.drona.Models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Pratham Gahlout
 */
public class DependencyScanResult {
    
    private final ArrayList<DependencyManager> result;

    public DependencyScanResult(ArrayList<DependencyManager> result) {
        this.result = result;
    }
    public DependencyScanResult() {
        result = new ArrayList<>();
    }
    

    public ArrayList<DependencyManager> getResult() {
        return result;
    }
    
    public void addDependencyManagerData(DependencyManager dm){
        result.add(dm);
    }

}
