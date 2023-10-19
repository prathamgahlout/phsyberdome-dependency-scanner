

package com.gahloutsec.drona.Utils;

import static com.gahloutsec.drona.Utils.NPMVersionHelper.fetchAllVersions;
import com.vdurmont.semver4j.Semver;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
            Semver semver = new Semver(ver,Semver.SemverType.NPM);
            if(semver.satisfies(version)){
                candidates.add(semver);
            }
        }
        
        sortCandidates(candidates);
        return candidates.size()>0 ? candidates.get(candidates.size()-1).getValue() : version;
    }

    private static void sortCandidates(List<Semver> candidates){
        if(candidates.isEmpty()){
            return;
        }
        Collections.sort(candidates);
        
    }
}
