package bankocr;

import java.util.List;

/** One account number as the scanner drew it: nine glyphs, left to right. */
record ScannedEntry(List<String> glyphs) {

    static final char ILLEGIBLE = '?';

    ScannedEntry {
        glyphs = List.copyOf(glyphs);
    }

    /** The digits as read, with {@value #ILLEGIBLE} for any unrecognized glyph. */
    String accountNumber() {
        var digits = new StringBuilder(glyphs.size());
        for (var glyph : glyphs) {
            digits.append(Glyphs.digitFor(glyph).orElse(ILLEGIBLE));
        }
        return digits.toString();
    }
}
