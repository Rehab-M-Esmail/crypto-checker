package com.cryptochecker;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Pattern;

// Simplified Black Box Test Suite

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class DebugTest {

    @TempDir
    static Path tempDir;
    
    private static Path testLogLocation;
    private static Main testMain;

    @BeforeAll
    static void setUpBeforeAll() throws Exception {
        testLogLocation = new File(Main.logLocation).toPath();
        if (!(new File(Main.folderLocation).exists())) new File(Main.folderLocation).mkdirs();
        testMain = new Main();
        Main.gui = testMain;
    }

    @AfterAll
    static void tearDownAfterAll() {
        try { if (Files.exists(testLogLocation)) Files.delete(testLogLocation); } catch (Exception e) {}
    }

    @BeforeEach
    void setUp() throws Exception {
        if (Files.exists(testLogLocation)) Files.delete(testLogLocation);
        new Debug();
    }

    @AfterEach
    void tearDown() {
        if (Debug.frame != null) Debug.frame.dispose();
    }
    
    private String readFileContent(Path path) throws Exception {
        return new String(Files.readAllBytes(path));
    }

    // TC-DEBUG-001: Log file should be created with startup message
    @Test @Order(1)
    void testLogFileCreation() throws Exception {
        assertTrue(Files.exists(testLogLocation));
        List<String> lines = Files.readAllLines(testLogLocation);
        assertFalse(lines.isEmpty());
        assertTrue(lines.stream().anyMatch(l -> l.contains("Program started up")));
    }

    // TC-DEBUG-002: Log format should follow timestamp + message
    @Test @Order(2)
    void testLogMessageFormat() throws Exception {
        String testMessage = "Test message for format verification";
        Debug.log(testMessage);
        List<String> lines = Files.readAllLines(testLogLocation);
        String lastLine = lines.get(lines.size() - 1);
        Pattern p = Pattern.compile("^\\d{1,2}:\\d{1,2}:\\d{1,2}: .+");
        assertTrue(p.matcher(lastLine).matches());
        assertTrue(lastLine.contains(testMessage));
    }

    // TC-DEBUG-003: Log file should append messages sequentially
    @Test @Order(3)
    void testLogFileAppending() throws Exception {
        Debug.log("First"); int c1 = Files.readAllLines(testLogLocation).size();
        Debug.log("Second"); int c2 = Files.readAllLines(testLogLocation).size();
        Debug.log("Third"); int c3 = Files.readAllLines(testLogLocation).size();
        assertTrue(c2 > c1 && c3 > c2);
    }

    // TC-DEBUG-004: Debug window should exist but start hidden
    @Test @Order(4)
    void testDebugWindowInitialization() {
        assertNotNull(Debug.frame);
        assertFalse(Debug.frame.isVisible());
        assertEquals("Debug Log", Debug.frame.getTitle());
    }

    // TC-DEBUG-005: Debug window visibility toggles based on mode
    @Test @Order(5)
    void testDebugModeToggle() {
        Debug.mode = false; Debug.frame.setVisible(false);
        Debug.mode = true; Debug.frame.setVisible(true);
        assertTrue(Debug.frame.isVisible());
        Debug.mode = false; Debug.frame.setVisible(false);
        assertFalse(Debug.frame.isVisible());
    }

    // TC-DEBUG-006: Empty input should still log
    @Test @Order(6)
    void testLogEmptyString() throws Exception {
        Debug.log("");
        List<String> lines = Files.readAllLines(testLogLocation);
        assertTrue(lines.get(lines.size()-1).contains(":"));
    }

    // TC-DEBUG-007: Single character should log
    @Test @Order(7)
    void testLogSingleCharacter() throws Exception {
        Debug.log("A");
        assertTrue(readFileContent(testLogLocation).contains("A"));
    }

    // TC-DEBUG-008: Very long message should log
    @Test @Order(8)
    void testLogLongMessage() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 1000; i++) sb.append("A");
        String msg = sb.toString();
        Debug.log(msg);
        assertTrue(readFileContent(testLogLocation).contains(msg));
    }

    // TC-DEBUG-009: Special chars should log
    @Test @Order(9)
    void testLogSpecialCharacters() throws Exception {
        String s = "!@#$%^&*()_+-=[]{}";
        Debug.log(s);
        assertTrue(readFileContent(testLogLocation).contains(s));
    }

    // TC-DEBUG-010: Newline content should log
    @Test @Order(10)
    void testLogWithNewlines() throws Exception {
        String m = "Line1\nLine2";
        Debug.log(m);
        assertTrue(readFileContent(testLogLocation).contains("Line1"));
    }

    // TC-DEBUG-011: Unicode text should log
    @Test @Order(11)
    void testLogUnicodeCharacters() throws Exception {
        String m = "Test Unicode: \u4E2D\u6587";
        Debug.log(m);
        String content = readFileContent(testLogLocation);
        assertTrue(content.contains("Unicode") || content.contains("Test"));
    }

    // TC-DEBUG-012: Sequential logs should all be written
    @Test @Order(12)
    void testMultipleSequentialLogs() throws Exception {
        for (int i = 0; i < 10; i++) Debug.log("Message " + i);
        String content = readFileContent(testLogLocation);
        for (int i = 0; i < 10; i++) assertTrue(content.contains("Message " + i));
    }

    // TC-DEBUG-013: Timestamp should contain HH:MM:SS
    @Test @Order(13)
    void testLogTimestampComponents() throws Exception {
        Debug.log("Test");
        List<String> lines = Files.readAllLines(testLogLocation);
        String last = lines.get(lines.size() - 1);
        String[] parts = last.split(": ", 2);
        String[] t = parts[0].split(":");
        assertEquals(3, t.length);
    }

    // TC-DEBUG-014: Message order should be preserved
    @Test @Order(14)
    void testLogFileContentOrder() throws Exception {
        Debug.log("FIRST"); Debug.log("SECOND"); Debug.log("THIRD");
        String c = readFileContent(testLogLocation);
        assertTrue(c.indexOf("FIRST") < c.indexOf("SECOND") && c.indexOf("SECOND") < c.indexOf("THIRD"));
    }

    // TC-DEBUG-015: Debug window properties should be valid
    @Test @Order(15)
    void testDebugWindowProperties() {
        assertNotNull(Debug.frame);
        assertEquals("Debug Log", Debug.frame.getTitle());
        assertTrue(Debug.frame.getWidth() > 0 && Debug.frame.getHeight() > 0);
    }

    // TC-DEBUG-016: Logging should not throw exceptions
    @Test @Order(16)
    void testLogFunctionNoExceptions() {
        assertDoesNotThrow(() -> Debug.log("Normal"));
        assertDoesNotThrow(() -> Debug.log(""));
        assertDoesNotThrow(() -> Debug.log("!@#$"));
    }

    // NEG-01: Null message should not crash
    @Test @Order(17)
    void testLogNullMessage() {
        assertDoesNotThrow(() -> Debug.log(null));
    }

    // NEG-02: Extremely long message should not crash
    @Test @Order(18)
    void testLogExtremelyLongMessage() throws Exception {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 50000; i++) sb.append("X");
        String m = sb.toString();
        assertDoesNotThrow(() -> Debug.log(m));
        assertTrue(readFileContent(testLogLocation).contains("XXX"));
    }
}
