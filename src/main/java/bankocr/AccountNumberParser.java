package bankocr;

import java.util.ArrayList;
import java.util.List;

/**
 * Splits the scanner's output into entries, one per account number.
 *
 * <p>Each entry is four lines: three rows of glyphs, 27 characters wide
 * (nine digits of three columns each), followed by a blank separator line.
 */
final class AccountNumberParser {

    static final int DIGITS_PER_ENTRY = 9;
    static final int LINE_WIDTH = DIGITS_PER_ENTRY * Glyphs.WIDTH;
    private static final int LINES_PER_ENTRY = Glyphs.HEIGHT + 1;

    private AccountNumberParser() {
    }

    static List<ScannedEntry> parse(String scannedFile) {
        var lines = scannedFile.lines().toList();
        var entries = new ArrayList<ScannedEntry>();
        for (int start = 0; start < lines.size(); start += LINES_PER_ENTRY) {
            int end = Math.min(start + LINES_PER_ENTRY, lines.size());
            entries.add(parseEntry(lines.subList(start, end), start + 1));
        }
        return entries;
    }

    private static ScannedEntry parseEntry(List<String> entryLines, int firstLineNumber) {
        // The trailing separator line is optional on the last entry.
        if (entryLines.size() < Glyphs.HEIGHT) {
            throw new IllegalArgumentException(
                "Incomplete entry starting at line " + firstLineNumber
                    + ": expected " + Glyphs.HEIGHT + " rows but found " + entryLines.size());
        }
        if (entryLines.size() == LINES_PER_ENTRY && !entryLines.get(Glyphs.HEIGHT).isBlank()) {
            throw new IllegalArgumentException(
                "Expected a blank separator at line " + (firstLineNumber + Glyphs.HEIGHT));
        }

        var rows = new ArrayList<String>();
        for (int i = 0; i < Glyphs.HEIGHT; i++) {
            rows.add(padToLineWidth(entryLines.get(i), firstLineNumber + i));
        }

        var glyphs = new ArrayList<String>(DIGITS_PER_ENTRY);
        for (int position = 0; position < DIGITS_PER_ENTRY; position++) {
            glyphs.add(glyphAt(rows, position));
        }
        return new ScannedEntry(glyphs);
    }

    // Trailing spaces are often stripped by editors and file transfers, so a
    // short line is treated as if it were padded with spaces.
    private static String padToLineWidth(String line, int lineNumber) {
        if (line.length() > LINE_WIDTH) {
            throw new IllegalArgumentException(
                "Line " + lineNumber + " is " + line.length()
                    + " characters wide; expected at most " + LINE_WIDTH);
        }
        return line + " ".repeat(LINE_WIDTH - line.length());
    }

    private static String glyphAt(List<String> rows, int position) {
        int column = position * Glyphs.WIDTH;
        var glyph = new StringBuilder(Glyphs.WIDTH * Glyphs.HEIGHT);
        for (var row : rows) {
            glyph.append(row, column, column + Glyphs.WIDTH);
        }
        return glyph.toString();
    }
}
