

package com.gahloutsec.drona.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gahloutsec.drona.Models.Dependencies;
import com.gahloutsec.drona.Models.DependencyScanResult;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pratham Gahlout
 */
public class JSONHelper {
    
    public static String scanResultToJSONObject(DependencyScanResult result) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(result);
    }

    public static String getValue(String keyPath,String json){
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.createObjectNode();
        try {
            root = objectMapper.readTree(json);
            return root.at(keyPath).asText();
        } catch (JsonProcessingException ex) {
            Logger.getLogger(JSONHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public static List<String> getValues(String keyPath,String json){
        List<String> res = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.createObjectNode();
        try {
            root = objectMapper.readTree(json);
            JsonNode myNode = root.at(keyPath);
            Iterator it = myNode.fields();
            while(it.hasNext()){
                Map.Entry<String,JsonNode> field = (Map.Entry<String,JsonNode>) it.next();
                res.add(field.getKey());
            }
            return res;
        } catch (JsonProcessingException ex) {
            Logger.getLogger(JSONHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
