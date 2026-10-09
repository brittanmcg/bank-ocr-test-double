package bankocr;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.stream.Collectors;

/**
 * Reads one scanned file and writes a results file with one account number
 * per line, corrected where possible and flagged {@code AMB} or {@code ILL}
 * when it can't be trusted.
 */
public final class Main {

    private static final int EXIT_OK = 0;
    private static final int EXIT_FAILURE = 1;
    private static final int EXIT_USAGE = 2;

    private Main() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream out, PrintStream err) {
        if (args.length != 2) {
            err.println("Usage: bank-ocr <scanned-file> <results-file>");
            return EXIT_USAGE;
        }
        var input = Path.of(args[0]);
        var output = Path.of(args[1]);

        String scannedFile;
        try {
            scannedFile = Files.readString(input);
        } catch (NoSuchFileException e) {
            err.println("Scanned file not found: " + input);
            return EXIT_FAILURE;
        } catch (IOException e) {
            err.println("Could not read " + input + ": " + e.getMessage());
            return EXIT_FAILURE;
        }

        // Parse the whole file before writing anything, so a malformed file
        // never leaves a partial results file behind.
        String report;
        int entries;
        try {
            var results = AccountNumberParser.parse(scannedFile).stream()
                .map(ScanResult::of)
                .toList();
            entries = results.size();
            report = results.stream()
                .map(result -> result.toReportLine() + "\n")
                .collect(Collectors.joining());
        } catch (IllegalArgumentException e) {
            err.println("Invalid scanned file " + input + ": " + e.getMessage());
            return EXIT_FAILURE;
        }

        try {
            Files.writeString(output, report);
        } catch (IOException e) {
            err.println("Could not write " + output + ": " + e.getMessage());
            return EXIT_FAILURE;
        }
        out.println("Wrote " + entries + " results to " + output);
        return EXIT_OK;
    }
}
