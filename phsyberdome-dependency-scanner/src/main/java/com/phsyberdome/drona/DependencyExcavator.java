

package com.phsyberdome.drona;

import com.phsyberdome.common.interfaces.PluginInterface;
import com.phsyberdome.common.utils.models.DependencyManager;
import com.phsyberdome.common.utils.models.DependencyScanResult;
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
            ArrayList<com.phsyberdome.common.utils.models.Module> d = reader.readForPlugin(plugin);
            DependencyManager currentDM = plugin.getPackageManager();
            currentDM.setDependencies(d);
            excavationResult.addDependencyManagerData(currentDM);
        }
        return excavationResult;
    }
}
