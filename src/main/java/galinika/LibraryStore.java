package galinika;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import org.apache.commons.csv.*;

/** UTF-8 CSV storage. Validate before writing; replace only after a complete write. */
public final class LibraryStore {
  private final Path directory;

  public LibraryStore(Path directory) {
    this.directory = directory;
  }

  public static LibraryStore defaultStore() {
    String override = System.getProperty("galinika.dataDir");
    String appData = System.getenv("APPDATA");
    Path path =
        override != null
            ? Path.of(override)
            : appData != null && !appData.isBlank()
                ? Path.of(appData, "Galinika")
                : Path.of(System.getProperty("user.home"), ".galinika");
    return new LibraryStore(path);
  }

  private Path file(String name) throws IOException {
    Files.createDirectories(directory);
    Path file = directory.resolve(name);
    if (!Files.exists(file)) {
      try (InputStream in = LibraryStore.class.getResourceAsStream("/" + name)) {
        if (in == null) throw new IOException("Λείπει το αρχείο " + name);
        Files.copy(in, file);
      }
    }
    return file;
  }

  public List<List<String>> read(String name, int columns) throws IOException {
    Path path = file(name);
    byte[] original = Files.readAllBytes(path);
    try {
      return parse(decode(original), name, columns);
    } catch (CharacterCodingException error) {
      if (!name.equals("substances.csv")) throw encodingError(path, error);
      byte[] repaired = repairLegacyIngredientName(original);
      if (Arrays.equals(original, repaired)) throw encodingError(path, error);
      List<List<String>> rows;
      try {
        rows = parse(decode(repaired), name, columns);
        validate(rows, columns);
      } catch (CharacterCodingException | IllegalArgumentException invalid) {
        throw encodingError(path, invalid);
      }
      // Preserve every original byte before changing only the known damaged name.
      Path backup =
          Files.createTempFile(directory, "substances.csv.before-encoding-repair-", ".bak");
      Files.write(backup, original);
      Path temporary = Files.createTempFile(directory, ".galinika-repair-", ".csv");
      try {
        Files.write(temporary, repaired);
        Files.move(
            temporary, path, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
      } finally {
        Files.deleteIfExists(temporary);
      }
      return rows;
    }
  }

  private static String decode(byte[] bytes) throws CharacterCodingException {
    return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
  }

  private static IOException encodingError(Path path, Exception cause) {
    return new IOException(
        "Το αρχείο "
            + path
            + " περιέχει μη έγκυρο κείμενο UTF-8. "
            + "Το αρχικό αρχείο διατηρήθηκε. Χρειάζεται διόρθωση της κωδικοποίησης.",
        cause);
  }

  private static byte[] repairLegacyIngredientName(byte[] original) {
    // The 0.1.3 defaults shipped a truncated UTF-8 Π in ΛΙΠΟΣ ΦΑΛΑΙΝΑΣ.
    ByteArrayOutputStream pattern = new ByteArrayOutputStream();
    pattern.writeBytes("ΛΙ".getBytes(StandardCharsets.UTF_8));
    pattern.write(0xCE);
    pattern.writeBytes(" ΟΣ ΦΑΛΑΙΝΑΣ".getBytes(StandardCharsets.UTF_8));
    byte[] broken = pattern.toByteArray();
    byte[] corrected = "ΛΙΠΟΣ ΦΑΛΑΙΝΑΣ".getBytes(StandardCharsets.UTF_8);
    ByteArrayOutputStream result = new ByteArrayOutputStream();
    for (int i = 0; i < original.length; ) {
      boolean matches = i + broken.length <= original.length;
      for (int j = 0; matches && j < broken.length; j++) matches = original[i + j] == broken[j];
      if (matches) {
        result.writeBytes(corrected);
        i += broken.length;
      } else {
        result.write(original[i]);
        i++;
      }
    }
    return result.toByteArray();
  }

  private static List<List<String>> parse(String text, String name, int columns)
      throws IOException {
    List<List<String>> rows = new ArrayList<>();
    try (CSVParser parser = CSVFormat.DEFAULT.parse(new StringReader(text))) {
      for (CSVRecord record : parser) {
        if (record.size() != columns)
          throw new IOException("Μη έγκυρη γραμμή " + record.getRecordNumber() + " στο " + name);
        List<String> row = new ArrayList<>();
        record.forEach(value -> row.add(value.trim()));
        rows.add(row);
      }
    } catch (UncheckedIOException e) {
      throw e.getCause();
    }
    return rows;
  }

  public static void validate(List<List<String>> rows, int columns) {
    for (int i = 0; i < rows.size(); i++) {
      List<String> row = rows.get(i);
      try {
        if (row.size() != columns || row.get(0) == null || row.get(0).isBlank())
          throw new IllegalArgumentException("Απαιτείται όνομα και όλες οι τιμές.");
        for (int j = 1; j < columns; j++) Numbers.parse(row.get(j), columns == 5 && j == 2);
      } catch (IllegalArgumentException e) {
        throw new IllegalArgumentException("Γραμμή " + (i + 1) + ": " + e.getMessage(), e);
      }
    }
  }

  public void save(String name, List<List<String>> rows, int columns) throws IOException {
    validate(rows, columns);
    Files.createDirectories(directory);
    Path temporary = Files.createTempFile(directory, ".galinika-", ".csv");
    try {
      try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8);
          CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT)) {
        for (List<String> row : rows) {
          List<String> normalized = new ArrayList<>();
          normalized.add(row.get(0).trim());
          for (int j = 1; j < columns; j++)
            normalized.add(Numbers.parse(row.get(j), columns == 5 && j == 2).toPlainString());
          csv.printRecord(normalized);
        }
      }
      // Fail safely if the filesystem cannot perform an atomic replacement.
      Files.move(
          temporary,
          directory.resolve(name),
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } finally {
      Files.deleteIfExists(temporary);
    }
  }

  public List<Substance> substances() throws IOException {
    List<List<String>> rows = read("substances.csv", 2);
    try {
      validate(rows, 2);
      return rows.stream()
          .map(r -> new Substance(r.get(0), Numbers.parse(r.get(1), false)))
          .toList();
    } catch (IllegalArgumentException e) {
      throw new IOException(e.getMessage(), e);
    }
  }

  public List<GalenicType> types() throws IOException {
    List<List<String>> rows = read("types.csv", 5);
    try {
      validate(rows, 5);
      return rows.stream()
          .map(
              r ->
                  new GalenicType(
                      r.get(0),
                      Numbers.parse(r.get(1), false),
                      Numbers.parse(r.get(2), true),
                      Numbers.parse(r.get(3), false),
                      Numbers.parse(r.get(4), false)))
          .toList();
    } catch (IllegalArgumentException e) {
      throw new IOException(e.getMessage(), e);
    }
  }
}
