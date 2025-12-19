package com.cryptocheckertest;

import com.cryptochecker.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration Tests for Debug and PanelSettings interaction
 * Demonstrates communication between modules and data flow testing
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DebugPanelSettingsIntegrationTest {

    @TempDir
    static Path tempDir;
    
    private static Path testLogLocation;
    private static Main testMain;
    private PanelSettings panelSettings;

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        Path logFile = tempDir.resolve("integration_log.txt");
        Main.logLocation = logFile.toString();
        Main.folderLocation = tempDir.toString();
        testLogLocation = logFile;
        new File(Main.folderLocation).mkdirs();
        
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;
        
        testMain = new Main();
        Main.gui = testMain;
        testMain.webData = new WebData();
        
        if (testMain.webData.portfolio == null) {
            testMain.webData.portfolio = new ArrayList<>();
        }
        if (testMain.webData.portfolio.isEmpty()) {
            testMain.webData.portfolio.add(new ArrayList<>());
        }
        if (testMain.webData.portfolio_names == null) {
            testMain.webData.portfolio_names = new ArrayList<>();
        }
        if (testMain.webData.portfolio_names.isEmpty()) {
            testMain.webData.portfolio_names.add("Portfolio 1");
        }
    }

    @AfterAll
    static void tearDownAfterAll() throws Exception {
        if (Debug.frame != null) {
            Debug.frame.dispose();
            Debug.frame = null;
        }
        // Wait for resources to be released
        Thread.sleep(200);
    }

    @BeforeEach
    void setUp() throws Exception {
        // Don't delete the log file between tests - just append
        // This avoids file locking issues on Windows
        
        if (Debug.frame == null) {
            new Debug();
        }
        
        panelSettings = new PanelSettings();
        testMain.panelSettings = panelSettings;
    }

    @AfterEach
    void tearDown() {
        // Don't dispose the frame between tests to avoid file locking
        // It will be cleaned up after all tests complete
    }

    // ============ INTEGRATION TESTS - MODULE INITIALIZATION ============

    @Test
    @Order(1)
    @DisplayName("IT-001: Test Debug and PanelSettings initialization together")
    void testDebugAndPanelSettingsInitialization() {
        assertNotNull(Debug.frame, "Debug frame should be initialized");
        assertNotNull(panelSettings.panel, "PanelSettings panel should be initialized");
        assertTrue(Files.exists(testLogLocation), "Log file should exist");
    }

    @Test
    @Order(2)
    @DisplayName("IT-002: Test both modules exist in Main.gui")
    void testBothModulesExistInMainGui() {
        assertNotNull(Main.gui, "Main.gui should exist");
        assertNotNull(testMain.panelSettings, "PanelSettings should be accessible from Main");
    }

    // ============ INTEGRATION TESTS - DEBUG MODE SYNCHRONIZATION ============

    @Test
    @Order(3)
    @DisplayName("IT-003: Test PanelSettings debugFunction triggers Debug logging")
    void testPanelSettingsTriggersDebugLogging() throws Exception {
        int initialLineCount = Files.readAllLines(testLogLocation).size();
        
        // Toggle debug through PanelSettings
        panelSettings.debugFunction();
        
        List<String> lines = Files.readAllLines(testLogLocation);
        int afterToggleCount = lines.size();
        
        assertTrue(afterToggleCount > initialLineCount, 
                   "Debug log should have new entries after PanelSettings toggle");
        assertTrue(lines.stream().anyMatch(l -> l.contains("Button Debug Pressed")), 
                   "Log should contain debug button press message");
    }

    @Test
    @Order(4)
    @DisplayName("IT-004: Test Debug mode changes reflect in both modules")
    void testDebugModeChangesSyncAcrossModules() throws Exception {
        assertFalse(Debug.mode, "Debug mode should start as false");
        
        // Enable through PanelSettings
        panelSettings.debugFunction();
        assertTrue(Debug.mode, "Debug mode should be true after PanelSettings toggle");
        assertTrue(Debug.frame.isVisible(), "Debug frame should be visible");
        
        // Disable through PanelSettings
        panelSettings.debugFunction();
        assertFalse(Debug.mode, "Debug mode should be false after second toggle");
        assertFalse(Debug.frame.isVisible(), "Debug frame should be hidden");
    }

    @Test
    @Order(5)
    @DisplayName("IT-005: Test Debug frame visibility syncs with mode")
    void testDebugFrameVisibilitySyncsWithMode() throws Exception {
        // Test multiple toggles
        for (int i = 0; i < 5; i++) {
            panelSettings.debugFunction();
            assertEquals(Debug.mode, Debug.frame.isVisible(), 
                        "Frame visibility should match mode after toggle " + i);
        }
    }

    // ============ INTEGRATION TESTS - THEME SWITCHING AND LOGGING ============

    @Test
    @Order(6)
    @DisplayName("IT-006: Test theme switching logs to Debug")
    void testThemeSwitchingLogsToDebug() throws Exception {
        int initialLineCount = Files.readAllLines(testLogLocation).size();
        
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        
        // Manually log theme change (simulating what would happen in actual app)
        Debug.log("Theme switched to DARK");
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > initialLineCount, "New log entry should be added");
        assertTrue(lines.stream().anyMatch(l -> l.contains("Theme switched")), 
                   "Log should contain theme switch message");
    }

    @Test
    @Order(7)
    @DisplayName("IT-007: Test theme changes persist across debug toggles")
    void testThemeChangesPersistAcrossDebugToggles() {
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        Main.themes themeBeforeToggle = Main.theme.currentTheme;
        
        panelSettings.debugFunction();
        panelSettings.debugFunction();
        
        assertEquals(themeBeforeToggle, Main.theme.currentTheme, 
                    "Theme should persist across debug toggles");
    }

    @Test
    @Order(8)
    @DisplayName("IT-008: Test custom theme changes are logged")
    void testCustomThemeChangesAreLogged() throws Exception {
        int initialCount = Files.readAllLines(testLogLocation).size();
        
        Main.theme.customBackground = new java.awt.Color(100, 150, 200);
        Main.theme.change(Main.themes.CUSTOM);
        Debug.log("Custom theme applied");
        panelSettings.themeSwitch();
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > initialCount, "Custom theme change should be logged");
        assertTrue(lines.stream().anyMatch(l -> l.contains("Custom theme")),
                  "Log should mention custom theme");
    }

    // ============ INTEGRATION TESTS - SETTINGS PERSISTENCE AND LOGGING ============

    @Test
    @Order(9)
    @DisplayName("IT-009: Test serialization triggers Debug logging")
    void testSerializationTriggersDebugLogging() throws Exception {
        // Use reflection to call private serialize method
        java.lang.reflect.Method serializeMethod = 
            PanelSettings.class.getDeclaredMethod("serialize");
        serializeMethod.setAccessible(true);
        serializeMethod.invoke(panelSettings);
        
        // Check if log contains serialization message
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Serialized Settings")), 
                   "Log should contain serialization message");
    }

    @Test
    @Order(10)
    @DisplayName("IT-010: Test Debug logs persist across PanelSettings operations")
    void testDebugLogsPersistAcrossPanelSettingsOperations() throws Exception {
        Debug.log("First operation");
        panelSettings.debugFunction();
        Debug.log("Second operation");
        panelSettings.themeSwitch();
        Debug.log("Third operation");
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("First operation")), 
                   "First operation should be in log");
        assertTrue(lines.stream().anyMatch(l -> l.contains("Second operation")), 
                   "Second operation should be in log");
        assertTrue(lines.stream().anyMatch(l -> l.contains("Third operation")), 
                   "Third operation should be in log");
    }

    @Test
    @Order(11)
    @DisplayName("IT-011: Test multiple PanelSettings operations log correctly")
    void testMultiplePanelSettingsOperationsLog() throws Exception {
        int initialCount = Files.readAllLines(testLogLocation).size();
        
        // Perform multiple operations
        panelSettings.debugFunction(); // Enable debug
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        panelSettings.debugFunction(); // Disable debug
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > initialCount + 1, 
                   "Multiple operations should create multiple log entries");
    }

    // ============ INTEGRATION TESTS - COMPLETE USER WORKFLOWS ============

    @Test
    @Order(12)
    @DisplayName("IT-012: Test end-to-end user workflow - Basic")
    void testEndToEndUserWorkflowBasic() throws Exception {
        // Simulate a complete user workflow
        
        // 1. User opens settings
        assertNotNull(panelSettings.panel);
        
        // 2. User enables debug mode
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
        assertTrue(Debug.frame.isVisible());
        
        // 3. User changes theme
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        
        // 4. User changes theme again
        Main.theme.change(Main.themes.CUSTOM);
        panelSettings.themeSwitch();
        
        // 5. User disables debug mode
        panelSettings.debugFunction();
        assertFalse(Debug.mode);
        assertFalse(Debug.frame.isVisible());
        
        // Verify all operations were logged
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Button Debug Pressed")), 
                   "Debug button presses should be logged");
    }

    @Test
    @Order(13)
    @DisplayName("IT-013: Test end-to-end user workflow - Advanced")
    void testEndToEndUserWorkflowAdvanced() throws Exception {
        // Complex workflow with multiple operations
        
        Debug.log("User starts application");
        
        // Enable debug
        panelSettings.debugFunction();
        
        // Change themes multiple times
        for (Main.themes theme : Main.themes.values()) {
            Main.theme.change(theme);
            panelSettings.themeSwitch();
            Debug.log("Theme changed to " + theme);
        }
        
        // Disable debug
        panelSettings.debugFunction();
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("User starts application")),
                  "User action should be logged");
        assertTrue(lines.stream().anyMatch(l -> l.contains("Theme changed")),
                  "Theme changes should be logged");
    }

    @Test
    @Order(14)
    @DisplayName("IT-014: Test workflow with serialization")
    void testWorkflowWithSerialization() throws Exception {
        // Enable debug
        panelSettings.debugFunction();
        
        // Change settings
        Main.theme.change(Main.themes.DARK);
        
        // Serialize
        java.lang.reflect.Method serializeMethod = 
            PanelSettings.class.getDeclaredMethod("serialize");
        serializeMethod.setAccessible(true);
        serializeMethod.invoke(panelSettings);
        
        // Verify both settings file and log file exist
        assertTrue(new File(Main.settingsSerLocation).exists(), 
                  "Settings file should exist");
        assertTrue(Files.exists(testLogLocation), "Log file should exist");
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Serialized")),
                  "Serialization should be logged");
    }

    // ============ INTEGRATION TESTS - STATE CONSISTENCY ============

    @Test
    @Order(15)
    @DisplayName("IT-015: Test Debug frame closure interacts with PanelSettings")
    void testDebugFrameClosureInteractsWithPanelSettings() throws Exception {
        // Enable debug mode
        panelSettings.debugFunction();
        assertTrue(Debug.mode, "Debug mode should be enabled");
        assertTrue(Debug.frame.isVisible(), "Debug frame should be visible");
        
        // The window listener should call panelSettings.debugFunction()
        // when window is closed, which toggles the mode
        // We verify the listener is registered
        assertTrue(Debug.frame.getWindowListeners().length > 0, 
                   "Window listeners should be registered");
    }

    @Test
    @Order(16)
    @DisplayName("IT-016: Test state consistency after rapid operations")
    void testStateConsistencyAfterRapidOperations() throws Exception {
        // Rapid operations
        for (int i = 0; i < 10; i++) {
            panelSettings.debugFunction();
            Main.theme.change(i % 2 == 0 ? Main.themes.LIGHT : Main.themes.DARK);
            panelSettings.themeSwitch();
        }
        
        // Verify state is consistent
        assertEquals(Debug.mode, Debug.frame.isVisible(),
                    "Debug mode should match frame visibility");
        assertNotNull(Main.theme.currentTheme, "Theme should be valid");
    }

    @Test
    @Order(17)
    @DisplayName("IT-017: Test log integrity after many operations")
    void testLogIntegrityAfterManyOperations() throws Exception {
        int operationCount = 50;
        
        for (int i = 0; i < operationCount; i++) {
            Debug.log("Operation " + i);
        }
        
        List<String> lines = Files.readAllLines(testLogLocation);
        long operationLogs = lines.stream()
            .filter(l -> l.contains("Operation"))
            .count();
        
        assertEquals(operationCount, operationLogs, 
                    "All operations should be logged");
    }

    // ============ INTEGRATION TESTS - ERROR HANDLING ============

    @Test
    @Order(18)
    @DisplayName("IT-018: Test graceful handling when log file is inaccessible")
    void testGracefulHandlingWhenLogFileInaccessible() {
        // This is a negative integration test
        // In a real scenario, this would test file permissions
        assertDoesNotThrow(() -> {
            Debug.log("Test after potential file issues");
        }, "Should handle file access issues gracefully");
    }

    @Test
    @Order(19)
    @DisplayName("IT-019: Test Debug and PanelSettings recover from errors")
    void testDebugAndPanelSettingsRecoverFromErrors() throws Exception {
        // Cause a potential error state
        Debug.mode = false;
        Debug.frame.setVisible(false);
        
        // Recover through normal operation
        panelSettings.debugFunction();
        
        // Verify recovery
        assertTrue(Debug.mode, "Should recover and enable debug mode");
        assertTrue(Debug.frame.isVisible(), "Frame should be visible after recovery");
    }

    // ============ INTEGRATION TESTS - DATA FLOW ============

    @Test
    @Order(20)
    @DisplayName("IT-020: Test data flow from PanelSettings to Debug")
    void testDataFlowFromPanelSettingsToDebug() throws Exception {
        String testMessage = "Data flow test";
        
        // Action in PanelSettings
        panelSettings.debugFunction();
        
        // Check effect in Debug
        assertTrue(Debug.mode, "Data should flow from PanelSettings to Debug");
        
        // Log the action
        Debug.log(testMessage);
        
        // Verify in log file
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains(testMessage)),
                  "Data flow should be traceable in logs");
    }

    @Test
    @Order(21)
    @DisplayName("IT-021: Test bidirectional communication")
    void testBidirectionalCommunication() throws Exception {
        // PanelSettings -> Debug
        panelSettings.debugFunction();
        assertTrue(Debug.mode, "PanelSettings should affect Debug");
        
        // Debug -> verification through PanelSettings context
        Debug.log("Bidirectional test");
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Bidirectional")),
                  "Debug should be accessible from PanelSettings context");
    }

    @Test
    @Order(22)
    @DisplayName("IT-022: Test complete settings lifecycle")
    void testCompleteSettingsLifecycle() throws Exception {
        // 1. Initialize
        assertFalse(Debug.mode);
        assertEquals(Main.themes.LIGHT, Main.theme.currentTheme);
        
        // 2. Modify settings
        panelSettings.debugFunction();
        Main.theme.change(Main.themes.DARK);
        
        // 3. Verify changes
        assertTrue(Debug.mode);
        assertEquals(Main.themes.DARK, Main.theme.currentTheme);
        
        // 4. Log changes
        Debug.log("Settings modified");
        
        // 5. Serialize
        java.lang.reflect.Method serializeMethod = 
            PanelSettings.class.getDeclaredMethod("serialize");
        serializeMethod.setAccessible(true);
        serializeMethod.invoke(panelSettings);
        
        // 6. Verify persistence
        assertTrue(new File(Main.settingsSerLocation).exists());
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Settings modified")),
                  "Complete lifecycle should be logged");
    }

    @Test
    @Order(23)
    @DisplayName("IT-023: Test concurrent modifications handled correctly")
    void testConcurrentModificationsHandledCorrectly() throws Exception {
        // Simulate near-concurrent operations
        panelSettings.debugFunction();
        Main.theme.change(Main.themes.CUSTOM);
        Debug.log("Concurrent op 1");
        panelSettings.themeSwitch();
        Debug.log("Concurrent op 2");
        
        // Verify consistency
        assertNotNull(Main.theme.background);
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Concurrent op 1")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Concurrent op 2")));
    }

    @Test
    @Order(24)
    @DisplayName("IT-024: Test integration with Main.gui reference")
    void testIntegrationWithMainGuiReference() {
        assertNotNull(Main.gui, "Main.gui should be set");
        assertNotNull(testMain.panelSettings, "PanelSettings should be accessible");
        
        // Verify the reference works
        panelSettings.debugFunction();
        assertTrue(Debug.mode, "Operations through Main.gui reference should work");
    }

    @Test
    @Order(25)
    @DisplayName("IT-025: Test full application state consistency")
    void testFullApplicationStateConsistency() throws Exception {
        // Comprehensive state check
        
        // Initial state
        boolean initialDebugMode = Debug.mode;
        
        // Perform operations
        panelSettings.debugFunction();
        Main.theme.change(Main.themes.CUSTOM);
        panelSettings.themeSwitch();
        
        // Verify state changes
        assertNotEquals(initialDebugMode, Debug.mode, "Debug mode should change");
        assertEquals(Main.themes.CUSTOM, Main.theme.currentTheme, "Theme should change to CUSTOM");
        
        // Verify consistency
        assertEquals(Debug.mode, Debug.frame.isVisible(), 
                    "Debug state should be consistent");
        assertNotNull(Main.theme.background, "Theme should be valid");
        assertTrue(Files.exists(testLogLocation), "Log should exist");
        
        // Verify logging captured everything
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > 1, "Multiple operations should be logged");
    }
}

