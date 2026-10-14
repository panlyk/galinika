Galinika 0.1.4 fixes input validation and improves pricing and saved-library reliability.

- Reject invalid, negative, zero, NaN, and infinite quantities; show useful errors.
- Validate library rows before saving and use atomic UTF-8 CSV replacement.
- Support commas and quotes in library names.
- Make calculated ingredient cells read-only.
- Use decimal arithmetic and round the final total once (HALF_UP, two decimals).
- Refresh galenic types after library edits and clear outdated totals.
- Separate pricing and storage from Swing, use resizable layouts, and launch on the Swing event thread.
- Repair a damaged character in the bundled ingredient CSV.
- Add 17 automated tests and Eclipse/Maven setup documentation.

## Run

Install Java 21 or later, download `galinika-0.1.4.jar`, and run:

```sh
java -jar galinika-0.1.4.jar
```

A graphical desktop is required. Dependencies are included in the JAR.

## Existing data

Back up your external library files before upgrading. Windows data remains in
`%APPDATA%/Galinika`; other systems default to `~/.galinika`. Existing files are
preserved. Malformed files now produce an error instead of partially loading.
If your old ingredient file contains damaged text, repair it or restore the
bundled default after backing it up. Final totals may change by a cent because
rounding now happens once at the end. Existing ingredients retain their price
when added; library price edits apply to newly added ingredients. Unit labels
do not perform mass/volume conversion.
