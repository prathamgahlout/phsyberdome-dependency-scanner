package com.gahloutsec.drona.Plugins;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gahloutsec.drona.Configuration;
import com.gahloutsec.drona.PluginInterface;
import com.gahloutsec.drona.Models.Dependencies;
import com.gahloutsec.drona.Models.Module;
import com.gahloutsec.drona.Models.DependencyManager;
import com.gahloutsec.drona.Models.Pair;
import com.gahloutsec.drona.SysRunner;
import com.gahloutsec.drona.Utils.FileUtil;
import com.gahloutsec.drona.Utils.JSONHelper;
import com.gahloutsec.drona.Utils.NPMVersionHelper;
import com.gahloutsec.drona.Utils.NPMVersionHelperV2;
import com.gahloutsec.drona.licensedetector.LicenseDetector;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.eclipse.jgit.util.StringUtils;

/**
 *
 * @author Pratham Gahlout
 */
public class NodePackageManagerPlugin implements PluginInterface{
    
    // TODO: MOVE ALL THIS TO A CONFIG FILE
    final private String CMD_VERSION = "cmd /c npm -v";
    final private String CMD_LIST = "cmd /c npm list -json";
    
    private DependencyManager pm;
    
    private Set<Module> scannedDependencies;
    
    private Dependencies modules;
    private LicenseDetector licenseDetector;

    public NodePackageManagerPlugin(LicenseDetector licenseDetector) {
        this.licenseDetector = licenseDetector;
        scannedDependencies = new HashSet<>();
    }
    
    

   
    @Override
    public void readModules() {
        readModulesV2();
    }
    
    public void readModulesV2() {
        modules = new Dependencies();
        String pm_version = SysRunner.run(CMD_VERSION);
        pm = new DependencyManager("npm",pm_version);
        
        File pkgLockFile = FileUtil.searchFile(Configuration.getConfiguration().getBasePath().toFile(), "package-lock.json");
        
        if(pkgLockFile!=null && pkgLockFile.exists()){
            // Read the package-lock.json
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.createObjectNode();
            try{
                node = mapper.readTree(pkgLockFile);
                String name = node.get("name").asText("null");
                String version = node.get("version").asText("null");
                Module root = new Module(name,version);
                scannedDependencies.add(root);
                getRootModuleWithDependenciesFromLockFile(root, node);
            }catch(IOException e){
                Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, e);
            }
            licenseDetector.printScanStats();
        }else {
            // Read the package.json file and resolve the versions
            File file = FileUtil.searchFile(Configuration.getConfiguration().getBasePath().toFile(), "package.json");
            if(file==null){
                System.out.println("Couldn't find package.json");
                return;
            }
            Path path = file.toPath();
            //Path path = Configuration.getConfiguration().getBasePath().resolve("package-lock.json");
            if(path.toFile().exists()){
                ObjectMapper mapper = new ObjectMapper();
                JsonNode node = mapper.createObjectNode() ;
                try {
                    node = mapper.readTree(path.toFile());    
                    String name = node.get("name").asText("null");
                    String version = node.get("version").asText("null");
                    Module root = new Module(name,version);
                    scannedDependencies.add(root);
                    getRootModuleWithDependencies(root,node);
                    modules.addToDependencies(root);
                } catch (IOException ex) {
                    Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, ex);
                    return;
                }
                licenseDetector.printScanStats();
            }else{
                System.out.println("Project does not has npm as its package manager!");
            }
        }
        
    }
    
    private void getRootModuleWithDependenciesFromLockFile(Module root,JsonNode node){
        JsonNode deps = node.get("dependencies");
        if(deps==null){
            System.out.println("No dependenices for module "+root.getName());
            return;
        }
        Iterator<Map.Entry<String,JsonNode>> iter = deps.fields();
        while(iter.hasNext()) {
            Map.Entry<String,JsonNode> field = iter.next();
            String pkgName = field.getKey();
            JsonNode body = field.getValue();
            String pkgVersion = body.get("version").asText("null");
            Module m = new Module(pkgName, pkgVersion);
            if(alreadyScanned(m)){
                Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.INFO, "Dependency "+pkgName+" already scanned");
                root.addToDependencies(getScannedModule(m));
                continue;
            }
            Path modulePath = Configuration.getConfiguration().getBasePath().resolve("node_modules/" + pkgName);
            if(!modulePath.toFile().exists()){
                String registryUrl = buildNpmRegistryUrl(pkgName, pkgVersion);
                if(registryUrl == null || registryUrl.isBlank()) continue;
                modulePath = FileUtil.getFilePathFromURL(registryUrl, Configuration.getConfiguration().getCloneLocation().toString());
            }
            
            Pair<String,String> detectionResult = licenseDetector.detect(modulePath.toString());
            String license = detectionResult.first;
            m.setLicense(license);
            m.setAnalyzedContent(detectionResult.second);
            JsonNode dependencies = body.get("dependencies");
            if(dependencies!=null){
                resolveTransitiveDependencies(m,dependencies);
            }else{
                System.out.println("No dependencies for module "+m.getName());
            }
            scannedDependencies.add(m);
            root.addToDependencies(m);
        }
        modules.addToDependencies(root);
    }
    
    private void getRootModuleWithDependencies(Module root,JsonNode node) {
        
        
        JsonNode deps = node.get("dependencies");
        if(deps==null){
            System.out.println("No dependenices for module "+root.getName());
            return;
        }
        Iterator<Map.Entry<String,JsonNode>> iter = deps.fields();
        
        while(iter.hasNext()) {
            Map.Entry<String,JsonNode> field = iter.next();
            String pkgName = field.getKey();
            String _pkgVersion = field.getValue().asText();
            System.out.println(pkgName + " version before resolution "+_pkgVersion);
            String pkgVersion = NPMVersionHelperV2.pinpointPackageVersion(pkgName, _pkgVersion);
            System.out.println(pkgName + " version after resolution "+pkgVersion);
            Module m = new Module(pkgName, pkgVersion);
            if(alreadyScanned(m)){
                Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.INFO, "Dependency "+pkgName+" already scanned");
                root.addToDependencies(getScannedModule(m));
                continue;
            }
//            Map.Entry<String,JsonNode> field = iter.next();
//            String pkgName = field.getKey();
//            JsonNode body = field.getValue();
//            String pkgVersion = body.get("version").asText("null");
//            Module m = new Module(pkgName, pkgVersion);
            //Path modulePath = Configuration.getConfiguration().getBasePath().resolve("node_modules/" + pkgName);
            //if(!modulePath.toFile().exists()){
                // Dependencies are not yet installed
                String registryUrl = buildNpmRegistryUrl(pkgName, pkgVersion);
                if(registryUrl == null || registryUrl.isBlank()) continue;
                Path modulePath = FileUtil.getFilePathFromURL(registryUrl, Configuration.getConfiguration().getCloneLocation().toString());
            //}
            Pair<String,String> detectionResult = licenseDetector.detect(modulePath.toString());
            String license = detectionResult.first;
            m.setLicense(license);
            m.setAnalyzedContent(detectionResult.second);
            resolveTransitiveDependencies(m,modulePath);
            scannedDependencies.add(m);
//            if(body.get("dependencies") != null) {
//                ArrayList<Module> deps_t = resolveTransitiveDependencies(body.get("dependencies"));
//                m.setDependencies(deps_t);
//            }
            root.addToDependencies(m);
        }
        
       
    }
    
    private void resolveTransitiveDependencies(Module root,JsonNode node) {
        Iterator<Map.Entry<String,JsonNode>> iter = node.fields();
        while(iter.hasNext()) {
            Map.Entry<String,JsonNode> field = iter.next();
            String pkgName = field.getKey();
            JsonNode body = field.getValue();
            String pkgVersion = body.get("version").asText("null");
            Module m = new Module(pkgName, pkgVersion);
            if(alreadyScanned(m)){
                Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.INFO, "Dependency "+pkgName+" already scanned");
                root.addToDependencies(getScannedModule(m));
                continue;
            }
            Path modulePath = Configuration.getConfiguration().getBasePath().resolve("node_modules/" + pkgName);
            if(!modulePath.toFile().exists()){
                String registryUrl = buildNpmRegistryUrl(pkgName, pkgVersion);
                if(registryUrl == null || registryUrl.isBlank()) continue;
                modulePath = FileUtil.getFilePathFromURL(registryUrl, Configuration.getConfiguration().getCloneLocation().toString());
            }
            Pair<String,String> detectionResult = licenseDetector.detect(modulePath.toString());
            String license = detectionResult.first;
            m.setLicense(license);
            m.setAnalyzedContent(detectionResult.second);
            JsonNode dependencies = body.get("dependencies");
            if(dependencies != null) {
                resolveTransitiveDependencies(m,dependencies);
            }else{
                System.out.println("No dependencies for module "+m.getName());
            }
            root.addToDependencies(m);
        }
    }
    
    private void resolveTransitiveDependencies(Module root,Path modulePath){
        File file = FileUtil.searchFile(modulePath.toFile(), "package.json");
        if(file==null){
            System.out.println("Couldn't find package.json");
            return;
        }
        Path path = file.toPath();
        if(path.toFile().exists()){
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.createObjectNode() ;
            try {
                node = mapper.readTree(path.toFile());    
                
                getRootModuleWithDependencies(root,node);
            } catch (IOException ex) {
                Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    @Override
     public Dependencies getModules() {
        return this.modules;
    }
    
    
    
    private Module extractModule(String d) {
        // last @
        return new Module(extractName(d),extractVersion(d));
    }
    
    private String extractName(String d) {
        d = d.trim();
        if(d.length() == 0)
            return null;
        String[] s = d.split("@");
        String name = s[s.length - 2];
        if(name.split(" ").length > 1){
            String[] a = name.split(" ");
            return a[a.length - 1];
        }
        return name;
    }
    private String extractVersion(String d) {
        String[] s = d.split("@");
        return s[s.length - 1];
    }
    
    private String getLicenseOfModule(Path path) {
        return licenseDetector.detect(path.toString()).first;
    }

    @Override
    public DependencyManager getPackageManager() {
        return pm;
    }
    
    
    public static String buildNpmRegistryUrl(String packageName,String version){
        String metadataUrl = "https://registry.npmjs.org/" + packageName + "/" + version;
        StringBuilder temp = new StringBuilder();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(metadataUrl).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            for (String line; (line = bufferedReader.readLine()) != null; ) {
                temp.append(line);
            }
            String tarballUrl = JSONHelper.getValue("/dist/tarball", temp.toString());
            return tarballUrl;
        } catch (MalformedURLException ex) {
            Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, "Failed to get tarball for "+ packageName, ex);
        }
        return null;
    }
   
    private boolean alreadyScanned(Module m){
        Iterator it = scannedDependencies.iterator();
        while(it.hasNext()){
            Module a = (Module) it.next();
            if(a.getName()==m.getName() && a.getVersion() == m.getVersion()){
                return true;
            }
        }
        return false;
    }
    
    private Module getScannedModule(Module m){
        Iterator it = scannedDependencies.iterator();
        while(it.hasNext()){
            Module a = (Module) it.next();
            if(a.getName()==m.getName() && a.getVersion() == m.getVersion()){
                return a;
            }
        }
        return m;
    }
}
