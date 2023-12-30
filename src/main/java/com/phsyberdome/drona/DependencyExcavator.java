

package com.phsyberdome.drona;

import com.phsyberdome.drona.Models.Dependencies;
import com.phsyberdome.drona.Models.DependencyManager;
import com.phsyberdome.drona.Models.DependencyScanResult;
import com.phsyberdome.drona.Models.Module;
import java.util.ArrayList;

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
            ArrayList<Module> d = reader.readForPlugin(plugin);
            DependencyManager currentDM = plugin.getPackageManager();
            currentDM.setDependencies(d);
            excavationResult.addDependencyManagerData(currentDM);
        }
        return excavationResult;
    }
}
