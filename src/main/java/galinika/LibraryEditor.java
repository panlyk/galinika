package galinika;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

class LibraryEditor {
  final JFrame frame = new JFrame();
  final DefaultTableModel tableModel;
  final JTable table;

  LibraryEditor(String title, String file, String[] columns, Runnable onSaved) {
    LibraryStore store = LibraryStore.defaultStore();
    tableModel = new DefaultTableModel(columns, 0);
    table = new JTable(tableModel);
    try {
      List<List<String>> rows = store.read(file, columns.length);
      LibraryStore.validate(rows, columns.length);
      rows.forEach(row -> tableModel.addRow(row.toArray()));
    } catch (IOException | IllegalArgumentException e) {
      JOptionPane.showMessageDialog(
          null, e.getMessage(), "Σφάλμα βιβλιοθήκης", JOptionPane.ERROR_MESSAGE);
      frame.dispose();
      return;
    }
    frame.setTitle(title);
    frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    table.setAutoCreateRowSorter(true);
    frame.add(new JScrollPane(table), BorderLayout.CENTER);
    JPanel buttons = new JPanel();
    JButton add = new JButton("Προσθήκη"),
        delete = new JButton("Διαγραφή"),
        save = new JButton("Αποθήκευση");
    add.addActionListener(
        e -> {
          Object[] row = new Object[columns.length];
          java.util.Arrays.fill(row, "");
          tableModel.addRow(row);
        });
    delete.addActionListener(
        e -> {
          if (table.getSelectedRow() >= 0)
            tableModel.removeRow(table.convertRowIndexToModel(table.getSelectedRow()));
        });
    save.addActionListener(
        e -> {
          if (table.isEditing() && !table.getCellEditor().stopCellEditing()) return;
          List<List<String>> rows = new ArrayList<>();
          for (int i = 0; i < tableModel.getRowCount(); i++) {
            List<String> row = new ArrayList<>();
            for (int j = 0; j < columns.length; j++)
              row.add(java.util.Objects.toString(tableModel.getValueAt(i, j), ""));
            rows.add(row);
          }
          try {
            store.save(file, rows, columns.length);
            onSaved.run();
            JOptionPane.showMessageDialog(frame, "Οι αλλαγές αποθηκεύτηκαν!");
            frame.dispose();
          } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                frame, ex.getMessage(), "Σφάλμα αποθήκευσης", JOptionPane.ERROR_MESSAGE);
          }
        });
    buttons.add(add);
    buttons.add(delete);
    buttons.add(save);
    frame.add(buttons, BorderLayout.SOUTH);
    frame.setSize(columns.length == 5 ? 750 : 450, 450);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
