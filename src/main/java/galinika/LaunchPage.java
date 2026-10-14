package galinika;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

public class LaunchPage {
  final JFrame frame = new JFrame("Galinika");
  final JComboBox<GalenicType> galenicTypeComboBox = new JComboBox<>();
  final JTextField galenicQuantityField = new JTextField(10), restField = new JTextField(10);
  final JComboBox<String> galenicQuantityTypesBox = new JComboBox<>(new String[] {"gr", "ml"});
  final DefaultTableModel tableModel =
      new DefaultTableModel(new String[] {"Όνομα", "Ποσότητα", "Τιμή"}, 0) {
        @Override
        public boolean isCellEditable(int row, int column) {
          return false;
        }
      };
  final JTable table = new JTable(tableModel);
  final JLabel totalPriceJLabel = new JLabel("-€");
  final JButton addButton = new JButton("Προσθήκη Συστατικού"),
      removeButton = new JButton("Διαγραφή"),
      calculateButton = new JButton("Υπολογισμός Συνόλου");
  final List<PriceCalculator.Ingredient> ingredients = new ArrayList<>();

  public LaunchPage() {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    var icon = getClass().getResource("/galinos.png");
    if (icon != null) frame.setIconImage(new ImageIcon(icon).getImage());
    frame.setJMenuBar(new MainMenu().createMenu(this));
    frame.setSize(375, 650);
    frame.setResizable(false);
    frame.setLayout(null);
    frame.getContentPane().setBackground(Color.WHITE);
    AppIcon.apply(frame);
    Font labels = new Font("Segoe UI", Font.PLAIN, 14);
    JLabel title = new JLabel("Δημιουργία Γαληνικού");
    title.setFont(new Font("Segoe UI", Font.BOLD, 22));
    place(title, 30, 20, 300, 30);
    JLabel typeLabel = new JLabel("Τύπος Γαληνικού:");
    typeLabel.setFont(labels);
    place(typeLabel, 30, 70, 120, 25);
    place(galenicTypeComboBox, 175, 70, 150, 25);
    AppIcon.button(addButton, new Color(70, 130, 180));
    addButton.addActionListener(e -> new AddPage(this));
    place(addButton, 30, 110, 180, 25);
    table.setAutoCreateRowSorter(true);
    place(new JScrollPane(table), 30, 150, 300, 140);
    AppIcon.button(removeButton, new Color(220, 53, 69));
    removeButton.addActionListener(
        e -> {
          int row = table.getSelectedRow();
          if (row >= 0) {
            int index = table.convertRowIndexToModel(row);
            ingredients.remove(index);
            tableModel.removeRow(index);
            invalidateTotal();
          }
        });
    place(removeButton, 30, 300, 140, 25);
    JButton clear = new JButton("Διαγραφή Όλων");
    AppIcon.button(clear, new Color(220, 53, 69));
    clear.addActionListener(
        e -> {
          if (!ingredients.isEmpty()
              && JOptionPane.showConfirmDialog(
                      frame,
                      "Θέλετε να διαγράψετε όλα τα υλικά;",
                      "Επιβεβαίωση",
                      JOptionPane.YES_NO_OPTION)
                  == JOptionPane.YES_OPTION) {
            ingredients.clear();
            tableModel.setRowCount(0);
            invalidateTotal();
          }
        });
    place(clear, 190, 300, 140, 25);
    JLabel quantityLabel = new JLabel("Συνολική Ποσότητα:");
    quantityLabel.setFont(labels);
    place(quantityLabel, 30, 350, 140, 25);
    place(galenicQuantityField, 165, 350, 110, 25);
    place(galenicQuantityTypesBox, 285, 350, 45, 25);
    JLabel otherLabel = new JLabel("Άλλο κόστος (€):");
    otherLabel.setFont(labels);
    place(otherLabel, 30, 390, 150, 25);
    place(restField, 165, 390, 110, 25);
    AppIcon.button(calculateButton, new Color(40, 167, 69));
    calculateButton.addActionListener(e -> calculateTotal());
    place(calculateButton, 30, 450, 180, 30);
    totalPriceJLabel.setHorizontalAlignment(SwingConstants.CENTER);
    totalPriceJLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
    totalPriceJLabel.setForeground(new Color(0, 128, 0));
    totalPriceJLabel.setOpaque(true);
    totalPriceJLabel.setBackground(new Color(235, 255, 235));
    totalPriceJLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 128, 0), 1));
    place(totalPriceJLabel, 30, 495, 100, 35);
    DocumentListener listener =
        new DocumentListener() {
          public void insertUpdate(DocumentEvent e) {
            invalidateTotal();
          }

          public void removeUpdate(DocumentEvent e) {
            invalidateTotal();
          }

          public void changedUpdate(DocumentEvent e) {
            invalidateTotal();
          }
        };
    galenicQuantityField.getDocument().addDocumentListener(listener);
    restField.getDocument().addDocumentListener(listener);
    galenicTypeComboBox.addActionListener(e -> invalidateTotal());
    reloadTypes();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  private void place(JComponent component, int x, int y, int width, int height) {
    component.setBounds(x, y, width, height);
    frame.add(component);
  }

  void invalidateTotal() {
    totalPriceJLabel.setText("-€");
  }

  void reloadTypes() {
    GalenicType selected = (GalenicType) galenicTypeComboBox.getSelectedItem();
    try {
      List<GalenicType> types = LibraryStore.defaultStore().types();
      galenicTypeComboBox.removeAllItems();
      for (GalenicType type : types) {
        galenicTypeComboBox.addItem(type);
        if (selected != null && selected.name().equals(type.name()))
          galenicTypeComboBox.setSelectedItem(type);
      }
      calculateButton.setEnabled(!types.isEmpty());
      invalidateTotal();
    } catch (IOException e) {
      galenicTypeComboBox.removeAllItems();
      calculateButton.setEnabled(false);
      invalidateTotal();
      error(e.getMessage());
    }
  }

  void addIngredient(PriceCalculator.Ingredient ingredient) {
    ingredients.add(ingredient);
    tableModel.addRow(
        new Object[] {
          ingredient.substance().name(),
          ingredient.quantity().toPlainString() + " " + ingredient.unit(),
          PriceCalculator.money(ingredient.cost())
        });
    invalidateTotal();
  }

  void calculateTotal() {
    try {
      BigDecimal quantity = Numbers.parse(galenicQuantityField.getText(), true);
      BigDecimal other =
          restField.getText().isBlank()
              ? BigDecimal.ZERO
              : Numbers.parse(restField.getText(), false);
      totalPriceJLabel.setText(
          PriceCalculator.money(
              PriceCalculator.total(
                  (GalenicType) galenicTypeComboBox.getSelectedItem(),
                  quantity,
                  ingredients,
                  other)));
    } catch (IllegalArgumentException e) {
      invalidateTotal();
      error(e.getMessage());
    }
  }

  void error(String message) {
    JOptionPane.showMessageDialog(frame, message, "Σφάλμα", JOptionPane.ERROR_MESSAGE);
  }
}
