package com.cryptocheckertest;

import org.junit.*;
import static org.junit.Assert.*;

public class MenuTest {

    private String currentVisiblePage = null;
    private boolean applicationRunning = true;

    @Before
    public void setUp() {
        currentVisiblePage = "Coin Data";
        applicationRunning = true;
    }

    @Test
    public void testClickCoinDataButton() {
        currentVisiblePage = "Portfolio";
        simulateButtonClick("Coin Data");
        assertEquals("Coin Data", currentVisiblePage);
    }

    @Test
    public void testClickPortfolioButton() {
        currentVisiblePage = "Converter";
        simulateButtonClick("Portfolio");
        assertEquals("Portfolio", currentVisiblePage);
    }

    @Test
    public void testClickConverterButton() {
        currentVisiblePage = "Settings";
        simulateButtonClick("Converter");
        assertEquals("Converter", currentVisiblePage);
    }

    @Test
    public void testClickSettingsButton() {
        currentVisiblePage = "Coin Data";
        simulateButtonClick("Settings");
        assertEquals("Settings", currentVisiblePage);
    }


    @Test
    public void testExitButton() {
        assertTrue(applicationRunning);
        currentVisiblePage = "Coin Data";
        simulateButtonClick("Exit");
        assertFalse(applicationRunning);
    }

    private void simulateButtonClick(String buttonName) {
        if (!applicationRunning) {
            return;
        }

        switch (buttonName) {
            case "Coin Data":
                currentVisiblePage = "Coin Data";
                break;
            case "Portfolio":
                currentVisiblePage = "Portfolio";
                break;
            case "Converter":
                currentVisiblePage = "Converter";
                break;
            case "Settings":
                currentVisiblePage = "Settings";
                break;
            case "Exit":
                applicationRunning = false;
                break;
            default:
                fail("Unknown button: " + buttonName);
        }
    }

    @After
    public void tearDown() {
        currentVisiblePage = null;
        applicationRunning = false;
    }
}
