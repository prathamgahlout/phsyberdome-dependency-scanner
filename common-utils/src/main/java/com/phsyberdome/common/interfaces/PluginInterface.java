package com.phsyberdome.common.interfaces;

import com.phsyberdome.common.utils.models.DependencyManager;
import com.phsyberdome.common.utils.models.Module;
import java.nio.file.Path;
import java.util.ArrayList;

/**
 *
 * @author pgahl
 */
public interface PluginInterface {
    
    public void setLicenseDetector(LicenseDetectorInterface detector);
    public void setRootPath(Path path);
    public void setCloneLocation(Path cloneLocation);
    
    public DependencyManager getPackageManager();
    public ArrayList<Module> readModules();
    public ArrayList<Module> getModules();
}
