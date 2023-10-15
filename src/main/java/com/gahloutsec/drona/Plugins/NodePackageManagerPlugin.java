package com.gahloutsec.drona.Plugins;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gahloutsec.drona.Configuration;
import com.gahloutsec.drona.PluginInterface;
import com.gahloutsec.drona.Models.Dependencies;
import com.gahloutsec.drona.Models.Module;
import com.gahloutsec.drona.Models.DependencyManager;
import com.gahloutsec.drona.SysRunner;
import com.gahloutsec.drona.licensedetector.LicenseDetector;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        String version = SysRunner.run(CMD_VERSION);
        pm = new DependencyManager("npm",version);
        // Read the package-lock.json
        Path path = Configuration.getConfiguration().getBasePath().resolve("package-lock.json");
        if(path.toFile().exists()){
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.createObjectNode() ;
            try {
                node = mapper.readTree(path.toFile());        
                getRootModuleWithDependencies(node);
            } catch (IOException ex) {
                Logger.getLogger(NodePackageManagerPlugin.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }
            licenseDetector.printScanStats();
        }else{
            System.out.println("Project does not has npm as its package manager!");
        }
        
    }
    
    private void getRootModuleWithDependencies(JsonNode node) {
        String name = node.get("name").asText("null");
        String version = node.get("version").asText("null");
        Module root = new Module(name,version);
        
        JsonNode deps = node.get("dependencies");
        
        Iterator<Map.Entry<String,JsonNode>> iter = deps.fields();
        
        while(iter.hasNext()) {
            Map.Entry<String,JsonNode> field = iter.next();
            String pkgName = field.getKey();
            JsonNode body = field.getValue();
            String pkgVersion = body.get("version").asText("null");
            Module m = new Module(pkgName, pkgVersion);
            Path modulePath = Configuration.getConfiguration().getBasePath().resolve("node_modules/" + pkgName);
            String license = getLicenseOfModule(modulePath);
            m.setLicense(license);
            if(body.get("dependencies") != null) {
                ArrayList<Module> deps_t = resolveTransitiveDependencies(body.get("dependencies"));
                m.setDependencies(deps_t);
            }
            root.addToDependencies(m);
        }
        
        modules.addToDependencies(root);
    }
    
    private ArrayList<Module> resolveTransitiveDependencies(JsonNode node) {
        ArrayList<Module> deps = new ArrayList<>();
        Iterator<Map.Entry<String,JsonNode>> iter = node.fields();
        while(iter.hasNext()) {
            Map.Entry<String,JsonNode> field = iter.next();
            String pkgName = field.getKey();
            JsonNode body = field.getValue();
            String pkgVersion = body.get("version").asText("null");
            Module m = new Module(pkgName, pkgVersion);
            Path modulePath = Configuration.getConfiguration().getBasePath().resolve("node_modules/" + pkgName);
            String license = getLicenseOfModule(modulePath);
            m.setLicense(license);
            if(body.get("dependencies") != null) {
                ArrayList<Module> deps_t = resolveTransitiveDependencies(body.get("dependencies"));
                m.setDependencies(deps_t);
            }
            deps.add(m);
        }
        return deps;
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
        return licenseDetector.detect(path.toString());
    }

    @Override
    public DependencyManager getPackageManager() {
        return pm;
    }
}
