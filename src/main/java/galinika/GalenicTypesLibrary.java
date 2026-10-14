package galinika;

public class GalenicTypesLibrary extends LibraryEditor {
  public GalenicTypesLibrary() {
    this(() -> {});
  }

  public GalenicTypesLibrary(Runnable onSaved) {
    super(
        "Βιβλιοθήκη Τύπων Γαληνικού",
        "types.csv",
        new String[] {
          "Όνομα", "Ελάχιστη Ποσότητα", "Ποσότητα Κλάσματος", "Ελάχιστο Κόστος", "Κόστος Κλάσματος"
        },
        onSaved);
  }
}
