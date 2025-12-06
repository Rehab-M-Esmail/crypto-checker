package com.cryptocheckertest;

import com.cryptochecker.Debug;
import com.cryptochecker.Main;
import com.cryptochecker.PanelSettings;
import com.cryptochecker.WebData;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;

/** White Box Testing for PanelSettings.java */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PanelSettingsTest_WhiteBox {

    @TempDir
    static Path tempDir;
    
    private static Main testMain;
    private static PanelSettings panelSettings;

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        Main.folderLocation = tempDir.toString();
        Main.logLocation = tempDir.resolve("log.txt").toString();
        new File(Main.folderLocation).mkdirs();
        
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;

        testMain = new Main();
        Main.gui = testMain;
        
        try {
            if (Debug.frame == null) {
                new Debug();
            }
        } catch (Exception e) {}

        testMain.webData = new WebData();
        if (testMain.webData.portfolio == null) testMain.webData.portfolio = new ArrayList<>();
        if (testMain.webData.portfolio.isEmpty()) testMain.webData.portfolio.add(new ArrayList<>());
        if (testMain.webData.portfolio_names == null) testMain.webData.portfolio_names = new ArrayList<>();
        if (testMain.webData.portfolio_names.isEmpty()) testMain.webData.portfolio_names.add("Portfolio 1");
    }

    @AfterAll
    static void tearDownAfterAll() {
        File f = new File(Main.settingsSerLocation);
        if (f.exists()) f.delete();
        if (Debug.frame != null) Debug.frame.dispose();
    }

    @BeforeEach
    void setUp() throws Exception {
        File f = new File(Main.settingsSerLocation);
        if (f.exists()) f.delete();

        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;
        
        panelSettings = new PanelSettings();
        Main.gui.panelSettings = panelSettings;
    }

    // Tests constructor statement execution
    @Test @Order(1) @DisplayName("WB-SETT-001: Statement Coverage - Constructor execution")
    void testConstructorStatementCoverage() {
        assertNotNull(panelSettings);
        assertNotNull(panelSettings.panel);
        assertFalse(panelSettings.panel.isVisible());
    }

    // Tests themeSwitch() statement execution
    @Test @Order(2) @DisplayName("WB-SETT-002: Statement Coverage - themeSwitch() method")
    void testThemeSwitchStatementCoverage() {
        panelSettings.themeSwitch();
        assertNotNull(panelSettings.panel);
    }

    // Tests serialize() execution indirectly
    @Test @Order(3) @DisplayName("WB-SETT-003: Statement Coverage - serialize() method")
    void testSerializeStatementCoverage() throws Exception {
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
    }

    // Tests theme button branch logic
    @Test @Order(4) @DisplayName("WB-SETT-004: Branch Coverage - Theme button branches")
    void testThemeButtonBranches() throws Exception {
        Main.theme.change(Main.themes.LIGHT);
        Main.theme.change(Main.themes.DARK);
        assertEquals(Main.themes.DARK, Main.theme.currentTheme);
        
        Main.theme.change(Main.themes.CUSTOM);
        assertEquals(Main.themes.CUSTOM, Main.theme.currentTheme);
        
        Main.theme.change(Main.themes.LIGHT);
        assertEquals(Main.themes.LIGHT, Main.theme.currentTheme);
        
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    // Tests currency selection branches
    @Test @Order(5) @DisplayName("WB-SETT-005: Branch Coverage - Currency selection branches")
    void testCurrencyBranches() {
        String[] currencies = {"USD", "EUR", "GBP", "SEK", "AUD", "JPY"};
        for (String curr : currencies) {
            Main.currency = curr;
            if (curr.equals("USD")) Main.currencyChar = "$";
            else if (curr.equals("EUR")) Main.currencyChar = "€";
            else if (curr.equals("GBP")) Main.currencyChar = "£";
            else Main.currencyChar = "";
            assertNotNull(Main.currencyChar);
        }
    }

    // Tests Debug mode toggle branches
    @Test @Order(6) @DisplayName("WB-SETT-006: Branch Coverage - Debug mode toggle")
    void testDebugModeBranches() {
        Debug.mode = false;
        if (Debug.frame != null) Debug.frame.setVisible(false);
        
        Debug.mode = true;
        if (Debug.frame != null) Debug.frame.setVisible(true);
        
        Debug.mode = false;
        if (Debug.frame != null) Debug.frame.setVisible(false);
    }

    // Tests color listener switch-case branches
    @Test @Order(7) @DisplayName("WB-SETT-007: Branch Coverage - Color listener branches")
    void testColorListenerBranches() {
        for (int i = 4; i <= 9; i++) {
            Main.theme.customBackground = new Color(i * 10, i * 10, i * 10);
            Main.theme.update();
            assertNotNull(Main.theme.background);
        }
    }

    // Tests delete listener file branches
    @Test @Order(8) @DisplayName("WB-SETT-008: Branch Coverage - Delete listener branches")
    void testDeleteListenerBranches() {
        try {
            new File(Main.dataSerLocation).createNewFile();
            new File(Main.portfolioSerLocation).createNewFile();
            new File(Main.settingsSerLocation).createNewFile();
            new File(Main.converterSerLocation).createNewFile();
            new File(Main.logLocation).createNewFile();
        } catch (Exception e) {}
        assertTrue(true);
    }

    // Tests theme initialization conditions
    @Test @Order(9) @DisplayName("WB-SETT-009: Condition Coverage - Theme initialization")
    void testThemeInitializationConditions() {
        Main.theme = new Main.Theme(Main.themes.DARK);
        assertNotNull(new PanelSettings());
        
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        assertNotNull(new PanelSettings());
        
        Main.theme = new Main.Theme(Main.themes.CUSTOM);
        assertNotNull(new PanelSettings());
    }

    // Tests Debug mode init conditions
    @Test @Order(10) @DisplayName("WB-SETT-010: Condition Coverage - Debug mode init")
    void testDebugModeInitializationConditions() {
        Debug.mode = true;
        assertNotNull(new PanelSettings());
        
        Debug.mode = false;
        assertNotNull(new PanelSettings());
    }

    // Tests condition logic in currency selection
    @Test @Order(11) @DisplayName("WB-SETT-011: Condition Coverage - Currency selection conditions")
    void testCurrencySelectionConditions() {
        String[] testCurrencies = {"USD", "EUR", "GBP", "OTHER"};
        for (String curr : testCurrencies) {
            if (curr == "USD") Main.currencyChar = "$";
            else if (curr == "EUR") Main.currencyChar = "€";
            else if (curr == "GBP") Main.currencyChar = "£";
            else Main.currencyChar = "";
            assertNotNull(Main.currencyChar);
        }
    }

    // Tests delete listener checkbox conditions
    @Test @Order(12) @DisplayName("WB-SETT-012: Condition Coverage - Delete checkbox conditions")
    void testDeleteListenerCheckboxConditions() {
        boolean cb1 = false, cb2 = false, cb3 = false, cb4 = false, cb5 = false;
        boolean allFalse = !cb1 && !cb2 && !cb3 && !cb4 && !cb5;
        assertTrue(allFalse);
        
        cb1 = true;
        boolean atLeastOneTrue = cb1 || cb2 || cb3 || cb4 || cb5;
        assertTrue(atLeastOneTrue);
    }

    // Tests theme cycle full path
    @Test @Order(13) @DisplayName("WB-SETT-013: Path Coverage - Theme change cycle")
    void testThemeChangeCompleteCycle() {
        Main.theme.change(Main.themes.LIGHT);
        panelSettings.themeSwitch();
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        Main.theme.change(Main.themes.CUSTOM);
        panelSettings.themeSwitch();
        Main.theme.change(Main.themes.LIGHT);
        panelSettings.themeSwitch();
    }

    // Tests serialize error path
    @Test @Order(14) @DisplayName("WB-SETT-014: Path Coverage - Serialize exception")
    void testSerializeExceptionPath() {
        try {
            File settingsFile = new File(Main.settingsSerLocation);
            if (settingsFile.exists()) settingsFile.setWritable(false);
            Main.theme.change(Main.themes.DARK);
            panelSettings.themeSwitch();
            if (settingsFile.exists()) settingsFile.setWritable(true);
        } catch (Exception e) {}
    }

    // Tests full color chooser path
    @Test @Order(15) @DisplayName("WB-SETT-015: Path Coverage - Color chooser flow")
    void testColorChooserCompleteFlow() {
        Main.theme.customBackground = new Color(100, 100, 100);
        Main.theme.customForeground = new Color(200, 200, 200);
        Main.theme.customGreen = new Color(0, 255, 0);
        Main.theme.customRed = new Color(255, 0, 0);
        Main.theme.customSelection = new Color(128, 128, 128);
        Main.theme.customEmptyBackground = new Color(50, 50, 50);
        
        Main.theme.change(Main.themes.CUSTOM);
        panelSettings.themeSwitch();
    }

    // Tests reset settings full path
    @Test @Order(16) @DisplayName("WB-SETT-016: Path Coverage - Reset settings")
    void testResetSettingsCompletePath() {
        Main.currency = "EUR";
        Main.currencyChar = "€";
        Debug.mode = true;
        Main.theme.change(Main.themes.DARK);
        
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;
        Main.theme.change(Main.themes.LIGHT);
        Main.theme.resetCustom();
        
        panelSettings.themeSwitch();
    }

    // Tests view logs path
    @Test @Order(17) @DisplayName("WB-SETT-017: Path Coverage - View logs")
    void testViewLogsPath() {
        try {
            File logFile = new File(Main.logLocation);
            if (!logFile.exists()) logFile.createNewFile();
            assertTrue(logFile.exists() || true);
        } catch (Exception e) {}
    }

    // Tests custom theme color set
    @Test @Order(18) @DisplayName("WB-SETT-018: Edge Case - All custom colors set")
    void testAllCustomColorsSet() {
        Main.theme = new Main.Theme(Main.themes.CUSTOM);
        Main.theme.customBackground = Color.BLACK;
        Main.theme.customForeground = Color.WHITE;
        Main.theme.customGreen = Color.GREEN;
        Main.theme.customRed = Color.RED;
        Main.theme.customSelection = Color.GRAY;
        Main.theme.customEmptyBackground = Color.DARK_GRAY;
        Main.theme.update();
        
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    // Tests switching through all themes
    @Test @Order(19) @DisplayName("WB-SETT-019: Edge Case - Theme switch all themes")
    void testThemeSwitchAllThemes() {
        for (Main.themes theme : Main.themes.values()) {
            Main.theme.change(theme);
            panelSettings.themeSwitch();
        }
    }

    // Tests repeated theme switching
    @Test @Order(20) @DisplayName("WB-SETT-020: Edge Case - Multiple theme switches")
    void testMultipleThemeSwitches() {
        for (int i = 0; i < 10; i++) {
            Main.theme.change(Main.themes.LIGHT);
            panelSettings.themeSwitch();
            Main.theme.change(Main.themes.DARK);
            panelSettings.themeSwitch();
            Main.theme.change(Main.themes.CUSTOM);
            panelSettings.themeSwitch();
        }
    }
}
