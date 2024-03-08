

package com.phsyberdome.plugin.maven;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 *
 * @author Pratham Gahlout
 */


public class MavenVersion {
        
    private List<MavenVersionRange> ranges;      
    
    public MavenVersion(MavenVersionRange... ranges) {
        this.ranges = new ArrayList<>();
        for(var range:ranges) {
            this.ranges.add(range);
        }
    }
    
    public MavenVersion(List<MavenVersionRange> ranges) {
        this.ranges = ranges;
    }

    public boolean isSatisfied(String version) {
        boolean isSatisfied = false;
        
        for(var range:ranges) {
            isSatisfied |= range.isSatisfied(version);
        }
        
        return isSatisfied;
    }

    @Override
    public String toString() {
        return ranges.stream()
                .map(MavenVersionRange::toString)
                .collect(Collectors.joining(""));
    }
    
    
    
    
}
