
import com.gahloutsec.drona.licensedetector.LicenseDatabase;
import com.gahloutsec.drona.licensedetector.LicenseDetector;
import com.gahloutsec.drona.licensedetector.Normalizer;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author pgahl
 */
public class LicenseDetectorTests {
    
    LicenseDatabase db;
    LicenseDetector detector;
    public LicenseDetectorTests() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
        db = new LicenseDatabase();
        detector = new LicenseDetector();
    }
    
    @AfterEach
    public void tearDown() {
    }

    
    @Test
    public void TestNormalizer() {
        String licenseText = "Apache Software License 2.0\n" +
                            "\n" +
                            "Copyright (c) 2019, Pratham Gahlout\n" +
                            "\n" +
                            "Licensed under the Apache License, Version 2.0 (the \"License\");\n" +
                            "you may not use this file except in compliance with the License.\n" +
                            "You may obtain a copy of the License at\n" +
                            "\n" +
                            "http://www.apache.org/licenses/LICENSE-2.0\n" +
                            "\n" +
                            "Unless required by applicable law or agreed to in writing, software\n" +
                            "distributed under the License is distributed on an \"AS IS\" BASIS,\n" +
                            "WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n" +
                            "See the License for the specific language governing permissions and\n" +
                            "limitations under the License.";
        
        String normalizedLicenseText = "licensed under the apache license version the license you may not use this file except in compliance with the license you may obtain a copy of the license atnormalized urlunless required by applicable law or agreed to in writing softwaredistributed under the license is distributed on an as is basis without warranties or conditions of any kind either express or implied see the license for the specific language governing permissions andlimitations under the license";
        
        String normalizedText = Normalizer.normalize(licenseText);
        
        assertTrue(normalizedLicenseText.equals(normalizedText));
    }
    
    @Test
    public void TestLoadLicenses() {
        
        db.loadLicenses();
        
        assertTrue(db.getUrlsByID().size() > 0 && db.getIdByURL().size() > 0 && db.getNameByID().size() > 0);
    }
    
    @Test
    public void TestQueryLicenseText() {
        db.loadLicenses();
        String licenseText = "Copyright <YEAR> <COPYRIGHT HOLDER>\n" +
                                "\n" +
                                "Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:\n" +
                                "\n" +
                                "1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.\n" +
                                "\n" +
                                "2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.\n" +
                                "\n" +
                                "3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.\n" +
                                "\n" +
                                "THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS “AS IS” AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.";
        
        Map<String,Double> res = db.queryLicenseText(licenseText);
        
        assertTrue(res.entrySet().iterator().next().getKey().equals("BSD-3-Clause"));
    }
    
    @Test
    public void TestDetectorWithZipUrl(){
        String artifactUrl = "https://repo1.maven.org/maven2/junit/junit/4.13.2/junit-4.13.2.jar";
        
        String result = detector.detect(artifactUrl);
        assertTrue(result.equals("EPL-1.0"));
    }
    
    @Test
    public void TestDetecterWithGitURL() {
        
        String repoUrl = "https://github.com/prathamgahlout/WallRoach-wallpaper-android-app";
        
        String result = detector.detect(repoUrl);
        assertTrue(result.equals("Apache-2.0"));
    }
    
        
}
