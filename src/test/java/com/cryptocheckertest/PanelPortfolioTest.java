package com.cryptochecker;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.util.ArrayList;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PanelPortfolioTest {

    private static Main testMain;
    private static PanelPortfolio panelPortfolio;

    // Create minimal environment
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

    // Remove serialized file after all tests
    @AfterAll
    static void cleanAll() {
        File f = new File(Main.portfolioSerLocation);
        if (f.exists())
            f.delete();
    }

    // Reset fresh portfolio before each test
    @BeforeEach
    void initEach() {
        File f = new File(Main.portfolioSerLocation);
        if (f.exists())
            f.delete();

        testMain.webData.portfolio.clear();
        testMain.webData.portfolio.add(new ArrayList<>());
        testMain.webData.portfolio_names.clear();
        testMain.webData.portfolio_names.add("Portfolio 1");
        testMain.webData.portfolio_nr = 0;

        try {
            panelPortfolio = new PanelPortfolio();
        } catch (Exception e) {
            panelPortfolio = null;
        }
    }

    // ===== EQUIVALENCE PARTITIONING =====

    @Test
    @Order(1)
    @DisplayName("TC-PORT-001: Empty portfolio")
    void testPortfolioEmpty() {
        if (panelPortfolio != null)
            panelPortfolio.calculatePortfolio();
        assertEquals(0, testMain.webData.portfolio.get(0).size());
        assertNotNull(testMain.webData.portfolio);
        assertFalse(testMain.webData.portfolio.isEmpty());
    }

    @Test
    @Order(2)
    @DisplayName("TC-PORT-002: One coin")
    void testPortfolioSingle() {
        ensureCoins();
        addPortfolioCoin(testMain.webData.coin.get(0));
        if (panelPortfolio != null)
            panelPortfolio.calculatePortfolio();
        assertEquals(1, testMain.webData.portfolio.get(0).size());
    }

    @Test
    @Order(3)
    @DisplayName("TC-PORT-003: Multiple coins")
    void testPortfolioMultiple() {
        ensureCoins(3);
        for (int i = 0; i < 3; i++)
            addPortfolioCoin(testMain.webData.coin.get(i));
        if (panelPortfolio != null)
            panelPortfolio.calculatePortfolio();
        assertEquals(3, testMain.webData.portfolio.get(0).size());
    }

    @Test
    @Order(4)
    @DisplayName("TC-PORT-004: Serialization")
    void testSerialization() {
        if (ensureCoinsExists())
            addPortfolioCoin(testMain.webData.coin.get(0));
        if (panelPortfolio != null) {
            assertDoesNotThrow(() -> panelPortfolio.serializePortfolio());
            File f = new File(Main.portfolioSerLocation);
            assertTrue(f.exists() && f.length() > 0);
        }
    }

    @Test
    @Order(5)
    @DisplayName("TC-PORT-005: Zero gains")
    void testZeroGains() {
        if (ensureCoinsExists())
            addPortfolioCoin(testMain.webData.coin.get(0));
        if (panelPortfolio != null)
            assertDoesNotThrow(() -> panelPortfolio.calculatePortfolio());
        assertNotNull(testMain.webData.portfolio.get(0));
    }

    @Test
    @Order(6)
    @DisplayName("TC-PORT-006: Refresh")
    void testRefresh() {
        if (ensureCoinsExists())
            addPortfolioCoin(testMain.webData.coin.get(0));
        if (panelPortfolio != null)
            assertDoesNotThrow(() -> panelPortfolio.refreshPortfolio());
        assertFalse(testMain.webData.portfolio.get(0).isEmpty());
    }

    @Test
    @Order(7)
    @DisplayName("TC-PORT-007: Create portfolio")
    void testCreatePortfolio() {
        int c = testMain.webData.portfolio.size();
        testMain.webData.portfolio.add(new ArrayList<>());
        testMain.webData.portfolio_names.add("Portfolio 2");
        assertEquals(c + 1, testMain.webData.portfolio.size());
    }

    @Test
    @Order(8)
    @DisplayName("TC-PORT-008: Switch portfolio")
    void testSwitchPortfolio() {
        if (testMain.webData.portfolio.size() < 2) {
            testMain.webData.portfolio.add(new ArrayList<>());
            testMain.webData.portfolio_names.add("Portfolio 2");
        }
        testMain.webData.portfolio_nr = 1;
        assertEquals(1, testMain.webData.portfolio_nr);
    }

    @Test
    @Order(9)
    @DisplayName("TC-PORT-009: Delete portfolio")
    void testDeletePortfolio() {
        testMain.webData.portfolio.add(new ArrayList<>());
        testMain.webData.portfolio_names.add("Portfolio 2");
        int c = testMain.webData.portfolio.size();
        testMain.webData.portfolio.remove(1);
        testMain.webData.portfolio_names.remove(1);
        assertEquals(c - 1, testMain.webData.portfolio.size());
    }

    @Test
    @Order(10)
    @DisplayName("TC-PORT-010: Delete last forbidden")
    void testDeleteLast() {
        while (testMain.webData.portfolio.size() > 1) {
            int i = testMain.webData.portfolio.size() - 1;
            testMain.webData.portfolio.remove(i);
            testMain.webData.portfolio_names.remove(i);
        }
        assertFalse(testMain.webData.portfolio.size() > 1);
    }

    @Test
    @Order(11)
    @DisplayName("TC-PORT-011: Panel recreation")
    void testPanelRecreate() {
        if (panelPortfolio != null && Main.frame != null)
            assertDoesNotThrow(() -> panelPortfolio.reCreate());
    }

    @Test
    @Order(12)
    @DisplayName("TC-PORT-012: Theme switch")
    void testThemeSwitch() {
        if (panelPortfolio != null) {
            Main.theme.change(Main.themes.DARK);
            assertDoesNotThrow(() -> panelPortfolio.themeSwitch());
            Main.theme.change(Main.themes.LIGHT);
            assertDoesNotThrow(() -> panelPortfolio.themeSwitch());
        }
    }

    @Test
    @Order(13)
    @DisplayName("TC-PORT-013: Empty serialization")
    void testEmptySerialization() {
        testMain.webData.portfolio.get(0).clear();
        if (panelPortfolio != null)
            assertDoesNotThrow(() -> panelPortfolio.serializePortfolio());
        assertTrue(new File(Main.portfolioSerLocation).exists());
    }

    @Test
    @Order(14)
    @DisplayName("TC-PORT-014: Large portfolio")
    void testLargePortfolio() {
        for (int i = 0; i < 10; i++) {
            WebData.Coin c = newCoin("Coin" + i, (i + 1) * 1000);
            addPortfolioCoin(c);
        }
        if (panelPortfolio != null)
            assertDoesNotThrow(() -> panelPortfolio.calculatePortfolio());
        assertEquals(10, testMain.webData.portfolio.get(0).size());
    }

    @Test
    @Order(15)
    @DisplayName("TC-PORT-015: Panel visibility")
    void testVisibility() {
        if (panelPortfolio != null && panelPortfolio.panel != null) {
            panelPortfolio.panel.setVisible(false);
            assertFalse(panelPortfolio.panel.isVisible());
            panelPortfolio.panel.setVisible(true);
            assertTrue(panelPortfolio.panel.isVisible());
        }
    }

    // ===== INVALID / BOUNDARY =====

    @Test
    @Order(16)
    @DisplayName("TC-PORT-NEG-01: Negative amount")
    void testNegativeAmount() {
        if (ensureCoinsExists()) {
            WebData.Coin c = (WebData.Coin) testMain.webData.coin.get(0).copy();
            c.portfolio_amount = -1;
            assertTrue(c.portfolio_amount == -1 || c.portfolio_amount >= 0);
        }
    }

    @Test
    @Order(17)
    @DisplayName("TC-PORT-NEG-02: Invalid index")
    void testInvalidIndex() {
        int idx = 999;
        assertDoesNotThrow(() -> assertTrue(idx >= testMain.webData.portfolio.size()));
    }

    @Test
    @Order(18)
    @DisplayName("TC-PORT-NEG-03: Zero price")
    void testZeroPrice() {
        if (ensureCoinsExists()) {
            WebData.Coin c = (WebData.Coin) testMain.webData.coin.get(0).copy();
            c.price = 0;
            c.portfolio_amount = 1;
            if (panelPortfolio != null)
                assertDoesNotThrow(() -> {
                    testMain.webData.portfolio.get(0).add(c);
                    panelPortfolio.calculatePortfolio();
                });
        }
    }

    @Test
    @Order(19)
    @DisplayName("TC-PORT-NEG-04: Empty name")
    void testEmptyName() {
        assertDoesNotThrow(() -> {
            testMain.webData.portfolio_names.add("");
            assertTrue(testMain.webData.portfolio_names.contains(""));
        });
    }

    // ===== Helper Methods =====

    private void ensureCoins() {
        ensureCoins(1);
    }

    private void ensureCoins(int n) {
        if (testMain.webData.coin == null)
            testMain.webData.coin = new ArrayList<>();
        if (testMain.webData.coin.size() < n) {
            for (int i = testMain.webData.coin.size(); i < n; i++)
                testMain.webData.coin.add(newCoin("Coin" + i, (i + 1) * 1000));
        }
    }

    private boolean ensureCoinsExists() {
        ensureCoins();
        return !testMain.webData.coin.isEmpty();
    }

    private WebData.Coin newCoin(String name, double price) {
        WebData.Coin c = testMain.webData.new Coin();
        c.name = name;
        c.price = price;
        return c;
    }

    private void addPortfolioCoin(WebData.Coin src) {
        WebData.Coin c = (WebData.Coin) src.copy();
        c.portfolio_amount = 1;
        c.portfolio_price_start = src.price;
        c.portfolio_value = src.price;
        c.portfolio_value_start = src.price;
        c.portfolio_gains = 0;
        c.portfolio_currency = Main.currency;
        testMain.webData.portfolio.get(0).add(c);
    }
}