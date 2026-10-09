package bankocr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class AccountNumberParserTest {

    // Note: Java text blocks strip trailing whitespace, so several of these
    // rows are shorter than 27 characters. The parser must tolerate that,
    // which is also what we'd expect from a scanner file that's been through
    // an editor or a line-trimming transfer.
    static List<Arguments> userStoryOneCases() {
        return List.of(
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                | || || || || || || || || |
                |_||_||_||_||_||_||_||_||_|
                """, "000000000"),
            Arguments.of("""

                  |  |  |  |  |  |  |  |  |
                  |  |  |  |  |  |  |  |  |
                """, "111111111"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                 _| _| _| _| _| _| _| _| _|
                |_ |_ |_ |_ |_ |_ |_ |_ |_
                """, "222222222"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                 _| _| _| _| _| _| _| _| _|
                 _| _| _| _| _| _| _| _| _|
                """, "333333333"),
            Arguments.of("""

                |_||_||_||_||_||_||_||_||_|
                  |  |  |  |  |  |  |  |  |
                """, "444444444"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                |_ |_ |_ |_ |_ |_ |_ |_ |_
                 _| _| _| _| _| _| _| _| _|
                """, "555555555"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                |_ |_ |_ |_ |_ |_ |_ |_ |_
                |_||_||_||_||_||_||_||_||_|
                """, "666666666"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                  |  |  |  |  |  |  |  |  |
                  |  |  |  |  |  |  |  |  |
                """, "777777777"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                |_||_||_||_||_||_||_||_||_|
                |_||_||_||_||_||_||_||_||_|
                """, "888888888"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                |_||_||_||_||_||_||_||_||_|
                 _| _| _| _| _| _| _| _| _|
                """, "999999999"),
            Arguments.of("""
                    _  _     _  _  _  _  _
                  | _| _||_||_ |_   ||_||_|
                  ||_  _|  | _||_|  ||_| _|
                """, "123456789"));
    }

    @ParameterizedTest(name = "reads {1}")
    @MethodSource("userStoryOneCases")
    void readsASingleEntry(String entry, String expected) {
        assertEquals(List.of(expected), accountNumbersIn(entry));
    }

    @Test
    void readsEveryEntryInAFile() {
        var file = """
             _  _  _  _  _  _  _  _  _
            | || || || || || || || || |
            |_||_||_||_||_||_||_||_||_|

                _  _     _  _  _  _  _
              | _| _||_||_ |_   ||_||_|
              ||_  _|  | _||_|  ||_| _|

            """;

        assertEquals(List.of("000000000", "123456789"), accountNumbersIn(file));
    }

    @Test
    void readsAnEmptyFileAsNoEntries() {
        assertEquals(List.of(), accountNumbersIn(""));
    }

    @Test
    void rejectsAnEntryWithTooFewLines() {
        var truncated = """
             _  _  _  _  _  _  _  _  _
            | || || || || || || || || |
            """;

        assertThrows(IllegalArgumentException.class, () -> AccountNumberParser.parse(truncated));
    }

    static List<Arguments> userStoryThreeCases() {
        return List.of(
            Arguments.of("""
                 _  _  _  _  _  _  _  _
                | || || || || || || ||_   |
                |_||_||_||_||_||_||_| _|  |
                """, "000000051"),
            Arguments.of("""
                    _  _  _  _  _  _     _
                |_||_|| || ||_   |  |  | _
                  | _||_||_||_|  |  |  | _|
                """, "49006771?"),
            Arguments.of("""
                    _  _     _  _  _  _  _
                  | _| _||_| _ |_   ||_||_|
                  ||_  _|  | _||_|  ||_| _
                """, "1234?678?"));
    }

    @ParameterizedTest(name = "reads {1}")
    @MethodSource("userStoryThreeCases")
    void readsUnrecognizedDigitsAsQuestionMarks(String entry, String expected) {
        assertEquals(List.of(expected), accountNumbersIn(entry));
    }

    @Test
    void rejectsANonBlankSeparatorLine() {
        var misaligned = """
             _  _  _  _  _  _  _  _  _
            | || || || || || || || || |
            |_||_||_||_||_||_||_||_||_|
             _  _  _  _  _  _  _  _  _
            """;

        assertThrows(IllegalArgumentException.class, () -> AccountNumberParser.parse(misaligned));
    }

    @Test
    void rejectsALineWiderThanAnEntry() {
        var tooWide = """
             _  _  _  _  _  _  _  _  _  _
            | || || || || || || || || || |
            |_||_||_||_||_||_||_||_||_||_|
            """;

        assertThrows(IllegalArgumentException.class, () -> AccountNumberParser.parse(tooWide));
    }

    private static List<String> accountNumbersIn(String scannedFile) {
        return AccountNumberParser.parse(scannedFile).stream()
            .map(ScannedEntry::accountNumber)
            .toList();
    }
}
