package com.cryptocheckertest;

import com.cryptochecker.Main;
import com.cryptochecker.PanelConverter;
import com.cryptochecker.WebData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class PanelConverterTest_whiteBox {

    PanelConverter converter;

    @BeforeEach
    void setup() throws Exception {
        Main.gui = new Main();
        Main.currency = "USD";
        Main.converterSerLocation = System.getProperty("java.io.tmpdir") + "/converter_whitebox.ser";
        Main.gui.webData = new WebData();
        Main.theme = new Main.Theme(Main.themes.LIGHT);

        converter = new PanelConverter();

        // overwrite UI to avoid constructor NPEs in white-box paths
        converter.textBox1 = new JEditorPane();
        converter.textBox2 = new JEditorPane();
        converter.buttonCurrency1 = Main.gui.getButtonTemplate("");
        converter.buttonCurrency2 = Main.gui.getButtonTemplate("");
        converter.fieldCurrency1 = new JTextField("");
        converter.fieldCurrency2 = new JTextField("");
    }

    // ---------------- STATEMENT COVERAGE ----------------
    @Test
    void testCalculateCurrency_statementCoverage() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 5;
        converter.buttonCurrency2.setText("Bitcoin");

        String result = converter.calculateCurrency(2);
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    // ---------------- BRANCH COVERAGE ----------------
    @Test
    void testBranch_priceCurrency2IsZero() {
        converter.priceCurrency1 = 10;
        converter.priceCurrency2 = 0;
        converter.buttonCurrency2.setText(Main.currency);

        String result = converter.calculateCurrency(2);
        assertEquals("20", result); // 10 * 2
    }

    @Test
    void testBranch_priceCurrency1Zero() {
        converter.priceCurrency1 = 0;
        converter.priceCurrency2 = 5;
        converter.buttonCurrency2.setText("Bitcoin");

        String result = converter.calculateCurrency(10);
        assertEquals("0", result);
    }

    // ---------------- CONDITION COVERAGE ----------------
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
    }

    // ---------------- PATH COVERAGE (reCreate) ----------------
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

    // ---------------- EXCEPTION / DEFENSIVE PATH ----------------
    @Test
    void testRetrieveText_invalidBoxDoesNotCrash() {
        assertDoesNotThrow(() -> converter.retrieveText(99, "bad"));
    }

    // ---------------- SERIALIZATION PATH ----------------
    @Test
    void testSerializeAndDeserializePath() {
        converter.priceCurrency1 = 1.0;
        converter.priceCurrency2 = 2.0;

        converter.buttonCurrency1.setText("Bitcoin");
        converter.buttonCurrency2.setText(Main.currency);

        converter.fieldCurrency1.setText("1");
        converter.fieldCurrency2.setText("2");

        converter.infoCurrency1 = "info1";
        converter.infoCurrency2 = "info2";

        converter.serialize();

        File file = new File(Main.converterSerLocation);
        assertTrue(file.exists());

        converter.deserialize();

        assertEquals("Bitcoin", converter.buttonCurrency1.getText());

        file.delete();
    }
}
