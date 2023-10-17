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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
    
    private Dependencies modules;
    private LicenseDetector licenseDetector;

    public NodePackageManagerPlugin(LicenseDetector licenseDetector) {
        this.licenseDetector = licenseDetector;
    }
    
    

   
    @Override
    public void readModules() {
        readModulesV2();
    }
    
    public void readModulesV2() {
        modules = new Dependencies();
        String pm_version = SysRunner.run(CMD_VERSION);
        pm = new DependencyManager("npm",pm_version);
        // Read the package-lock.json
        File file = FileUtil.searchFile(FileSystems.getDefault().getPath(".").toFile(), "package.json");
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
                getRootModuleWithDependencies(root,node);
            } catch (IOException ex) {
                Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            licenseDetector.printScanStats();
        }else{
            System.out.println("Project does not has npm as its package manager!");
        }
        
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
            String pkgVersion = pinpointPackageVersion(pkgName, _pkgVersion);
            Module m = new Module(pkgName, pkgVersion);
//            Map.Entry<String,JsonNode> field = iter.next();
//            String pkgName = field.getKey();
//            JsonNode body = field.getValue();
//            String pkgVersion = body.get("version").asText("null");
//            Module m = new Module(pkgName, pkgVersion);
            //Path modulePath = Configuration.getConfiguration().getBasePath().resolve("node_modules/" + pkgName);
            //if(!modulePath.toFile().exists()){
                // Dependencies are not yet installed
                String registryUrl = buildNpmRegistryUrl(pkgName, pkgVersion);
                if(registryUrl == null) continue;
                Path modulePath = FileUtil.getFilePathFromURL(registryUrl, Configuration.getConfiguration().getCloneLocation().toString());
            //}
            Pair<String,String> detectionResult = licenseDetector.detect(modulePath.toString());
            String license = detectionResult.first;
            m.setLicense(license);
            m.setAnalyzedContent(detectionResult.second);
            resolveTransitiveDependencies(m,modulePath);
//            if(body.get("dependencies") != null) {
//                ArrayList<Module> deps_t = resolveTransitiveDependencies(body.get("dependencies"));
//                m.setDependencies(deps_t);
//            }
            root.addToDependencies(m);
        }
        
        modules.addToDependencies(root);
    }
    
//    private ArrayList<Module> resolveTransitiveDependencies(JsonNode node) {
//        ArrayList<Module> deps = new ArrayList<>();
//        Iterator<Map.Entry<String,JsonNode>> iter = node.fields();
//        while(iter.hasNext()) {
//            Map.Entry<String,JsonNode> field = iter.next();
//            String pkgName = field.getKey();
//            JsonNode body = field.getValue();
//            String pkgVersion = body.get("version").asText("null");
//            Module m = new Module(pkgName, pkgVersion);
//            //Path modulePath = Configuration.getConfiguration().getBasePath().resolve("node_modules/" + pkgName);
//            //if(!modulePath.toFile().exists()){
//                // Dependencies are not yet installed
//                String registryUrl = buildNpmRegistryUrl(pkgName, pkgVersion);
//                if(registryUrl == null) continue;
//                Path modulePath = FileUtil.getFilePathFromURL(registryUrl, Configuration.getConfiguration().getCloneLocation());
//            //}
//            String license = getLicenseOfModule(modulePath);
//            m.setLicense(license);
//            if(body.get("dependencies") != null) {
//                ArrayList<Module> deps_t = resolveTransitiveDependencies(body.get("dependencies"));
//                m.setDependencies(deps_t);
//            }
//            deps.add(m);
//        }
//        return deps;
//    }
    
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
            Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String pinpointPackageVersion(String name,String version){
        if(version==null || version.length()<0){
            return null;
        }
        char first_char = version.charAt(0);
        List<String> allVersions = fetchAllVersions(name);
        
        // TODO: Support all methods of version specificity
        if(first_char == '~'){
            /*
            *   ~version “Approximately equivalent to version”, will update you to all future patch versions, 
            *   without incrementing the minor version. ~1.2.3 will use releases from 1.2.3 to <1.3.0.
            */
            String v = findApproximatelyEquivalentVersion(version.substring(1), allVersions);
            System.out.println("patch version for "+name+" is "+v);
            return v;
        }else if(first_char == '^'){
             /*
             *  ^version “Compatible with version”, will update you to all future minor/patch versions, 
             *  without incrementing the major version. ^1.2.3 will use releases from 1.2.3 to <2.0.0.
             */
            return findVersionCompatibleWith(version.substring(1), allVersions);
        }else{
            return version.substring(1);
        }
    }
    
    private List<String> fetchAllVersions(String name){
        
        String repoUrl = "https://registry.npmjs.org/"+name;
        StringBuilder temp = new StringBuilder();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(repoUrl).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            for (String line; (line = bufferedReader.readLine()) != null; ) {
                temp.append(line);
            }
            return JSONHelper.getValues("/versions", temp.toString());
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    private String findApproximatelyEquivalentVersion(String version,List<String> allVersions){
        // Get greatest patch version for current minor version
        String currentMinor = version.substring(0,version.lastIndexOf('.'));
        List<String> currentMinorVersions = new ArrayList<>();
        for(String v:allVersions){
            String thisMinor = v.substring(0,v.lastIndexOf('.'));
            if(thisMinor.equals(currentMinor) && v.substring(v.lastIndexOf('.')+1).matches("\\d+(\\.\\d+)?")){
                currentMinorVersions.add(v);
            }
        }
        
        Collections.sort(currentMinorVersions, new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                String patch1 = o1.substring(o1.lastIndexOf('.')+1);
                String patch2 = o2.substring(o2.lastIndexOf('.')+1);
                
                Integer p1 = Integer.parseInt(patch1);
                Integer p2 = Integer.parseInt(patch2);
                
                if(p1==p2){
                    return 0;
                }else if(p1<p2){
                    return -1;
                }else{
                    return 1;
                }
            }
            
        });
        
        return currentMinorVersions.size()>0 ? currentMinorVersions.get(currentMinorVersions.size()-1) : version;
    }
    
     private String findVersionCompatibleWith(String version,List<String> allVersions){
        // Get greatest minor version for current major version
        String currentMajor = version.substring(0,version.indexOf('.'));
        List<String> currentMajorVersions = new ArrayList<>();
        for(String v:allVersions){
            String thisMinor = v.substring(0,version.indexOf('.'));
            if(thisMinor.equals(currentMajor) && v.substring(v.indexOf('.')+1).matches("\\d+(\\.\\d+)?")){
                currentMajorVersions.add(v);
            }
        }
        
        Collections.sort(currentMajorVersions, new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                String _minor1 = o1.substring(o1.indexOf('.')+1);
                String _minor2 = o2.substring(o2.indexOf('.')+1);
                String minor1 = _minor1.substring(0,_minor1.indexOf('.'));
                String minor2 = _minor2.substring(0,_minor2.indexOf('.'));
                
                Integer p1 = Integer.parseInt(minor1);
                Integer p2 = Integer.parseInt(minor2);
                
                if(p1==p2){
                    return 0;
                }else if(p1<p2){
                    return -1;
                }else{
                    return 1;
                }
            }
            
        });
        
        
        return currentMajorVersions.size()>0 ? findApproximatelyEquivalentVersion(currentMajorVersions.get(0),allVersions) : version;
    }
}
