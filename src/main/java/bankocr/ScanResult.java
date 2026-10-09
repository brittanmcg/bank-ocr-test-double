package bankocr;

/**
 * An account number as read from the scanner, along with whether it can be
 * trusted.
 */
record ScanResult(String accountNumber, Status status) {

    enum Status {
        /** Every digit was legible and the checksum is valid. */
        VALID,
        /** Every digit was legible but the checksum is invalid. */
        ERR,
        /** At least one digit could not be read. */
        ILL
    }

    static ScanResult classify(String accountNumber) {
        if (accountNumber.indexOf(AccountNumberParser.ILLEGIBLE) >= 0) {
            return new ScanResult(accountNumber, Status.ILL);
        }
        if (!Checksum.isValid(accountNumber)) {
            return new ScanResult(accountNumber, Status.ERR);
        }
        return new ScanResult(accountNumber, Status.VALID);
    }

    String toReportLine() {
        return status == Status.VALID ? accountNumber : accountNumber + " " + status;
    }
}
