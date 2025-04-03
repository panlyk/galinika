package galinika;
import java.awt.Color;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

public class GalenicTypesLibrary {
	
    JFrame frame = new JFrame();
    DefaultTableModel tableModel;
    JTable table;
    JScrollPane scrollPane;
    JButton saveButton = new JButton("Αποθήκευση");
    JButton addButton = new JButton("Προσθήκη");
    JButton deleteButton = new JButton("Διαγραφή");
	
    private final String[] columnNames = {"Όνομα", "Ελάχιστη Ποσότητα", "Ποσότητα Κλάσματος", "Ελάχιστο Κόστος", "Κόστος Κλάσματος"};
	// String GalenicTypeName, float minimumQuantity, float divisionQuantity, 
	//float minimumCost, float costIncrement
    public GalenicTypesLibrary() {
        frame.setSize(700, 500); // new size
        frame.setResizable(false);
        frame.setTitle("Βιβλιοθήκη Τύπων Γαληνικού");
        ImageIcon appIcon = new ImageIcon(getClass().getResource("/galinos.png"));
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);

        // Load data
        List<GalenicType> substances = loadTypesFromFile();
        tableModel = new DefaultTableModel(columnNames, 0);
        table = new JTable(tableModel);
        table.setRowHeight(25);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        for (GalenicType s : substances) {
            float[] GalenicTypeValues = s.getgalenicTypeValues();
            float minimumQuantity = GalenicTypeValues[0];
            float divisionQuantity = GalenicTypeValues[1];
            float minimumCost = GalenicTypeValues[2];
            float costIncrement = GalenicTypeValues[3];

            tableModel.addRow(new Object[]{s.toString(), minimumQuantity, divisionQuantity, minimumCost, costIncrement});
        }

        table.setAutoCreateRowSorter(true);

        scrollPane = new JScrollPane(table);
        scrollPane.setBounds(20, 20, 640, 360); // table takes most of the height
        scrollPane.setFocusable(false);
        frame.add(scrollPane);

        // Add Button
        addButton = new JButton("Προσθήκη");
        addButton.addActionListener(e -> tableModel.addRow(new Object[]{"", "", "", "", ""}));
        addButton.setBounds(60, 400, 140, 35);
        addButton.setBackground(new Color(70, 130, 180));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        frame.add(addButton);

        // Delete Button
        deleteButton = new JButton("Διαγραφή");
        deleteButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(table.convertRowIndexToModel(selectedRow));
            }
        });
        deleteButton.setBounds(280, 400, 140, 35);
        deleteButton.setBackground(new Color(220, 53, 69));
        deleteButton.setForeground(Color.WHITE);
        deleteButton.setFocusPainted(false);
        frame.add(deleteButton);

        // Save Button
        saveButton = new JButton("Αποθήκευση");
        saveButton.setBounds(500, 400, 140, 35);
        saveButton.setBackground(new Color(40, 167, 69));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.addActionListener(e -> saveTableToFile());
        frame.add(saveButton);

        frame.setLocationRelativeTo(null); // center on screen
        frame.setVisible(true);
    }
	
	public static List<GalenicType> loadTypesFromFile(){
		File file = getOrCreateExternalTypesCsv();
		List<GalenicType> typesList = new ArrayList<>();
		
		try (BufferedReader reader = new BufferedReader(new FileReader(file)))
		{
			String line;
			while ((line = reader.readLine())!= null) {
				String[] parts = line.split(",",5);

				String GalenicTypeName = parts[0].trim();
				Float minimumQuantity = Float.parseFloat(parts[1].trim().replace(",", "."));
				Float divisionQuantity = Float.parseFloat(parts[2].trim().replace(",", "."));
				Float minimumCost = Float.parseFloat(parts[3].trim().replace(",", "."));
				Float costIncrement = Float.parseFloat(parts[4].trim().replace(",", "."));
				typesList.add(new GalenicType(GalenicTypeName, minimumQuantity, divisionQuantity, minimumCost, costIncrement));
			}
		} catch (IOException | NumberFormatException e) {
            System.err.println("Could not read file: " + e.getMessage());
        }
		return typesList;
		
	}
	
	public void saveTableToFile() {
	    File file = getOrCreateExternalTypesCsv();

	    try (PrintWriter writer = new PrintWriter(new FileWriter(file))) {
	        for (int i = 0; i < tableModel.getRowCount(); i++) {
	            String name = tableModel.getValueAt(i, 0).toString().trim();
	            String minQty = tableModel.getValueAt(i, 1).toString().trim().replace(",", ".");
	            String divQty = tableModel.getValueAt(i, 2).toString().trim().replace(",", ".");
	            String minCost = tableModel.getValueAt(i, 3).toString().trim().replace(",", ".");
	            String costIncr = tableModel.getValueAt(i, 4).toString().trim().replace(",", ".");

	            if (!name.isEmpty() && !minQty.isEmpty() && !divQty.isEmpty() && !minCost.isEmpty() && !costIncr.isEmpty()) {
	                writer.println(name + "," + minQty + "," + divQty + "," + minCost + "," + costIncr);
	            }
	        }

	        JOptionPane.showMessageDialog(frame, "Οι αλλαγές αποθηκεύτηκαν!", "Επιτυχία", JOptionPane.INFORMATION_MESSAGE);
	        frame.dispose();
	    } catch (IOException e) {
	        JOptionPane.showMessageDialog(frame, "Σφάλμα κατά την αποθήκευση του αρχείου.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
	    }
	}

	
    public static File getOrCreateExternalTypesCsv() {
        // Get the path for the AppData Roaming folder
        String appDataFolder = System.getenv("APPDATA");
        File externalCsvDir = new File(appDataFolder, "Galinika");  // Folder where substances.csv will be stored
        File externalCsv = new File(externalCsvDir, "types.csv");

        // Check if the directory exists, if not, create it
        if (!externalCsvDir.exists()) {
            boolean dirsCreated = externalCsvDir.mkdirs();  // This will create the folder if it doesn't exist
            if (!dirsCreated) {
                System.err.println("Failed to create directory: " + externalCsvDir.getAbsolutePath());
                return null;  // Return null or handle the error as needed
            }
        }

        // Now check if the file exists, and if not, copy it from resources
        if (!externalCsv.exists()) {
            try (InputStream in = SubstanceLibrary.class.getClassLoader().getResourceAsStream("types.csv")) {
                if (in == null) {
                    throw new FileNotFoundException("Default types.csv not found in resources!");
                }
                // Copy the file from resources to the target directory
                Files.copy(in, externalCsv.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return externalCsv;
    }
	
}
