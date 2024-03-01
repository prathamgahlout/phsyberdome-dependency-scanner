package com.phsyberdome.drona.Utils;

import java.util.List;
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
public class MavenVersionHelperTest {
    
    public MavenVersionHelperTest() {
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
     * Test of getAllVersions method, of class MavenVersionHelper.
     */
    @Test
    public void testGetAllVersions() {
        System.out.println("getAllVersions");
        String groupId = "com.fasterxml.jackson.core";
        String artifactId = "jackson-databind";
        List<String> expResult = null;
        List<String> result = MavenVersionHelper.getAllVersions(groupId, artifactId);
        assertTrue(result.size() > 0);
//        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
    }

    /**
     * Test of resolveVersion method, of class MavenVersionHelper.
     */
    @Test
    public void testResolveVersion() {
        System.out.println("resolveVersion");
        String groupId = "com.fasterxml.jackson.core";
        String artifactId = "jackson-databind";
        String version = "[2.16.0,2.17.0-rc1)";
//        String version = "2.4.0-rc3";
        String expResult = "2.16.1";
        String result = MavenVersionHelper.resolveVersion(groupId,artifactId,version);
        assertEquals(expResult,result);
    }
    
}
