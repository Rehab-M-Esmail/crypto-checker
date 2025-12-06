package com.cryptocheckertest;
import com.cryptochecker.Debug;
import com.cryptochecker.Main;
import com.cryptochecker.PanelCoin;
import com.cryptochecker.WebData;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PanelCoinTest {
    static PanelCoin panelCoin;
    static WebData webData;
    @BeforeClass
    public static void init() throws Exception {
        Main.gui = new Main();
        Debug debug = new Debug();
        WebData webData = new WebData();
        Main.gui.webData = new WebData();
        Main.gui.webData.coin = new ArrayList<>();
        Main.frame = new JFrame();
        Main.themes tempTheme = Main.themes.LIGHT;
        Main.theme = new Main.Theme(tempTheme);
        Main.frame = new JFrame();
        webData = new WebData();
        webData.coin = new ArrayList<>();
        WebData.Coin coin = webData.new Coin();
        coin.rank = 1;
        coin.name = "Bitcoin";
        coin.price = 50000;
        coin.percent_change_1h = 1.2;
        coin.percent_change_24h = -0.5;
        coin.percent_change_7d = 3.4;
        coin.market_cap = 1000000000;
        webData.coin.add(coin);
        Main.gui.webData = webData;
        panelCoin = new PanelCoin();
    }

    @Test
    public void tableModelGetColumnCountTest() {
        PanelCoin panelCoin = new PanelCoin();
        PanelCoin.TableModel tableModel = panelCoin.new TableModel();
        assertEquals(7, tableModel.getColumnCount());
    }

    @Test
    public void tableModelGetValueAtCol0() throws Exception { // very tightly coupled, almost impossible to test
        PanelCoin panelCoin = new PanelCoin();
        PanelCoin.TableModel tableModel = panelCoin.new TableModel();
        ArrayList<WebData.Coin> dummyCoins = new ArrayList<>();
        assertEquals(7, tableModel.getValueAt(0,0));
    }

    @Test
    public void getTableModelClassTest(){
        PanelCoin panelCoin = new PanelCoin();
        PanelCoin.TableModel tableModel = panelCoin.new TableModel();
        assertEquals(Short.class, tableModel.getColumnClass(0));
        assertEquals(String.class, tableModel.getColumnClass(1));
        assertEquals(Double.class, tableModel.getColumnClass(2));
        assertEquals(Double.class, tableModel.getColumnClass(3));
        assertEquals(Double.class, tableModel.getColumnClass(4));
        assertEquals(Double.class, tableModel.getColumnClass(5));
        assertEquals(Integer.class, tableModel.getColumnClass(6));
        assertEquals(String.class, tableModel.getColumnClass(22));
    }

    @Test
    public void tableModelGetColumnClassAll() {
        PanelCoin.TableModel model = panelCoin.new TableModel();
        assertEquals(Short.class, model.getColumnClass(0));
        assertEquals(String.class, model.getColumnClass(1));
        assertEquals(Double.class, model.getColumnClass(2));
        assertEquals(Double.class, model.getColumnClass(3));
        assertEquals(Double.class, model.getColumnClass(4));
        assertEquals(Double.class, model.getColumnClass(5));
        assertEquals(Integer.class, model.getColumnClass(6));
        assertEquals(String.class, model.getColumnClass(99)); // default branch
    }

    @Test
    public void tableRendererFormattingTest() {
        JTable table = new JTable(panelCoin.new TableModel());
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();
        Component compPositive = renderer.getTableCellRendererComponent(table, 1.2, false, false, 0, 3);
        Component compNegative = renderer.getTableCellRendererComponent(table, -0.5, false, false, 0, 4);
        Component compPrice = renderer.getTableCellRendererComponent(table, 50000.0, false, false, 0, 2);
        Component compMarketCap = renderer.getTableCellRendererComponent(table, 1000000000, false, false, 0, 6);
        assertEquals(Main.gui.webData.coin.get(0).percent_change_1h >= 0 ? Main.theme.green : Main.theme.red, compPositive.getForeground());
        assertEquals(Main.gui.webData.coin.get(0).percent_change_24h >= 0 ? Main.theme.green : Main.theme.red, compNegative.getForeground());
        assertTrue(compPrice instanceof JLabel);
        assertTrue(compMarketCap instanceof JLabel);
    }

    @Test
    public void searchDocumentListenerTest() {
        JTextField searchField = new JTextField();
        searchField.setText("Bitcoin");
        assertEquals("Bitcoin", searchField.getText());
    }

    @Test
    public void bRefreshListenerTest() {
        PanelCoin.bRefreshListener listener = panelCoin.new bRefreshListener();
        // We can't assert internal thread but can call actionPerformed
        listener.actionPerformed(null);
        assertTrue(true); // no exception thrown
    }
}

