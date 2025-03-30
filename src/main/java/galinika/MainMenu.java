package galinika;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;



public class MainMenu {
	SubstanceLibrary substanceLibrary;
	GalenicTypesLibrary typesLibrary;
	public JMenuBar createMenu(JFrame frame) {
		
		JMenuBar menuBar = new JMenuBar();
		
		JMenu fileMenu = new JMenu("Αρχείο");
		JMenuItem exitItem = new JMenuItem("Εξοδος");
		exitItem.addActionListener(e-> System.exit(0));
		
		JMenu infoMenu = new JMenu("Βιβλιοθήκη");
		JMenuItem updateSubstances = new JMenuItem("Επεξεργασία Συστατικών");
		updateSubstances.addActionListener(e-> substanceLibrary = new SubstanceLibrary());
		JMenuItem updateGalenicTypes = new JMenuItem("Επεξεργασία Τύπων Γαληνικού");
		updateGalenicTypes.addActionListener(e-> typesLibrary = new GalenicTypesLibrary());
		
		
		fileMenu.add(exitItem);
		infoMenu.add(updateSubstances);
		infoMenu.add(updateGalenicTypes);
		
		menuBar.add(fileMenu);
		menuBar.add(infoMenu);
		
		return menuBar;
		
	}

}
