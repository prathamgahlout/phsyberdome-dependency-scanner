
package com.phsyberdome.common.utils.models;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pratham Gahlout
 */
public class LicenseDetectionResult {
    
    private static final double DIFFERENTIATING_TOLERANCE = 0.05; // 5% 
    
    private List<Pair<String,Double>> licenses;
    private String content;
    
    public LicenseDetectionResult(List<Pair<String,Double>> licenses, String content) {
        this.licenses = licenses;
        this.content = content;
    }
    
    public LicenseDetectionResult(String content) {
        this.content = content;
        this.licenses = new ArrayList<>();
    }
    
    public LicenseDetectionResult() {
        this.licenses = new ArrayList<>();
        this.content = "";
    }
    
    public void addProbableLicense(Pair<String,Double> license) {
        if(licenses==null) {
            this.licenses = new ArrayList<>();
        }
        this.licenses.add(license);
    }
    
    private void sortLicensesByProbability() {
        List<Pair<String,Double>> modifiable = new ArrayList<>(this.licenses);
        modifiable.sort((Pair<String, Double> o1, Pair<String, Double> o2) -> {
            if(o1.second > o2.second){
                return -1;
            }else if(o1.second < o2.second) {
                return 1;
            }else{
                return 0;
            }
        });
        this.setLicenses(modifiable);
    }
    
    public List<Pair<String,Double>> getLicenses(){
        return this.licenses;
    }
    
    public String getAnalyzedContent() {
        return this.content;
    }
    
    public void setAnalyzedContent(String content){
        this.content = content;
    }
    
    public void setLicenses(List<Pair<String,Double>> licenses) {
        this.licenses = licenses;
    }
    
    public LicenseDetectionResult getResultWithMostProbableLicenses() {
        var newResult = new LicenseDetectionResult();
        newResult.setAnalyzedContent(this.content);
        var highest = this.licenses.getFirst().second;
        newResult.setLicenses(this.licenses.stream().filter(item -> (highest - item.second) < DIFFERENTIATING_TOLERANCE)
                .map(item -> {
                     return new Pair<>(item.first,Math.floor(item.second * 1000)/1000);
                }).limit(3).toList());
        newResult.sortLicensesByProbability();
        return newResult;
    }
    
    public String getLicensesAsString() {
        return String.join(" | ", licenses.stream().map(item -> item.first + " " + item.second).toList());
    }
}
