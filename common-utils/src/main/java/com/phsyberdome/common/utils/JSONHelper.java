

package com.phsyberdome.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.phsyberdome.common.utils.models.Pair;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.fusesource.jansi.Ansi;

/**
 *
 * @author Pratham Gahlout
 */
public class JSONHelper {
    
    public static String convertToJson(Object object) {
        ObjectMapper objectMapper = new ObjectMapper();
        String json;
        try {
            json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(object);
        }catch(JsonProcessingException e) {
            CLIHelper.updateCurrentLine("Failed to convert to json!", Ansi.Color.RED);
            return null;
        }
        return json;
    }
    
    public static <T> T convertToObj(Class<T> c,String json) {
        try{
            ObjectMapper objectMapper = new ObjectMapper();
            return (T) objectMapper.readValue(json, c);
        }catch(Exception e) {
            CLIHelper.updateCurrentLine("Failed to parse json!", Ansi.Color.RED);
        }
        return null;
    }

    public static String getValue(String keyPath,String json){
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.createObjectNode();
        try {
            root = objectMapper.readTree(json);
            return root.at(keyPath).asText();
        } catch (Exception e){
            CLIHelper.updateCurrentLine("Failed to parse json!", Ansi.Color.RED);
        }
        return null;
    }
    
    public static List<Pair<String,String>> getValues(String keyPath,String json){
        List<Pair<String,String>> res = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.createObjectNode();
        try {
            root = objectMapper.readTree(json);
            JsonNode myNode = root.at(keyPath);
            Iterator it = myNode.fields();
            while(it.hasNext()){
                Map.Entry<String,JsonNode> field = (Map.Entry<String,JsonNode>) it.next();
                res.add(new Pair<>(field.getKey(),field.getValue().toPrettyString()));
            }
            return res;
        } catch (Exception ex) {
            CLIHelper.updateCurrentLine("Failed to parse json!", Ansi.Color.RED);
        }
        return null;
    }
    
    public static List<String> getArray(String keyPath, String json) {
        List<String> res = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode root = objectMapper.createObjectNode();
        try {
            root = objectMapper.readTree(json);
            JsonNode myNode = root.at(keyPath);
            if(myNode.isArray()) {
                myNode.forEach(obj -> res.add(obj.toString()));
                return res;
            }
            throw new Exception();
        } catch (Exception ex) {
            CLIHelper.updateCurrentLine("Failed to parse json!", Ansi.Color.RED);
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


