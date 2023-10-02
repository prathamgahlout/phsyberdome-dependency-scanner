

package com.gahloutsec.drona;

import com.gahloutsec.drona.Models.Dependencies;
import java.util.ArrayList;

/**
 *
 * @author Pratham Gahlout
 */
public class Reader {

    // Get all the list of packages from all plugins
    // Parse the Lists to a JAVA Class
    // Return the class
    
    public static Dependencies readForPlugin(PluginInterface plugin) {
        plugin.readModules();
        return plugin.getModules();
    } 
}
