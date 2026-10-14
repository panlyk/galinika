package galinika;

import java.awt.*;
import java.util.List;
import javax.swing.*;

/** Shared application icon and original button styling. */
final class AppIcon {
  private AppIcon() {}

  static void apply(Window window) {
    var resource = AppIcon.class.getResource("/galinos.png");
    if (resource == null) throw new IllegalStateException("Missing Galinika icon");
    Image image = new ImageIcon(resource).getImage();
    window.setIconImages(
        List.of(
            image.getScaledInstance(16, 16, Image.SCALE_SMOOTH),
            image.getScaledInstance(32, 32, Image.SCALE_SMOOTH),
            image.getScaledInstance(48, 48, Image.SCALE_SMOOTH),
            image));
  }

  static void button(JButton button, Color color) {
    button.setFont(new Font("Segoe UI", Font.BOLD, 13));
    button.setBackground(color);
    button.setForeground(Color.WHITE);
    button.setFocusPainted(false);
  }
}
