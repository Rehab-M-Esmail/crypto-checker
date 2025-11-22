package com.cryptocheckertest;
import com.cryptochecker.Debug;
import com.cryptochecker.Main;
import com.cryptochecker.WebData;
import org.junit.Test;
import java.io.File;
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
}
