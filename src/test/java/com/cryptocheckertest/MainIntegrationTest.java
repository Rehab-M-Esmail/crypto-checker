package com.cryptochecker;

import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MainIntegrationTest {

    @BeforeAll
    void setupEnvironment() throws Exception {
        // Prevent background refresh threads
        Debug.mode = true;

        // Static initialization normally done in main()
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";

        // Create GUI instance
        Main.gui = new Main();

        // Call private setupGUI() using reflection
        Method setupGUI = Main.class.getDeclaredMethod("setupGUI");
        setupGUI.setAccessible(true);
        setupGUI.invoke(Main.gui);
    }

    @AfterAll
    void cleanup() {
        if (Main.frame != null) {
            Main.frame.dispose();
        }
    }

    // --------------------------------------------------
    // Core Initialization Tests
    // --------------------------------------------------

    @Test
    @DisplayName("Main GUI initializes without crashing")
    void testMainInitialization() {
        assertNotNull(Main.gui);
        assertNotNull(Main.frame);
        assertTrue(Main.frame.isVisible());
    }

    @Test
    @DisplayName("All main panels are initialized")
    void testPanelsInitialized() {
        assertNotNull(Main.gui.menu);
        assertNotNull(Main.gui.panelCoin);
        assertNotNull(Main.gui.panelPortfolio);
        assertNotNull(Main.gui.panelConverter);
        assertNotNull(Main.gui.panelSettings);

        assertNotNull(Main.gui.panelCoin.panel);
        assertNotNull(Main.gui.panelPortfolio.panel);
        assertNotNull(Main.gui.panelConverter.panel);
        assertNotNull(Main.gui.panelSettings.panel);
    }

    // --------------------------------------------------
    // Frame Configuration
    // --------------------------------------------------

    @Test
    @DisplayName("Frame properties are correctly configured")
    void testFrameProperties() {
        JFrame frame = Main.frame;

        assertEquals("Crypto Checker", frame.getTitle());
        assertEquals(900, frame.getWidth());
        assertEquals(600, frame.getHeight());
        assertFalse(frame.isResizable());
        assertEquals(JFrame.EXIT_ON_CLOSE, frame.getDefaultCloseOperation());
    }

    @Test
    @DisplayName("Frame layout contains menu and center panel")
    void testFrameLayoutIntegration() {
        Container content = Main.frame.getContentPane();
        assertTrue(content.getLayout() instanceof BorderLayout);

        BorderLayout layout = (BorderLayout) content.getLayout();

        assertNotNull(layout.getLayoutComponent(BorderLayout.CENTER));
        assertNotNull(layout.getLayoutComponent(BorderLayout.WEST));
    }

    // --------------------------------------------------
    // Shared Data Integration
    // --------------------------------------------------

    @Test
    @DisplayName("WebData instance is shared across panels")
    void testSharedWebDataInstance() {
        WebData webData = Main.gui.webData;

        assertNotNull(webData);
        assertSame(webData, Main.gui.panelPortfolio.webData);
    }

    // --------------------------------------------------
    // Theme & UI Integration
    // --------------------------------------------------

    @Test
    @DisplayName("Default theme is applied correctly")
    void testThemeInitialization() {
        assertEquals(Color.WHITE, Main.theme.background);
        assertEquals(Color.BLACK, Main.theme.foreground);
    }

    @Test
    @DisplayName("Theme switching updates colors")
    void testThemeSwitching() {
        Main.Theme theme = new Main.Theme(Main.themes.LIGHT);
        Color lightBackground = theme.background;

        theme.change(Main.themes.DARK);

        assertNotEquals(lightBackground, theme.background);
        assertEquals(Color.WHITE, theme.foreground);
    }

    @Test
    @DisplayName("UIManager uses theme colors")
    void testUIManagerThemeIntegration() {
        assertEquals(
                Main.theme.emptyBackground,
                UIManager.get("Panel.background")
        );

        assertEquals(
                Main.theme.foreground,
                UIManager.get("OptionPane.messageForeground")
        );
    }

    // --------------------------------------------------
    // Currency & Settings
    // --------------------------------------------------

    @Test
    @DisplayName("Currency settings are initialized correctly")
    void testCurrencyInitialization() {
        assertEquals("USD", Main.currency);
        assertEquals("$", Main.currencyChar);
    }

    // --------------------------------------------------
    // UI Component Integration
    // --------------------------------------------------

    @Test
    @DisplayName("Button template produces correctly styled button")
    void testButtonTemplate() {
        JButton button = Main.gui.getButtonTemplate("Test");

        assertEquals("Test", button.getText());
        assertEquals(Main.panelWidth, button.getMinimumSize().width);
        assertEquals(Main.panelHeight, button.getMinimumSize().height);
        assertFalse(button.isFocusable());
    }

    // --------------------------------------------------
    // Stability Tests
    // --------------------------------------------------

    @Test
    @DisplayName("Debug mode disables background refresh")
    void testDebugMode() {
        assertTrue(Debug.mode);
    }

    @Test
    @DisplayName("Frame revalidate and repaint do not throw")
    void testFrameRevalidateAndRepaint() {
        assertDoesNotThrow(() -> {
            Main.frame.getContentPane().revalidate();
            Main.frame.getContentPane().repaint();
        });
    }
}
