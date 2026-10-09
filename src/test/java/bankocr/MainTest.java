package bankocr;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MainTest {

    @TempDir
    Path dir;

    private final ByteArrayOutputStream out = new ByteArrayOutputStream();
    private final ByteArrayOutputStream err = new ByteArrayOutputStream();

    private int run(String... args) {
        return Main.run(args, new PrintStream(out, true), new PrintStream(err, true));
    }

    @Test
    void writesAResultsFileForAScannedFile() throws IOException {
        var input = dir.resolve("scanned.txt");
        var output = dir.resolve("results.txt");
        // Full-width lines with trailing spaces intact, as the scanner writes them.
        Files.writeString(input, String.join("\n",
            " _  _  _  _  _  _  _  _    ",
            "| || || || || || || ||_   |",
            "|_||_||_||_||_||_||_| _|  |",
            "                           ",
            "    _  _  _  _  _  _     _ ",
            "|_||_|| || ||_   |  |  | _ ",
            "  | _||_||_||_|  |  |  | _|",
            "                           ",
            " _  _  _  _  _  _  _  _  _ ",
            "  |  |  |  |  |  |  |  |  |",
            "  |  |  |  |  |  |  |  |  |",
            "                           ",
            ""));

        int exitCode = run(input.toString(), output.toString());

        assertEquals(0, exitCode);
        assertEquals("""
            000000051
            49006771? ILL
            777777177
            """, Files.readString(output));
    }

    @Test
    void printsUsageWhenArgumentsAreMissing() {
        int exitCode = run("only-one-argument.txt");

        assertEquals(2, exitCode);
        assertTrue(err.toString().contains("Usage"));
    }

    @Test
    void reportsAMissingInputFile() {
        var output = dir.resolve("results.txt");

        int exitCode = run(dir.resolve("does-not-exist.txt").toString(), output.toString());

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("does-not-exist.txt"));
        assertFalse(Files.exists(output));
    }

    @Test
    void reportsAMalformedInputFileWithoutWritingResults() throws IOException {
        var input = dir.resolve("scanned.txt");
        var output = dir.resolve("results.txt");
        Files.writeString(input, "| || |\n");

        int exitCode = run(input.toString(), output.toString());

        assertEquals(1, exitCode);
        assertTrue(err.toString().contains("line 1"));
        assertFalse(Files.exists(output));
    }
}
