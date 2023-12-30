

package com.phsyberdome.drona.Plugins;

import com.phsyberdome.drona.CLIHelper;
import com.phsyberdome.drona.Configuration;
import com.phsyberdome.drona.Models.Dependencies;
import com.phsyberdome.drona.Models.DependencyManager;
import com.phsyberdome.drona.PluginInterface;
import com.phsyberdome.drona.licensedetector.LicenseDetector;
import com.phsyberdome.drona.Models.Module;
import com.phsyberdome.drona.Models.Pair;
import com.phsyberdome.drona.Utils.FileUtil;
import com.phsyberdome.drona.Utils.PomReader;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.fusesource.jansi.Ansi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Pratham Gahlout
 * 
 * Read the pom file
 * Extract dependencies
 * Build URL and detect the license
 */
public class JavaMavenPlugin implements PluginInterface 
{
    private DependencyManager pm;
    
    private ArrayList<Module> modules;
    private final LicenseDetector licenseDetector;

    public JavaMavenPlugin(LicenseDetector licenseDetector) {
        this.licenseDetector = licenseDetector;
    }
    
    

    @Override
    public DependencyManager getPackageManager() {
        return  pm;
    }

    @Override
    public void readModules() {
        modules = new ArrayList<>();
        pm = new DependencyManager("java-maven","null");
        
        //Read the pom.xml
        File file = FileUtil.searchFile(Configuration.getConfiguration().getBasePath().toFile(), "(.*\\.(pom|POM))|(pom\\.(xml|XML))");
        if(file == null) {
            CLIHelper.updateCurrentLine("pom file not found in project",Ansi.Color.RED);
            
            return;
        }
        Path path = file.toPath();
        if(path!=null && path.toFile().exists()){
            // Read the xml
            Document doc = PomReader.readPom(path);
            if(doc == null) {
                CLIHelper.updateCurrentLine("Couldn't read pom file",Ansi.Color.RED);
                return;
            }
            NodeList list = doc.getElementsByTagName("dependency");
            
            for(int i=0;i<list.getLength();i++){
                Node node = list.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    if(!node.getParentNode().getNodeName().equals("dependencies")){
                        continue;
                    }
                    Element element = (Element) node;
                    
                    
                    String artifactId = PomReader.extractAttributeFromNode(element, "artifactId");
                    String groupId = PomReader.extractAttributeFromNode(element, "groupId");
                    String version = PomReader.extractAttributeFromNode(element, "version");
                    String scope = PomReader.extractAttributeFromNode(element, "scope");
                    if(scope!=null && (scope.equals("test") || scope.equals("import"))){
                        continue;
                    }
                    // if we have a property as version.
                    if(version!=null)
                        version = PomReader.resolvePropertyValue(version, doc);
                    // If we dont even have version mentioned.
                    else
                        version = PomReader.getVersionFromParent(artifactId,PomReader.getParentPOM(doc));
                    
                    Module m = new Module(artifactId,version);
                    m.setSupplier(groupId);
                    if(version!=null){
                        getLicenseAndTransitiveDependenciesForModule(m);
                    }else{
                        CLIHelper.updateCurrentLine("Cannot proceed! REASON: Couldnt get version for "+m.getName(),Ansi.Color.CYAN);
                    }
                    modules.add(m);
                }
            }
        }else{
            CLIHelper.updateCurrentLine("pom file not found at " + path.toAbsolutePath().toString(),Ansi.Color.RED);
        }
        licenseDetector.printScanStats();
    }
    
    
    
    private void getLicenseAndTransitiveDependenciesForModule(Module root) {
        CLIHelper.updateCurrentLine("Building dep tree for "+root.getName(),Ansi.Color.CYAN);
        String repoUrlString = buildRepositoryUrl(root);
        String loc = Configuration.getConfiguration().getCloneLocation().toString();
        Path path = FileUtil.getFilePathFromURL(repoUrlString,loc);
        if(path == null){
            return;
        }
        buildDependencyTree(root,path);
        Pair<String,String> detectionResult = licenseDetector.detect(path.toString());
        root.setLicense(detectionResult.first);
        root.setAnalyzedContent(detectionResult.second);
    }

    private void buildDependencyTree(Module root, Path pathToModule) {
        
        //Read the pom.xml
        File file = FileUtil.searchFile(FileSystems.getDefault().getPath(pathToModule.toString()).toFile(), "(.*\\.(pom|POM))|(pom\\.(xml|XML))");
        if(file == null) {
            CLIHelper.updateCurrentLine("pom file not found in downloaded jar",Ansi.Color.CYAN);
            // May be jar doesnt come with a pom
            // Should I check for it at the repository?
            
            String urlToPomString = PomReader.buildUrlForPomFile(root.getSupplier(), root.getName(), root.getVersion());
            file = FileSystems.getDefault().getPath("/.drona/temp/poms/" + "pom.xml").toFile();
            if(file.exists()) {
                FileUtil.deleteDirectory(file);
            }
            try {
                FileUtils.copyURLToFile(new URL(urlToPomString), file);
            } catch (MalformedURLException ex) {
                Logger.getLogger(PomReader.class.getName()).log(Level.SEVERE, null, ex);
                return;
            } catch (IOException ex) {
                Logger.getLogger(PomReader.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            CLIHelper.updateCurrentLine("Downloaded pom from "+urlToPomString,Ansi.Color.GREEN);
        }
        Path path = file.toPath();
        
        if(path!=null && path.toFile().exists()){
            // Read the xml
            Document doc = PomReader.readPom(path);
            if(doc == null) {
                CLIHelper.updateCurrentLine("Couldn't read pom file",Ansi.Color.RED);
                return;
            }
            NodeList list = doc.getElementsByTagName("dependency");

            for(int i=0;i<list.getLength();i++){
                Node node = list.item(i);
                if(node.getNodeType() == Node.ELEMENT_NODE) {
                    if(!node.getParentNode().getNodeName().equals("dependencies")){
                        continue;
                    }
                    Element element = (Element) node;
                    String artifactId = PomReader.extractAttributeFromNode(element, "artifactId");
                    String groupId = PomReader.extractAttributeFromNode(element, "groupId");
                    String version = PomReader.extractAttributeFromNode(element, "version");
                    String scope = PomReader.extractAttributeFromNode(element, "scope");
                    if(scope!=null && (scope.equals("test") || scope.equals("import"))){
                        continue;
                    }
                    // if we have a property as version.
                    if(version!=null)
                        version = PomReader.resolvePropertyValue(version, doc);
                    // If we dont even have version mentioned.
                    else
                        version = PomReader.getVersionFromParent(artifactId,PomReader.getParentPOM(doc));
                    
                    Module m = new Module(artifactId,version);
                    m.setSupplier(groupId);
                    if(version!=null){
                        getLicenseAndTransitiveDependenciesForModule(m);
                    }else{
                        CLIHelper.updateCurrentLine("Cannot proceed! REASON: Couldnt get version for "+m.getName(),Ansi.Color.RED);
                    }
                    root.addToDependencies(m);
                }
            }
            
        }else{
            CLIHelper.updateCurrentLine("pom file not found at " + path.toAbsolutePath().toString(),Ansi.Color.RED);
        }
    }
    
    
    private static String buildRepositoryUrl(Module root) {
        String repoUrlString = "https://repo1.maven.org/maven2/";
        String[] domainSplit = root.getSupplier().split("\\.");
        for(String token : domainSplit) {
            repoUrlString += token;
            repoUrlString += "/";
        }
        
        repoUrlString += root.getName() + "/";
        repoUrlString += root.getVersion();
        repoUrlString += ("/" + root.getName() + "-" + root.getVersion() + ".jar");
        return repoUrlString;
    }
    
    @Override
    public ArrayList<Module> getModules() {
        return modules;
    }

}
