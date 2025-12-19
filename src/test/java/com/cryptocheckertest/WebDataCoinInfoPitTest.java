package com.cryptocheckertest;

import com.cryptochecker.Main;
import com.cryptochecker.WebData;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pitest-friendly tests for WebData.Coin.getInfo() and getPortfolio().
 * Uses a safe stubbed WebData that skips deserialize/fetch.
 */
public class WebDataCoinInfoPitTest {

    private WebData.Coin coin;

    static class StubWebData extends WebData {
        StubWebData() throws Exception {
            super();
        }
        @Override
        public void deserialize() {
            // no-op to avoid file/network
        }
    }

    @BeforeAll
    static void setUpAll() throws Exception {
        // Ensure dataSerLocation exists to satisfy WebData constructor if invoked
        File temp = File.createTempFile("pit_webdata_coininfo", ".ser");
        temp.deleteOnExit();
        Main.dataSerLocation = temp.getAbsolutePath();
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(temp))) {
            oos.writeObject(null); // global_data
            oos.writeObject(new ArrayList<>()); // coin list
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // Prevent HeadlessException on environments where headless=true
        System.setProperty("java.awt.headless", "false");

        // Use stub to avoid deserialize/fetch side effects
        WebData webData = new StubWebData();
        coin = webData.new Coin();

        // Seed required globals
        Main.currency = "USD";
        Main.currencyChar = "$";

        // Populate coin fields
        coin.rank = 1;
        coin.id = "bitcoin";
        coin.name = "Bitcoin";
        coin.symbol = "BTC";
        coin.price = 50000.1234;
        coin.market_cap = 1000000000d;
        coin._24h_volume = 25000000d;
        setField(coin, "available_supply", 19000000d);
        setField(coin, "total_supply", 21000000d);
        setField(coin, "max_supply", 21000000d);
        coin.percent_change_1h = 0.5;
        coin.percent_change_24h = 1.1;
        coin.percent_change_7d = -2.2;
        setField(coin, "last_updated", "2025-01-01T00:00:00Z");

        coin.portfolio_amount = 2.5;
        coin.portfolio_value = 125000.55;
        coin.portfolio_gains = 5000.25;
        coin.portfolio_currency = "USD";
        coin.portfolio_price_start = 48000.00;
        coin.portfolio_value_start = 120000.00;
    }

    @Test
    void getInfoContainsKeyFields() {
        String info = coin.getInfo();
        assertTrue(info.contains("Rank: 1"));
        assertTrue(info.contains("ID: bitcoin"));
        assertTrue(info.contains("Name: Bitcoin"));
        assertTrue(info.contains("Symbol: BTC"));
        assertTrue(info.contains("Price USD"));
        assertTrue(info.contains("Market Cap:"));
        assertTrue(info.contains("24 Hour Volume:"));
        assertTrue(info.contains("Percent 24 Hour: 1.1%"));
    }

    @Test
    void getPortfolioContainsPortfolioFields() {
        String portfolio = coin.getPortfolio();
        assertTrue(portfolio.contains("Portfolio Amount"));
        assertTrue(portfolio.contains("Portfolio Value"));
        assertTrue(portfolio.contains("Portfolio Gains"));
        assertTrue(portfolio.contains("Portfolio Currency: USD"));
        assertTrue(portfolio.contains("Portfolio Price Start"));
    }

    @Test
    void getInfoUsesTrimPriceForPrice() {
        String info = coin.getInfo();
        assertTrue(info.contains("Price USD: 50000.12"));
    }

    @Test
    void getPortfolioFormatsNumbers() {
        String portfolio = coin.getPortfolio();
        assertTrue(portfolio.contains("125,000.55"));
        assertTrue(portfolio.contains("5,000.25"));
    }

    private static void setField(WebData.Coin target, String name, Object value) {
        try {
            var f = WebData.Coin.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}

