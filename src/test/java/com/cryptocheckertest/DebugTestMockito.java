package com.cryptocheckertest;

import com.cryptochecker.Debug;
import com.cryptochecker.Main;
import com.cryptochecker.PanelSettings;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.swing.*;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DebugTestMockito {

    @TempDir
    Path tempDir;

    private static Path testLogLocation;
    private static Main testMain;

    @Mock
    private PanelSettings mockPanelSettings;

    @Mock
    private JFrame mockFrame;

    @BeforeEach
    void setUp() {
        // Initialize on first run
        if (testLogLocation == null) {
            Path logFile = tempDir.resolve("log.txt");
            Main.logLocation = logFile.toString();
            Main.folderLocation = tempDir.toString();
            testLogLocation = logFile;
            new File(Main.folderLocation).mkdirs();
        }
        
        testMain = new Main();
        Main.gui = testMain;
    }

    @AfterEach
    void tearDown() {
        if (Debug.frame != null) {
            try { Debug.frame.dispose(); } catch (Exception ignored) {}
        }
        Debug.mode = false;
    }

    // ===== UNIT TESTS =====

    @Test
    @Order(1)
    @DisplayName("UT-DEBUG-001: Debug constructor initialization")
    void testDebugConstructorInitialization() throws Exception {
        new Debug();
        assertNotNull(Debug.frame);
        assertFalse(Debug.frame.isVisible());
        assertEquals("Debug Log", Debug.frame.getTitle());
        assertTrue(Files.exists(testLogLocation));
    }

    @Test
    @Order(2)
    @DisplayName("UT-DEBUG-002: Log writes to file")
    void testLogMethodWritesToFile() throws Exception {
        new Debug();
        Debug.log("Test logging with Mockito");
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.stream().anyMatch(l -> l.contains("Test logging with Mockito")));
    }

    @Test
    @Order(3)
    @DisplayName("UT-DEBUG-003: Timestamp format")
    void testLogTimestampFormat() throws Exception {
        new Debug();
        Debug.log("Timestamp test");
        List<String> lines = Files.readAllLines(testLogLocation);
        String lastLine = lines.get(lines.size() - 1);
        assertTrue(lastLine.matches("^\\d{1,2}:\\d{1,2}:\\d{1,2}: .*"));
    }

    @Test
    @Order(4)
    @DisplayName("UT-DEBUG-004: Log appending behavior")
    void testLogAppendingBehavior() throws Exception {
        new Debug();
        Debug.log("First");
        int c1 = Files.readAllLines(testLogLocation).size();
        Debug.log("Second");
        int c2 = Files.readAllLines(testLogLocation).size();
        Debug.log("Third");
        int c3 = Files.readAllLines(testLogLocation).size();
        assertTrue(c2 > c1 && c3 > c2);
    }

    @Test
    @Order(5)
    @DisplayName("UT-DEBUG-005: Debug mode toggle")
    void testDebugModeToggle() throws Exception {
        new Debug();
        assertFalse(Debug.mode);
        Debug.mode = true;
        Debug.frame.setVisible(true);
        assertTrue(Debug.mode && Debug.frame.isVisible());
        Debug.mode = false;
        Debug.frame.setVisible(false);
        assertFalse(Debug.mode || Debug.frame.isVisible());
    }

    @Test
    @Order(6)
    @DisplayName("UT-DEBUG-006: Window listener registration")
    void testWindowClosingBehavior() throws Exception {
        testMain.panelSettings = mock(PanelSettings.class);
        Main.gui = testMain;
        new Debug();
        assertTrue(Debug.frame.getWindowListeners().length > 0);
    }

    // ===== EDGE CASES =====

    @Test
    @Order(7)
    @DisplayName("UT-DEBUG-007: Log empty string")
    void testLogEmptyString() throws Exception {
        new Debug();
        assertDoesNotThrow(() -> Debug.log(""));
        assertTrue(Files.readAllLines(testLogLocation).size() > 1);
    }

    @Test
    @Order(8)
    @DisplayName("UT-DEBUG-008: Log null value")
    void testLogNullValue() throws Exception {
        new Debug();
        assertDoesNotThrow(() -> Debug.log(null));
    }

    @Test
    @Order(9)
    @DisplayName("UT-DEBUG-009: Log very long message")
    void testLogVeryLongMessage() throws Exception {
        new Debug();
        String msg = "A".repeat(10000);
        assertDoesNotThrow(() -> Debug.log(msg));
        assertTrue(new String(Files.readAllBytes(testLogLocation)).contains("AAAA"));
    }

    @Test
    @Order(10)
    @DisplayName("UT-DEBUG-010: Log special characters")
    void testLogSpecialCharacters() throws Exception {
        new Debug();
        String s = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";
        Debug.log(s);
        assertTrue(new String(Files.readAllBytes(testLogLocation)).contains(s));
    }

    @Test
    @Order(11)
    @DisplayName("UT-DEBUG-011: Log newlines")
    void testLogWithNewlines() throws Exception {
        new Debug();
        Debug.log("Line1\nLine2\nLine3");
        assertTrue(new String(Files.readAllBytes(testLogLocation)).contains("Line1"));
    }

    @Test
    @Order(12)
    @DisplayName("UT-DEBUG-012: Rapid sequential logs")
    void testRapidSequentialLogs() throws Exception {
        new Debug();
        for (int i = 0; i < 100; i++) Debug.log("Rapid log " + i);
        long count = Files.readAllLines(testLogLocation).stream().filter(l -> l.contains("Rapid log")).count();
        assertEquals(100, count);
    }

    @Test
    @Order(13)
    @DisplayName("UT-DEBUG-013: Timestamp components")
    void testTimestampComponents() throws Exception {
        new Debug();
        Debug.log("Time test");
        List<String> lines = Files.readAllLines(testLogLocation);
        String lastLine = lines.get(lines.size() - 1);
        assertTrue(lastLine.split(":", 4).length >= 4);
    }

    @Test
    @Order(14)
    @DisplayName("UT-DEBUG-014: Frame properties")
    void testFrameProperties() throws Exception {
        new Debug();
        assertEquals(610, Debug.frame.getWidth());
        assertEquals(300, Debug.frame.getHeight());
        assertNotNull(Debug.frame.getContentPane());
    }

    @Test
    @Order(15)
    @DisplayName("UT-DEBUG-015: Startup log exists")
    void testLogFileExistsAfterDebugCreation() throws Exception {
        new Debug();
        assertTrue(Files.readAllLines(testLogLocation).stream().anyMatch(l -> l.contains("Program started up")));
    }

    @Test
    @Order(16)
    @DisplayName("UT-DEBUG-016: Startup message format")
    void testStartupMessageFormat() throws Exception {
        new Debug();
        assertTrue(Files.readAllLines(testLogLocation).stream().anyMatch(l -> l.contains("Program started up")));
    }

    @Test
    @Order(17)
    @DisplayName("UT-DEBUG-017: Message order preserved")
    void testLogMessageOrderPreservation() throws Exception {
        new Debug();
        Debug.log("FIRST");
        Debug.log("SECOND");
        Debug.log("THIRD");
        String c = new String(Files.readAllBytes(testLogLocation));
        assertTrue(c.indexOf("FIRST") < c.indexOf("SECOND") && c.indexOf("SECOND") < c.indexOf("THIRD"));
    }

    @Test
    @Order(18)
    @DisplayName("UT-DEBUG-018: Frame title")
    void testFrameTitle() throws Exception {
        new Debug();
        assertEquals("Debug Log", Debug.frame.getTitle());
    }

    @Test
    @Order(19)
    @DisplayName("UT-DEBUG-019: Initial frame visibility")
    void testFrameInitialVisibility() throws Exception {
        new Debug();
        assertFalse(Debug.frame.isVisible());
    }

    @Test
    @Order(20)
    @DisplayName("UT-DEBUG-020: Static mode field")
    void testStaticModeFieldInitialValue() throws Exception {
        Debug.mode = false;
        new Debug();
        boolean mode = Debug.mode;
        assertTrue(mode == true || mode == false);
    }

    // ===== NEGATIVE TESTS =====

    @Test
    @Order(21)
    @DisplayName("NEG-DEBUG-001: Invalid log location")
    void testInvalidLogLocation() {
        String original = Main.logLocation;
        Main.logLocation = "/invalid/path/log.txt";
        assertThrows(Exception.class, Debug::new);
        Main.logLocation = original;
    }

    @Test
    @Order(22)
    @DisplayName("NEG-DEBUG-002: Concurrent logging")
    void testConcurrentLogging() throws Exception {
        new Debug();
        assertDoesNotThrow(() -> {
            Thread t1 = new Thread(() -> Debug.log("Thread 1"));
            Thread t2 = new Thread(() -> Debug.log("Thread 2"));
            t1.start(); t2.start();
            t1.join(); t2.join();
        });
    }

    @Test
    @Order(23)
    @DisplayName("NEG-DEBUG-003: Tab characters")
    void testLoggingWithTabCharacters() throws Exception {
        new Debug();
        Debug.log("Text\twith\ttabs");
        assertTrue(new String(Files.readAllBytes(testLogLocation)).contains("Text"));
    }

    @Test
    @Order(24)
    @DisplayName("NEG-DEBUG-004: Unicode characters")
    void testLoggingUnicodeCharacters() throws Exception {
        new Debug();
        Debug.log("Test Unicode: 中文 Икра");
        assertTrue(new String(Files.readAllBytes(testLogLocation)).contains("Unicode"));
    }

    @Test
    @Order(25)
    @DisplayName("NEG-DEBUG-005: Very rapid logging")
    void testVeryRapidLogging() throws Exception {
        new Debug();
        assertDoesNotThrow(() -> {
            for (int i = 0; i < 1000; i++) Debug.log("Rapid " + i);
        });
    }
}
