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
| 4          | Correct single-segment scanner errors       | Done   |

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
build/install/bank-ocr/bin/bank-ocr samples/user-story-4.txt results.txt
```

This reads one scanned file and writes one results file, with one account
number per line:

```
711111111
777777177
200800000
...
888888888 AMB ['888886888', '888888880', '888888988']
...
490867715
```

When an entry is illegible or fails its checksum, the program tries every way
of adding or removing a single pipe or underscore. If exactly one of those
gives a legible number with a valid checksum, that number is reported as the
result. Otherwise the entry is flagged:

- No status: the number is valid, either as scanned or after correction.
- `AMB`: more than one single-segment correction gives a valid number. The
  number as scanned is shown, followed by every valid alternative.
- `ILL`: no single-segment correction gives a valid number. Illegible digits
  are shown as `?`.

`ERR` (legible, but the checksum is invalid) is part of the format but no
longer appears in practice; see [Open questions](#open-questions).

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

## Open questions

### Should an uncorrectable `ERR` be reported as `ERR` or `ILL`?

User Story 4 says that if an entry can't be corrected, "the status should be
reported ILL". This implementation does that, so an entry that was fully
legible but has a bad checksum (for example `222222222`) is reported as `ILL`,
and `ERR` never appears in the output.

That conflicts with User Story 3, where `ILL` means "illegible". Reporting
`222222222 ERR` instead would tell the reader the digits were read clearly. It
is a one-line change in `ScanResult.of` once the bank confirms which it wants.

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
