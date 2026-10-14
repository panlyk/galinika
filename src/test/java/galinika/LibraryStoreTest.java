package galinika;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class LibraryStoreTest {
  @TempDir Path directory;

  @Test
  void seedsBundledDataWithoutOverwritingExistingFiles() throws Exception {
    LibraryStore store = new LibraryStore(directory);
    assertEquals(2, store.types().size());
    assertFalse(store.substances().isEmpty());
    store.save("substances.csv", List.of(List.of("Custom", "1.23")), 2);
    assertEquals("Custom", store.substances().getFirst().name());
  }

  @Test
  void roundTripsCommasQuotesGreekAndMultilineNames() throws Exception {
    LibraryStore store = new LibraryStore(directory);
    String name = "Κρέμα, \"ειδική\"\nδεύτερη γραμμή";
    store.save("substances.csv", List.of(List.of(name, "1,25")), 2);
    assertEquals(name, store.substances().getFirst().name());
    assertEquals("1.25", store.substances().getFirst().pricePerQuantity().toPlainString());
  }

  @Test
  void invalidSavePreservesEveryByteOfOriginal() throws Exception {
    LibraryStore store = new LibraryStore(directory);
    store.types();
    byte[] original = Files.readAllBytes(directory.resolve("types.csv"));
    for (String value : List.of("abc", "NaN", "Infinity", "-1", "0")) {
      assertThrows(
          IllegalArgumentException.class,
          () -> store.save("types.csv", List.of(List.of("Bad", "50", value, "2", "1")), 5));
      assertArrayEquals(original, Files.readAllBytes(directory.resolve("types.csv")));
    }
    assertThrows(
        IllegalArgumentException.class,
        () -> store.save("types.csv", List.of(List.of("Incomplete", "50")), 5));
    assertArrayEquals(original, Files.readAllBytes(directory.resolve("types.csv")));
  }

  @Test
  void malformedInputFailsClearlyInsteadOfReturningPartialData() throws Exception {
    LibraryStore store = new LibraryStore(directory);
    Files.writeString(directory.resolve("types.csv"), "broken\n");
    assertThrows(IOException.class, store::types);
    Files.writeString(directory.resolve("substances.csv"), "Valid,1\nBad,abc\n");
    assertThrows(IOException.class, store::substances);
  }

  @Test
  void failedReplacementLeavesNoTemporaryFiles() throws Exception {
    LibraryStore store = new LibraryStore(directory);
    Files.createDirectory(directory.resolve("substances.csv"));
    Files.writeString(directory.resolve("substances.csv/keep"), "keep");
    assertThrows(
        IOException.class, () -> store.save("substances.csv", List.of(List.of("Name", "1")), 2));
    assertEquals("keep", Files.readString(directory.resolve("substances.csv/keep")));
    try (var files = Files.list(directory)) {
      assertEquals(1, files.count());
    }
  }

  @Test
  void repairsLegacyUtf8WithoutLosingCustomRowsAndKeepsBackup() throws Exception {
    java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
    bytes.writeBytes("CUSTOM,3.45\nΛΙ".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    bytes.write(0xCE);
    bytes.writeBytes(
        " ΟΣ ΦΑΛΑΙΝΑΣ,0.0088\nLAST,7.89\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    byte[] original = bytes.toByteArray();
    Path path = directory.resolve("substances.csv");
    Files.write(path, original);
    LibraryStore store = new LibraryStore(directory);
    var substances = store.substances();
    assertEquals(3, substances.size());
    assertEquals("ΛΙΠΟΣ ΦΑΛΑΙΝΑΣ", substances.get(1).name());
    assertEquals("CUSTOM,3.45\nΛΙΠΟΣ ΦΑΛΑΙΝΑΣ,0.0088\nLAST,7.89\n", Files.readString(path));
    try (var files = Files.list(directory)) {
      var backups = files.filter(p -> p.toString().endsWith(".bak")).toList();
      assertEquals(1, backups.size());
      assertArrayEquals(original, Files.readAllBytes(backups.getFirst()));
    }
    store.substances();
    try (var files = Files.list(directory)) {
      assertEquals(2, files.count());
    }
  }

  @Test
  void unknownDamagedEncodingLeavesTheFileUntouched() throws Exception {
    byte[] original = new byte[] {(byte) 0xFF, ',', '1', '\n'};
    Path path = directory.resolve("substances.csv");
    Files.write(path, original);
    IOException error =
        assertThrows(IOException.class, () -> new LibraryStore(directory).substances());
    assertTrue(error.getMessage().contains("UTF-8"));
    assertArrayEquals(original, Files.readAllBytes(path));
    try (var files = Files.list(directory)) {
      assertEquals(1, files.count());
    }
  }
}
