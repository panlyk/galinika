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
    AppIcon.apply(frame);
    frame.setResizable(false);
    frame.setLayout(null);
    frame.getContentPane().setBackground(Color.WHITE);
    table.setAutoCreateRowSorter(true);
    table.setRowHeight(25);
    table.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    boolean types = columns.length == 5;
    JScrollPane scroll = new JScrollPane(table);
    scroll.setFocusable(false);
    scroll.setBounds(20, 20, types ? 640 : 340, types ? 360 : 250);
    frame.add(scroll);
    JButton add = new JButton("Προσθήκη"),
        delete = new JButton("Διαγραφή"),
        save = new JButton("Αποθήκευση");
    AppIcon.button(add, new Color(70, 130, 180));
    AppIcon.button(delete, new Color(220, 53, 69));
    AppIcon.button(save, new Color(40, 167, 69));
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
    if (types) {
      add.setBounds(60, 400, 140, 35);
      delete.setBounds(280, 400, 140, 35);
      save.setBounds(500, 400, 140, 35);
    } else {
      add.setBounds(60, 285, 120, 30);
      delete.setBounds(200, 285, 120, 30);
      save.setBounds(120, 325, 140, 30);
    }
    frame.add(add);
    frame.add(delete);
    frame.add(save);
    frame.setSize(types ? 700 : 400, types ? 500 : 400);
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
  }
}
