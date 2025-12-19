package com.cryptocheckertest;

import com.cryptochecker.Debug;
import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

public class DebugPitTest {

    @BeforeEach
    void setUp() {
        Debug.mode = false;
    }

    // ===== BASIC MODE TESTS =====

    @Test
    void testDebugModeInitiallyFalse() {
        assertFalse(Debug.mode);
    }

    @Test
    void testDebugModeCanBeSetTrue() {
        Debug.mode = true;
        assertTrue(Debug.mode);
    }

    @Test
    void testDebugModeCanBeSetFalse() {
        Debug.mode = false;
        assertFalse(Debug.mode);
    }

    @Test
    void testDebugModeToggleFalseToTrue() {
        Debug.mode = false;
        Debug.mode = !Debug.mode;
        assertTrue(Debug.mode);
    }

    @Test
    void testDebugModeToggleTrueToFalse() {
        Debug.mode = true;
        Debug.mode = !Debug.mode;
        assertFalse(Debug.mode);
    }

    // ===== BOOLEAN LOGIC TESTS =====

    @Test
    void testDebugModeLogicalAndBothTrue() {
        Debug.mode = true;
        assertTrue(Debug.mode && true);
    }

    @Test
    void testDebugModeLogicalAndMixedTrueFalse() {
        Debug.mode = true;
        assertFalse(Debug.mode && false);
    }

    @Test
    void testDebugModeLogicalAndModeFalse() {
        Debug.mode = false;
        assertFalse(Debug.mode && true);
    }

    @Test
    void testDebugModeLogicalOrModeTrue() {
        Debug.mode = true;
        assertTrue(Debug.mode || false);
    }

    @Test
    void testDebugModeLogicalOrModeFalseOtherTrue() {
        Debug.mode = false;
        assertTrue(Debug.mode || true);
    }

    @Test
    void testDebugModeLogicalOrBothFalse() {
        Debug.mode = false;
        assertFalse(Debug.mode || false);
    }

    @Test
    void testDebugModeLogicalNotWhenTrue() {
        Debug.mode = true;
        assertFalse(!Debug.mode);
    }

    @Test
    void testDebugModeLogicalNotWhenFalse() {
        Debug.mode = false;
        assertTrue(!Debug.mode);
    }

    // ===== CONDITIONAL TESTS =====

    @Test
    void testDebugModeInIfStatementTrue() {
        Debug.mode = true;
        boolean executed = false;
        if (Debug.mode) executed = true;
        assertTrue(executed);
    }

    @Test
    void testDebugModeInIfStatementFalse() {
        Debug.mode = false;
        boolean executed = false;
        if (Debug.mode) executed = true;
        assertFalse(executed);
    }

    @Test
    void testDebugModeTernaryTrue() {
        Debug.mode = true;
        String result = Debug.mode ? "enabled" : "disabled";
        assertEquals("enabled", result);
    }

    @Test
    void testDebugModeTernaryFalse() {
        Debug.mode = false;
        String result = Debug.mode ? "enabled" : "disabled";
        assertEquals("disabled", result);
    }

    // ===== EQUALITY TESTS =====

    @Test
    void testDebugModeEqualityTrue() {
        Debug.mode = true;
        assertTrue(Debug.mode == true);
        assertFalse(Debug.mode == false);
    }

    @Test
    void testDebugModeEqualityFalse() {
        Debug.mode = false;
        assertTrue(Debug.mode == false);
        assertFalse(Debug.mode == true);
    }

    @Test
    void testDebugModeInequalityTrue() {
        Debug.mode = true;
        assertFalse(Debug.mode != true);
        assertTrue(Debug.mode != false);
    }

    @Test
    void testDebugModeInequalityFalse() {
        Debug.mode = false;
        assertFalse(Debug.mode != false);
        assertTrue(Debug.mode != true);
    }

    // ===== XOR TESTS =====

    @Test
    void testDebugModeXorBothTrue() {
        Debug.mode = true;
        assertFalse(Debug.mode ^ true);
    }

    @Test
    void testDebugModeXorTrueFalse() {
        Debug.mode = true;
        assertTrue(Debug.mode ^ false);
    }

    @Test
    void testDebugModeXorFalseTrue() {
        Debug.mode = false;
        assertTrue(Debug.mode ^ true);
    }

    @Test
    void testDebugModeXorBothFalse() {
        Debug.mode = false;
        assertFalse(Debug.mode ^ false);
    }
}
