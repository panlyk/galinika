package galinika;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;


import java.awt.*;
import java.net.URL;
import java.util.List;

public class LaunchPage {

    JFrame frame = new JFrame();
    MainMenu mainMenu = new MainMenu();

    JLabel titleLabel = new JLabel("Δημιουργία Γαληνικού");

    JLabel galenicTypeLabel = new JLabel("Τύπος Γαληνικού:");
    List<GalenicType> typesList = GalenicTypesLibrary.loadTypesFromFile();
    JComboBox<GalenicType> galenicTypeComboBox = new JComboBox<>(typesList.toArray(new GalenicType[0]));

    JLabel galenicQuantityLabel = new JLabel("Συνολική Ποσότητα:");
    JTextField galenicQuantityField = new JTextField();
    JComboBox<String> galenicQuantityTypesBox = new JComboBox<>(new String[]{"gr", "ml"});

    JButton addButton = new JButton("Προσθήκη Συστατικού");
    JButton removeButton = new JButton("Διαγραφή");
    JButton removeAllButton = new JButton("Διαγραφή Όλων");

    String[] columnNames = {"Όνομα", "Ποσότητα", "Τιμή"};
    DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
    JTable table = new JTable(tableModel);
    JScrollPane scrollPane = new JScrollPane(table);

    JLabel restLabel = new JLabel("Άλλο κόστος (€):");
    JTextField restField = new JTextField();

    JButton calculateButton = new JButton("Υπολογισμός Συνόλου");
    
    JLabel totalPriceJLabel = new JLabel("-€");

    Font labelFont = new Font("Segoe UI", Font.PLAIN, 14);
    Font titleFont = new Font("Segoe UI", Font.BOLD, 22);
    Font buttonFont = new Font("Segoe UI", Font.BOLD, 13);

    private AddPage addpage = null;

    public LaunchPage() {
        frame.setSize(375, 650);
        URL iconUrl = getClass().getResource("/galinos.png");
        if (iconUrl != null) {
            frame.setIconImage(new ImageIcon(iconUrl).getImage());
        } else {
            System.err.println("Icon not found!");
        }
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(null);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setJMenuBar(mainMenu.createMenu(frame));

        // Title
        titleLabel.setFont(titleFont);
        titleLabel.setBounds(30, 20, 300, 30);
        frame.add(titleLabel);

        // Galenic Type
        galenicTypeLabel.setFont(labelFont);
        galenicTypeLabel.setBounds(30, 70, 120, 25);
        frame.add(galenicTypeLabel);


        galenicTypeComboBox.setBounds(175, 70, 150, 25);
        frame.add(galenicTypeComboBox);

        // Add Button
        addButton.setFont(buttonFont);
        addButton.setBounds(30, 110, 180, 25);
        addButton.setBackground(new Color(70, 130, 180));
        addButton.setForeground(Color.WHITE);
        addButton.setFocusPainted(false);
        addButton.addActionListener(e -> addpage = new AddPage(this));
        frame.add(addButton);

        // Table
        table.setAutoCreateRowSorter(true);
        scrollPane.setBounds(30, 150, 300, 140);
        frame.add(scrollPane);

        // Remove Button
        removeButton.setFont(buttonFont);
        removeButton.setBounds(30, 300, 140, 25);
        removeButton.setBackground(new Color(220, 53, 69));
        removeButton.setForeground(Color.WHITE);
        removeButton.setFocusPainted(false);
        removeButton.addActionListener(e -> {
            int selectedRow = table.getSelectedRow();
            if (selectedRow != -1) {
                tableModel.removeRow(table.convertRowIndexToModel(selectedRow));
            }
        });
        frame.add(removeButton);

        // Delete All Button
        JButton deleteAllButton = new JButton("Διαγραφή Όλων");
        deleteAllButton.setFont(buttonFont);
        deleteAllButton.setBounds(190, 300, 140, 25);
        deleteAllButton.setBackground(new Color(220, 53, 69));
        deleteAllButton.setForeground(Color.WHITE);
        deleteAllButton.setFocusPainted(false);
        deleteAllButton.addActionListener(e -> {
            if (tableModel.getRowCount() > 0) {
                int confirm = JOptionPane.showConfirmDialog(frame, "Θέλετε να διαγράψετε όλα τα υλικά;", "Επιβεβαίωση", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    tableModel.setRowCount(0);
                }
            }
        });
        frame.add(deleteAllButton);

        // Quantity 
        galenicQuantityLabel.setFont(labelFont);
        galenicQuantityLabel.setBounds(30, 350, 140, 25);
        frame.add(galenicQuantityLabel);
        
        galenicQuantityField.setBounds(165, 350, 110, 25);
        frame.add(galenicQuantityField);
        
        galenicQuantityTypesBox.setBounds(285, 350, 45, 25);
        frame.add(galenicQuantityTypesBox);



        // Other cost
        restLabel.setFont(labelFont);
        restLabel.setBounds(30, 390, 150, 25);
        frame.add(restLabel);

        restField.setBounds(165, 390, 110, 25);
        frame.add(restField);

        // Calculate Button
        calculateButton.setFont(buttonFont);
        calculateButton.setBounds(30, 450, 180, 30);
        calculateButton.setBackground(new Color(40, 167, 69));
        calculateButton.setForeground(Color.WHITE);
        calculateButton.setFocusPainted(false);
        calculateButton.addActionListener(e -> calculateTotal());
        frame.add(calculateButton);

        // Total Price Label
        // Total Price Label - emphasized
        totalPriceJLabel.setBounds(30, 495, 100, 35);
        totalPriceJLabel.setHorizontalAlignment(SwingConstants.CENTER);
        totalPriceJLabel.setFont(new Font("Segoe UI", Font.BOLD, 18)); // Larger and bold
        totalPriceJLabel.setForeground(new Color(0, 128, 0)); // Dark green
        totalPriceJLabel.setOpaque(true);
        totalPriceJLabel.setBackground(new Color(235, 255, 235)); // Very light green
        totalPriceJLabel.setBorder(BorderFactory.createLineBorder(new Color(0, 128, 0), 1)); // Green border
        frame.add(totalPriceJLabel);



        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }


    public void addToTable(Object[] rowdata) {
        tableModel.addRow(rowdata);
    }
    
    
    
    private float calculateTieredCost (float minimumQuantity, float divisionQuantity, float minimumCost, float costIncrement, float quantity)
    {
    	float quantityOverMinimum = (quantity-minimumQuantity);
    	if (quantityOverMinimum <= 0) {
    		return minimumCost;
    	} else if (Math.abs(quantityOverMinimum % divisionQuantity) < 0.001f) {
    		
    		return minimumCost + (quantityOverMinimum/divisionQuantity)*costIncrement;
    	} else {
    		int times = (int) (quantityOverMinimum / divisionQuantity);
    		return minimumCost + (times+1) * costIncrement;
    	}
    }
    
    private void calculateTotal() {
    	String galenicQuantityString = galenicQuantityField.getText().trim().replace(",", ".");
    	float galenicQuantity = 0;
    	if (!galenicQuantityString.isEmpty()) {
    		galenicQuantity = Float.parseFloat(galenicQuantityField.getText().trim().replace(",", "."));
    	} else {
			JOptionPane.showMessageDialog(frame, "Μη έγκυρη ποσότητα Γαληνικού", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
		}
    	
    	GalenicType type = (GalenicType) galenicTypeComboBox.getSelectedItem();
    	float total = 0;
    	
    	//calculate quantity cost
    	try {float quantityCost = calculateTieredCost(type.getMinimumQuantity(), type.getDivisionQuantity(), type.getMinimumCost(), type.getCostIncrement(), galenicQuantity);
    	total += quantityCost;}
    	catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Παρακαλώ εισάγετε έναν έγκυρο αριθμό στο πεδίο 'Ποσότητα'.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
			return;
		}
    	
    	//calculate substance cost
    	float substancesCost = 0;
    	for (int row = 0; row < tableModel.getRowCount(); row++) {
    	    String priceString = ((String) tableModel.getValueAt(row, 2)).replace("€", "").replace(",", ".").trim();
    	    if (!priceString.isEmpty()) {
    	    	substancesCost += Float.parseFloat(priceString);
    	    }
    	    
    	}
    	total += substancesCost;
    	
    	//calculate other costs
    	try {float otherCost = Float.parseFloat(restField.getText().replace(",", ".")); 
    	total += otherCost;}
    	catch (NumberFormatException e) {
			float otherCost = 0;
		}
		
		//change the label
		totalPriceJLabel.setText (String.format("%.2f €", total));
    }
}