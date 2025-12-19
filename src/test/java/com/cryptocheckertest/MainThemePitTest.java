package com.cryptocheckertest;

import com.cryptochecker.Main;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pitest-friendly tests for Main.Theme (pure logic, no GUI).
 */
public class MainThemePitTest {

    @Test
    void lightThemeSetsExpectedColors() {
        Main.Theme theme = new Main.Theme(Main.themes.LIGHT);
        assertEquals(Color.WHITE, theme.background);
        assertEquals(Color.BLACK, theme.foreground);
        assertEquals(new Color(0, 128, 0), theme.green);
        assertEquals(new Color(220, 20, 60), theme.red);
        assertEquals(Color.GRAY, theme.selection);
    }

    @Test
    void darkThemeSetsExpectedColors() {
        Main.Theme theme = new Main.Theme(Main.themes.DARK);
        assertEquals(new Color(15, 15, 15), theme.background);
        assertEquals(Color.WHITE, theme.foreground);
        assertEquals(Color.GREEN, theme.green);
        assertEquals(Color.RED, theme.red);
        assertEquals(Color.GRAY, theme.selection);
        assertEquals(new Color(78, 78, 78), theme.emptyBackground);
    }

    @Test
    void customThemeUsesCustomFields() {
        Main.Theme theme = new Main.Theme(Main.themes.CUSTOM);
        // defaults come from resetCustom()
        assertEquals(new Color(0, 0, 0), theme.customBackground);
        assertEquals(new Color(14, 255, 0), theme.customForeground);
        assertEquals(new Color(244, 255, 0), theme.customGreen);
        assertEquals(new Color(255, 0, 0), theme.customRed);
        assertEquals(new Color(128, 128, 128), theme.customSelection);
        assertEquals(new Color(45, 45, 45), theme.customEmptyBackground);
    }

    @Test
    void changeUpdatesCurrentTheme() {
        Main.Theme theme = new Main.Theme(Main.themes.LIGHT);
        theme.change(Main.themes.DARK);
        assertEquals(Main.themes.DARK, theme.currentTheme);
        assertEquals(new Color(15, 15, 15), theme.background);

        theme.change(Main.themes.CUSTOM);
        assertEquals(Main.themes.CUSTOM, theme.currentTheme);
        assertEquals(theme.customBackground, theme.background);
    }

    @Test
    void resetCustomRestoresDefaults() {
        Main.Theme theme = new Main.Theme(Main.themes.CUSTOM);
        theme.customBackground = new Color(1, 2, 3);
        theme.customForeground = new Color(4, 5, 6);
        theme.customGreen = new Color(7, 8, 9);
        theme.customRed = new Color(10, 11, 12);
        theme.customSelection = new Color(13, 14, 15);
        theme.customEmptyBackground = new Color(16, 17, 18);

        theme.resetCustom();

        assertEquals(new Color(0, 0, 0), theme.customBackground);
        assertEquals(new Color(14, 255, 0), theme.customForeground);
        assertEquals(new Color(244, 255, 0), theme.customGreen);
        assertEquals(new Color(255, 0, 0), theme.customRed);
        assertEquals(new Color(128, 128, 128), theme.customSelection);
        assertEquals(new Color(45, 45, 45), theme.customEmptyBackground);
    }
}

