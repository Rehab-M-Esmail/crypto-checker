package com.cryptocheckertest;
import com.cryptochecker.Debug;
import com.cryptochecker.Main;
import com.cryptochecker.WebData;
import org.junit.Test;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.Mockito.*;

public class WebDataTest {
    Debug debug = new Debug();
    WebData webData = new WebData();

    public WebDataTest() throws Exception {
    }

    @Test
    public void fetchJsonIncorrectURLTest() throws IOException, InterruptedException {
        String incorrectUrl = "https://notreal";
        assertThrows(IOException.class, ()->{
            webData.fetchJson(incorrectUrl);
        });
    }

    @Test
    public void fetchJsonCorrectURLTest() throws IOException, InterruptedException {
        String correctUrl = "https://httpbin.org/json";
        String json = webData.fetchJson(correctUrl);
        assertNotNull(json);
        assertFalse(json.isEmpty());
    }

    @Test
    public void fetchJsonThrows409()  {
        String url = "https://httpbin.org/status/409";
        assertThrows(IOException.class, ()->{
            webData.fetchJson(url);
        });
    }

    @Test
    public void deserializeFileExistsTest() throws Exception {
        WebData webData = spy(new WebData());
        Main.dataSerLocation = "valid.ser";
        File file = new File(Main.dataSerLocation);
        file.createNewFile();
        doNothing().when(webData).fetch();
        webData.deserialize();
        verify(webData, times(1)).fetch();
        file.delete();
    }
    @Test
    public void deserializeFileMissingTest() throws Exception { // this test case fails because deserialize is called in the webdata constructor. That's why the file existing is always true
        Main.dataSerLocation = "non_existing_file.ser";
        WebData webData = spy(new WebData());
        doNothing().when(webData).fetch();
        webData.deserialize();
        verify(webData, times(1)).fetch();
    }
    @Test
    public void trimPrice_Above1_ReturnsTwoDecimals(){
        WebData.Coin coin = webData.new Coin();
        assertEquals("1", coin.trimPrice(1));
        assertEquals("1.01", coin.trimPrice(1.01));
        assertEquals("2.56", coin.trimPrice(2.55893));
    }
    @Test
    public void trimPrice_0_1_To_1_ReturnsThreeDecimals(){
        WebData.Coin coin = webData.new Coin();
        assertEquals("0.1", coin.trimPrice(0.10001));
        assertEquals("0.0999", coin.trimPrice(0.0999));
        assertEquals("0.1", coin.trimPrice(0.1));
    }
    @Test
    public void trimPrice_0_01_To_0_1_ReturnsFourDecimals(){
        WebData.Coin coin = webData.new Coin();
        assertEquals("0.0099", coin.trimPrice(0.0099));
        assertEquals("0.0101", coin.trimPrice(0.0101));
        assertEquals("0.01", coin.trimPrice(0.01));
    }
    @Test
    public void trimPrice_0_001_To_0_01_ReturnsFiveDecimals() {
        WebData.Coin coin = webData.new Coin();
        assertEquals("0.00101", coin.trimPrice(0.00101));
        assertEquals("0.001", coin.trimPrice(0.001));
        assertEquals("0.00099", coin.trimPrice(0.00099));
    }
    @Test
    public void trimPrice_0_0001_To_0_001_ReturnsSixDecimals() {
        WebData.Coin coin = webData.new Coin();
        assertEquals("0.0001", coin.trimPrice(0.0001));
        assertEquals("0.000099", coin.trimPrice(0.000099));
        assertEquals("0.000101", coin.trimPrice(0.000101));
    }

    // fetch function test cases
    @Test
    public void fetchAllSuccessTest() throws Exception {
        WebData webData = spy(new WebData());
        doReturn("[{\"id\":\"bitcoin\"}]").when(webData).fetchJson(contains("coins/markets"));
        doReturn("{\"data\":{}}").when(webData).fetchJson(contains("global"));
        Main.dataSerLocation = "test.ser";
        webData.fetch();
        assertFalse(webData.coin.isEmpty());
        assertNotNull(webData.global_data);
    }

    @Test
    public void fetchFirstCallFailingTest() throws Exception {
        WebData webData = spy(new WebData());
        doThrow(new IOException("fail")).when(webData).fetchJson(anyString());
        webData.fetch();
        assertNotNull(webData.coin);
        assertNotNull(webData.global_data);
    }

    @Test
    public void fetchSecondCallFailingTest() throws Exception {
        WebData webData = spy(new WebData());
        doThrow(new IOException("Global fetch failed")).when(webData).fetchJson(contains("/global"));
        webData.fetch();
        assertNotNull(webData.coin);
        assertEquals(100, webData.coin.size());
        assertNotNull(webData.global_data);
    }
    @Test
    public void trimPriceVerySmall() {
        WebData.Coin coin = webData.new Coin();
        String price = coin.trimPrice(0.000000123456);
        assertTrue(price.startsWith("0.0000001"));
    }
    @Test
    public void getCoinReturnsCoinTest() {
        WebData.Coin coin = webData.getCoin();
        assertNotNull(coin);
    }
    @Test
    public void refreshCoinsExceptionTest() throws Exception {
        WebData wd = spy(new WebData());
        doThrow(new RuntimeException("fail")).when(wd).fetch();
        WebData.RefreshCoins rc = new WebData.RefreshCoins() {
            @Override
            public void run() {
                try {
                    wd.fetch();
                } catch (Exception ex) {
                    assertEquals("fail", ex.getMessage());
                }
            }
        };
        rc.run();
    }
    @Test
    public void globalDataToStringTest() {
        WebData.Global_Data gd = webData.new Global_Data();
        gd.total_market_cap = 1000000;
        gd.total_24h_volume = 50000;
        gd.bitcoin_percentage_of_market_cap = 60.5;
        gd.active_currencies = 2000;
        gd.active_assets = 3000;
        gd.active_markets = 4000;
        gd.last_updated = 1234567890;

        String s = gd.toString();
        assertTrue(s.contains("Total Market Cap"));
        assertTrue(s.contains("Bitcoin Dominance"));
        assertTrue(s.contains("Active Currencies"));
    }
    @Test
    public void coinGetInfoAndPortfolioTest() {
        WebData.Coin coin = webData.new Coin();
        coin.name = "Bitcoin";
        coin.price = 50000;
        coin.portfolio_amount = 2;
        coin.portfolio_value = 100000;
        coin.portfolio_gains = 2000;
        coin.portfolio_currency = "USD";
        coin.portfolio_price_start = 48000;
        coin.portfolio_value_start = 96000;
        String info = coin.getInfo();
        assertTrue(info.contains("Bitcoin"));
        assertTrue(info.contains("50000"));
        String portfolio = coin.getPortfolio();
        assertTrue(portfolio.contains("Portfolio Amount"));
        assertTrue(portfolio.contains("Portfolio Gains"));
        assertTrue(portfolio.contains("USD"));
    }
    @Test
    public void coinCloneAndCopyTest() throws Exception {
        WebData.Coin coin = webData.new Coin();
        coin.name = "TestCoin";
        WebData.Coin cloned = (WebData.Coin) coin.clone();
        assertEquals("TestCoin", cloned.name);
        Object copied = coin.copy();
        assertTrue(copied instanceof WebData.Coin);
        WebData.Coin copiedCoin = (WebData.Coin) copied;
        assertEquals("TestCoin", copiedCoin.name);
    }
}
