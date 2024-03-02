

package com.phsyberdome.common.utils;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.fusesource.jansi.Ansi;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author Pratham Gahlout
 */
public class MavenVersionHelper {
    
//    public static String softVersionRegex = "(?<eq>[\\d.\\D]+)";
//    public static String hardVersionRegex = "(?:^\\[(?<heq>[\\d.\\D]+)\\]$)";
//    public static String conditionalVersionRegex = "(?:(?<or>(?<=\\]|\\)),(?=\\[|\\())|,|(?:(?<=,)(?:(?<lte>[\\d.\\D]+)\\]|(?<lt>[\\d.]+)\\)))|(?:(?:\\[(?<gte>[\\d.]+)|\\((?<gt>[\\d.]+))(?=,)))+";


    public static List<String> getAllVersions(String groupId,String artifactId) {
        Document document = MavenRepoHelper.getMavenMetadataDocument(groupId, artifactId);
        if(document == null) {
            return new ArrayList<>();
        } 
        NodeList list = document.getElementsByTagName("version");
        List<String> versions = new ArrayList<>();
        for(int i=0;i<list.getLength();i++) {
            Element el = (Element) list.item(i);
            versions.add(el.getTextContent());
        }
        return versions;
    }
    
    public static String resolveVersion(String groupId,String artifactId,String version) {
        if(version.length() == 0 || isPureVersion(version)) {
            return version;
        }
        ArrayList<String> allVersions = (ArrayList<String>) getAllVersions(groupId, artifactId);
        if(allVersions.isEmpty()) {
            return version;
        }
        List<MavenVersionRange> ranges = extractRanges(version);
        MavenVersion versionSpec = new MavenVersion(ranges);
        List<String> satisfiedVersions = new ArrayList<>();
        try {
            for(var v: allVersions) {
                if(versionSpec.isSatisfied(v)){
                    satisfiedVersions.add(v);
                }
            }
            List<Semver> semverVersions = new ArrayList<>();
        
            for(var s: satisfiedVersions) {
                semverVersions.add(new Semver(s,Semver.SemverType.NPM));
            }
            Collections.sort(semverVersions);
            return !semverVersions.isEmpty()? semverVersions.get(semverVersions.size()-1).toString() : version;
        } catch (SemverException ex) {
            return version;
        }
    }
    
    private static List<MavenVersionRange> extractRanges(String version) {
        List<MavenVersionRange> ranges = new ArrayList<>();
        
        Pattern combinationOfVersionPattern = Pattern.compile("(?<versionLeft>[\\[(]([\\d\\W\\w]\\.?)*,([\\d\\W\\w]\\.?)*[\\])])(?<delimiter>,)(?<versionRight>[\\[(]([\\d\\W\\w]\\.?)*,(\\d\\.?)*[\\])])");
        Matcher matcher = combinationOfVersionPattern.matcher(version);
        try {
            if(matcher.matches()){
                matcher.reset();
                while(matcher.find()){
                    String versionLeft = matcher.group("versionLeft");
                    String versionRight = matcher.group("versionRight");
                    // now this will cause duplication of mavenversionrange in the list,
                    // we can make it comparable and thus avoid adding the same range again,
                    // anyways we dont care right now because its not going to be a very long list
                    System.out.println(versionLeft + "," + versionRight);
                    ranges.add(MavenVersionRange.parse(versionLeft));
                    ranges.add(MavenVersionRange.parse(versionRight));

                }
                if(ranges.isEmpty()){
                    // Means no matches for combination of ranges
                    ranges.add(MavenVersionRange.parse(version));
                    System.out.println(MavenVersionRange.parse(version));
                }

            }else {
                ranges.add(MavenVersionRange.parse(version));
            }
        } catch(Exception e) {
            CLIHelper.updateCurrentLine("Failed to resolve version!", Ansi.Color.RED);
        }
        
        return ranges;
    }
    
    private static boolean isPureVersion(String version) {
        return !version.contains("(") 
                || !version.contains(")")
                || !version.contains("]")
                || !version.contains("[")
                || !version.contains(".");
    }
}
