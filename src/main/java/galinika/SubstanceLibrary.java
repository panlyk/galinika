package galinika;

public class SubstanceLibrary extends LibraryEditor {
  public SubstanceLibrary() {
    this(() -> {});
  }

  public SubstanceLibrary(Runnable onSaved) {
    super(
        "Βιβλιοθήκη Συστατικών",
        "substances.csv",
        new String[] {"Όνομα", "Τιμή ανά g/ml (€)"},
        onSaved);
  }
}
