package com.gahloutsec.drona;

import com.gahloutsec.drona.Models.Dependencies;
import com.gahloutsec.drona.Models.Module;
import com.gahloutsec.drona.Models.DependencyManager;
import java.util.ArrayList;

/**
 *
 * @author pgahl
 */
public interface PluginInterface {
    
    public DependencyManager getPackageManager();
    public void readModules();
    public Dependencies getModules();
}
