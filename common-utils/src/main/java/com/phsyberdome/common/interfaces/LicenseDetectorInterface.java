
package com.phsyberdome.common.interfaces;

import com.phsyberdome.common.utils.CLIHelper;
import com.phsyberdome.common.utils.models.Pair;
import org.fusesource.jansi.Ansi;


/**
 *
 * @author pgahl
 */
public interface LicenseDetectorInterface {
        
    default public Pair<String,String> detect(String content) {
        return new Pair<>("Detector not initialized!","1.0");
    }
    
    default public void printScanStats() {
        CLIHelper.print("NO SCAN DATA!", Ansi.Color.MAGENTA);
    }
    
}
