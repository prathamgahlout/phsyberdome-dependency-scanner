

package com.phsyberdome.common.utils;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import org.apache.commons.io.IOUtils;

/**
 *
 * @author Pratham Gahlout
 */
public class NetworkHelper {
    
    public static boolean isValidURL(String urlString) {
        try{
            URL url = new URL(urlString);
            url.toURI();
            return true;
        }catch(Exception e){
            return false;
        }
    }
    
    public static String postData(String destUrl, String payload) {
        
        String response = "{\"ERROR\":\"NO_RESPONSE\"}";
        
        try {
            URL url = new URL(destUrl);
            /**
             * Move to HTTPS
             */
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            /**
             * TODO: Set HEADERS for the request
             */
            
            byte[] data = payload.getBytes(StandardCharsets.UTF_8);
            int length = data.length;
            
            connection.setFixedLengthStreamingMode(length);
            connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
            connection.connect();
            try(OutputStream os = connection.getOutputStream()){
                os.write(data);
            }
            StringWriter writer = new StringWriter();
            IOUtils.copy(connection.getInputStream(),writer,Charset.forName("UTF-8"));
            response = writer.toString();
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(NetworkHelper.class.getName()).log(Level.SEVERE, "Failed to create a URL");
        } catch (ProtocolException ex) {
            Logger.getLogger(NetworkHelper.class.getName()).log(Level.SEVERE, "Illegal HTTP/S Method");
        } catch (IOException ex) {
            Logger.getLogger(NetworkHelper.class.getName()).log(Level.SEVERE, "Unable to open connection!");
        }
        
        return response;
    }
    
    
    public static String getData(String endpoint) {
        String response = "NO_RESPONSE";
        try {
            URL url = new URL(endpoint);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            StringWriter writer = new StringWriter();
            IOUtils.copy(connection.getInputStream(),writer,Charset.forName("UTF-8"));
            response = writer.toString();
            
        }catch(Exception e) {
            
        }
        return response;
    }

}
