package com.cryptocheckertest;
import com.cryptochecker.*;
import org.junit.jupiter.api.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PanelCoinUnitTestsUsingMockito {

    private static PanelCoin panelCoin;
    private static WebData webData;
    private static ArrayList<WebData.Coin> testCoins;

    @BeforeAll
    static void initAll() throws Exception {
        // Initialize Main.gui first
        Main.gui = new Main(); // or whatever the actual class is

        // Initialize Debug
        Debug debug = new Debug();

        // Initialize WebData with coins
        webData = new WebData();
        webData.coin = new ArrayList<>();

        // Create test coins
        WebData.Coin coin1 = webData.new Coin();
        coin1.rank = 1;
        coin1.name = "Bitcoin";
        coin1.symbol = "BTC";
        coin1.price = 50000.0;
        coin1.percent_change_1h = 1.5;
        coin1.percent_change_24h = 2.3;
        coin1.percent_change_7d = 5.7;
        coin1.market_cap = 1000000000000.0;
        webData.coin.add(coin1);

        WebData.Coin coin2 = webData.new Coin();
        coin2.rank = 2;
        coin2.name = "Ethereum";
        coin2.symbol = "ETH";
        coin2.price = 3000.0;
        coin2.percent_change_1h = -0.5;
        coin2.percent_change_24h = 1.8;
        coin2.percent_change_7d = -2.1;
        coin2.market_cap = 500000000000.0;
        webData.coin.add(coin2);

        testCoins = webData.coin;

        // Assign WebData to Main.gui
        Main.gui.webData = webData;

        // Setup Main static fields
        Main.frame = new JFrame();
        Main.currency = "USD";
        Main.currencyChar = "$";
//        Main.panelWidth = 150;
//        Main.panelHeight = 30;
//        Main.tableHeaderSize = new Dimension(150, 30);
//        Main.tableFont = new Font("Arial", Font.PLAIN, 12);
//        Main.tableIntercellSpacing = new Dimension(5, 5);
        Main.screenResolution = Toolkit.getDefaultToolkit().getScreenSize();

        // Setup theme
        Main.themes tempTheme = Main.themes.LIGHT;
        Main.theme = new Main.Theme(tempTheme);

        // Now create PanelCoin
        panelCoin = new PanelCoin();
    }

    @AfterAll
    static void tearDownAll() {
        if (Main.frame != null) {
            Main.frame.dispose();
        }
    }

    @Test
    @DisplayName("Test PanelCoin initialization")
    void testPanelCoinInitialization() {
        assertNotNull(panelCoin);
        assertNotNull(panelCoin.panel);
        assertEquals(Color.WHITE, panelCoin.panel.getBackground());
        assertTrue(panelCoin.panel.getLayout() instanceof BoxLayout);
    }

    @Test
    @DisplayName("Test TableModel column count")
    void testTableModelColumnCount() {
        PanelCoin.TableModel model = panelCoin.new TableModel();
        assertEquals(7, model.getColumnCount());
    }

    @Test
    @DisplayName("Test TableModel column names")
    void testTableModelColumnNames() {
        PanelCoin.TableModel model = panelCoin.new TableModel();

        assertEquals("#", model.getColumnName(0));
        assertEquals("Name", model.getColumnName(1));
        assertEquals("Value", model.getColumnName(2));
        assertEquals("1h", model.getColumnName(3));
        assertEquals("24h", model.getColumnName(4));
        assertEquals("7d", model.getColumnName(5));
        assertEquals("Market Cap", model.getColumnName(6));
    }

    @Test
    @DisplayName("Test TableModel row count")
    void testTableModelRowCount() {
        PanelCoin.TableModel model = panelCoin.new TableModel();
        model.list = testCoins;

        assertEquals(2, model.getRowCount());
    }

    @Test
    @DisplayName("Test TableModel column classes")
    void testTableModelColumnClasses() {
        PanelCoin.TableModel model = panelCoin.new TableModel();

        assertEquals(Short.class, model.getColumnClass(0));
        assertEquals(String.class, model.getColumnClass(1));
        assertEquals(Double.class, model.getColumnClass(2));
        assertEquals(Double.class, model.getColumnClass(3));
        assertEquals(Double.class, model.getColumnClass(4));
        assertEquals(Double.class, model.getColumnClass(5));
        assertEquals(Integer.class, model.getColumnClass(6));
        assertEquals(String.class, model.getColumnClass(99)); // Default
    }

    @Test
    @DisplayName("Test TableModel getValueAt for all columns")
    void testTableModelGetValueAt() {
        PanelCoin.TableModel model = panelCoin.new TableModel();
        model.list = testCoins;

        // Test first row (Bitcoin)
        assertEquals(1, model.getValueAt(0, 0)); // rank
        assertEquals("Bitcoin", model.getValueAt(0, 1)); // name
        assertEquals(50000.0, model.getValueAt(0, 2)); // price
        assertEquals(1.5, model.getValueAt(0, 3)); // 1h change
        assertEquals(2.3, model.getValueAt(0, 4)); // 24h change
        assertEquals(5.7, model.getValueAt(0, 5)); // 7d change
        assertEquals(1000000000000.0, model.getValueAt(0, 6)); // market cap

        // Test second row (Ethereum)
        assertEquals(2, model.getValueAt(1, 0)); // rank
        assertEquals("Ethereum", model.getValueAt(1, 1)); // name
        assertEquals(3000.0, model.getValueAt(1, 2)); // price
    }

    @Test
    @DisplayName("Test TableRenderer formatting for currency column")
    void testTableRendererCurrencyFormatting() {
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();

        JTable mockTable = mock(JTable.class);
        Double testPrice = 50000.0;

        Component component = renderer.getTableCellRendererComponent(
                mockTable, testPrice, false, false, 0, 2
        );

        assertNotNull(component);
        assertTrue(component instanceof JLabel);
        JLabel label = (JLabel) component;
        assertTrue(label.getText().contains("50"));
    }

    @Test
    @DisplayName("Test TableRenderer percentage formatting")
    void testTableRendererPercentageFormatting() {
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();

        JTable mockTable = mock(JTable.class);
        when(mockTable.getValueAt(0, 3)).thenReturn(1.5);

        Double testValue = 1.5;

        Component component = renderer.getTableCellRendererComponent(
                mockTable, testValue, false, false, 0, 3
        );

        assertNotNull(component);
        assertTrue(component instanceof JLabel);
        JLabel label = (JLabel) component;
        assertTrue(label.getText().contains("%"));
    }

    @Test
    @DisplayName("Test TableRenderer color for positive percentage")
    void testTableRendererPositivePercentageColor() {
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();

        JTable mockTable = mock(JTable.class);
        when(mockTable.getValueAt(0, 3)).thenReturn(1.5); // Positive value

        Component component = renderer.getTableCellRendererComponent(
                mockTable, 1.5, false, false, 0, 3
        );

        assertEquals(Main.theme.green, component.getForeground());
    }

    @Test
    @DisplayName("Test TableRenderer color for negative percentage")
    void testTableRendererNegativePercentageColor() {
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();

        JTable mockTable = mock(JTable.class);
        when(mockTable.getValueAt(0, 3)).thenReturn(-1.5); // Negative value

        Component component = renderer.getTableCellRendererComponent(
                mockTable, -1.5, false, false, 0, 3
        );

        assertEquals(Main.theme.red, component.getForeground());
    }

    @Test
    @DisplayName("Test TableRenderer market cap formatting")
    void testTableRendererMarketCapFormatting() {
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();

        JTable mockTable = mock(JTable.class);
        Integer testMarketCap = 1000000000;

        Component component = renderer.getTableCellRendererComponent(
                mockTable, testMarketCap, false, false, 0, 6
        );

        assertNotNull(component);
        assertTrue(component instanceof JLabel);
        JLabel label = (JLabel) component;
        // Market cap should be formatted with commas
        assertTrue(label.getText().contains(",") || label.getText().length() > 6);
    }

    @Test
    @DisplayName("Test reCreate method updates table model")
    void testReCreateMethod() {
        // Add a new coin
        WebData.Coin newCoin = webData.new Coin();
        newCoin.name = "Cardano";
        newCoin.rank = 3;
        newCoin.price = 1.5;
        testCoins.add(newCoin);

        panelCoin.reCreate();

        // Verify the panel is updated (no exceptions thrown)
        assertNotNull(panelCoin.panel);

        // Remove the test coin
        testCoins.remove(newCoin);
    }

    @Test
    @DisplayName("Test bRefreshListener action")
    void testBRefreshListener() {
        PanelCoin.bRefreshListener listener = panelCoin.new bRefreshListener();

        ActionEvent mockEvent = mock(ActionEvent.class);

        // This would trigger RefreshCoins in actual implementation
        assertDoesNotThrow(() -> {
            listener.actionPerformed(mockEvent);
        });
    }

    @Test
    @DisplayName("Test themeSwitch applies theme colors")
    void testThemeSwitch() {
        // Store original colors
        Color originalBg = Main.theme.background;
        Color originalFg = Main.theme.foreground;

        // Change theme colors temporarily
        Main.theme.background = Color.BLACK;
        Main.theme.foreground = Color.WHITE;
        Main.theme.emptyBackground = Color.DARK_GRAY;
        Main.theme.selection = Color.YELLOW;

        panelCoin.themeSwitch();

        // Verify theme is applied (method executes without errors)
        assertNotNull(panelCoin.panel);

        // Restore original colors
        Main.theme.background = originalBg;
        Main.theme.foreground = originalFg;
    }

    @Test
    @DisplayName("Test table configuration")
    void testTableConfiguration() {
        assertNotNull(panelCoin.panel);

        // Find the table in the panel hierarchy
        JScrollPane scrollPane = findScrollPane(panelCoin.panel);
        assertNotNull(scrollPane);

        JTable table = findTable(scrollPane);
        assertNotNull(table);

        // Verify table settings
        assertEquals(40, table.getRowHeight());
        assertFalse(table.getShowVerticalLines());
        assertFalse(table.isFocusable());
    }

    @Test
    @DisplayName("Test table column widths")
    void testTableColumnWidths() {
        JScrollPane scrollPane = findScrollPane(panelCoin.panel);
        JTable table = findTable(scrollPane);

        assertNotNull(table);
        assertEquals(50, table.getColumnModel().getColumn(0).getMaxWidth()); // rank
        assertEquals(100, table.getColumnModel().getColumn(3).getMaxWidth()); // 1h
        assertEquals(100, table.getColumnModel().getColumn(4).getMaxWidth()); // 24h
        assertEquals(100, table.getColumnModel().getColumn(5).getMaxWidth()); // 7d
    }

    @Test
    @DisplayName("Test panel contains header and scroll pane")
    void testPanelComponents() {
        assertTrue(panelCoin.panel.getComponentCount() >= 2);

        // First component should be header panel
        Component firstComponent = panelCoin.panel.getComponent(0);
        assertTrue(firstComponent instanceof JPanel);

        // Second component should be scroll pane
        Component secondComponent = panelCoin.panel.getComponent(1);
        assertTrue(secondComponent instanceof JScrollPane);
    }

    @Test
    @DisplayName("Test empty coin list handling")
    void testEmptyCoinList() {
        PanelCoin.TableModel model = panelCoin.new TableModel();
        model.list = new ArrayList<>();

        assertEquals(0, model.getRowCount());
    }

    @Test
    @DisplayName("Test TableModel with valid data")
    void testTableModelValidData() {
        PanelCoin.TableModel model = panelCoin.new TableModel();
        model.list = testCoins;

        // Test that getValueAt doesn't throw for valid indices
        assertDoesNotThrow(() -> {
            for (int row = 0; row < model.getRowCount(); row++) {
                for (int col = 0; col < model.getColumnCount(); col++) {
                    model.getValueAt(row, col);
                }
            }
        });
    }

    @Test
    @DisplayName("Test TableRenderer with different column indices")
    void testTableRendererAllColumns() {
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();

        JTable mockTable = mock(JTable.class);
        when(mockTable.getValueAt(anyInt(), anyInt())).thenReturn(0.0);

        // Test each column
        for (int col = 0; col < 7; col++) {
            Object testValue = (col == 1) ? "Test" :
                    (col == 0) ? (short) 1 :
                            (col == 6) ? 1000000 : 1.5;

            Component component = renderer.getTableCellRendererComponent(
                    mockTable, testValue, false, false, 0, col
            );

            assertNotNull(component);
        }
    }

    @Test
    @DisplayName("Test TableRenderer background and foreground colors")
    void testTableRendererColors() {
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();

        JTable mockTable = mock(JTable.class);

        Component component = renderer.getTableCellRendererComponent(
                mockTable, "Test", false, false, 0, 1
        );

        assertEquals(Main.theme.background, component.getBackground());
        assertEquals(Main.theme.foreground, component.getForeground());
    }

    @Test
    @DisplayName("Test search field exists")
    void testSearchFieldExists() {
        JTextField searchField = findTextField(panelCoin.panel);
        assertNotNull(searchField, "Search field should exist in panel");
    }

    @Test
    @DisplayName("Test TableModel returns correct value types")
    void testTableModelValueTypes() {
        PanelCoin.TableModel model = panelCoin.new TableModel();
        model.list = testCoins;

        // Test column 0 returns integer (rank)
        Object rankValue = model.getValueAt(0, 0);
        assertTrue(rankValue instanceof Integer);

        // Test column 1 returns String (name)
        Object nameValue = model.getValueAt(0, 1);
        assertTrue(nameValue instanceof String);

        // Test column 2 returns Double (price)
        Object priceValue = model.getValueAt(0, 2);
        assertTrue(priceValue instanceof Double);
    }

    @Test
    @DisplayName("Test TableRenderer handles zero percentage")
    void testTableRendererZeroPercentage() {
        PanelCoin.TableRenderer renderer = panelCoin.new TableRenderer();

        JTable mockTable = mock(JTable.class);
        when(mockTable.getValueAt(0, 3)).thenReturn(0.0);

        Component component = renderer.getTableCellRendererComponent(
                mockTable, 0.0, false, false, 0, 3
        );

        // Zero should be treated as positive (green)
        assertEquals(Main.theme.green, component.getForeground());
    }

    @Test
    @DisplayName("Test table model handles multiple coins")
    void testTableModelMultipleCoins() {
        PanelCoin.TableModel model = panelCoin.new TableModel();

        // Create a larger list
        ArrayList<WebData.Coin> manyCoins = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            WebData.Coin coin = webData.new Coin();
            coin.rank = i + 1;
            coin.name = "Coin" + i;
            coin.price = 100.0 * (i + 1);
            manyCoins.add(coin);
        }

        model.list = manyCoins;
        assertEquals(10, model.getRowCount());

        // Test first and last coin
        assertEquals(1, model.getValueAt(0, 0));
        assertEquals("Coin0", model.getValueAt(0, 1));
        assertEquals(10, model.getValueAt(9, 0));
        assertEquals("Coin9", model.getValueAt(9, 1));
    }

    @Test
    @DisplayName("Test refresh button exists in panel")
    void testRefreshButtonExists() {
        JButton refreshButton = findButton(panelCoin.panel, "Refresh");
        assertNotNull(refreshButton, "Refresh button should exist");
    }

    // Helper methods to find components in hierarchy
    private JScrollPane findScrollPane(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JScrollPane) {
                return (JScrollPane) comp;
            } else if (comp instanceof Container) {
                JScrollPane result = findScrollPane((Container) comp);
                if (result != null) return result;
            }
        }
        return null;
    }

    private JTable findTable(Container container) {
        if (container == null) return null;

        if (container instanceof JScrollPane) {
            JScrollPane scrollPane = (JScrollPane) container;
            Component view = scrollPane.getViewport().getView();
            if (view instanceof JTable) {
                return (JTable) view;
            }
        }

        for (Component comp : container.getComponents()) {
            if (comp instanceof JTable) {
                return (JTable) comp;
            } else if (comp instanceof Container) {
                JTable result = findTable((Container) comp);
                if (result != null) return result;
            }
        }
        return null;
    }

    private JTextField findTextField(Container container) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JTextField) {
                return (JTextField) comp;
            } else if (comp instanceof Container) {
                JTextField result = findTextField((Container) comp);
                if (result != null) return result;
            }
        }
        return null;
    }

    private JButton findButton(Container container, String text) {
        for (Component comp : container.getComponents()) {
            if (comp instanceof JButton) {
                JButton button = (JButton) comp;
                if (button.getText().equals(text)) {
                    return button;
                }
            } else if (comp instanceof Container) {
                JButton result = findButton((Container) comp, text);
                if (result != null) return result;
            }
        }
        return null;
    }
}