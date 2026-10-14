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
  final JLabel totalPriceJLabel = new JLabel("— €");
  final JButton addButton = new JButton("Προσθήκη Συστατικού"),
      removeButton = new JButton("Διαγραφή"),
      calculateButton = new JButton("Υπολογισμός Συνόλου");
  final List<PriceCalculator.Ingredient> ingredients = new ArrayList<>();

  public LaunchPage() {
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    var icon = getClass().getResource("/galinos.png");
    if (icon != null) frame.setIconImage(new ImageIcon(icon).getImage());
    frame.setJMenuBar(new MainMenu().createMenu(this));
    JPanel content = new JPanel(new BorderLayout(10, 10));
    content.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    JPanel top = new JPanel(new GridLayout(0, 1, 5, 5));
    top.add(new JLabel("Δημιουργία Γαληνικού"));
    top.add(row("Τύπος Γαληνικού:", galenicTypeComboBox));
    addButton.addActionListener(e -> new AddPage(this));
    top.add(addButton);
    content.add(top, BorderLayout.NORTH);
    table.setAutoCreateRowSorter(true);
    content.add(new JScrollPane(table), BorderLayout.CENTER);
    JPanel bottom = new JPanel(new GridLayout(0, 1, 5, 5));
    JPanel deletes = new JPanel();
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
    JButton clear = new JButton("Διαγραφή Όλων");
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
    deletes.add(removeButton);
    deletes.add(clear);
    bottom.add(deletes);
    JPanel quantity = row("Συνολική Ποσότητα:", galenicQuantityField);
    quantity.add(galenicQuantityTypesBox);
    bottom.add(quantity);
    bottom.add(row("Άλλο κόστος (€):", restField));
    calculateButton.addActionListener(e -> calculateTotal());
    bottom.add(calculateButton);
    bottom.add(totalPriceJLabel);
    content.add(bottom, BorderLayout.SOUTH);
    frame.setContentPane(content);
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
    frame.setMinimumSize(new Dimension(450, 600));
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }

  static JPanel row(String label, JComponent field) {
    JPanel p = new JPanel(new FlowLayout(FlowLayout.LEADING));
    p.add(new JLabel(label));
    p.add(field);
    return p;
  }

  void invalidateTotal() {
    totalPriceJLabel.setText("— €");
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
