
package com.phsyberdome.plugin.cargo;

/**
 *
 * @author Pratham Gahlout
 */
public class CargoVersionHelper {
    
    /*
    https://doc.rust-lang.org/cargo/reference/specifying-dependencies.html
    */
    
    private static boolean isResolved(String v) {
        return true;
    }
    
    
    public static String resolveVersion(String reqSpec) {
        return reqSpec;
    }
    
}
