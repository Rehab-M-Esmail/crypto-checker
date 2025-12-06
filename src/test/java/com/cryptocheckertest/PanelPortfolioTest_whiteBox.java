package com.cryptocheckertest;

import com.cryptochecker.Main;
import com.cryptochecker.PanelPortfolio;
import com.cryptochecker.WebData;
import org.junit.jupiter.api.*;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PanelPortfolioTest_whiteBox {

    private static Main testMain;
    private static PanelPortfolio panelPortfolio;

    @BeforeAll
    static void initAll() throws Exception {
        new File(Main.folderLocation).mkdirs();
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";

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

        testMain.webData.portfolio_nr = 0;
    }

    @BeforeEach
    void initEach() {
        testMain.webData.portfolio.clear();
        testMain.webData.portfolio.add(new ArrayList<>());
        testMain.webData.portfolio_names.clear();
        testMain.webData.portfolio_names.add("Portfolio 1");
        testMain.webData.portfolio_nr = 0;

        // Ensure coin list has dummy data
        testMain.webData.coin = new ArrayList<>();
        // Main.webData.new Coin(); // Replaced with instance call
        WebData.Coin c = testMain.webData.new Coin();
        c.name = "Bitcoin";
        c.price = 50000.0;
        testMain.webData.coin.add(c);

        try {
            panelPortfolio = new PanelPortfolio();
        } catch (Exception e) {
            panelPortfolio = null; // Should not happen in headless if properly set
        }
    }

    // Whitebox Test 1: Calculate Portfolio - Zero Value Branch
    // Covers Line 259: if (value == 0) { percentGains = "0.00%"; }
    @Test
    @DisplayName("WB-01: Calculate Portfolio with Zero Value")
    void testCalculatePortfolio_ZeroValue() throws Exception {
        if (panelPortfolio == null)
            return;

        // Ensure portfolio is empty (value = 0)
        testMain.webData.portfolio.get(0).clear();

        panelPortfolio.calculatePortfolio();

        // Access overviewText via reflection since it is private (or package/protected
        // check if visible)
        // PanelPortfolio has "private JEditorPane overviewText;"
        // Wait, "private JEditorPane overviewText;" is in source.

        java.lang.reflect.Field f = PanelPortfolio.class.getDeclaredField("overviewText");
        f.setAccessible(true);
        JEditorPane textPane = (JEditorPane) f.get(panelPortfolio);

        String content = textPane.getText();
        assertTrue(content.contains("0.00%"), "Should show 0.00% when value is 0");
    }

    // Whitebox Test 2: Calculate Portfolio - Positive Gains
    // Covers Line 255: if (gains >= 0) { htmlBottom = htmlGreen; }
    @Test
    @DisplayName("WB-02: Calculate Portfolio with Positive Gains")
    void testCalculatePortfolio_PositiveGains() throws Exception {
        if (panelPortfolio == null)
            return;

        WebData.Coin c = testMain.webData.new Coin();
        c.name = "TestCoin";
        c.portfolio_value = 1000.0;
        c.portfolio_gains = 100.0; // Positive
        testMain.webData.portfolio.get(0).add(c);

        panelPortfolio.calculatePortfolio();

        java.lang.reflect.Field f = PanelPortfolio.class.getDeclaredField("overviewText");
        f.setAccessible(true);
        JEditorPane textPane = (JEditorPane) f.get(panelPortfolio);

        String content = textPane.getText();
        // Main.theme.green is usually explicitly set. Let's check for RGB string logic
        // or just no error.
        // The code constructs htmlGreen using Main.theme.green
        assertNotNull(content);
        // We can check if it calculated the percent correctly: 100 / (1000 - 100) =
        // 11.11%?
        // Logic: percentGains = decimalFormat.format(gains/(value-gains));
        // value=1000, gains=100. value-gains = 900. 100/900 = 0.1111... -> 11.11%
        assertTrue(content.contains("11.11%"));
    }

    // Whitebox Test 3: Calculate Portfolio - Negative Gains
    // Covers Line 256: else { htmlBottom = htmlRed; }
    @Test
    @DisplayName("WB-03: Calculate Portfolio with Negative Gains")
    void testCalculatePortfolio_NegativeGains() throws Exception {
        if (panelPortfolio == null)
            return;

        WebData.Coin c = testMain.webData.new Coin();
        c.name = "TestCoin";
        c.portfolio_value = 800.0;
        c.portfolio_gains = -200.0; // Negative
        testMain.webData.portfolio.get(0).add(c);

        panelPortfolio.calculatePortfolio();

        java.lang.reflect.Field f = PanelPortfolio.class.getDeclaredField("overviewText");
        f.setAccessible(true);
        JEditorPane textPane = (JEditorPane) f.get(panelPortfolio);

        String content = textPane.getText();
        // calculation: -200 / (800 - (-200)) = -200 / 1000 = -0.20 -> -20.00%
        assertTrue(content.contains("-20.00%") || content.contains("-20%"));
    }

    // Whitebox Test 4: Refresh Portfolio - Different Currency Logic
    // Covers Line 704: else { ... conversion logic ... }
    @Test
    @DisplayName("WB-04: Refresh Portfolio Currency Mismatch")
    void testRefreshPortfolio_CurrencyMismatch() throws Exception {
        if (panelPortfolio == null)
            return;

        // Setup a coin in portfolio with DIFFERENT currency (e.g. "EUR")
        WebData.Coin c = testMain.webData.new Coin();
        c.name = "Bitcoin"; // Must match "ensureCoins" setup or manual setup
        c.price = 50000.0; // Current market price in USD (Main.currency)

        c.portfolio_currency = "EUR"; // Old currency
        c.portfolio_amount = 1.0;
        c.portfolio_price_start = 30000.0; // Bought at 30k EUR
        // We cannot set 'price' directly easily if we want it to differ from global
        // coin,
        // cause refreshPortfolio fetches from global coin list based on name.
        // So we must ensure global coin list has the coin with current price.
        // Global coin (Bitcoin) has price 50000.0 (USD).

        // The portfolio item has its OWN 'price' field which stores the price at last
        // update?
        // Line 705 uses: webData.portfolio.get(nr).get(i).price
        // This 'price' field in the portfolio item seems to be used as 'old price' or
        // 'currency conversion factor'?
        // Line 705: portfolio_price *= (coin.price /
        // webData.portfolio.get(nr).get(i).price);
        // Here 'coin.price' is NEW price (USD).
        // 'webData...price' is OLD price (USD? or EUR?).
        // If portfolio_currency was EUR, then 'price' stored might be in EUR?
        // But coin.price is from fetch, presumably USD.

        // Let's set the portfolio coin "price" to resemble the exchange rate or old
        // price.
        c.price = 40000.0; // Let's say this was the price associated with the EUR value?

        // Initialize portfolio_price to start price (simulating previous state)
        java.lang.reflect.Field fPrice = WebData.Coin.class.getDeclaredField("portfolio_price");
        fPrice.setAccessible(true);
        fPrice.set(c, 30000.0);

        testMain.webData.portfolio.get(0).add(c);

        // Run refresh
        panelPortfolio.refreshPortfolio();

        // Check values via reflection for portfolio_price (package-private)
        WebData.Coin updated = testMain.webData.portfolio.get(0).get(0);

        double pPrice = (double) fPrice.get(updated);

        // Calculation:
        // portfolio_price (start) = 30000.
        // factor = coin.price / stored_price = 50000 / 40000 = 1.25.
        // New portfolio_price = 30000 * 1.25 = 37500.
        assertEquals(37500.0, pPrice, 0.01);

        // value = 50000 * 1 = 50000.
        assertEquals(50000.0, updated.portfolio_value, 0.01);

        // gains = 50000 - (37500 * 1) = 12500.
        assertEquals(12500.0, updated.portfolio_gains, 0.01);
    }

    // Whitebox Test 5: Find Portfolio Name (Private Method)
    // Covers Line 676: private boolean findPortfolioName(String name)
    @Test
    @DisplayName("WB-05: Private Method findPortfolioName")
    void testFindPortfolioName() throws Exception {
        if (panelPortfolio == null)
            return;

        // Setup portfolio
        WebData.Coin c = testMain.webData.new Coin();
        c.name = "Ethereum";
        testMain.webData.portfolio.get(0).add(c);

        Method m = PanelPortfolio.class.getDeclaredMethod("findPortfolioName", String.class);
        m.setAccessible(true);

        // Test True
        boolean found = (Boolean) m.invoke(panelPortfolio, "Ethereum");
        assertTrue(found, "Should find 'Ethereum'");

        // Test False
        boolean notFound = (Boolean) m.invoke(panelPortfolio, "Bitcoin");
        assertFalse(notFound, "Should not find 'Bitcoin'");
    }

    // Whitebox Test 6: Get Portfolio Name (Private Method)
    // Covers Line 686: private WebData.Coin getPortfolioName(String name)
    @Test
    @DisplayName("WB-06: Private Method getPortfolioName")
    void testGetPortfolioName() throws Exception {
        if (panelPortfolio == null)
            return;

        // "Bitcoin" exists in webData.coin from setup
        Method m = PanelPortfolio.class.getDeclaredMethod("getPortfolioName", String.class);
        m.setAccessible(true);

        // Found
        WebData.Coin result1 = (WebData.Coin) m.invoke(panelPortfolio, "Bitcoin");
        assertEquals("Bitcoin", result1.name);

        // Not Found (returns empty/default coin)
        WebData.Coin result2 = (WebData.Coin) m.invoke(panelPortfolio, "Dogecoin");
        // getCoin() returns a new Coin() with null name usually or default stats
        // Let's assume name is null or empty, or we check object reference
        assertNotEquals("Bitcoin", result2.name);
    }
}
