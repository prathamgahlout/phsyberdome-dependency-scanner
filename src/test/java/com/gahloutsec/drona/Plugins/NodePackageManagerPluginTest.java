package com.gahloutsec.drona.Plugins;

import com.gahloutsec.drona.Models.Dependencies;
import com.gahloutsec.drona.Models.DependencyManager;
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
public class NodePackageManagerPluginTest {
    
    public NodePackageManagerPluginTest() {
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
     * Test of readModules method, of class NodePackageManagerPlugin.
     */
    @Test
    public void testReadModules() {
        System.out.println("readModules");
        NodePackageManagerPlugin instance = null;
        instance.readModules();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of readModulesV2 method, of class NodePackageManagerPlugin.
     */
    @Test
    public void testReadModulesV2() {
        System.out.println("readModulesV2");
        NodePackageManagerPlugin instance = null;
        instance.readModulesV2();
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getModules method, of class NodePackageManagerPlugin.
     */
    @Test
    public void testGetModules() {
        System.out.println("getModules");
        NodePackageManagerPlugin instance = null;
        Dependencies expResult = null;
        Dependencies result = instance.getModules();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getPackageManager method, of class NodePackageManagerPlugin.
     */
    @Test
    public void testGetPackageManager() {
        System.out.println("getPackageManager");
        NodePackageManagerPlugin instance = null;
        DependencyManager expResult = null;
        DependencyManager result = instance.getPackageManager();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of buildNpmRegistryUrl method, of class NodePackageManagerPlugin.
     */
    @Test
    public void testBuildNpmRegistryUrl() {
        
    }
    
}
