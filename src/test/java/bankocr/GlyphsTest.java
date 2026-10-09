package bankocr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class GlyphsTest {

    // The kata warns that it doesn't list every alternative, so this table
    // was worked out by hand from the glyphs rather than copied from the spec.
    @ParameterizedTest(name = "{0} is one change away from [{1}]")
    @CsvSource(delimiter = '|', value = {
        "0 | 8",
        "1 | 7",
        "2 | ''",
        "3 | 9",
        "4 | ''",
        "5 | 69",
        "6 | 58",
        "7 | 1",
        "8 | 069",
        "9 | 358",
    })
    void findsEveryDigitOneSegmentAwayFromADigit(char digit, String expected) {
        assertEquals(chars(expected), Glyphs.oneChangeAway(Glyphs.glyphFor(digit)));
    }

    @Test
    void findsDigitsOneSegmentAwayFromAnIllegibleGlyph() {
        var illegible =
            " _ " +
            " _ " +
            " _|";

        assertEquals(Set.of('3', '5'), Glyphs.oneChangeAway(illegible));
    }

    @Test
    void doesNotTreatReplacingOneSegmentWithAnotherAsOneChange() {
        // A '|' where an '_' belongs takes a removal and an addition to fix.
        var zeroWithAPipeOnTop =
            " | " +
            "| |" +
            "|_|";

        assertEquals(Set.of(), Glyphs.oneChangeAway(zeroWithAPipeOnTop));
    }

    private static Set<Character> chars(String digits) {
        return digits.chars().mapToObj(c -> (char) c).collect(Collectors.toSet());
    }
}
