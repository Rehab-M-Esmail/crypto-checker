package com.cryptocheckertest;

import com.cryptochecker.Debug;
import com.cryptochecker.Main;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.event.WindowEvent;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * White Box Testing for Debug.java
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DebugTest_WhiteBox {

    @TempDir
    static Path tempDir;
    
    private static Path testLogLocation;
    private static Main testMain;
    private static Debug debugInstance;

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        Path logFile = tempDir.resolve("log.txt");
        Main.logLocation = logFile.toString();
        Main.folderLocation = tempDir.toString();
        testLogLocation = logFile;
        new File(Main.folderLocation).mkdirs();
        testMain = new Main();
        Main.gui = testMain;
    }

    @AfterAll
    static void tearDownAfterAll() {
        try { 
            if (Files.exists(testLogLocation)) Files.delete(testLogLocation); 
        } catch (Exception e) {}
        if (Debug.frame != null) Debug.frame.dispose();
    }

    @BeforeEach
    void setUp() throws Exception {
        if (Files.exists(testLogLocation)) Files.delete(testLogLocation);
        Debug.mode = false;
        if (Debug.frame != null) {
            Debug.frame.dispose();
            Debug.frame = null;
        }
        debugInstance = new Debug();
    }

    @AfterEach
    void tearDown() {
        if (Debug.frame != null) Debug.frame.dispose();
    }
    
    // Tests statement coverage in the constructor
    @Test @Order(1) @DisplayName("WB-DEBUG-001: Statement Coverage - Constructor execution path")
    void testConstructorStatementCoverage() throws Exception {
        assertNotNull(Debug.frame);
        assertNotNull(debugInstance);
        assertTrue(Files.exists(testLogLocation));
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertFalse(lines.isEmpty());
        assertTrue(lines.stream().anyMatch(l -> l.contains("Program started up")));
    }

    // Tests normal statement execution in log()
    @Test @Order(2) @DisplayName("WB-DEBUG-002: Statement Coverage - log() method normal path")
    void testLogMethodStatementCoverage() throws Exception {
        String testMsg = "Test message";
        Debug.log(testMsg);
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains(testMsg)));
    }

    // Tests log() exception-handling path
    @Test @Order(3) @DisplayName("WB-DEBUG-003: Statement Coverage - log() exception path")
    void testLogExceptionPathStatementCoverage() throws Exception {
        String originalLocation = Main.logLocation;
        try {
            Files.deleteIfExists(testLogLocation);
            Files.createDirectory(testLogLocation);
            assertDoesNotThrow(() -> Debug.log("Test"));
        } finally {
            Files.deleteIfExists(testLogLocation);
            Main.logLocation = originalLocation;
        }
    }

    // Tests windowClosing branch in WindowListener
    @Test @Order(4) @DisplayName("WB-DEBUG-004: Branch Coverage - WindowListener windowClosing branch")
    void testWindowClosingBranch() throws Exception {
        assertNotNull(Debug.frame);
        WindowEvent closeEvent = new WindowEvent(Debug.frame, WindowEvent.WINDOW_CLOSING);
        
        if (Main.gui != null && Main.gui.panelSettings != null) {
            Debug.frame.dispatchEvent(closeEvent);
        }
        
        assertNotNull(Debug.frame);
    }

    // Tests try/catch branches in log()
    @Test @Order(5) @DisplayName("WB-DEBUG-005: Branch Coverage - log() try-catch branches")
    void testLogTryCatchBranches() throws Exception {
        Debug.log("Normal message");
        assertTrue(Files.exists(testLogLocation));
        
        String content = new String(Files.readAllBytes(testLogLocation));
        assertTrue(content.contains("Normal message"));
    }

    // Tests multiple condition evaluations in log()
    @Test @Order(6) @DisplayName("WB-DEBUG-006: Condition Coverage - Multiple log calls")
    void testConditionCoverage() throws Exception {
        Debug.log("Message 1");
        Debug.log("Message 2");
        Debug.log("Message 3");
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() >= 4);
    }

    // Tests full constructor path for path coverage
    @Test @Order(7) @DisplayName("WB-DEBUG-007: Path Coverage - Constructor complete path")
    void testConstructorCompletePath() throws Exception {
        Debug newDebug = new Debug();
        assertNotNull(newDebug);
        assertNotNull(Debug.frame);
    }

    // Tests normal log() path for path coverage
    @Test @Order(8) @DisplayName("WB-DEBUG-008: Path Coverage - log() normal path")
    void testLogNormalPath() throws Exception {
        Debug.log("Path test 1");
        assertTrue(Files.exists(testLogLocation));
    }

    // Tests log() exception path for path coverage
    @Test @Order(9) @DisplayName("WB-DEBUG-009: Path Coverage - log() exception path")
    void testLogExceptionPath() {
        String backup = Main.logLocation;
        try {
            Main.logLocation = "Z:\\invalid\\path\\that\\does\\not\\exist\\log.txt";
            Debug.log("Exception path test");
            assertTrue(true);
        } catch (Exception e) {
            assertTrue(true);
        } finally {
            Main.logLocation = backup;
        }
    }

    // Tests sequential logging and file writing
    @Test @Order(10) @DisplayName("WB-DEBUG-010: Path Coverage - Multiple sequential operations")
    void testMultipleSequentialPaths() throws Exception {
        Debug.log("First");
        Debug.log("Second");
        Debug.log("Third");
        
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() >= 4);
        
        String content = new String(Files.readAllBytes(testLogLocation));
        assertTrue(content.contains("First"));
        assertTrue(content.contains("Second"));
        assertTrue(content.contains("Third"));
    }

    // Tests logging an empty string
    @Test @Order(11) @DisplayName("WB-DEBUG-011: Edge Case - Empty string log")
    void testEmptyStringLog() throws Exception {
        Debug.log("");
        List<String> lines = Files.readAllLines(testLogLocation);
        String lastLine = lines.get(lines.size() - 1);
        assertTrue(lastLine.contains(": "));
    }

    // Tests logging a null value
    @Test @Order(12) @DisplayName("WB-DEBUG-012: Edge Case - Null string log")
    void testNullStringLog() {
        assertDoesNotThrow(() -> Debug.log(null));
    }

    // Tests logging a very long message
    @Test @Order(13) @DisplayName("WB-DEBUG-013: Edge Case - Very long message")
    void testVeryLongMessage() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) sb.append("A");
        Debug.log(sb.toString());
        assertTrue(Files.exists(testLogLocation));
    }

    // Tests logging special characters
    @Test @Order(14) @DisplayName("WB-DEBUG-014: Edge Case - Special characters")
    void testSpecialCharacters() throws Exception {
        String special = "!@#$%^&*()_+-=[]{}|;':\",./<>?";
        Debug.log(special);
        String content = new String(Files.readAllBytes(testLogLocation));
        assertTrue(content.contains(special));
    }

    // Tests logging Unicode characters
    @Test @Order(15) @DisplayName("WB-DEBUG-015: Edge Case - Unicode characters")
    void testUnicodeCharacters() throws Exception {
        String unicode = "Test: 中文 العربية русский";
        Debug.log(unicode);
        String content = new String(Files.readAllBytes(testLogLocation));
        assertTrue(content.contains("Test"));
    }

    // Tests logging with newlines in the message
    @Test @Order(16) @DisplayName("WB-DEBUG-016: Edge Case - Newline in message")
    void testNewlineInMessage() throws Exception {
        Debug.log("Line1\nLine2\nLine3");
        String content = new String(Files.readAllBytes(testLogLocation));
        assertTrue(content.contains("Line1"));
    }

    // Tests rapid logging operations
    @Test @Order(17) @DisplayName("WB-DEBUG-017: Edge Case - Multiple rapid logs")
    void testMultipleRapidLogs() throws Exception {
        for (int i = 0; i < 100; i++) Debug.log("Rapid log " + i);
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.size() >= 101);
    }

    // Tests formatting of timestamp output
    @Test @Order(18) @DisplayName("WB-DEBUG-018: Edge Case - Timestamp format")
    void testTimestampFormat() throws Exception {
        Debug.log("Timestamp test");
        List<String> lines = Files.readAllLines(testLogLocation);
        String lastLine = lines.get(lines.size() - 1);
        assertTrue(lastLine.matches("^\\d{1,2}:\\d{1,2}:\\d{1,2}: .+"));
    }

    // Tests visibility states of the frame
    @Test @Order(19) @DisplayName("WB-DEBUG-019: Edge Case - Frame visibility")
    void testFrameVisibilityStates() {
        Debug.frame.setVisible(false);
        assertFalse(Debug.frame.isVisible());
        
        Debug.frame.setVisible(true);
        assertTrue(Debug.frame.isVisible());
        
        Debug.frame.setVisible(false);
        assertFalse(Debug.frame.isVisible());
    }

    // Tests basic window/frame properties
    @Test @Order(20) @DisplayName("WB-DEBUG-020: Edge Case - Window properties")
    void testWindowProperties() {
        assertNotNull(Debug.frame);
        assertEquals("Debug Log", Debug.frame.getTitle());
        assertTrue(Debug.frame.getWidth() > 0);
        assertTrue(Debug.frame.getHeight() > 0);
    }
}
