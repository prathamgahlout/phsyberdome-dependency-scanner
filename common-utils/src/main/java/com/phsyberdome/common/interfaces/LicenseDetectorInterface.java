
package com.phsyberdome.common.interfaces;

import com.phsyberdome.common.utils.CLIHelper;
import com.phsyberdome.common.utils.models.LicenseDetectionResult;
import com.phsyberdome.common.utils.models.Pair;
import org.fusesource.jansi.Ansi;


/**
 *
 * @author pgahl
 */
public interface LicenseDetectorInterface {
        
    default public LicenseDetectionResult detect(String content) {
        var result = new LicenseDetectionResult();
        result.addProbableLicense(new Pair<>("No dependencies scanned!",1.0));
        return result;
    }
    
    default public void printScanStats() {
        CLIHelper.print("NO SCAN DATA!", Ansi.Color.MAGENTA);
    }
    
}
