

package com.phsyberdome.drona;

import com.phsyberdome.drona.ConfigurationInterface;
import com.phsyberdome.drona.Plugins.JavaMavenPlugin;
import com.phsyberdome.drona.Plugins.NodePackageManagerPlugin;
import com.phsyberdome.drona.licensedetector.LicenseDetector;
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
        plugins.add(new JavaMavenPlugin(licenseDetector));
    }

    public ArrayList<PluginInterface> getPlugins() {
        return plugins;
    }
    
    
    
}
