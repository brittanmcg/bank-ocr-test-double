package bankocr;

import java.util.Map;
import java.util.Optional;

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
}
