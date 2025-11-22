package com.cryptochecker;

import com.cryptochecker.Main;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import javax.swing.*;
import java.io.File;

class PanelConverterTest {

    PanelConverter converter;

    static class WebDataMock {
        java.util.ArrayList<CoinMock> coin = new java.util.ArrayList<>();

        WebDataMock() {
            coin.add(new CoinMock("Bitcoin", 10000.0));
            coin.add(new CoinMock("Ethereum", 2000.0));
        }

        static class CoinMock {
            String name;
            double price;

            CoinMock(String name, double price) {
                this.name = name;
                this.price = price;
            }

            String getInfo() { return name + ": $" + price; }
            public String toString() { return name; }
        }
    }

    @BeforeEach
    void setup() throws Exception {
        Main.gui = new Main();
        Main.currency = "USD";
        Main.converterSerLocation = System.getProperty("java.io.tmpdir") + "/converter.ser";
        Main.gui.webData = new WebData();
        Main.theme = new Main.Theme(Main.themes.LIGHT);

        converter = new PanelConverter();
        converter.textBox1 = new JEditorPane();
        converter.textBox2 = new JEditorPane();
        converter.buttonCurrency1 = Main.gui.getButtonTemplate("");
        converter.buttonCurrency2 = Main.gui.getButtonTemplate("");
    }

    @Test
    void testCalculateCurrency() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 0;
        converter.buttonCurrency2.setText(Main.currency);
        assertEquals("20", converter.calculateCurrency(2));

        converter.priceCurrency1 = 0;
        converter.priceCurrency2 = 5;
        converter.buttonCurrency2.setText("Bitcoin");
        assertEquals("0", converter.calculateCurrency(1));

        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 5;
        converter.buttonCurrency2.setText("Ethereum");
        assertEquals("4", converter.calculateCurrency(2));
    }

    @Test
    void testRetrieveText() {
        converter.retrieveText(1, "Info1");
        converter.retrieveText(2, "Info2");
        assertEquals("Info1", converter.textBox1.getText());
        assertEquals("Info2", converter.textBox2.getText());
    }

    @Test
    void testSerializeCreatesFile() {
        converter.priceCurrency1 = 1.0;
        converter.priceCurrency2 = 1.0;
        converter.buttonCurrency1.setText("Bitcoin");
        converter.buttonCurrency2.setText(Main.currency);
        converter.fieldCurrency1 = new JTextField("1");
        converter.fieldCurrency2 = new JTextField("1");
        converter.infoCurrency1 = "info1";
        converter.infoCurrency2 = "info2";

        converter.serialize();

        File file = new File(Main.converterSerLocation);
        assertTrue(file.exists());
        file.delete();
    }

}
