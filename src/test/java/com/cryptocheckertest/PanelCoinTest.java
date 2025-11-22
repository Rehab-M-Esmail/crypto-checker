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
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class PanelCoinTest {

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
}

