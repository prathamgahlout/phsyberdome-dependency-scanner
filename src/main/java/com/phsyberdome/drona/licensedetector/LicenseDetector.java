

package com.phsyberdome.drona.licensedetector;

import com.phsyberdome.drona.CLIHelper;
import com.phsyberdome.drona.Configuration;
import com.phsyberdome.drona.Models.Pair;
import com.phsyberdome.drona.Utils.FileUtil;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.fusesource.jansi.Ansi;


/**
 *
 * @author Pratham Gahlout
 */
public class LicenseDetector {
    
    private LicenseDatabase db;
    private int totalRogues = 0;
    private int totalScanned = 0;
    String[] variationsInNames = {
            "LICENSE","license","licence","LICENCE","LISENCE","lisence",
            "LICENSE.txt","license.txt","licence.txt","LICENCE.txt","LISENCE.txt","lisence.txt",
            "LICENSE.md","license.md","licence.md","LICENCE.md","LISENCE.md","lisence.md",
            "LICENSE.html","license.html","licence.html","LICENCE.html","LISENCE.html","lisence.html",
            "LICENSE-junit.txt"
        };
    // TODO: Improve regex/Use 'i' flag for case insensitivity
    String licenseFileRegex = "([Ll][Ii][CScs][Ee][Nn][CScs][Ee]([Ss]?)).*(\\.(txt|md|TXT|MD|html|HTML))?";
    String readMeFileRegex = "(?i)(readme|guide)(\\.(txt|md|TXT|MD|html|HTML))?";
    
    private final String cloneLocation = Configuration.getConfiguration().getCloneLocation().toString();

    public LicenseDetector() {
        this.db = new LicenseDatabase();
        db.loadLicenses();
    }
    
    
    
    
    private Path searchLicenseFile(String rootString) {
        Path root = Paths.get(rootString);
        
        //for(String name:[]){
            File file = FileUtil.searchFile(root.toFile(), licenseFileRegex);
              if(file != null) {
                return file.toPath();
            }
        //}
        // Search the README for data
        return null;
    }
    
    private Path searchReadmeFile(String rootString){
        Path root = Paths.get(rootString);
        
        //for(String name:[]){
            File file = FileUtil.searchFile(root.toFile(), readMeFileRegex);
              if(file != null) {
                return file.toPath();
            }
        //}
        return null;
    }
    
    private String readLicense(Path licensePath) {
        // If it is plain text file
        return FileUtil.readFile(licensePath);
    }
    
    private Map<String,Double> analyze(String licenseContent) {
        /*
        Algorithm from
        https://github.com/go-enry/go-license-detector
        Normalize the text according to SPDX recommendations.
        Split the text into unigrams and build the weighted bag of words.
        Calculate Weighted MinHash.
        Apply Locality Sensitive Hashing and pick the reference licenses which are close.
        For each of the candidate, calculate the Levenshtein distance - D. the corresponding text is the single line with each unigram represented by a single rune (character).
        Set the similarity as 1 - D / L where L is the number of unigrams in the quieried license.
        */
        
        Map<String,Double> queryResult = db.queryLicenseText(licenseContent);
        
        // Should filter here based on threshold
        
        return queryResult;
    }
   
    public Pair<String,String> detect(String path){
        // Check if it is valid url
        
        if(isValidURL(path)){
            
            Path rootPath = FileUtil.getFilePathFromURL(path,cloneLocation);
            Pair<String,String> result = filePathDetection(rootPath.toString());
            if(rootPath.toFile().exists()){
                FileUtil.deleteDirectory(rootPath.toFile());
            }
            return result;
        }else{
            return filePathDetection(path);
        }
        
    }
    
    
    private Pair<String,String> filePathDetection(String path) {
        Path pathToFile = searchLicenseFile(path);
        totalScanned++;
        if(pathToFile == null) {
            CLIHelper.updateCurrentLine("Couldn't get license file at "+path,Ansi.Color.RED);
            totalRogues++;
            // Do some investigation on README?
            // Maybe handover this responsiblity to the cloud service.
            pathToFile = searchReadmeFile(path);
            if(pathToFile == null){
                return new Pair<>("null","null");
            }
            String content = FileUtil.readFile(pathToFile);
            return new Pair<>("null",content);
        }

        String content = readLicense(pathToFile);

        Map<String,Double> res = analyze(content);
        if(res.isEmpty()) {
            return new Pair<>("null","null");
        }else{
            return new Pair<>(res.entrySet().iterator().next().getKey(),content);
        }
    }
    
    
    
    public void printScanStats() {
        CLIHelper.updateCurrentLine("", Ansi.Color.YELLOW);
        CLIHelper.printLine("Total Dependencies Scanned: "+ totalScanned,Ansi.Color.CYAN);
        CLIHelper.printLine("Total Dependencies where License not found: " + totalRogues,Ansi.Color.CYAN);
        CLIHelper.printDivider(Ansi.Color.YELLOW);
    }
    
    private boolean isValidURL(String urlString) {
        try{
            URL url = new URL(urlString);
            url.toURI();
            return true;
        }catch(Exception e){
            return false;
        }
    }
    
    
    
   
}
