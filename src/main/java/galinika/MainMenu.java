package galinika;

import javax.swing.*;

public class MainMenu {
  public JMenuBar createMenu(LaunchPage page) {
    JMenuBar bar = new JMenuBar();
    JMenu file = new JMenu("Αρχείο"), library = new JMenu("Βιβλιοθήκη");
    JMenuItem exit = new JMenuItem("Έξοδος"),
        substances = new JMenuItem("Επεξεργασία Συστατικών"),
        types = new JMenuItem("Επεξεργασία Τύπων Γαληνικού");
    exit.addActionListener(
        e ->
            page.frame.dispatchEvent(
                new java.awt.event.WindowEvent(
                    page.frame, java.awt.event.WindowEvent.WINDOW_CLOSING)));
    substances.addActionListener(e -> new SubstanceLibrary(page::invalidateTotal));
    types.addActionListener(e -> new GalenicTypesLibrary(page::reloadTypes));
    file.add(exit);
    library.add(substances);
    library.add(types);
    bar.add(file);
    bar.add(library);
    return bar;
  }
}
