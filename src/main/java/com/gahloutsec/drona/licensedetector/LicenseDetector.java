

package com.gahloutsec.drona.licensedetector;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public LicenseDetector() {
        this.db = new LicenseDatabase();
        db.loadLicenses();
    }
    
    
    
    
    private Path searchLicenseFile(Path root) {
        String[] variationsInNames = {
            "LICENSE","license","licence","LICENCE","LISENCE","lisence",
            "LICENSE.txt","license.txt","licence.txt","LICENCE.txt","LISENCE.txt","lisence.txt",
            "LICENSE.md","license.md","licence.md","LICENCE.md","LISENCE.md","lisence.md",
            "LICENSE.html","license.html","licence.html","LICENCE.html","LISENCE.html","lisence.html",
        };
        for(String name:variationsInNames){
            Path path = root.resolve(name);
            if(path.toFile().exists()){
                return path;
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
   
    public String detect(Path path){
        Path pathToFile = searchLicenseFile(path);
        if(pathToFile == null) {
            System.out.println("Couldn't get license file at "+path.toString());
            return "null";
        }
        String content = readLicense(pathToFile);
        
        Map<String,Double> res = analyze(content);
        if(res.isEmpty()) {
            return "null";
        }else{
            return res.entrySet().iterator().next().getKey();
        }
    }
    
    
}
