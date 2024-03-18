

package com.phsyberdome.plugin.npm;

import com.phsyberdome.common.utils.JSONHelper;
import com.phsyberdome.common.utils.models.Pair;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pratham Gahlout
 */

/*
    The class is deprecated in favor of an external library 'semver4j'. Why reinvent the wheel?
    Use NPMVersionHelperV2 to resolve NPM versions now.
*/

@Deprecated
class NPMVersion implements Comparable<NPMVersion>{
        public int major;
        public int minor;
        public int patch;

        public NPMVersion(int major, int minor, int patch) {
            this.major = major;
            this.minor = minor;
            this.patch = patch;
        }

        @Override
        public int compareTo(NPMVersion o) {
            if(major<o.major){
                return -1;
            }else if(major>o.major){
                return 1;
            }else{
                if(minor<o.minor){
                    return -1;
                }else if(minor>o.minor){
                    return 1;
                }else{
                    if(patch<o.patch){
                        return -1;
                    }else if(patch>o.patch){
                        return 1;
                    }else{
                        return 0;
                    }
                }
            }
        }

    @Override
    public String toString() {
        return major+"."+minor+"."+patch;
    }
        
}

@Deprecated
public class NPMVersionHelper {
    
    
     
    @Deprecated
     private static String findApproximatelyEquivalentVersion(String version,List<String> allVersions){
        // Get greatest patch version for current minor version
        if(!isMajorMinorPatchVersionFormat(version)){
            return null;
        }
        
        NPMVersion npmVersion = createNPMVersion(version);
        
        List<NPMVersion> currentMinorVersions = new ArrayList<>();
        for(String v:allVersions){
            NPMVersion _v = createNPMVersion(v);
            if(_v==null)continue;
            if(_v.minor == npmVersion.minor && _v.major == npmVersion.major){
                currentMinorVersions.add(_v);
            }
        }
        
        Collections.sort(currentMinorVersions);
        
        
        return currentMinorVersions.size()>0 ? currentMinorVersions.get(currentMinorVersions.size()-1).toString(): version;
    }
    
     @Deprecated
    private static String findVersionGreaterThan(String version, List<String> allVersions){
        sortVersions(allVersions, true);
        
        NPMVersion npmVersion = createNPMVersion(version);
        NPMVersion versionGreaterThan = npmVersion; 
        for(String v: allVersions){
            NPMVersion _v = createNPMVersion(v);
            if(_v==null)continue;
            if(versionGreaterThan.compareTo(_v) == -1){
                versionGreaterThan = _v;
            }
        }
        if(versionGreaterThan.equals(npmVersion)){
            //Equal can't be taken and list has no version greater than this so null it is
            return null;
        }
        return versionGreaterThan.toString();
    }
    
    @Deprecated
    private static String findVersionSmallerThan(String version, List<String> allVersions){
        sortVersions(allVersions, true);
        
        NPMVersion npmVersion = createNPMVersion(version);
        NPMVersion versionSmallerThan = npmVersion; 
        for(String v: allVersions){
            NPMVersion _v = createNPMVersion(v);
            if(_v==null)continue;
            if(versionSmallerThan.compareTo(_v) == 1){
                versionSmallerThan = _v;
            }
        }
        if(versionSmallerThan.equals(npmVersion)){
            //Equal can't be taken and list has no version greater than this so null it is
            return null;
        }
        return versionSmallerThan.toString();
    }
    @Deprecated
    private static String findVersionGreaterThanOrEqual(String version, List<String> allVersions){
        sortVersions(allVersions, true);
        
        NPMVersion npmVersion = createNPMVersion(version);
        NPMVersion versionGreaterThan = npmVersion; 
        for(String v: allVersions){
            NPMVersion _v = createNPMVersion(v);
            if(_v==null)continue;
            if(versionGreaterThan.compareTo(_v) == -1){
                versionGreaterThan = _v;
            }
        }
        
        return versionGreaterThan.toString();
    }
    
    @Deprecated
    private static String findVersionSmallerThanOrEqual(String version, List<String> allVersions){
        sortVersions(allVersions, true);
        
        NPMVersion npmVersion = createNPMVersion(version);
        NPMVersion versionSmallerThan = npmVersion; 
        for(String v: allVersions){
            NPMVersion _v = createNPMVersion(v);
            if(_v==null)continue;
            if(versionSmallerThan.compareTo(_v) == 1){
                versionSmallerThan = _v;
            }
        }
        
        return versionSmallerThan.toString();
    }
    
    @Deprecated
    public static String pinpointPackageVersion(String name,String version){
        if(version==null || version.length()<0){
            return null;
        }
        
        if(version.isBlank() || version.equals("*") || version.equals("latest")){
            return NPMVersionHelper.getLatestVersion(fetchAllVersions(name));
        }
        if(!isSupportedVersionsFormat(version)){
            System.out.println("Unsupported versions format! " + version);
            return version;
        }
        char first_char = version.charAt(0);
        List<String> allVersions = NPMVersionHelper.fetchAllVersions(name);
        System.out.println(allVersions);
        if(isMajorMinorPatchVersionFormat(version)){
            if(first_char == '~'){
                /*
                *   ~version “Approximately equivalent to version”, will update you to all future patch versions, 
                *   without incrementing the minor version. ~1.2.3 will use releases from 1.2.3 to <1.3.0.
                */
                String v = version;
                if(version.split("\\.").length == 3 || version.split("\\.").length == 2){
                    v = NPMVersionHelper.findApproximatelyEquivalentVersion(convertToMajorMinorPatchFormat(version.substring(1)), allVersions);
                }else if(version.split("\\.").length == 1){
                    v = NPMVersionHelper.findVersionCompatibleWith(convertToMajorMinorPatchFormat(version.substring(1)), allVersions);
                }
                return v;
            }else if(first_char == '^'){
                 /*
                 *  ^version “Compatible with version”, will update you to all future minor/patch versions, 
                 *  without incrementing the major version. ^1.2.3 will use releases from 1.2.3 to <2.0.0.
                 */
                String v = version;
                v = convertToMajorMinorPatchFormat(v.substring(1));
                NPMVersion ver = createNPMVersion(v);
                if(ver.major == 0 && ver.minor == 0){
                    // increment only patch
                    return NPMVersionHelper.findApproximatelyEquivalentVersion(v, allVersions);
                }else if(ver.major == 0){
                    return NPMVersionHelper.findApproximatelyEquivalentVersion(v, allVersions);
                }
                    return NPMVersionHelper.findVersionCompatibleWith(convertToMajorMinorPatchFormat(version.substring(1)), allVersions);
            }else if(first_char == '>' || first_char == '<'){
                if(version.charAt(1) == '='){
                    String v = first_char == '>'?
                            findVersionGreaterThanOrEqual(convertToMajorMinorPatchFormat(version.substring(2)), allVersions):
                            findVersionSmallerThanOrEqual(convertToMajorMinorPatchFormat(version.substring(2)), allVersions);
                    return v;
                }else{
                    String v = first_char == '>'?
                            findVersionGreaterThan(convertToMajorMinorPatchFormat(version.substring(1)), allVersions):
                            findVersionSmallerThan(convertToMajorMinorPatchFormat(version.substring(1)), allVersions);
                    return v;
                }
            }
        }else if(isDashedRange(version)){
            String[] bounds = version.split("-");
            if(bounds.length != 2){
                return version;
            }
            String lower_bound = convertToMajorMinorPatchFormat(bounds[0].trim());
            String upper_bound = convertToMajorMinorPatchFormat(bounds[1].trim());
            List<NPMVersion> versions = getVersionsWithinRange(lower_bound, upper_bound, allVersions);
            return versions.size()>0 ? versions.get(versions.size()-1).toString() : version;
        }else if(isRange(version)){
            String[] bounds = version.split(" ");
            if(bounds.length != 2){
                return version;
            }
            String lower_bound = convertToMajorMinorPatchFormat(bounds[0].trim());
            String upper_bound = convertToMajorMinorPatchFormat(bounds[1].trim());
            List<NPMVersion> versions = getVersionsWithinRange(lower_bound, upper_bound, allVersions);
            return versions.size()>0 ? versions.get(versions.size()-1).toString() : version;
        }else{
            System.out.println("Unsupported versions format! " + version);
        }
        
        
        return version;
    }
    
    public static String getLatestVersion(List<String> allVersions){
        Collections.sort(allVersions);
        return allVersions.get(allVersions.size()-1);
    }
    
    @Deprecated
    private static String findVersionCompatibleWith(String version,List<String> allVersions){
        // Get greatest minor version for current major version
        if(!isMajorMinorPatchVersionFormat(version)){
            return null;
        }
        
        NPMVersion npmVersion = createNPMVersion(version);
        
        List<NPMVersion> currentMajorVersions = new ArrayList<>();
        for(String v:allVersions){
            NPMVersion _v = createNPMVersion(v);
            if(_v==null)continue;
            if(_v.major == npmVersion.major){
                currentMajorVersions.add(_v);
            }
        }
        
        Collections.sort(currentMajorVersions);
        
        
        return currentMajorVersions.size()>0 ? currentMajorVersions.get(currentMajorVersions.size()-1).toString(): version;
    }
    
    @Deprecated
    private static List<NPMVersion> getVersionsWithinRange(String lowerBound,String upperBound,List<String> allVersions){
        // TODO: Handle conditional operators
        String firstOperator = "";
        String secondOperator = "";
        while(lowerBound.length() > 0 && !Character.isDigit(lowerBound.charAt(0))){
            firstOperator += lowerBound.charAt(0);
            lowerBound = lowerBound.substring(1);
        }
        while(upperBound.length() > 0 && !Character.isDigit(upperBound.charAt(0))){
            secondOperator += upperBound.charAt(0);
            upperBound = upperBound.substring(1);
        }
        NPMVersion lowerBoundVersion =  createNPMVersion(lowerBound);
        NPMVersion upperBoundVersion = createNPMVersion(upperBound);
        
        List<NPMVersion> versions = new ArrayList<>();
        for(String v:allVersions){
            NPMVersion _v = createNPMVersion(v);
            if(_v == null) continue;
            if(firstOperator.isBlank() && secondOperator.isBlank()){
                if(lowerBoundVersion.compareTo(_v)<=0 && upperBoundVersion.compareTo(_v)>=0){
                    versions.add(_v);
                }
            }else{
                
                if(firstOperator.charAt(0) == '<'){
                    if(firstOperator.length()>1 && firstOperator.charAt(1) == '='){
                        if(lowerBoundVersion.compareTo(_v) < 0 )continue;
                    }else{
                        if(lowerBoundVersion.compareTo(_v) <= 0)continue;
                    }
                }else if(firstOperator.charAt(0) == '>'){
                    if(firstOperator.length()>1 && firstOperator.charAt(1) == '='){
                        if(lowerBoundVersion.compareTo(_v) > 0 )continue;
                    }else{
                        if(lowerBoundVersion.compareTo(_v) >= 0)continue;
                    }
                }
                if(secondOperator.charAt(0) == '<'){
                    if(secondOperator.length()>1 && secondOperator.charAt(1) == '='){
                        if(upperBoundVersion.compareTo(_v) < 0 )continue;
                    }else{
                        if(upperBoundVersion.compareTo(_v) <= 0)continue;
                    }
                }else if(secondOperator.charAt(0) == '>'){
                    if(secondOperator.length()>1 && secondOperator.charAt(1) == '='){
                        if(upperBoundVersion.compareTo(_v) > 0 )continue;
                    }else{
                        if(upperBoundVersion.compareTo(_v) >= 0)continue;
                    }
                }
                versions.add(_v);
            }
        }
        Collections.sort(versions);
        return versions;
    }
    
    public static List<String> fetchAllVersions(String name){
        
        String repoUrl = "https://registry.npmjs.org/"+name;
        StringBuilder temp = new StringBuilder();
        try {
            HttpURLConnection conn = (HttpURLConnection) new URL(repoUrl).openConnection();
            conn.setRequestMethod("GET");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            for (String line; (line = bufferedReader.readLine()) != null; ) {
                temp.append(line);
            }
            List<Pair<String,String>> versionObjs =  JSONHelper.getValues("/versions", temp.toString());
            return versionObjs.stream().map(pair -> pair.first).toList();
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(NPMVersionHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NPMVersionHelper.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    @Deprecated
    public static void sortVersions(List<String> allVersions, boolean toAsc){
        Collections.sort(allVersions,new Comparator<String>(){
            @Override
            public int compare(String o1, String o2) {
                try {
                    int o1major = Integer.parseInt(o1.split("\\.")[0]);
                    int o1minor = Integer.parseInt(o1.split("\\.")[1]);
                    int o1patch = Integer.parseInt(o1.split("\\.")[2]);
                    
                    int o2major = Integer.parseInt(o2.split("\\.")[0]);
                    int o2minor = Integer.parseInt(o2.split("\\.")[1]);
                    int o2patch = Integer.parseInt(o2.split("\\.")[2]);
                    
                    NPMVersion o1Version = new NPMVersion(o1major,o1minor,o1patch);
                    NPMVersion o2Version = new NPMVersion(o2major,o2minor,o2patch);
                    
                    return o1Version.compareTo(o2Version);
                }catch(NumberFormatException e){
                    System.out.println("Failed to sort versions, at least one of them is not in major.minor.patch format!");
                    return 0;
                }
            }
            
        });
        
        if(!toAsc){
           Collections.reverse(allVersions);
        }
    }
    
    
    @Deprecated
    public static boolean isMajorMinorPatchVersionFormat(String version){
        while(version.length() > 0 && !Character.isDigit(version.charAt(0))){
            version = version.substring(1);
        }
        
        version = convertToMajorMinorPatchFormat(version);
        if(version.split("\\.").length != 3){
            return false;
        }
        
        try {
            Integer.parseInt(version.split("\\.")[0]);
            Integer.parseInt(version.split("\\.")[1]);
            Integer.parseInt(version.split("\\.")[2]);
        }catch(NumberFormatException e){
            return false;
        }
        return true;
    }
    
    @Deprecated
    public static boolean isDashedRange(String version){
        if(!version.contains("-")){
            return false;
        }
        if(version.split("-").length !=2){
            return false;
        }
        if(!isMajorMinorPatchVersionFormat(version.split("-")[0].trim()) ||
           !isMajorMinorPatchVersionFormat(version.split("-")[1].trim())
        ){
            return false;
        }
        return true;
    }
    
    @Deprecated
    public static boolean isRange(String version){
        if(!version.contains(" ") || version.split(" ").length != 2){
            return false;
        }
        if(!isMajorMinorPatchVersionFormat(version.split(" ")[0].trim()) ||
           !isMajorMinorPatchVersionFormat(version.split(" ")[1].trim())
                )
        {
            return false;
        }
        return true;
    }
    
    @Deprecated
    private static boolean isSupportedVersionsFormat(String version){
        
        return isMajorMinorPatchVersionFormat(version)
                || isDashedRange(version)
                || isRange(version);
    }
    
    @Deprecated
    private static NPMVersion createNPMVersion(String v){
        if(!isMajorMinorPatchVersionFormat(v)){
            return null;
        }
        try {
            int major = Integer.parseInt(v.split("\\.")[0]);
            int minor = Integer.parseInt(v.split("\\.")[1]);
            int patch = Integer.parseInt(v.split("\\.")[2]);
            return new NPMVersion(major,minor,patch);
        }catch(NumberFormatException e){
            return null;
        }
    }
    
    @Deprecated
    private static String convertToMajorMinorPatchFormat(String v){
        if(v.split("\\.").length == 3){
            return v;
        }else if(v.split("\\.").length == 2){
            return v + ".0";
        }else if(v.split("\\.").length == 1){
            return v + ".0.0";
        }
        return v;
    }
    
}
