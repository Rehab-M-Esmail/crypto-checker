package com.cryptochecker;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

class MenuTest {

    Menu menu;

    @BeforeEach
    void setup() {
        Main.gui = new Main();

        Main.gui.panelCoin = new PanelCoin();
        Main.gui.panelPortfolio = new PanelPortfolio();
        Main.gui.panelConverter = new PanelConverter();
        Main.gui.panelSettings = new PanelSettings();

        Main.frame = new JFrame();

        menu = new Menu();

        // Add menu panel to a dummy frame content pane (optional)
        Main.frame.getContentPane().add(menu.panel);
    }

    @Test
    void testPortfolioButtonShowsPortfolioPanel() {
        JButton portfolioButton = getButtonByText(menu.panel, "Portfolio");
        assertNotNull(portfolioButton);

        for (ActionListener listener : portfolioButton.getActionListeners()) {
            listener.actionPerformed(null);
        }

        assertTrue(Main.gui.panelPortfolio.panel.isVisible());
        assertFalse(Main.gui.panelCoin.panel.isVisible());
    }

    @Test
    void testConverterButtonShowsConverterPanel() {
        JButton converterButton = getButtonByText(menu.panel, "Converter");
        assertNotNull(converterButton);

        for (ActionListener listener : converterButton.getActionListeners()) {
            listener.actionPerformed(null);
        }

        assertTrue(Main.gui.panelConverter.panel.isVisible());
        assertFalse(Main.gui.panelCoin.panel.isVisible());
    }

    @Test
    void testSettingsButtonShowsSettingsPanel() {
        JButton settingsButton = getButtonByText(menu.panel, "Settings");
        assertNotNull(settingsButton);

        for (ActionListener listener : settingsButton.getActionListeners()) {
            listener.actionPerformed(null);
        }

        assertTrue(Main.gui.panelSettings.panel.isVisible());
        assertFalse(Main.gui.panelCoin.panel.isVisible());
    }

    @Test
    void testExitButtonCallsSystemExit() {
        JButton exitButton = getButtonByText(menu.panel, "Exit");
        assertNotNull(exitButton);

        // Mock System.exit or flag to test exit called without actually exiting
        ExitSecurityManager securityManager = new ExitSecurityManager();
        System.setSecurityManager(securityManager);

        try {
            for (ActionListener listener : exitButton.getActionListeners()) {
                listener.actionPerformed(null);
            }
            fail("Expected System.exit to be called");
        } catch (ExitException e) {
            assertEquals(0, e.status);
        } finally {
            System.setSecurityManager(null); // reset security manager
        }
    }

    private JButton getButtonByText(Container container, String text) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton && text.equals(((JButton) comp).getText())) {
                return (JButton) comp;
            } else if (comp instanceof Container) {
                JButton found = getButtonByText((Container) comp, text);
                if (found != null) return found;
            }
        }
        return null;
    }

    // Security manager to intercept System.exit call
    private static class ExitSecurityManager extends SecurityManager {
        @Override
        public void checkPermission(java.security.Permission perm) {
            // allow everything
        }
        @Override
        public void checkExit(int status) {
            super.checkExit(status);
            throw new ExitException(status);
        }
    }

    private static class ExitException extends SecurityException {
        final int status;
        ExitException(int status) { this.status = status; }
    }
}
