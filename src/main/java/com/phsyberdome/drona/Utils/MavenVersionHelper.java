

package com.phsyberdome.drona.Utils;

import com.phsyberdome.drona.CLIHelper;
import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fusesource.jansi.Ansi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 *
 * @author Pratham Gahlout
 */
public class MavenVersionHelper {
    
    public static String softVersionRegex = "(?<eq>[\\d.\\D]+)";
    public static String hardVersionRegex = "(?:^\\[(?<heq>[\\d.\\D]+)\\]$)";
    public static String conditionalVersionRegex = "(?:(?<or>(?<=\\]|\\)),(?=\\[|\\())|,|(?:(?<=,)(?:(?<lte>[\\d.\\D]+)\\]|(?<lt>[\\d.]+)\\)))|(?:(?:\\[(?<gte>[\\d.]+)|\\((?<gt>[\\d.]+))(?=,)))+";


    public static List<String> getAllVersions(String groupId,String artifactId) {
        Document document = MavenRepoHelper.getMavenMetadataDocument(groupId, artifactId);
        NodeList list = document.getElementsByTagName("version");
        List<String> versions = new ArrayList<String>();
        for(int i=0;i<list.getLength();i++) {
            Element el = (Element) list.item(i);
            versions.add(el.getTextContent());
        }
        return versions;
    }
    
    public static String resolveVersion(String groupId,String artifactId,String version) {
        ArrayList<String> allVersions = (ArrayList<String>) getAllVersions(groupId, artifactId);
        
        try {
            Pattern pattern = Pattern.compile(softVersionRegex+"|"+hardVersionRegex+"|"+conditionalVersionRegex,Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(version);
            
            if(matcher.find()){
                if(matcher.group("eq") != null){
                    return matcher.group("eq");
                }else if(matcher.group("heq")!=null){
                    return matcher.group("heq");
                }else if(matcher.group("lte")!=null){
                    if(matcher.group("gte") !=null) {
                    }else {
                        allVersions.removeIf(s -> {
                            try {
                                Semver version1 = new Semver(s,Semver.SemverType.IVY);
                                Semver version2 = new Semver(matcher.group("lte"), Semver.SemverType.IVY);
                                return version1.isGreaterThan(version2);
                            }catch(SemverException e){
                                System.out.println("not following semantic version "+s);
                                return false;
                            }
                        });
                        return allVersions.get(allVersions.size()-1);
                    }
                }else if(matcher.group("gte")!=null){

                }
            
                else if(matcher.group("gte")!=null && matcher.group("lt")!=null){
                    
                }else if(matcher.group("gt")!=null && matcher.group("lt")!=null){
                    
                }
            }else {
                CLIHelper.updateCurrentLine("Regex failed to match", Ansi.Color.RED);
            }
        }catch(IllegalStateException e){
            return version;
        }
        return version;
    }
    
}
