package bankocr;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class ChecksumTest {

    // 345882865 is the kata's worked example. It is only valid when the
    // weights run right to left (d1 is the last digit); weighting left to
    // right gives 259 mod 11 = 6, so this case guards against that mistake.
    @ParameterizedTest
    @ValueSource(strings = {"345882865", "457508000", "000000000", "123456789", "711111111"})
    void acceptsAccountNumbersWithAValidChecksum(String accountNumber) {
        assertTrue(Checksum.isValid(accountNumber));
    }

    @ParameterizedTest
    @ValueSource(strings = {"664371495", "111111111", "345882866", "888888888"})
    void rejectsAccountNumbersWithAnInvalidChecksum(String accountNumber) {
        assertFalse(Checksum.isValid(accountNumber));
    }

    @ParameterizedTest
    @ValueSource(strings = {"86110??36", "12345678", "1234567890"})
    void refusesInputThatIsNotNineDigits(String notAnAccountNumber) {
        assertThrows(IllegalArgumentException.class, () -> Checksum.isValid(notAnAccountNumber));
    }

    @Test
    void weightsTheFirstDigitByNineAndTheSecondToLastByTwo() {
        // d9 = 1 (weight 9) + d2 = 1 (weight 2) = 11, which is valid.
        // Weighted left to right it would be 1 * 1 + 1 * 8 = 9, invalid.
        assertTrue(Checksum.isValid("100000010"));
    }
}
