

package com.phsyberdome.drona.Utils;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.commons.io.FileUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Pratham Gahlout
 */
public class PomReader {
    
    public static boolean isProperty(String prop) {
        if(prop == null){
            return false;
        }
        Pattern propPattern = Pattern.compile("\\$\\{.*\\}");
        return propPattern.matcher(prop).matches();
    }
    
    public static Document readPom(Path path) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try{
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            // parse XML file
            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(path.toFile());

            doc.getDocumentElement().normalize();
            return doc;
        }catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public static String resolvePropertyValue(String property, Document pom) {
        if(pom == null){
            System.out.println("The property "+property+" could not be resolved!");
            return null;
        }
        if(!isProperty(property)) {
            return property;
        }
        // Read properties
        NodeList properties = pom.getElementsByTagName("properties");
        if(properties.getLength()>0){
            for(int i=0;i<properties.getLength();i++){
                Element node = (Element) properties.item(i);
                NodeList props = node.getElementsByTagName("*");
                
                for(int k=0;k<props.getLength();k++){
                    Element elem = (Element) props.item(k);
                    
                    if(property.equals("${"+props.item(k).getNodeName() + "}")) {
                        String value =  elem.getTextContent();
                        if(isProperty(value)){
                            return resolvePropertyValue(value, pom);
                        }else{
                            return value;
                        }
                    }
                }
            }
        }else {
            System.out.println("No properties in this file");
        }
        
        //If not found in properties, search the parent and fetch
        return resolvePropertyValue(property, getParentPOM(pom));
    }
    
    public static String getVersionFromParent(String artifactId,Document doc){
        if(doc!=null){
            NodeList list = doc.getElementsByTagName("dependency");
            for(int i=0;i<list.getLength();i++){
                Node node = list.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;

                    String Id = PomReader.extractAttributeFromNode(element, "artifactId");
                    String groupId = PomReader.extractAttributeFromNode(element, "groupId");
                    String version = PomReader.extractAttributeFromNode(element, "version");
                    
                    if(Id.equals(artifactId)){
                        return resolvePropertyValue(version, doc);
                    }
                }
            }
            getVersionFromParent(artifactId, getParentPOM(doc));
        }
        
        return null;
    }
    
    public static Document getParentPOM(Document pom) {
        NodeList parents = pom.getElementsByTagName("parent");
        if(parents.getLength() <= 0){
            System.out.println("This is the root pom!");
            return null;
        }
        Element parent = (Element) parents.item(0);
        String groupId = parent.getElementsByTagName("groupId").item(0).getTextContent().trim();
        String artifactId = parent.getElementsByTagName("artifactId").item(0).getTextContent().trim();
        String version = parent.getElementsByTagName("version").item(0).getTextContent();
        //version = resolvePropertyValue(version, pom);
        String urlToParentPom = buildUrlForPomFile(groupId, artifactId, version);
        File file = FileSystems.getDefault().getPath("/.drona/temp/poms/" + "pom.xml").toFile();
        if(file.exists()) {
            FileUtil.deleteDirectory(file);
        }
        try {
            FileUtils.copyURLToFile(new URL(urlToParentPom), file);
        } catch (MalformedURLException ex) {
            Logger.getLogger(PomReader.class.getName()).log(Level.SEVERE, null, ex);
            return  null;
        } catch (IOException ex) {
            Logger.getLogger(PomReader.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        Document doc = readPom(file.toPath());
        if(file.exists()){
            FileUtil.deleteDirectory(file);
        }
        return doc;
    }
    
    public static String buildUrlForPomFile(String groupId,String artifactId,String version) {
        String repoUrlString = "https://repo1.maven.org/maven2/";
        String[] domainSplit = groupId.split("\\.");
        for(String token : domainSplit) {
            repoUrlString += token;
            repoUrlString += "/";
        }
        
        repoUrlString += artifactId + "/";
        repoUrlString += version;
        repoUrlString += ("/" + artifactId + "-" + version + ".pom");
        return repoUrlString;
    }
 
    
    public static String extractAttributeFromNode(Element element, String attrib){
        NodeList n = element.getElementsByTagName(attrib);
        if(n== null || n.getLength() <=0){
            return null;
        }
        Node a = n.item(0);
        if(a == null){
            return null;
        }
        return a.getTextContent();
    }
}
