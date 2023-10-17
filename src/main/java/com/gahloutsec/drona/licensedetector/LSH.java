

package com.gahloutsec.drona.licensedetector;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Pratham Gahlout
 */
public class LSH {

    private Map<String,long[]> signatures;
    WeightedMinHash minHash;

    public LSH() {
        signatures = new HashMap<>();
        minHash = new WeightedMinHash(154);
    }
    
    public void Add(String key,List<WeightedElement> elems) {
        signatures.put(key, minHash.computeSignature(elems));
    }
    
    
    public Map<String,Double> Query(List<WeightedElement> querySet) {
        
        
        long[] querySig = minHash.computeSignature(querySet);
        
        Map<String,Double> result = new HashMap<>();
        
        Iterator it = signatures.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<String,long[]> pair = (Map.Entry<String,long[]>) it.next();
            long[] sig = pair.getValue();
            String licenseId = pair.getKey();
            
            double similarity = minHash.estimateJaccardSimilarity(querySig, sig);
            if(similarity > 0.7)
                result.put(licenseId, similarity);
        }
        
        if(result.isEmpty()) {
            System.out.println("No license match found from the database... Getting help from the OpenAI API");
            
            //Call the api and get the result
            
            return result;
        }
        
        result = sortByValue(result);
        return result;
    }
    public static HashMap<String, Double> sortByValue(Map<String, Double> hm)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, Double> > list =
               new LinkedList<Map.Entry<String, Double> >(hm.entrySet());
 
        // Sort the list
        Collections.sort(list, new Comparator<Map.Entry<String, Double> >() {
            public int compare(Map.Entry<String, Double> o1, 
                               Map.Entry<String, Double> o2)
            {
                return (o2.getValue()).compareTo(o1.getValue());
            }
        });
         
        // put data from sorted list to hashmap 
        HashMap<String, Double> temp = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> aa : list) {
            temp.put(aa.getKey(), aa.getValue());
        }
        return temp;
    }
}
