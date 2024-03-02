

package com.phsyberdome.common.utils;

/**
 *
 * @author Pratham Gahlout
 */
public class CommandBuilder {
    
    
    // TODO: Rewrite the logic to handle spaces
    public static String[] buildCommandWith(String cmd) {
        String[] s = cmd.split(" ");
        return s;
    }

}
