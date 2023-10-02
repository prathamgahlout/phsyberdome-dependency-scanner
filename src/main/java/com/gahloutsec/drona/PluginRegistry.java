

package com.gahloutsec.drona;

import com.gahloutsec.drona.ConfigurationInterface;
import com.gahloutsec.drona.Plugins.NodePackageManagerPlugin;
import java.util.ArrayList;

/**
 *
 * @author Pratham Gahlout
 */
public class PluginRegistry {

    private ArrayList<PluginInterface> plugins;
    // TODO: MOVE ALL THESE TO A CONFIGURATION FILE

    public PluginRegistry() {
        plugins = new ArrayList<>();
        init();
    }
    
    private void init() {
        plugins.add(new NodePackageManagerPlugin());
    }

    public ArrayList<PluginInterface> getPlugins() {
        return plugins;
    }
    
    
    
}
