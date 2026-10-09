package bankocr;

import java.util.List;
import java.util.stream.Collectors;

/**
 * An account number as read from the scanner, along with whether it can be
 * trusted.
 */
record ScanResult(String accountNumber, Status status, List<String> alternatives) {

    enum Status {
        /** Every digit was legible and the checksum is valid. */
        VALID,
        /** Every digit was legible but the checksum is invalid. */
        ERR,
        /** At least one digit could not be read, or the entry couldn't be corrected. */
        ILL,
        /** More than one single-segment correction gives a valid number. */
        AMB
    }

    ScanResult {
        alternatives = List.copyOf(alternatives);
    }

    /** Reads an entry, correcting it if exactly one single-segment fix makes it valid. */
    static ScanResult of(ScannedEntry entry) {
        var asRead = classify(entry.accountNumber());
        if (asRead.status() == Status.VALID) {
            return asRead;
        }

        var alternatives = ErrorCorrection.validAlternatives(entry);
        return switch (alternatives.size()) {
            case 0 -> new ScanResult(asRead.accountNumber(), Status.ILL, List.of());
            case 1 -> new ScanResult(alternatives.getFirst(), Status.VALID, List.of());
            default -> new ScanResult(asRead.accountNumber(), Status.AMB, alternatives);
        };
    }

    /** Classifies a number as read, without attempting any correction. */
    static ScanResult classify(String accountNumber) {
        if (accountNumber.indexOf(ScannedEntry.ILLEGIBLE) >= 0) {
            return new ScanResult(accountNumber, Status.ILL, List.of());
        }
        if (!Checksum.isValid(accountNumber)) {
            return new ScanResult(accountNumber, Status.ERR, List.of());
        }
        return new ScanResult(accountNumber, Status.VALID, List.of());
    }

    String toReportLine() {
        return switch (status) {
            case VALID -> accountNumber;
            case ERR, ILL -> accountNumber + " " + status;
            case AMB -> accountNumber + " AMB " + alternatives.stream()
                .map(alternative -> "'" + alternative + "'")
                .collect(Collectors.joining(", ", "[", "]"));
        };
    }
}
