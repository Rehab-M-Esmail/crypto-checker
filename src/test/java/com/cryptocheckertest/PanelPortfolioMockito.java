package com.cryptocheckertest;

import com.cryptochecker.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PanelPortfolioMockito {

    private static Main testMain;
    private static WebData webData;
    private PanelPortfolio panelPortfolio;

    @Mock
    private JButton mockButton;
    @Mock
    private ActionEvent mockActionEvent;

    @BeforeAll
    void setUpBeforeAll() throws Exception {
        new File(Main.folderLocation).mkdirs();
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        Main.currencyChar = "$";
        Debug.mode = true;

        testMain = new Main();
        Main.gui = testMain;
        Main.frame = new JFrame("Test Frame");
        Main.frame.setVisible(false);

        new Debug();
        webData = new WebData();
        testMain.webData = webData;

        // Create test coins
        webData.coin = new ArrayList<>();
        WebData.Coin coin1 = webData.getCoin();
        coin1.name = "Bitcoin";
        coin1.price = 50000.0;
        coin1.percent_change_1h = 1.5;
        webData.coin.add(coin1);

        if (webData.portfolio == null)
            webData.portfolio = new ArrayList<>();
        if (webData.portfolio.isEmpty())
            webData.portfolio.add(new ArrayList<>());
        if (webData.portfolio_names == null)
            webData.portfolio_names = new ArrayList<>();
        if (webData.portfolio_names.isEmpty())
            webData.portfolio_names.add("Portfolio 1");
    }

    @BeforeEach
    void setUp() {
        Main.theme = new Main.Theme(Main.themes.LIGHT);
        Main.currency = "USD";
        if (webData.portfolio.get(0) != null)
            webData.portfolio.get(0).clear();
    }

    @AfterAll
    void tearDownAfterAll() {
        if (Main.frame != null)
            Main.frame.dispose();
        if (Debug.frame != null)
            Debug.frame.dispose();
    }

    // ===== UNIT TESTS - Panel Initialization =====

    @Test
    @Order(1)
    @DisplayName("UT-PORTFOLIO-001: Panel initialization")
    void testPanelInitialization() {
        panelPortfolio = new PanelPortfolio();
        assertNotNull(panelPortfolio.panel);
        assertFalse(panelPortfolio.panel.isVisible());
        assertTrue(panelPortfolio.panel.getLayout() instanceof BoxLayout);
    }

    @Test
    @Order(2)
    @DisplayName("UT-PORTFOLIO-002: WebData reference")
    void testWebDataReference() {
        panelPortfolio = new PanelPortfolio();
        assertSame(Main.gui.webData, panelPortfolio.webData);
    }

    // ===== UNIT TESTS - Currency Converter with Mocking =====

    @Test
    @Order(10)
    @DisplayName("UT-CURRENCY-001: Gains calculation positive")
    void testGainsCalculationPositive() {
        WebData.Coin testCoin = webData.getCoin();
        testCoin.name = "Bitcoin";
        testCoin.portfolio_amount = 1.0;
        testCoin.portfolio_price_start = 40000.0;
        testCoin.portfolio_value_start = 40000.0;
        testCoin.portfolio_currency = "USD";
        testCoin.price = 50000.0;
        webData.portfolio.get(0).add(testCoin);

        panelPortfolio = new PanelPortfolio();
        panelPortfolio.refreshPortfolio();

        assertEquals(10000.0, webData.portfolio.get(0).get(0).portfolio_gains, 0.01);
    }

    @Test
    @Order(11)
    @DisplayName("UT-CURRENCY-002: Gains calculation negative")
    void testGainsCalculationNegative() {
        WebData.Coin testCoin = webData.getCoin();
        testCoin.name = "Bitcoin";
        testCoin.portfolio_amount = 1.0;
        testCoin.portfolio_price_start = 60000.0;
        testCoin.portfolio_value_start = 60000.0;
        testCoin.portfolio_currency = "USD";
        testCoin.price = 50000.0;
        webData.portfolio.get(0).add(testCoin);

        panelPortfolio = new PanelPortfolio();
        panelPortfolio.refreshPortfolio();

        assertEquals(-10000.0, webData.portfolio.get(0).get(0).portfolio_gains, 0.01);
    }

    @Test
    @Order(12)
    @DisplayName("UT-CURRENCY-003: Mock currency label")
    void testMockCurrencyLabel() {
        JLabel mockLabel = mock(JLabel.class);
        when(mockLabel.getText()).thenReturn("$50,000.00");
        assertEquals("$50,000.00", mockLabel.getText());
        verify(mockLabel).getText();
    }

    // ===== UNIT TESTS - API Parser =====

    @Test
    @Order(20)
    @DisplayName("UT-PARSER-001: Coin clone functionality")
    void testCoinClone() throws Exception {
        WebData.Coin original = webData.getCoin();
        original.name = "Litecoin";
        original.price = 150.0;

        WebData.Coin cloned = (WebData.Coin) original.clone();
        assertEquals(original.name, cloned.name);
        assertNotSame(original, cloned);
    }

    @Test
    @Order(21)
    @DisplayName("UT-PARSER-002: Price trimming")
    void testPriceTrimming() {
        WebData.Coin coin = webData.getCoin();
        assertEquals("100", coin.trimPrice(100.0));
        assertEquals("0.5", coin.trimPrice(0.5));
    }

    // ===== UNIT TESTS - Mocking with Stubbing =====

    @Test
    @Order(30)
    @DisplayName("UT-MOCK-001: ActionEvent stubbing")
    void testActionEventStubbing() {
        ActionEvent mockEvent = mock(ActionEvent.class);
        JButton mockSource = mock(JButton.class);
        when(mockEvent.getSource()).thenReturn(mockSource);
        when(mockSource.getText()).thenReturn("Refresh");

        assertEquals("Refresh", ((JButton) mockEvent.getSource()).getText());
        verify(mockEvent).getSource();
    }

    @Test
    @Order(31)
    @DisplayName("UT-MOCK-002: Sequential stubbing")
    void testSequentialStubbing() {
        JButton btn = mock(JButton.class);
        when(btn.getText()).thenReturn("First").thenReturn("Second");

        assertEquals("First", btn.getText());
        assertEquals("Second", btn.getText());
    }

    // ===== UNIT TESTS - Theme Switching =====

    @Test
    @Order(40)
    @DisplayName("UT-THEME-001: Theme switch to DARK")
    void testThemeSwitchToDark() {
        panelPortfolio = new PanelPortfolio();
        Main.theme.change(Main.themes.DARK);
        panelPortfolio.themeSwitch();
        assertEquals(Color.WHITE, Main.theme.foreground);
    }

    // ===== INTEGRATION TESTS - Data ↔ Converter =====

    @Test
    @Order(50)
    @DisplayName("IT-DATA-001: WebData shares portfolio")
    void testWebDataSharesPortfolio() {
        panelPortfolio = new PanelPortfolio();
        assertSame(Main.gui.webData.portfolio, panelPortfolio.webData.portfolio);
    }

    @Test
    @Order(51)
    @DisplayName("IT-DATA-002: Changes reflect in PanelPortfolio")
    void testChangesReflect() {
        panelPortfolio = new PanelPortfolio();
        WebData.Coin testCoin = webData.getCoin();
        testCoin.name = "TestCoin";
        Main.gui.webData.portfolio.get(0).add(testCoin);

        boolean found = panelPortfolio.webData.portfolio.get(0).stream()
                .anyMatch(c -> c.name.equals("TestCoin"));
        assertTrue(found);
    }

    @Test
    @Order(52)
    @DisplayName("IT-DATA-003: refreshPortfolio updates price")
    void testRefreshUpdatesPrice() {
        WebData.Coin coinInList = webData.getCoin();
        coinInList.name = "SyncCoin";
        coinInList.price = 1000.0;
        webData.coin.add(coinInList);

        WebData.Coin portfolioCoin = webData.getCoin();
        portfolioCoin.name = "SyncCoin";
        portfolioCoin.portfolio_amount = 1.0;
        portfolioCoin.portfolio_currency = "USD";
        portfolioCoin.price = 500.0;
        webData.portfolio.get(0).add(portfolioCoin);

        panelPortfolio = new PanelPortfolio();
        panelPortfolio.refreshPortfolio();

        assertEquals(1000.0, webData.portfolio.get(0).get(0).price, 0.01);
        webData.coin.remove(coinInList);
    }

    // ===== TEST-DRIVEN DEBUGGING =====

    @Test
    @Order(60)
    @DisplayName("TDD-DEBUG-001: Validate portfolio calculation")
    void testValidatePortfolioCalculation() {
        // First add coin to webData.coin list (refreshPortfolio looks up price from
        // here)
        WebData.Coin coinInList = webData.getCoin();
        coinInList.name = "DebugCoin";
        coinInList.price = 150.0; // Current market price
        webData.coin.add(coinInList);

        // Now add to portfolio
        WebData.Coin testCoin = webData.getCoin();
        testCoin.name = "DebugCoin";
        testCoin.portfolio_amount = 3.0;
        testCoin.portfolio_price_start = 100.0;
        testCoin.portfolio_value_start = 300.0;
        testCoin.portfolio_currency = "USD";
        testCoin.price = 100.0; // Old price when bought
        webData.portfolio.get(0).add(testCoin);

        panelPortfolio = new PanelPortfolio();
        panelPortfolio.refreshPortfolio();

        WebData.Coin result = webData.portfolio.get(0).get(0);
        // Debug: portfolio_value = coin.price * amount = 150 * 3 = 450
        assertEquals(450.0, result.portfolio_value, 0.01);
        // Debug: gains = value - start_value = 450 - 300 = 150
        assertEquals(150.0, result.portfolio_gains, 0.01);

        // Cleanup
        webData.coin.remove(coinInList);
    }

    @Test
    @Order(61)
    @DisplayName("TDD-DEBUG-002: Validate data consistency")
    void testValidateDataConsistency() {
        WebData.Coin testCoin = webData.getCoin();
        testCoin.name = "ConsistencyCoin";
        testCoin.portfolio_amount = 5.0;
        webData.portfolio.get(0).add(testCoin);

        panelPortfolio = new PanelPortfolio();
        int sizeBefore = webData.portfolio.get(0).size();
        panelPortfolio.refreshPortfolio();
        int sizeAfter = webData.portfolio.get(0).size();

        assertEquals(sizeBefore, sizeAfter, "Size should remain constant");
    }

    // ===== NEGATIVE TESTS =====

    @Test
    @Order(70)
    @DisplayName("NEG-001: Empty portfolio handling")
    void testEmptyPortfolio() {
        webData.portfolio.set(0, new ArrayList<>());
        panelPortfolio = new PanelPortfolio();
        assertDoesNotThrow(() -> panelPortfolio.calculatePortfolio());
    }

    @Test
    @Order(71)
    @DisplayName("NEG-002: Zero price handling")
    void testZeroPrice() {
        WebData.Coin testCoin = webData.getCoin();
        testCoin.name = "ZeroCoin";
        testCoin.price = 0.0;
        testCoin.portfolio_amount = 10.0;
        testCoin.portfolio_currency = "USD";
        webData.portfolio.get(0).add(testCoin);

        panelPortfolio = new PanelPortfolio();
        assertDoesNotThrow(() -> panelPortfolio.refreshPortfolio());
    }

    @Test
    @Order(72)
    @DisplayName("NEG-003: findPortfolioName with empty")
    void testFindPortfolioNameEmpty() throws Exception {
        webData.portfolio.set(0, new ArrayList<>());
        panelPortfolio = new PanelPortfolio();

        Method findMethod = PanelPortfolio.class.getDeclaredMethod("findPortfolioName", String.class);
        findMethod.setAccessible(true);
        boolean result = (boolean) findMethod.invoke(panelPortfolio, "NonExistent");

        assertFalse(result);
    }
}
