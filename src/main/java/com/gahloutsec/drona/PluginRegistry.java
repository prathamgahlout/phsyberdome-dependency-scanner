

package com.gahloutsec.drona;

import com.gahloutsec.drona.ConfigurationInterface;
import com.gahloutsec.drona.Plugins.NodePackageManagerPlugin;
import com.gahloutsec.drona.licensedetector.LicenseDetector;
import java.util.ArrayList;

/**
 *
 * @author Pratham Gahlout
 */
public class PluginRegistry {

    private ArrayList<PluginInterface> plugins;
    private LicenseDetector licenseDetector;
    // TODO: MOVE ALL THESE TO A CONFIGURATION FILE

    public PluginRegistry() {
        plugins = new ArrayList<>();
        licenseDetector = new LicenseDetector();
        init();
    }
    
    private void init() {
        plugins.add(new NodePackageManagerPlugin(licenseDetector));
    }

    public ArrayList<PluginInterface> getPlugins() {
        return plugins;
    }
    
    
    
}
