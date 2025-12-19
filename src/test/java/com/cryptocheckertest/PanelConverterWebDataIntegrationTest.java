package com.cryptocheckertest;

import com.cryptochecker.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;

import java.nio.file.Path;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

class PanelConverterWebDataIntegrationTest {

    PanelConverter converter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        // 1. Setup Environment
        Main.currency = "USD";
        Main.converterSerLocation = tempDir.resolve("converter.ser").toString();
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.frame = new JFrame();
        Main.gui = new Main();

        Main.gui.webData = new WebData() {
            { coin = new ArrayList<>(); global_data = new Global_Data(); }
            @Override
            public void fetch() { }
            @Override
            public void deserialize() {  }
        };

        converter = new PanelConverter();
    }

    @Test
    void testIntegration_WebDataUpdates_ReflectInConverter() {

        // 1. Initially, WebData is empty. Converter should handle this safely.
        assertTrue(Main.gui.webData.coin.isEmpty());

        WebData.Coin coin1 = Main.gui.webData.new Coin();
        coin1.name = "Cardano";
        coin1.price = 2.50;
        coin1.id = "cardano";

        WebData.Coin coin2 = Main.gui.webData.new Coin();
        coin2.name = "Polkadot";
        coin2.price = 15.00;
        coin2.id = "polkadot";

        Main.gui.webData.coin.add(coin1);
        Main.gui.webData.coin.add(coin2);

        converter.buttonCurrency1.setText("Cardano");

        converter.reCreate();

        assertEquals(2.50, converter.priceCurrency1, "Converter should sync with WebData price");

        coin1.price = 3.00;

        converter.reCreate();

        assertEquals(3.00, converter.priceCurrency1, "Converter should react to WebData price changes");
    }
}