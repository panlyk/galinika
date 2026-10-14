package galinika;

import java.awt.*;
import java.io.IOException;
import java.util.Comparator;
import javax.swing.*;

public class AddPage {
  final JFrame frame;
  final JComboBox<Substance> substanceComboBox = new JComboBox<>();
  final JTextField quantityField = new JTextField(10);
  final JButton confirmButton = new JButton("Επιβεβαίωση");
  final JComboBox<String> units = new JComboBox<>(new String[] {"gr", "ml"});

  public AddPage(LaunchPage page) {
    frame = new JFrame("Προσθήκη συστατικού");
    try {
      LibraryStore.defaultStore().substances().stream()
          .sorted(Comparator.comparing(Substance::name, String.CASE_INSENSITIVE_ORDER))
          .forEach(substanceComboBox::addItem);
    } catch (IOException e) {
      page.error(e.getMessage());
      frame.dispose();
      return;
    }
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    frame.setSize(360, 360);
    frame.setResizable(false);
    frame.setLayout(null);
    frame.getContentPane().setBackground(Color.WHITE);
    AppIcon.apply(frame);
    frame.setJMenuBar(new MainMenu().createMenu(page));
    JLabel title = new JLabel("Προσθήκη συστατικού");
    title.setFont(new Font("Arial", Font.BOLD, 24));
    title.setBounds(30, 20, 300, 30);
    frame.add(title);
    JLabel name = new JLabel("Συστατικό");
    name.setFont(new Font("Arial", Font.PLAIN, 14));
    name.setBounds(30, 70, 120, 25);
    frame.add(name);
    substanceComboBox.setBounds(130, 70, 160, 25);
    frame.add(substanceComboBox);
    JLabel quantity = new JLabel("Ποσότητα");
    quantity.setFont(new Font("Arial", Font.PLAIN, 14));
    quantity.setBounds(30, 110, 80, 25);
    frame.add(quantity);
    quantityField.setBounds(130, 110, 100, 25);
    frame.add(quantityField);
    units.setBounds(235, 110, 55, 25);
    frame.add(units);
    confirmButton.setBounds(30, 150, 120, 25);
    frame.add(confirmButton);
    confirmButton.setEnabled(substanceComboBox.getItemCount() > 0);
    confirmButton.addActionListener(
        e -> {
          try {
            page.addIngredient(
                new PriceCalculator.Ingredient(
                    (Substance) substanceComboBox.getSelectedItem(),
                    Numbers.parse(quantityField.getText(), true),
                    (String) units.getSelectedItem()));
            frame.dispose();
          } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                frame, ex.getMessage(), "Σφάλμα", JOptionPane.ERROR_MESSAGE);
          }
        });
    frame.getRootPane().setDefaultButton(confirmButton);
    frame.setLocationRelativeTo(page.frame);
    frame.setVisible(true);
  }
}
