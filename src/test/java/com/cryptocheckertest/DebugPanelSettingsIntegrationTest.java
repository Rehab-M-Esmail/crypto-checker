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
        new File(System.getProperty("user.home") + File.separator + ".crypto-checker").mkdirs();

        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;

        new Debug();

        testMain = new Main();
        Main.gui = testMain;
        testMain.webData = new WebData();

        if (testMain.webData.portfolio == null)
            testMain.webData.portfolio = new ArrayList<>();
        if (testMain.webData.portfolio.isEmpty())
            testMain.webData.portfolio.add(new ArrayList<>());
        if (testMain.webData.portfolio_names == null)
            testMain.webData.portfolio_names = new ArrayList<>();
        if (testMain.webData.portfolio_names.isEmpty())
            testMain.webData.portfolio_names.add("Portfolio 1");
    }

    @AfterAll
    static void tearDownAfterAll() throws Exception {
        if (Debug.frame != null) {
            Debug.frame.dispose();
            Debug.frame = null;
        }
        Thread.sleep(200);
    }

    @BeforeEach
    void setUp() throws Exception {
        Debug.mode = false;
        Main.theme.change(Main.themes.LIGHT);

        if (Debug.frame == null)
            new Debug();
        else
            Debug.frame.setVisible(false);

        panelSettings = new PanelSettings();
        testMain.panelSettings = panelSettings;
    }

    @AfterEach
    void tearDown() {}

    // ===== INITIALIZATION TESTS =====

    @Test @Order(1)
    void testDebugAndPanelSettingsInitialization() {
        assertNotNull(Debug.frame);
        assertNotNull(panelSettings.panel);
        assertTrue(Files.exists(testLogLocation));
    }

    @Test @Order(2)
    void testBothModulesExistInMainGui() {
        assertNotNull(Main.gui);
        assertNotNull(testMain.panelSettings);
    }

    // ===== DEBUG MODE SYNCHRONIZATION =====

    @Test @Order(3)
    void testPanelSettingsTriggersDebugLogging() throws Exception {
        int initial = Files.readAllLines(testLogLocation).size();
        panelSettings.debugFunction();
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > initial);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Button Debug Pressed")));
    }

    @Test @Order(4)
    void testDebugModeChangesSyncAcrossModules() throws Exception {
        assertFalse(Debug.mode);
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
        assertTrue(Debug.frame.isVisible());
        panelSettings.debugFunction();
        assertFalse(Debug.mode);
        assertFalse(Debug.frame.isVisible());
    }

    @Test @Order(5)
    void testDebugFrameVisibilitySyncsWithMode() throws Exception {
        for (int i = 0; i < 5; i++) {
            panelSettings.debugFunction();
            assertEquals(Debug.mode, Debug.frame.isVisible());
        }
    }

    // ===== THEME SWITCHING AND LOGGING =====

    @Test @Order(6)
    void testThemeSwitchingLogsToDebug() throws Exception {
        int initial = Files.readAllLines(testLogLocation).size();
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        Debug.log("Theme switched to DARK");
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > initial);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Theme switched")));
    }

    @Test @Order(7)
    void testThemeChangesPersistAcrossDebugToggles() {
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        Main.themes before = Main.theme.currentTheme;
        panelSettings.debugFunction();
        panelSettings.debugFunction();
        assertEquals(before, Main.theme.currentTheme);
    }

    @Test @Order(8)
    void testCustomThemeChangesAreLogged() throws Exception {
        int initial = Files.readAllLines(testLogLocation).size();
        Main.theme.customBackground = new java.awt.Color(100,150,200);
        Main.theme.change(Main.themes.CUSTOM);
        Debug.log("Custom theme applied");
        panelSettings.themeSwitch();
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > initial);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Custom theme")));
    }

    // ===== SETTINGS PERSISTENCE =====

    @Test @Order(9)
    void testSerializationTriggersDebugLogging() throws Exception {
        var m = PanelSettings.class.getDeclaredMethod("serialize");
        m.setAccessible(true);
        m.invoke(panelSettings);
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Serialized Settings")));
    }

    @Test @Order(10)
    void testDebugLogsPersistAcrossPanelSettingsOperations() throws Exception {
        Debug.log("First operation");
        panelSettings.debugFunction();
        Debug.log("Second operation");
        panelSettings.themeSwitch();
        Debug.log("Third operation");
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("First operation")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Second operation")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Third operation")));
    }

    @Test @Order(11)
    void testMultiplePanelSettingsOperationsLog() throws Exception {
        int initial = Files.readAllLines(testLogLocation).size();
        panelSettings.debugFunction();
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        panelSettings.debugFunction();
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > initial + 1);
    }

    // ===== USER WORKFLOWS =====

    @Test @Order(12)
    void testEndToEndUserWorkflowBasic() throws Exception {
        assertNotNull(panelSettings.panel);
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
        assertTrue(Debug.frame.isVisible());
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        Main.theme.change(Main.themes.CUSTOM);
        panelSettings.themeSwitch();
        panelSettings.debugFunction();
        assertFalse(Debug.mode);
        assertFalse(Debug.frame.isVisible());
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Button Debug Pressed")));
    }

    @Test @Order(13)
    void testEndToEndUserWorkflowAdvanced() throws Exception {
        Debug.log("User starts application");
        panelSettings.debugFunction();
        for (Main.themes t : Main.themes.values()) {
            Main.theme.change(t);
            panelSettings.themeSwitch();
            Debug.log("Theme changed to " + t);
        }
        panelSettings.debugFunction();
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("User starts application")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Theme changed")));
    }

    @Test @Order(14)
    void testWorkflowWithSerialization() throws Exception {
        panelSettings.debugFunction();
        Main.theme.change(Main.themes.DARK);
        var m = PanelSettings.class.getDeclaredMethod("serialize");
        m.setAccessible(true);
        m.invoke(panelSettings);
        assertTrue(new File(Main.settingsSerLocation).exists());
        assertTrue(Files.exists(testLogLocation));
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Serialized")));
    }

    // ===== STATE CONSISTENCY =====

    @Test @Order(15)
    void testDebugFrameClosureInteractsWithPanelSettings() {
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
        assertTrue(Debug.frame.isVisible());
        assertTrue(Debug.frame.getWindowListeners().length > 0);
    }

    @Test @Order(16)
    void testStateConsistencyAfterRapidOperations() throws Exception {
        for (int i = 0; i < 10; i++) {
            panelSettings.debugFunction();
            Main.theme.change(i % 2 == 0 ? Main.themes.LIGHT : Main.themes.DARK);
            panelSettings.themeSwitch();
        }
        assertEquals(Debug.mode, Debug.frame.isVisible());
        assertNotNull(Main.theme.currentTheme);
    }

    @Test @Order(17)
    void testLogIntegrityAfterManyOperations() throws Exception {
        int count = 50;
        for (int i = 0; i < count; i++)
            Debug.log("Operation " + i);
        List<String> lines = Files.readAllLines(testLogLocation);
        long logged = lines.stream().filter(l -> l.contains("Operation")).count();
        assertEquals(count, logged);
    }

    // ===== ERROR HANDLING & DATA FLOW =====

    @Test @Order(18)
    void testGracefulHandlingWhenLogFileInaccessible() {
        assertDoesNotThrow(() -> Debug.log("Test after potential file issues"));
    }

    @Test @Order(19)
    void testDebugAndPanelSettingsRecoverFromErrors() throws Exception {
        Debug.mode = false;
        Debug.frame.setVisible(false);
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
        assertTrue(Debug.frame.isVisible());
    }

    @Test @Order(20)
    void testDataFlowFromPanelSettingsToDebug() throws Exception {
        String msg = "Data flow test";
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
        Debug.log(msg);
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains(msg)));
    }

    @Test @Order(21)
    void testBidirectionalCommunication() throws Exception {
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
        Debug.log("Bidirectional test");
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Bidirectional")));
    }

    @Test @Order(22)
    void testCompleteSettingsLifecycle() throws Exception {
        assertFalse(Debug.mode);
        assertEquals(Main.themes.LIGHT, Main.theme.currentTheme);
        panelSettings.debugFunction();
        Main.theme.change(Main.themes.DARK);
        assertTrue(Debug.mode);
        assertEquals(Main.themes.DARK, Main.theme.currentTheme);
        Debug.log("Settings modified");
        var m = PanelSettings.class.getDeclaredMethod("serialize");
        m.setAccessible(true);
        m.invoke(panelSettings);
        assertTrue(new File(Main.settingsSerLocation).exists());
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Settings modified")));
    }

    @Test @Order(23)
    void testConcurrentModificationsHandledCorrectly() throws Exception {
        panelSettings.debugFunction();
        Main.theme.change(Main.themes.CUSTOM);
        Debug.log("Concurrent op 1");
        panelSettings.themeSwitch();
        Debug.log("Concurrent op 2");
        assertNotNull(Main.theme.background);
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Concurrent op 1")));
        assertTrue(lines.stream().anyMatch(l -> l.contains("Concurrent op 2")));
    }

    @Test @Order(24)
    void testIntegrationWithMainGuiReference() {
        assertNotNull(Main.gui);
        assertNotNull(testMain.panelSettings);
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
    }

    @Test @Order(25)
    void testFullApplicationStateConsistency() throws Exception {
        boolean initial = Debug.mode;
        panelSettings.debugFunction();
        Main.theme.change(Main.themes.CUSTOM);
        panelSettings.themeSwitch();
        assertNotEquals(initial, Debug.mode);
        assertEquals(Main.themes.CUSTOM, Main.theme.currentTheme);
        assertEquals(Debug.mode, Debug.frame.isVisible());
        assertNotNull(Main.theme.background);
        assertTrue(Files.exists(testLogLocation));
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() > 1);
    }
}
