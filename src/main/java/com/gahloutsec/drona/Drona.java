package com.gahloutsec.drona;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.gahloutsec.drona.Models.DependencyScanResult;
import com.gahloutsec.drona.Utils.FileUtil;
import com.gahloutsec.drona.Utils.JSONHelper;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author pgahl
 */
public class Drona {
    
    
    public static void main(String[] args) {
        
        //TODO: Arguments parse (CLI helper)
        
        
        Configuration configuration = Configuration.getConfiguration();
        configuration.setBasePath("");
        DependencyExcavator excavator = new DependencyExcavator();
        
        DependencyScanResult result = excavator.excavate();
        
        try {
            String s = JSONHelper.scanResultToJSONObject(result);
            System.out.println(s);
        } catch (JsonProcessingException ex) {
            Logger.getLogger(Drona.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        FileUtil.cleanup();
    }
    
}
