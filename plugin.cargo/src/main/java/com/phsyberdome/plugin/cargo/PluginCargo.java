
package com.phsyberdome.plugin.cargo;

import com.moandjiezana.toml.Toml;
import com.phsyberdome.common.interfaces.LicenseDetectorInterface;
import com.phsyberdome.common.interfaces.PluginInterface;
import com.phsyberdome.common.utils.CLIHelper;
import com.phsyberdome.common.utils.FileUtil;
import com.phsyberdome.common.utils.models.DependencyManager;
import com.phsyberdome.common.utils.models.Module;
import com.phsyberdome.common.utils.models.Pair;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.fusesource.jansi.Ansi;

/**
 *
 * @author Pratham Gahlout
 */
public class PluginCargo implements PluginInterface{
    
    private LicenseDetectorInterface licenseDetector;
    private Path rootPath;
    private Path cloneLocation;
    private DependencyManager packageManager;
    
    private ArrayList<Module> modules;
    
    private List<Module> scannedDependencies;
    
    private String cargoTomlFileNameRegex = "(?i)cargo\\.toml";
    
    
    public PluginCargo() {
        this.scannedDependencies = new ArrayList<>();
    }
    
    public PluginCargo(LicenseDetectorInterface licenseDetector) {
        this.licenseDetector = licenseDetector;
        this.scannedDependencies = new ArrayList<>();
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
    public DependencyManager getPackageManager() {
        return this.packageManager;
    }

    @Override
    public ArrayList<Module> readModules() {
        modules = new ArrayList<>();
        this.packageManager = new DependencyManager("Cargo","null");
        
        /*
        Read Cargo.toml file
        */
        File tomlFile = FileUtil.searchFile(this.rootPath.toFile(), cargoTomlFileNameRegex);
        
        if(tomlFile == null) {
            // I guess this is not a rust project
            CLIHelper.printLine("Couldn't find any cargo.toml", Ansi.Color.YELLOW);
            return modules;
        }
                
        Toml toml = new Toml().read(tomlFile);
        
        String moduleTitle = toml.getString("title", "N/A");
        String version = toml.getString("version","null");
        
        Toml dependencies = toml.getTable("dependencies");
        if(dependencies==null)
            dependencies = toml.getTable("workspace.dependencies");
        
        Module root = new Module(moduleTitle, version);
        if(dependencies != null) {
            dependencies.entrySet()
                    .stream()
                    .forEach(
                       dep -> {
                           String name = dep.getKey();
                           String ver = dep.getValue().toString();

                           // Resolve version before proceeding
                           String resolvedVersion = CargoVersionHelper.resolveVersion(ver);
                           Module module = new Module(name,resolvedVersion);
                           resolveDependencyTree(module);
                           root.addToDependencies(module);
                       }
                    );
        }
        modules.add(root);
        
        return modules;
    }
    
    private void resolveDependencyTree(Module module) {
        String license = RegistryHelper.getLicenseFromRegistry(module.getName(), module.getVersion());
        String analyzedContent = "{Registry-Metadata}";
        if(license.isEmpty()) {
            // Try to see if there is an open source url? If yes then analyze that
            String repoUrl = RegistryHelper.getSourceCodeRepoLink(module.getName());
            if(!repoUrl.isEmpty()) {
                Path path = FileUtil.getFilePathFromURL(repoUrl,this.cloneLocation.toString());
                if(path!=null){
                    var detectionResults = licenseDetector.detect(path.toString());
                    license = detectionResults.getResultWithMostProbableLicenses().getLicensesAsString();
                    analyzedContent = detectionResults.getAnalyzedContent();
                }
            }
        }
        module.setLicense(license);
        module.setAnalyzedContent(analyzedContent);
        // Get dependencies declared in the registry against this version
        List<Pair<String,String>> depData = RegistryHelper.getDependenciesOfCrate(module.getName(),module.getVersion());
        for(var dep: depData) {
            String resolvedVersion = CargoVersionHelper.resolveVersion(dep.second);
            Module m = new Module(dep.first,resolvedVersion);
            // Recurse
            resolveDependencyTree(m);
            module.addToDependencies(m);
        }
        
    }

    @Override
    public ArrayList<Module> getModules() {
        return modules != null ? modules : new ArrayList<>();
    }
    
    
    
    
}
