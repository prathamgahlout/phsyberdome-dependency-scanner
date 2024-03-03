

package com.phsyberdome.drona;

import com.phsyberdome.licensedetector.LicenseDetector;
import com.phsyberdome.common.interfaces.LicenseDetectorInterface;
import com.phsyberdome.common.interfaces.PluginInterface;

import java.util.ServiceLoader;

/**
 *
 * @author Pratham Gahlout
 */
public class PluginRegistry {

    private ServiceLoader<PluginInterface> plugins;
    private LicenseDetectorInterface licenseDetector;
    // TODO: MOVE ALL THESE TO A CONFIGURATION FILE

    public PluginRegistry() {
        licenseDetector = new LicenseDetector(
                    Configuration.getConfiguration().getCloneLocation(),
                    Configuration.getConfiguration().getLicenseDataUrl()
                );
        init();
    }
    
    private void init() {
         this.plugins = ServiceLoader.load(PluginInterface.class);
         plugins.forEach(plugin -> {
             plugin.setCloneLocation(Configuration.getConfiguration().getCloneLocation());
             plugin.setRootPath(Configuration.getConfiguration().getBasePath());
             plugin.setLicenseDetector(this.licenseDetector);
         });
    }

    public ServiceLoader<PluginInterface> getPlugins() {
        return plugins;
    }
    
    
    
}
