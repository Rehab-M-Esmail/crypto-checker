package com.cryptocheckertest;

import com.cryptochecker.Main;
import com.cryptochecker.WebData;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pitest-friendly unit tests for WebData.Coin.trimPrice() method
 * This method contains pure business logic with multiple conditional branches
 * that are perfect for mutation testing.
 */
public class WebDataCoinTrimPricePitTest {

    static {
        // Pitest often runs in a headless JVM. This project’s `Main` class has a static initializer
        // that calls `Toolkit.getDefaultToolkit().getScreenSize()` which can throw in headless mode.
        // Ensure headless is disabled BEFORE `Main` is initialized (we reference `Main.dataSerLocation`).
        System.setProperty("java.awt.headless", "false");
    }

    private WebData.Coin coin;
    private static File tempDataFile;

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        // Create a temporary serialized data file to prevent deserialization errors
        tempDataFile = File.createTempFile("test_data", ".ser");
        tempDataFile.deleteOnExit();
        Main.dataSerLocation = tempDataFile.getAbsolutePath();
        
        // Write minimal valid data
        try (FileOutputStream fos = new FileOutputStream(tempDataFile);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            // WebData.deserialize() reads: Global_Data first, then ArrayList<Coin>
            oos.writeObject(null);  // global_data
            oos.writeObject(new ArrayList<>());  // coin list
        }
    }

    @BeforeEach
    void setUp() throws Exception {
        // Use a stub to avoid deserialize/fetch (which touch Debug GUI)
        WebData webData = new WebData() {
            @Override
            public void deserialize() {
                // no-op
            }
        };
        coin = webData.new Coin();
    }

    @AfterAll
    static void tearDownAfterAll() {
        if (tempDataFile != null && tempDataFile.exists()) {
            tempDataFile.delete();
        }
    }

    // ===== BOUNDARY TESTS FOR EACH CONDITION =====

    @Test
    void testTrimPriceGreaterThan1() {
        // trimPrice > 1 -> format with 2 decimal places
        String result = coin.trimPrice(5.123456);
        assertEquals("5.12", result);
    }

    @Test
    void testTrimPriceExactly1() {
        // trimPrice == 1 (boundary)
        String result = coin.trimPrice(1.0);
        assertEquals("1", result);
    }

    @Test
    void testTrimPriceJustAbove1() {
        // trimPrice > 1 (just above boundary)
        String result = coin.trimPrice(1.001);
        assertEquals("1", result);
    }

    @Test
    void testTrimPriceGreaterThan0Point1() {
        // 0.1 < trimPrice <= 1 -> format with 3 decimal places
        String result = coin.trimPrice(0.5);
        assertEquals("0.5", result);
    }

    @Test
    void testTrimPriceExactly0Point1() {
        // trimPrice == 0.1 (boundary)
        String result = coin.trimPrice(0.1);
        assertEquals("0.1", result);
    }

    @Test
    void testTrimPriceJustAbove0Point1() {
        // trimPrice > 0.1 (just above boundary)
        String result = coin.trimPrice(0.101);
        assertEquals("0.101", result);
    }

    @Test
    void testTrimPriceGreaterThan0Point01() {
        // 0.01 < trimPrice <= 0.1 -> format with 4 decimal places
        String result = coin.trimPrice(0.05);
        assertEquals("0.05", result);
    }

    @Test
    void testTrimPriceExactly0Point01() {
        // trimPrice == 0.01 (boundary)
        String result = coin.trimPrice(0.01);
        assertEquals("0.01", result);
    }

    @Test
    void testTrimPriceJustAbove0Point01() {
        // trimPrice > 0.01 (just above boundary)
        String result = coin.trimPrice(0.0101);
        assertEquals("0.0101", result);
    }

    @Test
    void testTrimPriceGreaterThan0Point001() {
        // 0.001 < trimPrice <= 0.01 -> format with 5 decimal places
        String result = coin.trimPrice(0.005);
        assertEquals("0.005", result);
    }

    @Test
    void testTrimPriceExactly0Point001() {
        // trimPrice == 0.001 (boundary)
        String result = coin.trimPrice(0.001);
        assertEquals("0.001", result);
    }

    @Test
    void testTrimPriceJustAbove0Point001() {
        // trimPrice > 0.001 (just above boundary)
        String result = coin.trimPrice(0.00101);
        assertEquals("0.00101", result);
    }

    @Test
    void testTrimPriceGreaterThan0Point0001() {
        // 0.0001 < trimPrice <= 0.001 -> format with 6 decimal places
        String result = coin.trimPrice(0.0005);
        assertEquals("0.0005", result);
    }

    @Test
    void testTrimPriceExactly0Point0001() {
        // trimPrice == 0.0001 (boundary)
        String result = coin.trimPrice(0.0001);
        assertEquals("0.0001", result);
    }

    @Test
    void testTrimPriceJustAbove0Point0001() {
        // trimPrice > 0.0001 (just above boundary)
        String result = coin.trimPrice(0.000101);
        assertEquals("0.000101", result);
    }

    @Test
    void testTrimPriceLessThanOrEqual0Point0001() {
        // trimPrice <= 0.0001 -> format with 12 decimal places
        String result = coin.trimPrice(0.00005);
        assertEquals("0.00005", result);
    }

    @Test
    void testTrimPriceVerySmall() {
        // Very small value -> format with 12 decimal places
        String result = coin.trimPrice(0.000000123456789);
        assertEquals("0.000000123457", result);
    }

    @Test
    void testTrimPriceZero() {
        // Edge case: zero
        String result = coin.trimPrice(0.0);
        assertEquals("0", result);
    }

    // ===== CONDITIONAL BRANCH TESTS =====

    @Test
    void testFirstIfBranchExecutes() {
        // Test that first if branch (> 1) executes
        String result = coin.trimPrice(100.999);
        assertTrue(result.matches("\\d+\\.?\\d{0,2}"));
    }

    @Test
    void testSecondIfBranchExecutes() {
        // Test that second if branch (> 0.1) executes
        String result = coin.trimPrice(0.999);
        assertTrue(result.matches("0\\.\\d{1,3}"));
    }

    @Test
    void testThirdIfBranchExecutes() {
        // Test that third if branch (> 0.01) executes
        String result = coin.trimPrice(0.099);
        assertTrue(result.matches("0\\.\\d{1,4}"));
    }

    @Test
    void testFourthIfBranchExecutes() {
        // Test that fourth if branch (> 0.001) executes
        String result = coin.trimPrice(0.0099);
        assertTrue(result.matches("0\\.\\d{1,5}"));
    }

    @Test
    void testFifthIfBranchExecutes() {
        // Test that fifth if branch (> 0.0001) executes
        String result = coin.trimPrice(0.00099);
        assertTrue(result.matches("0\\.\\d{1,6}"));
    }

    @Test
    void testElseBranchExecutes() {
        // Test that else branch executes
        String result = coin.trimPrice(0.000099);
        assertTrue(result.matches("0\\.\\d+"));
    }

    // ===== REALISTIC CRYPTOCURRENCY PRICE TESTS =====

    @Test
    void testBitcoinPrice() {
        // Bitcoin: ~$50,000
        String result = coin.trimPrice(50000.00);
        assertEquals("50000", result);
    }

    @Test
    void testEthereumPrice() {
        // Ethereum: ~$3,000
        String result = coin.trimPrice(3000.50);
        assertEquals("3000.5", result);
    }

    @Test
    void testDogecoinPrice() {
        // Dogecoin: ~$0.08
        String result = coin.trimPrice(0.08);
        assertEquals("0.08", result);
    }

    @Test
    void testShibaInuPrice() {
        // Shiba Inu: ~$0.000008
        String result = coin.trimPrice(0.000008);
        assertEquals("0.000008", result);
    }

    @Test
    void testStablecoinPrice() {
        // USDT/USDC: ~$1.00
        String result = coin.trimPrice(1.00);
        assertEquals("1", result);
    }

    // ===== COMPARISON TESTS =====

    @Test
    void testLargerValueHasFewerDecimals() {
        String large = coin.trimPrice(100.123456789);
        String small = coin.trimPrice(0.000123456789);
        assertTrue(large.length() < small.length());
    }

    @Test
    void testDifferentBranchesProduceDifferentFormats() {
        String format1 = coin.trimPrice(10.0);      // 2 decimals max
        String format2 = coin.trimPrice(0.1);       // 3 decimals max
        String format3 = coin.trimPrice(0.01);      // 4 decimals max
        String format4 = coin.trimPrice(0.001);     // 5 decimals max
        String format5 = coin.trimPrice(0.0001);    // 6 decimals max
        String format6 = coin.trimPrice(0.00001);   // 12 decimals max
        
        assertNotNull(format1);
        assertNotNull(format2);
        assertNotNull(format3);
        assertNotNull(format4);
        assertNotNull(format5);
        assertNotNull(format6);
    }

    // ===== NEGATIVE VALUE TESTS =====

    @Test
    void testNegativeValueLarge() {
        // Negative values don't satisfy > 1, so they fall through to else
        String result = coin.trimPrice(-5.123);
        assertNotNull(result);
        assertTrue(result.startsWith("-"));
    }

    @Test
    void testNegativeValueSmall() {
        // Negative values fall through conditions
        String result = coin.trimPrice(-0.005);
        assertNotNull(result);
        assertTrue(result.startsWith("-"));
    }

    // ===== EXTREME VALUE TESTS =====

    @Test
    void testVeryLargeValue() {
        String result = coin.trimPrice(1000000.99);
        assertEquals("1000000.99", result);
    }

    @Test
    void testVerySmallPositiveValue() {
        String result = coin.trimPrice(0.000000000001);
        assertNotNull(result);
        assertTrue(result.startsWith("0."));
    }

    // ===== ROUNDING TESTS =====

    @Test
    void testRoundingUpForLargeValue() {
        String result = coin.trimPrice(5.999);
        assertEquals("6", result);
    }

    @Test
    void testRoundingDownForLargeValue() {
        String result = coin.trimPrice(5.123);
        assertEquals("5.12", result);
    }

    @Test
    void testRoundingForMediumValue() {
        String result = coin.trimPrice(0.1999);
        assertEquals("0.2", result);
    }

    @Test
    void testRoundingForSmallValue() {
        String result = coin.trimPrice(0.01999);
        assertEquals("0.02", result);
    }
}

