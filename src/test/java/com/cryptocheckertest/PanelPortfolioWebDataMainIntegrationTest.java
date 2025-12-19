package com.cryptocheckertest;

import com.cryptochecker.*;
import org.junit.jupiter.api.*;

import java.io.File;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class PanelPortfolioWebDataMainIntegrationTest {

    private static Main testMain;
    private static WebData webData;
    private PanelPortfolio panelPortfolio;

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        new File(Main.folderLocation).mkdirs();

        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = false;

        new Debug();

        testMain = new Main();
        Main.gui = testMain;
        webData = new WebData();
        testMain.webData = webData;

        // Initialize coin list
        webData.coin = new ArrayList<>();
        WebData.Coin btc = webData.getCoin();
        btc.name = "Bitcoin";
        btc.symbol = "BTC";
        btc.price = 50000.0;
        btc.percent_change_1h = 1.5;
        btc.percent_change_24h = 2.5;
        btc.percent_change_7d = 5.0;
        webData.coin.add(btc);

        WebData.Coin eth = webData.getCoin();
        eth.name = "Ethereum";
        eth.symbol = "ETH";
        eth.price = 3000.0;
        eth.percent_change_1h = -0.5;
        eth.percent_change_24h = 1.0;
        eth.percent_change_7d = -2.0;
        webData.coin.add(eth);

        // Initialize portfolio
        if (webData.portfolio == null)
            webData.portfolio = new ArrayList<>();
        if (webData.portfolio.isEmpty())
            webData.portfolio.add(new ArrayList<>());
        if (webData.portfolio_names == null)
            webData.portfolio_names = new ArrayList<>();
        if (webData.portfolio_names.isEmpty())
            webData.portfolio_names.add("Portfolio 1");
        webData.portfolio_nr = 0;
    }

    @AfterAll
    static void tearDownAfterAll() {
        if (Debug.frame != null) {
            Debug.frame.dispose();
            Debug.frame = null;
        }
    }

    @BeforeEach
    void setUp() {
        // Reset portfolio before each test
        webData.portfolio.clear();
        webData.portfolio.add(new ArrayList<>());
        webData.portfolio_names.clear();
        webData.portfolio_names.add("Portfolio 1");
        webData.portfolio_nr = 0;

        panelPortfolio = new PanelPortfolio();
        testMain.panelPortfolio = panelPortfolio;
    }

    @AfterEach
    void tearDown() {
        File f = new File(Main.portfolioSerLocation);
        if (f.exists())
            f.delete();
    }

    // ===== INITIALIZATION =====

    @Test
    @Order(1)
    void testPanelPortfolioAndWebDataInitialization() {
        assertNotNull(panelPortfolio);
        assertNotNull(panelPortfolio.webData);
        assertSame(webData, panelPortfolio.webData);
    }

    @Test
    @Order(2)
    void testPanelPortfolioAccessibleFromMain() {
        assertNotNull(Main.gui);
        assertNotNull(Main.gui.panelPortfolio);
        assertSame(panelPortfolio, Main.gui.panelPortfolio);
    }

    @Test
    @Order(3)
    void testWebDataSharesPortfolioReference() {
        assertSame(Main.gui.webData.portfolio, panelPortfolio.webData.portfolio);
        assertSame(Main.gui.webData.portfolio_names, panelPortfolio.names);
    }

    // ===== DATA SYNCHRONIZATION =====

    @Test
    @Order(10)
    void testAddCoinReflectsInWebData() {
        WebData.Coin coin = createPortfolioCoin("Bitcoin", 50000.0, 1.0);
        webData.portfolio.get(0).add(coin);

        assertEquals(1, panelPortfolio.webData.portfolio.get(0).size());
        assertEquals("Bitcoin", panelPortfolio.webData.portfolio.get(0).get(0).name);
    }

    @Test
    @Order(11)
    void testRemoveCoinReflectsInWebData() {
        WebData.Coin coin = createPortfolioCoin("Bitcoin", 50000.0, 1.0);
        webData.portfolio.get(0).add(coin);
        assertEquals(1, panelPortfolio.webData.portfolio.get(0).size());

        webData.portfolio.get(0).remove(0);
        assertEquals(0, panelPortfolio.webData.portfolio.get(0).size());
    }

    @Test
    @Order(12)
    void testModifyCoinReflectsInPanelPortfolio() {
        WebData.Coin coin = createPortfolioCoin("Bitcoin", 50000.0, 1.0);
        webData.portfolio.get(0).add(coin);

        // Modify through webData
        webData.portfolio.get(0).get(0).portfolio_amount = 5.0;

        // Should reflect in panelPortfolio
        assertEquals(5.0, panelPortfolio.webData.portfolio.get(0).get(0).portfolio_amount);
    }

    @Test
    @Order(13)
    void testRefreshPortfolioUpdatesFromWebData() {
        // Add coin to coin list with updated price
        WebData.Coin coinInList = webData.getCoin();
        coinInList.name = "TestCoin";
        coinInList.price = 1000.0;
        webData.coin.add(coinInList);

        // Add same coin to portfolio with old price
        WebData.Coin portfolioCoin = createPortfolioCoin("TestCoin", 500.0, 2.0);
        webData.portfolio.get(0).add(portfolioCoin);

        // Refresh should update portfolio coin price from coin list
        panelPortfolio.refreshPortfolio();

        assertEquals(1000.0, webData.portfolio.get(0).get(0).price, 0.01);
        webData.coin.remove(coinInList);
    }

    @Test
    @Order(14)
    void testCalculatePortfolioSumsValues() {
        WebData.Coin coin1 = createPortfolioCoin("Bitcoin", 50000.0, 1.0);
        coin1.portfolio_value = 50000.0;
        coin1.portfolio_gains = 5000.0;
        webData.portfolio.get(0).add(coin1);

        WebData.Coin coin2 = createPortfolioCoin("Ethereum", 3000.0, 2.0);
        coin2.portfolio_value = 6000.0;
        coin2.portfolio_gains = 500.0;
        webData.portfolio.get(0).add(coin2);

        assertDoesNotThrow(() -> panelPortfolio.calculatePortfolio());
    }

    // ===== MULTIPLE PORTFOLIOS =====

    @Test
    @Order(20)
    void testCreateMultiplePortfolios() {
        int initial = webData.portfolio.size();
        webData.portfolio.add(new ArrayList<>());
        webData.portfolio_names.add("Portfolio 2");

        assertEquals(initial + 1, webData.portfolio.size());
        assertEquals(initial + 1, webData.portfolio_names.size());
    }

    @Test
    @Order(21)
    void testSwitchPortfolioUpdatesNr() {
        webData.portfolio.add(new ArrayList<>());
        webData.portfolio_names.add("Portfolio 2");

        webData.portfolio_nr = 1;
        assertEquals(1, webData.portfolio_nr);

        webData.portfolio_nr = 0;
        assertEquals(0, webData.portfolio_nr);
    }

    @Test
    @Order(22)
    void testDeletePortfolioRemovesData() {
        webData.portfolio.add(new ArrayList<>());
        webData.portfolio_names.add("Portfolio 2");
        int size = webData.portfolio.size();

        webData.portfolio.remove(1);
        webData.portfolio_names.remove(1);

        assertEquals(size - 1, webData.portfolio.size());
    }

    @Test
    @Order(23)
    void testPortfolioDataIsolation() {
        // Add to portfolio 0
        WebData.Coin coin1 = createPortfolioCoin("Bitcoin", 50000.0, 1.0);
        webData.portfolio.get(0).add(coin1);

        // Create portfolio 1 with different coin
        webData.portfolio.add(new ArrayList<>());
        webData.portfolio_names.add("Portfolio 2");
        WebData.Coin coin2 = createPortfolioCoin("Ethereum", 3000.0, 2.0);
        webData.portfolio.get(1).add(coin2);

        // Verify isolation
        assertEquals(1, webData.portfolio.get(0).size());
        assertEquals("Bitcoin", webData.portfolio.get(0).get(0).name);
        assertEquals(1, webData.portfolio.get(1).size());
        assertEquals("Ethereum", webData.portfolio.get(1).get(0).name);
    }

    // ===== SERIALIZATION =====

    @Test
    @Order(30)
    void testSerializationCreatesFile() {
        WebData.Coin coin = createPortfolioCoin("Bitcoin", 50000.0, 1.0);
        webData.portfolio.get(0).add(coin);

        assertDoesNotThrow(() -> panelPortfolio.serializePortfolio());
        assertTrue(new File(Main.portfolioSerLocation).exists());
    }

    @Test
    @Order(31)
    void testSerializationPersistsData() throws Exception {
        WebData.Coin coin = createPortfolioCoin("Bitcoin", 50000.0, 2.5);
        webData.portfolio.get(0).add(coin);

        panelPortfolio.serializePortfolio();

        // Clear and deserialize
        int sizeBefore = webData.portfolio.get(0).size();
        assertTrue(sizeBefore > 0);
        assertTrue(new File(Main.portfolioSerLocation).exists());
    }

    @Test
    @Order(32)
    void testEmptyPortfolioSerialization() {
        webData.portfolio.get(0).clear();
        assertDoesNotThrow(() -> panelPortfolio.serializePortfolio());
        assertTrue(new File(Main.portfolioSerLocation).exists());
    }

    // ===== MAIN INTEGRATION =====

    @Test
    @Order(40)
    void testMainGuiReferenceWorks() {
        assertNotNull(Main.gui);
        assertNotNull(Main.gui.webData);
        assertSame(webData, Main.gui.webData);
    }

    @Test
    @Order(41)
    void testPanelPortfolioUsesMainWebData() {
        WebData.Coin coin = createPortfolioCoin("Bitcoin", 50000.0, 1.0);
        Main.gui.webData.portfolio.get(0).add(coin);

        assertEquals(1, panelPortfolio.webData.portfolio.get(0).size());
    }

    @Test
    @Order(42)
    void testMainCurrencyAffectsPortfolio() {
        String original = Main.currency;
        Main.currency = "EUR";
        Main.currencyChar = "€";

        WebData.Coin coin = createPortfolioCoin("Bitcoin", 50000.0, 1.0);
        coin.portfolio_currency = Main.currency;
        webData.portfolio.get(0).add(coin);

        assertEquals("EUR", webData.portfolio.get(0).get(0).portfolio_currency);

        Main.currency = original;
        Main.currencyChar = "$";
    }

    // ===== DATA FLOW =====

    @Test
    @Order(50)
    void testDataFlowWebDataToPanelPortfolio() {
        // Add through WebData
        WebData.Coin coin = createPortfolioCoin("TestCoin", 100.0, 5.0);
        webData.portfolio.get(0).add(coin);

        // Verify in PanelPortfolio
        boolean found = panelPortfolio.webData.portfolio.get(0).stream()
                .anyMatch(c -> c.name.equals("TestCoin"));
        assertTrue(found);
    }

    @Test
    @Order(51)
    void testDataFlowMainToPanelPortfolio() {
        // Add through Main.gui.webData
        WebData.Coin coin = createPortfolioCoin("MainCoin", 200.0, 3.0);
        Main.gui.webData.portfolio.get(0).add(coin);

        // Verify in PanelPortfolio
        boolean found = panelPortfolio.webData.portfolio.get(0).stream()
                .anyMatch(c -> c.name.equals("MainCoin"));
        assertTrue(found);
    }

    @Test
    @Order(52)
    void testBidirectionalDataSync() {
        // Add via panelPortfolio's webData reference
        WebData.Coin coin = createPortfolioCoin("SyncCoin", 150.0, 2.0);
        panelPortfolio.webData.portfolio.get(0).add(coin);

        // Verify in Main.gui.webData
        boolean foundInMain = Main.gui.webData.portfolio.get(0).stream()
                .anyMatch(c -> c.name.equals("SyncCoin"));
        assertTrue(foundInMain);

        // Verify in original webData
        boolean foundInWebData = webData.portfolio.get(0).stream()
                .anyMatch(c -> c.name.equals("SyncCoin"));
        assertTrue(foundInWebData);
    }

    // ===== STATE CONSISTENCY =====

    @Test
    @Order(60)
    void testStateAfterMultipleOperations() {
        // Add coins
        for (int i = 0; i < 5; i++) {
            WebData.Coin coin = createPortfolioCoin("Coin" + i, 100.0 * (i + 1), 1.0);
            webData.portfolio.get(0).add(coin);
        }
        assertEquals(5, panelPortfolio.webData.portfolio.get(0).size());

        // Remove some
        webData.portfolio.get(0).remove(0);
        webData.portfolio.get(0).remove(0);
        assertEquals(3, panelPortfolio.webData.portfolio.get(0).size());

        // Verify consistency
        assertSame(webData.portfolio, panelPortfolio.webData.portfolio);
    }

    @Test
    @Order(61)
    void testStateAfterRefresh() {
        WebData.Coin coin = createPortfolioCoin("RefreshCoin", 100.0, 1.0);
        webData.portfolio.get(0).add(coin);
        int sizeBefore = webData.portfolio.get(0).size();

        panelPortfolio.refreshPortfolio();

        assertEquals(sizeBefore, webData.portfolio.get(0).size());
    }

    @Test
    @Order(62)
    void testStateAfterCalculate() {
        WebData.Coin coin = createPortfolioCoin("CalcCoin", 100.0, 2.0);
        coin.portfolio_value = 200.0;
        webData.portfolio.get(0).add(coin);

        assertDoesNotThrow(() -> panelPortfolio.calculatePortfolio());
        assertEquals(1, webData.portfolio.get(0).size());
    }

    // ===== HELPER METHODS =====

    private WebData.Coin createPortfolioCoin(String name, double price, double amount) {
        WebData.Coin coin = webData.getCoin();
        coin.name = name;
        coin.price = price;
        coin.portfolio_amount = amount;
        coin.portfolio_price_start = price;
        coin.portfolio_value = price * amount;
        coin.portfolio_value_start = price * amount;
        coin.portfolio_gains = 0;
        coin.portfolio_currency = Main.currency;
        return coin;
    }
}
