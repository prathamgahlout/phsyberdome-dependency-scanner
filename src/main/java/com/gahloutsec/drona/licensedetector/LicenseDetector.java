

package com.gahloutsec.drona.licensedetector;

import com.gahloutsec.drona.Utils.FileUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    
    private final String cloneLocation = "/.drona/temp/remote_clones/";

    public LicenseDetector() {
        this.db = new LicenseDatabase();
        db.loadLicenses();
    }
    
    
    
    
    private Path searchLicenseFile(String rootString) {
        Path root = Paths.get(rootString);
        
        for(String name:variationsInNames){
            File file = FileUtil.searchFile(root.toFile(), name);
            if(file != null) {
                return file.toPath();
            }
        }
        // Search the README for data
        return null;
    }
    
    private String readLicense(Path licensePath) {
        // If it is plain text file
        try {
            String licenseText;
            try (Stream<String> content = Files.lines(licensePath)) {
                licenseText = content.collect(Collectors.joining("\n"));
            }
            return licenseText;
        } catch (IOException ex) {
            Logger.getLogger(LicenseDetector.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
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
   
    public String detect(String path){
        // Check if it is valid url
        
        if(isValidURL(path)){
            
            Path rootPath = FileUtil.getFilePathFromURL(path,cloneLocation);
            String result = filePathDetection(rootPath.toString());
            if(rootPath.toFile().exists()){
                FileUtil.deleteDirectory(rootPath.toFile());
            }
            return result;
        }else{
            return filePathDetection(path);
        }
        
    }
    
    
    private String filePathDetection(String path) {
        Path pathToFile = searchLicenseFile(path);
            if(pathToFile == null) {
                System.out.println("Couldn't get license file at "+path);
                totalRogues++;
                return "null";
            }
            totalScanned++;
            String content = readLicense(pathToFile);
        
            Map<String,Double> res = analyze(content);
            if(res.isEmpty()) {
                return "null";
            }else{
                return res.entrySet().iterator().next().getKey();
            }
    }
    
    
    
    public void printScanStats() {
        System.out.println("Total Dependencies Scanned: "+ totalScanned);
        System.out.println("Total Dependencies where License not found: " + totalRogues);
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
