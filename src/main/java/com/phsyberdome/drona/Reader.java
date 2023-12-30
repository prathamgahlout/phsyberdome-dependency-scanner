

package com.phsyberdome.drona;

import com.phsyberdome.drona.Models.Module;
import java.util.ArrayList;

/**
 *
 * @author Pratham Gahlout
 */
public class Reader {

    // Get all the list of packages from all plugins
    // Parse the Lists to a JAVA Class
    // Return the class
    
    public static ArrayList<Module> readForPlugin(PluginInterface plugin) {
        plugin.readModules();
        return plugin.getModules();
    } 
}
