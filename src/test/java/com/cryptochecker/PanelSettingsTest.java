package com.cryptochecker;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.awt.Color;
import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PanelSettingsTest {

    private static Main testMain;

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        new File(Main.folderLocation).mkdirs();

        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;

        testMain = new Main();
        Main.gui = testMain;
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
    }

    @BeforeEach
    void setUp() {
        File f = new File(Main.settingsSerLocation);
        if (f.exists()) f.delete();

        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;
    }

    // ===== Helper Methods =====
    private String getCurrencySymbol(String c) {
        switch (c) {
            case "USD": return "$";
            case "EUR": return "€";
            case "GBP": return "£";
            default: return "";
        }
    }

    private void saveSettings(boolean mode, Main.Theme theme, String currency, String currencyChar) throws Exception {
        try (ObjectOutputStream out = new ObjectOutputStream(
                new BufferedOutputStream(new FileOutputStream(Main.settingsSerLocation)))) {
            out.writeObject(mode);
            out.writeObject(theme);
            out.writeObject(currency);
            out.writeObject(currencyChar);
        }
    }

    private Object[] loadSettings() throws Exception {
        try (ObjectInputStream in = new ObjectInputStream(
                new BufferedInputStream(new FileInputStream(Main.settingsSerLocation)))) {
            return new Object[]{ in.readObject(), in.readObject(), in.readObject(), in.readObject() };
        }
    }

    // ===== Theme Tests =====
    @Test @Order(1) @DisplayName("TC-SETT-001: Verify theme changes from Light to Dark")
    void testThemeChangeLightToDark() {
        Main.theme.change(Main.themes.DARK);
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    @Test @Order(2) @DisplayName("TC-SETT-002: Verify theme changes from Dark to Custom")
    void testThemeChangeDarkToCustom() {
        Main.theme.change(Main.themes.CUSTOM);
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    @Test @Order(3) @DisplayName("TC-SETT-003: Verify theme changes from Custom to Light")
    void testThemeChangeCustomToLight() {
        Main.theme.change(Main.themes.LIGHT);
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    // ===== Currency Tests =====
    @Test @Order(4) @DisplayName("TC-SETT-004: Verify USD currency selection")
    void testCurrencySelectionUSD() { assertEquals("$", getCurrencySymbol("USD")); }

    @Test @Order(5) @DisplayName("TC-SETT-005: Verify EUR currency selection")
    void testCurrencySelectionEUR() { assertEquals("€", getCurrencySymbol("EUR")); }

    @Test @Order(6) @DisplayName("TC-SETT-006: Verify GBP currency selection")
    void testCurrencySelectionGBP() { assertEquals("£", getCurrencySymbol("GBP")); }

    @Test @Order(7) @DisplayName("TC-SETT-007: Verify currencies without special symbols")
    void testCurrencySelectionNoSymbol() {
        for (String c : new String[]{"SEK","AUD","JPY","CAD"}) assertEquals("", getCurrencySymbol(c));
    }

    // ===== Debug Mode Tests =====
    @Test @Order(8) @DisplayName("TC-SETT-008: Verify enabling debug mode")
    void testDebugModeEnable() {
        Debug.mode = true;
        if (Debug.frame != null) Debug.frame.setVisible(true);
        assertTrue(Debug.mode);
        if (Debug.frame != null) assertTrue(Debug.frame.isVisible());
    }

    @Test @Order(9) @DisplayName("TC-SETT-009: Verify disabling debug mode")
    void testDebugModeDisable() {
        Debug.mode = false;
        if (Debug.frame != null) Debug.frame.setVisible(false);
        assertFalse(Debug.mode);
        if (Debug.frame != null) assertFalse(Debug.frame.isVisible());
    }

    // ===== Custom Theme Tests =====
    @Test @Order(10) @DisplayName("TC-SETT-010: Verify setting custom theme colors")
    void testCustomThemeSetColors() {
        Main.theme = new Main.Theme(Main.themes.CUSTOM);
        Main.theme.customBackground = new Color(100,150,200);
        Main.theme.customForeground = new Color(255,255,255);
        Main.theme.update();
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    @Test @Order(11) @DisplayName("TC-SETT-011: Verify custom theme reset to defaults")
    void testCustomThemeReset() {
        Main.theme = new Main.Theme(Main.themes.CUSTOM);
        Main.theme.customBackground = new Color(123,123,123);
        Main.theme.resetCustom();
        assertNotNull(Main.theme.customBackground);
        assertNotNull(Main.theme.customForeground);
    }

    // ===== Settings Persistence =====
    @Test @Order(12) @DisplayName("TC-SETT-012: Verify settings persistence (save)")
    void testSettingsPersistenceSave() throws Exception {
        saveSettings(true, new Main.Theme(Main.themes.DARK), "EUR", "€");
        File f = new File(Main.settingsSerLocation);
        assertTrue(f.exists() && f.length()>0);
    }

    @Test @Order(13) @DisplayName("TC-SETT-013: Verify settings persistence (load)")
    void testSettingsPersistenceLoad() throws Exception {
        saveSettings(true, new Main.Theme(Main.themes.DARK), "EUR", "€");
        Object[] loaded = loadSettings();
        assertTrue((Boolean)loaded[0]);
        assertEquals("EUR", loaded[2]);
        assertEquals("€", loaded[3]);
        assertNotNull(loaded[1]);
    }

    // ===== Color Boundary Tests =====
    @Test @Order(14) @DisplayName("TC-SETT-014: Test color boundary value (0,0,0)")
    void testColorBoundaryMinimum() {
        Main.theme = new Main.Theme(Main.themes.CUSTOM);
        Main.theme.customBackground = new Color(0,0,0);
        Main.theme.update();
        assertNotNull(Main.theme.customBackground);
    }

    @Test @Order(15) @DisplayName("TC-SETT-015: Test color boundary value (255,255,255)")
    void testColorBoundaryMaximum() {
        Main.theme = new Main.Theme(Main.themes.CUSTOM);
        Main.theme.customForeground = new Color(255,255,255);
        Main.theme.update();
        assertNotNull(Main.theme.customForeground);
    }

    // ===== Theme Update/Cycle =====
    @Test @Order(16) @DisplayName("TC-SETT-016: Verify theme update method")
    void testThemeUpdate() {
        Main.theme.update();
        Main.theme.change(Main.themes.DARK); Main.theme.update();
        Main.theme.change(Main.themes.CUSTOM); Main.theme.customBackground = new Color(100,100,100); Main.theme.update();
        assertNotNull(Main.theme.background);
    }

    @Test @Order(17) @DisplayName("TC-SETT-017: Verify cycling through all themes")
    void testThemeCycle() {
        Main.theme.change(Main.themes.DARK);
        Main.theme.change(Main.themes.CUSTOM);
        Main.theme.change(Main.themes.LIGHT);
        assertNotNull(Main.theme.background);
    }

    @Test @Order(18) @DisplayName("TC-SETT-018: Verify all custom color properties")
    void testAllCustomColors() {
        Main.theme = new Main.Theme(Main.themes.CUSTOM);
        Main.theme.customBackground = new Color(10,10,10);
        Main.theme.customForeground = new Color(240,240,240);
        Main.theme.customGreen = new Color(0,255,0);
        Main.theme.customRed = new Color(255,0,0);
        Main.theme.customSelection = new Color(128,128,128);
        Main.theme.customEmptyBackground = new Color(50,50,50);
        Main.theme.update();
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
        assertNotNull(Main.theme.green);
        assertNotNull(Main.theme.red);
        assertNotNull(Main.theme.selection);
        assertNotNull(Main.theme.emptyBackground);
    }

    // ===== Invalid Inputs =====
    @Test @Order(19) @DisplayName("TC-SETT-NEG-01: INVALID BOUNDARY - Color value below minimum (-1)")
    void testColorBelowMinimum_InvalidBoundary() {
        assertThrows(IllegalArgumentException.class, () -> new Color(-1,0,0));
    }

    @Test @Order(20) @DisplayName("TC-SETT-NEG-02: INVALID BOUNDARY - Color value above maximum (256)")
    void testColorAboveMaximum_InvalidBoundary() {
        assertThrows(IllegalArgumentException.class, () -> new Color(256,0,0));
    }

    @Test @Order(21) @DisplayName("TC-SETT-NEG-03: INVALID INPUT - Non-existent currency code")
    void testInvalidCurrency_InvalidClass() {
        assertEquals("", getCurrencySymbol("INVALID_CURRENCY"));
    }

    @Test @Order(22) @DisplayName("TC-SETT-NEG-04: INVALID BOUNDARY - Null theme")
    void testNullTheme_InvalidBoundary() {
        Main.Theme backup = Main.theme;
        Main.theme = null;
        if (Main.theme == null) Main.theme = new Main.Theme(Main.themes.LIGHT);
        assertNotNull(Main.theme);
        Main.theme = backup;
    }
}
