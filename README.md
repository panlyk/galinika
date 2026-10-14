# Galinika

A Java Swing desktop app for calculating galenic preparation costs. The original
`.project`, `.classpath`, and `.settings` files indicate development in **Eclipse
with Maven**, using **Java 21**. The source is in `src/main/java/galinika`; the entry
point is `Main.java`. Default libraries and the icon are in `src/main/resources`.

## Downloaded release

On Windows, download **Galinika-0.1.5.exe** and run it to upgrade the original
0.1.3 installation. Close Galinika first. The installer includes Java, retains
the original machine-wide installer identity, and preserves libraries in
`%APPDATA%/Galinika`. Back up that data folder before upgrading.

For other systems, install Java 21 or later and run the release JAR:

```sh
java -jar galinika-0.1.5.jar
```

The JAR includes its dependencies. A graphical desktop is required.

## Open and run

Install a **Java 21 JDK** (not just a Java runtime) and Maven 3.9 or later.
From the repository directory:

```sh
mvn verify
mvn exec:java
```

For all 17 tests on headless Linux, run `xvfb-run -a mvn verify`. Without a
display, the 16 calculation/storage tests run and the desktop test is skipped.

In Eclipse: File → Import → Maven → Existing Maven Projects, select this directory,
configure a Java 21 JDK, and run `galinika.Main` as a Java Application. Other Java
IDEs can import `pom.xml` too. A graphical desktop is needed; on a Linux machine
without one, use `xvfb-run -a mvn exec:java` for automated checks.

## Data and pricing

On first use, the app copies bundled CSV defaults to `%APPDATA%/Galinika` on
Windows, or `~/.galinika` elsewhere. Existing data is preserved. Override the data
directory with `-Dgalinika.dataDir=/your/directory` (for example through
`MAVEN_OPTS` when running Maven). Back up that directory before editing libraries.
Names may include commas and quotes; files use UTF-8 CSV.

Quantities must be positive. Prices, minimum quantities, and other costs must be
nonnegative; quantity increments must be positive. Blank other cost means zero.
Tiered pricing charges the minimum cost, then a full cost increment for each
started quantity increment above the minimum. Money uses decimal arithmetic,
with HALF_UP rounding to two decimal places **once at the final total**.
Individual row prices are rounded for display only, so their displayed sum may
occasionally differ from the final total by a cent. Changing gr/ml changes the
label; the application does not convert units or densities. Library price edits
apply to newly added ingredients; existing ingredients retain their added price.
Type edits refresh the main calculator immediately. Calculated table cells are
read-only: remove and re-add an ingredient to change it.

Library saves validate all rows before writing, then replace the file atomically.
A filesystem without atomic replacement support produces an error and retains
the original file. Empty libraries are allowed; an empty type library disables
calculation. To restore defaults, back up and remove the relevant external CSV,
then reopen the application.

## Code map

- `LaunchPage`, `AddPage`, `LibraryEditor`, `MainMenu`: Swing interface.
- `Numbers`: decimal input validation.
- `PriceCalculator`: tiered pricing, ingredients, and totals.
- `LibraryStore`: CSV loading, validation, and safe saving.
- `Substance`, `GalenicType`: validated data records.
- `src/test/java/galinika`: calculation and persistence regression tests.

Build output belongs in `target/` and should not be committed. The app uses CSV
storage; the old unused SQLite code/dependency has been removed.

## Windows installer builds

The Release workflow builds an EXE on Windows, verifies a real upgrade from the
original 0.1.3 installer, checks saved CSV files are unchanged, and exercises the
installed app. Publishing depends on this check passing. For a manual Windows
build, install a Java 21 JDK, Maven, and WiX 3, run `mvn clean package`, then
`./packaging/Build-WindowsInstaller.ps1` in PowerShell. Do not run the upgrade test
on your own PC; it installs/uninstalls products and creates test library files.
The upgrade UUID in that script must remain unchanged in future versions.
