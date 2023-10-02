

package com.gahloutsec.drona.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gahloutsec.drona.Models.Dependencies;
import com.gahloutsec.drona.Models.DependencyScanResult;
import java.util.ArrayList;

/**
 *
 * @author Pratham Gahlout
 */
public class JSONHelper {
    
    public static String scanResultToJSONObject(DependencyScanResult result) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }

}
