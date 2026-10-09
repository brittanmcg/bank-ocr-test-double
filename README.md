# Bank OCR

Reads the account-number files produced by the bank's scanning machine and
turns them into account numbers, as described in the
[Bank OCR kata](https://github.com/testdouble/contributing-tests/wiki/Bank-OCR-kata).

## Status

| User story | Description                                 | Status |
|------------|---------------------------------------------|--------|
| 1          | Parse scanned entries into account numbers  | Done   |
| 2          | Validate account numbers with a checksum    | Done   |
| 3          | Write a results file with `ERR` / `ILL`     | Done   |
| 4          | Correct single-segment scanner errors       | Not started |

## Requirements

- JDK 25 or newer. The build uses the included Gradle wrapper, so you don't
  need to install Gradle.

## Building and testing

```sh
./gradlew build    # compile and run all tests
./gradlew test     # run the tests only
./gradlew test --tests 'bankocr.ChecksumTest'   # run one test class
```

## Running

```sh
./gradlew installDist
build/install/bank-ocr/bin/bank-ocr samples/user-story-3.txt results.txt
```

This reads one scanned file and writes one results file, with one account
number per line:

```
000000051
49006771? ILL
1234?678? ILL
123456789
888888888 ERR
```

- No status: the number was read cleanly and its checksum is valid.
- `ERR`: every digit was read, but the checksum is invalid.
- `ILL`: at least one digit was illegible; each one is shown as `?`.

An existing results file is overwritten. If the scanned file is malformed, the
program reports the problem and writes nothing, so a partial results file is
never left behind.

Exit codes: `0` success, `1` the file couldn't be read, written, or parsed,
`2` wrong arguments.

## Input format

Each entry in a scanned file is four lines. The first three lines draw nine
digits with pipes and underscores, three characters wide each (27 characters
per line), and the fourth line is blank:

```
    _  _     _  _  _  _  _
  | _| _||_||_ |_   ||_||_|
  ||_  _|  | _||_|  ||_| _|

```

The example above reads as `123456789`. Lines shorter than 27 characters are
treated as if padded with trailing spaces, because trailing whitespace is often
stripped by editors and file transfers. A digit that doesn't match any known
pattern is reported as `?`. Structurally malformed entries (too few lines,
lines wider than 27 characters, a non-blank separator line) are rejected with
an error that names the line number.

## Known limitations

### A valid checksum does not guarantee a correct account number

The checksum (`(1*d1 + 2*d2 + ... + 9*d9) mod 11 == 0`, with d1 the last digit)
has the following guarantees:

- **One misread digit is always detected.** Changing a single digit changes
  the sum by `weight × difference`, where both are between 1 and 9. Because 11
  is prime, that product is never a multiple of 11.
- **Two swapped digits are always detected**, for the same reason.
- **Two independently misread digits are not always detected.** Roughly 1 in
  11 such errors produces a different number that also has a valid checksum.
  For example, `000000000` and `100000010` are both valid. In that case the
  system will report the wrong account number with no error.

This is a property of the checksum the bank specified, not of this
implementation; detecting more errors would require a stronger check (for
example, more check digits). Treat a valid checksum as "very likely correct",
not "certainly correct".

### Error correction assumes at most one scanner mistake per entry

User Story 4 fixes an `ERR` or `ILL` entry by trying every way to add or remove
a single pipe or underscore. If the scanner made two or more mistakes in one
entry, this can "correct" it to the wrong valid account number. The `AMB`
status flags cases where a single change leads to more than one valid number.
It does not protect against multiple mistakes.
