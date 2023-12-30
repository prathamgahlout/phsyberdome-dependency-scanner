package com.phsyberdome.drona;

import com.phsyberdome.drona.Models.Dependencies;
import com.phsyberdome.drona.Models.Module;
import com.phsyberdome.drona.Models.DependencyManager;
import java.util.ArrayList;

/**
 *
 * @author pgahl
 */
public interface PluginInterface {
    
    public DependencyManager getPackageManager();
    public void readModules();
    public ArrayList<Module> getModules();
}
