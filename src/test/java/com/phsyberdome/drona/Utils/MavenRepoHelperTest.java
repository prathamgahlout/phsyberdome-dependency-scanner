package com.phsyberdome.drona.Utils;

import com.phsyberdome.drona.CLIHelper;
import com.phsyberdome.drona.Configuration;
import com.phsyberdome.drona.Utils.FileUtil;
import com.phsyberdome.drona.Utils.MavenRepoHelper;
import java.io.File;
import java.nio.file.Path;
import org.fusesource.jansi.Ansi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import org.w3c.dom.Document;

/**
 *
 * @author pgahl
 */
public class MavenRepoHelperTest {
    
    public MavenRepoHelperTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

    /**
     * Test of isProperty method, of class MavenRepoHelper.
     */
    @Test
    public void testIsProperty() {
        String property = "${jackson.annotations}";
        assertTrue(MavenRepoHelper.isProperty(property));
    }
    
    @Test
    public void testGetVersionFromParent() {
        String aritifactId = "jansi";
        Configuration.getConfiguration().setBasePath(".");
        File file = FileUtil.searchFile(Configuration.getConfiguration().getBasePath().toFile(), "(.*\\.(pom|POM))|(pom\\.(xml|XML))");
        if(file == null) {
            CLIHelper.updateCurrentLine("pom file not found in project",Ansi.Color.RED);

            return;
        }
        Path path = file.toPath();
        if(path!=null && path.toFile().exists()){
            // Read the xml
            Document doc = MavenRepoHelper.readXMLDocument(path);
            String value = MavenRepoHelper.getVersionFromParent(aritifactId, doc);
            System.out.println(value);
        }
    }
    
}
