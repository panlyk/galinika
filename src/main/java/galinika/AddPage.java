package galinika;

import java.awt.*;
import java.io.IOException;
import java.util.Comparator;
import javax.swing.*;

public class AddPage {
  private final JDialog dialog;
  final JComboBox<Substance> substanceComboBox = new JComboBox<>();
  final JTextField quantityField = new JTextField(10);
  final JButton confirmButton = new JButton("Επιβεβαίωση");
  final JComboBox<String> units = new JComboBox<>(new String[] {"gr", "ml"});

  public AddPage(LaunchPage page) {
    dialog = new JDialog(page.frame, "Προσθήκη συστατικού", true);
    try {
      LibraryStore.defaultStore().substances().stream()
          .sorted(Comparator.comparing(Substance::name, String.CASE_INSENSITIVE_ORDER))
          .forEach(substanceComboBox::addItem);
    } catch (IOException e) {
      page.error(e.getMessage());
      dialog.dispose();
      return;
    }
    JPanel content = new JPanel(new GridLayout(0, 1, 8, 8));
    content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    content.add(LaunchPage.row("Συστατικό:", substanceComboBox));
    JPanel quantity = LaunchPage.row("Ποσότητα:", quantityField);
    quantity.add(units);
    content.add(quantity);
    content.add(confirmButton);
    confirmButton.setEnabled(substanceComboBox.getItemCount() > 0);
    confirmButton.addActionListener(
        e -> {
          try {
            page.addIngredient(
                new PriceCalculator.Ingredient(
                    (Substance) substanceComboBox.getSelectedItem(),
                    Numbers.parse(quantityField.getText(), true),
                    (String) units.getSelectedItem()));
            dialog.dispose();
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                dialog, ex.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
          }
        });
    dialog.getRootPane().setDefaultButton(confirmButton);
    dialog.setContentPane(content);
    dialog.pack();
    dialog.setLocationRelativeTo(page.frame);
    dialog.setVisible(true);
  }
}
