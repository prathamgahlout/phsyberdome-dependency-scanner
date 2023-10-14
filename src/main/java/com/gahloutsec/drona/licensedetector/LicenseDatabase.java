

package com.gahloutsec.drona.licensedetector;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.hash.HashFunction;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Pratham Gahlout
 */
public class LicenseDatabase {
    
    /*
        Extract names
        Extract urls
        Extract licenseTexts
        Normalize & build min hashess
        Add func to query against the db
    */
    
    /*
        // license name -> text
	licenseTexts map[string]string
	// minimum license text length
	minLicenseLength int
	// official license URL -> id
	idByURL map[string]string
	// id -> license URLs
	urlsByID map[string][]string
	// id -> license name
	nameByID map[string]string
	// all URLs joined
	urlRe *regexp.Regexp
	// first line of each license OR-ed - used to split
	firstLineRe *regexp.Regexp
	// unique unigrams -> index
	tokens map[string]int
	// document frequencies of the unigrams, indexes match with `tokens`
	docfreqs []int
	// Weighted MinHash hashtables
	lsh *minhashlsh.MinhashLSH
	// turns a license text into a hash
	hasher *wmh.WeightedMinHasher
	// part of license short name (e,g, BSL-1.0) -> list of containing license names
	nameShortSubstrings map[string][]substring
	// number of substrings per short license name
	nameShortSubstringSizes map[string]int
	// part of license name (e,g, Boost Software License 1.0) -> list of containing license names
	nameSubstrings map[string][]substring
	// number of substrings per license name
	nameSubstringSizes map[string]int
    */
    
    private Map<String,String> licenseTexts;
    private int minimumLicenseTextLength;
    private Map<String,String> idByURL;
    private Map<String,ArrayList<String>> urlsByID;
    private Map<String,String> nameByID;
    
    private Map<String,Integer> tokens;
    private int[] docfreqs;
    
    private LSH lsh;
    
    private final String pathToLicenseListJSON = "/json_data/licenses.json";
    private final String pathToLicenseDetailsDirJSON = "/json_data/details/";
    
    public LicenseDatabase() {
        licenseTexts = new HashMap<>();
        idByURL = new HashMap<>();
        urlsByID = new HashMap<>();
        nameByID = new HashMap<>();
        tokens = new HashMap<>();
        lsh = new LSH();
        //docfreqs = new ArrayList<>();
        minimumLicenseTextLength = 0;
    }
    
    private void loadURLs() {
        try(InputStream in = LicenseDatabase.class.getResourceAsStream(pathToLicenseListJSON)){
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(in);
            JsonNode licenses = jsonNode.get("licenses");
            
            if(licenses.isArray()){
                for(JsonNode node: licenses) {
                    String url = node.get("detailsUrl").asText();
                    String id = node.get("licenseId").asText();
                    idByURL.put(url, id);
                    
                    if(urlsByID.get(id) != null){
                        urlsByID.get(id).add(url);
                    }else{
                        ArrayList<String> urls = new ArrayList<>();
                        urls.add(url);
                        urlsByID.put(id, urls);
                    }
                }
            }
            
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    private void loadNames() {
        try(InputStream in = LicenseDatabase.class.getResourceAsStream(pathToLicenseListJSON)){
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(in);
            JsonNode licenses = jsonNode.get("licenses");
            
            if(licenses.isArray()){
                for(JsonNode node: licenses) {
                    String id = node.get("licenseId").asText();
                    String name = node.get("name").asText();
                    nameByID.put(id, name);
                }
            }
            
        }
        catch(Exception e){
            throw new RuntimeException(e);
        }
    }
    
    public void loadLicenses() {
        /* Load the metadata */
        loadURLs();
        loadNames();
        
        /* Load the details */
        // File name is the ID.
        // Iterate over the IDs and get the file, read and populate

        Map<String,Map<String,Integer>> tokenFreqs = new HashMap<>();
        Iterator it = nameByID.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String,String> pair = (Map.Entry) it.next();
            String id = (String) pair.getKey();
            String pathToLicenseDetail = pathToLicenseDetailsDirJSON + id + ".json";
            
            try(InputStream in = LicenseDatabase.class.getResourceAsStream(pathToLicenseDetail)){
                ObjectMapper mapper = new ObjectMapper();
                JsonNode jsonNode = mapper.readTree(in);
                String normLicenseText = Normalizer.normalize(jsonNode.get("licenseText").asText("null"));
                licenseTexts.put(id, normLicenseText);
                System.out.println("Processing license: " + id);
                if(minimumLicenseTextLength == 0 || minimumLicenseTextLength > normLicenseText.length()) {
                    minimumLicenseTextLength = normLicenseText.length();
                }
                
                Map<String,Integer> uniqueTokens = new HashMap<>();
                String[] lines = normLicenseText.split("\n");
                for(String line: lines) {
                    String[] _tokens = line.split(" ");
                    for(String token: _tokens) {
                        uniqueTokens.merge(token, 1, Integer::sum);
                    }
                }
                tokenFreqs.put(id, uniqueTokens);
            }
            catch(Exception e){
                throw new RuntimeException(e);
            }
        }
        
        Map<String,Integer> _docfreqs = new HashMap<>();
        Iterator iter = tokenFreqs.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String,Map<String,Integer>> pair = (Map.Entry) iter.next();
            Iterator iterUniqueTokens = pair.getValue().entrySet().iterator();
            while(iterUniqueTokens.hasNext()) {
                Map.Entry<String,Integer> p = (Map.Entry<String,Integer>) iterUniqueTokens.next();
                String token = p.getKey();
                _docfreqs.merge(token, p.getValue(), Integer::sum);
            }
        }
        String[] uniqueTokens = new String[_docfreqs.size()];
        {
            int i = 0;
            Iterator iterDocFreq = _docfreqs.entrySet().iterator();
            while(iterDocFreq.hasNext()) {
                Map.Entry<String,Integer> pair = (Map.Entry<String,Integer>) iterDocFreq.next();
                uniqueTokens[i++] = pair.getKey();
            }
        }
        
        Arrays.sort(uniqueTokens);
        
        docfreqs = new int[uniqueTokens.length];
        for(int i=0;i<uniqueTokens.length;i++){
            tokens.put(uniqueTokens[i], i);
            docfreqs[i] = _docfreqs.get(uniqueTokens[i]);
        }
        
        
        /* Build minHash */
        
        iter = tokenFreqs.entrySet().iterator();
        while(iter.hasNext()) {
            Map.Entry<String,Map<String,Integer>> pair = (Map.Entry) iter.next();
            Iterator iterUniqueTokens = pair.getValue().entrySet().iterator();
            
            List<WeightedElement> set = new ArrayList<>();
            
            while(iterUniqueTokens.hasNext()) {
                Map.Entry<String,Integer> utok = (Map.Entry<String,Integer>) iterUniqueTokens.next();
                set.add(new WeightedElement(tokens.get(utok.getKey()),tfidf(utok.getValue(), docfreqs[tokens.get(utok.getKey())], licenseTexts.size())));
            }
            // Add this list against the licenseId
            lsh.Add(pair.getKey(), set);
            System.out.println("Added signature for " + pair.getKey());
        }
        
    }
    
    
    public Map<String,Double> queryLicenseText(String licenseText) {
        String normalizedText = Normalizer.normalize(licenseText);
        
        
        Map<Integer,Integer> _tokens = new HashMap<>();
        String[] lines = normalizedText.split("\n");
        for(String line : lines) {
            String[] toks = line.split(" ");
            for(String tok : toks) {
                if(tokens.containsKey(tok)) {
                    if(_tokens.containsKey(tokens.get(tok))) {
                        _tokens.put(tokens.get(tok), _tokens.get(tokens.get(tok)) + 1);
                    }else{
                        _tokens.put(tokens.get(tok), 1);
                    }
                }
            }
        }
        
        List<WeightedElement> set = new ArrayList<>();
        _tokens.entrySet().forEach(pair -> {
            set.add(new WeightedElement(pair.getKey(),tfidf(pair.getValue(), docfreqs[pair.getKey()], licenseTexts.size())));
        });
        
        // Query against the database
        
        Map<String,Double> result =  lsh.Query(set);
        
        return result;
    }

    public Map<String, String> getLicenseTexts() {
        return licenseTexts;
    }

    public void setLicenseTexts(Map<String, String> licenseTexts) {
        this.licenseTexts = licenseTexts;
    }

    public int getMinimumLicenseTextLength() {
        return minimumLicenseTextLength;
    }

    public void setMinimumLicenseTextLength(int minimumLicenseTextLength) {
        this.minimumLicenseTextLength = minimumLicenseTextLength;
    }

    public Map<String, String> getIdByURL() {
        return idByURL;
    }

    public void setIdByURL(Map<String, String> idByURL) {
        this.idByURL = idByURL;
    }

    public Map<String, ArrayList<String>> getUrlsByID() {
        return urlsByID;
    }

    public void setUrlsByID(Map<String, ArrayList<String>> urlsByID) {
        this.urlsByID = urlsByID;
    }

    public Map<String, String> getNameByID() {
        return nameByID;
    }

    public void setNameByID(Map<String, String> nameByID) {
        this.nameByID = nameByID;
    }

    public Map<String, Integer> getTokens() {
        return tokens;
    }

    public void setTokens(Map<String, Integer> tokens) {
        this.tokens = tokens;
    }

    public int[] getDocfreqs() {
        return docfreqs;
    }

    public void setDocfreqs(int []docfreqs) {
        this.docfreqs = docfreqs;
    }
    
    
    
    
    private double tfidf(int freq,int docfreq, int ndocs){
        double weight = Math.log(1 + freq) * Math.log(ndocs/docfreq);
        if(weight < 0) {
            return 0;
        }
        return weight;
    }
    
}
