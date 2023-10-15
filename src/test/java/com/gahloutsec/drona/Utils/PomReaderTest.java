package com.gahloutsec.drona.Utils;

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
public class PomReaderTest {
    
    public PomReaderTest() {
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
     * Test of isProperty method, of class PomReader.
     */
    @Test
    public void testIsProperty() {
        String property = "${jackson.annotations}";
        assertTrue(PomReader.isProperty(property));
    }
    
}
