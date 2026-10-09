package bankocr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/** User Story 4, using the kata's "use case 4" examples end to end. */
class ErrorCorrectionTest {

    static List<Arguments> userStoryFourCases() {
        return List.of(
            Arguments.of("""

                  |  |  |  |  |  |  |  |  |
                  |  |  |  |  |  |  |  |  |
                """, "711111111"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                  |  |  |  |  |  |  |  |  |
                  |  |  |  |  |  |  |  |  |
                """, "777777177"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                 _|| || || || || || || || |
                |_ |_||_||_||_||_||_||_||_|
                """, "200800000"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                 _| _| _| _| _| _| _| _| _|
                 _| _| _| _| _| _| _| _| _|
                """, "333393333"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                |_||_||_||_||_||_||_||_||_|
                |_||_||_||_||_||_||_||_||_|
                """, "888888888 AMB ['888886888', '888888880', '888888988']"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                |_ |_ |_ |_ |_ |_ |_ |_ |_
                 _| _| _| _| _| _| _| _| _|
                """, "555555555 AMB ['555655555', '559555555']"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                |_ |_ |_ |_ |_ |_ |_ |_ |_
                |_||_||_||_||_||_||_||_||_|
                """, "666666666 AMB ['666566666', '686666666']"),
            Arguments.of("""
                 _  _  _  _  _  _  _  _  _
                |_||_||_||_||_||_||_||_||_|
                 _| _| _| _| _| _| _| _| _|
                """, "999999999 AMB ['899999999', '993999999', '999959999']"),
            Arguments.of("""
                    _  _  _  _  _  _     _
                |_||_|| || ||_   |  |  ||_
                  | _||_||_||_|  |  |  | _|
                """, "490067715 AMB ['490067115', '490067719', '490867715']"),
            Arguments.of("""
                    _  _     _  _  _  _  _
                 _| _| _||_||_ |_   ||_||_|
                  ||_  _|  | _||_|  ||_| _|
                """, "123456789"),
            Arguments.of("""
                 _     _  _  _  _  _  _
                | || || || || || || ||_   |
                |_||_||_||_||_||_||_| _|  |
                """, "000000051"),
            Arguments.of("""
                    _  _  _  _  _  _     _
                |_||_|| ||_||_   |  |  | _
                  | _||_||_||_|  |  |  | _|
                """, "490867715"));
    }

    @ParameterizedTest(name = "reports {1}")
    @MethodSource("userStoryFourCases")
    void correctsOrFlagsAnEntry(String scanned, String expectedReportLine) {
        assertEquals(expectedReportLine, reportLineFor(scanned));
    }

    @Test
    void reportsIllWhenTwoDigitsAreIllegible() {
        // One change can fix at most one glyph, so two '?' can't be resolved.
        var twoIllegible = """
                _  _     _  _  _  _  _
              | _| _||_| _ |_   ||_||_|
              ||_  _|  | _||_|  ||_| _
            """;

        assertEquals("1234?678? ILL", reportLineFor(twoIllegible));
    }

    @Test
    void reportsIllWhenAnErrCannotBeCorrected() {
        // The spec says an entry that can't be worked out is reported ILL,
        // even when every digit was legible (an ERR). See the README.
        // 222222222 is ERR (2 * 45 = 90, 90 mod 11 = 2) and no digit 2 has
        // any one-segment alternative.
        var twos = """
             _  _  _  _  _  _  _  _  _
             _| _| _| _| _| _| _| _| _|
            |_ |_ |_ |_ |_ |_ |_ |_ |_
            """;

        assertEquals("222222222 ILL", reportLineFor(twos));
    }

    private static String reportLineFor(String scanned) {
        var entries = AccountNumberParser.parse(scanned);
        assertEquals(1, entries.size());
        return ScanResult.of(entries.getFirst()).toReportLine();
    }
}
