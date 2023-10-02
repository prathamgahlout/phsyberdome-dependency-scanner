

package com.gahloutsec.drona;

import com.gahloutsec.drona.Models.Dependencies;
import com.gahloutsec.drona.Models.DependencyManager;
import com.gahloutsec.drona.Models.DependencyScanResult;

/**
 *
 * @author Pratham Gahlout
 */
public class DependencyExcavator {
    
    private Reader reader;
    private PluginRegistry registry;
    
    private DependencyScanResult excavationResult;

    public DependencyExcavator() {
        registry = new PluginRegistry();
        reader = new Reader();
        excavationResult = new DependencyScanResult();
    }
    
    
    public DependencyScanResult excavate() {
        for(PluginInterface plugin: registry.getPlugins()) {
            Dependencies d = reader.readForPlugin(plugin);
            DependencyManager currentDM = plugin.getPackageManager();
            currentDM.setDependencies(d);
            excavationResult.addDependencyManagerData(currentDM);
        }
        return excavationResult;
    }
}
