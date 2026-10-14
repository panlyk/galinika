package galinika;

import java.awt.*;
import java.math.BigDecimal;
import java.nio.file.*;
import java.util.List;
import javax.swing.*;

public class UiSmokeTest {
  @org.junit.jupiter.api.Test
  void desktopWorkflow() throws Exception {
    org.junit.jupiter.api.Assumptions.assumeTrue(
        System.getenv("DISPLAY") != null, "Requires a desktop or xvfb-run");
    main(new String[0]);
  }

  static void check(boolean ok, String message) {
    if (!ok) throw new AssertionError(message);
  }

  static JButton button(Container root, String text) {
    for (Component c : root.getComponents()) {
      if (c instanceof JButton b && b.getText().equals(text)) return b;
      if (c instanceof Container p) {
        JButton b = button(p, text);
        if (b != null) return b;
      }
    }
    return null;
  }

  public static void main(String[] args) throws Exception {
    String originalDataDir = System.getProperty("galinika.dataDir");
    System.setProperty("galinika.dataDir", Files.createTempDirectory("galinika-ui-").toString());
    try {
      SwingUtilities.invokeAndWait(
          () -> {
            try {
              LaunchPage p = new LaunchPage();
              check(p.frame.isShowing(), "launch");
              check(p.galenicTypeComboBox.getItemCount() == 2, "types");
              check(!p.tableModel.isCellEditable(0, 2), "read-only prices");
              p.addIngredient(
                  new PriceCalculator.Ingredient(
                      new Substance("Example", new BigDecimal("0.25")), new BigDecimal("2"), "gr"));
              p.galenicQuantityField.setText("60");
              p.restField.setText("1");
              p.calculateButton.doClick();
              check(p.totalPriceJLabel.getText().equals("3.97 €"), "total");
              p.restField.setText("2");
              check(p.totalPriceJLabel.getText().equals("— €"), "stale total");
              p.table.setRowSelectionInterval(0, 0);
              p.removeButton.doClick();
              check(p.ingredients.isEmpty(), "remove");
              LibraryStore store = LibraryStore.defaultStore();
              store.save("types.csv", List.of(List.of("ΚΡΕΜΑ", "50", "10", "5", "1")), 5);
              p.reloadTypes();
              p.restField.setText("");
              p.calculateButton.doClick();
              check(p.totalPriceJLabel.getText().equals("6.00 €"), "type refresh");
              SubstanceLibrary library = new SubstanceLibrary();
              check(library.frame.isShowing() && library.tableModel.getRowCount() > 0, "library");
              library.table.editCellAt(0, 1);
              ((JTextField) library.table.getEditorComponent()).setText("1.23");
              Timer closeNotice =
                  new Timer(
                      100,
                      e -> {
                        for (Window w : Window.getWindows()) if (w instanceof JDialog) w.dispose();
                      });
              closeNotice.start();
              button(library.frame, "Αποθήκευση").doClick();
              closeNotice.stop();
              check(
                  store.substances().getFirst().pricePerQuantity().equals(new BigDecimal("1.23")),
                  "active edit saved");
              Timer fillDialog =
                  new Timer(
                      100,
                      e -> {
                        for (Window w : Window.getWindows())
                          if (w instanceof JDialog dialog && dialog.isShowing()) {
                            for (Component c : dialog.getContentPane().getComponents())
                              if (c instanceof JPanel panel)
                                for (Component field : panel.getComponents())
                                  if (field instanceof JTextField text) text.setText("2");
                            JButton confirm = button(dialog, "Επιβεβαίωση");
                            if (confirm != null) confirm.doClick();
                          }
                      });
              fillDialog.start();
              new AddPage(p);
              fillDialog.stop();
              check(p.ingredients.size() == 1, "add dialog");
              System.out.println(
                  "PASS: Swing launch, read-only prices, total, invalidation, remove, type refresh,"
                      + " library active edit save, add dialog");
            } catch (Exception e) {
              throw new RuntimeException(e);
            } finally {
              for (Window w : Window.getWindows()) w.dispose();
            }
          });
    } catch (Throwable t) {
      throw new AssertionError(t);
    } finally {
      if (originalDataDir == null) System.clearProperty("galinika.dataDir");
      else System.setProperty("galinika.dataDir", originalDataDir);
    }
  }
}
