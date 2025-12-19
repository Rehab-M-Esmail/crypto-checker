package com.cryptocheckertest;

import com.cryptochecker.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.swing.*;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PanelConverterMockTest {

    PanelConverter converter;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setup() throws Exception {
        Main.currency = "USD";
        Main.converterSerLocation = tempDir.resolve("converter_mock.ser").toString();
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.frame = new JFrame();
        Main.gui = new Main();

        // Mock WebData
        Main.gui.webData = new WebData() {
            { coin = new ArrayList<>(); global_data = new Global_Data(); }
            public void fetch() {}
            public void deserialize() {}
        };

        // Add Dummy Data
        WebData.Coin btc = Main.gui.webData.new Coin();
        btc.name = "Bitcoin"; btc.price = 50000.0; btc.symbol = "BTC";
        Main.gui.webData.coin.add(btc);

        WebData.Coin eth = Main.gui.webData.new Coin();
        eth.name = "Ethereum"; eth.price = 3000.0; eth.symbol = "ETH";
        Main.gui.webData.coin.add(eth);

        converter = mock(PanelConverter.class);
        //This will be used to avoid NPE since the mock converter will be empty
        injectPrivateField("df1", new DecimalFormat("#.##"));
        injectPrivateField("df2", new DecimalFormat("#.###"));
        injectPrivateField("df3", new DecimalFormat("#.####"));
        injectPrivateField("df4", new DecimalFormat("#.#####"));
        injectPrivateField("df5", new DecimalFormat("#.######"));
        injectPrivateField("df6", new DecimalFormat("#.############"));

        converter.fieldCurrency1 = new JTextField("");
        converter.fieldCurrency2 = new JTextField("");
        converter.buttonCurrency1 = new JButton("Bitcoin");
        converter.buttonCurrency2 = new JButton("Ethereum");
        converter.textBox1 = new JEditorPane();
        converter.textBox2 = new JEditorPane();
        converter.overviewText = new JEditorPane();

        injectPrivateField("overview", new JPanel());
        injectPrivateField("contentFilling1", new JPanel());
        injectPrivateField("contentFilling2", new JPanel());
        injectPrivateField("contentTop", new JPanel());
        injectPrivateField("contentBottom", new JPanel());
        injectPrivateField("middleBottom", new JPanel());
    }

    @Test
    void testCalculateCurrency_CryptoToCrypto() {
        converter.priceCurrency1 = 100.0;
        converter.priceCurrency2 = 50.0;
        converter.buttonCurrency2.setText("SomeCoin");

        // Tell Mockito to run the REAL code for this method
        doCallRealMethod().when(converter).calculateCurrency(anyDouble());

        String result = converter.calculateCurrency(2.0);
        assertEquals("4", result);
    }

    @Test
    void testCalculateCurrency_CryptoToFiat() {
        converter.priceCurrency1 = 100.0;
        converter.priceCurrency2 = 0.0;
        converter.buttonCurrency2.setText(Main.currency); // "USD"

        doCallRealMethod().when(converter).calculateCurrency(anyDouble());

        String result = converter.calculateCurrency(2.0);

        assertEquals("200", result);
    }

    @Test
    void testRetrieveText() {
        doCallRealMethod().when(converter).retrieveText(anyInt(), anyString());
        converter.retrieveText(1, "New Info");

        assertEquals("New Info", converter.textBox1.getText());
        assertEquals("New Info", converter.infoCurrency1);
    }

    @Test
    void testSerialize_CreatesFile() {
        converter.fieldCurrency1.setText("10");
        converter.fieldCurrency2.setText("20");
        converter.priceCurrency1 = 100.0;
        converter.priceCurrency2 = 50.0;

        doCallRealMethod().when(converter).serialize();

        converter.serialize();

        File f = new File(Main.converterSerLocation);
        assertTrue(f.exists(), "Serialize should create the file");
    }

    @Test
    void testReCreate_UpdatesPrices() {
        Main.gui.webData.coin.get(0).price = 999.0;

        converter.buttonCurrency1.setText("Bitcoin");
        doNothing().when(converter).retrieveText(anyInt(), anyString());

        doCallRealMethod().when(converter).reCreate();

        converter.reCreate();

        assertEquals(999.0, converter.priceCurrency1);
    }

    @Test
    void testThemeSwitch_ColorsUpdated() {
        doCallRealMethod().when(converter).themeSwitch();

        try {
            Field method = PanelConverter.class.getDeclaredField("calculateGlobal");
            method.setAccessible(true);
        } catch (Exception e) {}

        converter.themeSwitch();

        JPanel contentTop = getPrivateField("contentTop", JPanel.class);
        assertEquals(Main.theme.emptyBackground, contentTop.getBackground());
    }

    @AfterEach
    void cleanup() {
        File f = new File(Main.converterSerLocation);
        if(f.exists()) f.delete();
    }

    private void injectPrivateField(String fieldName, Object value) throws Exception {
        Field field = PanelConverter.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(converter, value);
    }

    private <T> T getPrivateField(String fieldName, Class<T> type) {
        try {
            Field field = PanelConverter.class.getDeclaredField(fieldName);
            field.setAccessible(true);
            return type.cast(field.get(converter));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}