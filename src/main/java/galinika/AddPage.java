package galinika;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


public class AddPage {

    // UI
    private JFrame frame = new JFrame();
    private MainMenu menu = new MainMenu();
    private LaunchPage launchPage;

    // Substances
    private List<Substance> substancesList;
    private JComboBox<Substance> substanceComboBox;

    // UI elements
    private JLabel titleLabel = new JLabel("Προσθήκη συστατικού");
    private JLabel substanceLabel = new JLabel("Συστατικό");
    private JLabel quantityLabel = new JLabel("Ποσότητα");
    private JTextField quantityField = new JTextField();
    private JButton confirmButton = new JButton("Επιβεβαίωση");
    private Font labelFont = new Font("Arial", Font.PLAIN, 14);
    JComboBox<String> galenicQuantityTypesBox = new JComboBox<>(new String[]{"gr", "ml"});

    public AddPage(LaunchPage page) {
        this.launchPage = page;

        // Load substances from persistent file and sort alphabetically
        substancesList = SubstanceLibrary.loadFromFile();
        substancesList.sort((s1, s2) -> s1.getName().compareToIgnoreCase(s2.getName()));

        // Initialize combo box
        substanceComboBox = new JComboBox<>(substancesList.toArray(new Substance[0]));

        // Frame setup
        frame.setSize(360, 360);
        URL iconUrl = getClass().getResource("/galinos.png");
        if (iconUrl != null) {
            frame.setIconImage(new ImageIcon(iconUrl).getImage());
        } else {
            System.err.println("Icon not found!");
        };
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(null);
        frame.setResizable(false);
        frame.getContentPane().setBackground(Color.WHITE);
        frame.setJMenuBar(menu.createMenu(frame));

        // Title
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titleLabel.setBounds(30, 20, 300, 30);
        frame.add(titleLabel);

        // Substance label and combo box
        substanceLabel.setFont(labelFont);
        substanceLabel.setBounds(30, 70, 120, 25);
        frame.add(substanceLabel);

        substanceComboBox.setBounds(130, 70, 160, 25);
        frame.add(substanceComboBox);

        // Quantity
        quantityLabel.setFont(labelFont);
        quantityLabel.setBounds(30, 110, 80, 25);
        frame.add(quantityLabel);
        
        galenicQuantityTypesBox.setBounds(235,110,55,25);
        frame.add(galenicQuantityTypesBox);

        quantityField.setBounds(130, 110, 100, 25);
        frame.add(quantityField);

        // Confirm button
        confirmButton.setBounds(30, 150, 120, 25);
        confirmButton.addActionListener(e -> buttonAction());
        frame.add(confirmButton);
        
        quantityField.addKeyListener(new KeyAdapter() {
        	@Override
        	public void keyPressed(KeyEvent e)
        	{
        		if (e.getKeyCode() == KeyEvent.VK_ENTER)
        		{
        			confirmButton.doClick();
        		}
        	}
		});

        frame.setVisible(true);
    }

    public boolean isOpen() {
        return frame.isActive();
    }

    private void buttonAction() {
        Substance selectedSubstance = (Substance) substanceComboBox.getSelectedItem();

        if (selectedSubstance == null) {
            JOptionPane.showMessageDialog(frame, "Δεν έχει επιλεγεί συστατικό.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        String quantityText = quantityField.getText().trim().replace(",", ".");

        if (quantityText.isEmpty()) {
            JOptionPane.showMessageDialog(frame, "Το πεδίο ποσότητας είναι κενό.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            float quantity = Float.parseFloat(quantityText);
            float endPrice = quantity * selectedSubstance.getPricePerQuantity();

            launchPage.addToTable(new Object[]{
                selectedSubstance.getName(), 
                new String(quantityField.getText()+" "+galenicQuantityTypesBox.getSelectedItem()),  // Show original format (with commas if needed)
                String.format("%.2f €", endPrice)
            });

            frame.dispose();

        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(frame, "Παρακαλώ εισάγετε έγκυρη αριθμητική ποσότητα.", "Σφάλμα", JOptionPane.ERROR_MESSAGE);
        }
    }

}
