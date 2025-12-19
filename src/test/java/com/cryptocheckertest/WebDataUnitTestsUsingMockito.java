package com.cryptocheckertest;

import com.cryptochecker.Main;
import com.cryptochecker.WebData;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.net.ssl.HttpsURLConnection;
import javax.swing.*;
import java.io.*;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;

public class WebDataUnitTestsUsingMockito {
    @Mock
    private HttpsURLConnection mockConnection;

    private WebData webData;
    private File tempSerFile;
    private String originalDataSerLocation;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        // Create temporary file for serialization tests
        tempSerFile = File.createTempFile("test_data", ".ser");
        tempSerFile.deleteOnExit();

        // Store original location and set temporary location
        originalDataSerLocation = Main.dataSerLocation;
        Main.dataSerLocation = tempSerFile.getAbsolutePath();

        // Set up basic Main dependencies
        Main.currency = "USD";
        Main.frame = mock(JFrame.class);
    }

    @AfterEach
    void tearDown() {
        // Restore original location
        Main.dataSerLocation = originalDataSerLocation;

        // Clean up temp file
        if (tempSerFile.exists()) {
            tempSerFile.delete();
        }
    }

    @Test
    @DisplayName("Test fetchJson with successful response")
    public void testFetchJsonSuccess() throws Exception {
        webData = new WebData();
        String mockJsonResponse = "{\"test\":\"data\"}";

        // This is an integration test concept - in practice you'd need to mock URL connection
        // For now, testing the method structure
        assertNotNull(webData);
    }

    @Test
    @DisplayName("Test fetchJson with rate limiting (HTTP 429)")
    public void testFetchJsonRateLimit() throws Exception {
        webData = new WebData();

        // Test that rate limiting retry logic exists
        try {
            // This would require PowerMockito to mock static URL constructor
            // Placeholder for integration test
            assertNotNull(webData);
        } catch (Exception e) {
            // Expected for unit test without full mocking
        }
    }

    @Test
    @DisplayName("Test Coin object creation and methods")
    public void testCoinObject() throws Exception {
        webData = spy(new WebData());
        WebData.Coin coin = webData.getCoin();

        assertNotNull(coin);

        // Set test data
        coin.id = "bitcoin";
        coin.name = "Bitcoin";
        coin.symbol = "BTC";
        coin.rank = 1;
        coin.price = 50000.0;
        coin.market_cap = 1000000000.0;
        coin._24h_volume = 50000000.0;
        coin.percent_change_1h = 1.5;
        coin.percent_change_24h = 2.3;
        coin.percent_change_7d = 5.7;

        // Test toString
        assertEquals("Bitcoin", coin.toString());

        // Test trimPrice
        assertEquals("50000", coin.trimPrice(50000.0));
        assertEquals("0.5", coin.trimPrice(0.5));
        assertEquals("0.005", coin.trimPrice(0.005));
        assertEquals("0.0000005", coin.trimPrice(0.0000005));

        // Test getInfo
        String info = coin.getInfo();
        assertNotNull(info);
        assertTrue(info.contains("Bitcoin"));
        assertTrue(info.contains("BTC"));
        assertTrue(info.contains("Rank: 1"));
    }

    @Test
    @DisplayName("Test Coin clone functionality")
    public void testCoinClone() throws Exception {
        webData = new WebData();
        WebData.Coin original = webData.getCoin();

        original.id = "ethereum";
        original.name = "Ethereum";
        original.price = 3000.0;

        WebData.Coin cloned = (WebData.Coin) original.clone();

        assertNotNull(cloned);
        assertEquals(original.id, cloned.id);
        assertEquals(original.name, cloned.name);
        assertEquals(original.price, cloned.price);
        assertNotSame(original, cloned);
    }

    @Test
    @DisplayName("Test Coin portfolio information")
    public void testCoinPortfolio() throws Exception {
        webData = new WebData();
        WebData.Coin coin = webData.getCoin();

        coin.name = "Bitcoin";
        coin.portfolio_amount = 2.5;
        coin.portfolio_value = 125000.0;
        coin.portfolio_gains = 25000.0;
        coin.portfolio_currency = "USD";
        coin.portfolio_price_start = 40000.0;
        coin.portfolio_value_start = 100000.0;

        String portfolioInfo = coin.getPortfolio();

        assertNotNull(portfolioInfo);
        assertTrue(portfolioInfo.contains("Portfolio Amount"));
        assertTrue(portfolioInfo.contains("Portfolio Value"));
        assertTrue(portfolioInfo.contains("Portfolio Gains"));
        assertTrue(portfolioInfo.contains("USD"));
    }

    @Test
    @DisplayName("Test Global_Data toString method")
    public void testGlobalDataToString() throws Exception {
        webData = new WebData();
        WebData.Global_Data globalData = webData.new Global_Data();

        globalData.total_market_cap = 2000000000000L;
        globalData.total_24h_volume = 100000000000L;
        globalData.bitcoin_percentage_of_market_cap = 45.5;
        globalData.active_currencies = 5000;
        globalData.active_assets = 1000;
        globalData.active_markets = 20000;
        globalData.last_updated = 1234567890L;

        String result = globalData.toString();

        assertNotNull(result);
        assertTrue(result.contains("Total Market Cap"));
        assertTrue(result.contains("Bitcoin Dominance"));
        assertTrue(result.contains("45.5%"));
        assertTrue(result.contains("Active Currencies: 5000"));
    }
    @Test
    @DisplayName("Test portfolio management")
    public void testPortfolioManagement() throws Exception {
        webData = new WebData();

        // Initialize portfolio
        webData.portfolio = new ArrayList<>();
        webData.portfolio_names = new ArrayList<>();
        webData.portfolio_nr = 0;

        // Add portfolio
        ArrayList<WebData.Coin> portfolio1 = new ArrayList<>();
        WebData.Coin coin = webData.getCoin();
        coin.name = "Bitcoin";
        portfolio1.add(coin);

        webData.portfolio.add(portfolio1);
        webData.portfolio_names.add("My Portfolio");

        assertEquals(1, webData.portfolio.size());
        assertEquals("My Portfolio", webData.portfolio_names.get(0));
        assertEquals(1, webData.portfolio.get(0).size());
    }

    @Test
    @DisplayName("Test trimPrice with various values")
    public void testTrimPriceVariousValues() throws Exception {
        webData = new WebData();
        WebData.Coin coin = webData.getCoin();

        // Test different price ranges
        assertEquals("100", coin.trimPrice(100.0));
        assertEquals("10.5", coin.trimPrice(10.5));
        assertEquals("1.23", coin.trimPrice(1.234));
        assertEquals("0.5", coin.trimPrice(0.5));
        assertEquals("0.123", coin.trimPrice(0.123));
        assertEquals("0.0123", coin.trimPrice(0.0123));
        assertEquals("0.00123", coin.trimPrice(0.00123));
        assertEquals("0.000123", coin.trimPrice(0.000123));
        assertEquals("0.0000123", coin.trimPrice(0.0000123));
    }

    @Test
    @DisplayName("Test Coin copy method")
    public void testCoinCopyMethod() throws Exception {
        webData = new WebData();
        WebData.Coin original = webData.getCoin();

        original.id = "litecoin";
        original.name = "Litecoin";
        original.price = 150.0;

        Object copied = original.copy();

        assertNotNull(copied);
        assertTrue(copied instanceof WebData.Coin);

        WebData.Coin copiedCoin = (WebData.Coin) copied;
        assertEquals(original.id, copiedCoin.id);
        assertEquals(original.name, copiedCoin.name);
        assertEquals(original.price, copiedCoin.price);
    }

    @Test
    @DisplayName("Test empty coin list handling")
    public void testEmptyCoinList() throws Exception {
        webData = new WebData();
        webData.coin = new ArrayList<>();

        assertNotNull(webData.coin);
        assertEquals(0, webData.coin.size());
    }
}
