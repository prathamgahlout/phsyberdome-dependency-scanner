

package com.phsyberdome.licensedetector;

import com.phsyberdome.common.interfaces.LicenseDetectorInterface;
import com.phsyberdome.common.utils.CLIHelper;
import com.phsyberdome.common.utils.models.Pair;
import com.phsyberdome.common.utils.FileUtil;
import com.phsyberdome.common.utils.models.LicenseDetectionResult;
import com.phsyberdome.common.utils.NetworkHelper;
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import org.fusesource.jansi.Ansi;


/**
 *
 * @author Pratham Gahlout
 */
public class LicenseDetector implements LicenseDetectorInterface {
    
    private LicenseDatabase db;
    private int totalRogues = 0;
    private int totalScanned = 0;
    private Path cloneLocation;
    private String licenseDataUrl = "";
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
    
    
    public LicenseDetector(Path cloneLocation, String licenseDataUrl) {
        this.cloneLocation = cloneLocation;
        this.licenseDataUrl = licenseDataUrl;
        this.db = new LicenseDatabase(cloneLocation,licenseDataUrl);
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
   
    public LicenseDetectionResult detect(String path){
        // Check if it is valid url
        
        if(NetworkHelper.isValidURL(path)){
            
            Path rootPath = FileUtil.getFilePathFromURL(path,cloneLocation.toString());
            LicenseDetectionResult result = filePathDetection(rootPath.toString());
            if(rootPath.toFile().exists()){
                FileUtil.deleteDirectory(rootPath.toFile());
            }
            return result;
        }else{
            return filePathDetection(path);
        }
        
    }
    
    
    private LicenseDetectionResult filePathDetection(String path) {
        Path pathToFile = searchLicenseFile(path);
        var results = new LicenseDetectionResult();
        totalScanned++;
        if(pathToFile == null) {
            CLIHelper.updateCurrentLine("Couldn't get license file at "+path,Ansi.Color.RED);
            totalRogues++;
            // Do some investigation on README?
            // Maybe handover this responsiblity to the cloud service.
            pathToFile = searchReadmeFile(path);
            if(pathToFile == null){
                results.addProbableLicense(new Pair<>("null",0.0));
                results.setAnalyzedContent("");
                return results;
            }
            String content = FileUtil.readFile(pathToFile);
            results.addProbableLicense(new Pair<>("null",0.0));
            results.setAnalyzedContent("");
            return results;
        }

        String content = readLicense(pathToFile);

        Map<String,Double> res = analyze(content);
        if(res.isEmpty()) {
            results.addProbableLicense(new Pair<>("null",0.0));
            results.setAnalyzedContent(content);
            return results;
        }else{
            res.entrySet().forEach(possibility ->  {
                results.addProbableLicense(new Pair<>(possibility.getKey(), possibility.getValue()));
            });
            results.setAnalyzedContent(content);
            return results;
        }
    }
    
    
    
    public void printScanStats() {
        CLIHelper.updateCurrentLine("", Ansi.Color.YELLOW);
        CLIHelper.printLine("Total Dependencies Scanned: "+ totalScanned,Ansi.Color.CYAN);
        CLIHelper.printLine("Total Dependencies where License not found: " + totalRogues,Ansi.Color.CYAN);
        CLIHelper.printDivider(Ansi.Color.YELLOW);
    }
    
    
    
    
   
}
