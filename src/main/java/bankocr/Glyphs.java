package bankocr;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;

/**
 * The seven-segment-style patterns the scanner produces for each digit.
 * A glyph is a 3x3 cell flattened row by row into a 9-character string.
 */
final class Glyphs {

    static final int WIDTH = 3;
    static final int HEIGHT = 3;

    private static final Map<String, Character> DIGITS_BY_GLYPH = Map.of(
        " _ " +
        "| |" +
        "|_|", '0',

        "   " +
        "  |" +
        "  |", '1',

        " _ " +
        " _|" +
        "|_ ", '2',

        " _ " +
        " _|" +
        " _|", '3',

        "   " +
        "|_|" +
        "  |", '4',

        " _ " +
        "|_ " +
        " _|", '5',

        " _ " +
        "|_ " +
        "|_|", '6',

        " _ " +
        "  |" +
        "  |", '7',

        " _ " +
        "|_|" +
        "|_|", '8',

        " _ " +
        "|_|" +
        " _|", '9');

    private Glyphs() {
    }

    static Optional<Character> digitFor(String glyph) {
        return Optional.ofNullable(DIGITS_BY_GLYPH.get(glyph));
    }

    static String glyphFor(char digit) {
        return DIGITS_BY_GLYPH.entrySet().stream()
            .filter(entry -> entry.getValue() == digit)
            .map(Map.Entry::getKey)
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Not a digit: " + digit));
    }

    /**
     * The digits that the scanner could have meant if it missed or added a
     * single pipe or underscore. Works for illegible glyphs too.
     */
    static Set<Character> oneChangeAway(String glyph) {
        var digits = new TreeSet<Character>();
        for (int i = 0; i < glyph.length(); i++) {
            char current = glyph.charAt(i);
            if (current == ' ') {
                digitFor(withCharAt(glyph, i, '_')).ifPresent(digits::add);
                digitFor(withCharAt(glyph, i, '|')).ifPresent(digits::add);
            } else if (current == '_' || current == '|') {
                digitFor(withCharAt(glyph, i, ' ')).ifPresent(digits::add);
            }
        }
        return digits;
    }

    private static String withCharAt(String glyph, int index, char replacement) {
        var chars = glyph.toCharArray();
        chars[index] = replacement;
        return new String(chars);
    }
}
