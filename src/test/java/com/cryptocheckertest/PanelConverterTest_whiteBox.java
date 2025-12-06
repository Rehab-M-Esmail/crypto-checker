package com.cryptocheckertest;

import com.cryptochecker.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PanelConverterTest_whiteBox {

    PanelConverter converter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        Debug.contentPane = new JScrollPane();

        Main.currency = "USD";
        Main.converterSerLocation = tempDir.resolve("converter_whitebox.ser").toString();
        Main.dataSerLocation = tempDir.resolve("data_whitebox.ser").toString();
        Main.theme = new Main.Theme(Main.themes.LIGHT);

        Main.frame = new JFrame();
        Main.gui = new Main();

        Main.gui.webData = new WebData() {
            {
                coin = new ArrayList<>();
                global_data = new Global_Data();
            }

            @Override
            public void fetch() throws Exception {
            }

            @Override
            public void deserialize() throws Exception {
            }
        };

        WebData.Coin bitcoin = Main.gui.webData.new Coin();
        bitcoin.name = "Bitcoin";
        bitcoin.price = 50000.0;
        bitcoin.market_cap = 1000000000000.0;
        bitcoin._24h_volume = 50000000000.0;
        bitcoin.symbol = "BTC";
        bitcoin.rank = 1;
        bitcoin.id = "bitcoin";

        WebData.Coin ethereum = Main.gui.webData.new Coin();
        ethereum.name = "Ethereum";
        ethereum.price = 3000.0;
        ethereum.market_cap = 400000000000.0;
        ethereum._24h_volume = 20000000000.0;
        ethereum.symbol = "ETH";
        ethereum.rank = 2;
        ethereum.id = "ethereum";

        Main.gui.webData.coin.add(bitcoin);
        Main.gui.webData.coin.add(ethereum);

        Main.gui.webData.global_data.total_market_cap = 2000000000000L;
        Main.gui.webData.global_data.total_24h_volume = 50000000000L;
        Main.gui.webData.global_data.bitcoin_percentage_of_market_cap = 45.5;

        converter = new PanelConverter();

    }

    @Test
    void testCalculateCurrency_statementCoverage() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 5;
        converter.buttonCurrency2.setText("Bitcoin");

        String result = converter.calculateCurrency(2);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testBranch_priceCurrency2IsZero() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 0;
        converter.buttonCurrency2.setText(Main.currency);

        String result = converter.calculateCurrency(2);
        assertEquals("20", result);
    }

    @Test
    void testBranch_priceCurrency1Zero() {
        converter.priceCurrency1 = 0;
        converter.priceCurrency2 = 5;
        converter.buttonCurrency2.setText("Bitcoin");

        String result = converter.calculateCurrency(10);
        assertEquals("0", result);
    }

    @Test
    void testBranch_bothPricesZero() {
        converter.priceCurrency1 = 0;
        converter.priceCurrency2 = 0;
        converter.buttonCurrency2.setText("Ethereum");

        String result = converter.calculateCurrency(10);
        assertEquals("0", result);
    }

    @Test
    void testConditionFormattingPaths() {
        converter.priceCurrency1 = 1;
        converter.priceCurrency2 = 2;
        converter.buttonCurrency2.setText("Ethereum");

        assertNotNull(converter.calculateCurrency(2));      // > 1
        assertNotNull(converter.calculateCurrency(0.2));    // > 0.1
        assertNotNull(converter.calculateCurrency(0.02));   // > 0.01
        assertNotNull(converter.calculateCurrency(0.002));  // > 0.001
        assertNotNull(converter.calculateCurrency(0.0002)); // > 0.0001
        assertNotNull(converter.calculateCurrency(0.00001)); // <= 0.0001
    }
    @Test
    void testReCreate_whenCurrency2IsFiat() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 0;
        converter.buttonCurrency1.setText("Bitcoin");
        converter.buttonCurrency2.setText(Main.currency);

        assertDoesNotThrow(() -> converter.reCreate());
    }

    @Test
    void testReCreate_whenBothCryptosSelected() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 20;
        converter.buttonCurrency1.setText("Bitcoin");
        converter.buttonCurrency2.setText("Ethereum");

        assertDoesNotThrow(() -> converter.reCreate());
    }

    @Test
    void testReCreate_whenCoinNotFound() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 20;
        converter.buttonCurrency1.setText("NonExistentCoin");
        converter.buttonCurrency2.setText("AnotherNonExistentCoin");

        assertDoesNotThrow(() -> converter.reCreate());
    }



    @Test
    void testDeserialize_whenFileDoesNotExist() {
        // Ensure file doesn't exist
        File file = new File(Main.converterSerLocation);
        if (file.exists()) {
            file.delete();
        }

        assertDoesNotThrow(() -> converter.deserialize());
    }


    @Test
    void testDocumentListener_invalidInput() {
        converter.fieldCurrency1.setText("abc");

        assertEquals("", converter.fieldCurrency2.getText());
    }

    @Test
    void testDocumentListener_emptyInput() {
        converter.fieldCurrency1.setText("");

        assertEquals("", converter.fieldCurrency2.getText());
    }

    @Test
    void testDocumentListener_decimalInput() {
        converter.fieldCurrency1.setText("10.5");

        String field2Text = converter.fieldCurrency2.getText();
        assertNotNull(field2Text);
        assertFalse(field2Text.isEmpty());
    }

    @Test
    void testThemeSwitch() {
        assertDoesNotThrow(() -> converter.themeSwitch());
    }


    @Test
    void testButtonCurrencyListener_selection() {
        converter.buttonCurrency1.setText("Test");
        converter.buttonCurrency2.setText("Test2");

        assertDoesNotThrow(() -> {
            converter.buttonCurrency1.setText("Changed");
            converter.buttonCurrency2.setText("Changed2");
        });
    }

    @Test
    void testCalculateCurrency_negativeInput() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 5;
        converter.buttonCurrency2.setText("Bitcoin");

        String result = converter.calculateCurrency(-2);
        assertNotNull(result);
        assertTrue(result.startsWith("-"));
    }




    @Test
    void testConstructor_setsInitialVisibility() {
        assertFalse(converter.panel.isVisible());
    }

    @AfterEach
    void cleanup() {
        File converterFile = new File(Main.converterSerLocation);
        if (converterFile.exists()) {
            converterFile.delete();
        }

        File dataFile = new File(Main.dataSerLocation);
        if (dataFile.exists()) {
            dataFile.delete();
        }
    }
}