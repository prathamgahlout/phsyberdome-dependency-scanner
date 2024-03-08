

package com.phsyberdome.plugin.maven;

import com.vdurmont.semver4j.Semver;
import com.vdurmont.semver4j.SemverException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Pratham Gahlout
 */
class MavenVersionRange {
    private boolean isLeftClosedInterval = false;
    private boolean isRightClosedInterval = false;
    private String A;
    private String B;

    private MavenVersionRange(String A, boolean isHard) {
        this.A = A;
        this.B = A;
        isLeftClosedInterval = isHard;
        isRightClosedInterval = isHard;
    }

    private MavenVersionRange(String A, String B, boolean isLeftClosed, boolean isRightClosed) {
        this.A = A;
        this.B = B;
        isLeftClosedInterval = isLeftClosed;
        isRightClosedInterval = isRightClosed;
    }


    public static MavenVersionRange parse(String version) throws Exception {
        Pattern pattern = Pattern.compile(",");
        Matcher matcher = pattern.matcher(version);

        if(matcher.find()){
            // This is a range. This only supports a single range and not a combination of ranges
            // so we assume the occurance of ',' will only be one time
            Pattern rangeMatchPattern = Pattern.compile("[(\\[](?<left>([\\d\\w\\W]\\.?)*),(?<right>([\\d\\w\\W]\\.?)*)[\\])]");
            Matcher rangeMatch = rangeMatchPattern.matcher(version);
            if(rangeMatch.matches()){
                var isLeftClosed = !version.contains("(");
                var isRightClosed = !version.contains(")");
                var leftInterval = rangeMatch.group("left");
                var rightInterval = rangeMatch.group("right");
                leftInterval = leftInterval.isBlank() ? "0" : leftInterval;
                rightInterval = rightInterval.isBlank() ? "999999" : rightInterval;
                return new MavenVersionRange(leftInterval,rightInterval,isLeftClosed,isRightClosed);
            }else {
                // Throw invalid version exception
                throw new Exception();
            }

        }else {
            // Its either a soft requirement or a hard requirement
            if(version.endsWith("]")){
                // its a hard requirement
                return new MavenVersionRange(version.replaceAll("\\]","").replaceAll("\\[", ""), true);
            }else {
                // its a soft requirement
                return new MavenVersionRange(version,false);
            }
        }
    }

    public boolean isSatisfied(String version) throws SemverException{
        Semver leftInterval = new Semver(A, Semver.SemverType.NPM);
        Semver rightInterval = new Semver(B, Semver.SemverType.NPM);
        Semver versionToCompare = new Semver(version, Semver.SemverType.NPM);
        if(leftInterval.isEqualTo(rightInterval)) {
            return versionToCompare.isEqualTo(leftInterval);
        }else {
            if(isLeftClosedInterval && isRightClosedInterval) {
                // Means a hard req

                return versionToCompare.isGreaterThanOrEqualTo(leftInterval)
                        && versionToCompare.isLowerThanOrEqualTo(rightInterval);
            }else if(isLeftClosedInterval) {
                return versionToCompare.isGreaterThanOrEqualTo(leftInterval)
                        && versionToCompare.isLowerThan(rightInterval);
            }else if(isRightClosedInterval) {
                return versionToCompare.isGreaterThan(leftInterval)
                        && versionToCompare.isLowerThanOrEqualTo(rightInterval);
            }else {
                return versionToCompare.isGreaterThan(leftInterval)
                        && versionToCompare.isLowerThan(rightInterval);
            }
        }
        
    }

    @Override
    public String toString() {
        String str = "";
        str += isLeftClosedInterval ? "[" : "(";
        str += A + ",";
        str += B;
        str += isRightClosedInterval ? "]" : ")";
        return str;
    }

    public boolean equals(MavenVersionRange obj) {
        return this.isLeftClosedInterval == obj.isLeftClosedInterval
                && this.isRightClosedInterval == obj.isRightClosedInterval
                && this.A.equals(obj.A)
                && this.B.equals(obj.B);
    }
    
    
        
}
