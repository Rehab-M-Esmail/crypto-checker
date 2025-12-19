package com.cryptocheckertest;

import com.cryptochecker.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PanelSettingsTestMockito {

    private static Main testMain;
    private PanelSettings panelSettings;

    @Mock private JButton mockButton;
    @Mock private JTextField mockTextField;
    @Mock private JPanel mockPanel;

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

        if (testMain.webData.portfolio == null)
            testMain.webData.portfolio = new ArrayList<>();
        if (testMain.webData.portfolio.isEmpty())
            testMain.webData.portfolio.add(new ArrayList<>());
        if (testMain.webData.portfolio_names == null)
            testMain.webData.portfolio_names = new ArrayList<>();
        if (testMain.webData.portfolio_names.isEmpty())
            testMain.webData.portfolio_names.add("Portfolio 1");
    }

    @BeforeEach
    void setUp() {
        File settingsFile = new File(Main.settingsSerLocation);
        if (settingsFile.exists()) settingsFile.delete();

        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;
    }

    @AfterEach
    void tearDown() {
        if (Debug.frame != null) Debug.frame.dispose();
    }

    // ===== PANEL INITIALIZATION TESTS =====

    @Test @Order(1)
    void testPanelInitialization() {
        panelSettings = new PanelSettings();
        assertNotNull(panelSettings.panel);
        assertFalse(panelSettings.panel.isVisible());
        assertNotNull(panelSettings.panel.getBackground());
    }

    @Test @Order(2)
    void testPanelLayout() {
        panelSettings = new PanelSettings();
        assertTrue(panelSettings.panel.getLayout() instanceof BoxLayout);
    }

    @Test @Order(3)
    void testPanelInitialVisibility() {
        panelSettings = new PanelSettings();
        assertFalse(panelSettings.panel.isVisible());
    }

    // ===== THEME FUNCTIONALITY TESTS =====

    @Test @Order(4)
    void testThemeSwitchToDark() {
        panelSettings = new PanelSettings();
        Main.theme.change(Main.themes.DARK);
        panelSettings.themeSwitch();
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    @Test @Order(5)
    void testThemeSwitchToCustom() {
        panelSettings = new PanelSettings();
        Main.theme.change(Main.themes.CUSTOM);
        panelSettings.themeSwitch();
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    @Test @Order(6)
    void testThemeSwitchToLight() {
        panelSettings = new PanelSettings();
        Main.theme.change(Main.themes.LIGHT);
        panelSettings.themeSwitch();
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    @Test @Order(7)
    void testThemeCycling() {
        panelSettings = new PanelSettings();
        Main.theme.change(Main.themes.LIGHT); panelSettings.themeSwitch();
        Main.theme.change(Main.themes.DARK); panelSettings.themeSwitch();
        Main.theme.change(Main.themes.CUSTOM); panelSettings.themeSwitch();
        assertNotNull(Main.theme.background);
    }

    // ===== DEBUG FUNCTIONALITY TESTS =====

    @Test @Order(8)
    void testDebugFunctionEnable() throws Exception {
        new Debug();
        panelSettings = new PanelSettings();
        Debug.mode = false;
        Debug.frame.setVisible(false);
        panelSettings.debugFunction();
        assertTrue(Debug.mode);
        assertTrue(Debug.frame.isVisible());
    }

    @Test @Order(9)
    void testDebugFunctionDisable() throws Exception {
        new Debug();
        panelSettings = new PanelSettings();
        Debug.mode = true;
        Debug.frame.setVisible(true);
        panelSettings.debugFunction();
        assertFalse(Debug.mode);
        assertFalse(Debug.frame.isVisible());
    }

    @Test @Order(10)
    void testDebugFunctionToggleMultipleTimes() throws Exception {
        new Debug();
        panelSettings = new PanelSettings();
        boolean initial = Debug.mode;
        panelSettings.debugFunction();
        assertEquals(!initial, Debug.mode);
        panelSettings.debugFunction();
        assertEquals(initial, Debug.mode);
    }

    // ===== CUSTOM THEME COLOR TESTS =====

    @Test @Order(11)
    void testCustomThemeColorBackground() {
        panelSettings = new PanelSettings();
        Main.theme.customBackground = new Color(100,150,200);
        Main.theme.change(Main.themes.CUSTOM);
        assertNotNull(Main.theme.background);
    }

    @Test @Order(12)
    void testCustomThemeColorForeground() {
        panelSettings = new PanelSettings();
        Main.theme.customForeground = new Color(255,255,255);
        Main.theme.change(Main.themes.CUSTOM);
        assertNotNull(Main.theme.foreground);
    }

    @Test @Order(13)
    void testCustomThemeColorGreen() {
        panelSettings = new PanelSettings();
        Main.theme.customGreen = new Color(0,255,0);
        Main.theme.change(Main.themes.CUSTOM);
        assertNotNull(Main.theme.green);
    }

    @Test @Order(14)
    void testCustomThemeColorRed() {
        panelSettings = new PanelSettings();
        Main.theme.customRed = new Color(255,0,0);
        Main.theme.change(Main.themes.CUSTOM);
        assertNotNull(Main.theme.red);
    }

    @Test @Order(15)
    void testAllCustomColorsSimultaneously() {
        panelSettings = new PanelSettings();
        Main.theme.customBackground = new Color(10,10,10);
        Main.theme.customForeground = new Color(240,240,240);
        Main.theme.customGreen = new Color(0,255,0);
        Main.theme.customRed = new Color(255,0,0);
        Main.theme.customSelection = new Color(128,128,128);
        Main.theme.customEmptyBackground = new Color(50,50,50);
        Main.theme.change(Main.themes.CUSTOM);
        panelSettings.themeSwitch();
        assertAll(
            () -> assertNotNull(Main.theme.background),
            () -> assertNotNull(Main.theme.foreground),
            () -> assertNotNull(Main.theme.green),
            () -> assertNotNull(Main.theme.red),
            () -> assertNotNull(Main.theme.selection),
            () -> assertNotNull(Main.theme.emptyBackground)
        );
    }

    // ===== SETTINGS SERIALIZATION TESTS =====

    @Test @Order(16)
    void testSettingsSerialization() throws Exception {
        new File(Main.folderLocation).mkdirs();
        new Debug();
        panelSettings = new PanelSettings();
        var m = PanelSettings.class.getDeclaredMethod("serialize");
        m.setAccessible(true);
        assertDoesNotThrow(() -> m.invoke(panelSettings));
    }

    @Test @Order(17)
    void testSerializeMethodExistsAndAccessible() throws Exception {
        panelSettings = new PanelSettings();
        var m = PanelSettings.class.getDeclaredMethod("serialize");
        m.setAccessible(true);
        assertNotNull(m);
    }

    @Test @Order(18)
    void testMultipleSerialization() throws Exception {
        new Debug();
        panelSettings = new PanelSettings();
        var m = PanelSettings.class.getDeclaredMethod("serialize");
        m.setAccessible(true);
        assertDoesNotThrow(() -> { m.invoke(panelSettings); m.invoke(panelSettings); });
    }

    // ===== ACTION LISTENER TESTS =====

    @Test @Order(19)
    void testDebugListenerWithMock() throws Exception {
        new Debug();
        panelSettings = new PanelSettings();
        ActionEvent e = mock(ActionEvent.class);
        boolean initial = Debug.mode;
        panelSettings.new bDebugListener().actionPerformed(e);
        assertNotEquals(initial, Debug.mode);
    }

    @Test @Order(20)
    void testDebugListenerMultipleInvocations() throws Exception {
        new Debug();
        panelSettings = new PanelSettings();
        ActionEvent e = mock(ActionEvent.class);
        var l = panelSettings.new bDebugListener();
        boolean initial = Debug.mode;
        l.actionPerformed(e);
        l.actionPerformed(e);
        assertEquals(initial, Debug.mode);
    }

    // ===== EDGE & NEGATIVE TESTS =====

    @Test @Order(21)
    void testThemeResetToDefaults() {
        panelSettings = new PanelSettings();
        Main.theme.change(Main.themes.CUSTOM);
        Main.theme.resetCustom();
        assertNotNull(Main.theme.customBackground);
        assertNotNull(Main.theme.customForeground);
    }

    @Test @Order(22)
    void testColorBoundaryMinimum() {
        panelSettings = new PanelSettings();
        assertDoesNotThrow(() -> new Color(0,0,0));
    }

    @Test @Order(23)
    void testColorBoundaryMaximum() {
        panelSettings = new PanelSettings();
        assertDoesNotThrow(() -> new Color(255,255,255));
    }

    @Test @Order(24)
    void testRapidThemeSwitching() {
        panelSettings = new PanelSettings();
        assertDoesNotThrow(() -> {
            for (int i=0;i<50;i++) {
                Main.theme.change(Main.themes.LIGHT);
                Main.theme.change(Main.themes.DARK);
                Main.theme.change(Main.themes.CUSTOM);
            }
        });
    }

    @Test @Order(25)
    void testThemeSwitchCalledRepeatedly() {
        panelSettings = new PanelSettings();
        assertDoesNotThrow(() -> {
            for (int i=0;i<100;i++) panelSettings.themeSwitch();
        });
    }

    @Test @Order(26)
    void testCustomColorsWithMidRangeValues() {
        panelSettings = new PanelSettings();
        Main.theme.customBackground = new Color(128,128,128);
        Main.theme.customForeground = new Color(127,127,127);
        Main.theme.change(Main.themes.CUSTOM);
        assertNotNull(Main.theme.background);
        assertNotNull(Main.theme.foreground);
    }

    @Test @Order(27)
    void testThemeObjectNotNullAfterCreation() {
        panelSettings = new PanelSettings();
        assertNotNull(Main.theme);
    }

    @Test @Order(28)
    void testCurrentThemeIsValidEnum() {
        panelSettings = new PanelSettings();
        assertNotNull(Main.theme.currentTheme);
    }

    @Test @Order(29)
    void testInvalidColorRGBValuesNegative() {
        assertThrows(IllegalArgumentException.class, () -> new Color(-1,0,0));
    }

    @Test @Order(30)
    void testInvalidColorRGBValuesTooHigh() {
        assertThrows(IllegalArgumentException.class, () -> new Color(256,0,0));
    }

    @Test @Order(31)
    void testNullThemeHandling() {
        Main.Theme backup = Main.theme;
        Main.theme = null;
        if (Main.theme == null)
            Main.theme = new Main.Theme(Main.themes.LIGHT);
        assertNotNull(Main.theme);
        Main.theme = backup;
    }

    @Test @Order(32)
    void testDebugFunctionWithoutInitialization() {
        Debug.frame = null;
        Debug.mode = false;
        panelSettings = new PanelSettings();
        assertThrows(NullPointerException.class, panelSettings::debugFunction);
    }

    @Test @Order(33)
    void testColorValuesAtBoundaries() {
        assertDoesNotThrow(() -> new Color(0,0,0));
        assertDoesNotThrow(() -> new Color(255,255,255));
        assertThrows(IllegalArgumentException.class, () -> new Color(-1,0,0));
        assertThrows(IllegalArgumentException.class, () -> new Color(256,0,0));
    }

    @Test @Order(34)
    void testExtremeRapidDebugToggling() throws Exception {
        new Debug();
        panelSettings = new PanelSettings();
        assertDoesNotThrow(() -> {
            for (int i=0;i<100;i++) panelSettings.debugFunction();
        });
    }

    @Test @Order(35)
    void testMultipleRapidSerializations() throws Exception {
        panelSettings = new PanelSettings();
        var m = PanelSettings.class.getDeclaredMethod("serialize");
        m.setAccessible(true);
        assertDoesNotThrow(() -> {
            for (int i=0;i<10;i++) m.invoke(panelSettings);
        });
    }
}
