
package com.phsyberdome.plugin.cargo;

import com.phsyberdome.common.utils.JSONHelper;
import com.phsyberdome.common.utils.NetworkHelper;
import com.phsyberdome.common.utils.models.Pair;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pratham Gahlout
 */
public class RegistryHelper {
    
    public static final String CRATE_API_BASE = "https://crates.io/api/v1/crates/";
    
    
    
    public static String getBaseCrateUrl(String pkgName) {
        return CRATE_API_BASE + pkgName;
    }
    
    public static String getCrateUrl(String pkgName, String version) {
        
        return getBaseCrateUrl(pkgName) + "/" + version;
    }
    
    public static List<String> getAllVersionsOfCrate(String pkgName) {
        String crateUrl = getBaseCrateUrl(pkgName);
        String metadata = NetworkHelper.getData(crateUrl);
        List<String> versions = JSONHelper.getArray("/versions", metadata);
        
        
        return versions == null ? new ArrayList<>() : versions
                .stream()
                .map(
                        version -> JSONHelper.getValue("/num", version)
                ).toList();
                
    }
    
    // This will always be prioritized unless it returns empty handed
    public static String getLicenseFromRegistry(String pkgName,String version) {
        String crateUrl = getCrateUrl(pkgName,version);
        String metadata = NetworkHelper.getData(crateUrl);
        
        
        String license = JSONHelper.getValue("/version/license",metadata);
        return license != null ? license : "";
    }
    
    public static String getSourceCodeRepoLink(String pkgName) {
        String crateUrl = getBaseCrateUrl(pkgName);
        String data = NetworkHelper.getData(crateUrl);
        String repoUrl = JSONHelper.getValue("/crate/repository", data);
        
        return repoUrl != null ? repoUrl : "";
    }
    
    // Returns dependency name and version requirement spec
    public static List<Pair<String,String>> getDependenciesOfCrate(String pkgName, String version) {
        String crateDependencyUrl = getCrateUrl(pkgName,version) + "/dependencies";
        String data = NetworkHelper.getData(crateDependencyUrl);
        List<String> dependencies = JSONHelper.getArray("/dependencies",data);
        
        return dependencies == null ? new ArrayList<>() :
                dependencies.stream()
                        .map(
                                dep -> {
                                    String name = JSONHelper.getValue("/crate_id",dep);
                                    String req = JSONHelper.getValue("/req", dep);
                                    return new Pair<String,String>(name,req);
                                }
                        ).toList();
    }
}
