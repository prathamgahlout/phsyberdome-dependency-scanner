

package com.phsyberdome.plugin.npm;

import static com.phsyberdome.plugin.npm.NPMVersionHelper.fetchAllVersions;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Pratham Gahlout
 */
public class NPMVersionHelperV2 {
    
    
    public static String pinpointPackageVersion(String name,String version){
        if(version==null || version.length()<0){
            return null;
        }
        
        if(version.isBlank() || version.equals("*") || version.equals("latest")){
            return NPMVersionHelper.getLatestVersion(fetchAllVersions(name));
        }
        
        List<String> allVersions = fetchAllVersions(name);
        List<Semver> candidates = new ArrayList<>();
        for(String ver:allVersions){
            try {
                Semver semver = new Semver(ver,Semver.SemverType.NPM);
                if(semver.satisfies(version)){
                    candidates.add(semver);
                }
            } catch(SemverException ex) {
            }
        }
        
        sortCandidates(candidates);
        return !candidates.isEmpty() ? candidates.get(candidates.size()-1).getValue() : version;
    }

    private static void sortCandidates(List<Semver> candidates){
        if(candidates.isEmpty()){
            return;
        }
        Collections.sort(candidates);
        
    }
}
