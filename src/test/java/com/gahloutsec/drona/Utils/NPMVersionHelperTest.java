package com.gahloutsec.drona.Utils;

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
public class NPMVersionHelperTest {
    
    public NPMVersionHelperTest() {
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
     * Test of pinpointPackageVersion method, of class NPMVersionHelper.
     */
    @Test
    public void testPinpointPackageVersion() {
        System.out.println("pinpointPackageVersion");
        String name = "browserslist";
        String version = "^1.3";
        String resolved = NPMVersionHelper.pinpointPackageVersion(name, version);
        System.out.println(resolved);
    }

    /**
     * Test of getLatestVersion method, of class NPMVersionHelper.
     */
    @Test
    public void testGetLatestVersion() {
        System.out.println("getLatestVersion");
        
    }

    /**
     * Test of fetchAllVersions method, of class NPMVersionHelper.
     */
    @Test
    public void testFetchAllVersions() {
        System.out.println("fetchAllVersions");
        String name = "browserslist";
        List<String> versions = NPMVersionHelper.fetchAllVersions(name);
        System.out.println(versions);
    }

    /**
     * Test of sortVersions method, of class NPMVersionHelper.
     */
    @Test
    public void testSortVersions() {
        System.out.println("sortVersions");
        
    }

    /**
     * Test of isMajorMinorPatchVersionFormat method, of class NPMVersionHelper.
     */
    @Test
    public void testIsMajorMinorPatchVersionFormat() {
        System.out.println("isMajorMinorPatchVersionFormat");
        
    }

   
    
    
}
