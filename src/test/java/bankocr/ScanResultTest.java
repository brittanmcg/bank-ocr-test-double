package bankocr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class ScanResultTest {

    @ParameterizedTest
    @CsvSource({
        "457508000, VALID",
        "664371495, ERR",
        "86110??36, ILL",
        // Illegible wins over a bad checksum: we can't checksum a '?'.
        "?????????, ILL",
    })
    void classifiesAnAccountNumber(String accountNumber, ScanResult.Status expected) {
        assertEquals(expected, ScanResult.classify(accountNumber).status());
    }

    @ParameterizedTest
    @CsvSource(delimiter = '|', value = {
        "457508000 | 457508000",
        "664371495 | 664371495 ERR",
        "86110??36 | 86110??36 ILL",
    })
    void formatsAReportLine(String accountNumber, String expectedLine) {
        assertEquals(expectedLine, ScanResult.classify(accountNumber).toReportLine());
    }
}
