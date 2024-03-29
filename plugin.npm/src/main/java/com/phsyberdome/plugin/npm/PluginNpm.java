package com.phsyberdome.plugin.npm;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.phsyberdome.common.utils.CLIHelper;
import com.phsyberdome.common.interfaces.PluginInterface;
import com.phsyberdome.common.utils.models.DependencyManager;
import com.phsyberdome.common.utils.models.Module;
import com.phsyberdome.common.utils.models.Pair;
import com.phsyberdome.common.utils.FileUtil;
import com.phsyberdome.common.utils.JSONHelper;
import com.phsyberdome.common.interfaces.LicenseDetectorInterface;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.fusesource.jansi.Ansi;

/**
 *
 * @author Pratham Gahlout
 */
public class PluginNpm implements PluginInterface{
    
//    // TODO: MOVE ALL THIS TO A CONFIG FILE
//    final private String CMD_VERSION = "cmd /c npm -v";
//    final private String CMD_LIST = "cmd /c npm list -json";
    
    private DependencyManager pm;
    
    private Path cloneLocation;
    private Path rootPath;
    
    private Set<Module> scannedDependencies;
    
    private ArrayList<Module> modules;
    private LicenseDetectorInterface licenseDetector;
    
    public PluginNpm() {
        this.scannedDependencies = new HashSet<>();
    }

    public PluginNpm(LicenseDetectorInterface licenseDetector) {
        this.licenseDetector = licenseDetector;
        scannedDependencies = new HashSet<>();
    }
    
    @Override
    public void setLicenseDetector(LicenseDetectorInterface detector) {
        this.licenseDetector = detector;
    }
    
    @Override
    public void setRootPath(Path path) {
        this.rootPath = path;
    }
    
    @Override
    public void setCloneLocation(Path cloneLocation) {
        this.cloneLocation = cloneLocation;
    }
    

   
    @Override
    public ArrayList<Module> readModules() {
        return readModulesV2();
    }
    
    public ArrayList<Module> readModulesV2() {
        modules = new ArrayList<>();
//        String pm_version = SysRunner.run(CMD_VERSION);
        pm = new DependencyManager("npm","unknown");
        
        
        
        File pkgLockFile = FileUtil.searchFile(this.rootPath.toFile(), "package-lock.json");
        
        /**
         * Forcing to resolve dependencies using `package.json` file until support for lockFile version 2 & 3 are
         * added.
         */
        pkgLockFile = null;
        
        
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
                Logger.getLogger(PluginNpm.class.getName()).log(Level.SEVERE, null, e);
            }
            licenseDetector.printScanStats();
        }else {
            // Read the package.json file and resolve the versions
            File file = FileUtil.searchFile(this.rootPath.toFile(), "package.json");
            if(file==null){
                CLIHelper.updateCurrentLine("Couldn't find package.json",Ansi.Color.RED);
                return modules;
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
                    modules.add(root);
                } catch (IOException ex) {
                    Logger.getLogger(PluginNpm.class.getName()).log(Level.SEVERE, null, ex);
                    return modules;
                }
                licenseDetector.printScanStats();
            }else{
                CLIHelper.updateCurrentLine("Project does not has npm as its package manager!",Ansi.Color.RED);
            }
        }
        return modules;
    }
    
    private void getRootModuleWithDependenciesFromLockFile(Module root,JsonNode node){
        /*
        Assuming lockfileVerions 1
        */
        JsonNode deps = node.get("dependencies");
        if(deps==null){
            /*
            Could this be lockFileVersion 2?
            */
            getRootModuleWithDependenciesFromLockFileV2(root,node);
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
                Logger.getLogger(PluginNpm.class.getName()).log(Level.INFO, "Dependency "+pkgName+" already scanned");
                root.addToDependencies(getScannedModule(m));
                continue;
            }
            Path modulePath = this.rootPath.resolve("node_modules/" + pkgName);
            if(!modulePath.toFile().exists()){
                String registryUrl = buildNpmRegistryUrl(pkgName, pkgVersion);
                if(registryUrl == null || registryUrl.isBlank()) continue;
                modulePath = FileUtil.getFilePathFromURL(registryUrl, this.cloneLocation.toString());
            }
            
            var detectionResult = licenseDetector.detect(modulePath.toString());
            m.setLicense(detectionResult.getResultWithMostProbableLicenses().getLicensesAsString());
            m.setAnalyzedContent(detectionResult.getAnalyzedContent());
            JsonNode dependencies = body.get("dependencies");
            if(dependencies!=null){
                resolveTransitiveDependencies(m,dependencies);
            }else{
                //No dependency, good!
            }
            scannedDependencies.add(m);
            root.addToDependencies(m);
        }
        modules.add(root);
    }
    
    
    private void getRootModuleWithDependenciesFromLockFileV2(Module root, JsonNode node ){
        JsonNode pkgs = node.get("packages");
        
        if(pkgs == null) {
            // This won't be the case as there would be atleast the root project entry
            return;
        }
        Map.Entry<String,JsonNode> project = pkgs.fields().next();
        getRootModuleWithDependencies(root, project.getValue());
    }
    private void getRootModuleWithDependencies(Module root,JsonNode node) {
        
        
        JsonNode deps = node.get("dependencies");
        if(deps==null){
            return;
        }
        Iterator<Map.Entry<String,JsonNode>> iter = deps.fields();
        
        while(iter.hasNext()) {
            Map.Entry<String,JsonNode> field = iter.next();
            String pkgName = field.getKey();
            String _pkgVersion = field.getValue().asText();
            CLIHelper.updateCurrentLine(pkgName + " version before resolution "+_pkgVersion,Ansi.Color.CYAN);
            String pkgVersion = NPMVersionHelperV2.pinpointPackageVersion(pkgName, _pkgVersion);
            CLIHelper.updateCurrentLine(pkgName + " version after resolution "+pkgVersion,Ansi.Color.CYAN);
            Module m = new Module(pkgName, pkgVersion);
            if(alreadyScanned(m)){
                CLIHelper.updateCurrentLine("Dependency "+pkgName+" already scanned",Ansi.Color.GREEN);
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
                Path modulePath = FileUtil.getFilePathFromURL(registryUrl, this.cloneLocation.toString());
            //}
            var detectionResult = licenseDetector.detect(modulePath.toString());
            m.setLicense(detectionResult.getResultWithMostProbableLicenses().getLicensesAsString());
            m.setAnalyzedContent(detectionResult.getAnalyzedContent());
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
                CLIHelper.updateCurrentLine("Dependency "+pkgName+" already scanned",Ansi.Color.GREEN);
                root.addToDependencies(getScannedModule(m));
                continue;
            }
            Path modulePath = this.rootPath.resolve("node_modules/" + pkgName);
            if(!modulePath.toFile().exists()){
                String registryUrl = buildNpmRegistryUrl(pkgName, pkgVersion);
                if(registryUrl == null || registryUrl.isBlank()) continue;
                modulePath = FileUtil.getFilePathFromURL(registryUrl, this.cloneLocation.toString());
            }
            var detectionResult = licenseDetector.detect(modulePath.toString());
            m.setLicense(detectionResult.getResultWithMostProbableLicenses().getLicensesAsString());
            m.setAnalyzedContent(detectionResult.getAnalyzedContent());
            JsonNode dependencies = body.get("dependencies");
            if(dependencies != null) {
                resolveTransitiveDependencies(m,dependencies);
            }else{
                CLIHelper.updateCurrentLine("No dependencies for module "+m.getName(),Ansi.Color.RED);
            }
            root.addToDependencies(m);
        }
    }
    
    private void resolveTransitiveDependencies(Module root,Path modulePath){
        File file = FileUtil.searchFile(modulePath.toFile(), "package.json");
        if(file==null){
            CLIHelper.updateCurrentLine("Couldn't find package.json",Ansi.Color.RED);
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
                Logger.getLogger(PluginNpm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    @Override
     public ArrayList<Module> getModules() {
        return this.modules;
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
            Logger.getLogger(PluginNpm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(PluginNpm.class.getName()).log(Level.SEVERE, "Failed to get tarball for "+ packageName, ex);
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
