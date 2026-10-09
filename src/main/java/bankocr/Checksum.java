package bankocr;

/**
 * Validates an account number's checksum.
 *
 * <p>Digits are named right to left, d1 being the last digit and d9 the first:
 * <pre>
 * account number:  3  4  5  8  8  2  8  6  5
 * position names:  d9 d8 d7 d6 d5 d4 d3 d2 d1
 * </pre>
 * and the number is valid when (1*d1 + 2*d2 + ... + 9*d9) mod 11 == 0.
 */
final class Checksum {

    private Checksum() {
    }

    static boolean isValid(String accountNumber) {
        if (!accountNumber.matches("\\d{" + AccountNumberParser.DIGITS_PER_ENTRY + "}")) {
            throw new IllegalArgumentException(
                "Checksum requires exactly " + AccountNumberParser.DIGITS_PER_ENTRY
                    + " digits but got \"" + accountNumber + "\"");
        }

        int sum = 0;
        int length = accountNumber.length();
        for (int index = 0; index < length; index++) {
            int weight = length - index;
            sum += weight * Character.getNumericValue(accountNumber.charAt(index));
        }
        return sum % 11 == 0;
    }
}
