

package com.phsyberdome.drona.Utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.phsyberdome.drona.Models.Pair;
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
    
    public static String convertToJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try {
            json = objectMapper.writeValueAsString(object);
        }catch(JsonProcessingException e) {
            Logger.getLogger(JSONHelper.class.getCanonicalName()).log(Level.WARNING, e.getLocalizedMessage());
            return null;
        }
        return json;
    }
    
    public static <T> T convertToObj(Class<T> c,String json) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            return (T) objectMapper.readValue(json, c);
        }catch(Exception e) {
            Logger.getLogger(JSONHelper.class.getCanonicalName()).log(Level.WARNING, e.getLocalizedMessage());
        }
        return null;
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
    
    public static String createJSONObjectString(ArrayList<Pair<String,String>> kvs){
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        for(Pair<String,String> kv:kvs) {
            node.put(kv.first, kv.second);
        }
        return node.toPrettyString();
    }
}


