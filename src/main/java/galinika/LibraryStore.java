package galinika;

import java.io.*;
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
    List<List<String>> rows = new ArrayList<>();
    try (Reader reader = Files.newBufferedReader(file(name), StandardCharsets.UTF_8);
        CSVParser parser = CSVFormat.DEFAULT.parse(reader)) {
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
