package bankocr;

import java.util.List;
import java.util.TreeSet;

/**
 * Guesses what an entry should have been, assuming the scanner added or
 * missed exactly one pipe or underscore somewhere in it.
 */
final class ErrorCorrection {

    private ErrorCorrection() {
    }

    /** Every fully legible, checksum-valid number one change away, sorted. */
    static List<String> validAlternatives(ScannedEntry entry) {
        var asRead = entry.accountNumber();
        var alternatives = new TreeSet<String>();
        for (int position = 0; position < entry.glyphs().size(); position++) {
            for (char digit : Glyphs.oneChangeAway(entry.glyphs().get(position))) {
                var candidate = withDigitAt(asRead, position, digit);
                if (candidate.indexOf(ScannedEntry.ILLEGIBLE) < 0 && Checksum.isValid(candidate)) {
                    alternatives.add(candidate);
                }
            }
        }
        return List.copyOf(alternatives);
    }

    private static String withDigitAt(String accountNumber, int position, char digit) {
        var digits = accountNumber.toCharArray();
        digits[position] = digit;
        return new String(digits);
    }
}
